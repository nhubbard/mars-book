"""QOI cumulative publication order and safe observation preparation."""
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest

import book


class QoiConstructionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.folder = book.ROOT / 'drafts/qoi-construction'
        cls.steps = json.loads((cls.folder / 'steps.json').read_text())['steps']
        cls.recipes = json.loads((cls.folder / 'inputs.json').read_text())['recipes']
        cls.source = (cls.folder / 'main.asm').read_text()
        spec = importlib.util.spec_from_file_location('qoi_prepare_tests', cls.folder / 'prepare.py')
        cls.prep = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.prep)

    def test_native_order_and_observation_protocol(self):
        self.assertEqual((65, 57, 65), (len(self.steps), sum(s['kind'] == 'author' for s in self.steps), len(self.recipes)))
        order = [p.stem for p, _ in book.sources('drafts/qoi.tex')
                 if p.parent.name == '030-qoi' and re.fullmatch(r's[0-9]{2}', p.stem)]
        self.assertEqual([s['id'] for s in self.steps], order)
        chapter_files = [p for p, _ in book.sources('drafts/qoi.tex') if p.parent.name == '030-qoi']
        self.assertEqual(len(chapter_files), len(set(chapter_files)),
                         'Each authored chapter fragment appears once in the reading sequence')
        by_step = {q['step']: q for q in self.recipes}
        for step in self.steps:
            self.assertEqual(2, len(step['hints']))
            self.assertNotEqual(step['hints'][0]['text'], step['hints'][1]['text'])
            self.assertNotIn('correctChoice', step['quiz'])
            self.assertNotIn('answer', step['quiz'])
            text = (book.ROOT / 'includes/chapters/030-qoi' / (step['id'] + '.tex')).read_text()
            self.assertIn(r'\texttt{' + step['id'] + '-1}', text)
            recipe = by_step[step['id']]
            self.assertEqual(step['checkpoint'], recipe['functionCheckpoint'])
            self.assertEqual(step['target'] + '_return' if step['checkpoint'] == 'function_return'
                             else step['checkpoint'], recipe['callerCheckpoint'])
            if step['kind'] == 'author':
                self.assertIn(r'\texttt{' + step['checkpoint'].replace('_', r'\_') + '}', text)
                self.assertIn('after_' + step['region'] + ':\n    nop\n', self.source)
        figures = json.loads((self.folder / 'captures/plan.json').read_text())['figures']
        self.assertEqual(4, len(figures))
        for figure in figures:
            self.assertEqual('IntelliJ Light', figure['theme'])
            self.assertIn('no IDE capture', figure['status'])
            self.assertEqual(by_step[figure['step']]['callerCheckpoint'], figure['checkpoint'])

    def test_preparation_preserves_authored_code_and_all_control_bits(self):
        bodies = lambda text: re.findall(r'^# BEGIN .*?^# END .*?$', text, re.M | re.S)
        source = self.source.replace('# BEGIN b_unpack\n', '# BEGIN b_unpack\n# student comment\n', 1)
        for recipe in self.recipes:
            prepared = self.prep.prepare(source, recipe)
            self.assertEqual(bodies(source), bodies(prepared))
            self.assertIn('mode: .word ' + str(self.prep.TARGETS.index(recipe['target'])), prepared)
        q = next(q for q in self.recipes if q['target'] == 'validate')
        prepared = self.prep.prepare(source, {**q, 'count': str(2**63)})
        self.assertIn('byte_count: .dword -9223372036854775808', prepared)
        q = next(q for q in self.recipes if q['target'] == 'load')
        prepared = self.prep.prepare(source, {**q, 'capacity': str(2**64-1)})
        self.assertIn('byte_capacity: .dword -1', prepared)
        q = next(q for q in self.recipes if q['target'] == 'encode')
        prepared = self.prep.prepare(source, {**q, 'channels': str(2**32-1)})
        self.assertIn('metadata: .word 8,1,-1,0', prepared)
        self.assertIn('count: .dword -1', prepared)

    def test_rejects_invalid_domains_and_keeps_existing_work(self):
        q = self.recipes[0]
        for changes in [{'count': '-1'}, {'count': '01'}, {'capacity': str(2**64)},
                        {'width': str(2**32)}, {'height': 1}, {'fileHex': 'FF'},
                        {'rgbaHex': '0'}, {'target': 'unknown'}, {'inputExists': 1},
                        {'readChunk': 0}, {'writeChunk': True}, {'firstDescriptor': 32},
                        {'failReadAfter': -1}, {'outputFileHex': '0'}, {'callerOnly': 1}]:
            with self.subTest(changes=changes), self.assertRaises(ValueError):
                self.prep.prepare(self.source, {**q, **changes})
        enc = next(q for q in self.recipes if q['target'] == 'encode')
        with self.assertRaises(ValueError):
            self.prep.prepare(self.source, {**enc, 'rgbaHex': ''})
        for source in [self.source.replace('source_end:', 'missing:'),
                       self.source.replace('mode: .word 0', 'mode: .word 0\nmode: .word 0')]:
            with self.assertRaises(ValueError):
                self.prep.prepare(source, q)
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / 'kept.asm'
            output.write_text('student work')
            command = [sys.executable, str(self.folder / 'prepare.py'), q['id'], '--output', str(output)]
            result = subprocess.run(command, capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual('student work', output.read_text())
            created = Path(directory) / 'new.asm'
            result = subprocess.run(command[:-1] + [str(created)], capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertTrue(created.read_text().startswith('.data'))
