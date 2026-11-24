"""Keep representation boundaries, clipping and student source composition observable."""

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
STAGE = ROOT / "drafts/h263-reconstruction-construction"
MODULES = {}
for name in ("reference", "prepare", "compose"):
    spec = importlib.util.spec_from_file_location(
        "block_" + name, STAGE / (name + ".py")
    )
    m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m)
    MODULES[name] = m
REF = MODULES["reference"]
PREP = MODULES["prepare"]
COMPOSE = MODULES["compose"]


class BlockTraceTests(unittest.TestCase):
    def test_two_successive_inter_blocks_use_reconstructed_prediction(self):
        first = REF.reconstruct([130] * 64, [128] * 64)
        self.assertEqual([2] * 64, first["residual"])
        self.assertEqual([16] + [0] * 63, first["forward"])
        self.assertEqual([1] + [0] * 63, first["levels"])
        self.assertEqual([23] + [0] * 63, first["reconstructedCoefficients"])
        self.assertEqual([3] * 64, first["inverse"])
        self.assertEqual([131] * 64, first["pixels"])
        self.assertEqual(1, first["count"])
        second = REF.reconstruct([132] * 64, first["pixels"])
        self.assertEqual([131] * 64, second["pixels"])
        self.assertEqual(0, second["count"])
        wrong = REF.reconstruct([132] * 64, [130] * 64)
        self.assertEqual([133] * 64, wrong["pixels"])
        self.assertEqual(
            [134] * 64, [a + b for a, b in zip(wrong["inverse"], first["pixels"])]
        )

    def test_intra_dc_and_no_prediction_addition(self):
        r = REF.reconstruct([128] * 64, [255] * 64, intra=True)
        self.assertEqual([255] + [0] * 63, r["levels"])
        self.assertEqual([1024] + [0] * 63, r["reconstructedCoefficients"])
        self.assertEqual([128] * 64, r["pixels"])
        self.assertEqual(0, r["count"])

    def test_mixed_sign_block_exercises_both_final_clippers(self):
        source = [0 if (i // 8 + i % 8) % 2 == 0 else 255 for i in range(64)]
        prediction = [128] * 64
        r = REF.reconstruct(source, prediction)
        sums = [v + 128 for v in r["inverse"]]
        self.assertLess(min(sums), 0)
        self.assertGreater(max(sums), 255)
        self.assertEqual([max(0, min(255, v)) for v in sums], r["pixels"])
        for values in ([0] * 63, [256] + [0] * 63, [False] + [0] * 63):
            with self.assertRaises(ValueError):
                REF.reconstruct(values, [0] * 64)
        with self.assertRaises(ValueError):
            REF.reconstruct([0] * 64, [0] * 64, intra=1)

    def test_prepare_keeps_all_instruction_text(self):
        s = (STAGE / "main.asm").read_text()
        for intra in (0, 1):
            for mode in (0, 1):
                out = PREP.prepare(s, intra, mode, 128, 57, 31)
                self.assertEqual(s.split("\n.text\n")[1], out.split("\n.text\n")[1])
                self.assertIn(
                    f"block_request: .dword 0, 8, 0, {0 if intra else 8}, 31, {intra}, {mode}, 0",
                    out,
                )
                self.assertIn("block_current: .byte -128:64", out)
                self.assertIn("block_prediction: .byte 57:64", out)
        for args in [
            (2, 1, 130, 128, 8),
            (0, 2, 130, 128, 8),
            (0, 1, -1, 128, 8),
            (0, 1, 130, 256, 8),
            (0, 1, 130, 128, 0),
            (False, 1, 130, 128, 8),
        ]:
            with self.assertRaises(ValueError):
                PREP.prepare(s, *args)
        with self.assertRaises(ValueError):
            PREP.prepare(s + "\n.text\n")

    def test_composition_uses_student_data_entries_and_following_helpers(self):
        s = (STAGE / "main.asm").read_text()
        t = ".data\nstudent_basis: .half 7\n.text\nmain:\n nop\nh263_transform8:\n li $v0, 17\nstudent_transform_helper:\n nop\n"
        q = ".data\nstudent_quant_table: .half 9\n.text\nmain:\n nop\nh263_quantize8:\n li $v0, 21\nstudent_quant_helper:\n nop\n"
        out = COMPOSE.compose(s, t, q)
        self.assertEqual(
            ("", "h263_transform8:\n nop\n"),
            COMPOSE.component(
                ".text\nmain:\n nop\nh263_transform8:\n nop\n", "h263_transform8"
            ),
        )
        self.assertEqual(1, out.count("\n.text\n"))
        self.assertEqual(1, out.count("\nmain:\n"))
        for part in [
            "student_basis: .half 7",
            "student_quant_table: .half 9",
            "li $v0, 17",
            "li $v0, 21",
            "student_transform_helper:",
            "student_quant_helper:",
        ]:
            self.assertIn(part, out)
        self.assertEqual(
            s.split("\n.text\n")[1].split(COMPOSE.MARKER)[0],
            out.split("\n.text\n")[1].split("\nh263_transform8:")[0],
        )
        self.assertEqual(1, out.count("\nh263_transform8:"))
        self.assertEqual(1, out.count("\nh263_quantize8:"))
        for broken in (t + "\n.text\n", t.replace("h263_transform8:", "other:")):
            with self.assertRaises(ValueError):
                COMPOSE.compose(s, broken, q)
        with self.assertRaises(ValueError):
            COMPOSE.compose(s.replace(COMPOSE.MARKER, ""), t, q)

    def test_compose_cli_preserves_an_existing_working_file(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            transform = root / "transform.asm"
            quant = root / "quant.asm"
            out = root / "joined.asm"
            transform.write_text(
                ".text\nmain:\n nop\nh263_transform8:\n jr $ra\n nop\n"
            )
            quant.write_text(".text\nmain:\n nop\nh263_quantize8:\n jr $ra\n nop\n")
            args = [
                sys.executable,
                str(STAGE / "compose.py"),
                "--transform",
                str(transform),
                "--quantizer",
                str(quant),
                "--output",
                str(out),
            ]
            r = subprocess.run(args, capture_output=True, text=True)
            self.assertEqual(0, r.returncode, r.stderr)
            before = out.read_bytes()
            self.assertNotEqual(0, subprocess.run(args, capture_output=True).returncode)
            self.assertEqual(before, out.read_bytes())

    def test_prepare_refuses_existing_output(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "observation.asm"
            args = [sys.executable, str(STAGE / "prepare.py"), "--output", str(out)]
            r = subprocess.run(args, capture_output=True, text=True)
            self.assertEqual(0, r.returncode, r.stderr)
            before = out.read_bytes()
            self.assertNotEqual(0, subprocess.run(args, capture_output=True).returncode)
            self.assertEqual(before, out.read_bytes())


class BlockFeedbackRunnerTests(unittest.TestCase):
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
            result = subprocess.CompletedProcess([], 0, "PASS case\n" * 144, "")
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
                    subprocess.CompletedProcess([], 0, "PASS case\n" * 143, ""),
                    subprocess.CompletedProcess(
                        [], 0, "PASS case\n" * 144 + "FAIL extra\n", ""
                    ),
                    subprocess.CompletedProcess([], 1, "PASS case\n" * 144, "error"),
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
