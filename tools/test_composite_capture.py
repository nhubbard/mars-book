"""A student figure cannot silently substitute images, state, or native annotations."""

import copy
import hashlib
import json
from pathlib import Path
import shutil
import tempfile
import unittest

import course


class CompositeCaptureTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        package = course.ROOT / "packages/034-lossless-video"
        shutil.copytree(package / "captures", self.root / "captures")
        (self.root / "native").mkdir()
        shutil.copyfile(
            package / "native/fig-vector-count.tex",
            self.root / "native/fig-vector-count.tex",
        )
        self.figure = next(
            f
            for f in course.read_json(package / "captures/plan.json")["figures"]
            if f["id"] == "video-vector-count"
        )
        self.receipt = course.read_json(self.root / self.figure["receipt"])

    def validate(self, receipt=None):
        course.validate_composite_capture(
            self.root, self.figure, self.receipt if receipt is None else receipt
        )

    def test_two_actual_views_and_three_asserted_panels_pass(self):
        self.validate()
        self.assertEqual(2, len(self.receipt["parts"]))
        self.assertEqual(3, len(self.receipt["panels"]))

    def test_two_details_from_one_actual_capture_pass(self):
        receipt = copy.deepcopy(self.receipt)
        receipt["parts"] = [
            part for part in receipt["parts"] if part["id"] == "counter"
        ]
        receipt["panels"] = [
            panel for panel in receipt["panels"] if panel["part"] == "counter"
        ]
        self.assertEqual(2, len(receipt["panels"]))
        self.validate(receipt)

    def test_empty_capture_set_or_single_detail_is_not_a_composite(self):
        for change in ("empty", "single-panel"):
            with self.subTest(change=change):
                receipt = copy.deepcopy(self.receipt)
                receipt["parts"] = [
                    part for part in receipt["parts"] if part["id"] == "counter"
                ]
                receipt["panels"] = [
                    panel for panel in receipt["panels"] if panel["part"] == "counter"
                ]
                if change == "empty":
                    receipt["parts"] = []
                else:
                    receipt["panels"] = receipt["panels"][:1]
                with self.assertRaises(ValueError):
                    self.validate(receipt)

    def test_optional_raw_receipt_digest_binds_additional_observation_metadata(self):
        receipt = copy.deepcopy(self.receipt)
        part = receipt["parts"][0]
        path = self.root / Path(self.figure["receipt"]).parent / part["receipt"]
        part["receiptSha256"] = hashlib.sha256(path.read_bytes()).hexdigest()
        self.validate(receipt)
        raw = json.loads(path.read_text())
        raw["stdout"] = "forged observation"
        path.write_text(json.dumps(raw))
        with self.assertRaisesRegex(ValueError, "Raw capture receipt changed"):
            self.validate(receipt)

    def test_unknown_or_downgraded_receipt_format_fails(self):
        for key, value in [("formatVersion", 1), ("kind", "unrecognized")]:
            with self.subTest(field=key):
                receipt = copy.deepcopy(self.receipt)
                receipt[key] = value
                with self.assertRaises(ValueError):
                    self.validate(receipt)

    def test_changed_raw_preview_or_native_bytes_fail(self):
        for relative in (
            "captures/video-vector-count/counter.png",
            self.figure["artifact"],
            self.figure["nativeAnnotations"],
        ):
            with self.subTest(relative=relative):
                path = self.root / relative
                original = path.read_bytes()
                try:
                    path.write_bytes(original + b"changed")
                    with self.assertRaises(ValueError):
                        self.validate()
                finally:
                    path.write_bytes(original)

    def test_different_source_runtime_or_readiness_fails(self):
        path = self.root / "captures/video-vector-count/lanes.json"
        original = path.read_text()
        for key, value in [
            ("sourceSha256", "0" * 64),
            ("sourceLine", 151),
            ("captureDriverSha256", "1" * 64),
            ("notificationBalloonsVisible", 1),
            ("visibleAnalysisIndicators", ["Analyzing"]),
        ]:
            with self.subTest(field=key):
                raw = json.loads(original)
                raw[key] = value
                path.write_text(json.dumps(raw))
                with self.assertRaises(ValueError):
                    self.validate()
                path.write_text(original)

    def test_crop_cannot_clip_its_asserted_row_or_leave_the_image(self):
        for change in ("clip", "outside", "noninteger", "missing-callout"):
            with self.subTest(change=change):
                receipt = copy.deepcopy(self.receipt)
                panel = receipt["panels"][1]
                if change == "clip":
                    panel["crop"][2] = 10
                if change == "outside":
                    panel["crop"][0] = 1280
                if change == "noninteger":
                    panel["crop"][1] = 0.5
                if change == "missing-callout":
                    panel["callout"] = -1
                with self.assertRaises(ValueError):
                    self.validate(receipt)

    def test_additional_assertions_cannot_be_clipped_or_duplicated(self):
        for indices in ([0], [1]):
            with self.subTest(indices=indices):
                receipt = copy.deepcopy(self.receipt)
                receipt["panels"][1]["additionalCallouts"] = indices
                with self.assertRaises(ValueError):
                    self.validate(receipt)

    def test_raw_parts_cannot_escape_duplicate_or_disappear(self):
        for change in ("escape", "duplicate", "missing-part", "unused-part"):
            with self.subTest(change=change):
                receipt = copy.deepcopy(self.receipt)
                if change == "escape":
                    receipt["parts"][0]["image"] = "../../../../outside.png"
                if change == "duplicate":
                    receipt["parts"][1]["id"] = receipt["parts"][0]["id"]
                if change == "missing-part":
                    receipt["panels"][0]["part"] = "absent"
                if change == "unused-part":
                    receipt["panels"] = [
                        p for p in receipt["panels"] if p["part"] == "counter"
                    ]
                with self.assertRaises(ValueError):
                    self.validate(receipt)


class CaptureModeTests(unittest.TestCase):
    def test_editor_requires_no_process_and_Run_requires_exact_native_evidence(self):
        course.validate_capture_mode(
            {"captureMode": "editor"}, dict(mode="editor", stdout="", stderr="")
        )
        figure = dict(
            captureMode="run", expectedStdout="Answer: 41\n", standardInput="20\n"
        )
        raw = dict(
            mode="run",
            stdout="Answer: 41\n",
            stderr="",
            exitCode=0,
            standardInput="20\n",
            inputMethod="Native console typing and Enter handlers",
        )
        course.validate_capture_mode(figure, raw)
        for key, value in [
            ("stdout", "Answer: 42\n"),
            ("stderr", "error"),
            ("exitCode", 1),
            ("standardInput", "21\n"),
            ("inputMethod", "painted echo"),
            ("mode", "debug"),
        ]:
            with self.subTest(key=key):
                with self.assertRaises(ValueError):
                    course.validate_capture_mode(figure, dict(raw, **{key: value}))
        for key, value in [
            ("exitCode", 0),
            ("stdout", "text"),
            ("stderr", "error"),
            ("standardInput", "20\n"),
        ]:
            with self.subTest(editor=key):
                editor_raw = dict(mode="editor", stdout="", stderr="")
                editor_raw[key] = value
                with self.assertRaises(ValueError):
                    course.validate_capture_mode({"captureMode": "editor"}, editor_raw)
        with self.assertRaises(ValueError):
            course.validate_capture_mode({"captureMode": "unknown"}, raw)
        with self.assertRaises(ValueError):
            course.validate_capture_mode({"captureMode": "run"}, raw)


if __name__ == "__main__":
    unittest.main()
