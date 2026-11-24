"""Packed sources, staging aliases and native whole-file lessons retain their contracts."""

import importlib.util, json, re, subprocess, sys, tempfile, unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
D = ROOT / "drafts/video-file-encode-construction"


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


ATTACH = load("file_attach", D / "attach.py")
PREP = load("file_prepare", D / "prepare.py")
FIX = PREP.fixtures
EARLY = load("file_earlier", ROOT / "tools/test_video_delta_encode.py")


def source():
    return EARLY.ATTACH.attach(EARLY.source(), EARLY.kernel())


class VideoFileEncodeTests(unittest.TestCase):
    def test_alias_reuses_existing_file_arena_and_preserves_instructions(self):
        src = source()
        out = ATTACH.attach(src)
        self.assertEqual(1, out.count("file_encode_output:\nindex_file_data:"))
        self.assertEqual(1, out.count("\nmain:\n"))
        self.assertTrue(
            out.endswith(
                src.split("\n.text\n")[1].replace(
                    "main:", "record_encoder_example_main:", 1
                )
            )
        )
        self.assertEqual(src.count(".space 734"), out.count(".space 734"))

    def test_attachment_rejects_conflicts_and_missing_earlier_entries(self):
        src = source()
        for text in (
            src + "\n.text\n",
            src.replace("video_encode_record:", "changed:"),
            src.replace("index_file_data:", "changed:"),
            src + "\nfe_other:\n",
            src + "\nvs_other:\n",
            ATTACH.attach(src),
        ):
            with self.assertRaises(ValueError):
                ATTACH.attach(text)

    def test_controls_only_modify_data(self):
        src = ATTACH.attach(source())
        code = src.split("\n.text\n")[1]
        recipes = json.loads((D / "inputs.json").read_text())["recipes"]
        self.assertEqual(18, len(recipes))
        for r in recipes:
            out = PREP.prepare(src, r)
            self.assertEqual(code, out.split("\n.text\n")[1])
            self.assertIn(r["checkpoint"] + ":", out)
        self.assertIn(
            "file_encode_source_count: .dword 16384",
            PREP.prepare(src, dict(fixture="single")),
        )

    def test_controls_are_full_width_and_paths_are_bounded(self):
        src = ATTACH.attach(source())
        for key in (
            "sourceCount",
            "capacity",
            "frameCount",
            "period",
            "duration",
            "previousCapacity",
            "scratchCapacity",
            "target",
        ):
            for value in (-1, 2**64, True, "1", 1.0):
                with self.assertRaises(ValueError):
                    PREP.prepare(src, {key: value})
        self.assertIn(
            "file_encode_context: .dword -1,",
            PREP.prepare(src, dict(capacity=2**64 - 1)),
        )
        for path in ("x" * 128, "abc\0def", 3):
            with self.assertRaises(ValueError):
                PREP.prepare(src, dict(path=path))
        self.assertIn("    .space 0", PREP.prepare(src, dict(path="x" * 127)))
        for label in (
            "file_encode_frames_end",
            "file_encode_source_count",
            "file_encode_context",
            "file_encode_path_end",
        ):
            with self.assertRaises(ValueError):
                PREP.prepare(src.replace(label + ":", "changed:"), {})

    def test_generated_frames_have_exact_native_extent_and_last_frame_change(self):
        self.assertEqual(8, len(FIX.material()["frames"]))
        self.assertEqual(1, len(FIX.material("single")["frames"]))
        m = FIX.material("max-count")
        self.assertEqual(256, len(m["frames"]))
        self.assertEqual(64, m["period"])
        self.assertEqual(0x8000, m["frames"][0][-1] ^ m["frames"][-1][-1])
        self.assertEqual(m["frames"][0][:-1], m["frames"][-1][:-1])
        self.assertIn(".space 0", FIX.declaration(m["frames"]))
        for name in ("invalid-first", "invalid-middle", "invalid-last"):
            self.assertEqual(
                1, sum(v >= 2**24 for f in FIX.material(name)["frames"] for v in f)
            )
        with self.assertRaises(ValueError):
            FIX.material("unknown")

    def test_native_steps_match_regions_and_quizzes_without_keys(self):
        steps = json.loads((D / "steps.json").read_text())["steps"]
        self.assertEqual([f"s{n}" for n in range(84, 102)], [s["id"] for s in steps])
        regions = re.findall(
            r"^# BEGIN (\w+)$",
            "\n".join((D / n).read_text() for n in ("encoder.asm", "store.asm")),
            re.M,
        )
        self.assertEqual(regions, [s["region"] for s in steps])
        for s in steps:
            text = (
                ROOT / f"includes/chapters/034-lossless-video/file-encode-{s['id']}.tex"
            ).read_text()
            self.assertIn(s["recipe"], text)
            self.assertIn(s["checkpoint"].replace("_", r"\_"), text)
            self.assertIn(s["quiz"]["question"], text)
            self.assertEqual(1, text.count(r"\paragraph{Observe}"))
            self.assertEqual(1, text.count(r"\paragraph{Check}"))
            self.assertEqual(2, len(s["hints"]))
            self.assertNotIn("answer", s["quiz"])
            self.assertNotIn("correctChoice", s["quiz"])
            for c in s["quiz"]["choices"]:
                self.assertIn(r"\item " + c["text"], text)

    def test_existing_outputs_remain_untouched(self):
        with tempfile.TemporaryDirectory() as td:
            p = Path(td)
            src = p / "record.asm"
            src.write_text(source())
            full = p / "file.asm"
            full.write_text(ATTACH.attach(source()))
            out = p / "existing"
            out.write_text("student work")
            for args in (
                ["attach.py", "--source", str(src), "--output", str(out)],
                ["prepare.py", "s84-1", "--source", str(full), "--output", str(out)],
                ["fixtures.py", "single", "--output", str(out)],
            ):
                r = subprocess.run(
                    [sys.executable, str(D / args[0]), *args[1:]],
                    capture_output=True,
                    text=True,
                )
                self.assertNotEqual(0, r.returncode)
                self.assertEqual("student work", out.read_text())

    def test_numeric_fixture_export_contains_no_encoded_solution(self):
        with tempfile.TemporaryDirectory() as td:
            out = Path(td) / "source.json"
            r = subprocess.run(
                [
                    sys.executable,
                    str(D / "fixtures.py"),
                    "single",
                    "--output",
                    str(out),
                ],
                capture_output=True,
                text=True,
            )
            self.assertEqual(0, r.returncode, r.stderr)
            m = json.loads(out.read_text())
            self.assertEqual({"fixture", "frames", "period", "duration"}, set(m))
            self.assertEqual(4096, len(m["frames"][0]))


if __name__ == "__main__":
    unittest.main()
