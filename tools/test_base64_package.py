"""Base64 distribution structure and privacy contracts."""
import json
from pathlib import Path
import re
import tempfile
import unittest
import zipfile

import book
import course


class Base64PackageTests(unittest.TestCase):
    def test_native_sequence_and_guided_checks_are_exported(self):
        root, data = course.validate('028-base64')
        self.assertEqual(43, len(data['steps']))
        self.assertEqual(38, sum(s['completion']['type'] == 'cli-grade' for s in data['steps']))
        native = course.ROOT / 'includes/chapters/028-base64'
        for path in native.glob('*.tex'):
            self.assertEqual(path.read_bytes(), (root / 'native' / path.name).read_bytes())
        names = re.findall(r'\\input\{includes/chapters/028-base64/([^}]+)\}', (native / 'chapter.tex').read_text())
        reading = course.reading_segments(root, data)
        actual = [s['step'] if s['kind'] == 'step' else s['figure'].removeprefix('base64-')
                  if s['kind'] == 'figure' else Path(s['typesetting']['nativeLatex']).stem for s in reading]
        self.assertEqual(names, actual)
        self.assertEqual(7, len(data['publicChecks']))
        self.assertEqual(37, len(list((root / 'checks/construction').glob('*.kts'))))
        for step in data['steps']:
            if step['completion']['type'] == 'cli-grade':
                check = (root / step['completion']['check']).read_text()
                self.assertNotIn('case("prepared ', check)
            if step['kind'] == 'author':
                text = (root / step['content']).read_text()
                self.assertEqual(re.findall(r'```asm\n(.*?)```', text, re.S),
                                 [m[1] for m in book.CODE.finditer((root / 'native' / (step['id'] + '.tex')).read_text())])
        self.assertIn('expectVectorLane', data['environment']['graderRequirements'])

    def test_archive_is_exact_and_does_not_contain_instructor_content(self):
        root, data = course.validate('028-base64')
        with tempfile.TemporaryDirectory() as directory:
            first = course.bundle(data['id'], Path(directory) / 'first.zip')
            second = course.bundle(data['id'], Path(directory) / 'second.zip')
            self.assertEqual(first.read_bytes(), second.read_bytes())
            with zipfile.ZipFile(first) as archive:
                expected = {data['id'] + '/' + p.relative_to(root).as_posix(): p.read_bytes()
                            for p in course.student_files(root, data)}
                self.assertEqual(set(expected), set(archive.namelist()))
                for name, content in expected.items():
                    self.assertEqual(content, archive.read(name))
                    self.assertFalse(any(part in {'private', 'instructor', 'reference', 'solutions'}
                                         for part in Path(name).parts))
                    self.assertNotIn('quiz-key', name)
                    if name.endswith(('.json', '.md', '.py', '.kts')):
                        self.assertNotIn(b'correctChoice', content)
        figures = json.loads((root / data['figures']).read_text())['figures']
        self.assertEqual(4, len(figures))
        recipes = {q['id']: q for q in json.loads((root / 'inputs.json').read_text())['recipes']}
        for figure in figures:
            self.assertEqual('planned', figure['captureStatus'])
            self.assertEqual('IntelliJ Light', figure['theme'])
            for name in figure['recipe']['observations']:
                self.assertEqual(recipes[name]['callerCheckpoint'], figure['recipe']['breakpoint'])


if __name__ == '__main__':
    unittest.main()
