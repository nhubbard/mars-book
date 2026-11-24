import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from console_cases import parse, render
from marsbot_lesson_cases import profile
from native_lesson import bundle

ROOT = Path(__file__).resolve().parents[1]
UNIT = ROOT / "revision/lessons/006-marsbot-feedback"


class MarsBotNativeUnitTest(unittest.TestCase):
    def test_worked_sources_and_independent_starter_preserve_the_written_contract(self):
        east = (ROOT / "revision/labs/files/marsbot-east.asm").read_bytes()
        self.assertEqual(east, (UNIT / "worked/east.asm").read_bytes())
        self.assertEqual(
            east.decode()
            .replace("instructionsPerTick=64", "instructionsPerTick=128")
            .replace("side: .word 16", "side: .word 2"),
            (UNIT / "worked/east-slow.asm").read_text(),
        )
        self.assertEqual(
            (ROOT / "revision/labs/files/marsbot-square.asm").read_bytes(),
            (UNIT / "independent/square.asm").read_bytes(),
        )
        self.assertEqual(
            1,
            (UNIT / "independent/square.asm")
            .read_text()
            .split("main:")[1]
            .count("    break"),
        )
        for folder, square in [("worked", False), ("independent", True)]:
            name = "square" if square else "east"
            raw = (UNIT / f"{folder}/{name}.cases.json").read_bytes()
            self.assertEqual(profile(square), json.loads(raw))
            self.assertEqual(
                render(parse(raw)), (UNIT / f"{folder}/{name}.kts").read_text()
            )

    def test_guidance_separates_prediction_source_boundaries_transcript_and_actual_device_checks(
        self,
    ):
        lesson = json.loads((UNIT / "lesson.json").read_text())
        steps = lesson["steps"]
        self.assertEqual(11, lesson["formatVersion"])
        self.assertEqual(12, len(steps))
        self.assertEqual(7, len([s for s in steps if s["kind"] == "prediction"]))
        debug = [s for s in steps if s["kind"] == "debug"]
        self.assertEqual(
            ["worked/east.asm", "worked/east-slow.asm"], [s["source"] for s in debug]
        )
        for activity, side in zip(debug, [16, 2]):
            self.assertEqual(
                ["east_stop", "east_stopped", "done"],
                [s["anchor"]["label"] for s in activity["stops"]],
            )
            self.assertTrue(
                all(
                    int(s["registers"]["$s1"], 16)
                    == int(s["registers"]["$t2"], 16)
                    == side
                    for s in activity["stops"]
                )
            )
        self.assertEqual("", next(s for s in steps if s["kind"] == "run")["stdout"])
        assessments = [s for s in steps if s["kind"] == "assessment"]
        self.assertEqual(
            ["worked/east.asm", "independent/square.asm"],
            [s["source"] for s in assessments],
        )
        self.assertEqual(
            [16, 3],
            [len(parse((UNIT / s["caseProfile"]).read_bytes())) for s in assessments],
        )
        self.assertEqual(3, len(assessments[-1]["hints"]))

    def test_explicit_archive_is_reproducible_and_has_no_implementation_for_the_independent_task(
        self,
    ):
        manifest = json.loads((UNIT / "package.json").read_text())
        names = {
            "package.json",
            manifest["entry"],
            manifest["assignment"],
            manifest["nativeLesson"],
            *manifest["supportingFiles"],
        }
        actual = {str(p.relative_to(UNIT)) for p in UNIT.rglob("*") if p.is_file()}
        self.assertEqual(names, actual)
        self.assertEqual(11, len(names))
        with tempfile.TemporaryDirectory() as directory:
            a = Path(directory) / "a.zip"
            b = Path(directory) / "b.zip"
            bundle(UNIT, a)
            bundle(UNIT, b)
            self.assertEqual(a.read_bytes(), b.read_bytes())
            with zipfile.ZipFile(a) as archive:
                self.assertEqual(
                    {"006-marsbot-feedback/" + name for name in names},
                    set(archive.namelist()),
                )
                for name in names:
                    self.assertEqual(
                        (UNIT / name).read_bytes(),
                        archive.read("006-marsbot-feedback/" + name),
                    )
                self.assertIn(
                    b"    break",
                    archive.read("006-marsbot-feedback/independent/square.asm"),
                )
                self.assertTrue(
                    all(
                        "reference" not in name and "private" not in name
                        for name in archive.namelist()
                    )
                )


if __name__ == "__main__":
    unittest.main()
