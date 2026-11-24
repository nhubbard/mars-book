import json
from pathlib import Path
import re
import unittest
from sha_lesson_cases import groups as original_groups
from sha_guided_cases import build, stages

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / "revision/lessons/025-sha256"


class ShaNativeUnitTest(unittest.TestCase):
    def test_staged_checks_preserve_every_original_case_and_add_only_update_prefixes(
        self,
    ):
        groups = stages()
        self.assertEqual(
            {k: len(v) for k, v in groups.items()},
            dict(
                schedule=16,
                compression=12,
                initialization=3,
                update=17,
                streaming=23,
                file=40,
                caller=12,
                reuse=22,
            ),
        )
        original = {c["name"]: c for rows in original_groups().values() for c in rows}
        completed = [c for key, rows in groups.items() if key != "update" for c in rows]
        self.assertEqual({c["name"]: c for c in completed}, original)
        for prefix in groups["update"]:
            full = original[prefix["name"].removeprefix("update prefix: ")]
            calls = (
                full["calls"][:-1]
                if full["name"].startswith("message ")
                else full["calls"]
            )
            self.assertEqual(prefix["calls"], calls)
            self.assertTrue(all(c["entry"] != "sha256_final" for c in calls))
            self.assertEqual(
                {k: v for k, v in prefix.items() if k not in ["name", "calls"]},
                {k: v for k, v in full.items() if k not in ["name", "calls"]},
            )

    def test_lesson_owns_every_profile_once_and_keeps_one_independent_source(self):
        files = build(UNIT / "independent", check=True)
        lesson = json.loads((UNIT / "lesson.json").read_text())
        assessments = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        final = assessments[-1]
        self.assertEqual(final["id"], "recheck-completed-source")
        self.assertEqual(
            sum(
                len(json.loads((UNIT / p).read_text())["cases"])
                for p in final["caseProfiles"]
            ),
            128,
        )
        self.assertEqual(
            set(final["caseProfiles"]),
            {
                p
                for s in assessments[:-1]
                if s["id"] != "complete-update"
                for p in s["caseProfiles"]
            },
        )
        assessments = assessments[:-1]
        self.assertEqual(
            [
                sum(
                    len(json.loads((UNIT / p).read_text())["cases"])
                    for p in s["caseProfiles"]
                )
                for s in assessments
            ],
            [16, 12, 3, 17, 23, 40, 12, 22],
        )
        references = [p for s in assessments for p in s["caseProfiles"]]
        self.assertEqual(len(references), len(set(references)))
        self.assertEqual(set(references), {"independent/" + name for name in files})
        self.assertEqual(len(files), 42)
        self.assertTrue(
            all(s["source"] == "independent/sha256.asm" for s in assessments)
        )
        self.assertEqual(
            (UNIT / "independent/sha256.asm").read_bytes(),
            (ROOT / "revision/labs/files/sha256.asm").read_bytes(),
        )
        for name in ["kernel", "file", "program", "reuse"]:
            self.assertEqual(
                (UNIT / f"independent/{name}.kts").read_bytes(),
                (ROOT / f"packages/025-sha256/checks/{name}.kts").read_bytes(),
            )

    def test_export_and_activity_surface_is_explicit_and_observation_sources_are_limited(
        self,
    ):
        manifest = json.loads((UNIT / "package.json").read_text())
        names = [
            "package.json",
            manifest["entry"],
            manifest["assignment"],
            manifest["nativeLesson"],
            *manifest["supportingFiles"],
        ]
        self.assertEqual(len(names), len(set(names)))
        self.assertEqual(
            set(names),
            {str(p.relative_to(UNIT)) for p in UNIT.rglob("*") if p.is_file()},
        )
        lesson = json.loads((UNIT / "lesson.json").read_text())
        self.assertEqual(len(lesson["steps"]), 35)
        predictions = [s for s in lesson["steps"] if s["kind"] == "prediction"]
        self.assertEqual(
            [s["correctChoice"] for s in predictions], ["abc"[i % 3] for i in range(16)]
        )
        debug = [s for s in lesson["steps"] if s["kind"] == "debug"]
        self.assertEqual(len(debug), 9)
        self.assertEqual(sum(len(s["stops"]) for s in debug), 32)
        self.assertTrue(all(s["source"] in names for s in lesson["steps"]))
        for source in (UNIT / "worked").glob("*.asm"):
            self.assertIsNone(
                re.search(
                    r"^sha256_(schedule|compress|init|update|final|file):",
                    source.read_text(),
                    re.M,
                )
            )
        self.assertEqual(
            (UNIT / "independent/sha256.asm").read_text().count("    break"), 6
        )

    def test_padding_and_file_observations_keep_real_boundaries_and_failure_meanings(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        debug = {s["id"]: s for s in lesson["steps"] if s["kind"] == "debug"}
        self.assertEqual(
            [
                int(s["stops"][1]["registers"]["$s3"], 16)
                for key, s in debug.items()
                if key.startswith("pad-")
            ],
            [64, 128],
        )
        high = debug["encode-high-length"]["stops"][-1]["registers"]
        self.assertEqual(int(high["$t3"], 16), 0x8000001F8)
        fill = debug["fill-before-compressing"]["stops"][-1]["registers"]
        self.assertEqual((int(fill["$s1"], 16), int(fill["$s2"], 16)), (1, 64))
        self.assertIn(
            "provisional used=64", debug["fill-before-compressing"]["explanation"]
        )
        normal, error = debug["observe-short-file"], debug["observe-read-failure"]
        self.assertEqual(normal["fileRead"]["bytes"], "616263")
        self.assertEqual(normal["fileRead"]["maxReadSize"], 2)
        self.assertEqual(normal["fileRead"]["firstDescriptor"], 19)
        self.assertEqual(error["fileRead"], dict(normal["fileRead"], failAfterBytes=2))
        self.assertEqual(int(error["stops"][-1]["registers"]["$v0"], 16), 2)
        self.assertEqual(normal["source"], "worked/read-prefixes.asm")
        self.assertEqual(error["source"], normal["source"])
        self.assertTrue(all(s["source"].startswith("worked/") for s in debug.values()))
        self.assertEqual(
            debug["serialize-given-state"]["source"], "worked/serialize.asm"
        )
        for source in ["padding-55", "padding-56", "padding-high-length"]:
            text = (UNIT / f"worked/{source}.asm").read_text()
            self.assertIn("dsll $t3, $s2, 3", text)
            self.assertIn("dsrlv $t6, $t3, $t4", text)
