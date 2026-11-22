import json
from pathlib import Path
import tempfile
import unittest

import matvec_lesson_cases as matvec


class MatvecLessonCasesTests(unittest.TestCase):
    def test_original_shapes_offsets_values_and_signed_products(self):
        trials=matvec.trials();self.assertEqual(10,len(trials))
        for seed,trial in enumerate(trials[:8]):
            m,k=matvec.SHAPES[seed]
            self.assertEqual((m,k,seed),(trial['m'],trial['k'],trial['seed']))
            self.assertEqual(seed%(min(7,256-m*k)+1),trial['matrixOffset'])
            self.assertEqual(seed%(min(7,16-k)+1),trial['vectorOffset'])
            self.assertEqual(seed%(min(7,16-m)+1),trial['outputOffset'])
            for row,total in enumerate(matvec.product(trial)):
                actual=sum(trial['matrix'][trial['matrixOffset']+row*k+q]*trial['vector'][trial['vectorOffset']+q] for q in range(k))
                self.assertEqual(actual,total)
            self.assertEqual(256,len(trial['matrix']));self.assertEqual(16,len(trial['vector']))
        self.assertEqual([50,122],matvec.product(trials[-2]))
        self.assertEqual([2147483648],matvec.product(trials[-1]))
        self.assertEqual('0000008000000000',matvec.doublewords(matvec.product(trials[-1])))

    def test_profiles_retain_independent_width_ownership_and_repeated_initialization(self):
        profile=matvec.profile();self.assertEqual(8,profile['formatVersion'])
        self.assertEqual(11,len(profile['cases']))
        self.assertEqual(21,sum(len(case['calls']) for case in profile['cases']))
        for trial,case in zip(matvec.trials(),profile['cases']):
            self.assertEqual(['function_return','matvec_done'],[call['returnTo'] for call in case['calls']])
            self.assertEqual(9,len(case['preservedRegisters']))
            self.assertEqual(trial['m']*8,case['initializedOutput'][0]['length'])
            policy=case['accessPolicies'][0]
            self.assertEqual({'label':'matrix_guard'},policy['from'])
            self.assertEqual({'label':'completed','offset':4},policy['until'])
            self.assertEqual([case['initializedOutput'][0]],policy['writable'])
            if trial['m']==0:
                self.assertEqual([0,0],[window['length'] for window in policy['readable'][:2]])
            memory={item['label']:item['bytes'] for item in case['calls'][0]['memory']}
            output=bytes.fromhex(memory['output']);offset=trial['outputOffset']*8
            self.assertEqual(matvec.doublewords(matvec.product(trial)),output[offset:offset+trial['m']*8].hex())
            self.assertEqual(128,len(output))
        caller=profile['cases'][-1]
        self.assertEqual('main',caller['calls'][0]['entry']);self.assertEqual('done',caller['calls'][0]['returnTo'])
        self.assertNotIn('argumentAddresses',caller['calls'][0])
        self.assertEqual('02000000',next(item['bytes'] for item in caller['calls'][0]['memory'] if item['label']=='completed'))
        self.assertEqual(2,len(caller['accessPolicies'][0]['writable']))

    def test_checked_in_data_is_current_and_build_refuses_stale_data(self):
        root=Path(__file__).resolve().parents[1]/'revision/lessons/022-measuring-programs/independent'
        matvec.build(root,check=True)
        with tempfile.TemporaryDirectory() as temporary:
            directory=Path(temporary);matvec.build(directory);matvec.build(directory,check=True)
            self.assertEqual(matvec.profile(),json.loads((directory/'matrix-vector.cases.json').read_text()))
            self.assertEqual('',json.loads((directory/'caller-exit.cases.json').read_text())['cases'][0]['input'])
            (directory/'matrix-vector.cases.json').write_text('{}')
            with self.assertRaises(ValueError):matvec.build(directory,check=True)

class MatvecNativePackageTests(unittest.TestCase):
    def test_student_exports_bind_all_worked_sources_and_independent_checks(self):
        from native_lesson import bundle
        import zipfile
        root=Path(__file__).resolve().parents[1]
        unit=root/'revision/lessons/022-measuring-programs'
        manifest=json.loads((unit/'package.json').read_text());lesson=json.loads((unit/'lesson.json').read_text())
        self.assertEqual((root/'revision/labs/files/matrix-measure.asm').read_bytes(),(unit/'matrix-measure.asm').read_bytes())
        self.assertEqual((root/'revision/labs/files/matrix-vector.asm').read_bytes(),(unit/'independent/matrix-vector.asm').read_bytes())
        self.assertEqual((root/'revision/labs/checks/matrix-vector.kts').read_bytes(),(unit/'independent/matrix-vector.kts').read_bytes())
        self.assertEqual(9,sum(step['kind']=='prediction' for step in lesson['steps']))
        self.assertEqual(19,sum(len(step['stops']) for step in lesson['steps'] if step['kind']=='debug'))
        self.assertEqual(5,sum(step['kind']=='run' for step in lesson['steps']))
        self.assertEqual(2,sum(step['kind']=='assessment' for step in lesson['steps']))
        with tempfile.TemporaryDirectory() as temporary:
            archive=bundle(unit,Path(temporary)/'student.zip')
            with zipfile.ZipFile(archive) as opened:
                expected={manifest['id']+'/'+name for name in ['package.json',manifest['entry'],manifest['assignment'],manifest['nativeLesson'],*manifest['supportingFiles']]}
                self.assertEqual(expected,set(opened.namelist()))
                for step in lesson['steps']:
                    self.assertIn(manifest['id']+'/'+step.get('source',manifest['entry']),expected)
                    if step['kind']=='assessment':self.assertIn(manifest['id']+'/'+step['caseProfile'],expected)
                self.assertFalse(any('solution' in name or 'reference' in name or 'private' in name for name in expected))


if __name__=='__main__':unittest.main()
