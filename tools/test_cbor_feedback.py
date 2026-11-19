"""Deterministic, standalone public CBOR feedback and guided exercise export."""
import importlib.util
import json
from pathlib import Path
import re
import tempfile
import unittest

import book
import cbor_feedback
import cbor_guided


class CborFeedbackTests(unittest.TestCase):
    def test_completed_exports_are_reproducible_and_have_unique_case_names(self):
        previous = cbor_feedback.DEST
        try:
            with tempfile.TemporaryDirectory() as directory:
                cbor_feedback.DEST = Path(directory)
                counts = cbor_feedback.build()
                self.assertEqual({'head':278,'skip':56,'decode':84,'format':34,'bound':150,'encode':166,'caller':7}, counts)
                self.assertEqual(775, sum(counts.values()))
                for name,count in counts.items():
                    raw = (Path(directory)/(name+'.kts')).read_bytes()
                    self.assertEqual(raw, (previous/(name+'.kts')).read_bytes())
                    names = re.findall(r'^case\("((?:[^"\\]|\\.)*)"', raw.decode(), re.M)
                    self.assertEqual(count, len(names))
                    self.assertEqual(count, len(set(names)))
        finally:
            cbor_feedback.DEST = previous

    def test_encoder_feedback_includes_all_sample_count_shapes(self):
        bodies = ''.join(cbor_feedback.cases()['encode'])
        counts = {int(n) for n in re.findall(r'recordCheck\(example\(\d+uL,\d+,(\d+)\)', bodies)}
        self.assertEqual({0,1,10,64}, counts)
        self.assertIn('recordCheck(worked,37L)', bodies)
        self.assertIn('recordCheck(worked,36L)', bodies)
        self.assertIn('recordCheck(worked,Long.MIN_VALUE)', bodies)

    def test_guided_exports_are_exact_standalone_checkpoint_checks(self):
        previous = cbor_guided.DEST
        try:
            with tempfile.TemporaryDirectory() as directory:
                cbor_guided.DEST = Path(directory)
                counts = cbor_guided.build()
                self.assertEqual((56,109), (len(counts),sum(counts.values())))
                for name,count in counts.items():
                    raw = (Path(directory)/(name+'.kts')).read_bytes()
                    self.assertEqual(raw, (previous/(name+'.kts')).read_bytes())
                    text = raw.decode()
                    self.assertEqual(count, len(re.findall(r'^case\(', text, re.M)))
                    self.assertNotIn('runToExit()', text[text.index('case('):])
                mixed = (Path(directory)/'s55.kts').read_text()
                self.assertIn('runUntil("encode_return")', mixed)
                self.assertIn('runUntil("decode_return")', mixed)
                self.assertIn('runUntil("size_exit")', (Path(directory)/'s33.kts').read_text())
        finally:
            cbor_guided.DEST = previous

    def test_student_feedback_uses_public_inputs_and_no_quiz_keys(self):
        folder = book.ROOT/'drafts/cbor-construction'
        for path in (folder/'checks').rglob('*.kts'):
            text = path.read_text()
            self.assertNotIn('correctChoice', text)
            self.assertNotIn('quiz-keys.json', text)
            self.assertNotIn('mars-book-instructor', text)
        recipes = json.loads((folder/'inputs.json').read_text())['recipes']
        steps = json.loads((folder/'steps.json').read_text())['steps']
        self.assertEqual(57, len(recipes))
        for step in steps:
            path = folder/'checks/construction'/(step['id']+'.kts')
            for recipe in [r for r in recipes if r['id'] in step['recipes']]:
                self.assertIn(recipe['inputHex'], path.read_text())


if __name__ == '__main__':
    unittest.main()
