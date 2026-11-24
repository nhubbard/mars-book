import hashlib
import json
from pathlib import Path
import unittest
from chacha_lesson_cases import (
    ARENAS,
    WORD,
    block,
    groups,
    initial_state,
    profiles,
    quarter,
    transform,
    word_bytes,
)


class ChachaLessonCasesTest(unittest.TestCase):
    def test_preserves_original_73_case_domains_budgets_and_same_session_calls(self):
        all_groups = groups()
        manifest = json.loads(
            Path(__file__).with_name("chacha_original_checks.json").read_text()
        )
        self.assertEqual(manifest["formatVersion"], 1)
        root = Path(__file__).resolve().parents[1]
        for source in manifest["sources"]:
            self.assertEqual(
                hashlib.sha256((root / source["source"]).read_bytes()).hexdigest(),
                source["sha256"],
            )
            self.assertEqual(len(all_groups[source["group"]]), source["cases"])
        self.assertEqual(
            {k: len(v) for k, v in all_groups.items()},
            dict(quarter=12, scalar=4, msa=4, xor=37, caller=11, reuse=5),
        )
        cases = [c for rows in all_groups.values() for c in rows]
        self.assertEqual(len({c["name"] for c in cases}), 73)
        self.assertEqual(sum(len(c["calls"]) for c in cases), 95)
        self.assertEqual(
            [len(c["calls"]) for c in all_groups["reuse"]], [3, 3, 2, 4, 4]
        )
        self.assertTrue(
            all(c["instructionLimit"] == 100000 for c in all_groups["quarter"])
        )
        self.assertTrue(
            all(
                c["instructionLimit"] == 100000
                for c in all_groups["scalar"] + all_groups["msa"]
            )
        )
        for p in profiles().values():
            self.assertEqual(p["formatVersion"], 15)
            self.assertLessEqual(
                sum(c["instructionLimit"] for c in p["cases"]), 10000000
            )
            self.assertLessEqual(
                len(json.dumps(p, separators=(",", ":")).encode()) + 1, 1048576
            )

    def test_host_words_and_serialized_block_match_the_retained_chapter_examples(self):
        self.assertEqual(
            quarter([0x11111111, 0x01020304, 0x9B8D6F43, 0x01234567]),
            [0xEA2A92F4, 0xCB1CF8CE, 0x4581472E, 0x5881C4BB],
        )
        expected = bytes.fromhex(
            "10f1e7e4d13b5915500fdd1fa32071c4c7d1f4c733c068030422aa9ac3d46c4ed2826446079faa0914c2d705d98b02a2b5129cd1de164eb9cbd083e8a2503c4e"
        )
        self.assertEqual(word_bytes(block(initial_state())), expected)
        source = bytes(range(65))
        encrypted = transform(initial_state(), source)
        self.assertEqual(encrypted[:64], bytes(a ^ b for a, b in zip(source, expected)))
        next_state = initial_state(2)
        self.assertEqual(encrypted[64], 64 ^ word_bytes(block(next_state))[0])
        self.assertEqual(transform(initial_state(), encrypted), source)
        self.assertEqual(len(transform(initial_state(WORD), bytes(64))), 64)
        with self.assertRaises(ValueError):
            transform(initial_state(WORD), bytes(65))

    def test_every_callee_retains_complete_snapshots_exact_permissions_and_enclosing_history(
        self,
    ):
        for group, rows in groups().items():
            for case in rows:
                calls = case["calls"][1:] if group == "caller" else case["calls"]
                self.assertEqual(case["callerStackBytes"], 0)
                self.assertEqual(case["stackAlignment"], 8)
                self.assertEqual(len(case["preservedRegisters"]), 9)
                for call in calls:
                    self.assertEqual(
                        {
                            m["label"]: len(bytes.fromhex(m["bytes"]))
                            for m in call["memory"]
                        },
                        ARENAS,
                    )
                    self.assertEqual(len(call["accessPolicies"]), 8)
                    self.assertTrue(
                        all(p["maxAccesses"] == 250000 for p in call["accessPolicies"])
                    )
                    history = call["accessHistory"]
                    self.assertEqual(history["maxAccesses"], 250000)
                    self.assertEqual(
                        [(w["at"]["label"], w["length"]) for w in history["regions"]],
                        [("source", 4160), ("status", 4)],
                    )
                    if group != "caller":
                        self.assertEqual(
                            {
                                m["label"]: len(bytes.fromhex(m["bytes"]))
                                for m in call["setupMemory"]
                            },
                            ARENAS,
                        )
        for name in [
            "invalid count 4097 before pointers",
            "invalid count 4294967296 before pointers",
            "invalid count -1 before pointers",
            "empty before pointers",
        ]:
            c = next(c for c in groups()["xor"] if c["name"] == name)["calls"][0]
            self.assertTrue(
                all(
                    not p["readable"] and not p["writable"] for p in c["accessPolicies"]
                )
            )
            self.assertEqual(
                [int(c["arguments"][r], 16) for r in ["a0", "a1", "a2"]], [1, 1, 1]
            )
        exhausted = next(
            c for c in groups()["xor"] if c["name"] == f"counter {WORD} count 65"
        )["calls"][0]
        self.assertEqual(int(exhausted["expectedRegisters"]["v0"], 16), 2)
        self.assertTrue(all(not p["writable"] for p in exhausted["accessPolicies"]))

    def test_real_caller_dispatch_is_outside_callee_permissions_and_exit_requires_stored_status(
        self,
    ):
        for case in groups()["caller"]:
            dispatch, call = case["calls"]
            self.assertEqual(dispatch["entry"], "main")
            self.assertEqual(dispatch["returnTo"], call["entry"])
            self.assertEqual(call["returnTo"], "main_done")
            self.assertNotIn("setupMemory", call)
            self.assertNotIn("accessPolicies", dispatch)
            self.assertEqual({m["label"] for m in dispatch["setupMemory"]}, set(ARENAS))
            status = int(call["expectedRegisters"]["v0"], 16)
            self.assertEqual(
                case["exit"],
                dict(
                    code=0,
                    memory=[
                        dict(label="status", bytes=status.to_bytes(4, "little").hex())
                    ],
                ),
            )
