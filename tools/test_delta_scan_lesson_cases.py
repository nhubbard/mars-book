import json
from pathlib import Path
import tempfile
import unittest
import delta_scan_lesson_cases as scan


class DeltaScanLessonCasesTests(unittest.TestCase):
    def test_canonical_groups_and_full_width_mapping(self):
        self.assertEqual(scan.WIRE, scan.encode([1000, 1002, 1001, 1001, 20000]))
        self.assertEqual(
            [255, 255, 255, 255, 15, 254, 255, 255, 255, 31, 253, 255, 255, 255, 31],
            scan.encode([-2147483648, 2147483647, -2147483648]),
        )
        for value in [0, 1, 127, 128, 16383, 16384, 2147483648, 4294967295, 8589934591]:
            wire = scan.groups(value)
            self.assertLessEqual(len(wire), 5)
            self.assertLess(wire[-1], 128)
            self.assertEqual(
                value, sum((byte & 127) << (7 * i) for i, byte in enumerate(wire))
            )
            if len(wire) > 1:
                self.assertNotEqual(0, wire[-1])
        with self.assertRaises(AssertionError):
            scan.groups(8589934592)

    def test_decoder_candidate_prefix_syntax_range_and_capacity_precedence(self):
        cases = [
            ([2, 128, 0], 32, (1, [1], 2)),
            ([2, 128, 0], 1, (1, [1], 1)),
            ([128], 32, (0, [], 2)),
            ([128, 0], 32, (0, [], 2)),
            ([129, 0], 32, (0, [], 2)),
            ([128, 128, 128, 128, 32], 32, (0, [], 2)),
            ([128, 128, 128, 128, 128, 0], 32, (0, [], 2)),
            ([255, 255, 255, 255, 31], 32, (0, [], 3)),
            ([254, 255, 255, 255, 15, 2], 32, (5, [2147483647], 3)),
            ([], 0, (0, [], 0)),
            ([128], 0, (0, [], 1)),
        ]
        for wire, limit, expected in cases:
            self.assertEqual(expected, scan.decode(wire, limit))

    def test_original_296_shapes_offsets_and_full_arena_initialization(self):
        trials = scan.trials()
        self.assertEqual(296, len(trials))
        self.assertEqual(264, sum(t["group"] == "sequences" for t in trials))
        self.assertEqual(32, sum(t["group"] == "rejected" for t in trials))
        for trial in trials:
            row = scan.construction(trial)
            offset = trial["seed"] % 32
            self.assertEqual(offset, row["offset"])
            self.assertEqual(192, len(row["arena"]))
            self.assertEqual(
                trial["wire"], row["arena"][offset : offset + len(trial["wire"])]
            )
            for i, byte in enumerate(row["arena"]):
                if not offset <= i < offset + len(trial["wire"]):
                    self.assertEqual((i * 31 + trial["seed"] * 17 + 193) & 255, byte)

    def test_all_budgets_bounds_saved_bits_and_readonly_call_policies_survive(self):
        profiles = scan.profiles()
        self.assertEqual(32, len(profiles))
        cases = [c for p in profiles.values() for c in p["cases"]]
        self.assertEqual(298, len(cases))
        self.assertEqual(303, sum(len(c["calls"]) for c in cases))
        for profile in profiles.values():
            self.assertEqual(9, profile["formatVersion"])
            self.assertLessEqual(len(profile["cases"]), 10)
            self.assertLessEqual(
                sum(c["instructionLimit"] for c in profile["cases"]), 10000000
            )
        for case in cases:
            self.assertEqual(1000000, case["instructionLimit"])
            self.assertEqual(9, len(case["preservedRegisters"]))
            self.assertEqual(16, case["stackAlignment"])
            self.assertEqual(32, case["callerStackBytes"])
            self.assertEqual(
                [{"from": "function_return", "until": "done"}], case["noExecution"]
            )
            if case["calls"][0]["entry"] != "main":
                for call in case["calls"]:
                    self.assertEqual(case["initialMemory"], call["memory"])
                    policy = call["accessPolicies"][0]
                    self.assertEqual([], policy["writable"])
                    self.assertEqual(
                        call["argumentAddresses"]["a0"], policy["readable"][0]["at"]
                    )
                    length = (
                        int(call["arguments"]["a1"], 16)
                        if int(call["arguments"]["a2"], 16) > 0
                        else 0
                    )
                    self.assertEqual(length, policy["readable"][0]["length"])

    def test_reuse_errors_successes_zero_limit_and_empty_input_have_independent_results(
        self,
    ):
        case = scan.reuse_case()
        calls = case["calls"]
        self.assertEqual(5, len(calls))

        def result(c):
            return tuple(
                int(c["expectedRegisters"][r], 16) for r in ["v0", "v1", "a2", "a3"]
            )

        self.assertEqual(
            [(1, 1, 1, 2), (8, 5, 20000, 0), (0, 0, 0, 1), (2, 2, 0, 0), (0, 0, 0, 0)],
            [result(c) for c in calls],
        )
        self.assertEqual(
            [3, 41, 83, 123, 171],
            [c["argumentAddresses"]["a0"]["offset"] for c in calls],
        )
        self.assertEqual(
            [3, 8, 0, 2, 0],
            [c["accessPolicies"][0]["readable"][0]["length"] for c in calls],
        )
        caller = scan.caller_case()
        self.assertEqual(
            ["scan_ready", "results_ready"], [c["returnTo"] for c in caller["calls"]]
        )
        self.assertEqual([], caller["calls"][0]["accessPolicies"][0]["writable"])
        self.assertEqual([], caller["calls"][1]["accessPolicies"][0]["readable"])
        for label, value in [
            ("input_used", 8),
            ("reading_count", 5),
            ("last_reading", 20000),
            ("status", 0),
        ]:
            self.assertEqual(
                "ffffffff",
                next(
                    m["bytes"]
                    for m in caller["calls"][0]["memory"]
                    if m["label"] == label
                ),
            )
            self.assertEqual(
                value,
                int.from_bytes(
                    bytes.fromhex(
                        next(
                            m["bytes"]
                            for m in caller["calls"][1]["memory"]
                            if m["label"] == label
                        )
                    ),
                    "little",
                ),
            )

    def test_reproducible_checked_in_profiles_and_stale_data_detection(self):
        root = Path(__file__).resolve().parents[1]
        scan.build(root / "revision/lessons/023-delta-varint/independent", check=True)
        with tempfile.TemporaryDirectory() as temporary:
            files = scan.build(temporary)
            scan.build(temporary, check=True)
            self.assertEqual(33, len(files))
            for name, text in files.items():
                self.assertEqual(
                    json.loads(text), json.loads((Path(temporary) / name).read_text())
                )
            (Path(temporary) / "caller-exit.cases.json").write_text("{}")
            with self.assertRaises(ValueError):
                scan.build(temporary, check=True)


class DeltaNativePackageTests(unittest.TestCase):
    def test_complete_observation_sources_retain_the_executed_codec_bodies(self):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/023-delta-varint"
        base = (unit / "delta-codec.asm").read_text()
        self.assertEqual(
            (root / "revision/labs/files/delta-codec.asm").read_text(), base
        )
        candidates = list((unit / "worked").glob("*.asm"))
        self.assertEqual(10, len(candidates))
        for source in candidates:
            self.assertEqual(
                base.split("delta_encode:", 1)[1],
                source.read_text().split("delta_encode:", 1)[1],
            )
        for suffix, directory in [("asm", "files"), ("kts", "checks")]:
            self.assertEqual(
                (root / f"revision/labs/{directory}/delta-scan.{suffix}").read_bytes(),
                (unit / f"independent/delta-scan.{suffix}").read_bytes(),
            )

    def test_archive_binds_every_observation_and_complete_profile_batch_without_private_files(
        self,
    ):
        import zipfile
        from native_lesson import bundle

        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/023-delta-varint"
        manifest = json.loads((unit / "package.json").read_text())
        lesson = json.loads((unit / "lesson.json").read_text())
        self.assertEqual(8, lesson["formatVersion"])
        self.assertEqual(10, sum(s["kind"] == "prediction" for s in lesson["steps"]))
        debug = [s for s in lesson["steps"] if s["kind"] == "debug"]
        self.assertEqual(25, sum(len(s["stops"]) for s in debug))
        self.assertEqual(11, len({s["source"] for s in debug}))
        self.assertEqual(3, sum(s["kind"] == "run" for s in lesson["steps"]))
        assess = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        self.assertEqual(4, len(assess))
        self.assertEqual([27, 4, 1], [len(s["caseProfiles"]) for s in assess[:-1]])
        self.assertEqual(
            [264, 32, 2],
            [
                sum(
                    len(json.loads((unit / path).read_text())["cases"])
                    for path in s["caseProfiles"]
                )
                for s in assess[:-1]
            ],
        )
        with tempfile.TemporaryDirectory() as temporary:
            archive = bundle(unit, Path(temporary) / "student.zip")
            with zipfile.ZipFile(archive) as opened:
                expected = {
                    manifest["id"] + "/" + name
                    for name in [
                        "package.json",
                        manifest["entry"],
                        manifest["assignment"],
                        manifest["nativeLesson"],
                        *manifest["supportingFiles"],
                    ]
                }
                self.assertEqual(expected, set(opened.namelist()))
                for step in lesson["steps"]:
                    self.assertIn(
                        manifest["id"] + "/" + step.get("source", manifest["entry"]),
                        expected,
                    )
                    if step["kind"] == "assessment":
                        for path in step.get("caseProfiles", [step.get("caseProfile")]):
                            self.assertIn(manifest["id"] + "/" + path, expected)
                self.assertFalse(
                    any(
                        "private" in n or "solution" in n or "reference" in n
                        for n in expected
                    )
                )
