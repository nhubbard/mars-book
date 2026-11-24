from pathlib import Path
import json
import tempfile
import unittest

from console_cases import parse, render
import utf8_lesson_cases as utf8


class Utf8LessonCasesTest(unittest.TestCase):
    def test_interval_oracle_agrees_with_strict_host_decoder_for_every_byte_pair(self):
        for first in range(256):
            for second in range(256):
                data = bytes([first, second])
                model = utf8.Model()
                status, used = model.feed(data, True)
                try:
                    decoded = data.decode("utf-8")
                except UnicodeDecodeError as failure:
                    self.assertEqual(
                        -2 if failure.reason == "unexpected end of data" else -1,
                        status,
                        (first, second),
                    )
                else:
                    self.assertEqual((1, 2, len(decoded)), (status, used, model.count))
                self.assertEqual(model.total, used)
                before = model.fields()
                self.assertEqual((-3, 0), model.feed(utf8.FIXTURE, False))
                self.assertEqual(before, model.fields())

    def test_boundary_scalar_encodings_and_every_cut_preserve_pending_and_final_state(
        self,
    ):
        points = [
            0,
            0x7F,
            0x80,
            0x7FF,
            0x800,
            0xD7FF,
            0xE000,
            0xFFFF,
            0x10000,
            0x10FFFF,
            0xFEFF,
            0xFDD0,
            0x1F642,
        ]
        for point in points:
            data = utf8.encode(point)
            for cut in range(len(data) + 1):
                model = utf8.Model()
                self.assertEqual((0, cut), model.feed(data[:cut]))
                self.assertEqual((0, 0), model.feed(()))
                self.assertEqual((1, len(data) - cut), model.feed(data[cut:], True))
                self.assertEqual([len(data), 1, 0, 128, 191, -1, 1, -1], model.fields())
        for lead, low, high in [
            (0xE0, 160, 191),
            (0xED, 128, 159),
            (0xF0, 144, 191),
            (0xF4, 128, 143),
        ]:
            model = utf8.Model()
            model.feed([lead])
            self.assertEqual([low, high], model.fields()[3:5])
            model.feed([low])
            self.assertEqual([128, 191], model.fields()[3:5])

    def test_all_original_streams_keep_setup_salts_state_offsets_and_terminal_access_rules(
        self,
    ):
        rows = utf8.streams()
        self.assertEqual(426, len(rows))
        self.assertEqual(426, len({s.name for s in rows}))
        statuses = set()
        for index, stream in enumerate(rows):
            case = utf8.function_case(index, stream)
            self.assertEqual(stream.name, case["name"])
            self.assertEqual(50000, case["instructionLimit"])
            model = utf8.Model()
            model.feed(stream.prefix, stream.prefix_final)
            target = "state_a" if index % 2 == 0 else "state_b"
            self.assertEqual(
                utf8.words(target, model.fields()), case["initialMemory"][0]
            )
            for number, (data, call) in enumerate(zip(stream.chunks, case["calls"])):
                terminal = bool(model.status)
                result = model.feed(data, stream.is_final(number))
                statuses.add(result[0])
                offset = (index + number) % 8
                self.assertEqual(
                    {"v0": utf8.bits(result[0]), "v1": utf8.bits(result[1])},
                    call["expectedRegisters"],
                )
                self.assertEqual(
                    utf8.memory("input", utf8.arena(data, offset, index + number)),
                    call["setupMemory"][0],
                )
                self.assertEqual(utf8.words(target, model.fields()), call["memory"][0])
                self.assertEqual(offset, call["argumentAddresses"]["a1"]["offset"])
                self.assertEqual(
                    [] if terminal else [{"at": utf8.address(target), "length": 32}],
                    call["accessPolicies"][0]["writable"],
                )
                self.assertEqual(
                    (
                        []
                        if terminal
                        else [
                            {"at": utf8.address("input", offset), "length": len(data)}
                        ]
                    ),
                    call["accessPolicies"][1]["readable"],
                )
                self.assertEqual(14, len(call["arguments"]))
                self.assertEqual(
                    "function_return" if index % 3 == 0 else "done", call["returnTo"]
                )
            self.assertEqual([], case["exit"]["accessPolicies"][0]["writable"])
        self.assertEqual({-3, -2, -1, 0, 1}, statuses)

    def test_interleaving_observes_both_states_and_revisits_terminal_outcomes(self):
        rows = utf8.interleaved_cases()
        self.assertEqual(12, len(rows))
        for case in rows:
            self.assertEqual(6, len(case["calls"]))
            targets = [c["argumentAddresses"]["a0"]["label"] for c in case["calls"]]
            self.assertNotEqual(targets[0], targets[1])
            self.assertEqual(targets[:2] * 3, targets)
            for call in case["calls"]:
                self.assertEqual(
                    ["state_a", "state_b", "input"],
                    [m["label"] for m in call["memory"]],
                )
            for call in case["calls"][-2:]:
                self.assertEqual(utf8.bits(-3), call["expectedRegisters"]["v0"])
                self.assertEqual([], call["accessPolicies"][0]["writable"])
                self.assertEqual([], call["accessPolicies"][1]["readable"])

    def test_caller_preserves_fifteen_original_variants_and_interval_policies(self):
        cases = parse(json.dumps(utf8.caller_profile()).encode())
        self.assertEqual(15, len(cases))
        for count in range(5):
            for mode in range(3):
                case = cases[count * 3 + mode]
                self.assertEqual(f"main count{count} mode{mode}", case.name)
                self.assertEqual(count, len(case.publication.snapshots))
                self.assertEqual(
                    (), case.publication.final_state.access_policies[0].writable
                )

        def fields(index):
            return [
                int.from_bytes(
                    bytes.fromhex(cases[index].publication.final_state.memory[0].bytes)[
                        i : i + 4
                    ],
                    "little",
                    signed=True,
                )
                for i in range(0, 32, 4)
            ]

        self.assertEqual([11, 5, 0, 128, 191, -1, 1, -1], fields(12))
        self.assertEqual(-1, fields(13)[6])
        self.assertEqual(5, fields(13)[7])
        self.assertEqual(-2, fields(14)[6])
        self.assertEqual(7, fields(14)[7])
        self.assertIn("ConsolePublicationCheck", render(cases))

    def test_bounded_native_exports_are_reproducible_without_regenerating_prose(self):
        root = Path(__file__).resolve().parents[1] / "revision/labs/checks/utf8-native"
        utf8.build(root, True)
        with tempfile.TemporaryDirectory() as temporary:
            utf8.build(temporary)
            for file in root.iterdir():
                self.assertEqual(
                    file.read_bytes(), (Path(temporary) / file.name).read_bytes()
                )
        profiles = sorted(root.glob("stream-*.cases.json"))
        self.assertEqual(9, len(profiles))
        self.assertEqual(
            [50] * 8 + [26], [len(json.loads(p.read_text())["cases"]) for p in profiles]
        )
        for p in root.glob("*.cases.json"):
            self.assertLessEqual(p.stat().st_size, 1024 * 1024)
        with tempfile.TemporaryDirectory() as temporary:
            with self.assertRaises(FileNotFoundError):
                utf8.build(temporary, True)


if __name__ == "__main__":
    unittest.main()
