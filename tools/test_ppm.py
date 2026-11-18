"""PPM exercise protocol and student-work preservation checks."""
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest

import book


class PpmConstructionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.folder = book.ROOT / 'drafts/ppm-construction'
        cls.steps = json.loads((cls.folder / 'steps.json').read_text())['steps']
        cls.recipes = json.loads((cls.folder / 'inputs.json').read_text())['recipes']
        cls.source = (cls.folder / 'main.asm').read_text()
        spec = importlib.util.spec_from_file_location('ppm_prepare_tests', cls.folder / 'prepare.py')
        cls.prep = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.prep)

    def test_native_order_quizzes_and_observation_boundaries(self):
        self.assertEqual((42, 37, 62), (len(self.steps), sum(s['kind'] == 'author' for s in self.steps), len(self.recipes)))
        order = [p.stem for p, _ in book.sources('drafts/binary-ppm.tex')
                 if p.parent.name == '029-binary-ppm' and re.fullmatch(r's[0-9]{2}', p.stem)]
        self.assertEqual([s['id'] for s in self.steps], order)
        self.assertEqual(62, len({q['id'] for q in self.recipes}))
        self.assertEqual(37, len(re.findall(r'^# BEGIN ', self.source, re.M)))
        for s in self.steps:
            self.assertEqual(2, len(s['hints']))
            self.assertNotEqual(s['hints'][0]['text'], s['hints'][1]['text'])
            self.assertNotIn('correctChoice', s['quiz'])
            self.assertNotIn('answer', s['quiz'])
            if s['kind'] == 'author':
                self.assertTrue(s['recipes'])
                text = (book.ROOT / 'includes/chapters/029-binary-ppm' / (s['id'] + '.tex')).read_text()
                self.assertIn(r'\texttt{' + s['checkpoint'].replace('_', r'\_') + '}', text)
                self.assertIn(r'\texttt{' + s['recipes'][0] + '}', text)
        for q in self.recipes:
            self.assertEqual(q['checkpoint'], q['functionCheckpoint'])
            self.assertEqual(q['target'] + '_return' if q['checkpoint'] == 'function_return'
                             else q['checkpoint'], q['callerCheckpoint'])
            if q['id'].endswith('-limit'):
                self.assertEqual(str(2**63), q['count'] if q['target'] in ['parse', 'save'] else q['capacity'])
        variants = (book.ROOT / 'includes/chapters/029-binary-ppm/variants.tex').read_text()
        self.assertTrue({q['id'] for q in self.recipes if not q['id'].endswith('-1')}.issubset(
            set(re.findall(r'\\texttt\{(s[0-9]+-[a-z-]+)\}', variants))))
        figures = json.loads((self.folder / 'captures/plan.json').read_text())['figures']
        self.assertEqual(4, len(figures))
        by_id = {q['id']: q for q in self.recipes}
        for figure in figures:
            self.assertEqual('IntelliJ Light', figure['theme'])
            self.assertIn('no IDE capture', figure['status'])
            self.assertEqual(by_id[figure['recipe']]['callerCheckpoint'], figure['checkpoint'])

    def test_preparation_preserves_authored_regions_and_unsigned_controls(self):
        bodies = lambda t: re.findall(r'^# BEGIN .*?^# END .*?$', t, re.M | re.S)
        modified = self.source.replace('# BEGIN p_bound\n', '# BEGIN p_bound\n# student explanation\n', 1)
        for q in self.recipes:
            result = self.prep.prepare(modified, q)
            self.assertEqual(bodies(modified), bodies(result))
            self.assertIn('mode: .word ' + str(self.prep.TARGETS.index(q['target'])) + '\n', result)
        q = next(q for q in self.recipes if q['id'] == 's02-limit')
        result = self.prep.prepare(modified, q)
        self.assertIn('byte_count: .dword -9223372036854775808\n', result)
        q = next(q for q in self.recipes if q['id'] == 's30-width-ten')
        result = self.prep.prepare(modified, q)
        self.assertIn('dimensions: .dword 4294967306\n', result)
        # The helper prepares only controls; full returned dimensions have separate storage.
        self.assertIn('count: .dword -1\n', result)

    def test_rejects_bad_domains_and_keeps_existing_files(self):
        q = self.recipes[0]
        for changes in [{'count': '-1'}, {'count': '01'}, {'capacity': str(2**64)},
                        {'width': str(2**32)}, {'height': 1}, {'fileHex': 'FF'},
                        {'pixelHex': '00'}, {'target': 'unknown'}, {'inputExists': 1},
                        {'readChunk': 0}, {'writeChunk': True}, {'firstDescriptor': 32},
                        {'failReadAfter': -1}, {'outputFileHex': '0'}]:
            with self.subTest(changes=changes), self.assertRaises(ValueError):
                self.prep.prepare(self.source, {**q, **changes})
        enc = next(q for q in self.recipes if q['target'] == 'encode')
        with self.assertRaises(ValueError):
            self.prep.prepare(self.source, {**enc, 'pixelHex': ''})
        for source in [self.source.replace('source_end:', 'missing:'),
                       self.source.replace('mode: .word 0', 'mode: .word 3')]:
            with self.assertRaises(ValueError):
                self.prep.prepare(source, q)
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / 'kept.asm'
            output.write_text('student work')
            result = subprocess.run([sys.executable, str(self.folder / 'prepare.py'), q['id'],
                                     '--output', str(output)], capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual('student work', output.read_text())
            source = Path(directory) / 'working.asm'
            source.write_text(self.source)
            created = Path(directory) / 'new.asm'
            result = subprocess.run([sys.executable, str(self.folder / 'prepare.py'), 's30-width-ten',
                                     '--source', str(source), '--output', str(created)], capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertEqual(self.source, source.read_text())
            self.assertIn('dimensions: .dword 4294967306\n', created.read_text())


if __name__ == '__main__':
    unittest.main()
