"""Loader authoring controls preserve code and distinguish physical files from injected I/O."""

import importlib.util, json, re, subprocess, sys, tempfile, unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
D = ROOT / "drafts/video-io-construction"


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


ATTACH = load("io_test_attach", D / "attach.py")
PREP = load("io_test_prepare", D / "prepare.py")
FIX = load("io_test_fixtures", D / "fixtures.py")
INDEX = load("io_index_attach", ROOT / "drafts/video-index-construction/attach.py")


def source():
    return (
        INDEX.attach((ROOT / "drafts/video-record-construction/main.asm").read_text())
        + "\nstudent_extra:\n    daddiu $t0, $t0, 67\n"
    )


class VideoIoTests(unittest.TestCase):
    def test_attachment_keeps_all_index_and_record_instructions(self):
        src = source()
        result = ATTACH.attach(src)
        self.assertTrue(
            result.endswith(
                src.split("\n.text\n")[1].replace("main:", "index_example_main:", 1)
            )
        )
        self.assertEqual(1, result.count("\nvideo_load:\n"))
        self.assertEqual(1, result.count("\nvideo_index:\n"))
        self.assertEqual(1, result.count("\nmain:\n"))

    def test_conflicts_and_ambiguous_entries_are_refused(self):
        for src in (
            source() + "\n.text\n",
            source().replace("video_index:", "changed:"),
            source().replace("index_file_data:", "changed:"),
            source() + "\nio_other:\n",
            source() + "\nindex_example_main:\n",
            ATTACH.attach(source()),
        ):
            with self.assertRaises(ValueError):
                ATTACH.attach(src)

    def test_recipes_change_only_data(self):
        src = ATTACH.attach(source())
        recipes = json.loads((D / "inputs.json").read_text())["recipes"]
        self.assertEqual(23, len(recipes))
        code = src.split("\n.text\n")[1]
        for r in recipes:
            result = PREP.prepare(src, r)
            self.assertEqual(code, result.split("\n.text\n")[1])
            self.assertIn(r["checkpoint"] + ":", result)
            self.assertIn("io_load_context: .dword ", result)

    def test_path_limits_unsigned_capacity_and_missing_anchors(self):
        src = ATTACH.attach(source())
        for cap in (-1, 2**64, True, "1", 1.5):
            with self.assertRaises(ValueError):
                PREP.prepare(src, dict(capacity=cap))
        for path in (1, "a\0b", "a" * 128, "é" * 64):
            with self.assertRaises(ValueError):
                PREP.prepare(src, dict(capacity=65536, path=path))
        for text in (
            src.replace("io_path_end:", "changed:"),
            src.replace("io_capacity:", "changed:"),
            src + "\n.text\n",
        ):
            with self.assertRaises(ValueError):
                PREP.prepare(text, dict(capacity=65536))
        result = PREP.prepare(src, dict(capacity=65536, path="é" * 63))
        self.assertIn("io_path: .byte -61, -87", result)

    def test_fixtures_are_actual_files_and_keep_existing_outputs(self):
        self.assertEqual(b"", FIX.material("empty"))
        self.assertEqual(24795, len(FIX.material("moving")))
        with tempfile.TemporaryDirectory() as td:
            out = Path(td) / "animation.mlv"
            r = subprocess.run(
                [
                    sys.executable,
                    str(D / "fixtures.py"),
                    "moving",
                    "--output",
                    str(out),
                ],
                capture_output=True,
                text=True,
            )
            self.assertEqual(0, r.returncode, r.stderr)
            self.assertEqual(FIX.material("moving"), out.read_bytes())
            r = subprocess.run(
                [sys.executable, str(D / "fixtures.py"), "empty", "--output", str(out)],
                capture_output=True,
                text=True,
            )
            self.assertNotEqual(0, r.returncode)
            self.assertEqual(FIX.material("moving"), out.read_bytes())

    def test_native_steps_have_explanations_and_private_free_quizzes(self):
        steps = json.loads((D / "steps.json").read_text())["steps"]
        self.assertEqual([f"s{n}" for n in range(37, 46)], [s["id"] for s in steps])
        self.assertEqual(
            [s["region"] for s in steps],
            re.findall(r"^# BEGIN (\w+)$", (D / "loader.asm").read_text(), re.M),
        )
        for s in steps:
            text = (
                ROOT / f"includes/chapters/034-lossless-video/io-{s['id']}.tex"
            ).read_text()
            self.assertIn(s["recipe"], text)
            self.assertIn(s["checkpoint"].replace("_", r"\_"), text)
            self.assertIn(s["quiz"]["question"], text)
            self.assertEqual(1, text.count(r"\paragraph{Observe}"))
            self.assertEqual(1, text.count(r"\paragraph{Check}"))
            self.assertEqual(3, len(s["quiz"]["choices"]))
            self.assertEqual(2, len(s["hints"]))
            self.assertNotIn("answer", s["quiz"])

    def test_attach_and_prepare_refuse_existing_outputs(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td)
            src = p / "index.asm"
            src.write_text(source())
            full = p / "reader.asm"
            full.write_text(ATTACH.attach(source()))
            out = p / "existing"
            out.write_text("student work")
            for args in (
                ["attach.py", "--source", str(src), "--output", str(out)],
                ["prepare.py", "s37-1", "--source", str(full), "--output", str(out)],
            ):
                r = subprocess.run(
                    [sys.executable, str(D / args[0]), *args[1:]],
                    capture_output=True,
                    text=True,
                )
                self.assertNotEqual(0, r.returncode)
                self.assertEqual("student work", out.read_text())


if __name__ == "__main__":
    unittest.main()
