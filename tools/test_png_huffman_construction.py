"""Huffman-stage attachment, deterministic inputs and native composition links."""

import importlib.util, json, re, struct, subprocess, sys, tempfile, unittest, zlib
from pathlib import Path
import book


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


class HuffmanConstructionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root = book.ROOT / "drafts/png-huffman-construction"
        cls.attach = load("huff_attach", cls.root / "attach.py")
        cls.prep = load("huff_prepare", cls.root / "prepare.py")
        cls.fixture = load("huff_fixture", cls.root / "fixtures.py")
        a = load(
            "collector_attach_huff",
            book.ROOT / "drafts/png-collector-construction/attach.py",
        )
        b = load(
            "stored_attach_huff",
            book.ROOT / "drafts/png-inflater-construction/attach.py",
        )
        cls.previous = b.attach(
            a.attach((book.ROOT / "drafts/png-construction/main.asm").read_text())
        )
        cls.recipes = json.loads((cls.root / "inputs.json").read_text())["recipes"]

    def test_attachment_keeps_all_earlier_edits_and_extra_functions(self):
        original = (
            self.previous.replace("png_load:\n", "png_load:\n    # own loader\n")
            .replace("png_collect:\n", "png_collect:\n    # own collector\n")
            .replace("png_inflate:\n", "png_inflate:\n    # own stored code\n")
            + "\nextra_student_function:\n    jr $ra\n    nop\n"
        )
        result = self.attach.attach(original)
        self.assertEqual(27, result.count("# BEGIN huff_"))
        for text in (
            "# own loader",
            "# own collector",
            "# own stored code",
            "extra_student_function:",
        ):
            self.assertEqual(1, result.count(text))
        anchor = self.attach.ANCHOR
        prefix, suffix = original.split(anchor)
        self.assertIn(prefix.split("\n.text\n")[1], result)
        self.assertIn("inflate_missing:\n" + suffix, result)

    def test_attachment_refuses_conflicts_and_changed_boundaries(self):
        for source in (
            self.previous.replace("dynamic_block:", "changed_dynamic:"),
            self.previous + "\n.text\n",
            self.previous + "\ndecode_symbol:\n",
            self.attach.attach(self.previous),
        ):
            with self.assertRaises(ValueError):
                self.attach.attach(source)

    def test_read_only_format_table_boundaries(self):
        text = (self.root / "tables.asm").read_text()

        def values(label):
            return [
                int(x.strip())
                for x in re.search(
                    r"^" + label + r": \.(?:byte|half) ([^\n]+)", text, re.M
                )[1].split(",")
            ]

        self.assertEqual(
            [16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15],
            values("alphabet_order"),
        )
        base = values("length_base")
        extra = values("length_extra")
        self.assertEqual(29, len(base))
        self.assertEqual(list(range(3, 11)), base[:8])
        self.assertEqual((31, 2), (base[15], extra[15]))
        self.assertEqual((258, 0), (base[-1], extra[-1]))
        self.assertEqual(30, len(values("distance_base")))
        self.assertEqual([1, 2, 3, 4], values("distance_base")[:4])
        self.assertEqual(
            (24577, 13), (values("distance_base")[-1], values("distance_extra")[-1])
        )

    def test_valid_fixed_dynamic_and_repeat_inputs_close_exactly(self):
        for name in (
            "fixed-literals",
            "fixed-match",
            "dynamic-literals",
            "repeat16",
            "repeat17",
            "repeat18",
            "mixed",
        ):
            m = self.fixture.material(name)
            obj = zlib.decompressobj()
            raw = obj.decompress(m["zlib"]) + obj.flush()
            self.assertTrue(obj.eof)
            self.assertFalse(obj.unused_data)
            self.assertEqual(m["plain"], raw)
            self.assertEqual(35, len(raw))
        for name in ("repeat16", "repeat17", "repeat18"):
            self.assertEqual(
                self.fixture.material("dynamic-literals")["plain"],
                self.fixture.material(name)["plain"],
            )
        self.assertEqual(10, len(self.fixture.material("fixed-match")["zlib"]))
        self.assertEqual(53, len(self.fixture.material("dynamic-literals")["zlib"]))
        self.assertEqual(37, len(self.fixture.material("repeat18")["zlib"]))
        mixed = self.fixture.material("mixed")["zlib"]
        self.assertEqual(0, mixed[2] & 1)
        self.assertEqual(3, mixed[14] & 7)

    def test_invalid_streams_still_have_valid_container_crcs(self):
        for name in self.fixture.NAMES:
            m = self.fixture.material(name)
            raw = m["png"]
            at = 8
            joined = bytearray()
            while at < len(raw):
                n = struct.unpack_from(">I", raw, at)[0]
                kind = raw[at + 4 : at + 8]
                body = raw[at + 8 : at + 8 + n]
                crc = struct.unpack_from(">I", raw, at + 8 + n)[0]
                self.assertEqual(zlib.crc32(kind + body), crc)
                if kind == b"IDAT":
                    joined.extend(body)
                at += n + 12
            self.assertEqual(len(raw), at)
            self.assertEqual(m["zlib"], bytes(joined))
            if name.startswith(("bad-", "reserved-", "missing-")):
                with self.assertRaises(zlib.error):
                    zlib.decompress(m["zlib"])
        with self.assertRaises(ValueError):
            self.fixture.material("unknown")

    def test_every_recipe_preserves_current_instructions_and_indexes_native_steps(self):
        source = self.attach.attach(self.previous).replace(
            "decode_symbol:\n", "decode_symbol:\n    # own symbol reader\n"
        )
        steps = json.loads((self.root / "steps.json").read_text())["steps"]
        self.assertEqual(list(range(44, 71)), [int(s["id"][1:]) for s in steps])
        self.assertEqual(35, len(self.recipes))
        self.assertEqual(27, len(set(s["region"] for s in steps)))
        for r in self.recipes:
            self.assertEqual(
                source.split("\n.text\n")[1],
                self.prep.prepare(source, r).split("\n.text\n")[1],
            )
        for prefix, s in enumerate(steps, 1):
            self.assertEqual(1, source.count("# BEGIN " + s["region"] + "\n"))
            self.assertEqual(
                prefix,
                next(r for r in self.recipes if r["id"] == s["recipes"][0])["prefix"],
            )
            self.assertEqual(3, len(s["quiz"]["choices"]))
            self.assertEqual(2, len(s["hints"]))
            self.assertTrue(
                (
                    book.ROOT
                    / "includes/chapters/033-png"
                    / ("huff-" + s["id"] + ".tex")
                ).is_file()
            )
        with self.assertRaises(ValueError):
            self.prep.prepare(self.previous, self.recipes[0])

    def test_commands_refuse_existing_outputs(self):
        with tempfile.TemporaryDirectory() as folder:
            d = Path(folder)
            source = d / "previous.asm"
            source.write_text(self.previous)
            target = d / "next.asm"
            command = [
                sys.executable,
                str(self.root / "attach.py"),
                "--source",
                str(source),
                "--output",
                str(target),
            ]
            self.assertEqual(0, subprocess.run(command, capture_output=True).returncode)
            before = target.read_bytes()
            self.assertNotEqual(
                0, subprocess.run(command, capture_output=True).returncode
            )
            self.assertEqual(before, target.read_bytes())
            target = d / "repeat18.png"
            command = [
                sys.executable,
                str(self.root / "fixtures.py"),
                "repeat18",
                str(target),
            ]
            self.assertEqual(0, subprocess.run(command, capture_output=True).returncode)
            before = target.read_bytes()
            self.assertNotEqual(
                0, subprocess.run(command, capture_output=True).returncode
            )
            self.assertEqual(before, target.read_bytes())
