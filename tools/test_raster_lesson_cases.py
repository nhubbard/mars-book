import itertools
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from native_lesson import bundle
import raster_lesson_cases as raster


class RasterLessonCasesTests(unittest.TestCase):
    def test_geometry_includes_endpoints_is_reversible_and_resolves_painted_halfway_cells(
        self,
    ):
        self.assertEqual(
            [(1, 1), (2, 2), (3, 2), (4, 3), (5, 3), (6, 4)],
            raster.line_cells(1, 1, 6, 4),
        )
        self.assertEqual((2, 1), raster.line_cells(0, 0, 4, 1)[2])
        self.assertEqual((1, 2), raster.line_cells(0, 0, 1, 4)[2])
        for ax, ay, bx, by in itertools.product(range(8), repeat=4):
            cells = raster.line_cells(ax, ay, bx, by)
            self.assertEqual(cells, raster.line_cells(bx, by, ax, ay))
            self.assertEqual(max(abs(bx - ax), abs(by - ay)) + 1, len(cells))
            self.assertEqual(len(cells), len(set(cells)))
            self.assertEqual(min((ax, ay), (bx, by)), cells[0])
            self.assertEqual(max((ax, ay), (bx, by)), cells[-1])
            self.assertTrue(all(0 <= x < 8 and 0 <= y < 8 for x, y in cells))
            self.assertTrue(
                all(
                    max(abs(x - u), abs(y - v)) == 1
                    for (x, y), (u, v) in zip(cells, cells[1:])
                )
            )

    def test_public_cases_protect_inputs_and_preserve_unpainted_prefilled_cells(self):
        profile = raster.profile()
        self.assertEqual(3, profile["formatVersion"])
        self.assertEqual(16, len(profile["cases"]))
        for endpoints, case in zip(raster.ENDPOINTS, profile["cases"]):
            self.assertEqual(10000, case["instructionLimit"])
            self.assertEqual(10, len(case["preservedRegisters"]))
            self.assertEqual(32, case["callerStackBytes"])
            self.assertEqual(16, case["stackAlignment"])
            self.assertEqual(
                {"endpoints": 16, "color": 4, **dict.fromkeys(raster.GUARDS, 4)},
                {r["label"]: r["length"] for r in case["noStores"]},
            )
            initial = {
                r["label"]: bytes.fromhex(r["bytes"]) for r in case["initialMemory"]
            }
            selected = set(raster.line_cells(*endpoints)[::2])
            self.assertEqual(
                ["line_complete", "line_repeat"], [c["returnTo"] for c in case["calls"]]
            )
            for call in case["calls"]:
                rgb = int(call["arguments"]["a2"], 16)
                self.assertNotEqual(rgb, int.from_bytes(initial["color"], "little"))
                self.assertEqual(
                    len(selected), int(call["expectedRegisters"]["v0"], 16)
                )
                self.assertEqual(
                    {"a0": {"label": "endpoints"}, "a1": {"label": "frame"}},
                    call["argumentAddresses"],
                )
                expected = {
                    r["label"]: bytes.fromhex(r["bytes"]) for r in call["memory"]
                }
                self.assertEqual(256, len(expected["frame"]))
                for y, x in itertools.product(range(8), repeat=2):
                    at = 4 * (8 * y + x)
                    self.assertEqual(
                        (
                            rgb.to_bytes(4, "little")
                            if (x, y) in selected
                            else initial["frame"][at : at + 4]
                        ),
                        expected["frame"][at : at + 4],
                    )
                for label in initial.keys() - {"frame"}:
                    self.assertEqual(initial[label], expected[label])
        self.assertIn("protected.forEach { it.expectNone() }", raster.script())

    def test_sources_exports_and_archive_keep_activity_dependencies_without_solutions(
        self,
    ):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/007-integer-raster"
        self.assertEqual(
            (root / "revision/labs/files/integer-line.asm").read_bytes(),
            (unit / "integer-line.asm").read_bytes(),
        )
        starter = (unit / "independent/dashed-line.asm").read_text()
        self.assertIn("draw_dashed:\n    break", starter)
        self.assertEqual(2, starter.count("balc draw_dashed"))
        raster.build(unit / "independent", check=True)
        lesson = json.loads((unit / "lesson.json").read_text())
        manifest = json.loads((unit / "package.json").read_text())
        self.assertEqual(6, lesson["formatVersion"])
        self.assertEqual(5, sum(s["kind"] == "prediction" for s in lesson["steps"]))
        self.assertEqual(
            8, sum(len(s["stops"]) for s in lesson["steps"] if s["kind"] == "debug")
        )
        self.assertEqual(1, sum(s["kind"] == "run" for s in lesson["steps"]))
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            raster.build(directory)
            self.assertEqual(
                raster.profile(),
                json.loads((directory / "dashed-line.cases.json").read_text()),
            )
            (directory / "dashed-line.kts").write_text("stale")
            with self.assertRaises(ValueError):
                raster.build(directory, check=True)
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
                        "reference" in n or "alternative" in n or "solution" in n
                        for n in names
                    )
                )
