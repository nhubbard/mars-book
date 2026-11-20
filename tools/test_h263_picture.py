import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
ROOT=Path(__file__).resolve().parents[1]
STAGE=ROOT/'drafts/h263-picture-construction'
def load(name):
    s=importlib.util.spec_from_file_location('picture_test_'+name,STAGE/(name+'.py'));m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
MODEL=load('model');PREP=load('prepare');COMPOSE=load('compose');CHECK=load('check_stream')
def stream(frames=2):
    bits=''
    block=[[255]+[0]*63 for _ in range(6)]
    for frame in range(frames):
        picture=int(frame!=0);bits+=MODEL.header(picture,frame*6,8,picture)
        for row in range(9):
            if row:
                bits+='0'*((-len(bits))%8)+format(1,'017b')+format(row,'05b')+format(picture,'02b')+format(8,'05b')
            bits+=(MODEL.MACRO.encode(block)if not picture else '1')*11
        bits+='0'*((-len(bits))%8)
    return int(bits,2).to_bytes(len(bits)//8,'big')
class PictureToolsTest(unittest.TestCase):
    def test_geometry_covers_every_sample_once(self):
        seen=[]
        for row in range(9):
            for col in range(11):
                for block in range(6):
                    at,stride=MODEL.block_origin(row,col,block)
                    seen.extend(at+y*stride+x for y in range(8)for x in range(8))
        self.assertEqual(sorted(seen),list(range(38016)))
    def test_geometry_rejects_outside_and_noninteger(self):
        for args in [(-1,0,0),(9,0,0),(0,11,0),(0,0,6),(False,0,0)]:
            with self.assertRaises(ValueError):MODEL.block_origin(*args)
    def test_two_complete_pictures(self):
        pictures=MODEL.inspect_stream(stream());self.assertEqual([p['bytes']for p in pictures],[696,48]);self.assertEqual([p['temporalReference']for p in pictures],[0,6])
        self.assertTrue(all(b['blocks'][0][0]==255 for b in pictures[0]['macroblocks']))
        self.assertTrue(all(b['skip']and b['motion']==(0,0)for b in pictures[1]['macroblocks']))
    def test_truncated_and_trailing_streams_rejected(self):
        data=stream()
        for bad in [b'',data[:3],data[:-1],data+b'\xa5',data[1:]]:
            with self.assertRaises(ValueError):MODEL.inspect_stream(bad)
    def test_group_header_and_identity_errors_rejected(self):
        data=bytearray(stream());at=MODEL.inspect_stream(bytes(data))[1]['offset'];bits=''.join(format(b,'08b')for b in data)
        # P picture's first GOB starts after its 50-bit header, eleven skips and alignment.
        gob=at*8+64
        for pos in [gob+16,gob+22]:
            changed=bits[:pos]+str(1-int(bits[pos]))+bits[pos+1:]
            with self.assertRaises(ValueError):MODEL.inspect_stream(int(changed,2).to_bytes(len(data),'big'))
    def test_invalid_controls(self):
        for values in [(2,0,8,0),(0,256,8,0),(0,0,0,0),(0,0,32,0),(0,0,8,4),(False,0,8,0)]:
            with self.assertRaises(ValueError):MODEL.header(*values)
    def test_prepare_keeps_instructions(self):
        source=(STAGE/'main.asm').read_text();result=PREP.prepare(source,1,130,128,8,0,6,1)
        self.assertEqual(source.split('\n.text\n')[1],result.split('\n.text\n')[1]);self.assertIn('169088,1,6,8,0,1,0',result)
    def test_prepare_rejects_domains_and_changed_format(self):
        source=(STAGE/'main.asm').read_text()
        for kwargs in [dict(picture=2),dict(current=-1),dict(reference=256),dict(quant=0),dict(mode=3),dict(tr=256),dict(gfid=4)]:
            with self.assertRaises(ValueError):PREP.prepare(source,**kwargs)
        with self.assertRaises(ValueError):PREP.prepare(source.replace('picture_request:','removed:'))
    def test_compose_retains_students_entries(self):
        source=(STAGE/'main.asm').read_text();deps=['.data\n'+f'owned_{i}: .word {i}\n.text\nmain:\n nop\n{entry}:\n li $v0, {i}\n jr $ra\n nop\n'for i,entry in enumerate(COMPOSE.ENTRIES)]
        joined=COMPOSE.compose(source,deps);self.assertEqual(joined.count('\n.text\n'),1)
        for i,e in enumerate(COMPOSE.ENTRIES):self.assertEqual(joined.count(e+':'),1);self.assertIn(f'owned_{i}:',joined)
        self.assertIn(source.split('\n.text\n')[1].split(COMPOSE.MARKER)[0],joined)
    def test_compose_rejects_missing_group(self):
        with self.assertRaises(ValueError):COMPOSE.compose((STAGE/'main.asm').read_text(),[])
        with self.assertRaises(ValueError):COMPOSE.component('.text\nh263_search16:\n.text\n','h263_search16')
    def test_external_check_reports_quality_and_drift(self):
        with tempfile.TemporaryDirectory()as temp:
            d=Path(temp);encoded=d/'encoded.263';raw=d/'raw.yuv';local=d/'local.yuv';tool=d/'ffmpeg';encoded.write_bytes(stream());raw.write_bytes(bytes([128])*76032);local.write_bytes(raw.read_bytes());tool.write_text('fixture executable identity')
            def run(args,**kwargs):Path(args[-1]).write_bytes(raw.read_bytes());return type('Result',(),dict(returncode=0,stderr=''))()
            with patch.object(CHECK.shutil,'which',return_value=str(tool)),patch.object(CHECK.subprocess,'run',side_effect=run),patch.object(CHECK.subprocess,'check_output',return_value='test tool'):
                result=CHECK.check(encoded,raw,local,2,6);self.assertEqual(result['encodedBytes'],744);self.assertTrue(all(q['psnrDb']is None and q['maximumLocalDecoderDifference']==0 for q in result['quality']))
                local.write_bytes(bytes([0])*76032)
                with self.assertRaises(ValueError):CHECK.check(encoded,raw,local,2,6)
    def test_external_check_rejects_counts_before_decoding(self):
        with tempfile.TemporaryDirectory()as temp:
            d=Path(temp);encoded=d/'encoded';raw=d/'raw';local=d/'local';encoded.write_bytes(stream());raw.write_bytes(b'');local.write_bytes(b'')
            with self.assertRaises(ValueError):CHECK.check(encoded,raw,local,2,6)
