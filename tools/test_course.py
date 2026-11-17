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
        for package in course.read_json(course.ROOT / 'course.json')['packages']:
            root, data = course.validate(package)
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

    def test_reading_rejects_missing_or_repeated_steps(self):
        root, data = course.validate(PACKAGE)
        reading = course.read_json(root / data['reading'])
        for changed in (reading['segments'][:-2], reading['segments'] + [reading['segments'][1]]):
            with patch.object(course, 'read_json', return_value={'formatVersion': 1, 'segments': changed}):
                with self.assertRaises(ValueError):
                    course.reading_segments(root, data, {'f01-project', 'f02-console', 'f03-before-add', 'f04-after-add', 'f05-after-one'})

    def test_author_passage_can_be_inserted_inside_chapter(self):
        original = course.read_json
        config = original(course.ROOT / 'book/book.json')
        content = 'book/interludes/001-before.md'
        config['chapters'][0]['insertions'] = [{'anchor': 's10', 'position': 'after', 'content': content}]
        def read(path):
            return config if path == course.ROOT / 'book/book.json' else original(path)
        with tempfile.TemporaryDirectory() as folder, patch.object(course, 'read_json', side_effect=read):
            output = course.book(Path(folder) / 'book.md').read_text()
        passage = (course.ROOT / content).read_text().strip()
        self.assertLess(output.index('Step 10'), output.index(passage))
        self.assertLess(output.index(passage), output.index('An immediate is part of the instruction'))

    def test_unknown_author_anchor_fails_build(self):
        original = course.read_json
        config = original(course.ROOT / 'book/book.json')
        config['chapters'][0]['insertions'] = [{'anchor': 'nonexistent', 'position': 'after', 'content': 'book/interludes/001-before.md'}]
        def read(path):
            return config if path == course.ROOT / 'book/book.json' else original(path)
        with tempfile.TemporaryDirectory() as folder, patch.object(course, 'read_json', side_effect=read):
            with self.assertRaises(ValueError):
                course.book(Path(folder) / 'book.md')

    def test_book_retains_diagrams_and_native_sources_in_student_archive(self):
        with tempfile.TemporaryDirectory() as folder:
            output = Path(folder) / 'book.md'
            text = course.book(output).read_text()
            self.assertIn('fixtures/assets/string-address.svg', text)
            self.assertIn('The service determines how an argument is used', text)
            archive = course.bundle(PACKAGE, Path(folder) / 'student.zip')
            with zipfile.ZipFile(archive) as zipped:
                self.assertIn(f'{PACKAGE}/fixtures/latex/string-address.tex', zipped.namelist())
                self.assertIn(f'{PACKAGE}/reading.json', zipped.namelist())


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
