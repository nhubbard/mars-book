import json
from pathlib import Path
import re
import tempfile
import unittest
import zipfile
from native_lesson import bundle
from rsa_lesson_cases import groups, profiles

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / 'revision/lessons/027-rsa-arithmetic'


class RsaNativeUnitTest(unittest.TestCase):
    def test_stages_preserve_every_original_case_and_final_rechecks_the_same_source(self):
        lesson = json.loads((UNIT/'lesson.json').read_text())
        assessments = [s for s in lesson['steps'] if s['kind'] == 'assessment']
        self.assertEqual([s['id'] for s in assessments], ['complete-'+k for k in groups()] + ['recheck-completed-source'])
        expected = profiles()
        actual = {}
        for name, data in expected.items():
            path = 'independent/rsa-'+name.removesuffix('.json')+'.cases.json'
            actual[path] = json.loads((UNIT/path).read_text())
            self.assertEqual(data, actual[path])
        stages = assessments[:-1]
        paths = [p for s in stages for p in s['caseProfiles']]
        self.assertEqual(len(paths), len(set(paths)))
        self.assertEqual(set(paths), set(actual))
        self.assertEqual(assessments[-1]['caseProfiles'], paths)
        self.assertTrue(all(s['source'] == 'independent/rsa-arithmetic.asm' for s in assessments))
        self.assertEqual([sum(len(actual[p]['cases']) for p in s['caseProfiles']) for s in assessments], [8,22,13,22,17,14,11,12,4,123])
        self.assertEqual(sum(len(c['calls']) for s in assessments for p in s['caseProfiles'] for c in actual[p]['cases']), 284)
        for key in groups():
            self.assertEqual((UNIT/f'independent/{key}.kts').read_bytes(), (ROOT/f'packages/027-rsa-arithmetic/checks/{key}.kts').read_bytes())
        self.assertEqual((UNIT/'independent/rsa-arithmetic.asm').read_bytes(), (ROOT/'revision/labs/files/rsa-arithmetic.asm').read_bytes())

    def test_export_is_explicit_and_bundling_is_deterministic(self):
        manifest = json.loads((UNIT/'package.json').read_text())
        names = ['package.json',manifest['entry'],manifest['assignment'],manifest['nativeLesson'],*manifest['supportingFiles']]
        self.assertEqual(len(names),40)
        self.assertEqual(len(names),len(set(names)))
        self.assertEqual(set(names),{str(p.relative_to(UNIT)) for p in UNIT.rglob('*') if p.is_file()})
        with tempfile.TemporaryDirectory() as directory:
            first, second = Path(directory)/'a.zip', Path(directory)/'b.zip'
            bundle(UNIT,first);bundle(UNIT,second)
            self.assertEqual(first.read_bytes(),second.read_bytes())
            with zipfile.ZipFile(first) as archive:
                self.assertEqual(len(archive.namelist()),40)
                self.assertTrue(all(n.startswith('027-rsa-arithmetic/') for n in archive.namelist()))
                self.assertFalse(any('instructor' in n for n in archive.namelist()))

    def test_required_observations_do_not_edit_the_continuing_implementation(self):
        lesson = json.loads((UNIT/'lesson.json').read_text())
        self.assertEqual(len(lesson['steps']),35)
        predictions = [s for s in lesson['steps'] if s['kind'] == 'prediction']
        debug = [s for s in lesson['steps'] if s['kind'] == 'debug']
        self.assertEqual(len(predictions),16)
        self.assertEqual(len(debug),8)
        self.assertEqual(sum(len(s['stops']) for s in debug),26)
        self.assertTrue(all(s['source'].startswith('worked/') for s in debug + predictions))
        self.assertEqual([s['correctChoice'] for s in predictions], ['abc'[i%3] for i in range(16)])
        for source in (UNIT/'worked').glob('*.asm'):
            self.assertIsNone(re.search(r'^rsa_(gcd64|pow32|mul128|addmod128|reduce128|mulmod128|pow128):',source.read_text(),re.M))
        self.assertEqual((UNIT/'independent/rsa-arithmetic.asm').read_text().count('    break'),7)
        run = [s for s in lesson['steps'] if s['kind']=='run']
        self.assertEqual(len(run),1)
        self.assertEqual(run[0]['source'],'independent/rsa-arithmetic.asm')
        self.assertEqual(run[0]['stdout'],'')

    def test_width_carry_prefix_and_ownership_observations_remain_distinct(self):
        debug = {s['id']:s for s in json.loads((UNIT/'lesson.json').read_text())['steps'] if s['kind']=='debug'}
        product = debug['observe-product-width']['stops'][-1]['registers']
        self.assertEqual([int(product[k],16) for k in ['$t3','$t4','$t5']],[0,1,(1<<64)-1])
        carry = debug['observe-carry-decision']['stops'][1]['registers']
        self.assertEqual([int(carry[k],16) for k in ['$t4','$t5','$t6']],[244,1,1])
        prefixes = debug['observe-prefixes']['stops'][:-1]
        self.assertEqual([int(s['registers']['$t6'],16) for s in prefixes],[1,3,1,3])
        self.assertEqual(len({s['anchor']['label'] for s in prefixes}),1)
        shifted = debug['observe-exponent-shift']['stops'][1]['registers']
        self.assertEqual(int(shifted['$t2'],16),1<<63)
        self.assertEqual(int(shifted['$t3'],16),1<<62)
        validation = debug['observe-validation-boundary']['stops'][-1]['registers']
        self.assertEqual(int(validation['$v0'],16),1)
        self.assertEqual(int(validation['$t1'],16),0x5a5a5a5a)


if __name__ == '__main__':
    unittest.main()
