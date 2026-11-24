import hashlib
import runpy
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
import zipfile
import zlib
from native_lesson import bundle
from video_bounds_lesson_cases import profiles

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / "revision/lessons/034-lossless-video"


def chunk(text, start, end=None):
    result = start + text.split(start, 1)[1]
    return result.split(end, 1)[0] if end else result


class LosslessNativeUnitTest(unittest.TestCase):
    def test_original_profiles_are_staged_then_rechecked_on_one_unchanged_starter(self):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        stages = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        expected = profiles()
        self.assertEqual(
            [s["id"] for s in stages],
            [
                "check-equality",
                "check-edges",
                "check-shapes",
                "check-boundaries",
                "recheck-completed-source",
            ],
        )
        self.assertEqual(
            [
                sum(len(expected[p.split("/")[-1]]["cases"]) for p in s["caseProfiles"])
                for s in stages
            ],
            [44, 66, 44, 28, 182],
        )
        first = [p for s in stages[:-1] for p in s["caseProfiles"]]
        self.assertEqual(set(first), set(stages[-1]["caseProfiles"]))
        self.assertEqual(len(first), 8)
        self.assertTrue(
            all(s["source"] == "independent/video-change-bounds.asm" for s in stages)
        )
        for name, data in expected.items():
            self.assertEqual(
                json.loads((UNIT / "independent" / name).read_text()), data
            )
        starter = (UNIT / "independent/video-change-bounds.asm").read_bytes()
        self.assertEqual(
            starter, (ROOT / "revision/labs/files/video-change-bounds.asm").read_bytes()
        )
        self.assertEqual(starter.count(b"    break"), 1)
        for path in (UNIT / "worked").glob("*.asm"):
            self.assertNotIn("video_change_bounds:", path.read_text())

    def test_supplied_helpers_and_required_static_data_are_retained_exactly_without_whole_program_duplication(
        self,
    ):
        original = (ROOT / "revision/labs/files/lossless-video.asm").read_text()
        groups = {
            "xor-alias-tail.asm": [
                ("video_xor_words:\n", "\nfull_encoder_example_main:")
            ],
            "record-publication.asm": [("video_apply_record:\n", None)],
            "encoding-cost.asm": [
                ("video_encode_record:\n", "\n# Provided library:"),
                ("video_record_crc:\n", "\ndeltaKernel_main:"),
                ("video_xor_words:\n", "\nfull_encoder_example_main:"),
                ("video_encode_full:\n", "\nseek_example_main:"),
            ],
            "directory-seek.asm": [
                ("video_index:\n", "\nrecord_example_main:"),
                ("video_seek:\n", "\nreader_example_main:"),
                ("video_apply_record:\n", None),
            ],
        }
        for name, ranges in groups.items():
            path = UNIT / "worked" / name
            text = path.read_text()
            self.assertLess(path.stat().st_size, 150000)
            self.assertEqual(text.count("\nmain:"), 1)
            self.assertNotIn("codec_file_example_main:", text)
            for start, end in ranges:
                self.assertIn(chunk(original, start, end), text)
        data = [
            (
                "record-publication.asm",
                "record_data:",
                "\n# Complete-file input arena;",
            ),
            ("encoding-cost.asm", "delta_source:", "\n.align 4\nfile_encode_frames:"),
            ("directory-seek.asm", "index_file_data:", "\nio_path:"),
            ("directory-seek.asm", "seek_frame:", "\n.align 4\nencode_source:"),
        ]
        for name, start, end in data:
            self.assertIn(
                chunk(original, start, end), (UNIT / "worked" / name).read_text()
            )
        self.assertEqual(
            (UNIT / "tools/fixture.py").read_bytes(),
            (ROOT / "drafts/video-fixtures/fixture.py").read_bytes(),
        )

    def test_late_rejection_candidate_has_correct_crc_expected_sequence_and_invalid_progress(
        self,
    ):
        raw = (UNIT / "worked/late-record.bin").read_bytes()
        self.assertEqual(len(raw), 27)
        self.assertEqual(raw[:4], bytes([1, 0, 0, 0]))
        self.assertEqual(int.from_bytes(raw[4:8], "big"), 2)
        self.assertEqual(int.from_bytes(raw[8:12], "big"), 11)
        self.assertEqual(
            zlib.crc32(raw[:12] + raw[16:]), int.from_bytes(raw[12:16], "big")
        )
        self.assertEqual(raw[16:20], bytes([0, 0, 0, 1]))
        self.assertEqual(raw[23:], bytes(4))
        line = next(
            s
            for s in (UNIT / "worked/record-publication.asm").read_text().splitlines()
            if s.startswith("lesson_bad_record:")
        )
        self.assertEqual(
            bytes(int(v) & 255 for v in line.split(".byte ", 1)[1].split(",")), raw
        )

    def test_native_predictions_and_observations_separate_counts_publication_deadlines_and_dimensions(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        self.assertEqual(len(lesson["steps"]), 24)
        questions = [s for s in lesson["steps"] if s["kind"] == "prediction"]
        self.assertEqual(len(questions), 12)
        self.assertEqual(
            [s["correctChoice"] for s in questions], ["abc"[i % 3] for i in range(12)]
        )
        debug = {s["id"]: s for s in lesson["steps"] if s["kind"] == "debug"}
        self.assertEqual(len(debug), 6)
        self.assertEqual(sum(len(s["stops"]) for s in debug.values()), 18)
        self.assertTrue(
            all(s["integerInputs"] == [] and "stdout" not in s for s in debug.values())
        )
        xor = debug["observe-xor"]["stops"]
        self.assertEqual(
            xor[0]["vectorRegisters"]["$w2"], "0xffffffff000000100000000080000000"
        )
        self.assertEqual([int(s["registers"]["$v1"], 16) for s in xor], [3, 4, 0])
        encoding = debug["observe-encoding"]["stops"]
        self.assertEqual(
            [int(s["registers"]["$v1"], 16) for s in encoding], [40, 20, 0]
        )
        seek = debug["observe-seek"]["stops"][1]
        self.assertEqual(
            [int(seek["registers"][r], 16) for r in ["$s0", "$s1", "$s2"]], [6, 4, 3]
        )
        deadlines = debug["observe-deadlines"]["stops"]
        self.assertEqual(int(deadlines[-1]["registers"]["$s5"], 16), 1320)
        text = (UNIT / "worked/anchored-deadlines.asm").read_text()
        self.assertEqual(text.count("syscall"), 1)
        self.assertIn("no host-clock measurement", text)
        wide = debug["observe-coordinates"]["stops"][-1]
        self.assertEqual(int(wide["registers"]["$s0"], 16), 0x100000001)
        run = [s for s in lesson["steps"] if s["kind"] == "run"]
        self.assertEqual(len(run), 1)
        self.assertEqual(run[0]["stdout"], "")
        self.assertEqual(run[0]["source"], "worked/directory-seek.asm")

    def test_archive_is_explicit_complete_deterministic_and_contains_no_private_implementation(
        self,
    ):
        manifest = json.loads((UNIT / "package.json").read_text())
        expected = {
            "package.json",
            manifest["entry"],
            manifest["assignment"],
            manifest["nativeLesson"],
            *manifest["supportingFiles"],
        }
        actual = {str(p.relative_to(UNIT)) for p in UNIT.rglob("*") if p.is_file()}
        self.assertEqual(expected, actual)
        self.assertEqual(len(actual), 21)
        with tempfile.TemporaryDirectory() as directory:
            a = Path(directory) / "a.zip"
            b = Path(directory) / "b.zip"
            bundle(UNIT, a)
            bundle(UNIT, b)
            self.assertEqual(a.read_bytes(), b.read_bytes())
            with zipfile.ZipFile(a) as z:
                self.assertEqual(
                    set(z.namelist()), {"034-lossless-video/" + p for p in expected}
                )
                for name in expected:
                    self.assertEqual(
                        z.read("034-lossless-video/" + name), (UNIT / name).read_bytes()
                    )
                self.assertEqual(
                    z.read(
                        "034-lossless-video/independent/video-change-bounds.asm"
                    ).count(b"    break"),
                    1,
                )

    def test_distributed_fixture_generates_exact_original_artifacts_and_refuses_overwrite(
        self,
    ):
        script = UNIT / "tools/fixture.py"
        module = runpy.run_path(str(script), run_name="native_video_fixture")
        with tempfile.TemporaryDirectory() as directory:
            for name in module["NAMES"]:
                out = Path(directory) / name
                r = subprocess.run(
                    [sys.executable, str(script), name, "--output", str(out)],
                    capture_output=True,
                    text=True,
                )
                self.assertEqual(r.returncode, 0, r.stderr)
                model = module["material"](name)
                self.assertEqual((out / "animation.mlv").read_bytes(), model["file"])
                for i, frame in enumerate(model["frames"]):
                    self.assertEqual(
                        (out / f"frame-{i:03}.ppm").read_bytes(),
                        b"P6\n64 64\n255\n" + frame,
                    )
                observed = json.loads((out / "observations.json").read_text())
                self.assertEqual(observed["file_bytes"], len(model["file"]))
                self.assertEqual(observed["directory"], model["directory"])
                before = hashlib.sha256(
                    (out / "animation.mlv").read_bytes()
                ).hexdigest()
                r = subprocess.run(
                    [sys.executable, str(script), name, "--output", str(out)],
                    capture_output=True,
                )
                self.assertNotEqual(r.returncode, 0)
                self.assertEqual(
                    hashlib.sha256((out / "animation.mlv").read_bytes()).hexdigest(),
                    before,
                )


if __name__ == "__main__":
    unittest.main()
