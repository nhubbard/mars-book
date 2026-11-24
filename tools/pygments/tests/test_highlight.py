import hashlib
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from pygments import lex
from pygments.lexers import get_lexer_by_name
from pygments.styles import get_style_by_name
from mars_book_highlight import MarsIdeaLexer, _load, appearance_token


class HighlightTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.path = Path(self.directory.name) / "export.json"
        self.source = "# a comment 😀\nnop\n"
        self.document = {
            "formatVersion": 1,
            "exports": [
                {
                    "source": self.source,
                    "sha256": hashlib.sha256(self.source.encode()).hexdigest(),
                    "scheme": "Default",
                    "runs": [
                        {
                            "start": 0,
                            "end": len(self.source),
                            "keys": ["MARS_MIPS_COMMENT"],
                            "appearance": {
                                "foreground": "808080",
                                "background": "ffffff",
                                "bold": False,
                                "italic": True,
                            },
                        }
                    ],
                }
            ],
        }
        self.write()
        self.environment = patch.dict(
            os.environ, {"MARS_HIGHLIGHT_MANIFEST": str(self.path)}
        )
        self.environment.start()

    def tearDown(self):
        self.environment.stop()
        self.directory.cleanup()
        _load.cache_clear()

    def write(self):
        self.path.write_text(json.dumps(self.document))
        _load.cache_clear()

    def test_plugin_discovery_and_unicode_round_trip(self):
        lexer = get_lexer_by_name("mars-idea", stripnl=False, ensurenl=False)
        tokens = list(lex(self.source, lexer))
        self.assertEqual(self.source, "".join(value for _, value in tokens))
        style = get_style_by_name("mars-idea-light")
        appearance = style.style_for_token(tokens[0][0])
        from mars_book_highlight.style import contrast_on_white

        self.assertGreaterEqual(contrast_on_white(appearance["color"]), 4.5)
        self.assertTrue(appearance["italic"])
        self.assertFalse(appearance["bold"])

    def test_changed_source_fails_without_guessing_tokens(self):
        with self.assertRaisesRegex(ValueError, "refresh"):
            list(MarsIdeaLexer().get_tokens_unprocessed("different\n"))

    def test_corrupt_hash_and_overlapping_spans_fail(self):
        self.document["exports"][0]["sha256"] = "0" * 64
        self.write()
        with self.assertRaisesRegex(ValueError, "Corrupt"):
            list(MarsIdeaLexer().get_tokens_unprocessed(self.source))
        self.document["exports"][0]["sha256"] = hashlib.sha256(
            self.source.encode()
        ).hexdigest()
        self.document["exports"][0]["runs"][0]["start"] = 1
        self.write()
        with self.assertRaisesRegex(ValueError, "cover"):
            list(MarsIdeaLexer().get_tokens_unprocessed(self.source))

    def test_missing_final_span_fails(self):
        self.document["exports"][0]["runs"][0]["end"] -= 1
        self.write()
        with self.assertRaisesRegex(ValueError, "Incomplete"):
            list(MarsIdeaLexer().get_tokens_unprocessed(self.source))

    def test_empty_source_is_valid(self):
        self.document["exports"][0].update(
            source="", sha256=hashlib.sha256(b"").hexdigest(), runs=[]
        )
        self.write()
        self.assertEqual([], list(MarsIdeaLexer().get_tokens_unprocessed("")))

    def test_rejects_invalid_color_dark_background_and_font_flags(self):
        base = self.document["exports"][0]["runs"][0]["appearance"]
        for key, value in [
            ("foreground", "red\\input"),
            ("background", "000000"),
            ("bold", 1),
            ("italic", "true"),
        ]:
            with self.subTest(key=key):
                bad = dict(base, **{key: value})
                with self.assertRaises(ValueError):
                    appearance_token(bad)

    def test_print_palette_preserves_compliant_colors_and_darkens_pale_ide_colors(self):
        from mars_book_highlight.style import contrast_on_white, print_color

        for color in [
            "000000",
            "000080",
            "0000ff",
            "0033b3",
            "008000",
            "644db2",
            "660e7a",
        ]:
            self.assertEqual(color, print_color(color))
        for color in ["4a86e8", "808080"]:
            darker = print_color(color)
            self.assertNotEqual(color, darker)
            self.assertGreaterEqual(contrast_on_white(darker), 4.5)
