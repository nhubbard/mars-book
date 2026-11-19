"""Student record controls retain instructions, storage extents and honest observations."""
import importlib.util,json,re,struct,subprocess,sys,tempfile,unittest,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'drafts/video-record-construction'
def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
FIX=load('record_fixture_tests',D/'fixtures.py');PREP=load('record_prepare_tests',D/'prepare.py')
class RecordConstructionTests(unittest.TestCase):
 def test_valid_records_match_exact_known_frames(self):
  for name in FIX.video.NAMES:
   m=FIX.video.material(name)
   for n,info in enumerate(m['directory']):
    r=FIX.material(name,n);raw=r['record'];self.assertEqual(m['frames'][n],r['after']);self.assertEqual(m['frames'][n-1]if n else bytes([255,0,255])*4096,r['before']);self.assertEqual(info['record_bytes'],len(raw));self.assertEqual(zlib.crc32(raw[:12]+raw[16:]),int.from_bytes(raw[12:16],'big'))
 def test_late_overflow_has_valid_crc_and_valid_changed_prefix(self):
  r=FIX.material(case='late-skip');raw=r['record'];self.assertEqual(r['before'],r['after']);self.assertEqual(zlib.crc32(raw[:12]+raw[16:]),int.from_bytes(raw[12:16],'big'));self.assertEqual((0,1),struct.unpack_from('>HH',raw,16));self.assertEqual(bytes([1,0,0]),raw[20:23]);self.assertEqual((4096,0),struct.unpack_from('>HH',raw,23));self.assertEqual(4097,1+struct.unpack_from('>H',raw,23)[0])
 def test_bad_crc_changes_only_checksum(self):
  good=FIX.material()['record'];bad=FIX.material(case='bad-crc')['record'];self.assertEqual(good[:12]+good[16:],bad[:12]+bad[16:]);self.assertNotEqual(zlib.crc32(bad[:12]+bad[16:]),int.from_bytes(bad[12:16],'big'))
 def test_all_recipes_preserve_student_instruction_text(self):
  src=(D/'main.asm').read_text()+'\nstudent_extra:\n    daddiu $t0, $t0, 73\n';code=src.split('\n.text\n')[1];recipes=json.loads((D/'inputs.json').read_text())['recipes'];self.assertEqual(37,len(recipes))
  for r in recipes:
   with self.subTest(recipe=r['id']):
    prepared=PREP.prepare(src,r);self.assertEqual(code,prepared.split('\n.text\n')[1]);self.assertIn(r['checkpoint']+':',prepared);self.assertEqual(1,prepared.count('record_count: .dword '));self.assertIn('.byte -91:60\n.align 3\nrecord_context:',prepared)
 def test_controls_reject_unsigned_type_errors_and_missing_anchors(self):
  src=(D/'main.asm').read_text();r=dict(fixture='moving',index=1)
  for value in(-1,2**64,True,'1',1.5):
   for key in('capacity','sequence','period','declaredCount'):
    with self.subTest(key=key,value=value),self.assertRaises(ValueError):PREP.prepare(src,dict(r,**{key:value}))
  for bad in(src+'\n.text\n',src.replace('record_context:','changed:'),src.replace('video_apply_record:','changed:'),src.replace('record_data:','changed:')):
   with self.assertRaises(ValueError):PREP.prepare(bad,r)
 def test_steps_have_native_explanations_quizzes_and_stable_regions(self):
  steps=json.loads((D/'steps.json').read_text())['steps'];self.assertEqual([f's{n:02}'for n in range(7,24)],[s['id']for s in steps]);src=(D/'main.asm').read_text();self.assertEqual([s['region']for s in steps],re.findall(r'^# BEGIN (\w+)$',src,re.M))
  for s in steps:
   text=(ROOT/f"includes/chapters/034-lossless-video/record-{s['id']}.tex").read_text();self.assertIn(s['recipe'],text);self.assertIn(s['checkpoint'].replace('_',r'\_'),text);self.assertIn(s['quiz']['question'],text);self.assertEqual(1,text.count(r'\paragraph{Observe}'));self.assertEqual(1,text.count(r'\paragraph{Check}'));self.assertEqual(3,len(s['quiz']['choices']));self.assertEqual(2,len(s['hints']));self.assertNotIn('answer',s['quiz'])
 def test_cli_exclusivity_retains_existing_work(self):
  with tempfile.TemporaryDirectory()as td:
   out=Path(td)/'work';out.write_text('student work')
   for args in (['prepare.py','s07-1','--output',str(out)],['fixtures.py','--output',str(out)]):
    result=subprocess.run([sys.executable,str(D/args[0]),*args[1:]],capture_output=True,text=True);self.assertNotEqual(0,result.returncode);self.assertEqual('student work',out.read_text())
if __name__=='__main__':unittest.main()
