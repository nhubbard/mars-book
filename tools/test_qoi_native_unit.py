"""Keep the QOI guide tied to its original examples and behavioral contracts."""

import json
from pathlib import Path
import unittest
from qoi_mirror_lesson_cases import CACHE_POISON, INITIAL_INDEX, TRACE, build

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / "revision/lessons/030-qoi"


class QoiNativeUnitTest(unittest.TestCase):
    def test_all_original_native_profiles_have_exactly_one_assessment_owner(self):
        files = build(UNIT / "independent", check=True)
        lesson = json.loads((UNIT / "lesson.json").read_text())
        self.assertEqual(lesson["formatVersion"], 11)
        groups = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        refs = [v for s in groups for v in s["caseProfiles"]]
        self.assertEqual(set(refs), {"independent/" + n for n in files})
        self.assertEqual(len(refs), len(set(refs)))
        self.assertEqual(
            [
                sum(
                    len(json.loads((UNIT / n).read_text())["cases"])
                    for n in s["caseProfiles"]
                )
                for s in groups
            ],
            [150, 9, 32, 4, 7, 1, 2],
        )
        self.assertTrue(
            all(s["source"] == "independent/qoi-mirrored.asm" for s in groups)
        )
        for name, folder in [
            ("qoi-mirrored.asm", "files"),
            ("qoi-mirrored.kts", "checks"),
        ]:
            self.assertEqual(
                (UNIT / "independent" / name).read_bytes(),
                (ROOT / "revision/labs" / folder / name).read_bytes(),
            )
        self.assertIn(
            "qoi_decode_mirrored:\n    break",
            (UNIT / "independent/qoi-mirrored.asm").read_text(),
        )

    def test_complete_variants_change_only_the_documented_input_or_caller_selection(
        self,
    ):
        base = (ROOT / "revision/labs/files/qoi-codec.asm").read_text()

        def actual(name):
            return (UNIT / name).read_text().split("\n", 1)[1]

        self.assertEqual(
            actual("qoi-codec.asm"), base.replace("mode: .word 0", "mode: .word 2")
        )
        self.assertEqual(actual("worked/bound.asm"), base)
        self.assertEqual(
            actual("worked/validate.asm"),
            base.replace("mode: .word 0", "mode: .word 1"),
        )
        self.assertEqual(
            actual("worked/file-load.asm"),
            base.replace("mode: .word 0", "mode: .word 4"),
        )
        self.assertEqual(
            actual("worked/short-capacity.asm"),
            actual("qoi-codec.asm").replace(
                "pixel_capacity: .dword 4096", "pixel_capacity: .dword 7"
            ),
        )
        self.assertEqual(
            actual("worked/descriptive-flags.asm"),
            actual("qoi-codec.asm").replace("0,0,0,1,4,0,-64", "0,0,0,1,3,1,-64"),
        )
        for name, data in [
            ("initial-index", INITIAL_INDEX),
            ("cache-reset", CACHE_POISON),
            ("late-marker", TRACE[:-1] + b"\0"),
        ]:
            source = actual("worked/" + name + ".asm")
            start = source.index("source: .byte")
            end = source.index("source_end:", start)
            encoded = source[start:end].splitlines()
            self.assertEqual(
                bytes(int(v) & 255 for v in encoded[0].split(".byte ")[1].split(",")),
                data,
            )
            self.assertEqual(encoded[1], "    .byte -91:" + str(20544 - len(data)))
            old = actual("qoi-codec.asm")
            a = old.index("source: .byte")
            b = old.index("source_end:", a)
            restored = source[:start] + old[a:b] + source[end:]
            expected = old.replace(
                "byte_count: .dword 38", "byte_count: .dword " + str(len(data))
            )
            if name == "late-marker":
                expected = expected.replace(
                    "pixel_capacity: .dword 4096", "pixel_capacity: .dword 0"
                )
            self.assertEqual(restored, expected)

    def test_exports_are_complete_and_supplied_read_bytes_equal_the_real_fixture(self):
        manifest = json.loads((UNIT / "package.json").read_text())
        refs = [
            "package.json",
            manifest["entry"],
            manifest["assignment"],
            manifest["nativeLesson"],
            *manifest["supportingFiles"],
        ]
        self.assertEqual(
            set(refs),
            {str(f.relative_to(UNIT)) for f in UNIT.rglob("*") if f.is_file()},
        )
        self.assertEqual(len(refs), len(set(refs)))
        lesson = json.loads((UNIT / "lesson.json").read_text())
        for step in lesson["steps"]:
            if "source" in step:
                self.assertIn(step["source"], refs)
        fixture = next(s["fileRead"] for s in lesson["steps"] if "fileRead" in s)
        self.assertEqual(
            bytes.fromhex(fixture["bytes"]), (UNIT / "worked/input.qoi").read_bytes()
        )
        self.assertEqual(fixture["maxReadSize"], 7)
        self.assertEqual(fixture["firstDescriptor"], 19)

    def test_counterexamples_and_repeated_checkpoint_have_explicit_distinct_states(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        steps = {s["id"]: s for s in lesson["steps"]}
        indexes = [
            s
            for s in steps["chunk-transitions"]["stops"]
            if s["anchor"]["label"] == "after_w_index"
        ]
        self.assertEqual(
            [s["registers"]["$s3"] for s in indexes],
            ["0x00000000ff000000", "0x00000000ff010101"],
        )
        self.assertEqual(
            [s["registers"]["$s2"] for s in indexes],
            ["0x0000000000000005", "0x0000000000000001"],
        )
        reset = steps["counterexample-states"]["stops"][1]
        self.assertEqual(reset["registers"]["$fp"], "0x0000000000000001")
        self.assertEqual(reset["registers"]["$s3"], "0x0000000000000000")
        self.assertEqual(
            steps["late-rejection"]["stops"][0]["registers"]["$v0"],
            "0x0000000000000001",
        )
        self.assertEqual(
            steps["valid-capacity-failure"]["stops"][-1]["registers"]["$v0"],
            "0x0000000000000002",
        )
