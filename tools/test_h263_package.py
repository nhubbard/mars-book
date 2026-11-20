import importlib.util
import json
import shutil
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import patch
import h263_package as package
import course
ROOT=Path(__file__).resolve().parents[1]
ID='035-h263'

def input_tree(target):
    (target/'docs').mkdir(parents=True)
    shutil.copyfile(ROOT/'docs/h263-grader-revision.txt',target/'docs/h263-grader-revision.txt')
    shutil.copytree(ROOT/'includes/chapters'/ID,target/'includes/chapters'/ID)
    for name in [s[1]for s in package.STAGES]+['h263-fixtures','h263-package']:
        shutil.copytree(ROOT/'drafts'/name,target/'drafts'/name,ignore=shutil.ignore_patterns('__pycache__','*.pyc'))

def load_runner(root):
    s=importlib.util.spec_from_file_location('h263_package_runner',root/'grade.py');m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m

class H263PackageTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp=tempfile.TemporaryDirectory();cls.root=Path(cls.temp.name)/'public';input_tree(cls.root);cls.manifest=package.build(cls.root);cls.dest=cls.root/'packages'/ID
    @classmethod
    def tearDownClass(cls):cls.temp.cleanup()
    def test_all_native_steps_and_domains_are_preserved(self):
        steps=self.manifest['steps'];self.assertEqual([s['number']for s in steps],list(range(1,121)));self.assertEqual(len(set(s['id']for s in steps)),120);self.assertEqual(sum(s[3]for s in package.STAGES),1196)
        for step in steps:self.assertEqual(len(step['quiz']['choices']),3);self.assertNotIn('answer',step['quiz']);self.assertNotIn('correctChoice',step['quiz'])
        chapter=(self.dest/'native/chapter.tex').read_text();self.assertIn(r'\label{chap:h263}',chapter);self.assertNotIn('includes/chapters/',chapter)
    def test_each_domain_retains_its_own_bounded_harness(self):
        mapping=json.loads((self.dest/'checks/harnesses.json').read_text())
        self.assertEqual(set(mapping),{s[0]for s in package.STAGES})
        for domain,path in mapping.items():
            text=(self.dest/'checks'/path).read_text();self.assertEqual(text.count('.data 0x100000000'),1)
            self.assertIn('.dword -6510615555426900571:',text)
            self.assertEqual(self.manifest['checkHarnesses']['checks/'+domain+'.kts'],'checks/'+path)
    def test_exported_checks_and_student_code_remain_exact(self):
        for domain,folder,_,_ in package.STAGES:
            self.assertEqual((self.dest/'checks'/(domain+'.kts')).read_bytes(),(ROOT/'drafts'/folder/'check.kts').read_bytes())
            self.assertEqual((self.dest/'construction'/folder/'main.asm').read_bytes(),(ROOT/'drafts'/folder/'main.asm').read_bytes())
    def test_native_paths_change_without_changing_authoritative_prose(self):
        original=(ROOT/'includes/chapters'/ID/'clip-workflow.tex').read_text();exported=(self.dest/'native/clip-workflow.tex').read_text()
        self.assertEqual(exported,original.replace('drafts/h263-','construction/h263-'));self.assertIn('drafts/h263-',original)
    def test_rebuild_is_deterministic_and_relocated_archive_is_exact(self):
        with patch.object(course,'ROOT',self.root):
            course.validate(ID);first=course.bundle(ID).read_bytes();package.build(self.root);second=course.bundle(ID).read_bytes();self.assertEqual(first,second)
            with zipfile.ZipFile(course.bundle(ID))as z:
                self.assertEqual(set(z.namelist()),{str(Path(ID)/p.relative_to(self.dest))for p in course.student_files(self.dest,self.manifest)})
                self.assertFalse(any('quiz-keys'in n or any(x in ('instructor','private','solutions','__pycache__')for x in Path(n).parts)for n in z.namelist()))
    def test_capture_review_state_is_not_reset(self):
        p=self.dest/'captures/plan.json';old=p.read_bytes();x=json.loads(old);x['figures'][0]['reviewNote']='Retain editorial decision';p.write_text(json.dumps(x));kept=p.read_bytes()
        try:package.build(self.root);self.assertEqual(p.read_bytes(),kept)
        finally:p.write_bytes(old)
    def test_edited_native_export_is_retained(self):
        p=self.dest/'native/purpose.tex';old=p.read_bytes();p.write_bytes(old+b'\n% Human revision\n')
        try:
            with self.assertRaisesRegex(ValueError,'Edited export retained'):package.build(self.root)
            self.assertTrue(p.read_bytes().endswith(b'% Human revision\n'))
        finally:p.write_bytes(old)
    def test_symlink_source_is_not_exported(self):
        p=self.root/'drafts/h263-fixtures/outside.json'
        with tempfile.TemporaryDirectory()as temp:
            outside=Path(temp)/'private.json';outside.write_text('private material');p.symlink_to(outside)
            try:
                with self.assertRaisesRegex(ValueError,'symlink'):package.build(self.root)
            finally:p.unlink()
    def test_capture_review_state_survives_reexport(self):
        path=self.dest/'captures/plan.json';old=path.read_bytes();data=json.loads(old);data['figures'][0]['captureStatus']='verified';data['figures'][0]['reviewNote']='Temporary unit fixture, never a published capture';path.write_text(json.dumps(data))
        try:
            package.build(self.root)
            self.assertEqual(json.loads(path.read_text())['figures'][0]['captureStatus'],'verified')
        finally:path.write_bytes(old)
    def test_course_runner_selects_per_check_harness_and_preserves_default(self):
        data=dict(publicChecks=['checks/a.kts','checks/b.kts'],gradingHarness='checks/default.asm',checkHarnesses={'checks/a.kts':'checks/a.asm'})
        with patch.object(course,'validate',return_value=(self.dest,data)),patch.object(course.subprocess,'run')as run:
            course.grade(ID,Path('/tmp/cli.jar'),Path('/tmp/student.asm'))
        self.assertEqual(run.call_args_list[0].args[0][-1],str(self.dest/'checks/a.asm'))
        self.assertEqual(run.call_args_list[1].args[0][-1],str(self.dest/'checks/default.asm'))
    def test_course_rejects_unknown_missing_and_escaping_harness_overrides(self):
        path=self.dest/'package.json';old=path.read_bytes();data=json.loads(old)
        try:
            for mapping in [{'unknown.kts':'checks/harness-sad.asm'},{'checks/sad.kts':'../outside.asm'},{'checks/sad.kts':'checks/missing.asm'},['checks/sad.kts']]:
                data['checkHarnesses']=mapping;path.write_text(json.dumps(data))
                with patch.object(course,'ROOT',self.root),self.assertRaises(ValueError):course.validate(ID)
        finally:path.write_bytes(old)
    def test_entry_only_starter_contains_no_completed_body(self):
        text=(self.dest/'starter/interfaces.asm').read_text()
        for entry in package.ENTRIES:self.assertIn(entry+':\n    li $v0, 7',text)
        self.assertNotIn('syntax_mcbpc_codes',text)
    def runner_fixture(self,d):
        root=d/'package';(root/'checks').mkdir(parents=True);(root/'checks/counts.json').write_text('{"sad":2}');(root/'checks/sad.kts').write_text('fixture');(root/'checks/harness.asm').write_text('fixture');shutil.copyfile(ROOT/'drafts/h263-package/grade.py',root/'grade.py');source=d/'student.asm';source.write_text('student');cli=d/'cli.jar';cli.write_bytes(b'archive');return load_runner(root),source,cli
    def test_runner_requires_the_exact_case_count(self):
        with tempfile.TemporaryDirectory()as temp:
            d=Path(temp);runner,source,cli=self.runner_fixture(d)
            with patch.object(runner.subprocess,'run',return_value=type('Result',(),dict(returncode=0,stdout='PASS one\n',stderr=''))()):
                self.assertEqual(runner.run([str(source),'--cli',str(cli)]),1)
    def test_runner_protects_existing_reports(self):
        with tempfile.TemporaryDirectory()as temp:
            d=Path(temp);runner,source,cli=self.runner_fixture(d);report=d/'report.json';report.write_text('existing')
            with self.assertRaises(SystemExit),patch.object(runner.subprocess,'run')as run:
                runner.run([str(source),'--cli',str(cli),'--report',str(report)])
            run.assert_not_called();self.assertEqual(report.read_text(),'existing')
    def test_runner_success_records_source_and_harness_hashes(self):
        with tempfile.TemporaryDirectory()as temp:
            d=Path(temp);runner,source,cli=self.runner_fixture(d);report=d/'report.json'
            with patch.object(runner.subprocess,'run',return_value=type('Result',(),dict(returncode=0,stdout='PASS one\nPASS two\n',stderr=''))()):
                self.assertEqual(runner.run([str(source),'--cli',str(cli),'--report',str(report)]),0)
            x=json.loads(report.read_text());self.assertEqual(x['casesPassed'],2);self.assertEqual(len(x['harnessSha256']['sad']),64);self.assertTrue(x['passed'])
