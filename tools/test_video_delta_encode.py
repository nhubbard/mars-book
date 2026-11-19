"""Delta authoring controls preserve both earlier projects and native expectations."""
import importlib.util,json,re,subprocess,sys,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];D=ROOT/'drafts/video-delta-encode-construction'
def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
ATTACH=load('delta_test_attach',D/'attach.py');PREP=load('delta_test_prepare',D/'prepare.py');FIX=PREP.fixtures
INDEX=load('delta_index',ROOT/'drafts/video-index-construction/attach.py');IO=load('delta_io',ROOT/'drafts/video-io-construction/attach.py');SEEK=load('delta_seek',ROOT/'drafts/video-seek-construction/attach.py');FULL=load('delta_full',ROOT/'drafts/video-full-encode-construction/attach.py')
def source():return FULL.attach(SEEK.attach(IO.attach(INDEX.attach((ROOT/'drafts/video-record-construction/main.asm').read_text()))))+'\nstudent_extra:\n    daddiu $t0, $t0, 83\n'
def kernel():return(ROOT/'drafts/video-construction/kernel.asm').read_text()
class VideoDeltaEncodeTests(unittest.TestCase):
 def test_attachment_keeps_earlier_encoder_instructions(self):
  src=source();out=ATTACH.attach(src,kernel());self.assertTrue(out.endswith(src.split('\n.text\n')[1].replace('main:','full_encoder_example_main:',1)));self.assertEqual(1,out.count('\nvideo_encode_record:\n'));self.assertEqual(1,out.count('\nmain:\n'));self.assertEqual(1,out.count('\nvideo_xor_words_msa:\n'))
 def test_namespace_preserves_constants_opcodes_registers_and_comments(self):
  k=kernel().replace('\n.text\n','\n    data: .word 7\n  and: .word 9\n    t0: .asciiz "data and t0" # data and t0\nA: .byte \'A\'\n.text\n')+'\nconstant_observation:\n    and $t0, $t0, $t0 # data\n    dla $t0, data\n';out=ATTACH.attach(source(),k);self.assertIn('deltaKernel_data: .word 7',out);self.assertIn('deltaKernel_and: .word 9',out);self.assertIn('deltaKernel_t0: .asciiz "data and t0" # data and t0',out);self.assertIn("deltaKernel_A: .byte 'A'",out);self.assertIn('    and $t0, $t0, $t0 # data',out);self.assertIn('    dla $t0, deltaKernel_data',out);self.assertEqual(1,out.count('\n.data\n'))
 def test_conflicts_and_unsupported_kernel_shapes_fail(self):
  for s in(source()+'\n.text\n',source().replace('video_encode_full:','changed:'),source()+'\ndr_other:\n',source()+'\ndelta_other:\n',source()+'\ndeltaKernel_other:\n',ATTACH.attach(source(),kernel())):
   with self.assertRaises(ValueError):ATTACH.attach(s,kernel())
  for k in(kernel()+'\n.text\n',kernel().replace('video_xor_words_msa:','changed:'),kernel()+'\n.local_label:\n'):
   with self.assertRaises(ValueError):ATTACH.attach(source(),k)
 def test_all_recipes_modify_data_only(self):
  src=ATTACH.attach(source(),kernel());recipes=json.loads((D/'inputs.json').read_text())['recipes'];self.assertEqual(33,len(recipes));code=src.split('\n.text\n')[1]
  for r in recipes:
   out=PREP.prepare(src,r);self.assertEqual(code,out.split('\n.text\n')[1]);self.assertIn(r['checkpoint']+':',out);previous,current=FIX.words(r['fixture']);self.assertIn(FIX.declaration('delta_source',current),out);self.assertIn(FIX.declaration('delta_previous',previous),out)
 def test_unsigned_controls_and_missing_data_markers_fail(self):
  src=ATTACH.attach(source(),kernel())
  for key in('sourceCount','capacity','sequence','period','previousCapacity','scratchCapacity'):
   for value in(-1,2**64,True,'1',1.5):
    with self.assertRaises(ValueError):PREP.prepare(src,{key:value})
  self.assertIn('delta_context: .dword -1, 1, 4, 0, 16384',PREP.prepare(src,dict(capacity=2**64-1)))
  for text in(src.replace('delta_source_end:','changed:'),src.replace('delta_previous_end:','changed:'),src.replace('delta_context:','changed:'),src.replace('delta_scratch_fields:','changed:'),src+'\n.text\n'):
   with self.assertRaises(ValueError):PREP.prepare(text,{})
 def test_fixtures_expose_numeric_changes_and_tie(self):
  a,b=FIX.words('few');self.assertEqual([0,325,326,4095],[i for i in range(4096)if a[i]!=b[i]]);self.assertEqual([1,0x102040,0x102040,0x8000],[a[i]^b[i]for i in(0,325,326,4095)]);a,b=FIX.words('still');self.assertEqual(a,b);a,b=FIX.words('tie');self.assertEqual(4092,sum(x!=y for x,y in zip(a,b)));self.assertTrue(all(0<=v<2**24 for v in a+b))
  for name in('invalid-source-last','invalid-previous-last'):
   a,b=FIX.words(name);self.assertGreaterEqual((b if name=='invalid-source-last'else a)[-1],2**24)
  with self.assertRaises(ValueError):FIX.words('unknown')
 def test_native_steps_match_regions_quizzes_and_checkpoints(self):
  steps=json.loads((D/'steps.json').read_text())['steps'];self.assertEqual([f's{n}'for n in range(69,84)],[s['id']for s in steps]);self.assertEqual([s['region']for s in steps],re.findall(r'^# BEGIN (\w+)$',(D/'encoder.asm').read_text(),re.M))
  for s in steps:
   text=(ROOT/f"includes/chapters/034-lossless-video/delta-encode-{s['id']}.tex").read_text();self.assertIn(s['recipe'],text);self.assertIn(s['checkpoint'].replace('_',r'\_'),text);self.assertIn(s['quiz']['question'],text);self.assertEqual(1,text.count(r'\paragraph{Observe}'));self.assertEqual(1,text.count(r'\paragraph{Check}'));self.assertEqual(3,len(s['quiz']['choices']));self.assertEqual(2,len(s['hints']));self.assertNotIn('answer',s['quiz']);self.assertNotIn('correctChoice',s['quiz'])
   for c in s['quiz']['choices']:self.assertIn(r'\item '+c['text'],text)
 def test_helpers_protect_existing_outputs(self):
  with tempfile.TemporaryDirectory()as td:
   p=Path(td);src=p/'full.asm';src.write_text(source());k=p/'xor.asm';k.write_text(kernel());full=p/'record.asm';full.write_text(ATTACH.attach(source(),kernel()));out=p/'existing';out.write_text('student work')
   for args in(['attach.py','--source',str(src),'--kernel-source',str(k),'--output',str(out)],['prepare.py','s69-1','--source',str(full),'--output',str(out)],['fixtures.py','few','--output',str(out)]):
    r=subprocess.run([sys.executable,str(D/args[0]),*args[1:]],capture_output=True,text=True);self.assertNotEqual(0,r.returncode);self.assertEqual('student work',out.read_text())
 def test_fixture_export_is_frame_data_only(self):
  with tempfile.TemporaryDirectory()as td:
   out=Path(td)/'pair.json';r=subprocess.run([sys.executable,str(D/'fixtures.py'),'few','--output',str(out)],capture_output=True,text=True);self.assertEqual(0,r.returncode,r.stderr);m=json.loads(out.read_text());self.assertEqual({'fixture','previous','current'},set(m));a,b=FIX.words();self.assertEqual(a,m['previous']);self.assertEqual(b,m['current'])
if __name__=='__main__':unittest.main()
