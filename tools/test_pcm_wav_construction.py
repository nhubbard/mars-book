"""PCM WAV native construction order and preservation of student-authored code."""
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest

import book


class WavConstructionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.folder = book.ROOT/'drafts/pcm-wav-construction'
        cls.steps = json.loads((cls.folder/'steps.json').read_text())['steps']
        cls.recipes = json.loads((cls.folder/'inputs.json').read_text())['recipes']
        cls.source = (cls.folder/'main.asm').read_text()
        spec = importlib.util.spec_from_file_location('wav_prepare_tests', cls.folder/'prepare.py')
        cls.prep = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.prep)

    def test_native_order_regions_and_observation_protocol(self):
        self.assertEqual((64, 55, 65), (len(self.steps), sum(s['kind'] == 'author' for s in self.steps), len(self.recipes)))
        files = [p for p, _ in book.sources('drafts/pcm-wav.tex') if p.parent.name == '031-pcm-wav']
        self.assertEqual(len(files), len(set(files)))
        order = [p.stem for p in files if re.fullmatch(r's[0-9]{2}', p.stem)]
        self.assertEqual([s['id'] for s in self.steps], order)
        self.assertEqual(65, len({q['id'] for q in self.recipes}))
        for step in self.steps:
            self.assertEqual(2, len(step['hints']))
            self.assertNotEqual(step['hints'][0]['text'], step['hints'][1]['text'])
            self.assertEqual(3, len(step['quiz']['choices']))
            self.assertNotIn('correctChoice', step['quiz'])
            self.assertNotIn('answer', step['quiz'])
            native = (book.ROOT/'includes/chapters/031-pcm-wav'/(step['id']+'.tex')).read_text()
            selected = [q for q in self.recipes if q['id'] in step['recipes']]
            self.assertEqual(len(selected), len(step['recipes']))
            for q in selected:
                self.assertEqual(step['id'], q['step'])
                self.assertEqual(step['target'], q['target'])
                self.assertEqual(step['checkpoint'], q['functionCheckpoint'])
                self.assertEqual(step['target']+'_return' if step['checkpoint'] == 'function_return'
                                 else step['checkpoint'], q['callerCheckpoint'])
                self.assertIn(r'\texttt{'+q['id']+'}', native)
                self.assertIn(r'\texttt{'+q['callerCheckpoint'].replace('_', r'\_')+'}', native)
                self.assertIn(q['callerCheckpoint']+':', self.source)
            if step['kind'] == 'author':
                self.assertEqual(1, self.source.count('# BEGIN '+step['region']+'\n'))
                self.assertIn('after_'+step['region']+':\n    nop\n', self.source)
        plans = json.loads((self.folder/'captures/plan.json').read_text())['figures']
        self.assertEqual(4, len(plans))
        for plan in plans:
            recipe = next(q for q in self.recipes if q['id'] == plan['recipe'])
            self.assertEqual(plan['checkpoint'], recipe['callerCheckpoint'])
            self.assertEqual('IntelliJ Light', plan['theme'])
            self.assertIn('no IDE capture', plan['status'])

    def test_preparation_preserves_entire_instruction_section(self):
        source = self.source.replace('# BEGIN b_fields\n', '# BEGIN b_fields\n# student comment\n', 1)
        instructions = source.split('\n.text\n')[1]
        for recipe in self.recipes:
            with self.subTest(recipe=recipe['id']):
                prepared = self.prep.prepare(source, recipe)
                self.assertEqual(instructions, prepared.split('\n.text\n')[1])
                self.assertIn('mode: .word '+str(self.prep.TARGETS.index(recipe['target'])), prepared)
        first = self.recipes[0]
        prepared = self.prep.prepare(source, {**first, 'rate': str(2**32-1),
            'channels': str(2**32-1), 'frames': str(2**64-1), 'count': str(2**63), 'capacity': str(2**64-1)})
        self.assertIn('metadata: .word -1,-1\n    .dword -1', prepared)
        self.assertIn('byte_count: .dword -9223372036854775808', prepared)
        self.assertIn('byte_capacity: .dword -1', prepared)
        parse = next(q for q in self.recipes if q['target'] == 'parse')
        self.assertIn('metadata: .byte -91:64', self.prep.prepare(source, parse))

    def test_rejects_bad_domains_and_missing_boundaries(self):
        q = self.recipes[0]
        for changes in [{'frames': '-1'}, {'count': '01'}, {'capacity': str(2**64)},
                        {'rate': str(2**32)}, {'channels': 2}, {'fileHex': 'FF'},
                        {'fileHex': '0'}, {'target': 'unknown'}, {'inputExists': 1},
                        {'readChunk': 0}, {'writeChunk': True}, {'firstDescriptor': 32},
                        {'failReadAfter': -1}, {'outputFileHex': '0'}, {'outputWritable': 1},
                        {'kernelCount': False}, {'pcmHex': 'ff'*262177}]:
            with self.subTest(changes=list(changes)), self.assertRaises(ValueError):
                self.prep.prepare(self.source, {**q, **changes})
        enc = next(q for q in self.recipes if q['target'] == 'encode')
        kernel = next(q for q in self.recipes if q['target'] == 'kernel')
        for recipe in [{**enc, 'pcmHex': ''}, {**kernel, 'kernelCount': '65'},
                       {**kernel, 'kernelHex': ''}, {**kernel, 'coefficientHex': ''}]:
            with self.assertRaises(ValueError):
                self.prep.prepare(self.source, recipe)
        for source in [self.source.replace('source_end:', 'missing:'),
                       self.source.replace('mode: .word 0', 'mode: .word 0\nmode: .word 0'),
                       self.source.replace('kernel_window_end:', 'missing:'),
                       self.source.replace('\n.text\n', '\n.text\n.text\n')]:
            with self.assertRaises(ValueError):
                self.prep.prepare(source, q)

    def test_distinct_input_layouts_and_empty_audio(self):
        trace = next(q for q in self.recipes if q['id'] == 's07-1')
        extended = next(q for q in self.recipes if q['scenario'] == 'trace18')
        bad = next(q for q in self.recipes if q['scenario'] == 'missing-pad')
        empty = next(q for q in self.recipes if q['scenario'] == 'empty18')
        self.assertEqual((82, 84, 81, 58), tuple(len(bytes.fromhex(q['fileHex'])) for q in [trace, extended, bad, empty]))
        self.assertEqual(bytes.fromhex(trace['fileHex'])[56:72], bytes.fromhex(extended['fileHex'])[58:74])
        self.assertEqual(73, int.from_bytes(bytes.fromhex(bad['fileHex'])[4:8], 'little'))
        self.assertEqual('0', empty['frames'])
        self.assertEqual(b'data'+bytes(4), bytes.fromhex(empty['fileHex'])[38:46])
        zero = next(q for q in self.recipes if q['scenario'] == 'zero-filter')
        self.assertEqual('', zero['fileHex'])
        self.assertIn('metadata: .word 16000,2\n    .dword 0', self.prep.prepare(self.source, zero))

    def test_boundary_recipe_matches_native_numeric_fixture(self):
        q = next(q for q in self.recipes if q['scenario'] == 'boundary65')
        pcm = bytes.fromhex(q['fileHex'])
        lanes = [[int.from_bytes(pcm[4*n+2*c:4*n+2*c+2], 'little', signed=True)
                  for n in range(65)] for c in range(2)]
        actual = [[(sum(lanes[c][n-7:n+1])+4)//8 for c in range(2)] for n in [63, 64]]
        self.assertEqual([[-84, 913], [-6282, -5285]], actual)
        native = (book.ROOT/'includes/chapters/031-pcm-wav/s42.tex').read_text()
        self.assertIn('63 & $-84$ & 913', native)
        self.assertIn('64 & $-6282$ & $-5285$', native)

    def test_cli_never_replaces_existing_work(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)/'kept.asm'; output.write_text('student work')
            command = [sys.executable, str(self.folder/'prepare.py'), 's01-1', '--output', str(output)]
            result = subprocess.run(command, capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual('student work', output.read_text())
            fresh = Path(directory)/'new.asm'
            result = subprocess.run(command[:-1]+[str(fresh)], capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertIn('mode: .word 0', fresh.read_text())
