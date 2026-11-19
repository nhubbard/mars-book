"""Fixed QOI teaching examples and non-destructive fixture preparation."""
import importlib.util
from pathlib import Path
import struct
import subprocess
import sys
import tempfile
import unittest

import book


class QoiFixtureTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path = book.ROOT / 'drafts/qoi-fixtures/fixture.py'
        spec = importlib.util.spec_from_file_location('qoi_fixture_tests', cls.path)
        cls.fixture = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.fixture)

    def test_worked_offsets_and_rgba_values(self):
        width, height, pixels, data = self.fixture.material('trace')
        self.assertEqual((8, 1, 32, 38), (width, height, len(pixels), len(data)))
        self.assertEqual(b'qoif' + struct.pack('>IIBB', 8, 1, 4, 0), data[:14])
        for offset, encoded in [(14, 'c0'), (15, 'feff0000'), (19, 'c0'), (20, '35'),
                                (21, '7f'), (22, 'a597'), (24, 'ff07060580'), (29, '04')]:
            raw = bytes.fromhex(encoded)
            self.assertEqual(raw, data[offset:offset + len(raw)])
        self.assertEqual(bytes(7) + b'\x01', data[30:])
        self.assertEqual(bytes.fromhex('07060580'), pixels[24:28])
        self.assertEqual(bytes.fromhex('010101ff'), pixels[28:32])
        for pixel in [pixels[i:i + 4] for i in range(0, len(pixels), 4)]:
            self.assertLess(sum(a * b for a, b in zip(pixel, (3, 5, 7, 11))), 6631)

    def test_sprite_orientation_and_preview_domain(self):
        width, height, pixels, encoded = self.fixture.material('sprite')
        self.assertEqual((8, 8, 256, 61), (width, height, len(pixels), len(encoded)))
        self.assertEqual(bytes.fromhex('163c7edbff244281'), self.fixture.ROWS)
        white_columns = [{3, 5, 6}, {2, 3, 4, 5}, {1, 2, 3, 4, 5, 6},
                         {0, 1, 3, 4, 6, 7}, set(range(8)), {2, 5}, {1, 6}, {0, 7}]
        for row, columns in enumerate(white_columns):
            for column in range(8):
                pixel = pixels[4 * (8 * row + column):4 * (8 * row + column + 1)]
                self.assertEqual(bytes((255, 255, 255, 255) if column in columns else (0, 0, 0, 255)), pixel)
        preview = self.fixture.fixture('trace', 'ppm')
        self.assertEqual(b'P6\n8 1\n255\n', preview[:11])
        self.assertEqual(24, len(preview) - 11)
        with self.assertRaises(ValueError):
            self.fixture.material('unknown')
        with self.assertRaises(ValueError):
            self.fixture.fixture('trace', 'unknown')

    def test_writer_refuses_existing_output(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'image.qoi'
            command = [sys.executable, str(self.path), str(path), '--fixture', 'trace']
            result = subprocess.run(command, capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            expected = self.fixture.fixture('trace')
            self.assertEqual(expected, path.read_bytes())
            result = subprocess.run(command, capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertEqual(expected, path.read_bytes())
