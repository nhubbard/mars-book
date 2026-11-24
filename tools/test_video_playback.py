"""Playback attachment, controls and native steps preserve student working code."""

import importlib.util, json, re, subprocess, sys, tempfile, unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
D = ROOT / "drafts/video-playback-construction"


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


ATTACH = load("playback_attachment_tests", D / "attach.py")
PREP = load("playback_preparation_tests", D / "prepare.py")
EARLIER = load("playback_file_stage", ROOT / "tools/test_video_file_encode.py")


def source():
    return EARLIER.ATTACH.attach(EARLIER.source())


class VideoPlaybackTests(unittest.TestCase):
    def test_attachment_preserves_earlier_instructions(self):
        src = source()
        out = ATTACH.attach(src)
        self.assertTrue(
            out.endswith(
                src.split("\n.text\n")[1].replace(
                    "main:", "file_codec_example_main:", 1
                )
            )
        )
        self.assertEqual(1, out.count("\nmain:\n"))
        self.assertEqual(1, out.count("\nvideo_play:\n"))
        self.assertEqual(1, out.count("\nvideo_now:\n"))
        self.assertEqual(1, out.count("\nplayback_player_return:\n"))

    def test_repeated_attachment_conflicts_and_missing_entries_fail(self):
        src = source()
        for bad in (
            src + "\n.text\n",
            src.replace("video_apply_record:", "changed:"),
            src.replace("video_seek:", "changed:"),
            src + "\nvp_other:\n",
            src + "\nvn_other:\n",
            ATTACH.attach(src),
        ):
            with self.assertRaises(ValueError):
                ATTACH.attach(bad)

    def test_all_recipes_keep_instruction_text(self):
        src = ATTACH.attach(source())
        recipes = json.loads((D / "inputs.json").read_text())["recipes"]
        self.assertEqual(21, len(recipes))
        code = src.split("\n.text\n")[1]
        for r in recipes:
            out = PREP.prepare(src, r)
            self.assertEqual(code, out.split("\n.text\n")[1])
            self.assertIn(r["checkpoint"] + ":", out)

    def test_player_scratch_control_does_not_change_encoder_scratch(self):
        src = ATTACH.attach(source())
        out = PREP.prepare(src, dict(playbackScratchCapacity=17, start=6))
        self.assertIn("playback_context: .dword 16384, 0, 2048, 0, 17, 6", out)
        self.assertIn(
            "file_encode_context: .dword 7344152, 8, 4, 40, 0, 16384, 0, 16384", out
        )

    def test_controls_are_unsigned_and_require_context_markers(self):
        src = ATTACH.attach(source())
        for key in (
            "imageCapacity",
            "directoryCapacity",
            "playbackScratchCapacity",
            "start",
        ):
            for value in (-1, 2**64, True, "1", 1.5):
                with self.assertRaises(ValueError):
                    PREP.prepare(src, {key: value})
        self.assertIn(
            "playback_context: .dword 16384, 0, 2048, 0, 16384, -1",
            PREP.prepare(src, dict(start=2**64 - 1)),
        )
        for bad in (
            src.replace("playback_context:", "changed:"),
            src.replace("video_play:", "changed:"),
        ):
            with self.assertRaises(ValueError):
                PREP.prepare(bad, {})

    def test_native_steps_quizzes_and_teaching_order_agree(self):
        steps = json.loads((D / "steps.json").read_text())["steps"]
        self.assertEqual([f"s{n}" for n in range(102, 123)], [s["id"] for s in steps])
        self.assertEqual(
            set(re.findall(r"^# BEGIN (\w+)$", (D / "player.asm").read_text(), re.M)),
            {s["region"] for s in steps},
        )
        self.assertLess(
            [s["region"] for s in steps].index("vp_advance"),
            [s["region"] for s in steps].index("vp_window"),
        )
        for s in steps:
            t = (
                ROOT / f"includes/chapters/034-lossless-video/playback-{s['id']}.tex"
            ).read_text()
            self.assertIn(s["recipe"], t)
            self.assertIn(s["checkpoint"].replace("_", r"\_"), t)
            self.assertIn(s["quiz"]["question"], t)
            self.assertEqual(1, t.count(r"\paragraph{Observe}"))
            self.assertEqual(1, t.count(r"\paragraph{Check}"))
            self.assertEqual(2, len(s["hints"]))
            self.assertNotIn("answer", s["quiz"])
            self.assertNotIn("correctChoice", s["quiz"])
            for c in s["quiz"]["choices"]:
                self.assertIn(r"\item " + c["text"], t)

    def test_helpers_do_not_overwrite_existing_work(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td)
            src = p / "file.asm"
            src.write_text(source())
            full = p / "playback.asm"
            full.write_text(ATTACH.attach(source()))
            out = p / "existing"
            out.write_text("student work")
            for args in (
                ["attach.py", "--source", str(src), "--output", str(out)],
                ["prepare.py", "s102-1", "--source", str(full), "--output", str(out)],
            ):
                r = subprocess.run(
                    [sys.executable, str(D / args[0]), *args[1:]],
                    capture_output=True,
                    text=True,
                )
                self.assertNotEqual(0, r.returncode)
                self.assertEqual("student work", out.read_text())

    def test_native_contract_distinguishes_streaming_failure_from_final_publication(
        self,
    ):
        t = (
            ROOT / "includes/chapters/034-lossless-video/playback-contract.tex"
        ).read_text()
        self.assertIn("actual count", t)
        self.assertIn("Final context fields", t)
        self.assertIn("already changed display", t)
        self.assertIn("runUntilSyscall", t)
        self.assertIn("Actual annotated IDE", t)


if __name__ == "__main__":
    unittest.main()
