import hashlib
import importlib.util
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
import tempfile
import unittest
import zipfile
import zlib
from native_lesson import bundle
from png_filter_lesson_cases import profiles

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / "revision/lessons/033-png"


class PngNativeUnitTest(unittest.TestCase):
    def test_every_original_case_is_staged_and_rechecked_in_the_unchanged_independent_starter(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        stages = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        expected = profiles()
        paths = ["independent/" + name for name in expected]
        self.assertEqual(
            [s["id"] for s in stages],
            [
                "check-none",
                "check-sub",
                "check-up",
                "check-average",
                "check-paeth",
                "check-boundaries",
                "recheck-completed-source",
            ],
        )
        self.assertEqual([p for s in stages[:-1] for p in s["caseProfiles"]], paths)
        self.assertEqual(stages[-1]["caseProfiles"], paths)
        self.assertTrue(
            all(s["source"] == "independent/png-filter-scanline.asm" for s in stages)
        )
        self.assertEqual(
            [
                sum(len(expected[p.split("/")[-1]]["cases"]) for p in s["caseProfiles"])
                for s in stages
            ],
            [44] * 5 + [46, 266],
        )
        for name, data in expected.items():
            self.assertEqual(
                json.loads((UNIT / "independent" / name).read_text()), data
            )
        self.assertEqual(
            (UNIT / "independent/png-filter-scanline.asm").read_bytes(),
            (ROOT / "revision/labs/files/png-filter-scanline.asm").read_bytes(),
        )
        self.assertEqual(
            (UNIT / "independent/png-filter-scanline.asm")
            .read_text()
            .count("    break"),
            1,
        )
        for path in (UNIT / "worked").glob("*.asm"):
            self.assertNotIn("png_filter_scanline:", path.read_text())

    def test_actual_decoder_helpers_and_trace_are_preserved_in_the_small_worked_callers(
        self,
    ):
        original = (ROOT / "revision/labs/files/png-viewer.asm").read_text()
        functions = "png_load:" + original.split("\npng_load:", 1)[1]
        for name in ["pipeline-boundaries.asm", "packing-tail.asm"]:
            text = (UNIT / "worked" / name).read_text()
            self.assertTrue(text.endswith(functions))
            for label in [
                "alphabet_order",
                "length_extra",
                "distance_extra",
                "length_base",
                "distance_base",
            ]:
                self.assertEqual(
                    next(
                        line
                        for line in text.splitlines()
                        if line.startswith(label + ":")
                    ),
                    next(
                        line
                        for line in original.splitlines()
                        if line.startswith(label + ":")
                    ),
                )
        text = (UNIT / "worked/pipeline-boundaries.asm").read_text()
        line = next(
            line for line in text.splitlines() if line.startswith("lesson_png:")
        )
        raw = bytes(int(s) & 255 for s in line.split(".byte ", 1)[1].split(","))
        self.assertEqual(raw, (UNIT / "worked/trace.png").read_bytes())
        self.assertEqual(
            hashlib.sha256(raw).hexdigest(),
            "2bad59699144ccc1de8fcb6715f5d690b41c7cac5e28bc258ab7e99af5a7d642",
        )
        for name, source in [
            ("fixture.py", "drafts/png-fixtures/fixture.py"),
            ("png-from-rows.py", "revision/labs/files/png-from-rows.py"),
        ]:
            self.assertEqual(
                (UNIT / "tools" / name).read_bytes(), (ROOT / source).read_bytes()
            )

    def test_observations_keep_extents_history_precision_tails_and_native_width_distinct(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        self.assertEqual(len(lesson["steps"]), 26)
        quizzes = [s for s in lesson["steps"] if s["kind"] == "prediction"]
        self.assertEqual(len(quizzes), 12)
        self.assertEqual(
            [s["correctChoice"] for s in quizzes], ["abc"[i % 3] for i in range(12)]
        )
        debug = {s["id"]: s for s in lesson["steps"] if s["kind"] == "debug"}
        self.assertEqual(len(debug), 6)
        self.assertEqual(sum(len(s["stops"]) for s in debug.values()), 18)
        self.assertTrue(
            all(
                s["source"].startswith("worked/")
                for s in lesson["steps"]
                if s["kind"] != "assessment"
            )
        )
        stages = debug["observe-pipeline"]["stops"]
        self.assertEqual(
            [int(s["registers"]["$v1"], 16) for s in stages], [46, 35, 35, 40, 40]
        )
        self.assertEqual(int(stages[-1]["registers"]["$s4"], 16), 0x000F1923)
        self.assertEqual(
            [
                int(debug["observe-bit-domains"]["stops"][0]["registers"][k], 16)
                for k in ["$t2", "$t3"]
            ],
            [1, 2],
        )
        self.assertEqual(
            int(
                debug["observe-overlapping-history"]["stops"][-1]["registers"]["$t4"],
                16,
            ),
            6,
        )
        self.assertEqual(
            [
                int(
                    debug["observe-predictor-precision"]["stops"][0]["registers"][k], 16
                )
                for k in ["$t2", "$t3", "$t4", "$t5"]
            ],
            [400, 200, 72, 1],
        )
        self.assertEqual(
            int(debug["observe-vector-tail"]["stops"][-1]["registers"]["$v1"], 16), 68
        )
        vector = debug["observe-vector-tail"]["stops"]
        self.assertEqual(
            vector[0]["vectorRegisters"]["$w0"], "0x0f0e0d0c0b0a09080706050403020100"
        )
        self.assertEqual(
            vector[1]["vectorRegisters"]["$w3"], "0x00090a0b000607080003040500000102"
        )
        self.assertEqual(
            vector[2]["vectorRegisters"]["$w3"], "0x002d2e2f002a2b2c0027282900242526"
        )
        self.assertEqual(
            int(debug["observe-native-settings"]["stops"][0]["registers"]["$s0"], 16),
            (1 << 32) + 1,
        )
        run = [s for s in lesson["steps"] if s["kind"] == "run"]
        self.assertEqual(len(run), 1)
        self.assertEqual(run[0]["source"], "worked/pipeline-boundaries.asm")
        self.assertEqual(run[0]["stdout"], "")

    def test_actual_distributed_fixture_and_wrapper_make_valid_exact_rows_and_refuse_replacement(
        self,
    ):
        with tempfile.TemporaryDirectory() as directory:
            folder = Path(directory)
            image = folder / "trace.png"
            command = [sys.executable, str(UNIT / "tools/fixture.py"), str(image)]
            subprocess.run(command, check=True, capture_output=True)
            self.assertEqual(
                image.read_bytes(), (UNIT / "worked/trace.png").read_bytes()
            )
            self.assertNotEqual(
                subprocess.run(command, capture_output=True).returncode, 0
            )
            rows = bytes.fromhex(
                "000a141e28323c0205050505050501141e281e1e1e030f141912121204050505050505"
            )
            (folder / "rows.bin").write_bytes(rows)
            for mode in ["stored", "fixed", "auto"]:
                target = folder / (mode + ".png")
                cmd = [
                    sys.executable,
                    str(UNIT / "tools/png-from-rows.py"),
                    str(folder / "rows.bin"),
                    str(target),
                    "--width",
                    "2",
                    "--height",
                    "5",
                    "--compression",
                    mode,
                ]
                subprocess.run(cmd, check=True, capture_output=True)
                raw = target.read_bytes()
                position = 8
                joined = b""
                lengths = []
                while position < len(raw):
                    n = struct.unpack_from(">I", raw, position)[0]
                    kind = raw[position + 4 : position + 8]
                    data = raw[position + 8 : position + 8 + n]
                    self.assertEqual(
                        zlib.crc32(kind + data),
                        struct.unpack_from(">I", raw, position + 8 + n)[0],
                    )
                    if kind == b"IDAT":
                        joined += data
                        lengths.append(n)
                    position += n + 12
                self.assertEqual(position, len(raw))
                self.assertEqual(lengths[0], 1)
                self.assertEqual(zlib.decompress(joined), rows)
                self.assertNotEqual(
                    subprocess.run(cmd, capture_output=True).returncode, 0
                )

    def test_explicit_student_exports_are_complete_and_reproducible(self):
        manifest = json.loads((UNIT / "package.json").read_text())
        names = [
            "package.json",
            manifest["entry"],
            manifest["assignment"],
            manifest["nativeLesson"],
            *manifest["supportingFiles"],
        ]
        self.assertEqual(len(names), 20)
        self.assertEqual(len(names), len(set(names)))
        self.assertEqual(
            set(names),
            {str(p.relative_to(UNIT)) for p in UNIT.rglob("*") if p.is_file()},
        )
        with tempfile.TemporaryDirectory() as directory:
            a, b = Path(directory) / "a.zip", Path(directory) / "b.zip"
            bundle(UNIT, a)
            bundle(UNIT, b)
            self.assertEqual(a.read_bytes(), b.read_bytes())
            with zipfile.ZipFile(a) as archive:
                self.assertEqual(len(archive.namelist()), 20)
                self.assertTrue(
                    all(n.startswith("033-png/") for n in archive.namelist())
                )
                self.assertFalse(
                    any(
                        re.search(r"instructor|reference|alternative|solution", n)
                        for n in archive.namelist()
                    )
                )


if __name__ == "__main__":
    unittest.main()
