from pathlib import Path
import json
import tempfile
import unittest
import zipfile

from console_cases import load, render
from native_lesson import bundle


class MazeLessonTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(__file__).resolve().parents[1]
        self.unit = self.root / "revision/lessons/010-maze-frontier"
        self.lesson = json.loads((self.unit / "lesson.json").read_text())

    def test_complete_geometry_experiments_lead_to_independent_renderer_and_search(
        self,
    ):
        source = (self.root / "revision/labs/files/maze-neighbor.asm").read_text()
        self.assertEqual(source, (self.unit / "worked/neighbor.asm").read_text())
        self.assertEqual(
            source.replace(
                "    li $a0, 7\n    li $a1, 1", "    li $a0, 9\n    li $a1, 0"
            ),
            (self.unit / "worked/north.asm").read_text(),
        )
        self.assertEqual(
            (self.root / "revision/labs/files/maze-search.asm").read_bytes(),
            (self.unit / "independent/maze.asm").read_bytes(),
        )
        steps = self.lesson["steps"]
        self.assertEqual(13, len(steps))
        self.assertEqual(13, len({step["id"] for step in steps}))
        self.assertEqual(7, sum(step["kind"] == "prediction" for step in steps))
        debug = [step for step in steps if step["kind"] == "debug"]
        self.assertEqual([3, 3], [len(step["stops"]) for step in debug])
        self.assertEqual(
            ["0xffffffffffffffff", "0x0000000000000001"],
            [step["stops"][-1]["registers"]["$v0"] for step in debug],
        )
        assessments = [step for step in steps if step["kind"] == "assessment"]
        self.assertEqual(
            ["check-render", "check-search"], [step["id"] for step in assessments]
        )
        self.assertEqual(
            [5, 8], [len(load(self.unit / step["caseProfile"])) for step in assessments]
        )
        self.assertEqual(
            {"independent/maze.asm"}, {step["source"] for step in assessments}
        )
        for step in assessments:
            profile = self.unit / step["caseProfile"]
            generated = profile.with_suffix("").with_suffix(".kts")
            self.assertEqual(render(load(profile)), generated.read_text())
            self.assertEqual(
                profile.read_bytes(),
                (self.root / "revision/labs/checks" / profile.name).read_bytes(),
            )

    def test_student_archive_has_explicit_public_exports_and_no_finished_search(self):
        with tempfile.TemporaryDirectory() as temporary:
            archive = bundle(self.unit, Path(temporary) / "student.zip")
            manifest = json.loads((self.unit / "package.json").read_text())
            with zipfile.ZipFile(archive) as package:
                expected = [
                    "package.json",
                    manifest["entry"],
                    manifest["assignment"],
                    manifest["nativeLesson"],
                    *manifest["supportingFiles"],
                ]
                self.assertEqual(
                    sorted("010-maze-frontier/" + name for name in expected),
                    package.namelist(),
                )
                self.assertEqual(12, len(package.namelist()))
                for name in expected:
                    self.assertEqual(
                        (self.unit / name).read_bytes(),
                        package.read("010-maze-frontier/" + name),
                    )
                starter = package.read(
                    "010-maze-frontier/independent/maze.asm"
                ).decode()
                self.assertEqual(
                    2, sum(line.strip() == "break" for line in starter.splitlines())
                )
                self.assertNotIn("reference", "\n".join(package.namelist()))


if __name__ == "__main__":
    unittest.main()
