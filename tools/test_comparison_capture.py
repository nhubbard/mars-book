"""Comparison plates keep each source stop explicit while sharing runtime provenance."""

import copy
import hashlib
import json
from pathlib import Path
import unittest
import course
import test_composite_capture


class ComparisonCaptureTests(unittest.TestCase):
    def setUp(self):
        test_composite_capture.CompositeCaptureTests.setUp(self)
        self.receipt["formatVersion"] = 3
        self.receipt["kind"] = "native-comparison"
        self.receipt["sharedState"] = {"checkpoint": self.figure["checkpoint"]}
        self.receipt["sessions"] = {}
        self.keys = (
            "sourceSha256",
            "source",
            "checkpoint",
            "sourceLine",
            "breakpointCondition",
            "backstepFrom",
            "observedBackstep",
        )
        for i, part in enumerate(self.receipt["parts"]):
            name = "first" if i == 0 else "second"
            part["session"] = name
            path = self.root / Path(self.figure["receipt"]).parent / part["receipt"]
            raw = json.loads(path.read_text())
            if i:
                raw.update(
                    source="another-fixture.asm", sourceSha256="a" * 64, sourceLine=77
                )
            path.write_text(json.dumps(raw))
            part["receiptSha256"] = hashlib.sha256(path.read_bytes()).hexdigest()
            self.receipt["sessions"][name] = {key: raw.get(key) for key in self.keys}

    def validate(self, receipt=None):
        course.validate_composite_capture(
            self.root, self.figure, self.receipt if receipt is None else receipt
        )

    def test_two_declared_source_states_pass(self):
        self.validate()

    def test_missing_unknown_or_unused_sessions_fail(self):
        for kind in [
            "missing",
            "unknown",
            "unused",
            "partial-state",
            "invalid-name",
            "one-session",
        ]:
            with self.subTest(kind=kind):
                r = copy.deepcopy(self.receipt)
                if kind == "missing":
                    del r["parts"][0]["session"]
                elif kind == "unknown":
                    r["parts"][0]["session"] = "absent"
                elif kind == "unused":
                    r["sessions"]["extra"] = r["sessions"]["first"]
                elif kind == "partial-state":
                    del r["sessions"]["first"]["sourceLine"]
                elif kind == "invalid-name":
                    r["sessions"]["bad/name"] = r["sessions"].pop("first")
                else:
                    del r["sessions"]["second"]
                with self.assertRaises(ValueError):
                    self.validate(r)

    def test_session_state_remains_bound_to_raw_receipt(self):
        for key, value in [
            ("sourceSha256", "b" * 64),
            ("source", "forged.asm"),
            ("sourceLine", 78),
            ("breakpointCondition", "$t0 == 7"),
        ]:
            with self.subTest(key=key):
                r = copy.deepcopy(self.receipt)
                r["sessions"]["second"][key] = value
                with self.assertRaisesRegex(ValueError, "session state"):
                    self.validate(r)

    def test_common_runtime_and_settled_views_remain_required(self):
        part = self.receipt["parts"][1]
        path = self.root / Path(self.figure["receipt"]).parent / part["receipt"]
        original = path.read_bytes()
        for key, value in [
            ("captureDriverSha256", "b" * 64),
            ("ideBuild", "different"),
            ("locale", "different"),
            ("notificationBalloonsVisible", 1),
        ]:
            with self.subTest(key=key):
                raw = json.loads(original)
                raw[key] = value
                path.write_text(json.dumps(raw))
                r = copy.deepcopy(self.receipt)
                r["parts"][1]["receiptSha256"] = hashlib.sha256(
                    path.read_bytes()
                ).hexdigest()
                with self.assertRaises(ValueError):
                    self.validate(r)
        path.write_bytes(original)

    def test_composite_cannot_ignore_comparison_metadata(self):
        r = copy.deepcopy(self.receipt)
        r.update(formatVersion=2, kind="native-composite")
        with self.assertRaisesRegex(ValueError, "explicit comparison"):
            self.validate(r)

    def test_invalid_group_identity_fails(self):
        for key, value in [("sourceSha256", "short"), ("source", "")]:
            r = copy.deepcopy(self.receipt)
            r["sessions"]["first"][key] = value
            with self.assertRaisesRegex(ValueError, "source identity"):
                self.validate(r)


if __name__ == "__main__":
    unittest.main()
