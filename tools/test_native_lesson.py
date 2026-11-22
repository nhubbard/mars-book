import hashlib
import json
from pathlib import Path
import tempfile
import unittest
import zipfile
from native_lesson import bundle


class NativeLessonPackagingTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name) / 'lesson'
        self.root.mkdir()
        self.output = self.root.parent / 'student.zip'
        self.data = dict(formatVersion=1, id='004-byte-inspector', version='0.2.0',
                         entry='main.asm', assignment='assignment.md', nativeLesson='lesson.json',
                         supportingFiles=['experiment.asm'])
        for name, text in {'main.asm': '.text\nnop\n', 'assignment.md': 'Explain.',
                           'lesson.json': '{"formatVersion":2}', 'experiment.asm': '.text\nbreak\n',
                           'not-exported.txt': 'Do not copy unlisted material.'}.items():
            (self.root / name).write_text(text)
        self.manifest()

    def manifest(self):
        (self.root / 'package.json').write_text(json.dumps(self.data))

    def test_explicit_exports_are_deterministic_and_preserve_literal_sources(self):
        bundle(self.root, self.output)
        first = self.output.read_bytes()
        bundle(self.root, self.output)
        self.assertEqual(first, self.output.read_bytes())
        with zipfile.ZipFile(self.output) as archive:
            self.assertEqual([f'004-byte-inspector/{name}' for name in sorted(['package.json', 'main.asm', 'assignment.md', 'lesson.json', 'experiment.asm'])], archive.namelist())
            for name in archive.namelist():
                self.assertEqual((self.root / Path(name).name).read_bytes(), archive.read(name))
                self.assertFalse(archive.getinfo(name).is_dir())
        self.assertTrue(self.output.with_suffix('.zip.sha256').read_text().startswith(hashlib.sha256(first).hexdigest()))

    def test_unsafe_missing_duplicate_and_nonportable_exports_fail_before_building(self):
        for bad in ['../outside', '/absolute', 'a/../main.asm', './main.asm', 'a//main.asm', 'a\\main.asm', '', 'missing.asm', 42]:
            with self.subTest(bad=bad):
                self.data['supportingFiles'] = [bad]; self.manifest()
                with self.assertRaises(ValueError):
                    bundle(self.root, self.output)
                self.assertFalse(self.output.exists())
        self.data['supportingFiles'] = ['main.asm']; self.manifest()
        with self.assertRaises(ValueError):
            bundle(self.root, self.output)
        self.data['supportingFiles'] = 'main.asm'; self.manifest()
        with self.assertRaises(ValueError):
            bundle(self.root, self.output)

    def test_symlinks_and_output_over_source_are_rejected(self):
        (self.root / 'linked').symlink_to(self.root, target_is_directory=True)
        self.data['supportingFiles'] = ['linked/main.asm']; self.manifest()
        with self.assertRaises(ValueError):
            bundle(self.root, self.output)
        self.data['supportingFiles'] = ['experiment.asm']; self.manifest()
        with self.assertRaises(ValueError):
            bundle(self.root, self.root / 'main.asm')
        self.data['id'] = '../bad'; self.manifest()
        with self.assertRaises(ValueError):
            bundle(self.root, self.output)
