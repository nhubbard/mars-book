"""Exercise preparation preserves student code; completed feedback refuses false success."""

import contextlib
import importlib.util
import io
import json
from pathlib import Path
import runpy
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[1]
STAGE = ROOT / "drafts/h263-motion-construction"
SPEC = importlib.util.spec_from_file_location(
    "motion_preparation_tests", STAGE / "prepare.py"
)
PREPARE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PREPARE)


class MotionPreparationTests(unittest.TestCase):
    def test_real_windows_and_unchanged_student_instructions(self):
        original = (
            STAGE / "main.asm"
        ).read_text() + "\nstudent_helper:\n    daddiu $t0, $t0, 27\n"
        for mode in (0, 1):
            prepared = PREPARE.prepare(original, mode)
            self.assertEqual(
                original.split("\n.text\n")[1], prepared.split("\n.text\n")[1]
            )
            self.assertIn(".dword 16, 32, 2, " + str(mode), prepared)
            arrays = []
            for label, expected_length in [
                ("motion_current", 2688),
                ("motion_reference", 25376),
            ]:
                body = prepared.split(label + ":\n", 1)[1].split(label + "_end:", 1)[0]
                raw = bytes(
                    int(value) & 255
                    for line in body.splitlines()
                    for value in line.strip().removeprefix(".byte ").split(",")
                )
                self.assertEqual(expected_length, len(raw))
                self.assertEqual(bytes([165] * 16), raw[:16])
                self.assertEqual(bytes([165] * 16), raw[-16:])
                arrays.append(raw)
            a, b = arrays
            candidates = []
            for dy in range(-2, 3):
                for dx in range(-2, 3):
                    score = sum(
                        abs(
                            a[16 + 176 * y + x]
                            - b[16 + (32 + dy + y) * 176 + 16 + dx + x]
                        )
                        for y in range(16)
                        for x in range(16)
                    )
                    candidates.append((score, dy, dx))
            self.assertEqual((2976, -2, -2), min(candidates))
            self.assertEqual(
                7872, next(score for score, dy, dx in candidates if dx == dy == 0)
            )

    def test_bad_controls_and_changed_provided_boundaries(self):
        source = (STAGE / "main.asm").read_text()
        for mode in (-1, 2, True, "0"):
            with self.assertRaises(ValueError):
                PREPARE.prepare(source, mode)
        for broken in [
            source + "\n.text\n",
            source.replace("motion_current_end:", "other:"),
            source.replace("motion_reference:", "motion_current:"),
            source.replace(".dword 16, 32, 2, 1", ".dword 16, 32, 3, 1"),
            source.replace(".dword 0, 0, 176, 176", ".dword 0, 0, 16, 176"),
        ]:
            with self.assertRaises(ValueError):
                PREPARE.prepare(broken, 0)

    def test_cli_never_overwrites_a_working_source(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary) / "worked.asm"
            command = [
                sys.executable,
                str(STAGE / "prepare.py"),
                "--mode",
                "1",
                "--output",
                str(output),
            ]
            first = subprocess.run(command, capture_output=True, text=True)
            self.assertEqual(0, first.returncode, first.stderr)
            before = output.read_bytes()
            self.assertNotEqual(
                0, subprocess.run(command, capture_output=True).returncode
            )
            self.assertEqual(before, output.read_bytes())


class MotionFeedbackRunnerTests(unittest.TestCase):
    def execute(self, source, cli, report, result):
        args = [
            "grade.py",
            str(source),
            "--cli",
            str(cli),
            "--java",
            "test-java",
            "--report",
            str(report),
        ]
        with (
            patch.object(sys, "argv", args),
            patch("subprocess.run", return_value=result) as run,
            contextlib.redirect_stdout(io.StringIO()),
            contextlib.redirect_stderr(io.StringIO()),
            self.assertRaises(SystemExit) as stop,
        ):
            runpy.run_path(str(STAGE / "grade.py"), run_name="__main__")
        return stop.exception.code, run

    def test_complete_output_and_exclusive_report(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / "student.asm"
            source.write_text(".text\n")
            cli = root / "cli.jar"
            cli.touch()
            report = root / "report.json"
            result = subprocess.CompletedProcess([], 0, "PASS case\n" * 56, "")
            code, run = self.execute(source, cli, report, result)
            self.assertEqual(0, code)
            self.assertTrue(json.loads(report.read_text())["passed"])
            self.assertEqual(
                [
                    "test-java",
                    "-jar",
                    str(cli),
                    "grade",
                    str(STAGE / "check.kts"),
                    str(source),
                    str(STAGE / "harness.asm"),
                ],
                run.call_args.args[0],
            )
            before = report.read_bytes()
            code, run = self.execute(source, cli, report, result)
            self.assertEqual(2, code)
            run.assert_not_called()
            self.assertEqual(before, report.read_bytes())

    def test_partial_output_failure_or_failed_process_never_passes(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / "student.asm"
            source.touch()
            cli = root / "cli.jar"
            cli.touch()
            for index, result in enumerate(
                [
                    subprocess.CompletedProcess([], 0, "PASS case\n" * 55, ""),
                    subprocess.CompletedProcess(
                        [], 0, "PASS case\n" * 56 + "FAIL extra\n", ""
                    ),
                    subprocess.CompletedProcess([], 1, "PASS case\n" * 56, "error"),
                ]
            ):
                report = root / (str(index) + ".json")
                code, _ = self.execute(source, cli, report, result)
                self.assertEqual(1, code)
                self.assertFalse(json.loads(report.read_text())["passed"])

    def test_missing_inputs_stop_before_process_creation(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            code, run = self.execute(
                root / "missing.asm", root / "missing.jar", root / "report.json", None
            )
            self.assertEqual(2, code)
            run.assert_not_called()
            self.assertFalse((root / "report.json").exists())


if __name__ == "__main__":
    unittest.main()
