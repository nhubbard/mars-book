"""Packing controls preserve student work and describe exact executable observations."""

import importlib.util
import json
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest
import zlib

ROOT = Path(__file__).resolve().parents[1]
D = ROOT / "drafts/png-packing-construction"


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


ATTACH = load("test_pack_attach", D / "attach.py")
PREP = load("test_pack_prep", D / "prepare.py")
FIX = load("test_pack_fixture", D / "fixtures.py")


def source():
    return (
        '.eqv FRAME_STRIDE 512\n.data\nimage_path: .asciiz "image.png"\n    .space 118\nimage_path_end:\nvector_mode: .word 1\nfile_capacity: .dword 1048576\ncollector_capacity: .dword 46\n.text\n'
        + (ROOT / "drafts/png-row-construction/wrapper.asm").read_text()
        + "png_unfilter:\n    nop\npng_unfilter_msa:\n    nop\nstudent_extra:\n    daddiu $t0, $t0, 23\n"
    )


class PackingConstructionTests(unittest.TestCase):
    def test_attachment_preserves_all_student_functions(self):
        old = source()
        text = ATTACH.attach(old)
        self.assertIn(old[old.index("png_unfilter:\n") :], text)
        self.assertEqual(1, text.count("png_decode_msa:"))
        self.assertEqual(1, text.count("rgb_shuffle:"))
        self.assertIn("jal png_blit_msa", text)
        self.assertIn("# BEGIN publish_results", text)

    def test_conflicting_or_changed_attachments_are_refused(self):
        for old in (
            source().replace("png_unfilter_msa:", "changed:"),
            source().replace("row_scalar_call:", "changed:"),
            source() + "rgb_shuffle:\n",
            source() + "png_blit:\n",
            source() + "publication_entry:\n",
            source() + "\n.text\n",
        ):
            with self.subTest(old=old[-40:]), self.assertRaises(ValueError):
                ATTACH.attach(old)
        with self.assertRaises(ValueError):
            ATTACH.attach(ATTACH.attach(source()))

    def test_observation_controls_keep_every_instruction(self):
        src = ATTACH.attach(source())
        code = src.split("\n.text\n")[1]
        recipes = json.loads((D / "inputs.json").read_text())["recipes"]
        self.assertEqual(28, len(recipes))
        for r in recipes:
            text = PREP.prepare(src, r)
            self.assertEqual(code, text.split("\n.text\n")[1])
            self.assertIn(
                ".byte " + ",".join(str(v) for v in r["path"].encode() + b"\0"), text
            )
            self.assertIn(r["checkpoint"] + ":", text)
        with self.assertRaises(ValueError):
            PREP.prepare(source(), recipes[0])

    def test_compact_stride_changes_only_the_provided_constant(self):
        src = ATTACH.attach(source())
        recipes = json.loads((D / "inputs.json").read_text())["recipes"]
        recipe = next(r for r in recipes if r["id"] == "s100-display")
        base = PREP.prepare(src, dict(recipe, **{"frameStride": "512"}))
        compact = PREP.prepare(src, recipe)
        self.assertEqual(
            base.replace(".eqv FRAME_STRIDE 512", ".eqv FRAME_STRIDE 2"), compact
        )
        self.assertEqual(src.split("\n.text\n")[1], compact.split("\n.text\n")[1])
        for stride in ("0", "513", "01", "-1", 2):
            with self.subTest(stride=stride), self.assertRaises(ValueError):
                PREP.prepare(src, dict(recipe, frameStride=stride))
        for broken in (
            src.replace(".eqv FRAME_STRIDE 512", ""),
            src.replace(
                ".eqv FRAME_STRIDE 512", ".eqv FRAME_STRIDE 512\n.eqv FRAME_STRIDE 512"
            ),
        ):
            with self.assertRaises(ValueError):
                PREP.prepare(broken, recipe)

    def test_fixtures_have_exact_rgb_geometry_and_valid_wrappers(self):
        for name in FIX.NAMES:
            m = FIX.material(name)
            self.assertEqual(3 * m["width"] * m["height"], len(m["rgb"]))
            self.assertEqual((1 + 3 * m["width"]) * m["height"], len(m["filtered"]))
            data = m["png"]
            at = 8
            joined = b""
            kinds = []
            while at < len(data):
                n = int.from_bytes(data[at : at + 4], "big")
                kind = data[at + 4 : at + 8]
                raw = data[at + 8 : at + 8 + n]
                crc = int.from_bytes(data[at + 8 + n : at + 12 + n], "big")
                self.assertEqual(zlib.crc32(kind + raw), crc)
                kinds.append(kind)
                if kind == b"IDAT":
                    joined += raw
                at += 12 + n
            self.assertEqual(at, len(data))
            self.assertEqual([b"IHDR", b"IDAT", b"IDAT", b"IEND"], kinds)
            self.assertEqual(m["filtered"], zlib.decompress(joined))
            if name == "bad-selector":
                self.assertEqual(5, m["filtered"][28])
            elif name.startswith("width"):
                self.assertEqual(int(name[5:]), m["width"])
                self.assertEqual(bytes.fromhex("0061c2"), m["rgb"][:3])

    def test_native_steps_distinguish_edits_and_diagnostics(self):
        items = json.loads((D / "steps.json").read_text())["steps"]
        self.assertEqual([f"s{n}" for n in range(84, 101)], [s["id"] for s in items])
        text = "\n".join(
            (D / n).read_text() for n in ("packing.asm", "masks.asm", "wrapper.asm")
        )
        regions = re.findall(r"^# BEGIN (\w+)$", text, re.M)
        self.assertEqual(15, len(regions))
        self.assertEqual(
            set(regions), {s["region"] for s in items if s["kind"] == "edit"}
        )
        self.assertEqual(
            ["s98", "s99"], [s["id"] for s in items if s["kind"] == "observe"]
        )
        for s in items:
            native = (
                ROOT / f"includes/chapters/033-png/pack-{s['id']}.tex"
            ).read_text()
            self.assertIn(s["recipe"], native)
            self.assertIn(s["checkpoint"].replace("_", r"\_"), native)
            self.assertEqual(3, len(s["quiz"]["choices"]))
            self.assertEqual(2, len(s["hints"]))
            self.assertNotIn("answer", s["quiz"])

    def test_blank_masks_keep_their_fixed_storage_extent(self):
        text = (D / "masks.asm").read_text()
        self.assertIn("rgb_shuffle: .byte 0:64", text)
        self.assertEqual(1, text.count("# BEGIN pack_masks"))
        self.assertIn("pack_masks_ready:", (D / "packing.asm").read_text())

    def test_cli_refuses_existing_outputs(self):
        with tempfile.TemporaryDirectory() as directory:
            p = Path(directory)
            src = p / "source.asm"
            src.write_text(source())
            out = p / "existing"
            out.write_text("student work")
            for args in (
                ["attach.py", "--source", str(src), "--output", str(out)],
                ["prepare.py", "s84-1", "--source", str(src), "--output", str(out)],
                ["fixtures.py", "worked", "--output", str(out)],
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
