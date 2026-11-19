"""Native video exports, independent storage and compaction preserve student work."""
import importlib.util,json,re,tempfile,unittest,zipfile
from pathlib import Path
from unittest.mock import patch
import course
import video_package
ROOT=Path(__file__).resolve().parents[1];ID='034-lossless-video'
def load(path):
 s=importlib.util.spec_from_file_location('video_package_compactor_tests',path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
COMPACT=load(ROOT/'drafts/video-feedback/compact.py')
class VideoPackageTests(unittest.TestCase):
 def test_native_order_steps_and_all_regions_are_preserved(self):
  root,m=course.validate(ID);native=ROOT/'includes/chapters'/ID;expected=re.findall(r'\\input\{includes/chapters/'+ID+r'/([^}]+)\}',(native/'chapter.tex').read_text());steps={s['id']:Path(s['content']).stem for s in m['steps']};actual=[steps[s['step']]if s['kind']=='step'else Path(s['content']).stem for s in course.reading_segments(root,m)if s['kind']!='figure'];self.assertEqual(expected,actual);self.assertEqual(122,len(m['steps']));self.assertEqual(m['steps'],json.loads((root/'steps.json').read_text())['steps']);self.assertEqual(122,len(re.findall(r'^# BEGIN ',(root/m['entry']).read_text(),re.M)));self.assertEqual(244,sum(len(s['hints'])for s in m['steps']))
  for p in native.glob('*.tex'):
   text=p.read_text().replace('includes/chapters/'+ID,'packages/'+ID+'/native').replace('book/captures/'+ID,'packages/'+ID+'/captures');text=re.sub(r'drafts/(video-[a-z-]+)',lambda x:'packages/'+ID+'/construction/'+x[1],text);self.assertEqual(text,(root/'native'/p.name).read_text())
 def test_all_stages_checks_and_recipes_are_exact_exports(self):
  root,m=course.validate(ID);self.assertEqual(11,len(m['publicChecks']))
  for stage,_,_,_,_ in video_package.STAGES:
   for p in(ROOT/'drafts'/stage).glob('*'):
    if p.is_file()and p.suffix in('.py','.asm','.json','.md'):self.assertEqual(p.read_bytes(),(root/'construction'/stage/p.name).read_bytes())
  for p in(ROOT/'drafts/video-feedback/checks').glob('*'):
   if p.is_file():self.assertEqual(p.read_bytes(),(root/'checks'/p.name).read_bytes())
  self.assertTrue(all('correctChoice'not in s['quiz']and'answer'not in s['quiz']for s in m['steps']));self.assertTrue(all(not s['title'].startswith('\\section')for s in m['steps']))
 def test_compaction_changes_only_unused_arenas_and_capacity_controls(self):
  source=video_package.combined(ROOT/'drafts');custom=source+'\nstudent_extra:\n    daddiu $t0, $t0, 37\n';result=COMPACT.compact(custom);self.assertEqual(custom.split('\n.text\n')[1],result.split('\n.text\n')[1]);self.assertEqual((ROOT/'packages'/ID/'starter/main.asm').read_text(),COMPACT.compact(source));self.assertIn('file_encode_context: .dword 262144,',result);self.assertIn('io_capacity: .dword 262144',result)
  for bad in(source.replace('index_file_data:','changed:'),source.replace('file_encode_frames:','changed:'),source.replace('.space 4063232','.space 0'),result):
   with self.assertRaises(ValueError):COMPACT.compact(bad)
 def test_student_archive_is_deterministic_and_has_no_private_material(self):
  root,m=course.validate(ID)
  with tempfile.TemporaryDirectory()as td:
   a=course.bundle(ID,Path(td)/'a.zip');b=course.bundle(ID,Path(td)/'b.zip');self.assertEqual(a.read_bytes(),b.read_bytes())
   with zipfile.ZipFile(a)as z:
    expected={str(Path(ID)/p.relative_to(root)):p.read_bytes()for p in course.student_files(root,m)};self.assertEqual(set(expected),set(z.namelist()))
    for name,data in expected.items():self.assertEqual(data,z.read(name));self.assertNotIn('quiz-keys',name);self.assertFalse(any(x.lower()in('private','instructor','solutions','reference','__pycache__')for x in Path(name).parts))
 def test_course_feedback_supplies_the_separate_harness(self):
  root,m=course.validate(ID)
  with patch('course.subprocess.run')as run:course.grade(ID,Path('/tmp/cli.jar'),root/m['entry'],java='java-test')
  self.assertEqual(11,run.call_count)
  for call in run.call_args_list:self.assertEqual(str(root/'checks/harness.asm'),call.args[0][-1]);self.assertTrue(call.kwargs['check'])
 def test_capture_plans_are_honest_and_stage_local(self):
  root,m=course.validate(ID);plans=json.loads((root/m['figures']).read_text())['figures'];self.assertEqual(6,len(plans));recipes={r['id']for r in json.loads((root/'inputs.json').read_text())['recipes']}
  self.assertEqual(['video-vector-count','video-record-publication','video-index-publication'],[f['id']for f in plans if f['captureStatus']=='verified'])
  for f in plans:
   self.assertEqual('IntelliJ Light',f['theme']);self.assertIn(f['recipe'],recipes);self.assertTrue((root/'construction'/f['stage']).is_dir())
   if f['captureStatus']=='planned':self.assertNotIn('artifact',f);self.assertNotIn('receipt',f)
   else:self.assertTrue((root/f['artifact']).is_file());self.assertTrue((root/f['receipt']).is_file());self.assertTrue((root/f['nativeAnnotations']).is_file())
 def test_grading_harness_is_disjoint_and_algorithm_free(self):
  root,m=course.validate(ID);h=(root/m['gradingHarness']).read_text();self.assertIn('.data 0x100000000',h);self.assertIn('videoCheck_stack: .space 32768',h);self.assertEqual(['videoCheck_return'],re.findall(r'^(\w+):',h.split('.text\n')[1],re.M));self.assertNotIn('video_play:',h)
if __name__=='__main__':unittest.main()
