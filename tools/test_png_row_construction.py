"""Public row construction controls retain student work and encode exact observations."""
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest
import zlib

ROOT=Path(__file__).resolve().parents[1]
D=ROOT/'drafts/png-row-construction'
def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
ATTACH=load('row_attach_tests',D/'attach.py');PREP=load('row_prep_tests',D/'prepare.py');FIX=load('row_fixture_tests',D/'fixtures.py')
def source():
 return '.data\nimage_path: .asciiz "image.png"\n    .space 118\nimage_path_end:\nvector_mode: .word 1\nfile_capacity: .dword 1048576\ncollector_capacity: .dword 46\n.text\n'+(ROOT/'drafts/png-inflater-construction/wrapper.asm').read_text()+'png_inflate:\n    nop\nbuild_tree:\n    nop\ndecode_symbol:\n    nop\nstudent_extra:\n    daddiu $t0, $t0, 23\n'
class RowConstructionTests(unittest.TestCase):
 def test_attach_preserves_student_algorithms(self):
  old=source();new=ATTACH.attach(old)
  self.assertIn(old[old.index('build_tree:'):],new);self.assertIn((D/'unfilter.asm').read_text(),new)
  self.assertEqual(1,new.count('png_decode_msa:'));self.assertIn('jal png_unfilter_msa',new)
  self.assertNotIn('png_blit:',new)
 def test_conflicting_and_incomplete_attachments_are_refused(self):
  for old in (source().replace('build_tree:','changed:'),source().replace('decode_symbol:','changed:'),source()+'png_unfilter:\n',source()+'row_decode_entry:\n',source()+'\n.text\n',source().replace('inflate_call:','changed:')):
   with self.subTest(old=old[-40:]),self.assertRaises(ValueError):ATTACH.attach(old)
  with self.assertRaises(ValueError):ATTACH.attach(ATTACH.attach(source()))
 def test_all_controls_preserve_instruction_text(self):
  src=ATTACH.attach(source());code=src.split('\n.text\n')[1];recipes=json.loads((D/'inputs.json').read_text())['recipes'];self.assertEqual(19,len(recipes))
  for r in recipes:
   prepared=PREP.prepare(src,r);self.assertEqual(code,prepared.split('\n.text\n')[1]);self.assertIn('.byte '+','.join(str(v) for v in r['path'].encode()+b'\0'),prepared);self.assertIn(r['checkpoint']+':',prepared)
  with self.assertRaises(ValueError):PREP.prepare(source(),recipes[0])
 def test_six_inputs_have_valid_container_and_compression(self):
  for name in FIX.NAMES:
   with self.subTest(name=name):
    m=FIX.material(name);data=m['png'];self.assertEqual(bytes.fromhex('89504e470d0a1a0a'),data[:8]);at=8;joined=b'';types=[]
    while at<len(data):
     n=int.from_bytes(data[at:at+4],'big');kind=data[at+4:at+8];payload=data[at+8:at+8+n];crc=int.from_bytes(data[at+8+n:at+12+n],'big');self.assertEqual(zlib.crc32(kind+payload),crc);types.append(kind)
     if kind==b'IDAT':joined+=payload
     at+=12+n
    self.assertEqual(at,len(data));self.assertEqual([b'IHDR',b'IDAT',b'IDAT',b'IEND'],types);self.assertEqual(m['filtered'],zlib.decompress(joined));self.assertEqual(m['height']*(1+3*m['width']),len(m['filtered']))
    if name=='bad-selector':self.assertEqual(5,m['filtered'][28])
    elif name.startswith('up'):
     w=m['width'];self.assertEqual(bytes([2]+[250]*(3*w)+[2]+[10]*(3*w)),m['filtered']);self.assertEqual(bytes([250]*(3*w)+[4]*(3*w)),m['rgb'])
 def test_native_steps_and_quizzes_have_stable_ids(self):
  steps=json.loads((D/'steps.json').read_text())['steps'];self.assertEqual([f's{n}'for n in range(71,84)],[s['id']for s in steps]);regions=re.findall(r'^# BEGIN (\w+)$',(D/'unfilter.asm').read_text(),re.M);self.assertEqual(13,len(regions));self.assertEqual(set(regions),{s['region']for s in steps})
  for s in steps:
   native=(ROOT/f"includes/chapters/033-png/row-{s['id']}.tex").read_text();self.assertIn(s['recipe'],native);self.assertIn(s['checkpoint'].replace('_',r'\_'),native);self.assertEqual(3,len(s['quiz']['choices']));self.assertEqual(2,len(s['hints']));self.assertNotIn('answer',s['quiz'])
 def test_existing_cli_outputs_are_refused(self):
  with tempfile.TemporaryDirectory()as directory:
   p=Path(directory);src=p/'input.asm';src.write_text(source());out=p/'existing';out.write_text('student data')
   for cmd in ([sys.executable,str(D/'attach.py'),'--source',str(src),'--output',str(out)],[sys.executable,str(D/'fixtures.py'),'worked','--output',str(out)]):
    r=subprocess.run(cmd,capture_output=True,text=True);self.assertNotEqual(0,r.returncode);self.assertEqual('student data',out.read_text())
if __name__=='__main__':unittest.main()
