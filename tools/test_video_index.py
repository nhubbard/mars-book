"""Index exercises preserve student work and distinguish inputs from published results."""
import importlib.util,json,re,struct,subprocess,sys,tempfile,unittest,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'drafts/video-index-construction'
def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
ATTACH=load('index_test_attachment',D/'attach.py');PREP=load('index_test_preparer',D/'prepare.py');FIX=load('index_test_fixture',D/'fixtures.py')
def source():return(ROOT/'drafts/video-record-construction/main.asm').read_text()+'\nstudent_extra:\n    daddiu $t0, $t0, 71\n'
class IndexConstructionTests(unittest.TestCase):
 def test_attachment_keeps_all_record_instructions(self):
  src=source();new=ATTACH.attach(src);old=src.split('\n.text\n')[1].replace('main:','record_example_main:',1);self.assertTrue(new.endswith(old));self.assertEqual(1,new.count('\nvideo_apply_record:\n'));self.assertEqual(1,new.count('\nvideo_index:\n'));self.assertEqual(1,new.count('\nmain:\n'));self.assertIn('and $sp, $sp, $t0',new)
 def test_conflicts_and_ambiguous_source_are_refused(self):
  for src in(source()+'\n.text\n',source().replace('video_apply_record:','changed:'),source()+'\nindex_other:\n',source()+'\nrecord_example_main:\n',ATTACH.attach(source())):
   with self.assertRaises(ValueError):ATTACH.attach(src)
 def test_all_recipes_retain_every_instruction(self):
  src=ATTACH.attach(source());code=src.split('\n.text\n')[1];recipes=json.loads((D/'inputs.json').read_text())['recipes'];self.assertEqual(25,len(recipes))
  for r in recipes:
   result=PREP.prepare(src,r);self.assertEqual(code,result.split('\n.text\n')[1]);self.assertIn(r['checkpoint']+':',result);self.assertEqual(1,result.count('index_context: .dword '));self.assertIn('index_context_guard: .byte -91:32',result)
 def test_unsigned_controls_and_required_labels(self):
  src=ATTACH.attach(source());r=dict(fixture='moving')
  for v in(-1,2**64,True,'2',2.5):
   for key in('capacity','scratchCapacity','declaredCount'):
    with self.subTest(key=key,value=v),self.assertRaises(ValueError):PREP.prepare(src,dict(r,**{key:v}))
  for bad in(src.replace('index_file_count:','changed:'),src.replace('index_context:','changed:'),src.replace('index_file_data:','changed:'),src+'\n.text\n'):
   with self.assertRaises(ValueError):PREP.prepare(bad,r)
 def test_public_defects_isolate_late_record_validation(self):
  good=FIX.material();self.assertEqual(24795,len(good['file']));self.assertTrue(good['valid']);self.assertEqual(good['file']+b'X',FIX.material(case='file-suffix')['file']);late=FIX.material(case='late-command');at=late['directory'][-1]['offset'];raw=late['file'][at:];self.assertEqual(7,int.from_bytes(raw[4:8],'big'));self.assertEqual(zlib.crc32(raw[:12]+raw[16:]),int.from_bytes(raw[12:16],'big'));self.assertEqual((0,1),struct.unpack_from('>HH',raw,16));self.assertEqual((4096,0),struct.unpack_from('>HH',raw,23));self.assertFalse(late['valid'])
  bad=FIX.material(case='late-crc')['file'];self.assertEqual(good['file'][:at+12],bad[:at+12]);self.assertNotEqual(good['file'][at+12:at+16],bad[at+12:at+16])
 def test_native_steps_cover_regions_and_explanatory_checks(self):
  steps=json.loads((D/'steps.json').read_text())['steps'];self.assertEqual([f's{n}'for n in range(24,37)],[s['id']for s in steps]);self.assertEqual([s['region']for s in steps],re.findall(r'^# BEGIN (\w+)$',(D/'index.asm').read_text(),re.M))
  for s in steps:
   text=(ROOT/f"includes/chapters/034-lossless-video/index-{s['id']}.tex").read_text();self.assertIn(s['recipe'],text);self.assertIn(s['checkpoint'].replace('_',r'\_'),text);self.assertIn(s['quiz']['question'],text);self.assertEqual(1,text.count(r'\paragraph{Observe}'));self.assertEqual(1,text.count(r'\paragraph{Check}'));self.assertEqual(3,len(s['quiz']['choices']));self.assertEqual(2,len(s['hints']));self.assertNotIn('answer',s['quiz'])
 def test_cli_existing_outputs_remain_untouched(self):
  with tempfile.TemporaryDirectory()as td:
   p=Path(td);src=p/'record.asm';src.write_text(source());attached=p/'attached.asm';attached.write_text(ATTACH.attach(source()));out=p/'existing';out.write_text('student work')
   for args in(['attach.py','--source',str(src),'--output',str(out)],['prepare.py','s24-1','--source',str(attached),'--output',str(out)],['fixtures.py','moving','--output',str(out)]):
    r=subprocess.run([sys.executable,str(D/args[0]),*args[1:]],capture_output=True,text=True);self.assertNotEqual(0,r.returncode);self.assertEqual('student work',out.read_text())
if __name__=='__main__':unittest.main()
