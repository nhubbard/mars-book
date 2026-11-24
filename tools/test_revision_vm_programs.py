import json
from pathlib import Path
import tempfile
import unittest
from revision_vm_programs import assemble, material, prepare, prepare_source

ROOT = Path(__file__).resolve().parents[1]


class GuestFixtureTest(unittest.TestCase):
    def test_countdown_offsets_and_forward_backward_labels(self):
        self.assertEqual(
            material("countdown").hex(), "010001032031102223102201011130042100"
        )

    def test_signed_immediates_keep_their_full_bit_patterns(self):
        self.assertEqual(
            assemble([("PUSH8", -128), ("PUSH32", -(2**31))]).hex(), "01800200000080"
        )
        self.assertEqual(
            assemble([("PUSH8", 127), ("PUSH32", 2**31 - 1)]).hex(), "017f02ffffff7f"
        )

    def test_rejects_out_of_range_immediates(self):
        for op, value in [
            ("PUSH8", -129),
            ("PUSH8", 128),
            ("PUSH32", -(2**31) - 1),
            ("PUSH32", 2**31),
        ]:
            with self.subTest(op=op, value=value), self.assertRaises(ValueError):
                assemble([(op, value)])

    def test_branch_cannot_name_the_end_or_an_undefined_label(self):
        for rows in [[("JMP", "end"), ("LABEL", "end")], [("JZ", "missing")]]:
            with self.assertRaises(ValueError):
                assemble(rows)

    def test_duplicate_and_empty_labels_fail(self):
        for rows in [[("LABEL", "x"), ("LABEL", "x")], [("LABEL", "")]]:
            with self.assertRaises(ValueError):
                assemble(rows)

    def test_unknown_opcodes_and_wrong_arity_fail(self):
        for rows in [[("NOPE",)], [("HALT", 1)], [("PUSH8",)], [()]]:
            with self.assertRaises(ValueError):
                assemble(rows)

    def test_exact_image_limit_and_empty_image(self):
        self.assertEqual(assemble([]), b"")
        self.assertEqual(assemble([("HALT",)] * 64), bytes(64))
        with self.assertRaises(ValueError):
            assemble([("HALT",)] * 65)

    def test_bad_target_changes_only_the_operand(self):
        good, bad = material("countdown"), material("bad-target")
        self.assertEqual([n for n in range(len(good)) if good[n] != bad[n]], [6])
        self.assertEqual(bad[6], 6)

    def test_structural_failure_fixtures_are_explicit(self):
        self.assertEqual(material("structural-priority"), bytes([0x30, 1, 255]))
        self.assertEqual(material("after-halt"), bytes([0, 255]))
        self.assertEqual(material("truncated-word"), bytes([2, 0, 0, 0]))

    def test_control_only_preparation_preserves_all_native_instructions(self):
        text = (ROOT / "revision/labs/files/bytecode-vm.asm").read_text()
        result = prepare_source(text, material("signed-word"), 2, 1)
        self.assertEqual(result.split(".text", 1)[1], text.split(".text", 1)[1])
        self.assertIn("code_a: .byte 2,0,0,0,128,0\n    .space 90\ncode_a_end:", result)
        self.assertIn("state_a: .word 99,99,99,99,2,99", result)
        self.assertIn("length: .word 6", result)
        self.assertIn("fuel: .word 1", result)

    def test_empty_fixture_retains_a_96_byte_arena(self):
        text = (ROOT / "revision/labs/files/bytecode-vm.asm").read_text()
        self.assertIn("code_a: .space 96\ncode_a_end:", prepare_source(text, b"", 0, 0))

    def test_preparation_rejects_bounds_and_missing_harness(self):
        text = (ROOT / "revision/labs/files/bytecode-vm.asm").read_text()
        for size, cap, budget in [
            (65, 8, 64),
            (1, 9, 64),
            (1, -1, 1),
            (1, 8, 65),
            (1, 8, -1),
        ]:
            with self.assertRaises(ValueError):
                prepare_source(text, bytes(size), cap, budget)
        with self.assertRaises(ValueError):
            prepare_source(text.replace("state_a:", "other:"), b"", 0, 0)

    def test_fresh_output_and_receipt_record_the_selected_fixture(self):
        with tempfile.TemporaryDirectory() as folder:
            output = Path(folder) / "work.asm"
            source = ROOT / "revision/labs/files/bytecode-vm.asm"
            receipt = prepare(source, output, "self-loop", 0, 4)
            data = json.loads(receipt.read_text())
            self.assertEqual(data["imageHex"], "3000")
            self.assertEqual(data["guestBudget"], 4)
            with self.assertRaises(FileExistsError):
                prepare(source, output)
            output.unlink()
            with self.assertRaises(FileExistsError):
                prepare(source, output)
            self.assertFalse(output.exists())

    def test_unknown_recipe_is_rejected(self):
        with self.assertRaises(ValueError):
            material("not-a-recipe")
