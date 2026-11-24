from kotlin_format import format_kotlin

"""WAV student exports preserve native authority, unfinished code and exact privacy boundaries."""

import importlib.util
import hashlib
import json
from pathlib import Path
import re
import tempfile
import unittest
import zipfile

import book
import course


class WavPackageTests(unittest.TestCase):
    def test_native_composition_is_the_reading_authority(self):
        root, manifest = course.validate("031-pcm-wav")
        self.assertEqual("assignment.tex", manifest["assignment"])
        self.assertEqual(64, len(manifest["steps"]))
        self.assertTrue(
            all(s["content"] == "native/" + s["id"] + ".tex" for s in manifest["steps"])
        )
        self.assertTrue(
            all(s["completion"]["type"] == "cli-grade" for s in manifest["steps"])
        )
        native = course.ROOT / "includes/chapters/031-pcm-wav"
        for path in native.glob("*.tex"):
            self.assertEqual(
                path.read_text().replace(
                    "book/captures/031-pcm-wav/", "packages/031-pcm-wav/captures/"
                ),
                (root / "native" / path.name).read_text(),
            )
        expected = re.findall(
            r"\\input\{includes/chapters/031-pcm-wav/([^}]+)\}",
            (native / "chapter.tex").read_text(),
        )
        reading = course.reading_segments(root, manifest)
        actual = [
            (
                r["step"]
                if r["kind"] == "step"
                else (
                    r["figure"].removeprefix("pcm-wav-")
                    if r["kind"] == "figure"
                    else Path(r["content"]).stem
                )
            )
            for r in reading
        ]
        self.assertEqual(expected, actual)
        self.assertEqual(4, sum(r["kind"] == "figure" for r in reading))
        steps = json.loads((root / "steps.json").read_text())["steps"]
        self.assertEqual(manifest["steps"], steps)
        self.assertTrue(
            all(
                "prose" not in step and "expectedObservation" not in step
                for step in steps
            )
        )
        plans = json.loads((root / manifest["figures"]).read_text())["figures"]
        recipes = {
            q["id"]: q
            for q in json.loads((root / "inputs.json").read_text())["recipes"]
        }
        for plan in plans:
            self.assertEqual("verified", plan["captureStatus"])
            self.assertEqual("IntelliJ Light", plan["theme"])
            receipt = json.loads((root / plan["receipt"]).read_text())
            self.assertEqual("native-composite", receipt["kind"])
            self.assertEqual(plan["id"], receipt["figure"])
            self.assertEqual(
                plan["recipe"]["breakpoint"], receipt["sharedState"]["checkpoint"]
            )
            self.assertTrue(receipt["assertionsPassed"])
            self.assertEqual(
                receipt["previewSha256"],
                hashlib.sha256((root / plan["artifact"]).read_bytes()).hexdigest(),
            )
            self.assertEqual(
                receipt["nativeExportSha256"],
                hashlib.sha256(
                    (root / plan["nativeAnnotations"]).read_bytes()
                ).hexdigest(),
            )
            for rid in plan["recipe"]["observations"]:
                self.assertEqual(
                    recipes[rid]["callerCheckpoint"], plan["recipe"]["breakpoint"]
                )

    def test_feedback_matches_public_generators_and_draft(self):
        root, manifest = course.validate("031-pcm-wav")
        draft = course.ROOT / "drafts/pcm-wav-construction"
        spec = importlib.util.spec_from_file_location(
            "wave_feedback_tests", course.ROOT / "tools/wav_feedback.py"
        )
        gen = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(gen)
        model = (course.ROOT / "tools/wav-feedback/model.kts").read_text()
        counts = {
            "bound": 12,
            "parse": 12,
            "filter": 20,
            "encode": 14,
            "format": 31,
            "files": 33,
            "caller": 5,
        }
        self.assertEqual(counts, json.loads((draft / "checks/cases.json").read_text()))
        for domain, cases in gen.cases().items():
            expected = format_kotlin(model + "\n" + "".join(cases))
            self.assertEqual(
                expected, (draft / "checks" / (domain + ".kts")).read_text()
            )
            self.assertEqual(
                expected, (root / "checks" / (domain + ".kts")).read_text()
            )
        self.assertEqual(7, len(manifest["publicChecks"]))
        guided = list((root / "checks/construction").glob("*.kts"))
        self.assertEqual(64, len(guided))
        self.assertEqual(
            127, sum(len(re.findall(r"^case\(", p.read_text(), re.M)) for p in guided)
        )
        for path in guided:
            self.assertEqual(
                path.read_bytes(),
                (draft / "checks/construction" / path.name).read_bytes(),
            )
            self.assertNotIn(
                "runToExit()",
                path.read_text()[len(model) :],
                "Guided checks stop at their checkpoint",
            )
        for name in ["main.asm", "inputs.json"]:
            copy = root / ("starter/main.asm" if name == "main.asm" else name)
            self.assertEqual((draft / name).read_bytes(), copy.read_bytes())
        self.assertEqual(
            (draft / "prepare.py")
            .read_text()
            .replace('default=ROOT / "main.asm"', 'default=ROOT / "starter/main.asm"'),
            (root / "prepare.py").read_text(),
        )
        self.assertEqual(
            (course.ROOT / "drafts/pcm-wav-fixtures/fixture.py").read_bytes(),
            (root / "fixtures/fixture.py").read_bytes(),
        )

    def test_archive_is_deterministic_complete_and_student_only(self):
        root, manifest = course.validate("031-pcm-wav")
        with tempfile.TemporaryDirectory() as directory:
            first = course.bundle("031-pcm-wav", Path(directory) / "first.zip")
            second = course.bundle("031-pcm-wav", Path(directory) / "second.zip")
            self.assertEqual(first.read_bytes(), second.read_bytes())
            wanted = {
                "031-pcm-wav/" + p.relative_to(root).as_posix(): p.read_bytes()
                for p in course.student_files(root, manifest)
            }
            with zipfile.ZipFile(first) as archive:
                self.assertEqual(set(wanted), set(archive.namelist()))
                for name, raw in wanted.items():
                    self.assertEqual(raw, archive.read(name))
                    self.assertFalse(
                        {"private", "instructor", "reference", "solutions"}
                        & set(Path(name).parts)
                    )
                    self.assertNotIn("quiz-key", name)
                    if name.endswith((".json", ".md", ".py", ".kts", ".tex")):
                        self.assertNotIn(b"correctChoice", raw)
                source = archive.read("031-pcm-wav/starter/main.asm").decode()
                bodies = re.findall(
                    r"^# BEGIN (\w+)\n(.*?)^# END \1$", source, re.M | re.S
                )
                self.assertEqual(55, len(bodies))
                for _, body in bodies:
                    self.assertRegex(body, r"^    b \w+\n    nop\n$")
