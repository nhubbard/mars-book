"""Contract checks for the unpublished Base64 cumulative exercise draft."""
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest

import book


class Base64ConstructionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.folder = book.ROOT / 'drafts/base64-construction'
        cls.steps = json.loads((cls.folder / 'steps.json').read_text())['steps']
        cls.recipes = json.loads((cls.folder / 'inputs.json').read_text())['recipes']
        cls.source = (cls.folder / 'main.asm').read_text()
        spec = importlib.util.spec_from_file_location('base64_prepare_tests', cls.folder / 'prepare.py')
        cls.prep = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.prep)

    def test_native_order_regions_and_recipe_boundaries(self):
        self.assertEqual((43, 37, 52), (len(self.steps), sum(s['kind'] == 'author' for s in self.steps), len(self.recipes)))
        order = [path.stem for path, _ in book.sources('drafts/base64.tex')
                 if path.parent.name == '028-base64' and re.fullmatch(r's[0-9]{2}', path.stem)]
        self.assertEqual([s['id'] for s in self.steps], order)
        self.assertEqual(37, len(re.findall(r'^# BEGIN ', self.source, re.M)))
        self.assertEqual(52, len({q['id'] for q in self.recipes}))
        for q in self.recipes:
            if q['id'].endswith('-limit'):
                self.assertEqual(str(2**63), q['count'])
        variants = (book.ROOT / 'includes/chapters/028-base64/variants.tex').read_text()
        self.assertEqual({q['id'] for q in self.recipes if not q['id'].endswith('-1')},
                         set(re.findall(r'\\texttt\{(s[0-9]+-[a-z]+)\}', variants)))
        for q in self.recipes:
            self.assertEqual(q['checkpoint'], q['functionCheckpoint'])
            self.assertEqual(q['target'] + '_return' if q['checkpoint'] == 'function_return'
                             else q['checkpoint'], q['callerCheckpoint'])
        for step in self.steps:
            self.assertEqual(2, len(step['hints']))
            self.assertNotEqual(step['hints'][0]['text'], step['hints'][1]['text'])
            self.assertNotIn('correctChoice', step['quiz'])
            self.assertNotIn('answer', step['quiz'])
            text = (book.ROOT / 'includes/chapters/028-base64' / (step['id'] + '.tex')).read_text()
            if step['kind'] == 'author':
                self.assertEqual(1, self.source.count('# BEGIN ' + step['region'] + '\n'))
                self.assertEqual(1, len(book.CODE.findall(text)))
                self.assertIn(r'\texttt{' + step['checkpoint'].replace('_', r'\_') + '}', text)
                self.assertTrue(any(q['step'] == step['id'] for q in self.recipes))
            else:
                self.assertEqual([], book.CODE.findall(text))
        figures = json.loads((self.folder / 'captures/plan.json').read_text())['figures']
        self.assertEqual(4, len(figures))
        for figure in figures:
            q = next(q for q in self.recipes if q['id'] == figure['recipe'])
            self.assertEqual((q['step'], q['checkpoint']), (figure['step'], figure['checkpoint']))
            self.assertEqual('IntelliJ Light', figure['theme'])
            self.assertIn('no IDE capture', figure['status'])

    def test_preparation_retains_student_code_and_full_width_controls(self):
        bodies = lambda text: re.findall(r'^# BEGIN .*?^# END .*?$', text, re.M | re.S)
        for q in self.recipes:
            prepared = self.prep.prepare(self.source, q)
            self.assertEqual(bodies(self.source), bodies(prepared))
            self.assertIn('mode: .word ' + str(self.prep.TARGETS.index(q['target'])) + '\n', prepared)
        q = next(q for q in self.recipes if q['target'] == 'encode_scalar')
        prepared = self.prep.prepare(self.source, {**q, 'inputHex': '', 'count': str(2**63), 'capacity': str(2**64 - 1)})
        self.assertIn('count: .dword -9223372036854775808\n', prepared)
        self.assertIn('capacity: .dword -1\n', prepared)
        # An authored comment is work, even while the function is still unfinished.
        changed = self.source.replace('# BEGIN enc_guard\n', '# BEGIN enc_guard\n# student note\n', 1)
        self.assertIn('# student note\n', self.prep.prepare(changed, q))

    def test_rejects_invalid_or_unsafe_observations(self):
        q = next(q for q in self.recipes if q['target'] == 'encode_scalar')
        for changes in [{'count': '-1'}, {'count': '01'}, {'capacity': str(2**64)},
                        {'capacity': 3}, {'inputHex': 'FF'}, {'inputHex': 'f'},
                        {'inPlace': True}, {'inPlace': 0}, {'target': 'unknown'},
                        {'inputHex': '', 'count': '3'}, {'inputHex': '00' * 5465}]:
            with self.subTest(changes=changes), self.assertRaises(ValueError):
                self.prep.prepare(self.source, {**q, **changes})
        for target, data in [('digit', ''), ('validate', '41'), ('encode12_msa', '00'),
                             ('decode16_msa', '3d' * 16)]:
            with self.subTest(target=target), self.assertRaises(ValueError):
                self.prep.prepare(self.source, {**q, 'target': target, 'inputHex': data, 'count': str(len(data) // 2)})
        with self.assertRaises(ValueError):
            self.prep.prepare(self.source.replace('source_end:', 'missing:'), q)
        with self.assertRaises(ValueError):
            self.prep.prepare(self.source.replace('mode: .word 2', 'mode: .word 3'), q)
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / 'kept.asm'
            output.write_text('student work')
            result = subprocess.run([sys.executable, str(self.folder / 'prepare.py'), q['id'],
                                     '--output', str(output)], capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual('student work', output.read_text())


if __name__ == '__main__':
    unittest.main()
