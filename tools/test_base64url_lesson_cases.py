from pathlib import Path
import json
import tempfile
import unittest
import base64url_lesson_cases as url


class Base64UrlLessonCasesTests(unittest.TestCase):
    def test_url_alphabet_and_exact_unpadded_tail_lengths(self):
        for raw, expected in [
            ([], b""),
            ([77], b"TQ"),
            ([77, 97], b"TWE"),
            ([77, 97, 110], b"TWFu"),
            (url.DEFAULT, b"-____w"),
        ]:
            self.assertEqual(list(expected), url.encode(raw))
        for n in range(65):
            output = url.encode([(i * 73 + n) & 255 for i in range(n)])
            self.assertEqual((8 * n + 5) // 6, len(output))
            self.assertNotIn(61, output)
        self.assertEqual("0000000000000080", url.integer_bytes(1 << 63))
        self.assertEqual("ffffffffffffffff", url.integer_bytes(-1))

    def test_all_original_case_shapes_offsets_and_entire_arena_initializations(self):
        trials = url.trials()
        self.assertEqual(593, len(trials))
        self.assertEqual(
            [160, 165, 256, 12],
            [
                sum(t["group"] == g for t in trials)
                for g in ["short", "long", "bytes", "edges"]
            ],
        )
        for trial in trials:
            row = url.construction(trial)
            self.assertEqual(trial["seed"] % 32, row["sourceOffset"])
            self.assertEqual((trial["seed"] * 7 + 3) % 32, row["destinationOffset"])
            self.assertEqual(192, len(row["source"]))
            self.assertEqual(192, len(row["destination"]))
            self.assertEqual(
                trial["raw"],
                row["source"][
                    row["sourceOffset"] : row["sourceOffset"] + len(trial["raw"])
                ],
            )
            for j, value in enumerate(row["destination"]):
                self.assertEqual((j * 31 + trial["seed"] * 17 + 193) & 255, value)
        self.assertEqual(
            {0, 1 << 63, 192},
            {t["capacity"] for t in trials if t["raw"] == [] and t["count"] == 0},
        )

    def test_unsigned_bounds_and_preflight_precede_invalid_pointer_access(self):
        edges = [t for t in url.trials() if t["badSource"]]
        self.assertEqual([1, 1, 1, 0, 3], [url.outcome(t)[0] for t in edges])
        self.assertEqual([65, -1, -(1 << 63), 0, 3], [t["count"] for t in edges])
        for trial in edges:
            call = url.definition(trial)["calls"][0]
            self.assertEqual({}, call["argumentAddresses"])
            self.assertEqual("0x0000000000000001", call["arguments"]["a0"])
            self.assertEqual("0x0000000000000001", call["arguments"]["a2"])
            policy = call["accessPolicies"][0]
            self.assertTrue(
                all(w["length"] == 0 for w in policy["readable"] + policy["writable"])
            )
        wide = next(
            t for t in url.trials() if t["count"] == 64 and t["capacity"] == 1 << 63
        )
        self.assertEqual(0, url.outcome(wide)[0])
        self.assertEqual(86, len(url.outcome(wide)[1]))

    def test_every_original_budget_and_symbolic_workspace_stack_is_preserved(self):
        profiles = url.profiles()
        self.assertEqual(61, len(profiles))
        cases = [case for profile in profiles.values() for case in profile["cases"]]
        self.assertEqual(595, len(cases))
        self.assertEqual(600, sum(len(c["calls"]) for c in cases))
        for profile in profiles.values():
            self.assertEqual(10, profile["formatVersion"])
            self.assertLessEqual(
                sum(c["instructionLimit"] for c in profile["cases"]), 10000000
            )
        for case in cases:
            self.assertEqual(1000000, case["instructionLimit"])
            self.assertEqual(
                {"sp": {"label": "workspace", "offset": 256}},
                case["initialRegisterAddresses"],
            )
            self.assertNotIn("sp", case["initialRegisters"])
            self.assertEqual(9, len(case["preservedRegisters"]))
            self.assertEqual(
                [{"from": "function_return", "until": "done"}], case["noExecution"]
            )
        for trial in url.trials():
            case = url.definition(trial)
            call = case["calls"][0]
            policy = call["accessPolicies"][0]
            status, result = url.outcome(trial)
            self.assertEqual(
                [
                    len(trial["raw"]) if status == 0 and trial["count"] != 0 else 0,
                    len(result),
                ],
                [w["length"] for w in policy["readable"]],
            )
            self.assertEqual([len(result)], [w["length"] for w in policy["writable"]])
            before = {row["label"]: row["bytes"] for row in case["initialMemory"]}
            after = {row["label"]: row["bytes"] for row in call["memory"]}
            self.assertEqual(before["source"], after["source"])
            offset = url.construction(trial)["destinationOffset"]
            output = bytearray.fromhex(before["destination"])
            output[offset : offset + len(result)] = result
            self.assertEqual(output.hex(), after["destination"])
            self.assertTrue(
                all(before[k] == after[k] for k in before if k != "destination")
            )

    def test_repeated_calls_and_caller_phases_keep_independent_results_and_access_permissions(
        self,
    ):
        case = url.reuse_case()
        calls = case["calls"]
        self.assertEqual(
            [3, 0, 0, 1, 0], [int(c["expectedRegisters"]["v0"], 16) for c in calls]
        )
        self.assertEqual(
            [0, 6, 0, 0, 2], [int(c["expectedRegisters"]["v1"], 16) for c in calls]
        )
        self.assertEqual(
            [0, 6, 0, 0, 2],
            [c["accessPolicies"][0]["writable"][0]["length"] for c in calls],
        )
        for c in calls[1:]:
            self.assertEqual(
                bytes(url.encode(url.DEFAULT)),
                bytes.fromhex(
                    next(
                        row["bytes"]
                        for row in c["memory"]
                        if row["label"] == "destination"
                    )
                )[7:13],
            )
        caller = url.caller_case()
        self.assertEqual(
            ["url_ready", "results_ready"], [c["returnTo"] for c in caller["calls"]]
        )
        self.assertEqual(
            ["destination"],
            [
                w["at"]["label"]
                for w in caller["calls"][0]["accessPolicies"][0]["writable"]
            ],
        )
        self.assertEqual([], caller["calls"][1]["accessPolicies"][0]["readable"])
        self.assertEqual(
            ["status", "written"],
            [
                w["at"]["label"]
                for w in caller["calls"][1]["accessPolicies"][0]["writable"]
            ],
        )

    def test_checked_in_native_profiles_are_reproducible_and_stale_data_is_rejected(
        self,
    ):
        root = Path(__file__).resolve().parents[1]
        url.build(root / "revision/lessons/028-base64/independent", check=True)
        with tempfile.TemporaryDirectory() as temporary:
            files = url.build(temporary)
            url.build(temporary, check=True)
            self.assertEqual(62, len(files))
            for name, text in files.items():
                self.assertEqual(
                    json.loads(text), json.loads((Path(temporary) / name).read_text())
                )
            (Path(temporary) / "caller-exit.cases.json").write_text("{}")
            with self.assertRaises(ValueError):
                url.build(temporary, check=True)


class Base64NativePackageTests(unittest.TestCase):
    def test_complete_worked_sources_preserve_all_executed_function_bodies(self):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/028-base64"
        base = (unit / "base64-codec.asm").read_text()
        self.assertEqual(
            (root / "revision/labs/files/base64-codec.asm").read_text(), base
        )
        variants = list((unit / "worked").glob("*.asm"))
        self.assertEqual(11, len(variants))
        for variant in variants:
            self.assertEqual(
                base.split("b64_digit:", 1)[1],
                variant.read_text().split("b64_digit:", 1)[1],
            )
        for suffix, directory in [("asm", "files"), ("kts", "checks")]:
            self.assertEqual(
                (root / f"revision/labs/{directory}/base64url.{suffix}").read_bytes(),
                (unit / f"independent/base64url.{suffix}").read_bytes(),
            )

    def test_exact_archive_retains_all_scalar_vector_boundaries_and_complete_assessment_batches(
        self,
    ):
        import zipfile
        from native_lesson import bundle

        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/028-base64"
        manifest = json.loads((unit / "package.json").read_text())
        lesson = json.loads((unit / "lesson.json").read_text())
        self.assertEqual(8, lesson["formatVersion"])
        self.assertEqual(10, sum(s["kind"] == "prediction" for s in lesson["steps"]))
        debug = [s for s in lesson["steps"] if s["kind"] == "debug"]
        self.assertEqual(12, len(debug))
        self.assertEqual(25, sum(len(s["stops"]) for s in debug))
        self.assertEqual(3, sum(s["kind"] == "run" for s in lesson["steps"]))
        assess = [s for s in lesson["steps"] if s["kind"] == "assessment"]
        self.assertEqual(5, len(assess))
        self.assertEqual([16, 17, 26, 2], [len(s["caseProfiles"]) for s in assess[:-1]])
        self.assertEqual(
            [160, 165, 256, 14],
            [
                sum(
                    len(json.loads((unit / path).read_text())["cases"])
                    for path in s["caseProfiles"]
                )
                for s in assess[:-1]
            ],
        )
        for step in debug:
            for stop in step["stops"]:
                if stop["anchor"]["mnemonic"] == "syscall":
                    self.assertEqual("0x000000000000000a", stop["registers"]["$v0"])
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
