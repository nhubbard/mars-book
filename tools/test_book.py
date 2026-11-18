from pathlib import Path
import hashlib
import json
import re
import struct
import wave
import tempfile
import unittest
from unittest.mock import patch

import book


class NativeCompositionTests(unittest.TestCase):
    def test_actual_native_composition_and_highlight_integrity(self):
        self.assertEqual(491, len(book.validate()))
        self.assertEqual(1, len(book.validate('specimen.tex')))
        self.assertEqual(13, len(book.validate('drafts/matrix-transpose.tex')))
        self.assertEqual(16, len(book.validate('drafts/fixed-point-ball.tex')))
        self.assertEqual(15, len(book.validate('drafts/binary-packet-parser.tex')))
        self.assertEqual(18, len(book.validate('drafts/ring-buffer-logger.tex')))
        self.assertEqual(22, len(book.validate('drafts/utf8-scanner.tex')))
        self.assertEqual(18, len(book.validate('drafts/clipped-sprite-blitter.tex')))
        self.assertEqual(43, len(book.validate('drafts/expression-evaluator.tex')))
        self.assertEqual(46, len(book.validate('drafts/tiny-bytecode-interpreter.tex')))
        self.assertEqual(43, len(book.validate('drafts/rgb-formats.tex')))
        self.assertEqual(32, len(book.validate('drafts/pcm-mixing.tex')))
        self.assertEqual(43, len(book.validate('drafts/fixed-point-fir.tex')))
        self.assertEqual(44, len(book.validate('drafts/blocked-matmul.tex')))

    def test_pcm_construction_transcript_matches_editable_native_code(self):
        folder = book.ROOT / 'drafts/pcm-construction'
        transcript = json.loads((folder / 'steps.json').read_text())
        steps = transcript['steps']
        self.assertEqual(40, len(steps))
        self.assertEqual(30, sum(s['kind'] == 'author' for s in steps))
        self.assertEqual(40, len({s['quiz']['id'] for s in steps}))
        self.assertNotIn('correctChoice', (folder / 'steps.json').read_text())
        for step in steps:
            self.assertEqual(2, len(step['hints']))
            native = (book.ROOT / 'includes/chapters/020-pcm-mixing' / (step['id'] + '.tex')).read_text()
            self.assertEqual([step['code']] if step['kind'] == 'author' else [],
                             [match[1] for match in book.CODE.finditer(native)])
        recipes = json.loads((folder / 'inputs.json').read_text())['recipes']
        self.assertEqual(48, len(recipes))
        self.assertEqual(48, len({r['id'] for r in recipes}))
        self.assertEqual({s['id'] for s in steps if s['kind'] == 'author'},
                         {r['step'] for r in recipes})

    def test_pcm_listening_assets_and_native_waveform_agree(self):
        folder = book.ROOT / 'book/media/020-pcm-mixing'
        receipt = json.loads((folder / 'receipt.json').read_text())
        self.assertTrue(receipt['verifiedBothImplementations'])
        self.assertEqual(12, len(receipt['files']))
        samples = {}
        for name, record in receipt['files'].items():
            path = folder / name
            self.assertEqual(record['sha256'], hashlib.sha256(path.read_bytes()).hexdigest())
            with wave.open(str(path), 'rb') as sound:
                self.assertEqual((1, 2, 16000, 32000),
                                 (sound.getnchannels(), sound.getsampwidth(), sound.getframerate(), sound.getnframes()))
                raw = sound.readframes(sound.getnframes())
            self.assertEqual(record['pcmSha256'], hashlib.sha256(raw).hexdigest())
            samples[name] = struct.unpack('<' + 'h' * 32000, raw)
        for scene, expected_clipped in [('quiet', 0), ('clipping', 11000), ('phase-offset', 11000)]:
            sums = [a + b for a, b in zip(samples[scene + '-a.wav'], samples[scene + '-b.wav'])]
            expected = tuple(min(32767, max(-32768, value)) for value in sums)
            self.assertEqual(expected, samples[scene + '-saturated.wav'])
            self.assertEqual(tuple((value + 32768) % 65536 - 32768 for value in sums),
                             samples[scene + '-wrapped.wav'])
            self.assertEqual(expected_clipped, sum(a != b for a, b in zip(sums, expected)))
            self.assertEqual(expected_clipped, receipt['scenes'][scene]['clipped'])
        self.assertEqual(samples['quiet-saturated.wav'], samples['quiet-wrapped.wav'])
        figure = (book.ROOT / 'includes/chapters/020-pcm-mixing/listening.tex').read_text()
        plotted = re.findall(r'\\addplot\[[^\]]*\] coordinates \{([^}]*)\};\s*\\addlegendentry\{([^}]*)\}', figure)
        self.assertEqual(3, len(plotted))
        expected = {
            'Exact sum': [a + b for a, b in zip(samples['clipping-a.wav'][:64], samples['clipping-b.wav'][:64])],
            'Saturated': samples['clipping-saturated.wav'][:64],
            'Wrapped': samples['clipping-wrapped.wav'][:64],
        }
        for coordinates, label in plotted:
            points = [(int(x), int(y)) for x, y in re.findall(r'\((\d+),(-?\d+)\)', coordinates)]
            self.assertEqual(list(enumerate(expected[label])), points)

    def test_fir_construction_transcript_matches_native_edits(self):
        folder = book.ROOT / 'drafts/fir-construction'
        steps = json.loads((folder / 'steps.json').read_text())['steps']
        self.assertEqual(47, len(steps))
        self.assertEqual(36, sum(s['kind'] == 'author' for s in steps))
        self.assertEqual(47, len({s['quiz']['id'] for s in steps}))
        self.assertNotIn('correctChoice', (folder / 'steps.json').read_text())
        for step in steps:
            self.assertEqual(2, len(step['hints']))
            native = (book.ROOT / 'includes/chapters/021-fixed-point-fir' / (step['id'] + '.tex')).read_text()
            self.assertEqual([step['code']] if step['kind'] == 'author' else [],
                             [match[1] for match in book.CODE.finditer(native)])
        recipes = json.loads((folder / 'inputs.json').read_text())['recipes']
        self.assertEqual(66, len(recipes))
        self.assertEqual(66, len({r['id'] for r in recipes}))
        self.assertEqual({s['id'] for s in steps if s['kind'] == 'author'} | {'s42'},
                         {r['step'] for r in recipes})

    def test_matmul_construction_transcript_matches_native_edits(self):
        folder = book.ROOT / 'drafts/matmul-construction'
        steps = json.loads((folder / 'steps.json').read_text())['steps']
        self.assertEqual(50, len(steps))
        native_order = [path.stem for path, _ in book.sources('drafts/blocked-matmul.tex')
                        if path.parent.name == '022-blocked-matmul' and re.fullmatch(r's[0-9]{2}', path.stem)]
        self.assertEqual([step['id'] for step in steps], native_order)
        self.assertEqual(42, sum(s['kind'] == 'author' for s in steps))
        self.assertEqual(50, len({s['quiz']['id'] for s in steps}))
        self.assertNotIn('correctChoice', (folder / 'steps.json').read_text())
        for step in steps:
            self.assertEqual(2, len(step['hints']))
            native = (book.ROOT / 'includes/chapters/022-blocked-matmul' / (step['id'] + '.tex')).read_text()
            self.assertEqual([step['code']] if step['kind'] == 'author' else [],
                             [match[1] for match in book.CODE.finditer(native)])
        recipes = json.loads((folder / 'inputs.json').read_text())['recipes']
        self.assertEqual(58, len(recipes))
        self.assertEqual(58, len({r['id'] for r in recipes}))
        self.assertEqual({s['id'] for s in steps if s['kind'] == 'author'} | {'s48'},
                         {r['step'] for r in recipes})
        self.assertEqual(('block_reduction', 2),
                         next((r['checkpoint'], r['visit']) for r in recipes if r['step'] == 's48'))

    def test_fir_listening_samples_and_native_plot_agree(self):
        folder = book.ROOT / 'book/media/021-fixed-point-fir'
        receipt = json.loads((folder / 'receipt.json').read_text())
        self.assertTrue(receipt['verifiedBothImplementations'])
        self.assertEqual([4096] * 8, receipt['tapsOldestFirst'])
        self.assertEqual(6, len(receipt['files']))
        samples = {}
        for name, record in receipt['files'].items():
            path = folder / name
            self.assertEqual(record['sha256'], hashlib.sha256(path.read_bytes()).hexdigest())
            with wave.open(str(path), 'rb') as sound:
                self.assertEqual((1, 2, 16000, 32000),
                                 (sound.getnchannels(), sound.getsampwidth(), sound.getframerate(), sound.getnframes()))
                raw = sound.readframes(sound.getnframes())
            self.assertEqual(record['pcmSha256'], hashlib.sha256(raw).hexdigest())
            samples[name] = struct.unpack('<' + 'h' * 32000, raw)
        def low(position):
            u = position % 64
            return 375 * (u if u < 16 else 32 - u if u < 48 else u - 64)
        def signal(scene, position):
            high = (0, 12000, 0, -12000)[position % 4]
            return low(position) if scene == 'low' else high if scene == 'high' else low(position) + high
        for scene in ['low', 'high', 'mixed']:
            self.assertEqual(tuple(signal(scene, i) for i in range(32000)), samples[scene + '-input.wav'])
            wanted = tuple((sum(signal(scene, i - j) for j in range(8)) + 4) // 8 for i in range(32000))
            self.assertEqual(wanted, samples[scene + '-filtered.wav'])
        self.assertFalse(any(samples['high-filtered.wav']))
        self.assertEqual(samples['low-filtered.wav'], samples['mixed-filtered.wav'])
        figure = (book.ROOT / 'includes/chapters/021-fixed-point-fir/listening.tex').read_text()
        plotted = re.findall(r'\\addplot\[[^\]]*\] coordinates \{([^}]*)\};\s*\\addlegendentry\{([^}]*)\}', figure)
        self.assertEqual(2, len(plotted))
        expected = {'High input': samples['high-input.wav'][:16], 'Filtered': samples['high-filtered.wav'][:16]}
        for coordinates, label in plotted:
            points = [(int(x), int(y)) for x, y in re.findall(r'\((\d+),(-?\d+)\)', coordinates)]
            self.assertEqual(list(enumerate(expected[label])), points)

    def test_author_can_include_prose_between_any_two_segments(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / 'main.tex').write_text(r'\input{first}\input{author}\input{second}')
            for name in ['first', 'author', 'second']:
                (root / (name + '.tex')).write_text(name)
            before = {p.name: p.read_bytes() for p in root.iterdir()}
            self.assertEqual(['first', 'author', 'second'], [text for _, text in book.sources(root=root)[1:]])
            self.assertEqual(before, {p.name: p.read_bytes() for p in root.iterdir()})

    def test_missing_cyclic_and_escaping_include_fail(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            for text in [r'\input{missing}', r'\input{main}', r'\input{../escape}']:
                (root / 'main.tex').write_text(text)
                with self.assertRaises((ValueError, FileNotFoundError)):
                    book.sources(root=root)

    def test_stale_native_code_requires_a_fresh_ide_export(self):
        with patch.object(book, 'snippets', return_value=['li $v0, 999 # changed\n']):
            with self.assertRaisesRegex(ValueError, 'Source changed'):
                book.validate()
