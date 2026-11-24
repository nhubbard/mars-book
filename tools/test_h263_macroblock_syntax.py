"""Restricted baseline macroblock meaning, motion wrapping and safe student composition."""

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
STAGE = ROOT / "drafts/h263-macroblock-syntax"
MODULES = {}
for name in ("model", "prepare", "compose"):
    spec = importlib.util.spec_from_file_location(
        "syntax_" + name, STAGE / (name + ".py")
    )
    m = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(m)
    MODULES[name] = m
MODEL = MODULES["model"]
PREP = MODULES["prepare"]
COMPOSE = MODULES["compose"]


class MacroblockModelTests(unittest.TestCase):
    def test_supported_numeric_tables_and_prefix_codes(self):
        self.assertEqual(
            [12, 16, 64], [len(MODEL.TABLE[k]) for k in ("mcbpc", "cbpyIntra", "mvd")]
        )
        for rows in [
            MODEL.TABLE["mcbpc"][:4],
            MODEL.TABLE["mcbpc"][4:],
            MODEL.TABLE["cbpyIntra"],
            MODEL.TABLE["mvd"],
        ]:
            codes = [format(v, f"0{w}b") for v, w in rows]
            self.assertEqual(len(codes), len(set(codes)))
            for i, a in enumerate(codes):
                for j, b in enumerate(codes):
                    if i != j:
                        self.assertFalse(a.startswith(b))
        self.assertEqual([1, 1], MODEL.TABLE["mvd"][32])
        self.assertEqual([5, 13], MODEL.TABLE["mvd"][0])
        self.assertEqual([6, 13], MODEL.TABLE["mvd"][63])
        s = (STAGE / "tables.asm").read_text()
        for label, key in [
            ("syntax_mcbpc_codes", "mcbpc"),
            ("syntax_cbpy_codes", "cbpyIntra"),
            ("syntax_mvd_codes", "mvd"),
        ]:
            lines = s.split(label + ":\n", 1)[1].split("\nsyntax_", 1)[0].splitlines()
            values = [
                int(v)
                for line in lines
                for v in line.strip().removeprefix(".dword ").split(",")
            ]
            self.assertEqual([v for pair in MODEL.TABLE[key] for v in pair], values)

    def test_every_presence_pattern_and_type_accepts_both_coefficient_encodings(self):
        for picture, intra in [(False, True), (True, True), (True, False)]:
            for mask in range(64):
                blocks = PREP.blocks(int(intra), mask)
                for compact in (False, True):
                    bits = MODEL.encode(
                        blocks, picture, intra, (0, 0), (0, 0), False, compact
                    )
                    decoded = MODEL.decode(bits, picture)
                    self.assertEqual(intra, decoded["intra"])
                    self.assertEqual(mask, decoded["mask"])
                    self.assertEqual(blocks, decoded["blocks"])
        dc = PREP.blocks(1)
        self.assertEqual(0, MODEL.presence(dc, True))
        self.assertEqual(53, len(MODEL.encode(dc)))

    def test_every_selected_predictor_pair_wraps_both_components(self):
        blocks = PREP.blocks(0)
        for actual in range(-32, 32):
            for predicted in range(-32, 32):
                motion = (actual, -actual - 1)
                predictor = (predicted, -predicted - 1)
                bits = MODEL.encode(blocks, True, False, motion, predictor, False)
                decoded = MODEL.decode(bits, True, predictor)
                self.assertEqual(motion, decoded["motion"])
                self.assertFalse(decoded["skip"])
        self.assertEqual(1, MODEL.wrap(-63))
        self.assertEqual(-1, MODEL.wrap(63))

    def test_skip_means_actual_zero_and_coded_zero_uses_nonzero_delta(self):
        blocks = PREP.blocks(0)
        pred = (31, -32)
        short = MODEL.encode(blocks, True, False, (0, 0), pred, True, choose_skip=True)
        coded = MODEL.encode(blocks, True, False, (0, 0), pred, True, choose_skip=False)
        self.assertEqual("1", short)
        self.assertGreater(len(coded), 1)
        for bits in (short, coded):
            d = MODEL.decode(bits, True, pred)
            self.assertEqual((0, 0), d["motion"])
            self.assertEqual(blocks, d["blocks"])
        self.assertFalse(
            MODEL.decode(MODEL.encode(blocks, True, False, (1, 0), pred), True, pred)[
                "skip"
            ]
        )

    def test_prefix_decoder_composes_multiple_blocks_and_strict_decoder_rejects_trailing_bits(
        self,
    ):
        first = PREP.blocks(1, 33)
        second = PREP.blocks(0)
        a = MODEL.encode(first, True, True)
        b = MODEL.encode(second, True, False, predictor=(31, -32))
        d, n = MODEL.decode_prefix(a + b, True)
        self.assertEqual(first, d["blocks"])
        self.assertEqual(len(a), n)
        e, m = MODEL.decode_prefix((a + b)[n:], True, (31, -32))
        self.assertEqual(second, e["blocks"])
        self.assertEqual(len(b), m)
        with self.assertRaises(ValueError):
            MODEL.decode(a + b, True)
        coeff = MODEL.COEFF
        block = first[0]
        bits = coeff.encode(block, True)
        out, used = coeff.decode_prefix(bits + "101", True, True)
        self.assertEqual(block, out)
        self.assertEqual(len(bits), used)
        with self.assertRaises(ValueError):
            coeff.decode(bits + "101", True)

    def test_rejects_bad_modes_vectors_domains_and_truncated_syntax(self):
        valid = PREP.blocks(1)
        for picture, intra, motion, pred in [
            (False, False, (0, 0), (0, 0)),
            (True, True, (1, 0), (0, 0)),
            (True, True, (0, 0), (1, 0)),
            (True, False, (32, 0), (0, 0)),
            (True, False, (0, 0), (-33, 0)),
        ]:
            with self.assertRaises(ValueError):
                MODEL.encode(valid, picture, intra, motion, pred)
        invalid = PREP.blocks(1)
        invalid[5][63] = 128
        with self.assertRaises(ValueError):
            MODEL.encode(invalid)
        bits = MODEL.encode(valid)
        for end in range(len(bits)):
            with self.assertRaises(ValueError):
                MODEL.decode(bits[:end])

    def test_preparation_retains_all_instructions_and_code_tables(self):
        source = (STAGE / "main.asm").read_text() + "\nstudent_helper:\n    nop\n"
        for picture, intra in [(0, 1), (1, 1), (1, 0)]:
            out = PREP.prepare(
                source,
                picture,
                intra,
                33,
                "tail",
                mx=-4 if intra == 0 else 0,
                my=6 if intra == 0 else 0,
            )
            self.assertEqual(source.split("\n.text\n")[1], out.split("\n.text\n")[1])
            self.assertEqual(
                source.split("syntax_levels:", 1)[0], out.split("syntax_levels:", 1)[0]
            )
            values = [
                int(v)
                for line in out.split("syntax_levels:\n", 1)[1]
                .split("syntax_levels_end:", 1)[0]
                .splitlines()
                for v in line.strip().removeprefix(".half ").split(",")
            ]
            self.assertEqual(
                [v for b in PREP.blocks(intra, 33, "tail") for v in b], values
            )
        for kwargs in [
            dict(picture=2),
            dict(intra=2),
            dict(allow_skip=2),
            dict(capacity=1153),
            dict(capacity=1, used=9),
            dict(mx=1),
            dict(mask=64),
            dict(picture=True),
        ]:
            with self.assertRaises(ValueError):
                PREP.prepare(source, **kwargs)

    def test_compose_carries_student_dependency_without_replacing_macroblock_entry(
        self,
    ):
        source = (STAGE / "main.asm").read_text()
        dependency = ".data\nstudent_table: .half 7\n.text\nmain:\n nop\nh263_emit_coefficients:\n li $v0, 21\nh263_write_bits:\n jr $ra\n nop\n"
        out = COMPOSE.compose(source, dependency)
        self.assertEqual(1, out.count("\n.text\n"))
        self.assertEqual(1, out.count("\nmain:\n"))
        self.assertIn("student_table: .half 7", out)
        self.assertIn("li $v0, 21", out)
        self.assertEqual(
            source.split("\n.text\n")[1].split(COMPOSE.MARKER)[0],
            out.split("\n.text\n")[1].split("\nh263_emit_coefficients:")[0],
        )
        for broken in (source.replace(COMPOSE.MARKER, ""), source + "\n.text\n"):
            with self.assertRaises(ValueError):
                COMPOSE.compose(broken, dependency)

    def test_prepare_and_compose_refuse_existing_work(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            dependency = root / "coeff.asm"
            dependency.write_text(
                ".text\nmain:\n nop\nh263_emit_coefficients:\n jr $ra\n nop\nh263_write_bits:\n jr $ra\n nop\n"
            )
            for label, args in [
                ("prepare", [sys.executable, str(STAGE / "prepare.py")]),
                (
                    "compose",
                    [
                        sys.executable,
                        str(STAGE / "compose.py"),
                        "--coefficients",
                        str(dependency),
                    ],
                ),
            ]:
                out = root / (label + ".asm")
                command = args + ["--output", str(out)]
                r = subprocess.run(command, capture_output=True, text=True)
                self.assertEqual(0, r.returncode, r.stderr)
                before = out.read_bytes()
                self.assertNotEqual(
                    0, subprocess.run(command, capture_output=True).returncode
                )
                self.assertEqual(before, out.read_bytes())


class SyntaxFeedbackRunnerTests(unittest.TestCase):
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
            result = subprocess.CompletedProcess([], 0, "PASS case\n" * 116, "")
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
                    subprocess.CompletedProcess([], 0, "PASS case\n" * 115, ""),
                    subprocess.CompletedProcess(
                        [], 0, "PASS case\n" * 116 + "FAIL extra\n", ""
                    ),
                    subprocess.CompletedProcess([], 1, "PASS case\n" * 116, "error"),
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
