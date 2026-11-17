import copy
import hashlib
import json
from pathlib import Path
import struct
import tempfile
import unittest
from unittest.mock import patch
import zipfile
import zlib

import annotate
import course

PACKAGE = '001-console-calculation'


class PackageTests(unittest.TestCase):
    def test_real_package(self):
        _, data = course.validate(PACKAGE)
        self.assertEqual(22, len(data['steps']))

    def test_markdown_quizzes_match_structured_choices(self):
        root, data = course.validate(PACKAGE)
        for step in data['steps']:
            text = (root / step['content']).read_text()
            self.assertIn(step['quiz']['prompt'], text)
            for choice in step['quiz']['choices']:
                self.assertIn(f"**{choice['id'].upper()}** {choice['text']}", text)

    def test_rejects_escape_and_symlink(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / 'good').write_text('ok')
            (root / 'link').symlink_to(root / 'good')
            for bad in ('../escape', '/etc/passwd', 'link', 'missing'):
                with self.assertRaises(ValueError):
                    course.within(root, bad)

    def test_missing_or_forward_prerequisite_rejected(self):
        root, data = course.validate(PACKAGE)
        data['steps'][0]['prerequisites'] = ['s99']
        with patch.object(course, 'read_json', return_value=data):
            with self.assertRaises(ValueError):
                course.validate(PACKAGE)

    def test_student_archive_is_deterministic_and_allowlisted(self):
        with tempfile.TemporaryDirectory() as folder:
            a = course.bundle(PACKAGE, Path(folder) / 'a.zip')
            b = course.bundle(PACKAGE, Path(folder) / 'b.zip')
            self.assertEqual(a.read_bytes(), b.read_bytes())
            with zipfile.ZipFile(a) as archive:
                names = archive.namelist()
                self.assertIn(f'{PACKAGE}/starter/main.asm', names)
                self.assertFalse(any('reference/' in name or 'private.kts' in name or 'quiz-keys' in name for name in names))

    def test_book_preserves_human_interludes(self):
        paths = list((course.ROOT / 'book/interludes').glob('*.md'))
        before = {path: path.read_bytes() for path in paths}
        with tempfile.TemporaryDirectory() as folder:
            result = course.book(Path(folder) / 'book.md').read_text()
            self.assertIn('Step 22', result)
            self.assertIn('Figure f01-project planned', result)
        self.assertEqual(before, {path: path.read_bytes() for path in paths})


class AnnotationTests(unittest.TestCase):
    def setUp(self):
        def chunk(kind, data):
            return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data))
        # Synthetic white image solely for exercising annotation validation.
        self.png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 80, 40, 8, 2, 0, 0, 0)) + chunk(b'IDAT', zlib.compress((b'\x00' + b'\xff' * 240) * 40)) + chunk(b'IEND', b'')
        self.receipt = {'figure': 'test', 'theme': 'IntelliJ Light', 'assertionsPassed': True, 'pngSha256': hashlib.sha256(self.png).hexdigest(), 'viewport': {'width': 80, 'height': 40}, 'callouts': [{'label': 'A & B', 'box': {'x': 2, 'y': 2, 'width': 40, 'height': 20}}]}

    def test_annotation_embeds_original_and_escapes_label(self):
        with tempfile.TemporaryDirectory() as folder:
            png = Path(folder) / 'test.png'
            svg = Path(folder) / 'test.svg'
            png.write_bytes(self.png)
            annotate.render(png, self.receipt, svg)
            self.assertIn('A &amp; B', svg.read_text())
            self.assertIn('data:image/png;base64,', svg.read_text())

    def test_rejects_dark_theme_and_wrong_digest_and_outside_box(self):
        with tempfile.TemporaryDirectory() as folder:
            png = Path(folder) / 'test.png'
            png.write_bytes(self.png)
            for change in ('theme', 'digest', 'box'):
                receipt = copy.deepcopy(self.receipt)
                if change == 'theme':
                    receipt['theme'] = 'Dark'
                elif change == 'digest':
                    receipt['pngSha256'] = 'wrong'
                else:
                    receipt['callouts'][0]['box']['x'] = 100
                with self.assertRaises(ValueError):
                    annotate.render(png, receipt, Path(folder) / 'test.svg')


if __name__ == '__main__':
    unittest.main()
