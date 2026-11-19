"""Fixed PCM WAV teaching inputs, independent container reading and native order."""
import importlib.util
import io
from pathlib import Path
import struct
import subprocess
import sys
import tempfile
import unittest
import wave

import book


class PcmWavFixtureTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path = book.ROOT / 'drafts/pcm-wav-fixtures/fixture.py'
        spec = importlib.util.spec_from_file_location('pcm_wav_fixture_tests', cls.path)
        cls.fixture = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.fixture)

    def test_worked_chunk_extents_and_signed_samples(self):
        rate, channels, pcm, raw = self.fixture.material('trace')
        self.assertEqual((16000, 2, 16, 82), (rate, channels, len(pcm), len(raw)))
        self.assertEqual(b'RIFF'+struct.pack('<I', 74)+b'WAVE', raw[:12])
        for offset, tag, size in [(12, b'JUNK', 3), (24, b'fmt ', 16),
                                  (48, b'data', 16), (72, b'note', 1)]:
            self.assertEqual(tag+struct.pack('<I', size), raw[offset:offset+8])
        self.assertEqual(0xa5, raw[23]); self.assertEqual(0x7f, raw[81])
        self.assertEqual((1, 2, 16000, 64000, 4, 16), struct.unpack('<HHIIHH', raw[32:48]))
        self.assertEqual(pcm, raw[56:72])
        self.assertEqual((12000, -12000, 0, 16000, -8000, 0, 32767, -32768), struct.unpack('<8h', pcm))
        with wave.open(io.BytesIO(raw), 'rb') as reader:
            self.assertEqual((2, 2, 16000, 4), (reader.getnchannels(), reader.getsampwidth(),
                                              reader.getframerate(), reader.getnframes()))
            self.assertEqual(pcm, reader.readframes(5))

    def test_two_second_input_is_deterministic_and_stereo(self):
        rate, channels, pcm, raw = self.fixture.material('listen')
        self.assertEqual((16000, 2, 128000, 128056), (rate, channels, len(pcm), len(raw)))
        self.assertEqual((-4000, -12000, -11600, -3500), struct.unpack('<4h', pcm[:8]))
        self.assertEqual(pcm[:1280]*100, pcm)
        self.assertNotEqual(pcm[0:2], pcm[2:4])
        with wave.open(io.BytesIO(raw), 'rb') as reader:
            self.assertEqual(32000, reader.getnframes())
            self.assertEqual(pcm, reader.readframes(32001))
        self.assertEqual(raw, self.fixture.material('listen')[3])
        with self.assertRaises(ValueError):
            self.fixture.material('unknown')

    def test_helper_preserves_existing_work(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)/'input.wav'
            command = [sys.executable, str(self.path), str(output)]
            first = subprocess.run(command, capture_output=True, text=True)
            self.assertEqual(0, first.returncode, first.stderr)
            self.assertEqual(self.fixture.TRACE, output.read_bytes())
            again = subprocess.run(command+['--fixture', 'listen'], capture_output=True, text=True)
            self.assertNotEqual(0, again.returncode)
            self.assertEqual(self.fixture.TRACE, output.read_bytes())

    def test_native_passages_are_included_once(self):
        passages = [p for p, _ in book.sources('drafts/pcm-wav.tex')
                    if p.parent.name == '031-pcm-wav']
        self.assertEqual(90, len(passages))
        self.assertEqual(len(passages), len(set(passages)))
        self.assertEqual('chapter', passages[0].stem)
        self.assertEqual('purpose', passages[1].stem)
        self.assertEqual('sources', passages[-1].stem)
        result = subprocess.run([sys.executable, str(book.ROOT/'tools/book.py'),
                                 'validate', '--entry', 'drafts/pcm-wav.tex'],
                                capture_output=True, text=True)
        self.assertEqual(0, result.returncode, result.stderr)
