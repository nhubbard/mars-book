from pathlib import Path
import json
import tempfile
import unittest

from console_cases import parse as console_parse, render as console_render
from function_lesson_cases import bits
import ring_lesson_cases as ring


class RingLessonCasesTest(unittest.TestCase):
    def test_all_original_kernel_trials_keep_results_bounds_and_post_return_policies(
        self,
    ):
        trials = ring.trials()
        self.assertEqual(520, len(trials))
        self.assertEqual({1, 2, 3, 7, 8, 13, 31, 32}, {t[0] for t in trials})
        statuses = set()
        for index, trial in enumerate(trials):
            case = ring.kernel_case(index, trial)
            capacity, head, records, pop, amount, _ = trial
            call = case["calls"][0]
            status = int(call["expectedRegisters"]["v0"], 16)
            if status >= 1 << 63:
                status -= 1 << 64
            auxiliary = int(call["expectedRegisters"]["v1"], 16)
            statuses.add(status)
            used = sum(len(record) + 1 for record in records)
            if pop:
                expected = (
                    (-1, 0)
                    if not records
                    else (
                        (-2, len(records[0]))
                        if amount < len(records[0])
                        else (len(records[0]), len(records[0]) + 1)
                    )
                )
            else:
                expected = (
                    (-2, 0)
                    if amount >= capacity
                    else (-1, 0) if amount + 1 > capacity - used else (0, amount + 1)
                )
            self.assertEqual(expected, (status, auxiliary))
            self.assertEqual(case["exit"]["accessPolicies"], call["accessPolicies"])
            self.assertEqual(case["exit"]["memory"], call["memory"])
            self.assertEqual(5000, case["instructionLimit"])
            self.assertEqual(bits(0x7FFF0000), case["initialRegisters"]["sp"])
            self.assertEqual([f"s{i}" for i in range(8)], case["preservedRegisters"])
            self.assertEqual(
                "function_return" if index % 2 == 0 else "done", call["returnTo"]
            )
            self.assertEqual(call["setupAddressMemory"], call["addressMemory"])
            broad = call["accessPolicies"][0]
            if status < 0:
                self.assertEqual([], broad["writable"])
            ring_policy = call["accessPolicies"][1]
            self.assertEqual(capacity, ring_policy["readable"][0]["length"])
            if pop:
                self.assertEqual([], ring_policy["writable"])
            else:
                self.assertEqual(
                    amount, call["accessPolicies"][2]["readable"][0]["length"]
                )
            self.assertLessEqual(head, capacity - 1)
        self.assertTrue({-2, -1, 0, 31} <= statuses)

    def test_caller_profiles_keep_all_twelve_prefixes_and_full_empty_failure_retry_states(
        self,
    ):
        cases = console_parse(json.dumps(ring.caller_profile()).encode())
        self.assertEqual(
            [f"main first {count} commands" for count in ring.COUNTS],
            [case.name for case in cases],
        )
        for count, case in zip(ring.COUNTS, cases):
            self.assertEqual(count, len(case.publication.snapshots))
            self.assertEqual(20000, case.instruction_limit)
            if count:
                self.assertEqual(
                    (), case.publication.final_state.access_policies[0].writable
                )
        sequence = cases[-1].publication.snapshots

        def value(index, label, offset=0):
            row = next(item for item in sequence[index].memory if item.label == label)
            return int.from_bytes(
                bytes.fromhex(row.bytes)[offset : offset + 4], "little", signed=True
            )

        self.assertEqual((0, 4), (value(7, "last_status"), value(7, "descriptor_a")))
        self.assertEqual(8, value(8, "descriptor_a", 8))
        self.assertEqual(-1, value(9, "last_status"))
        self.assertEqual((-2, 7), (value(10, "last_status"), value(10, "last_aux")))
        self.assertEqual((7, 8), (value(11, "last_status"), value(11, "last_aux")))
        self.assertEqual(sequence[9].memory[0], sequence[10].memory[0])
        self.assertEqual(
            (),
            tuple(
                w
                for w in sequence[10].access_policies[0].writable
                if w.at.label == "descriptor_a"
            ),
        )
        self.assertIn("ConsolePublicationCheck", console_render(cases))

    def test_window_merging_accepts_wide_stores_only_within_contiguous_permitted_bytes(
        self,
    ):
        self.assertEqual(
            [
                dict(at=ring.address("ring", 0), length=3),
                dict(at=ring.address("ring", 6), length=2),
            ],
            ring.windows("ring", [7, 1, 0, 6, 2, 2]),
        )
        self.assertEqual(
            [dict(at=ring.address("ring"), length=8)], ring.windows("ring", range(8))
        )
        self.assertEqual([], ring.windows("ring", []))

    def test_shipped_data_is_reproducible_with_bounded_individually_parsed_batches(
        self,
    ):
        root = Path(__file__).resolve().parents[1] / "revision/labs/checks/ring-native"
        ring.build(root, check=True)
        with tempfile.TemporaryDirectory() as temporary:
            ring.build(temporary)
            for file in root.iterdir():
                self.assertEqual(
                    file.read_bytes(), (Path(temporary) / file.name).read_bytes()
                )
        profiles = list(sorted(root.glob("kernels-*.cases.json")))
        self.assertEqual(6, len(profiles))
        self.assertEqual(
            520, sum(len(json.loads(p.read_text())["cases"]) for p in profiles)
        )
        for p in profiles:
            self.assertLessEqual(p.stat().st_size, 1024 * 1024)
            data = json.loads(p.read_text())
            self.assertEqual(23, data["formatVersion"])
            self.assertLessEqual(len(data["cases"]), 100)
        original = {
            row["name"]: row
            for p in profiles
            for row in json.loads(p.read_text())["cases"]
        }
        grouped = {}
        for operation in ["push", "pop"]:
            rows = [
                row
                for p in sorted(root.glob(operation + "-*.cases.json"))
                for row in json.loads(p.read_text())["cases"]
            ]
            self.assertEqual(260, len(rows))
            self.assertTrue(
                all(row["calls"][0]["entry"] == operation + "_record" for row in rows)
            )
            grouped.update({row["name"]: row for row in rows})
        self.assertEqual(original, grouped)
        with tempfile.TemporaryDirectory() as temporary:
            with self.assertRaises(FileNotFoundError):
                ring.build(temporary, check=True)


if __name__ == "__main__":
    unittest.main()
