import itertools
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from native_lesson import bundle
import life_lesson_cases as life


class LifeLessonCasesTests(unittest.TestCase):
    def test_all_local_rules_and_known_finite_board_transitions(self):
        neighbors = [(2, 2), (3, 2), (4, 2), (2, 3), (4, 3), (2, 4), (3, 4), (4, 4)]
        for center in (0, 1):
            for mask in range(256):
                live = [xy for bit, xy in enumerate(neighbors) if mask >> bit & 1]
                old = life.pattern(*live, *([(3, 3)] if center else []))
                result = life.evolve(old)
                n = mask.bit_count()
                self.assertEqual(int(n == 3 or center == 1 and n == 2), result[27])
                self.assertTrue(all(value in (0, 1) for value in result))
        patterns = dict(life.PATTERNS)
        horizontal = patterns["oscillator"]
        vertical = life.pattern((3, 2), (3, 3), (3, 4))
        self.assertEqual(vertical, life.evolve(horizontal))
        self.assertEqual(horizontal, life.evolve(vertical))
        self.assertEqual(4, sum(a != b for a, b in zip(horizontal, vertical)))
        self.assertEqual(
            patterns["stable block"], life.evolve(patterns["stable block"])
        )
        self.assertEqual([0] * 64, life.evolve([0] * 64))
        self.assertEqual(
            life.pattern((0, 0), (7, 0), (0, 7), (7, 7)), life.evolve([1] * 64)
        )
        self.assertEqual([0] * 64, life.evolve(life.pattern((0, 0), (7, 0), (0, 7))))

    def test_case_contract_uses_both_roles_protected_halos_and_repeated_old_snapshot(
        self,
    ):
        cases = life.profile()["cases"]
        self.assertEqual(3, life.profile()["formatVersion"])
        self.assertEqual(14, len(cases))
        for (name, old), pair in zip(life.PATTERNS, zip(cases[::2], cases[1::2])):
            for reverse, case in enumerate(pair):
                source, destination = (
                    ("board_b", "board_a") if reverse else ("board_a", "board_b")
                )
                self.assertEqual(25, len(case["noStores"]))
                self.assertEqual(160, sum(r["length"] for r in case["noStores"]))
                protected = {
                    i
                    for r in case["noStores"]
                    if r["label"] == destination
                    for i in range(r.get("offset", 0), r.get("offset", 0) + r["length"])
                }
                halo = {i for i in range(100) if i // 10 in (0, 9) or i % 10 in (0, 9)}
                self.assertEqual(halo, protected)
                initial = {
                    r["label"]: bytes.fromhex(r["bytes"]) for r in case["initialMemory"]
                }
                self.assertEqual(life.padded(old), initial[source])
                self.assertTrue(
                    all(initial[destination][i] != 0 for i in set(range(100)) - halo)
                )
                self.assertTrue(all(initial[destination][i] == 0 for i in halo))
                self.assertEqual(10, len(case["preservedRegisters"]))
                self.assertEqual(50000, case["instructionLimit"])
                self.assertEqual(
                    ["generation_built", "life_done"],
                    [c["returnTo"] for c in case["calls"]],
                )
                for call in case["calls"]:
                    self.assertEqual(
                        {"a0": {"label": source}, "a1": {"label": destination}},
                        call["argumentAddresses"],
                    )
                    self.assertEqual({}, call["arguments"])
                    memory = {
                        r["label"]: bytes.fromhex(r["bytes"]) for r in call["memory"]
                    }
                    self.assertEqual(life.padded(life.evolve(old)), memory[destination])
                    self.assertEqual(initial[source], memory[source])
                    self.assertEqual(
                        sum(a != b for a, b in zip(old, life.evolve(old))),
                        int(call["expectedRegisters"]["v0"], 16),
                    )
                    for guard in life.GUARDS:
                        self.assertEqual(initial[guard], memory[guard])
        script = life.script()
        self.assertIn("protected.forEach { it.expectNone() }", script)
        self.assertEqual(28, script.count('callFunction("life_changes"'))

    def test_literal_sources_fresh_exports_and_student_archive_dependencies(self):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/008-life-buffers"
        for relative in ["life-buffers.asm", "independent/life-changes.asm"]:
            self.assertEqual(
                (root / "revision/labs/files" / Path(relative).name).read_bytes(),
                (unit / relative).read_bytes(),
            )
        self.assertIn(
            "life_changes:\n    break",
            (unit / "independent/life-changes.asm").read_text(),
        )
        life.build(unit / "independent", check=True)
        lesson = json.loads((unit / "lesson.json").read_text())
        manifest = json.loads((unit / "package.json").read_text())
        self.assertEqual(5, sum(s["kind"] == "prediction" for s in lesson["steps"]))
        self.assertEqual(
            8, sum(len(s["stops"]) for s in lesson["steps"] if s["kind"] == "debug")
        )
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            life.build(directory)
            self.assertEqual(
                life.profile(),
                json.loads((directory / "life-changes.cases.json").read_text()),
            )
            (directory / "life-changes.kts").write_text("stale")
            with self.assertRaises(ValueError):
                life.build(directory, check=True)
            with zipfile.ZipFile(bundle(unit, directory / "student.zip")) as archive:
                names = set(archive.namelist())
                self.assertEqual(
                    {
                        manifest["id"] + "/" + name
                        for name in [
                            "package.json",
                            manifest["entry"],
                            manifest["assignment"],
                            manifest["nativeLesson"],
                            *manifest["supportingFiles"],
                        ]
                    },
                    names,
                )
                for step in lesson["steps"]:
                    self.assertIn(
                        manifest["id"] + "/" + step.get("source", manifest["entry"]),
                        names,
                    )
                    if step["kind"] == "assessment":
                        self.assertEqual("function", step["checkKind"])
                        self.assertIn(manifest["id"] + "/" + step["caseProfile"], names)
                self.assertFalse(
                    any(
                        "solution" in n or "reference" in n or "alternative" in n
                        for n in names
                    )
                )
