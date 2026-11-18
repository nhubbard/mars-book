from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import book


class NativeCompositionTests(unittest.TestCase):
    def test_actual_native_composition_and_highlight_integrity(self):
        self.assertEqual(151, len(book.validate()))
        self.assertEqual(1, len(book.validate('specimen.tex')))
        self.assertEqual(13, len(book.validate('drafts/matrix-transpose.tex')))

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
