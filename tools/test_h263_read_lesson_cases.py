import hashlib
import json
from pathlib import Path
import tempfile
import unittest
from h263_read_lesson_cases import INPUT_BYTES, build, cases, profiles


class H263ReadLessonCasesTest(unittest.TestCase):
    def test_complete_original_grid_budgets_orders_and_retained_cursor_sequence(self):
        rows = cases()
        self.assertEqual(len(rows), 592)
        self.assertEqual(len({r["name"] for r in rows}), 592)
        self.assertEqual(sum(len(r["calls"]) for r in rows), 598)
        self.assertEqual(
            [sum(r["byteOrder"] == o for r in rows) for o in ["big", "little"]],
            [296, 296],
        )
        for row in rows:
            self.assertEqual(row["instructionLimit"], 100000)
            self.assertEqual(row["stackAlignment"], 16)
            self.assertEqual(row["callerStackBytes"], 0)
            self.assertEqual(len(row["preservedRegisters"]), 10)
        for row in rows[-2:]:
            self.assertEqual(
                [int(c["expectedRegisters"]["v0"], 16) for c in row["calls"]],
                [0, 0, 2, 0],
            )
            self.assertEqual(
                [int(c["expectedRegisters"]["v1"], 16) for c in row["calls"]],
                [5, 419, 0, 0],
            )
            self.assertTrue(row["calls"][0]["setupAddressMemory"])
            self.assertTrue(
                all(
                    not c["setupMemory"] and not c["setupAddressMemory"]
                    for c in row["calls"][1:]
                )
            )

    def test_bit_strings_from_actual_bytes_match_every_success_and_exact_cursor_publication(
        self,
    ):
        successes = 0
        for row in cases():
            context = bytearray(48)
            data = {}
            source_offset = None
            order = row["byteOrder"]
            for c in row["calls"]:
                for r in c["setupMemory"]:
                    raw = bytes.fromhex(r["bytes"])
                    if r["label"] == "reader_context":
                        context[r["offset"] : r["offset"] + len(raw)] = raw
                    else:
                        data.update({r["offset"] + i: v for i, v in enumerate(raw)})
                if c["setupAddressMemory"]:
                    source_offset = c["setupAddressMemory"][0]["value"]["offset"]
                cursor = int.from_bytes(context[24:32], order)
                valid = int.from_bytes(context[16:24], order)
                width = int(c["arguments"]["a1"], 16)
                status = int(c["expectedRegisters"]["v0"], 16)
                if status == 0:
                    successes += 1
                    self.assertLessEqual(cursor + width, valid)
                    # Independent string slicing, rather than the model's bit-at-a-time accumulator.
                    first, last = cursor // 8, (cursor + width + 7) // 8
                    bits = (
                        "".join(
                            f"{data[source_offset+i]:08b}" for i in range(first, last)
                        )
                        if width
                        else ""
                    )
                    selected = bits[cursor % 8 : cursor % 8 + width] if width else ""
                    self.assertEqual(
                        int(c["expectedRegisters"]["v1"], 16),
                        int(selected, 2) if selected else 0,
                        row["name"],
                    )
                    self.assertEqual(
                        c["callerAccess"]["writable"],
                        (
                            [dict(at=dict(label="reader_context", offset=24), length=8)]
                            if width
                            else []
                        ),
                    )
                    for result in c["memory"]:
                        raw = bytes.fromhex(result["bytes"])
                        context[result["offset"] : result["offset"] + len(raw)] = raw
                    self.assertEqual(
                        int.from_bytes(context[24:32], order), cursor + width
                    )
                else:
                    self.assertEqual(c["memory"], [])
                    self.assertEqual(c["callerAccess"]["writable"], [])
                    self.assertEqual(int(c["expectedRegisters"]["v1"], 16), 0)
                prefix = c["accessPolicies"][0]
                self.assertEqual(prefix["from"], dict(label="reader_bytes", offset=0))
                self.assertEqual(
                    prefix["until"], dict(label="reader_bytes", offset=1048576)
                )
                self.assertIn(
                    dict(at=dict(label="reader_bytes_tail", offset=0), length=64),
                    c["callerAccess"]["objects"],
                )
                self.assertEqual(prefix["writable"], c["callerAccess"]["writable"])
                input_reads = [
                    w
                    for w in prefix["readable"]
                    if w["at"]["label"] in ["reader_bytes", "reader_bytes_tail"]
                ]
                self.assertEqual(len(input_reads), int(status == 0 and width > 0))
                if input_reads:
                    allowed = input_reads[0]
                    resolved = allowed["at"]["offset"] + (
                        1048544 if allowed["at"]["label"] == "reader_bytes_tail" else 0
                    )
                    self.assertEqual(resolved, source_offset + cursor // 8)
                    self.assertLessEqual(abs(allowed["at"]["offset"]), 1048576)
                    self.assertEqual(
                        allowed["length"], (cursor + width + 7) // 8 - cursor // 8
                    )
                    self.assertLessEqual(allowed["length"], 5)
        self.assertEqual(successes, 554)

    def test_public_tail_anchor_preserves_the_original_allocation_and_unfinished_body(
        self,
    ):
        root = Path(__file__).resolve().parents[1]
        source = (root / "revision/labs/files/h263-read-bits.asm").read_text()
        self.assertIn(
            "reader_bytes: .byte -70,53\n    .space 1048542\nreader_bytes_tail: .space 64",
            source,
        )
        self.assertEqual(2 + 1048542 + 64, INPUT_BYTES)
        self.assertEqual(
            source.split("h263_read_bits:\n", 1)[1],
            "    # Replace this deliberate unfinished trap with your implementation.\n    break\n    jrc $ra\n    nop\n",
        )

    def test_native_profiles_are_bounded_and_original_check_is_unchanged(self):
        p = profiles()
        self.assertEqual(
            [len(v["cases"]) for v in p.values()], [128, 136, 128, 136, 26, 30, 6, 2]
        )
        self.assertEqual(
            {c["name"] for v in p.values() for c in v["cases"]},
            {c["name"] for c in cases()},
        )
        for v in p.values():
            self.assertEqual(v["formatVersion"], 22)
            self.assertLessEqual(len(v["cases"]), 256)
            self.assertLessEqual(
                len(json.dumps(v, separators=(",", ":")).encode()) + 1, 1048576
            )
            self.assertLessEqual(
                sum(c["instructionLimit"] for c in v["cases"]), 40000000
            )
        for order in ["big", "little"]:
            final = [
                c
                for name, v in p.items()
                if not name.startswith("fields-") or name.startswith("fields-" + order)
                for c in v["cases"]
            ]
            self.assertEqual(len(final), 328)
            self.assertLessEqual(sum(c["instructionLimit"] for c in final), 40000000)
        root = Path(__file__).resolve().parents[1]
        binding = json.loads(
            Path(__file__).with_name("h263_read_original_check.json").read_text()
        )
        self.assertEqual(
            hashlib.sha256((root / binding["source"]).read_bytes()).hexdigest(),
            binding["sha256"],
        )
        with tempfile.TemporaryDirectory() as d:
            self.assertEqual(build(d), build(d, True))
            (Path(d) / "capacity.json").write_text("{}")
            with self.assertRaisesRegex(ValueError, "Stale reader profile"):
                build(d, True)


if __name__ == "__main__":
    unittest.main()
