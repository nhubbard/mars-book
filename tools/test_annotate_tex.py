"""Pixel coordinates, receipt integrity and prose remain intact in native figures."""

import copy
import hashlib
import struct
import tempfile
import unittest
import zlib
from pathlib import Path

import annotate_tex


class NativeAnnotationTests(unittest.TestCase):
    def setUp(self):
        def chunk(kind, data):
            return (
                struct.pack(">I", len(data))
                + kind
                + data
                + struct.pack(">I", zlib.crc32(kind + data))
            )

        self.png = (
            b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", 80, 40, 8, 2, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress((b"\0" + b"\xff" * 240) * 40))
            + chunk(b"IEND", b"")
        )
        self.receipt = dict(
            figure="test-only-synthetic",
            theme="IntelliJ Light",
            assertionsPassed=True,
            pngSha256=hashlib.sha256(self.png).hexdigest(),
            viewport=dict(width=80, height=40),
            callouts=[
                dict(label="A & B = $v1_2", box=dict(x=2, y=2, width=40, height=20))
            ],
        )

    def test_native_rectangles_reverse_vertical_pixels_and_keep_text_extractable(self):
        with tempfile.TemporaryDirectory() as folder:
            png = Path(folder) / "raw.png"
            png.write_bytes(self.png)
            out = Path(folder) / "figure.tex"
            annotate_tex.render(
                png, self.receipt, out, "Count & source", "Register $v1 is two."
            )
            text = out.read_text()
            self.assertIn(
                "(0.025000000,0.450000000) rectangle (0.525000000,0.950000000)", text
            )
            self.assertIn(r"\item A \& B = \$v1\_2", text)
            self.assertIn(r"\caption{Count \& source}", text)
            self.assertIn(self.receipt["pngSha256"], text)
            self.assertIn(r"\label{fig:test-only-synthetic}", text)
            self.assertNotIn(".svg", text)
            self.assertEqual(self.png, png.read_bytes())
            with self.assertRaises(FileExistsError):
                annotate_tex.render(png, self.receipt, out, "Count", "Count is two.")
            self.assertEqual(text, out.read_text())

    def test_crop_keeps_all_targets_and_translates_their_actual_coordinates(self):
        with tempfile.TemporaryDirectory() as folder:
            png = Path(folder) / "raw.png"
            png.write_bytes(self.png)
            out = Path(folder) / "figure.tex"
            annotate_tex.render(
                png, self.receipt, out, "Count", "Count is two.", crop=(2, 2, 40, 20)
            )
            self.assertIn(
                "(0.000000000,0.000000000) rectangle (1.000000000,1.000000000)",
                out.read_text(),
            )
            for crop in [(3, 2, 40, 20), (2, 2, 39, 20), (0, 0, 81, 40), (0, 0, 80, 0)]:
                with self.assertRaises(ValueError):
                    annotate_tex.render(
                        png,
                        self.receipt,
                        Path(folder) / "bad.tex",
                        "Count",
                        "Count.",
                        crop=crop,
                    )

    def test_integrity_and_reference_checks_precede_writing(self):
        with tempfile.TemporaryDirectory() as folder:
            png = Path(folder) / "raw.png"
            png.write_bytes(self.png)
            out = Path(folder) / "bad.tex"
            bad = copy.deepcopy(self.receipt)
            bad["pngSha256"] = "wrong"
            with self.assertRaises(ValueError):
                annotate_tex.render(png, bad, out, "Count", "Count.")
            for reference in [
                "different.png",
                r"bad}\input{other}",
                "bad%image.png",
                "bad\\image.png",
            ]:
                with self.assertRaises(ValueError):
                    annotate_tex.render(
                        png,
                        self.receipt,
                        out,
                        "Count",
                        "Count.",
                        image_reference=reference,
                    )
            bad = copy.deepcopy(self.receipt)
            bad["callouts"][0]["box"]["x"] = True
            with self.assertRaises(ValueError):
                annotate_tex.render(png, bad, out, "Count", "Count.")
            bad = copy.deepcopy(self.receipt)
            bad["figure"] = "invalid}label"
            with self.assertRaises(ValueError):
                annotate_tex.render(png, bad, out, "Count", "Count.")
            self.assertFalse(out.exists())
