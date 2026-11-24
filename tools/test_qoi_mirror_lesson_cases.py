import json
import unittest
from qoi_mirror_lesson_cases import (
    ARENAS,
    CACHE_POISON,
    INITIAL_INDEX,
    MIN_LONG,
    TRACE,
    definition,
    literal,
    outcome,
    profiles,
    raster,
    reflected_words,
    trials,
)
from revision_qoi_format import Image, decode


class QoiMirrorLessonCasesTest(unittest.TestCase):
    def test_original_domain_budgets_and_bounded_profile_batches_are_preserved(self):
        rows = trials()
        self.assertEqual(len(rows), 205)
        self.assertEqual(len({r["name"] for r in rows}), 205)
        self.assertEqual(sum(r["instructionLimit"] == 6000000 for r in rows), 182)
        self.assertEqual(sum(r["instructionLimit"] == 100000 for r in rows), 23)
        self.assertEqual(sum(r["kind"] == "memory" for r in rows), 197)
        files = profiles()
        flat = [c for p in files.values() for c in p["cases"]]
        self.assertEqual({c["name"] for c in flat}, {r["name"] for r in rows})
        self.assertEqual(len(flat), 205)
        self.assertGreater(len(files), 32)
        self.assertLessEqual(len(files), 256)
        for profile in files.values():
            self.assertEqual(profile["formatVersion"], 14)
            self.assertLessEqual(
                sum(c["instructionLimit"] for c in profile["cases"]), 10000000
            )
            self.assertLessEqual(
                len(json.dumps(profile, separators=(",", ":")).encode()) + 1,
                1024 * 1024,
            )

    def test_reflection_keeps_rows_alpha_and_native_word_byte_order(self):
        image = Image(3, 2, bytes(range(24)), 3, 1)
        expected = b"".join(
            bytes([4 * i + 2, 4 * i + 1, 4 * i, 4 * i + 3]) for i in [2, 1, 0, 5, 4, 3]
        )
        self.assertEqual(reflected_words(image), expected)
        self.assertEqual(decode(literal(image)), image)
        self.assertEqual(reflected_words(decode(INITIAL_INDEX)), bytes(4))
        self.assertEqual(
            reflected_words(decode(CACHE_POISON)), bytes.fromhex("0000011700000000")
        )
        for cap, status in [(0, 2), (7, 2), (8, 0), (MIN_LONG, 0)]:
            row = next(
                r
                for r in trials()
                if r["name"] == f"all six chunks mirror capacity {cap}"
            )
            self.assertEqual(outcome(row)["status"], status)
        malformed = next(r for r in trials() if r["group"] == "malformed")
        self.assertEqual(malformed["capacity"], 0)
        self.assertEqual(
            outcome(malformed)["status"],
            1,
            "Format rejection must precede capacity failure",
        )

    def test_memory_cases_retain_poisoned_arenas_exact_results_and_enclosing_history(
        self,
    ):
        for row in trials():
            case = definition(row)
            if row["kind"] == "caller":
                continue
            self.assertEqual(
                case["initialRegisterAddresses"],
                {"sp": {"label": "workspace", "offset": 8192}},
            )
            self.assertEqual(len(case["preservedRegisters"]), 9)
            self.assertEqual(case["callerStackBytes"], 0)
            initial = {
                v["label"]: bytes.fromhex(v["bytes"]) for v in case["initialMemory"]
            }
            self.assertEqual(initial["workspace"], bytes([165]) * 8192)
            for label, size in ARENAS.items():
                self.assertEqual(len(initial[label]), size)
                if label != "source":
                    self.assertEqual(initial[label], bytes([165]) * size)
            call = case["calls"][0]
            if row["kind"] == "count":
                self.assertEqual(call["arguments"]["a0"], "0x0000000000000001")
                self.assertNotIn("accessPolicies", call)
                self.assertNotIn("accessHistory", call)
                continue
            at = row["offset"]
            data = row["data"]
            self.assertEqual(initial["source"][at : at + len(data)], data)
            self.assertEqual(initial["source"][:at], bytes([165]) * at)
            self.assertEqual(
                initial["source"][at + len(data) :],
                bytes([165]) * (20544 - at - len(data)),
            )
            self.assertEqual(call["accessHistory"]["maxAccesses"], 200000)
            self.assertEqual(
                {
                    r["at"]["label"]: r["length"]
                    for r in call["accessHistory"]["regions"]
                },
                ARENAS,
            )
            self.assertEqual(len(call["accessPolicies"]), 13)
            self.assertTrue(
                all(p["maxAccesses"] == 200000 for p in call["accessPolicies"])
            )
            expected = outcome(row)
            if expected["status"]:
                self.assertNotIn("memory", call)
                self.assertTrue(all(not p["writable"] for p in call["accessPolicies"]))
            else:
                self.assertEqual(
                    bytes.fromhex(call["memory"][0]["bytes"]), expected["result"]
                )
                self.assertEqual(call["memory"][0]["offset"], row["outputOffset"])
                self.assertTrue(
                    all(
                        p["writable"][0]["length"] == len(expected["result"])
                        for p in call["accessPolicies"]
                    )
                )

    def test_ordinary_caller_requires_actual_exit_and_relocated_published_results(self):
        row = next(r for r in trials() if r["kind"] == "caller")
        case = definition(row)
        self.assertNotIn(
            "initialMemory", case, "The original caller uses the supplied image data"
        )
        self.assertEqual(case["calls"][0]["entry"], "main")
        self.assertEqual(case["calls"][0]["returnTo"], "mirrored_return")
        self.assertEqual(case["exit"]["code"], 0)
        expected = {m["label"]: m["bytes"] for m in case["exit"]["memory"]}
        self.assertEqual(expected, {"status": "00000000", "count": "0800000001000000"})
        self.assertEqual(
            bytes.fromhex(case["calls"][0]["memory"][0]["bytes"]),
            reflected_words(decode(TRACE)),
        )
