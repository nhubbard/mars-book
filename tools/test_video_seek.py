"""Seek construction controls preserve code, public boundaries and native reading."""

import importlib.util, json, re, subprocess, sys, tempfile, unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
D = ROOT / "drafts/video-seek-construction"


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


ATTACH = load("seek_test_attach", D / "attach.py")
PREP = load("seek_test_prepare", D / "prepare.py")
INDEX = load("seek_index_attach", ROOT / "drafts/video-index-construction/attach.py")
IO = load("seek_io_attach", ROOT / "drafts/video-io-construction/attach.py")


def source():
    return (
        IO.attach(
            INDEX.attach(
                (ROOT / "drafts/video-record-construction/main.asm").read_text()
            )
        )
        + "\nstudent_extra:\n    daddiu $t0, $t0, 73\n"
    )


class VideoSeekTests(unittest.TestCase):
    def test_attachment_keeps_reader_and_record_instructions(self):
        src = source()
        result = ATTACH.attach(src)
        self.assertTrue(
            result.endswith(
                src.split("\n.text\n")[1].replace("main:", "reader_example_main:", 1)
            )
        )
        self.assertEqual(1, result.count("\nvideo_seek:\n"))
        self.assertEqual(1, result.count("\nmain:\n"))
        self.assertEqual(1, result.count("\nvideo_load:\n"))

    def test_ambiguous_entries_and_conflicting_names_fail(self):
        for src in (
            source() + "\n.text\n",
            source().replace("video_load:", "changed:"),
            source().replace("video_index:", "changed:"),
            source() + "\nseek_other:\n",
            source() + "\nreader_example_main:\n",
            ATTACH.attach(source()),
        ):
            with self.assertRaises(ValueError):
                ATTACH.attach(src)

    def test_every_recipe_preserves_instructions_and_names_real_checkpoint(self):
        src = ATTACH.attach(source())
        recipes = json.loads((D / "inputs.json").read_text())["recipes"]
        self.assertEqual(20, len(recipes))
        code = src.split("\n.text\n")[1]
        for recipe in recipes:
            result = PREP.prepare(src, recipe)
            self.assertEqual(code, result.split("\n.text\n")[1])
            self.assertIn(recipe["checkpoint"] + ":", result)
            target = recipe.get("target", 6)
            self.assertIn(f"seek_target: .dword {target}", result)

    def test_full_width_controls_and_invalid_types(self):
        src = ATTACH.attach(source())
        result = PREP.prepare(src, dict(target=2**64 - 1))
        self.assertIn("seek_target: .dword -1", result)
        for key in ("target", "frameCapacity", "scratchCapacity"):
            for value in (-1, 2**64, True, "1", 1.5):
                with self.assertRaises(ValueError):
                    PREP.prepare(src, {key: value})
        for text in (
            src.replace("seek_target:", "changed:"),
            src.replace("seek_context:", "changed:"),
            src + "\n.text\n",
        ):
            with self.assertRaises(ValueError):
                PREP.prepare(text, {})

    def test_native_steps_have_explanation_observation_and_answer_free_quizzes(self):
        steps = json.loads((D / "steps.json").read_text())["steps"]
        self.assertEqual([f"s{n}" for n in range(46, 57)], [s["id"] for s in steps])
        self.assertEqual(
            [s["region"] for s in steps],
            re.findall(r"^# BEGIN (\w+)$", (D / "seek.asm").read_text(), re.M),
        )
        for s in steps:
            text = (
                ROOT / f"includes/chapters/034-lossless-video/seek-{s['id']}.tex"
            ).read_text()
            self.assertIn(s["recipe"], text)
            self.assertIn(s["checkpoint"].replace("_", r"\_"), text)
            self.assertIn(s["quiz"]["question"], text)
            self.assertEqual(1, text.count(r"\paragraph{Observe}"))
            self.assertEqual(1, text.count(r"\paragraph{Check}"))
            self.assertEqual(3, len(s["quiz"]["choices"]))
            self.assertEqual(2, len(s["hints"]))
            self.assertNotIn("answer", s["quiz"])
            self.assertNotIn("correctChoice", s["quiz"])
            for c in s["quiz"]["choices"]:
                self.assertIn(r"\item " + c["text"], text)

    def test_attach_and_prepare_protect_existing_outputs(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td)
            src = p / "reader.asm"
            src.write_text(source())
            full = p / "seeker.asm"
            full.write_text(ATTACH.attach(source()))
            out = p / "existing"
            out.write_text("student work")
            for args in (
                ["attach.py", "--source", str(src), "--output", str(out)],
                ["prepare.py", "s46-1", "--source", str(full), "--output", str(out)],
            ):
                r = subprocess.run(
                    [sys.executable, str(D / args[0]), *args[1:]],
                    capture_output=True,
                    text=True,
                )
                self.assertNotEqual(0, r.returncode)
                self.assertEqual("student work", out.read_text())

    def test_contract_states_nearest_full_and_failure_ownership(self):
        text = (
            ROOT / "includes/chapters/034-lossless-video/seek-contract.tex"
        ).read_text()
        self.assertIn("latest full frame", text)
        self.assertIn("96-byte", text)
        self.assertIn("Scratch contents are unspecified", text)
        self.assertIn("without storing", text)
        self.assertIn("Keep both unchanged", text)


if __name__ == "__main__":
    unittest.main()
