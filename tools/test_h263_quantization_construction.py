"""Quantization representation, boundaries, safe preparation and feedback reports."""

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
STAGE = ROOT / "drafts/h263-quantization-construction"
MODULES = {}
for name in ("model", "prepare"):
    spec = importlib.util.spec_from_file_location(
        "quant_" + name, STAGE / (name + ".py")
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    MODULES[name] = module
MODEL = MODULES["model"]
PREPARE = MODULES["prepare"]


class QuantModelTests(unittest.TestCase):
    def test_worked_input_has_distinct_level_and_reconstruction_records(self):
        values = [1024, 15, 16, -16, 31, 32, -32, 2047, -2048] + [0] * 55
        levels, rec, count = MODEL.quantize(values, 8, True)
        self.assertEqual([255, 0, 1, -1, 1, 2, -2, 127, -127], levels[:9])
        self.assertEqual([1024, 0, 23, -23, 23, 39, -39, 2039, -2039], rec[:9])
        self.assertEqual(7, count)

    def test_all_ordinary_coefficient_parameter_pairs(self):
        for q in range(1, 32):
            for base in range(-2048, 2048, 64):
                source = list(range(base, base + 64))
                levels, rec, count = MODEL.quantize(source, q)
                for c, l, r in zip(source, levels, rec):
                    # A signed truncating quotient independently expressed without magnitude division.
                    expected = int(c / (2 * q))
                    expected = max(-127, min(127, expected))
                    self.assertEqual(expected, l)
                    expected_rec = (
                        0
                        if l == 0
                        else (2 * q * l + (q - (q + 1) % 2) * (1 if l > 0 else -1))
                    )
                    self.assertEqual(max(-2048, min(2047, expected_rec)), r)
                self.assertEqual(sum(l != 0 for l in levels), count)

    def test_every_intra_dc_input_ignores_ordinary_quantizer(self):
        for c in range(-2048, 2048):
            d = min(254, max(1, (c + 4) // 8))
            for q in (1, 8, 31):
                levels, rec, count = MODEL.quantize([c] + [0] * 63, q, True)
                self.assertNotIn(levels[0], (0, 128))
                self.assertEqual(255 if d == 128 else d, levels[0])
                self.assertEqual(8 * d, rec[0])
                self.assertEqual(0, count)

    def test_rejects_bad_domain_and_controls(self):
        for bad in (0, 32, True, "8"):
            with self.assertRaises(ValueError):
                MODEL.quantize([0] * 64, bad)
        for source in (
            [0] * 63,
            [0] * 63 + [2048],
            [0] * 63 + [-2049],
            [False] + [0] * 63,
        ):
            with self.assertRaises(ValueError):
                MODEL.quantize(source)
        with self.assertRaises(ValueError):
            MODEL.quantize([0] * 64, 8, 1)

    def test_preparation_preserves_samples_and_instruction_text(self):
        source = (STAGE / "main.asm").read_text() + "\nmy_helper:\n    nop\n"
        for q in (1, 8, 31):
            for intra in (0, 1):
                for mode in (0, 1):
                    result = PREPARE.prepare(source, q, intra, mode)
                    self.assertEqual(
                        source.split("\n.text\n")[1], result.split("\n.text\n")[1]
                    )
                    self.assertEqual(
                        source.split("quant_input_end:")[0],
                        result.split("quant_input_end:")[0],
                    )
                    self.assertIn(
                        f"quant_request: .dword 0, {q}, {intra}, {mode}", result
                    )
        for q, intra, mode in (
            (0, 1, 1),
            (32, 1, 1),
            (8, 2, 1),
            (8, 1, 2),
            (True, 1, 1),
            (8, True, 1),
        ):
            with self.assertRaises(ValueError):
                PREPARE.prepare(source, q, intra, mode)
        for broken in (
            source + "\n.text\n",
            source.replace("quant_request:", "another:"),
            source.replace(".dword 0, 8, 1, 1", ".dword 0, 0, 1, 1"),
        ):
            with self.assertRaises(ValueError):
                PREPARE.prepare(broken)

    def test_preparation_does_not_replace_existing_file(self):
        with tempfile.TemporaryDirectory() as tmp:
            output = Path(tmp) / "working.asm"
            args = [sys.executable, str(STAGE / "prepare.py"), "--output", str(output)]
            result = subprocess.run(args, capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            before = output.read_bytes()
            self.assertNotEqual(0, subprocess.run(args, capture_output=True).returncode)
            self.assertEqual(before, output.read_bytes())


class QuantFeedbackRunnerTests(unittest.TestCase):
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
            result = subprocess.CompletedProcess([], 0, "PASS case\n" * 204, "")
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
                    subprocess.CompletedProcess([], 0, "PASS case\n" * 203, ""),
                    subprocess.CompletedProcess(
                        [], 0, "PASS case\n" * 204 + "FAIL extra\n", ""
                    ),
                    subprocess.CompletedProcess([], 1, "PASS case\n" * 204, "error"),
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
