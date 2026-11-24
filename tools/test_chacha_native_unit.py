"""Bind the delivered word/lane guide to the full original interface corpus."""

import json
from pathlib import Path
import re
import unittest
from chacha_lesson_cases import build, initial_state, word_bytes, block

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / "revision/lessons/026-chacha20"


class ChachaNativeUnitTest(unittest.TestCase):
    def test_each_profile_has_one_owner_and_independent_interfaces_keep_the_original_sources(
        self,
    ):
        generated = build(UNIT / "independent", check=True)
        lesson = json.loads((UNIT / "lesson.json").read_text())
        groups = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        refs = [p for s in groups for p in s["caseProfiles"]]
        self.assertEqual(set(refs), {"independent/" + p for p in generated})
        self.assertEqual(len(refs), len(set(refs)))
        self.assertEqual(
            [
                sum(
                    len(json.loads((UNIT / p).read_text())["cases"])
                    for p in s["caseProfiles"]
                )
                for s in groups
            ],
            [12, 4, 4, 37, 11, 5],
        )
        self.assertTrue(all(s["source"] == "independent/chacha20.asm" for s in groups))
        self.assertEqual(
            (UNIT / "independent/chacha20.asm").read_bytes(),
            (ROOT / "packages/026-chacha20/starter/main.asm").read_bytes(),
        )
        for name in ["quarter", "scalar", "msa", "xor", "caller", "reuse"]:
            self.assertEqual(
                (UNIT / "independent" / (name + ".kts")).read_bytes(),
                (ROOT / "packages/026-chacha20/checks" / (name + ".kts")).read_bytes(),
            )

    def test_worked_sources_preserve_the_complete_executable_program_and_documented_caller_selections(
        self,
    ):
        base = (ROOT / "revision/labs/files/chacha20.asm").read_text()
        self.assertEqual((UNIT / "chacha20.asm").read_text(), base)
        expected = {
            "word-wrap": (0, 65, 1, 0),
            "scalar": (1, 65, 1, 0),
            "msa": (2, 65, 1, 0),
            "stream-65": (3, 65, 1, 0),
            "in-place-65": (3, 65, 1, 1),
            "last-counter-64": (3, 64, 0xFFFFFFFF, 0),
            "last-counter-65": (3, 65, 0xFFFFFFFF, 0),
            "large-count": (3, 1 << 32, 1, 0),
            "empty": (3, 0, 1, 0),
        }
        self.assertEqual(
            {p.stem for p in (UNIT / "worked").glob("*.asm")}, set(expected)
        )
        for name, (mode, count, counter, inplace) in expected.items():
            text = (UNIT / "worked" / (name + ".asm")).read_text()
            self.assertEqual(text.split(".text", 1)[1], base.split(".text", 1)[1])
            for label, value in [
                ("mode", mode),
                ("length", count),
                ("in_place", inplace),
            ]:
                self.assertEqual(
                    int(re.search(r"^" + label + r": \.\w+ (\d+)$", text, re.M)[1]),
                    value,
                )
            state = text.split("state: .word ", 1)[1].split("state_end:", 1)[0]
            words = [
                int(v.strip(), 0)
                for line in state.splitlines()
                if ".byte" not in line
                for v in line.replace(".word", "").split(",")
                if v.strip()
            ]
            self.assertEqual(word_bytes(words), word_bytes(initial_state(counter)))

    def test_export_surface_and_quiz_positions_are_explicit(self):
        manifest = json.loads((UNIT / "package.json").read_text())
        files = [
            "package.json",
            manifest["entry"],
            manifest["assignment"],
            manifest["nativeLesson"],
            *manifest["supportingFiles"],
        ]
        self.assertEqual(
            set(files),
            {str(p.relative_to(UNIT)) for p in UNIT.rglob("*") if p.is_file()},
        )
        self.assertEqual(len(files), len(set(files)))
        lesson = json.loads((UNIT / "lesson.json").read_text())
        self.assertEqual(len(lesson["steps"]), 31)
        predictions = [s for s in lesson["steps"] if s["kind"] == "prediction"]
        self.assertEqual(len(predictions), 14)
        self.assertEqual({s["correctChoice"] for s in predictions}, {"a", "b", "c"})
        self.assertTrue(all(s["source"] in files for s in lesson["steps"]))
        self.assertEqual(
            [s["correctChoice"] for s in predictions], ["abc"[i % 3] for i in range(14)]
        )

    def test_vector_provenance_and_tail_visits_remain_distinct_from_final_bytes(self):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        debug = {s["id"]: s for s in lesson["steps"] if s["kind"] == "debug"}
        self.assertEqual(len(debug), 10)
        self.assertEqual(sum(len(s["stops"]) for s in debug.values()), 46)
        msa = debug["follow-msa-rows"]["stops"]

        def lanes(stop, name):
            v = int(stop["vectorRegisters"][name], 16)
            return [(v >> (32 * i)) & 0xFFFFFFFF for i in range(4)]

        for n in range(4):
            name = "$w" + str(n)
            self.assertEqual(lanes(msa[0], name), initial_state()[n * 4 : n * 4 + 4])
            for st in msa[:-1]:
                self.assertEqual(
                    lanes(st, "$w" + str(n + 4)), initial_state()[n * 4 : n * 4 + 4]
                )
            shift = n
            old = lanes(msa[1], name)
            self.assertEqual(lanes(msa[2], name), old[shift:] + old[:shift])
        self.assertEqual(
            [v for n in range(4) for v in lanes(msa[5], "$w" + str(n))],
            block(initial_state()),
        )
        for name in ["walk-two-fragments", "compare-in-place-fragments"]:
            visits = [
                s
                for s in debug[name]["stops"]
                if s["anchor"]["label"] == "after_stream_bytes"
            ]
            self.assertEqual([int(s["registers"]["$s2"], 16) for s in visits], [65, 1])
            self.assertEqual([int(s["registers"]["$s3"], 16) for s in visits], [64, 1])
        self.assertEqual(
            int(debug["use-final-counter"]["stops"][1]["registers"]["$t1"], 16),
            0xFFFFFFFF,
        )
        self.assertEqual(
            int(
                debug["reject-counter-exhaustion"]["stops"][-1]["registers"]["$v0"], 16
            ),
            2,
        )
