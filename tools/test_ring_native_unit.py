from pathlib import Path
import json
import tempfile
import unittest
import zipfile

from native_lesson import bundle


class RingNativeUnitTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(__file__).resolve().parents[1]
        self.unit = self.root / "revision/lessons/014-ring-records"
        self.lesson = json.loads((self.unit / "lesson.json").read_text())

    def test_separate_functions_retain_every_original_trial_and_public_debug_boundaries(
        self,
    ):
        steps = self.lesson["steps"]
        self.assertEqual(16, len(steps))
        self.assertEqual(16, len({s["id"] for s in steps}))
        self.assertEqual(8, sum(s["kind"] == "prediction" for s in steps))
        debug = [s for s in steps if s["kind"] == "debug"]
        self.assertEqual([3, 3, 4], [len(s["stops"]) for s in debug])
        self.assertEqual(
            [3, 8, 10, 11], [int(s["registers"]["$s3"], 16) for s in debug[-1]["stops"]]
        )
        self.assertTrue(
            all(s["anchor"]["label"] == "operation_ready" for s in debug[-1]["stops"])
        )
        self.assertEqual(
            20,
            sum(
                len(s["stops"]) if s["kind"] == "debug" else 1
                for s in steps
                if s["kind"] != "assessment"
            ),
        )
        checks = [s for s in steps if s["kind"] == "assessment"]
        self.assertEqual(
            ["check-push", "check-pop", "check-caller"], [s["id"] for s in checks]
        )
        rows = []
        for step in checks[:2]:
            profiles = [
                json.loads((self.unit / name).read_text())
                for name in step["caseProfiles"]
            ]
            self.assertEqual([100, 100, 60], [len(p["cases"]) for p in profiles])
            rows.extend(r for p in profiles for r in p["cases"])
        canonical = [
            r
            for p in sorted(
                (self.root / "revision/labs/checks/ring-native").glob(
                    "kernels-*.cases.json"
                )
            )
            for r in json.loads(p.read_text())["cases"]
        ]
        self.assertEqual(
            {r["name"]: r for r in canonical}, {r["name"]: r for r in rows}
        )
        self.assertEqual(
            12,
            len(
                json.loads((self.unit / checks[-1]["caseProfile"]).read_text())["cases"]
            ),
        )
        for name, original in [
            ("worked/cursor.asm", "ring-cursor.asm"),
            ("independent/ring.asm", "ring-records.asm"),
        ]:
            self.assertEqual(
                (self.root / "revision/labs/files" / original).read_bytes(),
                (self.unit / name).read_bytes(),
            )

    def test_archive_exports_only_public_sources_and_explicit_check_profiles(self):
        with tempfile.TemporaryDirectory() as temporary:
            archive = bundle(self.unit, Path(temporary) / "student.zip")
            manifest = json.loads((self.unit / "package.json").read_text())
            expected = [
                "package.json",
                manifest["entry"],
                manifest["assignment"],
                manifest["nativeLesson"],
                *manifest["supportingFiles"],
            ]
            self.assertEqual(17, len(expected))
            with zipfile.ZipFile(archive) as package:
                self.assertEqual(
                    sorted("014-ring-records/" + n for n in expected),
                    package.namelist(),
                )
                for name in expected:
                    self.assertEqual(
                        (self.unit / name).read_bytes(),
                        package.read("014-ring-records/" + name),
                    )
                starter = package.read("014-ring-records/independent/ring.asm").decode()
                self.assertEqual(
                    2, sum(line.strip() == "break" for line in starter.splitlines())
                )
                self.assertFalse(
                    any(
                        "reference" in name or "solution" in name
                        for name in package.namelist()
                    )
                )


if __name__ == "__main__":
    unittest.main()
