import hashlib
import importlib.util
import json
from pathlib import Path
import runpy
import subprocess
import sys
import tempfile
import unittest
import zipfile
from native_lesson import bundle
from h263_read_lesson_cases import profiles

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / "revision/lessons/035-h263-stream"


def load(path, name):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class H263StreamNativeUnitTest(unittest.TestCase):
    def test_every_original_reader_case_is_staged_and_rechecked_without_weakening_quotas(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        stages = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        data = profiles()
        self.assertEqual(
            [s["id"] for s in stages],
            [
                "check-short-fields",
                "check-wide-fields",
                "check-little-endian",
                "check-boundaries",
                "check-metadata",
                "check-capacity",
                "check-reuse",
                "recheck-big",
                "recheck-little",
            ],
        )
        self.assertEqual(
            [
                sum(len(data[p.split("/")[-1]]["cases"]) for p in s["caseProfiles"])
                for s in stages
            ],
            [128, 136, 264, 26, 30, 6, 2, 328, 328],
        )
        for name, expected in data.items():
            self.assertEqual(
                json.loads((UNIT / "independent" / name).read_text()), expected
            )
        self.assertTrue(
            all(s["source"] == "independent/h263-read-bits.asm" for s in stages)
        )
        first = {
            r["name"]
            for s in stages[:-2]
            for p in s["caseProfiles"]
            for r in data[p.split("/")[-1]]["cases"]
        }
        final = {
            r["name"]
            for s in stages[-2:]
            for p in s["caseProfiles"]
            for r in data[p.split("/")[-1]]["cases"]
        }
        self.assertEqual(first, final)
        self.assertEqual(len(first), 592)
        for stage in stages:
            self.assertLessEqual(
                sum(
                    r["instructionLimit"]
                    for p in stage["caseProfiles"]
                    for r in data[p.split("/")[-1]]["cases"]
                ),
                40000000,
            )
        self.assertEqual(
            (UNIT / "independent/h263-read-bits.asm").read_bytes(),
            (ROOT / "revision/labs/files/h263-read-bits.asm").read_bytes(),
        )
        for path in (UNIT / "worked").glob("*.asm"):
            self.assertNotIn("h263_read_bits:", path.read_text())

    def test_actual_helpers_and_normative_data_are_preserved_and_small_sources_stay_bounded(
        self,
    ):
        original = (ROOT / "revision/labs/files/h263-encoder.asm").read_text()
        writer = original[original.index("h263_write_bits:\n") :]
        suffixes = {
            "append-fields.asm": "h263_write_bits:\n",
            "coefficient-events.asm": "h263_emit_coefficients:\n",
            "macroblock-syntax.asm": "h263_emit_macroblock:\n",
            "picture-header.asm": "h263_write_bits:\n",
            "clip-progress.asm": "h263_encode_clip:\n",
        }
        for name, start in suffixes.items():
            actual = (UNIT / "worked" / name).read_text()
            self.assertEqual(
                actual[actual.index(start) :], original[original.index(start) :], name
            )
            self.assertEqual(actual.count("\nmain:"), 1)
            self.assertNotIn(".include", actual)
            self.assertNotIn("/Users/", actual)
            self.assertNotIn("/private/tmp", actual)
            if name != "clip-progress.asm":
                self.assertLess(len(actual.encode()), 40000)
        for folder, names in {
            "h263-fixtures": ["fixture.py"],
            "h263-picture-construction": ["check_stream.py", "model.py"],
            "h263-macroblock-syntax": ["model.py", "codes.json"],
            "h263-coefficient-construction": ["model.py", "codes.json"],
        }.items():
            for name in names:
                self.assertEqual(
                    (UNIT / "tools" / folder / name).read_bytes(),
                    (
                        ROOT / "packages/035-h263/construction" / folder / name
                    ).read_bytes(),
                )
        data = original[
            original.index("syntax_mcbpc_codes:") : original.index("\n.text\n")
        ]
        self.assertIn(data, (UNIT / "worked/macroblock-syntax.asm").read_text())
        self.assertIn(data, (UNIT / "worked/coefficient-events.asm").read_text())

    def test_predictions_observations_and_real_file_outcomes_have_distinct_meaning(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        self.assertEqual(len(lesson["steps"]), 28)
        questions = [s for s in lesson["steps"] if s["kind"] == "prediction"]
        self.assertEqual(len(questions), 12)
        self.assertEqual(
            [s["correctChoice"] for s in questions], ["abc"[i % 3] for i in range(12)]
        )
        debug = {s["id"]: s for s in lesson["steps"] if s["kind"] == "debug"}
        self.assertEqual(len(debug), 6)
        self.assertEqual(sum(len(s["stops"]) for s in debug.values()), 18)
        coefficients = debug["observe-coefficients"]["stops"]
        self.assertEqual(int(coefficients[1]["registers"]["$s0"], 16), 52)
        self.assertEqual(int(coefficients[1]["registers"]["$v1"], 16), 22)
        self.assertEqual(
            [int(s["registers"]["$v0"], 16) for s in debug["observe-clip"]["stops"]],
            [0, 3, 3],
        )
        self.assertEqual(
            [int(s["registers"]["$s0"], 16) for s in debug["observe-clip"]["stops"]],
            [8, 1, 2],
        )
        self.assertEqual(
            [int(s["registers"]["$s1"], 16) for s in debug["observe-clip"]["stops"]],
            [2212, 696, 908],
        )
        self.assertTrue(
            all(
                s["registers"]["$s5"] == "0x0000000000000001"
                for s in debug["observe-clip"]["stops"]
            )
        )
        run = [s for s in lesson["steps"] if s["kind"] == "run"]
        self.assertEqual(len(run), 1)
        self.assertEqual(run[0]["stdout"], "0/8\n3/1\n3/2\n")
        self.assertEqual(run[0]["source"], "worked/clip-progress.asm")

    def test_raw_inputs_are_exact_python_output_with_reproducible_truncation_and_trailing_byte(
        self,
    ):
        fixture = runpy.run_path(
            str(UNIT / "tools/h263-fixtures/fixture.py"),
            run_name="h263_stream_fixture_test",
        )
        raw = (UNIT / "worked/clip.yuv").read_bytes()
        description = json.loads((UNIT / "worked/clip.json").read_text())
        self.assertEqual(raw, b"".join(fixture["frame"](i) for i in range(8)))
        self.assertEqual(
            description,
            fixture["metadata"]()
            | dict(file="clip.yuv", sha256=hashlib.sha256(raw).hexdigest()),
        )
        self.assertEqual(len(raw), 304128)
        self.assertEqual((UNIT / "worked/truncated.yuv").read_bytes(), raw[:38032])
        self.assertEqual(
            (UNIT / "worked/trailing.yuv").read_bytes(), raw[:76032] + b"\xa5"
        )
        self.assertEqual(description["sha256"], hashlib.sha256(raw).hexdigest())
        with tempfile.TemporaryDirectory() as directory:
            out = Path(directory) / "generated"
            r = subprocess.run(
                [
                    sys.executable,
                    str(UNIT / "tools/h263-fixtures/fixture.py"),
                    "--output-dir",
                    str(out),
                ],
                capture_output=True,
                text=True,
            )
            self.assertEqual(r.returncode, 0, r.stderr)
            self.assertEqual((out / "clip.yuv").read_bytes(), raw)

    def test_toolchain_recovers_equivalent_events_and_refuses_to_replace_an_existing_report(
        self,
    ):
        coefficient = load(
            UNIT / "tools/h263-coefficient-construction/model.py",
            "stream_unit_coeff_test",
        )
        levels = [255] + [0] * 63
        levels[1] = 1
        levels[8] = -1
        compact = coefficient.encode(levels, intra=True, compact=True)
        escape = coefficient.encode(levels, intra=True, compact=False)
        self.assertEqual((len(compact), len(escape)), (16, 52))
        self.assertEqual(coefficient.decode(compact, intra=True), levels)
        self.assertEqual(coefficient.decode(escape, intra=True), levels)
        # Refusal happens before any output inspection or external decoder operation.
        with tempfile.TemporaryDirectory() as directory:
            report = Path(directory) / "keep.json"
            report.write_bytes(b"keep this report\n")
            r = subprocess.run(
                [
                    sys.executable,
                    str(UNIT / "tools/inspect_outputs.py"),
                    str(Path(directory) / "absent"),
                    "--report",
                    str(report),
                ],
                capture_output=True,
                text=True,
            )
            self.assertNotEqual(r.returncode, 0)
            self.assertIn("Existing report refused", r.stderr)
            self.assertEqual(report.read_bytes(), b"keep this report\n")

    def test_explicit_deterministic_export_contains_only_public_sources_and_unfinished_reader(
        self,
    ):
        manifest = json.loads((UNIT / "package.json").read_text())
        names = {
            "package.json",
            manifest["entry"],
            manifest["assignment"],
            manifest["nativeLesson"],
            *manifest["supportingFiles"],
        }
        actual = {
            str(p.relative_to(UNIT))
            for p in UNIT.rglob("*")
            if p.is_file() and "__pycache__" not in p.parts
        }
        self.assertEqual(names, actual)
        self.assertEqual(len(names), 31)
        with tempfile.TemporaryDirectory() as directory:
            a = Path(directory) / "a.zip"
            b = Path(directory) / "b.zip"
            bundle(UNIT, a)
            bundle(UNIT, b)
            self.assertEqual(a.read_bytes(), b.read_bytes())
            with zipfile.ZipFile(a) as z:
                self.assertEqual(
                    set(z.namelist()), {"035-h263-stream/" + n for n in names}
                )
                for name in names:
                    self.assertEqual(
                        z.read("035-h263-stream/" + name), (UNIT / name).read_bytes()
                    )
                body = z.read("035-h263-stream/independent/h263-read-bits.asm").split(
                    b"\nh263_read_bits:", 1
                )[1]
                self.assertIn(b"# Replace this deliberate unfinished trap", body)
                self.assertEqual(body.count(b"    break"), 1)


if __name__ == "__main__":
    unittest.main()
