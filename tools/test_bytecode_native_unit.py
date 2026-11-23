from pathlib import Path
import json
import tempfile
import unittest
import zipfile

from native_lesson import bundle


class BytecodeNativeUnitTest(unittest.TestCase):
    def setUp(self):
        self.root=Path(__file__).resolve().parents[1];self.unit=self.root/'revision/lessons/018-bytecode-machine';self.lesson=json.loads((self.unit/'lesson.json').read_text())

    def test_record_extent_experiments_lead_to_validation_retirement_and_replacement_checks(self):
        source=(self.root/'revision/labs/files/bytecode-record-width.asm').read_text();self.assertEqual(source,(self.unit/'worked/record.asm').read_text());self.assertEqual(source.replace('length: .word 5','length: .word 4'),(self.unit/'worked/truncated.asm').read_text())
        self.assertEqual((self.root/'revision/labs/files/bytecode-vm.asm').read_bytes(),(self.unit/'independent/vm.asm').read_bytes())
        steps=self.lesson['steps'];self.assertEqual(19,len(steps));self.assertEqual(19,len({s['id'] for s in steps}));self.assertEqual(8,sum(s['kind']=='prediction' for s in steps))
        debug=[s for s in steps if s['kind']=='debug'];self.assertEqual([3,4,3],[len(s['stops']) for s in debug]);self.assertEqual('0x0000000000000004',debug[1]['stops'][2]['registers']['$t3']);self.assertEqual(['vm_call','vm_ready','vm_stored'],[s['anchor']['label'] for s in debug[-1]['stops']])
        self.assertEqual(20,sum(len(s['stops']) if s['kind']=='debug' else 1 for s in steps if s['kind']!='assessment'))
        checks=[s for s in steps if s['kind']=='assessment'];self.assertEqual(['check-structure','check-targets','check-completed','check-runtime','check-replacement','check-caller'],[s['id'] for s in checks]);seen=[]
        for step,count in zip(checks,[500,18,421,308,4,23]):
            files=step.get('caseProfiles',[step.get('caseProfile')]);rows=[r for name in files for r in json.loads((self.unit/name).read_text())['cases']];self.assertEqual(count,len(rows));self.assertTrue(all(step['source']=='independent/vm.asm' for _ in rows));seen+=rows
            for name in files:self.assertEqual((self.root/'revision/labs/checks/bytecode-native'/Path(name).name).read_bytes(),(self.unit/name).read_bytes())
        self.assertEqual(1274,len({r['name'] for r in seen}))

    def test_student_archive_retains_one_unfinished_function_and_only_explicit_public_files(self):
        with tempfile.TemporaryDirectory() as temporary:
            archive=bundle(self.unit,Path(temporary)/'student.zip');manifest=json.loads((self.unit/'package.json').read_text());names=['package.json',manifest['entry'],manifest['assignment'],manifest['nativeLesson'],*manifest['supportingFiles']];self.assertEqual(40,len(names))
            with zipfile.ZipFile(archive) as package:
                self.assertEqual(sorted('018-bytecode-machine/'+n for n in names),package.namelist())
                for name in names:self.assertEqual((self.unit/name).read_bytes(),package.read('018-bytecode-machine/'+name))
                source=package.read('018-bytecode-machine/independent/vm.asm').decode();self.assertEqual(1,sum(line.strip()=='break' for line in source.splitlines()));self.assertFalse(any('reference' in n or 'solution' in n for n in package.namelist()))


if __name__=='__main__':unittest.main()
