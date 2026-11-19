"""Cumulative CBOR source preservation, caller recipes and native composition authority."""
import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest

import book


class CborConstructionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.folder = book.ROOT/'drafts/cbor-construction'
        cls.source = (cls.folder/'main.asm').read_text()
        cls.steps = json.loads((cls.folder/'steps.json').read_text())['steps']
        cls.recipes = json.loads((cls.folder/'inputs.json').read_text())['recipes']
        spec = importlib.util.spec_from_file_location('cbor_prepare', cls.folder/'prepare.py')
        cls.prep = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.prep)

    def test_unique_regions_recipes_and_native_step_order(self):
        regions = [s['region'] for s in self.steps if s['region']]
        self.assertEqual((56, 52, 57), (len(self.steps), len(regions), len(self.recipes)))
        self.assertEqual(len(regions), len(set(regions)))
        self.assertEqual(set(regions), set(re.findall(r'^# BEGIN (\w+)$', self.source, re.M)))
        self.assertEqual(set(regions), set(re.findall(r'^# END (\w+)$', self.source, re.M)))
        for region in regions:
            self.assertIn('after_'+region+':\n    nop\n', self.source)
        paths = [p for p, _ in book.sources('drafts/cbor-telemetry.tex')]
        self.assertEqual([s['id'] for s in self.steps], [p.stem for p in paths if re.fullmatch(r's\d\d', p.stem)])
        self.assertEqual(57, len({r['id'] for r in self.recipes}))
        for step in self.steps:
            native = (book.ROOT/'includes/chapters/032-cbor-telemetry'/(step['id']+'.tex')).read_text()
            self.assertEqual(2, len(step['hints']))
            self.assertNotEqual(step['hints'][0]['text'], step['hints'][1]['text'])
            self.assertEqual(3, len(step['quiz']['choices']))
            self.assertNotIn('correctChoice', step['quiz'])
            self.assertNotIn('answer', step['quiz'])
            for rid in step['recipes']:
                recipe = next(r for r in self.recipes if r['id'] == rid)
                self.assertEqual(step['id'], recipe['step'])
                self.assertIn(recipe['target'], self.prep.TARGETS)
                self.assertIn(recipe['callerCheckpoint']+':', self.source)
                self.assertIn(r'\texttt{'+rid+'}', native)
                self.assertIn(r'\texttt{'+recipe['callerCheckpoint'].replace('_', r'\_')+'}', native)

    def test_preparation_preserves_all_authored_instructions(self):
        source = self.source.replace('# BEGIN h_prefix\n', '# BEGIN h_prefix\n# student comment\n', 1)
        for recipe in self.recipes:
            prepared = self.prep.prepare(source, recipe)
            self.assertEqual(source.split('\n.text\n')[1], prepared.split('\n.text\n')[1])
            self.assertIn('mode: .word '+str(self.prep.TARGETS.index(recipe['target'])), prepared)
        self.assertEqual('', next(r for r in self.recipes if r['target'] == 'decode')['recordHex'])
        self.assertEqual(352, len(next(r for r in self.recipes if r['target'] == 'bound')['recordHex']))
        mixed = next(s for s in self.steps if s['id'] == 's55')
        self.assertEqual({'encode', 'decode'}, {r['target'] for r in self.recipes if r['id'] in mixed['recipes']})

    def test_full_width_controls_retain_unsigned_patterns(self):
        recipe = self.recipes[0]
        for value in (0, 2**32, 2**63, 2**64-1):
            changed = {**recipe, **{key: str(value) for key in self.prep.CONTROLS if key != 'writeMajor'}}
            prepared = self.prep.prepare(self.source, changed)
            signed = value if value < 2**63 else value-2**64
            for key, label in self.prep.CONTROLS.items():
                if key != 'writeMajor':
                    self.assertIn(label+': .dword '+str(signed)+'\n', prepared)

    def test_invalid_controls_data_and_boundaries_are_rejected(self):
        recipe = self.recipes[0]
        for invalid in ('-1', '01', '1.0', '', str(2**64), 1, True):
            with self.subTest(invalid=invalid), self.assertRaises(ValueError):
                self.prep.prepare(self.source, {**recipe, 'count': invalid})
        for key, invalid in [('target', 'unknown'), ('writeMajor', '3'), ('inputHex', 'AA'),
                             ('inputHex', 'a'), ('inputHex', '00'*1281), ('recordHex', '00'*177)]:
            with self.subTest(key=key), self.assertRaises(ValueError):
                self.prep.prepare(self.source, {**recipe, key: invalid})
        for broken in (self.source.replace('\n.text\n', '\n.text\n.text\n'),
                       self.source.replace('source_end:', 'missing_end:'),
                       self.source.replace('mode: .word 0', 'mode: .word 0\nmode: .word 0')):
            with self.assertRaises(ValueError):
                self.prep.prepare(broken, recipe)

    def test_cli_exclusive_output_and_unknown_recipe(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)/'prepared.asm'
            cmd = [sys.executable, str(self.folder/'prepare.py'), 's01-1', '--output', str(output)]
            result = subprocess.run(cmd, capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            original = output.read_bytes()
            result = subprocess.run(cmd, capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual(original, output.read_bytes())
            result = subprocess.run(cmd[:2]+['unknown']+cmd[3:], capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual(original, output.read_bytes())

    def test_typed_field_observations_reorder_without_changing_values(self):
        trace = bytes.fromhex(self.recipes[0]['inputHex'])
        root, optional, readings, identifier, counter = trace[:2], trace[2:12], trace[12:35], trace[35:41], trace[41:]
        selected = {r['step']: bytes.fromhex(r['inputHex']) for r in self.recipes if r['step'] in ('s25', 's26', 's27')}
        self.assertEqual(root+counter+optional+readings+identifier, selected['s25'])
        self.assertEqual(root+identifier+optional+readings+counter, selected['s26'])
        self.assertEqual(selected['s26'], selected['s27'])
        self.assertEqual(bytes.fromhex('00ff41'), selected['s26'][5:8])
        self.assertEqual({52}, {len(raw) for raw in selected.values()})
        inline = next(r for r in self.recipes if r['id'] == 's33-1')
        self.assertEqual('size_exit', inline['callerCheckpoint'])

    def test_capture_plans_have_no_claimed_pixels(self):
        plans = json.loads((self.folder/'captures/plan.json').read_text())['figures']
        self.assertEqual(4, len(plans))
        for plan in plans:
            recipe = next(r for r in self.recipes if r['id'] == plan['recipe'])
            self.assertEqual(recipe['callerCheckpoint'], plan['checkpoint'])
            self.assertEqual('IntelliJ Light', plan['theme'])
            self.assertIn('no IDE capture', plan['status'])
