import copy
import hashlib
import json
import re
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
    def test_codec_distribution_retains_native_order_and_checks(self):
        root, data = course.validate('023-delta-varint')
        steps = json.loads((root / 'steps.json').read_text())['steps']
        self.assertEqual([s['id'] for s in steps], [s['id'] for s in data['steps']])
        native = course.ROOT / 'includes/chapters/023-delta-varint'
        for path in native.glob('*.tex'):
            self.assertEqual(path.read_bytes(), (root / 'native' / path.name).read_bytes())
        names = re.findall(r'\\input\{includes/chapters/023-delta-varint/([^}]+)\}',
                           (native / 'chapter.tex').read_text())
        reading = course.reading_segments(root, data)
        ordered = [segment['step'] if segment['kind'] == 'step' else
                   segment['figure'] if segment['kind'] == 'figure' else
                   Path(segment['typesetting']['nativeLatex']).stem for segment in reading]
        self.assertEqual(names, ordered)
        self.assertEqual(53, sum(s['completion']['type'] == 'cli-grade' for s in data['steps']))
        for step in data['steps']:
            self.assertEqual(2, len(step['hints']))
            if step['completion']['type'] == 'cli-grade':
                self.assertTrue((root / step['completion']['check']).is_file())
        with tempfile.TemporaryDirectory() as folder:
            archive = course.bundle(data['id'], Path(folder) / 'student.zip')
            with zipfile.ZipFile(archive) as zipped:
                self.assertEqual(len(course.student_files(root, data)), len(zipped.namelist()))
                self.assertFalse(any('quiz-key' in name or '/private' in name or '/reference/' in name
                                     for name in zipped.namelist()))

    def test_rsa_distribution_retains_native_order_and_guided_exports(self):
        root, data = course.validate('027-rsa-arithmetic')
        self.assertEqual(42, len(data['steps']))
        self.assertEqual(36, sum(step['completion']['type'] == 'cli-grade' for step in data['steps']))
        native = course.ROOT / 'includes/chapters/027-rsa-arithmetic'
        for path in native.glob('*.tex'):
            self.assertEqual(path.read_bytes(), (root / 'native' / path.name).read_bytes())
        names = re.findall(r'\\input\{includes/chapters/027-rsa-arithmetic/([^}]+)\}', (native / 'chapter.tex').read_text())
        reading = json.loads((root / 'reading.json').read_text())['segments']
        actual = [row['step'] if row['kind'] == 'step' else row['figure'] if row['kind'] == 'figure' else Path(row['typesetting']['nativeLatex']).stem for row in reading]
        self.assertEqual(names, actual)
        exported = {path.relative_to(root).as_posix() for path in course.student_files(root, data)}
        for step in data['steps']:
            if step['completion']['type'] == 'cli-grade':
                self.assertIn(step['completion']['check'], exported)
                self.assertNotIn('case("prepared ', (root / step['completion']['check']).read_text())
        self.assertFalse(any('quiz-key' in name or '/private' in name or '/reference/' in name for name in exported))
        self.assertEqual(35, len(list((root / 'checks/construction').glob('*.kts'))))
        self.assertEqual(9, len(data['publicChecks']))
        self.assertIn('readBytes(address,length)', data['environment']['graderRequirements'])

    def test_chacha_distribution_retains_native_order_and_guided_exports(self):
        root, data = course.validate('026-chacha20')
        self.assertEqual(42, len(data['steps']))
        self.assertEqual(40, sum(step['completion']['type'] == 'cli-grade' for step in data['steps']))
        native = course.ROOT / 'includes/chapters/026-chacha20'
        for path in native.glob('*.tex'):
            self.assertEqual(path.read_bytes(), (root / 'native' / path.name).read_bytes())
        names = re.findall(r'\\input\{includes/chapters/026-chacha20/([^}]+)\}', (native / 'chapter.tex').read_text())
        reading = json.loads((root / 'reading.json').read_text())['segments']
        actual = [row['step'] if row['kind'] == 'step' else row['figure'] if row['kind'] == 'figure' else Path(row['typesetting']['nativeLatex']).stem for row in reading]
        self.assertEqual(names, actual)
        exported = {path.relative_to(root).as_posix() for path in course.student_files(root, data)}
        for step in data['steps']:
            if step['completion']['type'] == 'cli-grade':
                self.assertIn(step['completion']['check'], exported)
                self.assertNotIn('case("prepared ', (root / step['completion']['check']).read_text())
        self.assertFalse(any('quiz-key' in name or '/private' in name or '/reference/' in name for name in exported))
        self.assertEqual(36, len(list((root / 'checks/construction').glob('*.kts'))))
        self.assertEqual(6, len(data['publicChecks']))
        self.assertIn('expectVectorLane', data['environment']['graderRequirements'])
        self.assertIn('readBytes(address,length)', data['environment']['graderRequirements'])

    def test_sha_distribution_retains_native_order_and_guided_exports(self):
        root, data = course.validate('025-sha256')
        self.assertEqual(47, len(data['steps']))
        self.assertEqual(43, sum(step['completion']['type'] == 'cli-grade' for step in data['steps']))
        native = course.ROOT / 'includes/chapters/025-sha256'
        for path in native.glob('*.tex'):
            self.assertEqual(path.read_bytes(), (root / 'native' / path.name).read_bytes())
        names = re.findall(r'\\input\{includes/chapters/025-sha256/([^}]+)\}', (native / 'chapter.tex').read_text())
        reading = json.loads((root / 'reading.json').read_text())['segments']
        actual = [row['step'] if row['kind'] == 'step' else row['figure'] if row['kind'] == 'figure' else Path(row['typesetting']['nativeLatex']).stem for row in reading]
        self.assertEqual(names, actual)
        exported = {path.relative_to(root).as_posix() for path in course.student_files(root, data)}
        for step in data['steps']:
            if step['completion']['type'] == 'cli-grade':
                self.assertIn(step['completion']['check'], exported)
                self.assertNotIn('case("prepared ', (root / step['completion']['check']).read_text())
        self.assertFalse(any('quiz-key' in name or '/private' in name or '/reference/' in name for name in exported))
        self.assertIn('readBytes(address,length)', data['environment']['graderRequirements'])

    def test_crc_distribution_retains_native_order_and_checks(self):
        root, data = course.validate('024-crc32')
        steps = json.loads((root / 'steps.json').read_text())['steps']
        self.assertEqual([s['id'] for s in steps], [s['id'] for s in data['steps']])
        native = course.ROOT / 'includes/chapters/024-crc32'
        for path in native.glob('*.tex'):
            self.assertEqual(path.read_bytes(), (root / 'native' / path.name).read_bytes())
        names = re.findall(r'\\input\{includes/chapters/024-crc32/([^}]+)\}',
                           (native / 'chapter.tex').read_text())
        reading = course.reading_segments(root, data)
        ordered = [segment['step'] if segment['kind'] == 'step' else
                   segment['figure'] if segment['kind'] == 'figure' else
                   Path(segment['typesetting']['nativeLatex']).stem for segment in reading]
        self.assertEqual(names, ordered)
        self.assertEqual(31, sum(s['completion']['type'] == 'cli-grade' for s in data['steps']))
        for step in data['steps']:
            self.assertEqual(2, len(step['hints']))
            if step['completion']['type'] == 'cli-grade':
                self.assertTrue((root / step['completion']['check']).is_file())
        with tempfile.TemporaryDirectory() as folder:
            archive = course.bundle(data['id'], Path(folder) / 'student.zip')
            with zipfile.ZipFile(archive) as zipped:
                self.assertEqual(len(course.student_files(root, data)), len(zipped.namelist()))
                self.assertFalse(any('quiz-key' in name or '/private' in name or '/reference/' in name
                                     for name in zipped.namelist()))

    def test_real_package(self):
        _, data = course.validate(PACKAGE)
        self.assertEqual(22, len(data['steps']))

    def test_registry_covers_package_manifests_and_roadmap(self):
        registry = course.read_json(course.ROOT / 'course.json')
        available = sorted(p.parent.name for p in (course.ROOT / 'packages').glob('*/package.json'))
        self.assertEqual(available, registry['packages'])
        entries = [entry for entry in registry['roadmap'] if entry['package'] is not None]
        self.assertEqual(registry['packages'], [entry['package'] for entry in entries])
        self.assertTrue(all(entry['status'] == 'text-package-draft' for entry in entries))

    def test_reading_quizzes_match_structured_choices(self):
        escapes = {'\\': r'\textbackslash{}', '_': r'\_', '&': r'\&', '%': r'\%',
                   '#': r'\#', '$': r'\$', '{': r'\{', '}': r'\}'}
        def latex(text):
            return ''.join(escapes.get(char, char) for char in text)
        for package in course.read_json(course.ROOT / 'course.json')['packages']:
            root, data = course.validate(package)
            for step in data['steps']:
                text = (root / step['content']).read_text()
                native = Path(step['content']).suffix == '.tex'
                prompt = step['quiz']['prompt']
                self.assertIn(latex(prompt) if native else prompt, text)
                if native and r'\begin{enumerate}[label=\Alph*.]' in text:
                    quiz = text.split(r'\begin{enumerate}[label=\Alph*.]', 1)[1].split(r'\end{enumerate}', 1)[0]
                    items = [item.strip() for item in quiz.split(r'\item')[1:]]
                    self.assertEqual([latex(choice['text']) for choice in step['quiz']['choices']], items)
                    self.assertEqual([chr(ord('a') + i) for i in range(len(items))],
                                     [choice['id'].lower() for choice in step['quiz']['choices']])
                else:
                    for choice in step['quiz']['choices']:
                        if native:
                            self.assertIn(r'\item['+choice['id'].upper()+'.] '+latex(choice['text']), text)
                        else:
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

    def test_reading_rejects_missing_or_repeated_steps(self):
        root, data = course.validate(PACKAGE)
        reading = course.read_json(root / data['reading'])
        for changed in (reading['segments'][:-2], reading['segments'] + [reading['segments'][1]]):
            with patch.object(course, 'read_json', return_value={'formatVersion': 1, 'segments': changed}):
                with self.assertRaises(ValueError):
                    course.reading_segments(root, data, {'f01-project', 'f02-console', 'f03-before-add', 'f04-after-add', 'f05-after-one'})

    def test_student_archive_retains_diagram_previews_and_native_sources(self):
        with tempfile.TemporaryDirectory() as folder:
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
