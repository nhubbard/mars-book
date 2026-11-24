from pathlib import Path
import ast
import json
import random
import tempfile
import unittest

from console_cases import parse, render
import expression_lesson_cases as expression


class ExpressionLessonCasesTest(unittest.TestCase):
    def test_fixed_results_and_first_failure_priority_are_explicit(self):
        for text, expected in expression.KNOWN:
            self.assertEqual(expected, expression.result(text), text)
        for text, expected in [
            ("9-6/2-1", (0, 5)),
            ("1/0*?", (-3, 1)),
            ("2147483648/0", (-2, 9)),
            ("(2147483647+1)*0", (-2, 11)),
            ("-(-2147483647-1)", (-2, 0)),
            ("--(-2147483647-1)", (-2, 1)),
            ("12 34", (-1, 3)),
            ("1\x002", (-1, 1)),
            ("1\x0b+2", (-1, 1)),
            ("1+é", (-1, 2)),
            (" \t\r\n", (-1, 4)),
            ("1/(2-2)", (-3, 1)),
            ("1/(2-2", (-1, 6)),
        ]:
            self.assertEqual(expected, expression.result(text), text)
        for a, b, expected in [(-7, 3, -2), (7, -3, -2), (-7, -3, 2), (-1, 2, 0)]:
            self.assertEqual(expected, expression.divide(a, b))

    def test_precedence_climbing_agrees_with_independent_host_ast_for_valid_chains(
        self,
    ):
        def evaluate(node):
            if isinstance(node, ast.Constant):
                return node.value
            left, right = evaluate(node.left), evaluate(node.right)
            return (
                left + right
                if isinstance(node.op, ast.Add)
                else (
                    left - right
                    if isinstance(node.op, ast.Sub)
                    else (
                        left * right
                        if isinstance(node.op, ast.Mult)
                        else expression.divide(left, right)
                    )
                )
            )

        generator = random.Random(17017)
        for _ in range(1000):
            values = [
                str(generator.randrange(1, 10))
                for _ in range(generator.randrange(1, 10))
            ]
            text = values[0]
            for value in values[1:]:
                text += generator.choice("+-*/") + value
            self.assertEqual(
                (0, evaluate(ast.parse(text, mode="eval").body)),
                expression.result(text),
                text,
            )

    def test_all_540_original_trials_keep_arena_salts_offsets_sentinels_and_access_policies(
        self,
    ):
        inputs = expression.inputs()
        self.assertEqual(540, len(inputs))
        names = []
        status_counts = {}
        for index, data in enumerate(inputs):
            case = expression.kernel_case(index, data)
            call = case["calls"][0]
            status, value = expression.result(data)
            status_counts[status] = status_counts.get(status, 0) + 1
            label = "input_b" if index % 3 == 0 else "input_a"
            offset = index % 33
            self.assertEqual(
                {"v0": expression.bits(status), "v1": expression.bits(value)},
                call["expectedRegisters"],
            )
            self.assertEqual(
                expression.memory(label, expression.arena(data, offset, index)),
                call["setupMemory"][0],
            )
            self.assertEqual(call["setupMemory"], call["memory"])
            self.assertEqual(
                {"a0": expression.address(label, offset)}, call["argumentAddresses"]
            )
            self.assertEqual(expression.bits(len(data)), call["arguments"]["a1"])
            self.assertEqual(
                expression.bits(0x456789AB00000000 + index), call["arguments"]["t0"]
            )
            self.assertEqual(
                "function_return" if index % 2 == 0 else "done", call["returnTo"]
            )
            self.assertEqual(
                expression.bits(0x7FFE8000 if index % 2 == 0 else 0x7FFF0000),
                case["initialRegisters"]["sp"],
            )
            self.assertEqual(
                [*[f"s{i}" for i in range(8)], "fp"], case["preservedRegisters"]
            )
            policy = call["accessPolicies"][0]
            self.assertEqual([], policy["writable"])
            self.assertEqual(
                [dict(at=expression.address(label, offset), length=len(data))],
                policy["readable"],
            )
            self.assertEqual(case["exit"]["accessPolicies"], call["accessPolicies"])
            self.assertEqual(100000, case["instructionLimit"])
            self.assertTrue(case["name"].startswith(f"expression {index} "))
            self.assertLessEqual(len(case["name"]), 128)
            names.append(case["name"])
        self.assertEqual(540, len(set(names)))
        self.assertEqual(251, status_counts[0])
        self.assertEqual({-3, -2, -1, 0}, set(status_counts))

    def test_public_recovery_examples_use_one_machine_and_preserve_current_call_input(
        self,
    ):
        rows = expression.repeated_cases()
        self.assertEqual(6, len(rows))
        for index, case in enumerate(rows):
            self.assertEqual(3, len(case["calls"]))
            self.assertEqual(f"recovery sequence {index}", case["name"])
            statuses = []
            for phase, call in enumerate(case["calls"]):
                data = expression.REPEATED[index][phase].encode()
                expected = expression.result(data)
                statuses.append(expected[0])
                label = "input_a" if (index + phase) % 2 == 0 else "input_b"
                offset = (index * 7 + phase * 5) % 33
                self.assertEqual(
                    expression.address(label, offset), call["argumentAddresses"]["a0"]
                )
                self.assertEqual(
                    expression.bits(expected[0]), call["expectedRegisters"]["v0"]
                )
                self.assertEqual(
                    expression.bits(expected[1]), call["expectedRegisters"]["v1"]
                )
                self.assertEqual([], call["accessPolicies"][0]["writable"])
                before = bytes.fromhex(call["setupMemory"][0]["bytes"])
                self.assertEqual(data, before[offset : offset + len(data)])
            self.assertIn(0, statuses)
            self.assertTrue(any(status != 0 for status in statuses))

    def test_caller_initialization_return_and_publication_are_distinct_observations(
        self,
    ):
        cases = parse(json.dumps(expression.caller_profile()).encode())
        self.assertEqual(20, len(cases))
        for index, case in enumerate(cases):
            publication = case.publication
            self.assertEqual("expression_call", publication.label)
            self.assertEqual(
                [None, "expression_ready", "expression_stored"],
                [s.label for s in publication.snapshots],
            )
            status, value = expression.result(expression.PROGRAMS[index])
            for state in publication.snapshots[1:]:
                self.assertEqual(
                    (("v0", expression.bits(status)), ("v1", expression.bits(value))),
                    state.expected_registers,
                )
            self.assertEqual(
                "6300000063000000", publication.snapshots[1].memory[-1].bytes
            )
            self.assertEqual((), publication.snapshots[1].access_policies[0].writable)
            self.assertEqual((), publication.final_state.access_policies[0].writable)
            self.assertEqual(
                "status",
                publication.snapshots[2].access_policies[0].writable[0].at.label,
            )
        self.assertIn('label = "expression_ready"', render(cases))
        self.assertIn("expectedRegisters = mapOf", render(cases))

    def test_bounded_exports_reproduce_every_original_input_without_publishing_private_rotations(
        self,
    ):
        root = (
            Path(__file__).resolve().parents[1]
            / "revision/labs/checks/expression-native"
        )
        expression.build(root, True)
        with tempfile.TemporaryDirectory() as temporary:
            expression.build(temporary)
            for file in root.iterdir():
                self.assertEqual(
                    file.read_bytes(), (Path(temporary) / file.name).read_bytes()
                )
        for kind, count in [("values", 251), ("diagnostics", 289)]:
            profiles = [
                json.loads(p.read_text())
                for p in sorted(root.glob(kind + "-*.cases.json"))
            ]
            self.assertEqual(count, sum(len(p["cases"]) for p in profiles))
            self.assertTrue(
                all(
                    p["formatVersion"] == 23 and len(p["cases"]) <= 50 for p in profiles
                )
            )
        self.assertEqual(
            6, len(json.loads((root / "repeated.cases.json").read_text())["cases"])
        )
        for p in root.glob("*.cases.json"):
            self.assertLessEqual(p.stat().st_size, 1024 * 1024)
        with tempfile.TemporaryDirectory() as temporary:
            with self.assertRaises(FileNotFoundError):
                expression.build(temporary, True)


if __name__ == "__main__":
    unittest.main()
