import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from native_lesson import bundle

import function_lesson_cases as functions


class FunctionLessonCasesTests(unittest.TestCase):
    def test_all_inputs_upper_patterns_links_and_preservation_are_declared(self):
        cases = functions.profile()["cases"]
        self.assertEqual(3, len(cases))
        for case, (_, upper, seed, stack) in zip(cases, functions.CONFIGURATIONS):
            self.assertEqual(256, len(case["calls"]))
            self.assertEqual(100000, case["instructionLimit"])
            self.assertEqual(stack, int(case["initialRegisters"]["sp"], 16))
            self.assertEqual(
                set(functions.saved_values(seed)), set(case["preservedRegisters"])
            )
            self.assertEqual(10, len(case["preservedRegisters"]))
            self.assertEqual(32, case["callerStackBytes"])
            self.assertEqual(16, case["stackAlignment"])
            for byte, call in enumerate(case["calls"]):
                self.assertEqual(upper | byte, int(call["arguments"]["a0"], 16))
                expected = (
                    sum(((byte >> bit) & 1) << (7 - bit) for bit in range(8)) ^ 255
                )
                self.assertEqual(expected, int(call["expectedRegisters"]["v0"], 16))
                self.assertEqual(
                    "after_invert" if byte % 2 == 0 else "after_repeat",
                    call["returnTo"],
                )
        self.assertIn("uL.toLong()", functions.script())
        self.assertIn("helper.snapshot().instructions > before", functions.script())
        self.assertIn(
            "readBytes(top, 32).contentEquals(callerBytes)", functions.script()
        )

    def test_checked_in_sources_are_consistent_and_generated_data_is_current(self):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/003-row-functions"
        self.assertEqual(
            (root / "revision/labs/files/reverse-rows.asm").read_bytes(),
            (unit / "reverse-rows.asm").read_bytes(),
        )
        starter = (unit / "independent/reverse-and-invert.asm").read_text()
        self.assertIn("reverse_and_invert:\n    break", starter)
        self.assertEqual(2, starter.count("balc reverse_and_invert"))
        functions.build(unit / "independent", check=True)
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            functions.build(directory)
            data = json.loads((directory / "reverse-and-invert.cases.json").read_text())
            self.assertEqual(functions.profile(), data)
            (directory / "reverse-and-invert.kts").write_text("changed")
            with self.assertRaises(ValueError):
                functions.build(directory, check=True)

    def test_native_package_exports_all_activity_dependencies_and_only_student_files(
        self,
    ):
        root = (
            Path(__file__).resolve().parents[1] / "revision/lessons/003-row-functions"
        )
        plan = json.loads((root / "lesson.json").read_text())
        manifest = json.loads((root / "package.json").read_text())
        self.assertEqual(6, plan["formatVersion"])
        self.assertEqual(5, sum(s["kind"] == "prediction" for s in plan["steps"]))
        self.assertEqual(
            8, sum(len(s["stops"]) for s in plan["steps"] if s["kind"] == "debug")
        )
        with tempfile.TemporaryDirectory() as temporary:
            output = bundle(root, Path(temporary) / "student.zip")
            with zipfile.ZipFile(output) as archive:
                names = set(archive.namelist())
                expected = {
                    manifest["id"] + "/" + n
                    for n in [
                        "package.json",
                        manifest["entry"],
                        manifest["assignment"],
                        manifest["nativeLesson"],
                        *manifest["supportingFiles"],
                    ]
                }
                self.assertEqual(expected, names)
                for step in plan["steps"]:
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
