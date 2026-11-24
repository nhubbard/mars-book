from pathlib import Path
import json
import tempfile
import unittest
import crc_file_lesson_cases as files


class CrcFileLessonCasesTests(unittest.TestCase):
    def test_all_original_inputs_bounds_failures_offsets_and_budgets_are_retained(self):
        trials = files.trials()
        self.assertEqual(26, len(trials))
        self.assertEqual(18, sum(t["name"].startswith("file ") for t in trials))
        self.assertEqual(6, sum(t["name"].startswith("read error") for t in trials))
        self.assertEqual(
            [0, 1, 9, 0, 1, 9], [t["failAt"] for t in trials if t["failAt"] is not None]
        )
        for t in trials:
            row = files.construction(t)
            case = files.definition(t)
            self.assertEqual(1 + (t["capacity"] + t["method"]) % 16, row["pathOffset"])
            self.assertEqual((t["capacity"] + t["method"]) % 32, row["scratchOffset"])
            expected = bytearray([165] * 128)
            name = row["filename"].encode() + b"\0"
            expected[row["pathOffset"] : row["pathOffset"] + len(name)] = name
            self.assertEqual(bytes(expected), row["path"])
            self.assertEqual(
                100000 if t["missing"] else 2000000, case["instructionLimit"]
            )
            self.assertEqual(19, case["fileRead"]["firstDescriptor"])
            self.assertTrue(case["calls"][0]["readFile"])
            self.assertEqual(
                row["checksum"], int(case["calls"][0]["expectedRegisters"]["v0"], 16)
            )
            self.assertEqual(
                row["status"], int(case["calls"][0]["expectedRegisters"]["v1"], 16)
            )
            self.assertEqual(
                min(t["maxRead"], t["capacity"]),
                min(case["fileRead"]["maxReadSize"], t["capacity"]),
            )

    def test_EOF_error_and_failed_open_have_different_register_contracts(self):
        rows = [files.construction(t) for t in files.trials()]
        self.assertEqual([0] * 18 + [2] * 6 + [1] * 2, [r["status"] for r in rows])
        self.assertEqual([0, 1, 9, 0, 1, 9], [r["accepted"] for r in rows[18:24]])
        self.assertTrue(all(r["checksum"] == 0 for r in rows[18:]))
        self.assertEqual(0xCBF43926, rows[2]["checksum"])
        self.assertEqual(0, rows[0]["checksum"])

    def test_global_store_and_named_read_policies_preserve_padding_asymmetry(self):
        for t in files.trials():
            row = files.construction(t)
            policies = files.definition(t)["accessPolicies"]
            self.assertEqual(16, len(policies))
            root = policies[0]
            self.assertFalse(root["checkReads"])
            self.assertEqual([], root["readable"])
            self.assertEqual({"label": "data"}, root["from"])
            self.assertEqual({"label": "file_status", "offset": 4}, root["until"])
            self.assertEqual(
                [t["capacity"], 1024], [w["length"] for w in root["writable"]]
            )
            self.assertEqual(
                set(files.REGIONS), {p["from"]["label"] for p in policies[1:]}
            )
            for p in policies[1:]:
                self.assertEqual(
                    [t["capacity"], 1024, len(row["filename"]) + 1],
                    [w["length"] for w in p["readable"]],
                )
                self.assertEqual(root["writable"], p["writable"])
                self.assertEqual(
                    files.REGIONS[p["from"]["label"]], p["until"]["offset"]
                )
            self.assertTrue(all(p["maxAccesses"] == 100000 for p in policies))
            self.assertLessEqual(sum(p["maxAccesses"] for p in policies), 4194304)

    def test_all_modified_arenas_and_preserved_upper_bits_match_the_public_setup(self):
        for t in files.trials():
            row = files.construction(t)
            case = files.definition(t)
            memory = {
                r["label"]: bytes.fromhex(r["bytes"]) for r in case["initialMemory"]
            }
            self.assertEqual(row["path"], memory["path"])
            self.assertEqual(bytes([165]) * 320, memory["scratch"])
            self.assertEqual(bytes.fromhex("6d6c6b6a") * 272, memory["crc_table"])
            self.assertEqual(3, int.from_bytes(memory["exercise"], "little"))
            self.assertEqual(
                t["capacity"], int.from_bytes(memory["buffer_capacity"], "little")
            )
            self.assertEqual(t["method"], int.from_bytes(memory["method"], "little"))
            self.assertEqual(b"\xff" * 4, memory["file_crc"])
            self.assertEqual(b"\xff" * 4, memory["file_status"])
            self.assertEqual(0x7FFF0000, int(case["initialRegisters"]["sp"], 16))
            self.assertEqual(9, len(case["preservedRegisters"]))

    def test_repeated_method_case_reopens_without_resetting_the_case_machine(self):
        case = files.repeated_methods()
        self.assertEqual(3000000, case["instructionLimit"])
        self.assertEqual(2, len(case["calls"]))
        first, second = case["calls"]
        self.assertEqual([0, 1], [int(c["arguments"]["a3"], 16) for c in case["calls"]])
        self.assertEqual(first["argumentAddresses"], second["argumentAddresses"])
        self.assertEqual(first["expectedRegisters"], second["expectedRegisters"])
        self.assertEqual("input-7-0-9.bin", case["fileRead"]["path"])
        self.assertTrue(all(c["readFile"] for c in case["calls"]))

    def test_bounded_profiles_reproduce_and_refuse_stale_files(self):
        profiles = files.profiles()
        self.assertEqual(7, len(profiles))
        cases = [c for p in profiles.values() for c in p["cases"]]
        self.assertEqual(27, len(cases))
        self.assertEqual(28, sum(len(c["calls"]) for c in cases))
        self.assertEqual(27, len({c["name"] for c in cases}))
        for profile in profiles.values():
            self.assertEqual(13, profile["formatVersion"])
            self.assertLessEqual(
                sum(c["instructionLimit"] for c in profile["cases"]), 10000000
            )
            self.assertLessEqual(
                len(json.dumps(profile, separators=(",", ":")).encode()), 1048576
            )
        with tempfile.TemporaryDirectory() as tmp:
            outputs = files.build(tmp)
            files.build(tmp, check=True)
            self.assertEqual(7, len(outputs))
            (Path(tmp) / next(iter(outputs))).write_text("{}")
            with self.assertRaises(ValueError):
                files.build(tmp, check=True)
