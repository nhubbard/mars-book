import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from native_lesson import bundle
import sprite_lesson_cases as sprite


class SpriteLessonCasesTests(unittest.TestCase):
    def test_prefilled_frame_contract_preserves_left_half_and_changes_only_row_order(
        self,
    ):
        profile = sprite.profile()
        self.assertEqual(2, profile["formatVersion"])
        self.assertEqual(5, len(profile["cases"]))
        for index, (case, (_, rows)) in enumerate(
            zip(profile["cases"], sprite.PATTERNS)
        ):
            self.assertEqual(2, len(case["calls"]))
            self.assertEqual(
                ["after_vertical", "after_repeat"],
                [c["returnTo"] for c in case["calls"]],
            )
            self.assertEqual(10, len(case["preservedRegisters"]))
            self.assertEqual(20000, case["instructionLimit"])
            initial = {
                r["label"]: bytes.fromhex(r["bytes"]) for r in case["initialMemory"]
            }
            self.assertEqual(bytes(rows), initial["sprite"])
            for call in case["calls"]:
                expected = {
                    r["label"]: bytes.fromhex(r["bytes"]) for r in call["memory"]
                }
                self.assertEqual({}, call["expectedRegisters"])
                self.assertEqual(
                    {"a0": {"label": "sprite"}, "a1": {"label": "frame"}},
                    call["argumentAddresses"],
                )
                self.assertEqual(512, len(expected["frame"]))
                for y, byte in enumerate(reversed(rows)):
                    bits = f"{byte:08b}"
                    for x in range(16):
                        at = 4 * (16 * y + x)
                        actual = expected["frame"][at : at + 4]
                        if x < 8:
                            self.assertEqual(initial["frame"][at : at + 4], actual)
                        else:
                            self.assertEqual(
                                bytes.fromhex(
                                    "ffffff00" if bits[x - 8] == "1" else "00000000"
                                ),
                                actual,
                            )
                for label in ["sprite", "frame_guard", "rows_guard"]:
                    self.assertEqual(initial[label], expected[label])
            self.assertTrue(
                all(
                    b != bytes(4)
                    for b in [initial["frame"][at : at + 4] for at in range(0, 512, 4)]
                )
            )
        self.assertRegex(sprite.script(), r'setBytes\(\s*"frame"')
        self.assertIn('returnTo = "after_repeat"', sprite.script())

    def test_checked_in_sources_and_generated_profiles_remain_current(self):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/003-sprite-rows"
        self.assertEqual(
            (root / "revision/labs/files/sprite-rows.asm").read_bytes(),
            (unit / "sprite-rows.asm").read_bytes(),
        )
        starter = (unit / "independent/vertical-sprite.asm").read_text()
        self.assertIn("draw_vertical:\n    break", starter)
        self.assertEqual(2, starter.count("balc draw_vertical"))
        sprite.build(unit / "independent", check=True)
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary)
            sprite.build(path)
            self.assertEqual(
                sprite.profile(),
                json.loads((path / "vertical-sprite.cases.json").read_text()),
            )
            (path / "vertical-sprite.cases.json").write_text("changed")
            with self.assertRaises(ValueError):
                sprite.build(path, check=True)

    def test_student_archive_contains_every_native_activity_dependency_and_no_solution(
        self,
    ):
        unit = Path(__file__).resolve().parents[1] / "revision/lessons/003-sprite-rows"
        lesson = json.loads((unit / "lesson.json").read_text())
        manifest = json.loads((unit / "package.json").read_text())
        self.assertEqual(6, lesson["formatVersion"])
        self.assertEqual(5, sum(s["kind"] == "prediction" for s in lesson["steps"]))
        self.assertEqual(
            8, sum(len(s["stops"]) for s in lesson["steps"] if s["kind"] == "debug")
        )
        with tempfile.TemporaryDirectory() as temporary:
            with zipfile.ZipFile(
                bundle(unit, Path(temporary) / "student.zip")
            ) as archive:
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
                    any("reference" in n or "alternative" in n for n in names)
                )
