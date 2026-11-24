"""Complete baseline code data and independent round-trip block meaning."""

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
STAGE = ROOT / "drafts/h263-coefficient-construction"
MODULES = {}
for name in ("model", "prepare"):
    spec = importlib.util.spec_from_file_location(
        "coeff_" + name, STAGE / (name + ".py")
    )
    m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m)
    MODULES[name] = m
MODEL = MODULES["model"]
PREP = MODULES["prepare"]


class CoeffModelTests(unittest.TestCase):
    def test_complete_normative_numeric_table_and_scan(self):
        rows = MODEL.TABLE["events"]
        self.assertEqual(list(range(102)), [r["index"] for r in rows])
        self.assertEqual(list(range(64)), sorted(MODEL.SCAN))
        figure = [
            [1, 2, 6, 7, 15, 16, 28, 29],
            [3, 5, 8, 14, 17, 27, 30, 43],
            [4, 9, 13, 18, 26, 31, 42, 44],
            [10, 12, 19, 25, 32, 41, 45, 54],
            [11, 20, 24, 33, 40, 46, 53, 55],
            [21, 23, 34, 39, 47, 52, 56, 61],
            [22, 35, 38, 48, 51, 57, 60, 62],
            [36, 37, 49, 50, 58, 59, 63, 64],
        ]
        self.assertEqual(
            figure,
            [[MODEL.SCAN.index(y * 8 + x) + 1 for x in range(8)] for y in range(8)],
        )
        prefixes = ["0000011"] + [
            format(r["code"] >> 1, f"0{r['width']-1}b") for r in rows
        ]
        self.assertEqual(103, len(set(prefixes)))
        for i, a in enumerate(prefixes):
            for j, b in enumerate(prefixes):
                if i != j:
                    self.assertFalse(a.startswith(b))
        expected = [
            r[k] for r in rows for k in ("last", "run", "magnitude", "code", "width")
        ]
        source = (STAGE / "tables.asm").read_text().split("coeff_codes:\n")[1]
        self.assertEqual(
            expected,
            [
                int(v)
                for line in source.splitlines()
                for v in line.strip().removeprefix(".dword ").split(",")
            ],
        )
        self.assertEqual(
            (STAGE / "tables.asm").read_text().strip(),
            (STAGE / "main.asm")
            .read_text()
            .split(
                "# Complete h263_emit_coefficients and carry in your completed h263_write_bits.\n",
                1,
            )[1]
            .split("\n.align 1\n", 1)[0]
            .strip(),
        )

    def test_every_compact_event_both_signs_and_block_types(self):
        for row in MODEL.TABLE["events"]:
            for intra in (False, True):
                for sign in (-1, 1):
                    values = [0] * 64
                    if intra:
                        values[0] = 255
                    at = row["run"] + int(intra)
                    values[MODEL.SCAN[at]] = sign * row["magnitude"]
                    if row["last"] == 0:
                        values[MODEL.SCAN[at + 1]] = 1
                    for compact in (False, True):
                        encoded = MODEL.encode(values, intra, compact)
                        self.assertEqual(values, MODEL.decode(encoded, intra))
                    bits = MODEL.encode(values, intra)[8 if intra else 0 :]
                    self.assertTrue(
                        bits.startswith(
                            format(row["code"] | int(sign < 0), f"0{row['width']}b")
                        )
                    )

    def test_every_legal_single_escape_run_and_signed_level(self):
        for run in range(64):
            for level in range(-127, 128):
                if level == 0:
                    continue
                values = [0] * 64
                values[MODEL.SCAN[run]] = level
                encoded = MODEL.encode(values, False, False)
                self.assertEqual(22, len(encoded))
                self.assertEqual(values, MODEL.decode(encoded))
        self.assertEqual([0] * 64, MODEL.decode(""))
        self.assertEqual([255] + [0] * 63, MODEL.decode("11111111", True))

    def test_worked_streams_have_different_sizes_but_identical_meaning(self):
        values = PREP.levels(1, "worked")
        short = MODEL.encode(values, True)
        long = MODEL.encode(values, True, False)
        self.assertEqual("1111111110001111", short)
        self.assertEqual(16, len(short))
        self.assertEqual(52, len(long))
        self.assertEqual([(0, 0, 1), (1, 0, -1)], MODEL.events(values, True))
        self.assertEqual(values, MODEL.decode(short, True))
        self.assertEqual(values, MODEL.decode(long, True))

    def test_rejects_forbidden_levels_runs_and_unterminated_syntax(self):
        for values, intra in [
            ([0] * 63 + [128], False),
            ([0] * 63 + [-128], False),
            ([128] + [0] * 63, True),
            ([0] * 64, True),
            ([False] + [0] * 63, False),
            ([0] * 63, False),
        ]:
            with self.assertRaises(ValueError):
                MODEL.encode(values, intra)
        for bits, intra in [
            ("0000011100000000000000", False),
            ("0000011100000010000000", False),
            ("100", False),
            ("011100", False),
            ("0000000", False),
            ("00000000", True),
            ("10000000", True),
            ("11111111" + "00000111" + "111111" + "00000001", True),
        ]:
            with self.assertRaises(ValueError):
                MODEL.decode(bits, intra)

    def test_preparation_preserves_every_instruction_and_code_table(self):
        source = (STAGE / "main.asm").read_text() + "\nstudent_helper:\n    nop\n"
        for intra in (0, 1):
            for pattern in ("worked", "zero", "tail", "dense"):
                out = PREP.prepare(source, intra, pattern)
                self.assertEqual(
                    source.split("\n.text\n")[1], out.split("\n.text\n")[1]
                )
                self.assertEqual(
                    source.split("coeff_input:", 1)[0], out.split("coeff_input:", 1)[0]
                )
                values = [
                    int(v)
                    for line in out.split("coeff_input:\n", 1)[1]
                    .split("coeff_input_end:", 1)[0]
                    .splitlines()
                    for v in line.strip().removeprefix(".half ").split(",")
                ]
                self.assertEqual(PREP.levels(intra, pattern), values)
        for args in [
            (2, "worked", 192, 3),
            (1, "other", 192, 3),
            (1, "worked", 193, 3),
            (1, "worked", 1, 9),
            (True, "worked", 192, 3),
        ]:
            with self.assertRaises(ValueError):
                PREP.prepare(source, *args)
        with self.assertRaises(ValueError):
            PREP.prepare(source.replace("coeff_input_end:", "other:"))

    def test_preparation_protects_existing_work(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "worked.asm"
            args = [sys.executable, str(STAGE / "prepare.py"), "--output", str(out)]
            r = subprocess.run(args, capture_output=True, text=True)
            self.assertEqual(0, r.returncode, r.stderr)
            before = out.read_bytes()
            self.assertNotEqual(0, subprocess.run(args, capture_output=True).returncode)
            self.assertEqual(before, out.read_bytes())


class CoeffFeedbackRunnerTests(unittest.TestCase):
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
            result = subprocess.CompletedProcess([], 0, "PASS case\n" * 100, "")
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
                    subprocess.CompletedProcess([], 0, "PASS case\n" * 99, ""),
                    subprocess.CompletedProcess(
                        [], 0, "PASS case\n" * 100 + "FAIL extra\n", ""
                    ),
                    subprocess.CompletedProcess([], 1, "PASS case\n" * 100, "error"),
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
