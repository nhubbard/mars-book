"""Full encoder inputs and preparation preserve student code and native contracts."""
import importlib.util,json,re,subprocess,sys,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'drafts/video-full-encode-construction'
def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
ATTACH=load('full_test_attach',D/'attach.py');PREP=load('full_test_prepare',D/'prepare.py');FIX=PREP.fixtures
INDEX=load('full_index',ROOT/'drafts/video-index-construction/attach.py');IO=load('full_io',ROOT/'drafts/video-io-construction/attach.py');SEEK=load('full_seek',ROOT/'drafts/video-seek-construction/attach.py')
def source():return SEEK.attach(IO.attach(INDEX.attach((ROOT/'drafts/video-record-construction/main.asm').read_text())))+'\nstudent_extra:\n    daddiu $t0, $t0, 79\n'
class VideoFullEncodeTests(unittest.TestCase):
 def test_attachment_retains_seeker_and_record_instructions(self):
  src=source();out=ATTACH.attach(src);self.assertTrue(out.endswith(src.split('\n.text\n')[1].replace('main:','seek_example_main:',1)));self.assertEqual(1,out.count('\nvideo_encode_full:\n'));self.assertEqual(1,out.count('\nmain:\n'));self.assertEqual(1,out.count('\nvideo_apply_record:\n'))
 def test_conflicting_and_ambiguous_labels_are_refused(self):
  for src in(source()+'\n.text\n',source().replace('video_seek:','changed:'),source().replace('video_apply_record:','changed:'),source()+'\nef_extra:\n',source()+'\nencode_extra:\n',source()+'\nseek_example_main:\n',ATTACH.attach(source())):
   with self.assertRaises(ValueError):ATTACH.attach(src)
 def test_recipes_change_data_and_preserve_every_instruction(self):
  src=ATTACH.attach(source());recipes=json.loads((D/'inputs.json').read_text())['recipes'];self.assertEqual(24,len(recipes));code=src.split('\n.text\n')[1]
  for r in recipes:
   out=PREP.prepare(src,r);self.assertEqual(code,out.split('\n.text\n')[1]);self.assertIn(r['checkpoint']+':',out);self.assertIn(FIX.declaration(r['fixture']),out)
 def test_unsigned_control_limits_and_missing_anchors(self):
  src=ATTACH.attach(source())
  for key in('sourceCount','capacity','sequence','period','previousCapacity'):
   for value in(-1,2**64,True,'1',1.5):
    with self.assertRaises(ValueError):PREP.prepare(src,{key:value})
  self.assertIn('encode_context: .dword -1, 0, 4, 0, 16384',PREP.prepare(src,dict(capacity=2**64-1)))
  for text in(src.replace('encode_source_end:','changed:'),src.replace('encode_source_count:','changed:'),src.replace('encode_context:','changed:'),src+'\n.text\n'):
   with self.assertRaises(ValueError):PREP.prepare(text,{})
 def test_color_fixtures_distinguish_invalid_high_byte(self):
  gradient=FIX.words();self.assertEqual(4096,len(gradient));self.assertEqual([0x123456,0x123557,0x123654,0x123755],gradient[:4]);self.assertEqual(0x023aa9,gradient[-1]);self.assertTrue(all(v<2**24 for v in gradient));self.assertEqual([0]*4096,FIX.words('zero'));self.assertEqual([0xffffff]*4096,FIX.words('white'))
  for name,index in(('invalid-first',0),('invalid-last',4095)):
   words=FIX.words(name);self.assertGreaterEqual(words[index],2**24);self.assertEqual([i for i,(a,b)in enumerate(zip(words,gradient))if a!=b],[index])
  with self.assertRaises(ValueError):FIX.words('unknown')
 def test_native_steps_have_exact_recipes_observations_and_quizzes(self):
  steps=json.loads((D/'steps.json').read_text())['steps'];self.assertEqual([f's{n}'for n in range(57,69)],[s['id']for s in steps]);self.assertEqual([s['region']for s in steps],re.findall(r'^# BEGIN (\w+)$',(D/'encoder.asm').read_text(),re.M))
  for s in steps:
   text=(ROOT/f"includes/chapters/034-lossless-video/full-encode-{s['id']}.tex").read_text();self.assertIn(s['recipe'],text);self.assertIn(s['checkpoint'].replace('_',r'\_'),text);self.assertIn(s['quiz']['question'],text);self.assertEqual(1,text.count(r'\paragraph{Observe}'));self.assertEqual(1,text.count(r'\paragraph{Check}'));self.assertEqual(3,len(s['quiz']['choices']));self.assertEqual(2,len(s['hints']));self.assertNotIn('answer',s['quiz']);self.assertNotIn('correctChoice',s['quiz'])
   for c in s['quiz']['choices']:self.assertIn(r'\item '+c['text'],text)
 def test_output_helpers_refuse_existing_student_work(self):
  with tempfile.TemporaryDirectory()as td:
   p=Path(td);src=p/'seek.asm';src.write_text(source());full=p/'encoder.asm';full.write_text(ATTACH.attach(source()));out=p/'existing';out.write_text('student work')
   for args in(['attach.py','--source',str(src),'--output',str(out)],['prepare.py','s57-1','--source',str(full),'--output',str(out)],['fixtures.py','gradient','--output',str(out)]):
    r=subprocess.run([sys.executable,str(D/args[0]),*args[1:]],capture_output=True,text=True);self.assertNotEqual(0,r.returncode);self.assertEqual('student work',out.read_text())
 def test_fixture_export_contains_numeric_words_only(self):
  with tempfile.TemporaryDirectory()as td:
   p=Path(td)/'colors.json';r=subprocess.run([sys.executable,str(D/'fixtures.py'),'gradient','--output',str(p)],capture_output=True,text=True);self.assertEqual(0,r.returncode,r.stderr);data=json.loads(p.read_text());self.assertEqual(set(data),{'fixture','numericWords'});self.assertEqual(FIX.words(),data['numericWords'])
if __name__=='__main__':unittest.main()
