import json
from pathlib import Path
import re
import tempfile
import unittest
import zipfile
from native_lesson import bundle
from wav_reverse_lesson_cases import profiles

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT/'revision/lessons/031-pcm-wav'


class WavNativeUnitTest(unittest.TestCase):
    def test_all_original_cases_are_staged_once_and_rechecked_on_the_same_implementation(self):
        lesson = json.loads((UNIT/'lesson.json').read_text())
        checks = [s for s in lesson['steps'] if s['kind'] == 'assessment']
        expected = profiles()
        paths = ['independent/'+name for name in expected]
        self.assertEqual([s['id'] for s in checks], ['check-mono','check-stereo','check-boundaries-and-pipeline','recheck-completed-source'])
        self.assertEqual([p for s in checks[:-1] for p in s['caseProfiles']],paths)
        self.assertEqual(checks[-1]['caseProfiles'],paths)
        self.assertTrue(all(s['source'] == 'independent/wav-reverse-frames.asm' for s in checks))
        for name,data in expected.items():self.assertEqual(json.loads((UNIT/'independent'/name).read_text()),data)
        self.assertEqual([sum(len(expected[p.split('/')[-1]]['cases']) for p in s['caseProfiles']) for s in checks],[12,12,11,35])
        self.assertEqual(sum(len(c['calls']) for data in expected.values() for c in data['cases']),40)

    def test_standalone_source_preserves_the_actual_task_and_supplied_helpers(self):
        helper = (ROOT/'revision/labs/files/wav-functions.asm').read_text()
        original = (ROOT/'revision/labs/files/wav-reverse-frames.asm').read_text()
        actual = (UNIT/'independent/wav-reverse-frames.asm').read_text()
        expected = original.replace('# Independent frame-reversal exercise. Keep wav-functions.asm beside this file.',
            '# Independent frame-reversal exercise. Supplied WAV helpers are included below.').replace('.include "wav-functions.asm"',helper)
        self.assertEqual(actual,expected)
        self.assertEqual(actual.count('    break'),1)
        self.assertTrue(actual.endswith('wav_reverse_frames:\n    break\n'))
        for path in (UNIT/'worked').glob('*.asm'):
            text = path.read_text()
            self.assertNotIn('.include',text)
            self.assertNotIn('wav_reverse_frames:',text)
            if path.name in ['parser-publication.asm','channel-histories.asm','encoder-capacity.asm']:
                self.assertTrue(text.endswith(helper))

    def test_observations_keep_failure_publication_channel_order_and_units_distinct(self):
        lesson = json.loads((UNIT/'lesson.json').read_text())
        self.assertEqual(len(lesson['steps']),20)
        quizzes = [s for s in lesson['steps'] if s['kind']=='prediction']
        self.assertEqual(len(quizzes),10)
        self.assertEqual([s['correctChoice'] for s in quizzes],['abc'[i%3] for i in range(10)])
        debug = {s['id']:s for s in lesson['steps'] if s['kind']=='debug'}
        self.assertEqual(len(debug),5)
        self.assertEqual(sum(len(s['stops']) for s in debug.values()),10)
        self.assertTrue(all(s['source'].startswith('worked/') for s in lesson['steps'] if s['kind']!='assessment'))
        self.assertEqual(int(debug['observe-chunk-offsets']['stops'][-1]['registers']['$t2'],16),1<<32)
        failed = debug['observe-publication']['stops'][-1]['registers']
        self.assertEqual(failed['$v0'],'0x0000000000000001')
        self.assertEqual([failed['$s'+str(i)] for i in range(4)],['0xa5a5a5a5a5a5a5a5']*4)
        filtered = debug['observe-channel-histories']['stops'][0]['registers']
        values = [int(filtered['$s'+str(i)],16) for i in range(8)]
        values = [v-(1<<64) if v>>63 else v for v in values]
        self.assertEqual(values,[1500,-1500,1500,500,500,500,4596,-3596])
        self.assertEqual(int(debug['observe-encoder-boundary']['stops'][-1]['registers']['$v1'],16),60)
        self.assertEqual(int(debug['observe-frame-addresses']['stops'][0]['registers']['$t4'],16),12)

    def test_export_is_complete_deterministic_and_contains_no_instructor_sources(self):
        data = json.loads((UNIT/'package.json').read_text())
        names = ['package.json',data['entry'],data['assignment'],data['nativeLesson'],*data['supportingFiles']]
        self.assertEqual(len(names),17)
        self.assertEqual(len(names),len(set(names)))
        self.assertEqual(set(names),{str(p.relative_to(UNIT)) for p in UNIT.rglob('*') if p.is_file()})
        with tempfile.TemporaryDirectory() as directory:
            first,second = Path(directory)/'a.zip',Path(directory)/'b.zip'
            bundle(UNIT,first);bundle(UNIT,second)
            self.assertEqual(first.read_bytes(),second.read_bytes())
            with zipfile.ZipFile(first) as archive:
                self.assertEqual(len(archive.namelist()),17)
                self.assertTrue(all(n.startswith('031-pcm-wav/') for n in archive.namelist()))
                self.assertFalse(any(re.search(r'instructor|reference|alternative|solution',n) for n in archive.namelist()))


if __name__=='__main__':unittest.main()
