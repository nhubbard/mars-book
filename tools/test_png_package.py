"""PNG exports preserve native authority, safe archives and honest grading boundaries."""
import importlib.util,json,re,tempfile,unittest,zipfile
from pathlib import Path
from unittest.mock import patch
import course
import png_package
ROOT=Path(__file__).resolve().parents[1];ID='033-png'
class PngPackageTests(unittest.TestCase):
 def test_native_reading_order_and_steps_are_exact(self):
  root,m=course.validate(ID);native=ROOT/'includes/chapters'/ID;expected=re.findall(r'\\input\{includes/chapters/'+ID+r'/([^}]+)\}',(native/'chapter.tex').read_text());steps={s['id']:Path(s['content']).stem for s in m['steps']};segments=course.reading_segments(root,m);actual=[steps[s['step']]if s['kind']=='step'else Path(s['content']).stem for s in segments if s['kind']!='figure'];self.assertEqual(expected,actual);self.assertEqual(100,len(m['steps']));self.assertEqual(m['steps'],json.loads((root/'steps.json').read_text())['steps']);self.assertEqual(97,sum(s['kind']=='edit'for s in m['steps']));self.assertEqual(97,len(re.findall(r'^# BEGIN ',(root/m['entry']).read_text(),re.M)))
  for p in native.glob('*.tex'):
   text=p.read_text().replace('includes/chapters/'+ID,'packages/'+ID+'/native').replace('book/captures/'+ID,'packages/'+ID+'/captures');text=re.sub(r'drafts/(png-[a-z-]+)',lambda match:'packages/'+ID+'/construction/'+match[1],text);self.assertEqual(text,(root/'native'/p.name).read_text())
 def test_completed_checks_and_drafting_assets_are_exact(self):
  root,m=course.validate(ID);self.assertEqual(8,len(m['publicChecks']));self.assertEqual(200,sum(len(s['hints'])for s in m['steps']))
  for p in(ROOT/'drafts/png-feedback/checks').glob('*'):
   if p.is_file():self.assertEqual(p.read_bytes(),(root/'checks'/p.name).read_bytes())
  for stage,_,_,_,_ in png_package.STAGES:
   for p in(ROOT/'drafts'/stage).glob('*'):
    if p.is_file()and p.suffix in('.asm','.py','.json','.md'):self.assertEqual(p.read_bytes(),(root/'construction'/stage/p.name).read_bytes())
  self.assertTrue(all(s['completion']['type']=='native-observation'for s in m['steps']));self.assertTrue(all('answer'not in s['quiz']and'correctChoice'not in s['quiz']for s in m['steps']))
 def test_archive_is_deterministic_student_only_and_ignores_runtime_caches(self):
  root,m=course.validate(ID)
  with tempfile.TemporaryDirectory()as td:
   folder=Path(td);a=course.bundle(ID,folder/'a.zip');b=course.bundle(ID,folder/'b.zip');self.assertEqual(a.read_bytes(),b.read_bytes())
   with zipfile.ZipFile(a)as z:
    expected={str(Path(ID)/p.relative_to(root)):p.read_bytes()for p in course.student_files(root,m)};self.assertEqual(set(expected),set(z.namelist()))
    for name,raw in expected.items():self.assertEqual(raw,z.read(name));self.assertFalse(any(p.lower()in('private','instructor','solutions','reference','__pycache__')for p in Path(name).parts));self.assertNotIn('quiz-keys',name)
  fake=root/'construction/png-construction/__pycache__/temporary.pyc';fake.parent.mkdir(exist_ok=True)
  try:fake.write_bytes(b'not exported');self.assertNotIn(fake,course.student_files(root,m))
  finally:fake.unlink();
 def test_course_check_supplies_the_separate_public_harness(self):
  root,m=course.validate(ID)
  with patch('course.subprocess.run')as run:course.grade(ID,Path('/tmp/cli.jar'),root/m['entry'],java='java-test')
  self.assertEqual(8,run.call_count)
  for call in run.call_args_list:self.assertEqual(str(root/'checks/harness.asm'),call.args[0][-1]);self.assertTrue(call.kwargs['check'])
 def test_grading_harness_cannot_escape_the_package(self):
  original=course.read_json
  def read(path):
   m=original(path)
   if path.name=='package.json'and path.parent.name==ID:m['gradingHarness']='../escape.asm'
   return m
  with patch('course.read_json',side_effect=read),self.assertRaises(ValueError):course.validate(ID)
 def test_capture_plans_require_actual_reviewed_evidence(self):
  root,m=course.validate(ID);plans=json.loads((root/m['figures']).read_text())['figures'];self.assertEqual(5,len(plans));recipes={r['id']for r in json.loads((root/'inputs.json').read_text())['recipes']}
  self.assertEqual(5,sum(plan['captureStatus']=='verified'for plan in plans))
  for plan in plans:
   self.assertIn(plan['captureStatus'],('planned','verified'));self.assertEqual('IntelliJ Light',plan['theme']);self.assertIn(plan['recipe'],recipes)
   if plan['captureStatus']=='planned':self.assertNotIn('artifact',plan);self.assertNotIn('receipt',plan)
   else:
    self.assertTrue((root/plan['artifact']).is_file());self.assertTrue((root/plan['receipt']).is_file());self.assertTrue((root/plan['nativeAnnotations']).is_file())
 def test_capture_checkpoints_follow_the_actual_caller_recipes(self):
  root,m=course.validate(ID);plans=json.loads((root/m['figures']).read_text())['figures']
  recipes={r['id']:r for r in json.loads((root/'inputs.json').read_text())['recipes']}
  expected={'png-file-eof':('s10-empty','loader_return'),
            'png-container-fields':('s27-1','collect_publish_ready'),
            'png-row-reconstruction':('s83-1','row_up_tail_ready'),
            'png-vector-packing':('s97-1','pack_vector_advanced'),
            'png-final-publication':('s100-display','publication_results_ready')}
  self.assertEqual(expected,{p['id']:(p['recipe'],p['checkpoint'])for p in plans})
  for plan in plans:
   recipe=recipes[plan['recipe']]
   self.assertEqual(recipe.get('callerCheckpoint',recipe['checkpoint']),plan['checkpoint'])
  self.assertNotEqual(recipes['s10-empty']['functionCheckpoint'],expected['png-file-eof'][1])
if __name__=='__main__':unittest.main()
