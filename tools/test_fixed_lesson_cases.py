from fractions import Fraction
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

import fixed_lesson_cases as fixed
from native_lesson import bundle


class FixedLessonCasesTests(unittest.TestCase):
    def test_exact_rational_rounding_including_negative_half_ties_and_extreme_endpoints(
        self,
    ):
        for a, b in [
            (-2147483648, 2147483647),
            (2147483647, -2147483648),
            (-2, -1),
            (-1, 0),
            (0, 1),
            (127, -129),
        ]:
            previous = None
            for w in range(257):
                exact = Fraction((256 - w) * a + w * b, 256)
                rounded = exact + Fraction(1, 2)
                expected = rounded.numerator // rounded.denominator
                actual = fixed.interpolate(a, b, w)
                self.assertEqual(expected, actual)
                self.assertLessEqual(abs(Fraction(actual) - exact), Fraction(1, 2))
                self.assertTrue(min(a, b) <= actual <= max(a, b))
                if previous is not None:
                    self.assertTrue(
                        actual >= previous if b >= a else actual <= previous
                    )
                previous = actual
        self.assertEqual(0, fixed.interpolate(-1, 0, 128))
        self.assertEqual(-1, fixed.interpolate(-2, -1, 128))

    def test_complete_original_corpus_is_bounded_and_separates_function_result_from_exit(
        self,
    ):
        profile = fixed.profile()
        self.assertEqual(4, profile["formatVersion"])
        self.assertEqual(3368, len(fixed.trials()))
        self.assertEqual(128, len(fixed.samples()))
        self.assertEqual(8, len(profile["cases"]))
        calls = [call for case in profile["cases"][:-1] for call in case["calls"]]
        self.assertEqual(3368, len(calls))

        def signed(value):
            number = int(value, 16)
            return number - (1 << 64) if number >= 1 << 63 else number

        for index, (call, (a, b, w)) in enumerate(zip(calls, fixed.trials())):
            self.assertEqual(
                (a, b, w),
                tuple(signed(call["arguments"][r]) for r in ("a0", "a1", "a2")),
            )
            self.assertEqual(
                fixed.interpolate(a, b, w), signed(call["expectedRegisters"]["v0"])
            )
            self.assertEqual(
                "interpolate_exit" if index % 2 else "interpolate_done",
                call["returnTo"],
            )
        for case in profile["cases"][:-1]:
            self.assertLessEqual(len(case["calls"]), 512)
            self.assertTrue(case["protectCallerStack"])
            self.assertEqual(32, case["callerStackBytes"])
            self.assertEqual(10, len(case["preservedRegisters"]))
            self.assertEqual(
                [dict(label=label, length=4) for label in fixed.LABELS],
                case["noStores"],
            )
        caller = profile["cases"][-1]["calls"][0]
        self.assertEqual("main", caller["entry"])
        self.assertEqual("interpolate_exit", caller["returnTo"])
        self.assertEqual(
            "c0ffffff",
            next(r["bytes"] for r in caller["memory"] if r["label"] == "answer"),
        )

    def test_exact_sources_generated_exports_and_archive_keep_both_assessment_dependencies(
        self,
    ):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/012-fixed-point"
        for name in ("fixed-point-ball.asm", "fixed-interpolate.asm"):
            target = (
                unit / name
                if name.startswith("fixed-point")
                else unit / "independent" / name
            )
            self.assertEqual(
                (root / "revision/labs/files" / name).read_bytes(), target.read_bytes()
            )
        fixed.build(unit / "independent", check=True)
        lesson = json.loads((unit / "lesson.json").read_text())
        self.assertEqual(5, sum(s["kind"] == "prediction" for s in lesson["steps"]))
        self.assertEqual(
            8, len(next(s for s in lesson["steps"] if s["kind"] == "debug")["stops"])
        )
        assessments = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        self.assertEqual(["function", "console"], [s["checkKind"] for s in assessments])
        self.assertEqual(1, len({s["source"] for s in assessments}))
        with tempfile.TemporaryDirectory() as temporary:
            folder = Path(temporary)
            fixed.build(folder)
            self.assertLess(
                (folder / "fixed-interpolate.cases.json").stat().st_size, 1048576
            )
            (folder / "caller-exit.cases.json").write_text("stale")
            with self.assertRaises(ValueError):
                fixed.build(folder, check=True)
            with zipfile.ZipFile(bundle(unit, folder / "student.zip")) as archive:
                for s in assessments:
                    self.assertIn("012-fixed-point/" + s["source"], archive.namelist())
                    self.assertIn(
                        "012-fixed-point/" + s["caseProfile"], archive.namelist()
                    )
                self.assertFalse(
                    any("solution" in n or "reference" in n for n in archive.namelist())
                )
