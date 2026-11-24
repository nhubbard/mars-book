"""SAD controls select real fixture bytes while retaining every student instruction."""

import contextlib
import io
import json
import runpy
from unittest.mock import patch
import importlib.util
import re
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
D = ROOT / "drafts/h263-sad-construction"
SPEC = importlib.util.spec_from_file_location("sad_controls_tests", D / "prepare.py")
PREP = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PREP)


class SadConstructionTests(unittest.TestCase):
    def test_worked_windows_and_instruction_preservation(self):
        source = (
            D / "main.asm"
        ).read_text() + "\nstudent_helper:\n    daddiu $t0, $t0, 27\n"
        for mode in (0, 1):
            result = PREP.prepare(source, mode)
            self.assertEqual(source.split("\n.text\n")[1], result.split("\n.text\n")[1])
            self.assertIn("sad_mode: .word " + str(mode), result)
            arrays = []
            for name in ("sad_left", "sad_right"):
                body = result.split(name + ":\n", 1)[1].split(name + "_end:", 1)[0]
                raw = bytes(
                    int(v) & 255
                    for line in body.splitlines()
                    for v in line.strip().removeprefix(".byte ").split(",")
                )
                self.assertEqual(2688, len(raw))
                self.assertEqual(bytes([165] * 16), raw[:16])
                self.assertEqual(bytes([165] * 16), raw[-16:])
                arrays.append(raw)
            score = sum(
                abs(arrays[0][16 + 176 * y + x] - arrays[1][16 + 176 * y + x])
                for y in range(16)
                for x in range(16)
            )
            self.assertEqual(7872, score)

    def test_bad_controls_and_changed_arenas_are_rejected(self):
        source = (D / "main.asm").read_text()
        for mode in (-1, 2, True, "0"):
            with self.assertRaises(ValueError):
                PREP.prepare(source, mode)
        for broken in (
            source.replace("sad_left_end:", "other:"),
            source.replace("sad_right:", "sad_left:"),
            source.replace("sad_mode: .word", "sad_mode: .dword"),
            source.replace("sad_stride: .dword 176", "sad_stride: .dword 16"),
            source + "\n.text\n",
        ):
            with self.assertRaises(ValueError):
                PREP.prepare(broken, 0)

    def test_cli_refuses_existing_output(self):
        with tempfile.TemporaryDirectory() as temporary:
            target = Path(temporary) / "worked.asm"
            args = [
                sys.executable,
                str(D / "prepare.py"),
                "--mode",
                "1",
                "--output",
                str(target),
            ]
            result = subprocess.run(args, capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            before = target.read_bytes()
            self.assertNotEqual(0, subprocess.run(args, capture_output=True).returncode)
            self.assertEqual(before, target.read_bytes())


class SadFeedbackRunnerTests(unittest.TestCase):
    def execute(self, source, cli, report, result):
        args = [
            "grade.py",
            str(source),
            "--cli",
            str(cli),
            "--java",
            "java-proof",
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
            runpy.run_path(str(D / "grade.py"), run_name="__main__")
        return stop.exception.code, run

    def test_success_uses_the_separate_harness_and_protects_reports(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            source = root / "student.asm"
            source.write_text(".text\n")
            cli = root / "cli.jar"
            cli.touch()
            report = root / "result.json"
            result = subprocess.CompletedProcess([], 0, "PASS one\n" * 32, "")
            code, run = self.execute(source, cli, report, result)
            self.assertEqual(0, code)
            self.assertTrue(json.loads(report.read_text())["passed"])
            self.assertEqual(
                [
                    "java-proof",
                    "-jar",
                    str(cli),
                    "grade",
                    str(D / "check.kts"),
                    str(source),
                    str(D / "harness.asm"),
                ],
                run.call_args.args[0],
            )
            before = report.read_bytes()
            code, run = self.execute(source, cli, report, result)
            self.assertEqual(2, code)
            run.assert_not_called()
            self.assertEqual(before, report.read_bytes())

    def test_truncated_or_failed_case_output_is_not_success(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            source = root / "student.asm"
            source.write_text(".text\n")
            cli = root / "cli.jar"
            cli.touch()
            for index, result in enumerate(
                [
                    subprocess.CompletedProcess([], 0, "PASS one\n" * 31, ""),
                    subprocess.CompletedProcess(
                        [], 0, "PASS one\n" * 32 + "FAIL last\n", ""
                    ),
                    subprocess.CompletedProcess([], 1, "PASS one\n" * 32, "failure"),
                ]
            ):
                report = root / (str(index) + ".json")
                code, _ = self.execute(source, cli, report, result)
                self.assertEqual(1, code)
                self.assertFalse(json.loads(report.read_text())["passed"])

    def test_missing_source_or_cli_fails_before_execution(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            code, run = self.execute(
                root / "missing.asm", root / "missing.jar", root / "result.json", None
            )
            self.assertEqual(2, code)
            run.assert_not_called()
            self.assertFalse((root / "result.json").exists())


if __name__ == "__main__":
    unittest.main()
