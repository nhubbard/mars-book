"""PPM distribution publication order, privacy and exact export contracts."""

import json
import hashlib
from pathlib import Path
import re
import tempfile
import unittest
import zipfile

import course


class PpmPackageTests(unittest.TestCase):
    def test_native_reading_order_and_guided_boundaries(self):
        root, data = course.validate("029-binary-ppm")
        self.assertEqual(42, len(data["steps"]))
        self.assertEqual(
            38, sum(s["completion"]["type"] == "cli-grade" for s in data["steps"])
        )
        native = course.ROOT / "includes/chapters/029-binary-ppm"
        for path in native.glob("*.tex"):
            expected_export = path.read_text().replace(
                "book/captures/029-binary-ppm/", "packages/029-binary-ppm/captures/"
            )
            self.assertEqual(expected_export, (root / "native" / path.name).read_text())
        expected = re.findall(
            r"\\input\{includes/chapters/029-binary-ppm/([^}]+)\}",
            (native / "chapter.tex").read_text(),
        )
        actual = [
            (
                s["step"]
                if s["kind"] == "step"
                else (
                    s["figure"].removeprefix("ppm-")
                    if s["kind"] == "figure"
                    else Path(s["typesetting"]["nativeLatex"]).stem
                )
            )
            for s in course.reading_segments(root, data)
        ]
        self.assertEqual(expected, actual)
        self.assertEqual(6, len(data["publicChecks"]))
        self.assertEqual(38, len(list((root / "checks/construction").glob("*.kts"))))
        for step in data["steps"]:
            if step["completion"]["type"] == "cli-grade":
                check = (root / step["completion"]["check"]).read_text()
                self.assertNotIn('case("prepared ', check)
        figures = json.loads((root / data["figures"]).read_text())["figures"]
        recipes = {
            q["id"]: q
            for q in json.loads((root / "inputs.json").read_text())["recipes"]
        }
        self.assertEqual(4, len(figures))
        for figure in figures:
            self.assertEqual("verified", figure["captureStatus"])
            receipt_path = root / figure["receipt"]
            receipt = json.loads(receipt_path.read_text())
            self.assertEqual("native-composite", receipt["kind"])
            self.assertEqual(figure["id"], receipt["figure"])
            self.assertTrue(receipt["assertionsPassed"])
            self.assertEqual(
                figure["recipe"]["breakpoint"], receipt["sharedState"]["checkpoint"]
            )
            self.assertEqual(
                receipt["previewSha256"],
                hashlib.sha256((root / figure["artifact"]).read_bytes()).hexdigest(),
            )
            self.assertEqual(
                receipt["nativeExportSha256"],
                hashlib.sha256(
                    (root / figure["nativeAnnotations"]).read_bytes()
                ).hexdigest(),
            )
            for part in receipt["parts"]:
                raw_path = receipt_path.parent / part["receipt"]
                raw = json.loads(raw_path.read_text())
                self.assertEqual(
                    part["receiptSha256"],
                    hashlib.sha256(raw_path.read_bytes()).hexdigest(),
                )
                self.assertTrue(raw["assertionsPassed"])
                self.assertEqual(
                    raw["pngSha256"],
                    hashlib.sha256(
                        (receipt_path.parent / part["image"]).read_bytes()
                    ).hexdigest(),
                )
            self.assertEqual("IntelliJ Light", figure["theme"])
            for recipe in figure["recipe"]["observations"]:
                self.assertEqual(
                    recipes[recipe]["callerCheckpoint"], figure["recipe"]["breakpoint"]
                )

    def test_student_archive_is_exact_and_has_no_private_answers(self):
        root, data = course.validate("029-binary-ppm")
        with tempfile.TemporaryDirectory() as directory:
            first = course.bundle(data["id"], Path(directory) / "a.zip")
            second = course.bundle(data["id"], Path(directory) / "b.zip")
            self.assertEqual(first.read_bytes(), second.read_bytes())
            with zipfile.ZipFile(first) as archive:
                wanted = {
                    data["id"]
                    + "/"
                    + path.relative_to(root).as_posix(): path.read_bytes()
                    for path in course.student_files(root, data)
                }
                self.assertEqual(set(wanted), set(archive.namelist()))
                for name, content in wanted.items():
                    self.assertEqual(content, archive.read(name))
                    self.assertFalse(
                        any(
                            part in {"private", "instructor", "reference", "solutions"}
                            for part in Path(name).parts
                        )
                    )
                    self.assertNotIn("quiz-key", name)
                    if name.endswith((".json", ".md", ".py", ".kts")):
                        self.assertNotIn(b"correctChoice", content)
                source = archive.read(data["id"] + "/starter/main.asm").decode()
                regions = re.findall(
                    r"^# BEGIN (\w+)\n(.*?)^# END \1$", source, re.M | re.S
                )
                self.assertEqual(37, len(regions))
                for _, body in regions:
                    code = [
                        line.strip()
                        for line in body.splitlines()
                        if line.strip()
                        and not line.lstrip().startswith("#")
                        and not line.strip().endswith(":")
                    ]
                    self.assertEqual(2, len(code))
                    self.assertRegex(
                        code[0], r"^b (?:ppm|decode|load|save)_development$"
                    )
                    self.assertEqual("nop", code[1])
