import hashlib
import json
from pathlib import Path
import tempfile
import unittest
from png_filter_lesson_cases import (
    OBJECTS,
    WIDTHS,
    TIES,
    build,
    cases,
    inverse,
    invocation,
    predictor,
    profiles,
)


class PngFilterLessonCasesTest(unittest.TestCase):
    def test_full_grid_endian_settings_and_same_session_reuse(self):
        rows = cases()
        self.assertEqual(len(rows), 266)
        self.assertEqual(len({r["name"] for r in rows}), 266)
        self.assertEqual(sum(len(r["calls"]) for r in rows), 276)
        self.assertEqual(
            [sum(r["byteOrder"] == order for r in rows) for order in ["big", "little"]],
            [133, 133],
        )
        self.assertTrue(
            all(
                r["instructionLimit"] == 100000
                and r["stackAlignment"] == 8
                and r["callerStackBytes"] == 0
                for r in rows
            )
        )
        for r in rows:
            self.assertEqual(len(r["preservedRegisters"]), 9)
            for call in r["calls"]:
                self.assertEqual(
                    {
                        o["at"]["label"]: o["length"]
                        for o in call["callerAccess"]["objects"]
                    },
                    OBJECTS,
                )
                self.assertEqual(call["callerAccess"]["maxAccesses"], 100000)
                setup = bytes.fromhex(call["setupMemory"][-1]["bytes"])
                self.assertEqual(len(setup), 32)
                self.assertEqual(setup[24:], bytes([165]) * 8)
        reused = [r for r in rows if r["name"].startswith("repeated ")]
        self.assertEqual(len(reused), 2)
        for row in reused:
            self.assertEqual(len(row["calls"]), 6)
            self.assertEqual(
                [
                    int.from_bytes(
                        bytes.fromhex(c["setupMemory"][-1]["bytes"])[8:16],
                        row["byteOrder"],
                    )
                    for c in row["calls"]
                ],
                [4, 0, 3, 1, 2, 4],
            )

    def test_inverse_of_every_exact_expected_result_recovers_original_rgb(self):
        observed = 0
        for row in cases():
            for call in row["calls"]:
                if not call["memory"]:
                    continue
                setup = {
                    m["label"]: bytes.fromhex(m["bytes"]) for m in call["setupMemory"]
                }
                pointers = call["argumentAddresses"]
                selected = pointers["a0"]
                output = bytes.fromhex(call["memory"][0]["bytes"])
                n = len(output) - 1
                current = setup[selected["label"]][
                    selected["offset"] : selected["offset"] + n
                ]
                prior = pointers.get("a1")
                previous = (
                    setup[prior["label"]][prior["offset"] : prior["offset"] + n]
                    if prior is not None
                    else None
                )
                self.assertEqual(inverse(output, previous), current, row["name"])
                observed += 1
        self.assertEqual(observed, 260)

    def test_paeth_ties_precision_and_prevalidation_remain_observable(self):
        self.assertEqual([predictor(4, *tie) for tie in TIES], [255, 0, 3, 0])
        self.assertEqual(predictor(3, 255, 145, 0), 200)
        for tie, residual in zip(TIES, [202, 201, 198, 201]):
            call = invocation(2, 4, True, tie=tie)
            self.assertEqual(
                bytes.fromhex(call["memory"][0]["bytes"])[4:], bytes([residual]) * 3
            )
        for kind in [0, 1]:
            call = invocation(2, kind, True)
            self.assertEqual(call["arguments"]["a1"], "0x0000000000000001")
            self.assertEqual(len(call["callerAccess"]["readable"]), 2)
        top = invocation(2, 4, False, top=True)
        self.assertEqual(top["arguments"]["a1"], "0x0000000000000000")
        invalid = invocation(0x100000001, 0, True, capacity=7)
        self.assertEqual(invalid["expectedRegisters"]["v0"], "0x0000000000000001")
        self.assertEqual(invalid["memory"], [])
        self.assertEqual(invalid["callerAccess"]["writable"], [])
        high_capacity = invocation(2, 4, False, capacity=-(1 << 63))
        self.assertEqual(high_capacity["expectedRegisters"]["v0"], "0x0000000000000000")
        same = invocation(17, 4, True, same=True)
        self.assertEqual(
            same["argumentAddresses"]["a0"], same["argumentAddresses"]["a1"]
        )

    def test_profiles_retain_all_cases_within_existing_bounds_and_bind_the_original_checker(
        self,
    ):
        result = profiles()
        self.assertEqual(
            [len(p["cases"]) for p in result.values()], [44, 44, 44, 44, 44, 46]
        )
        for p in result.values():
            self.assertEqual(p["formatVersion"], 21)
            self.assertLessEqual(
                len(json.dumps(p, separators=(",", ":")).encode()) + 1, 1048576
            )
            self.assertLessEqual(
                sum(r["instructionLimit"] for r in p["cases"]), 40000000
            )
        root = Path(__file__).resolve().parents[1]
        binding = json.loads(
            Path(__file__).with_name("png_filter_original_check.json").read_text()
        )
        self.assertEqual(
            hashlib.sha256((root / binding["source"]).read_bytes()).hexdigest(),
            binding["sha256"],
        )
        with tempfile.TemporaryDirectory() as directory:
            self.assertEqual(build(directory), build(directory, True))
            (Path(directory) / "paeth.json").write_text("{}")
            with self.assertRaisesRegex(ValueError, "Stale PNG filter profile"):
                build(directory, True)


if __name__ == "__main__":
    unittest.main()
