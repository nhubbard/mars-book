from pathlib import Path
import json
import tempfile
import unittest
import zipfile

from native_lesson import bundle


class Utf8NativeUnitTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(__file__).resolve().parents[1]
        self.unit = self.root / 'revision/lessons/015-utf8-streams'
        self.lesson = json.loads((self.unit / 'lesson.json').read_text())

    def test_stream_assessments_retain_every_original_trial_and_independent_state_checks(self):
        steps = self.lesson['steps']
        self.assertEqual(16, len(steps))
        self.assertEqual(16, len({s['id'] for s in steps}))
        self.assertEqual(8, sum(s['kind'] == 'prediction' for s in steps))
        debug = [s for s in steps if s['kind'] == 'debug']
        self.assertEqual([3, 3, 4], [len(s['stops']) for s in debug])
        self.assertEqual([0, 1, 2, 3], [int(s['registers']['$s3'], 16) for s in debug[-1]['stops']])
        self.assertTrue(all(s['anchor']['label'] == 'chunk_ready' for s in debug[-1]['stops']))
        self.assertEqual(20, sum(len(s['stops']) if s['kind'] == 'debug' else 1 for s in steps if s['kind'] != 'assessment'))
        checks = [s for s in steps if s['kind'] == 'assessment']
        self.assertEqual(['check-stream','check-interleaved','check-caller'],[s['id'] for s in checks])
        profiles = [json.loads((self.unit/name).read_text()) for name in checks[0]['caseProfiles']]
        self.assertEqual([50]*8+[26],[len(p['cases']) for p in profiles])
        canonical = [r for p in sorted((self.root/'revision/labs/checks/utf8-native').glob('stream-*.cases.json')) for r in json.loads(p.read_text())['cases']]
        self.assertEqual(canonical,[r for p in profiles for r in p['cases']])
        self.assertEqual(12,len(json.loads((self.unit/checks[1]['caseProfile']).read_text())['cases']))
        self.assertEqual(15,len(json.loads((self.unit/checks[2]['caseProfile']).read_text())['cases']))
        self.assertEqual((self.root/'revision/labs/files/utf8-stream.asm').read_bytes(),(self.unit/'independent/stream.asm').read_bytes())
        source=(self.unit/'worked/range.asm').read_text()
        self.assertEqual(source.replace('sample: .byte 0x9f','sample: .byte 0xa0'),(self.unit/'worked/lower-bound.asm').read_text())

    def test_archive_exports_only_public_sources_and_explicit_check_profiles(self):
        with tempfile.TemporaryDirectory() as temporary:
            archive = bundle(self.unit, Path(temporary) / 'student.zip')
            manifest = json.loads((self.unit / 'package.json').read_text())
            expected = ['package.json',manifest['entry'],manifest['assignment'],manifest['nativeLesson'],*manifest['supportingFiles']]
            self.assertEqual(21,len(expected))
            with zipfile.ZipFile(archive) as package:
                self.assertEqual(sorted('015-utf8-streams/'+n for n in expected),package.namelist())
                for name in expected:self.assertEqual((self.unit/name).read_bytes(),package.read('015-utf8-streams/'+name))
                starter = package.read('015-utf8-streams/independent/stream.asm').decode()
                self.assertEqual(1,sum(line.strip()=='break' for line in starter.splitlines()))
                self.assertFalse(any('reference' in name or 'solution' in name for name in package.namelist()))


if __name__ == '__main__':
    unittest.main()
