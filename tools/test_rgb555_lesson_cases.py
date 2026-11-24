from pathlib import Path
import json
import tempfile
import unittest

import rgb555_lesson_cases as rgb


class Rgb555LessonCasesTests(unittest.TestCase):
    def test_all_counts_keep_original_byte_offsets_and_distinct_initial_output(self):
        trials = rgb.trials()
        self.assertEqual(list(range(33)), [row["count"] for row in trials])
        self.assertTrue(any(row["destinationOffset"] % 2 for row in trials))
        for row in trials:
            count = row["count"]
            offset = (count * 11) % 32
            self.assertEqual(offset, row["offset"])
            self.assertEqual(
                (offset * 7 + count // 32 + 1) % 32, row["destinationOffset"]
            )
            self.assertEqual(128, len(row["input"]))
            self.assertEqual(96, len(row["output"]))
            for pixel in range(count):
                red, green, blue = row["input"][
                    offset + pixel * 3 : offset + pixel * 3 + 3
                ]
                word = ((red >> 3) << 10) | ((green >> 3) << 5) | (blue >> 3)
                self.assertLess(word, 32768)
                self.assertEqual(
                    [word & 255, word >> 8], row["expected"][pixel * 2 : pixel * 2 + 2]
                )
            for index, value in enumerate(row["expected"]):
                self.assertNotEqual(
                    value, row["output"][row["destinationOffset"] + index]
                )
        self.assertEqual([0xA8, 0x3C], rgb.packed_bytes(rgb.DEFAULT, 1, 0))
        self.assertEqual([0xFF, 0x7F], rgb.packed_bytes([255, 255, 255], 1, 0))

    def test_profiles_retain_full_width_state_selected_windows_and_complete_caller(
        self,
    ):
        profile = rgb.profile()
        self.assertEqual(8, profile["formatVersion"])
        self.assertEqual(34, len(profile["cases"]))
        self.assertEqual(67, sum(len(case["calls"]) for case in profile["cases"]))
        for row, case in zip(rgb.trials(), profile["cases"]):
            self.assertEqual(
                ["function_return", "pack555_done"],
                [call["returnTo"] for call in case["calls"]],
            )
            self.assertEqual(9, len(case["preservedRegisters"]))
            self.assertEqual(200000, case["instructionLimit"])
            self.assertEqual(
                row["offset"], case["calls"][0]["argumentAddresses"]["a0"]["offset"]
            )
            policy = case["accessPolicies"][0]
            self.assertEqual(
                [row["count"] * 3, row["count"] * 2],
                [window["length"] for window in policy["readable"]],
            )
            self.assertEqual({"label": "rgb_guard"}, policy["from"])
            self.assertEqual({"label": "completed", "offset": 4}, policy["until"])
            output = next(
                item["bytes"]
                for item in case["calls"][0]["memory"]
                if item["label"] == "packed"
            )
            self.assertEqual(
                bytes(row["expected"]),
                bytes.fromhex(output)[
                    row["destinationOffset"] : row["destinationOffset"]
                    + row["count"] * 2
                ],
            )
            self.assertNotIn("initializedOutput", case)
        caller = profile["cases"][-1]
        self.assertEqual("main", caller["calls"][0]["entry"])
        self.assertEqual("done", caller["calls"][0]["returnTo"])
        self.assertEqual(
            "08000000",
            next(
                item["bytes"]
                for item in caller["calls"][0]["memory"]
                if item["label"] == "completed"
            ),
        )
        self.assertEqual(
            ["packed", "completed"],
            [
                window["at"]["label"]
                for window in caller["accessPolicies"][0]["writable"]
            ],
        )

    def test_current_data_and_original_starter_script_are_preserved(self):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/019-working-with-msa/independent"
        rgb.build(unit, check=True)
        for suffix, directory in [("asm", "files"), ("kts", "checks")]:
            self.assertEqual(
                (root / f"revision/labs/{directory}/rgb555.{suffix}").read_bytes(),
                (unit / f"rgb555.{suffix}").read_bytes(),
            )
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary)
            rgb.build(path)
            rgb.build(path, check=True)
            self.assertEqual(
                rgb.profile(), json.loads((path / "rgb555.cases.json").read_text())
            )
            (path / "rgb555.cases.json").write_text("{}")
            with self.assertRaises(ValueError):
                rgb.build(path, check=True)


class MsaNativePackageTests(unittest.TestCase):
    def test_complete_sources_preserve_the_written_program_with_only_count_changes(
        self,
    ):
        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/019-working-with-msa"
        source = (unit / "rgb-formats.asm").read_text()
        self.assertEqual(
            (root / "revision/labs/files/rgb-formats.asm").read_text(), source
        )
        for count in [0, 7, 8, 9, 17]:
            self.assertEqual(
                source.replace("count: .word 32", f"count: .word {count}"),
                (unit / f"worked/count-{count}.asm").read_text(),
            )

    def test_archive_exports_all_six_routes_and_bounded_independent_profiles(self):
        from native_lesson import bundle
        import zipfile

        root = Path(__file__).resolve().parents[1]
        unit = root / "revision/lessons/019-working-with-msa"
        manifest = json.loads((unit / "package.json").read_text())
        lesson = json.loads((unit / "lesson.json").read_text())
        self.assertEqual(7, lesson["formatVersion"])
        self.assertEqual(
            9, sum(step["kind"] == "prediction" for step in lesson["steps"])
        )
        self.assertEqual(
            31,
            sum(
                len(step["stops"])
                for step in lesson["steps"]
                if step["kind"] == "debug"
            ),
        )
        self.assertEqual(6, sum(step["kind"] == "run" for step in lesson["steps"]))
        self.assertEqual(
            2, sum(step["kind"] == "assessment" for step in lesson["steps"])
        )
        with tempfile.TemporaryDirectory() as temporary:
            archive = bundle(unit, Path(temporary) / "student.zip")
            with zipfile.ZipFile(archive) as opened:
                expected = {
                    manifest["id"] + "/" + name
                    for name in [
                        "package.json",
                        manifest["entry"],
                        manifest["assignment"],
                        manifest["nativeLesson"],
                        *manifest["supportingFiles"],
                    ]
                }
                self.assertEqual(expected, set(opened.namelist()))
                for step in lesson["steps"]:
                    self.assertIn(
                        manifest["id"] + "/" + step.get("source", manifest["entry"]),
                        expected,
                    )
                    if step["kind"] == "assessment":
                        self.assertIn(
                            manifest["id"] + "/" + step["caseProfile"], expected
                        )
                self.assertFalse(
                    any(
                        "solution" in name or "reference" in name or "private" in name
                        for name in expected
                    )
                )
