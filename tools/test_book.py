from pathlib import Path
import hashlib
import importlib.util
import subprocess
import sys
import json
import re
import struct
import wave
import zlib
import tempfile
import unittest
from unittest.mock import patch

import book


class NativeCompositionTests(unittest.TestCase):
    def test_actual_native_composition_and_highlight_integrity(self):
        self.assertEqual(796, len(book.validate()))
        self.assertEqual(1, len(book.validate("specimen.tex")))
        self.assertEqual(1, len(book.validate("revision/advanced-pilots.tex")))
        self.assertEqual(3, len(book.validate("drafts/h263.tex")))
        self.assertEqual(13, len(book.validate("drafts/matrix-transpose.tex")))
        self.assertEqual(16, len(book.validate("drafts/fixed-point-ball.tex")))
        self.assertEqual(15, len(book.validate("drafts/binary-packet-parser.tex")))
        self.assertEqual(18, len(book.validate("drafts/ring-buffer-logger.tex")))
        self.assertEqual(22, len(book.validate("drafts/utf8-scanner.tex")))
        self.assertEqual(18, len(book.validate("drafts/clipped-sprite-blitter.tex")))
        self.assertEqual(43, len(book.validate("drafts/expression-evaluator.tex")))
        self.assertEqual(46, len(book.validate("drafts/tiny-bytecode-interpreter.tex")))
        self.assertEqual(43, len(book.validate("drafts/rgb-formats.tex")))
        self.assertEqual(32, len(book.validate("drafts/pcm-mixing.tex")))
        self.assertEqual(43, len(book.validate("drafts/fixed-point-fir.tex")))
        self.assertEqual(44, len(book.validate("drafts/blocked-matmul.tex")))
        self.assertEqual(55, len(book.validate("drafts/delta-varint.tex")))
        self.assertEqual(31, len(book.validate("drafts/crc32.tex")))
        self.assertEqual(51, len(book.validate("drafts/sha256.tex")))
        self.assertEqual(53, len(book.validate("drafts/chacha20.tex")))
        self.assertEqual(51, len(book.validate("drafts/rsa-arithmetic.tex")))
        self.assertEqual(57, len(book.validate("drafts/base64.tex")))
        self.assertEqual(3, len(book.validate("drafts/binary-ppm.tex")))
        self.assertEqual(0, len(book.validate("drafts/qoi.tex")))
        self.assertEqual(0, len(book.validate("drafts/pcm-wav-assignment.tex")))

    def test_rsa_cumulative_order_and_observation_protocol(self):
        folder = book.ROOT / "drafts/rsa-construction"
        steps = json.loads((folder / "steps.json").read_text())["steps"]
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        figures = json.loads((folder / "captures/plan.json").read_text())["figures"]
        self.assertEqual(42, len(steps))
        self.assertEqual(35, sum(step["kind"] == "author" for step in steps))
        self.assertEqual(43, len(recipes))
        order = [
            path.stem
            for path, _ in book.sources("drafts/rsa-arithmetic.tex")
            if path.parent.name == "027-rsa-arithmetic"
            and re.fullmatch(r"s[0-9]{2}", path.stem)
        ]
        self.assertEqual([step["id"] for step in steps], order)
        starter = (folder / "main.asm").read_text()
        self.assertEqual(35, len(re.findall(r"^# BEGIN ", starter, re.M)))
        self.assertEqual(len(recipes), len({recipe["id"] for recipe in recipes}))
        for recipe in recipes:
            self.assertEqual(recipe["checkpoint"], recipe["functionCheckpoint"])
            self.assertEqual(
                (
                    recipe["target"] + "_return"
                    if recipe["checkpoint"] == "function_return"
                    else recipe["checkpoint"]
                ),
                recipe["callerCheckpoint"],
            )
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            self.assertNotIn("correctChoice", step["quiz"])
            self.assertNotIn("answer", step["quiz"])
            if step["kind"] == "author":
                name = step["region"]
                self.assertEqual(1, starter.count("# BEGIN " + name + "\n"))
                selected = [
                    recipe for recipe in recipes if recipe["step"] == step["id"]
                ]
                self.assertTrue(selected)
                self.assertTrue(
                    all(
                        recipe["target"] == step["target"]
                        and recipe["phase"] == step["phase"]
                        for recipe in selected
                    )
                )
                native = (
                    book.ROOT
                    / "includes/chapters/027-rsa-arithmetic"
                    / (step["id"] + ".tex")
                ).read_text()
                self.assertEqual(1, len(book.CODE.findall(native)))
                self.assertIn(
                    r"\texttt{" + step["checkpoint"].replace("_", r"\_") + "}", native
                )
        self.assertEqual(4, len(figures))
        by_id = {recipe["id"]: recipe for recipe in recipes}
        for figure in figures:
            recipe = by_id[figure["recipe"]]
            self.assertEqual(figure["step"], recipe["step"])
            self.assertEqual(figure["checkpoint"], recipe["checkpoint"])
            self.assertEqual("IntelliJ Light", figure["theme"])
            self.assertIn("no IDE capture", figure["status"])

    def test_rsa_preparation_preserves_authored_work_and_rejects_bad_values(self):
        folder = book.ROOT / "drafts/rsa-construction"
        spec = importlib.util.spec_from_file_location(
            "rsa_prepare_tests", folder / "prepare.py"
        )
        prep = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(prep)
        source = (folder / "main.asm").read_text()
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        bodies = lambda text: re.findall(r"^# BEGIN .*?^# END .*?$", text, re.M | re.S)
        for recipe in recipes:
            prepared = prep.prepare(source, recipe)
            self.assertEqual(bodies(source), bodies(prepared))
            self.assertEqual(256, len(prep.arenas(recipe)["output"]))
            self.assertIn(
                "mode: .word " + str(prep.TARGETS.index(recipe["target"])) + "\n",
                prepared,
            )
        sample = next(recipe for recipe in recipes if recipe["target"] == "addmod128")
        for changes in [
            {"left": "-1"},
            {"left": str(2**128)},
            {"left": "01"},
            {"modulus": "2", "left": "2"},
            {"target": "unknown"},
        ]:
            with self.assertRaises(ValueError):
                prep.prepare(source, {**sample, **changes})
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "kept.asm"
            output.write_text("student work")
            result = subprocess.run(
                [
                    sys.executable,
                    str(folder / "prepare.py"),
                    recipes[0]["id"],
                    "--output",
                    str(output),
                ],
                capture_output=True,
                text=True,
            )
            self.assertNotEqual(0, result.returncode)
            self.assertEqual("student work", output.read_text())
        with self.assertRaises(ValueError):
            prep.prepare(source.replace("left_end:", "missing_boundary:"), sample)

    def test_chacha_cumulative_native_order_and_preparation(self):
        folder = book.ROOT / "drafts/chacha-construction"
        steps = json.loads((folder / "steps.json").read_text())["steps"]
        self.assertEqual(
            (42, 36), (len(steps), sum(s["kind"] == "author" for s in steps))
        )
        order = [
            path.stem
            for path, _ in book.sources("drafts/chacha20.tex")
            if path.parent.name == "026-chacha20"
            and re.fullmatch(r"s[0-9]{2}", path.stem)
        ]
        self.assertEqual([s["id"] for s in steps], order)
        spec = importlib.util.spec_from_file_location(
            "chacha_prepare_test", folder / "prepare.py"
        )
        prepare = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(prepare)
        starter = (folder / "main.asm").read_text()
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        self.assertEqual(54, len(recipes))
        self.assertEqual(54, len({r["id"] for r in recipes}))
        variants = (
            book.ROOT / "includes/chapters/026-chacha20/variants.tex"
        ).read_text()
        self.assertEqual(
            {r["id"] for r in recipes if not r["id"].endswith("-1")},
            set(re.findall(r"\\texttt\{(s[0-9]+-[0-9]+)\}", variants)),
        )
        self.assertNotIn("correctChoice", (folder / "steps.json").read_text())
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            text = (
                book.ROOT / "includes/chapters/026-chacha20" / (step["id"] + ".tex")
            ).read_text()
            self.assertEqual(
                [step["code"]] if step["kind"] == "author" else [],
                [m[1] for m in book.CODE.finditer(text)],
            )
        for q in recipes:
            prepared = prepare.prepare(starter, q)
            self.assertEqual(
                (4160, 4160, 128, 96), tuple(map(len, prepare.arenas(q).values()))
            )
            for step in steps:
                if step["kind"] == "author":
                    name = step["region"]
                    pattern = r"# BEGIN " + name + r"\n(.*?)# END " + name
                    self.assertEqual(
                        re.search(pattern, starter, re.S)[1],
                        re.search(pattern, prepared, re.S)[1],
                    )
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "observation.asm"
            source = Path(directory) / "mine.asm"
            source.write_text(starter)
            args = [
                sys.executable,
                str(folder / "prepare.py"),
                "s05-1",
                "--source",
                str(source),
                "--output",
                str(output),
            ]
            subprocess.run(args, check=True, capture_output=True)
            self.assertEqual(
                prepare.prepare(
                    starter, next(r for r in recipes if r["id"] == "s05-1")
                ),
                output.read_text(),
            )
            self.assertNotEqual(0, subprocess.run(args, capture_output=True).returncode)
            self.assertEqual(starter, source.read_text())
        plan = json.loads((folder / "captures/plan.json").read_text())
        self.assertEqual(4, len(plan["figures"]))
        self.assertTrue(
            all(
                f["theme"] == "IntelliJ Light" and f["status"].startswith("planned")
                for f in plan["figures"]
            )
        )

    def test_sha_cumulative_edits_follow_native_order(self):
        folder = book.ROOT / "drafts/sha-construction"
        steps = json.loads((folder / "steps.json").read_text())["steps"]
        self.assertEqual(47, len(steps))
        self.assertEqual(38, sum(step["kind"] == "author" for step in steps))
        self.assertEqual(47, len({step["quiz"]["id"] for step in steps}))
        self.assertNotIn("correctChoice", (folder / "steps.json").read_text())
        order = [
            path.stem
            for path, _ in book.sources("drafts/sha256.tex")
            if path.parent.name == "025-sha256"
            and re.fullmatch(r"s[0-9]{2}", path.stem)
        ]
        self.assertEqual([step["id"] for step in steps], order)
        starter = (folder / "main.asm").read_text()
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            text = (
                book.ROOT / "includes/chapters/025-sha256" / (step["id"] + ".tex")
            ).read_text()
            self.assertEqual(
                [step["code"]] if step["kind"] == "author" else [],
                [match[1] for match in book.CODE.finditer(text)],
            )
            if step["kind"] == "author":
                self.assertEqual(1, starter.count("# BEGIN " + step["region"] + "\n"))
                self.assertIn(
                    "    b ",
                    starter.split("# BEGIN " + step["region"] + "\n")[1].split(
                        "# END "
                    )[0],
                )
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        self.assertEqual(58, len(recipes))
        self.assertEqual(58, len({q["id"] for q in recipes}))
        self.assertEqual(
            {step["id"] for step in steps if step["kind"] == "author"}
            | {"s10", "s20", "s28", "s37", "s45"},
            {q["step"] for q in recipes},
        )
        plan = json.loads((folder / "captures/plan.json").read_text())
        self.assertEqual(4, len(plan["figures"]))
        self.assertTrue(
            all(figure["status"].startswith("planned") for figure in plan["figures"])
        )

    def test_sha_preparation_preserves_work_and_refuses_existing_outputs(self):
        folder = book.ROOT / "drafts/sha-construction"
        spec = importlib.util.spec_from_file_location(
            "sha_prepare_test", folder / "prepare.py"
        )
        prepare = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(prepare)
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        source = (folder / "main.asm").read_text()
        for q in recipes:
            arenas = prepare.arenas(q)
            self.assertEqual(
                (4160, 96, 320, 832, 96, 128, 320), tuple(map(len, arenas.values()))
            )
            self.assertEqual(
                q["contextTotal"],
                int.from_bytes(bytes(arenas["context"][32:40]), "little"),
            )
            self.assertEqual(
                q["contextTail"], arenas["context"][48 : 48 + len(q["contextTail"])]
            )
            prepared = prepare.prepare(source, q)
            self.assertEqual(source.split(".text\n")[1], prepared.split(".text\n")[1])
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            working = root / "working.asm"
            working.write_text(source)
            output = root / "observation.asm"
            command = [
                sys.executable,
                str(folder / "prepare.py"),
                "s03-1",
                "--source",
                str(working),
                "--output",
                str(output),
            ]
            first = subprocess.run(command, capture_output=True, text=True)
            self.assertEqual(0, first.returncode, first.stderr)
            contents = output.read_bytes()
            second = subprocess.run(command, capture_output=True, text=True)
            self.assertNotEqual(0, second.returncode)
            self.assertEqual(contents, output.read_bytes())
            self.assertEqual(source, working.read_text())

    def test_sha_worked_digest_padding_and_measured_rows(self):
        native = book.ROOT / "includes/chapters/025-sha256"
        worked = (native / "worked.tex").read_text()
        digest = hashlib.sha256(b"abc").hexdigest()
        for index in range(0, 64, 8):
            self.assertIn(digest[index : index + 8], worked)
        padding = (native / "padding.tex").read_text()
        for length in [0, 3, 55, 56, 63, 64, 65, 120]:
            used = length % 64
            final_blocks = (used + 1 + 8 + 63) // 64
            self.assertIn(
                f"{length} & {length // 64} & {used} & {final_blocks}",
                " ".join(padding.split()),
            )
        receipt = json.loads(
            (book.ROOT / "book/measurements/025-sha256.json").read_text()
        )
        primary, alternative = receipt["implementations"]
        work = (native / "work.tex").read_text()
        labels = [
            "Empty",
            "ASCII abc",
            "55 bytes",
            "56 bytes",
            "64 bytes",
            "65 bytes",
            "4096 bytes",
        ]
        for label, a, b in zip(labels, primary["rows"], alternative["rows"]):
            self.assertEqual(
                (a["dataset"], a["bytes"], a["digest"]),
                (b["dataset"], b["bytes"], b["digest"]),
            )
            row = f"{label} & {a['updateInstructions']} & {b['updateInstructions']} & {a['finalizationInstructions']} & {b['finalizationInstructions']}"
            self.assertIn(row, " ".join(work.split()))
        for implementation in [primary, alternative]:
            for row in implementation["rows"]:
                length = row["bytes"]
                message = (
                    b"abc"
                    if row["dataset"] == "abc"
                    else bytes(
                        (i * 73 + 128) % 256 if length == 4096 else i % 256
                        for i in range(length)
                    )
                )
                self.assertEqual(hashlib.sha256(message).hexdigest(), row["digest"])

    def test_crc_construction_transcript_and_native_order(self):
        folder = book.ROOT / "drafts/crc-construction"
        steps = json.loads((folder / "steps.json").read_text())["steps"]
        self.assertEqual(34, len(steps))
        self.assertEqual(24, sum(step["kind"] == "author" for step in steps))
        self.assertEqual(34, len({step["quiz"]["id"] for step in steps}))
        self.assertNotIn("correctChoice", (folder / "steps.json").read_text())
        order = [
            path.stem
            for path, _ in book.sources("drafts/crc32.tex")
            if path.parent.name == "024-crc32" and re.fullmatch(r"s[0-9]{2}", path.stem)
        ]
        self.assertEqual([step["id"] for step in steps], order)
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            text = (
                book.ROOT / "includes/chapters/024-crc32" / (step["id"] + ".tex")
            ).read_text()
            self.assertEqual(
                [step["code"]] if step["kind"] == "author" else [],
                [match[1] for match in book.CODE.finditer(text)],
            )
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        self.assertEqual(52, len(recipes))
        self.assertEqual(52, len({recipe["id"] for recipe in recipes}))
        self.assertEqual(
            {step["id"] for step in steps if step["kind"] == "author"}
            | {"s09", "s16", "s22", "s30", "s31", "s32", "s34"},
            {recipe["step"] for recipe in recipes},
        )

    def test_crc_worked_values_and_measurement_tables(self):
        native = book.ROOT / "includes/chapters/024-crc32"
        worked = (native / "worked.tex").read_text()
        state = 0xFFFFFFFF ^ 0x31
        for _ in range(9):
            self.assertIn(f"{state:08x}", worked)
            state = (state >> 1) ^ (0xEDB88320 if state & 1 else 0)
        streaming = (native / "streaming.tex").read_text()
        for length in [0, 1, 3, 6, 7, 9]:
            checksum = zlib.crc32(b"123456789"[:length])
            self.assertIn(f"{checksum:08x}", streaming)
            self.assertIn(f"{checksum ^ 0xffffffff:08x}", streaming)
        self.assertEqual(0xAE426082, zlib.crc32(b"IEND"))
        self.assertIn("ae426082", (native / "png.tex").read_text())
        receipt = json.loads(
            (book.ROOT / "book/measurements/024-crc32.json").read_text()
        )
        self.assertTrue(receipt["tableConstructionSeparate"])
        one, two = receipt["implementations"]
        self.assertEqual(
            (17420, 18444),
            (
                one["tableInitializationInstructions"],
                two["tableInitializationInstructions"],
            ),
        )
        labels = [
            "Empty",
            "One byte",
            "Check message",
            "Zero bytes",
            "All octets",
            "Strided octets",
        ]
        text = (native / "work.tex").read_text()
        for index, label in enumerate(labels):
            a, b = one["rows"][index * 2 : index * 2 + 2]
            c, d = two["rows"][index * 2 : index * 2 + 2]
            self.assertEqual(
                (a["dataset"], a["bytes"], a["crc"]),
                (d["dataset"], d["bytes"], d["crc"]),
            )
            row = f"{label} & {a['instructions']} & {c['instructions']} & {b['instructions']} & {d['instructions']}"
            self.assertIn(row, " ".join(text.split()))

    def test_delta_construction_transcript_matches_native_edits(self):
        folder = book.ROOT / "drafts/delta-construction"
        steps = json.loads((folder / "steps.json").read_text())["steps"]
        self.assertEqual(60, len(steps))
        native_order = [
            path.stem
            for path, _ in book.sources("drafts/delta-varint.tex")
            if path.parent.name == "023-delta-varint"
            and re.fullmatch(r"s[0-9]{2}", path.stem)
        ]
        self.assertEqual([step["id"] for step in steps], native_order)
        self.assertEqual(50, sum(s["kind"] == "author" for s in steps))
        self.assertEqual(60, len({s["quiz"]["id"] for s in steps}))
        self.assertNotIn("correctChoice", (folder / "steps.json").read_text())
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            native = (
                book.ROOT / "includes/chapters/023-delta-varint" / (step["id"] + ".tex")
            ).read_text()
            self.assertEqual(
                [step["code"]] if step["kind"] == "author" else [],
                [match[1] for match in book.CODE.finditer(native)],
            )
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        self.assertEqual(84, len(recipes))
        self.assertEqual(84, len({r["id"] for r in recipes}))
        self.assertEqual(
            {s["id"] for s in steps if s["kind"] == "author"} | {"s28", "s57", "s58"},
            {r["step"] for r in recipes},
        )

    def test_pcm_construction_transcript_matches_editable_native_code(self):
        folder = book.ROOT / "drafts/pcm-construction"
        transcript = json.loads((folder / "steps.json").read_text())
        steps = transcript["steps"]
        self.assertEqual(40, len(steps))
        self.assertEqual(30, sum(s["kind"] == "author" for s in steps))
        self.assertEqual(40, len({s["quiz"]["id"] for s in steps}))
        self.assertNotIn("correctChoice", (folder / "steps.json").read_text())
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            native = (
                book.ROOT / "includes/chapters/020-pcm-mixing" / (step["id"] + ".tex")
            ).read_text()
            self.assertEqual(
                [step["code"]] if step["kind"] == "author" else [],
                [match[1] for match in book.CODE.finditer(native)],
            )
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        self.assertEqual(48, len(recipes))
        self.assertEqual(48, len({r["id"] for r in recipes}))
        self.assertEqual(
            {s["id"] for s in steps if s["kind"] == "author"},
            {r["step"] for r in recipes},
        )

    def test_pcm_listening_assets_and_native_waveform_agree(self):
        folder = book.ROOT / "book/media/020-pcm-mixing"
        receipt = json.loads((folder / "receipt.json").read_text())
        self.assertTrue(receipt["verifiedBothImplementations"])
        self.assertEqual(12, len(receipt["files"]))
        samples = {}
        for name, record in receipt["files"].items():
            path = folder / name
            self.assertEqual(
                record["sha256"], hashlib.sha256(path.read_bytes()).hexdigest()
            )
            with wave.open(str(path), "rb") as sound:
                self.assertEqual(
                    (1, 2, 16000, 32000),
                    (
                        sound.getnchannels(),
                        sound.getsampwidth(),
                        sound.getframerate(),
                        sound.getnframes(),
                    ),
                )
                raw = sound.readframes(sound.getnframes())
            self.assertEqual(record["pcmSha256"], hashlib.sha256(raw).hexdigest())
            samples[name] = struct.unpack("<" + "h" * 32000, raw)
        for scene, expected_clipped in [
            ("quiet", 0),
            ("clipping", 11000),
            ("phase-offset", 11000),
        ]:
            sums = [
                a + b
                for a, b in zip(samples[scene + "-a.wav"], samples[scene + "-b.wav"])
            ]
            expected = tuple(min(32767, max(-32768, value)) for value in sums)
            self.assertEqual(expected, samples[scene + "-saturated.wav"])
            self.assertEqual(
                tuple((value + 32768) % 65536 - 32768 for value in sums),
                samples[scene + "-wrapped.wav"],
            )
            self.assertEqual(
                expected_clipped, sum(a != b for a, b in zip(sums, expected))
            )
            self.assertEqual(expected_clipped, receipt["scenes"][scene]["clipped"])
        self.assertEqual(samples["quiet-saturated.wav"], samples["quiet-wrapped.wav"])
        figure = (
            book.ROOT / "includes/chapters/020-pcm-mixing/listening.tex"
        ).read_text()
        plotted = re.findall(
            r"\\addplot\[[^\]]*\] coordinates \{([^}]*)\};\s*\\addlegendentry\{([^}]*)\}",
            figure,
        )
        self.assertEqual(3, len(plotted))
        expected = {
            "Exact sum": [
                a + b
                for a, b in zip(
                    samples["clipping-a.wav"][:64], samples["clipping-b.wav"][:64]
                )
            ],
            "Saturated": samples["clipping-saturated.wav"][:64],
            "Wrapped": samples["clipping-wrapped.wav"][:64],
        }
        for coordinates, label in plotted:
            points = [
                (int(x), int(y))
                for x, y in re.findall(r"\((\d+),(-?\d+)\)", coordinates)
            ]
            self.assertEqual(list(enumerate(expected[label])), points)

    def test_fir_construction_transcript_matches_native_edits(self):
        folder = book.ROOT / "drafts/fir-construction"
        steps = json.loads((folder / "steps.json").read_text())["steps"]
        self.assertEqual(47, len(steps))
        self.assertEqual(36, sum(s["kind"] == "author" for s in steps))
        self.assertEqual(47, len({s["quiz"]["id"] for s in steps}))
        self.assertNotIn("correctChoice", (folder / "steps.json").read_text())
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            native = (
                book.ROOT
                / "includes/chapters/021-fixed-point-fir"
                / (step["id"] + ".tex")
            ).read_text()
            self.assertEqual(
                [step["code"]] if step["kind"] == "author" else [],
                [match[1] for match in book.CODE.finditer(native)],
            )
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        self.assertEqual(66, len(recipes))
        self.assertEqual(66, len({r["id"] for r in recipes}))
        self.assertEqual(
            {s["id"] for s in steps if s["kind"] == "author"} | {"s42"},
            {r["step"] for r in recipes},
        )

    def test_matmul_construction_transcript_matches_native_edits(self):
        folder = book.ROOT / "drafts/matmul-construction"
        steps = json.loads((folder / "steps.json").read_text())["steps"]
        self.assertEqual(50, len(steps))
        native_order = [
            path.stem
            for path, _ in book.sources("drafts/blocked-matmul.tex")
            if path.parent.name == "022-blocked-matmul"
            and re.fullmatch(r"s[0-9]{2}", path.stem)
        ]
        self.assertEqual([step["id"] for step in steps], native_order)
        self.assertEqual(42, sum(s["kind"] == "author" for s in steps))
        self.assertEqual(50, len({s["quiz"]["id"] for s in steps}))
        self.assertNotIn("correctChoice", (folder / "steps.json").read_text())
        for step in steps:
            self.assertEqual(2, len(step["hints"]))
            native = (
                book.ROOT
                / "includes/chapters/022-blocked-matmul"
                / (step["id"] + ".tex")
            ).read_text()
            self.assertEqual(
                [step["code"]] if step["kind"] == "author" else [],
                [match[1] for match in book.CODE.finditer(native)],
            )
        recipes = json.loads((folder / "inputs.json").read_text())["recipes"]
        self.assertEqual(58, len(recipes))
        self.assertEqual(58, len({r["id"] for r in recipes}))
        self.assertEqual(
            {s["id"] for s in steps if s["kind"] == "author"} | {"s48"},
            {r["step"] for r in recipes},
        )
        self.assertEqual(
            ("block_reduction", 2),
            next((r["checkpoint"], r["visit"]) for r in recipes if r["step"] == "s48"),
        )

    def test_fir_listening_samples_and_native_plot_agree(self):
        folder = book.ROOT / "book/media/021-fixed-point-fir"
        receipt = json.loads((folder / "receipt.json").read_text())
        self.assertTrue(receipt["verifiedBothImplementations"])
        self.assertEqual([4096] * 8, receipt["tapsOldestFirst"])
        self.assertEqual(6, len(receipt["files"]))
        samples = {}
        for name, record in receipt["files"].items():
            path = folder / name
            self.assertEqual(
                record["sha256"], hashlib.sha256(path.read_bytes()).hexdigest()
            )
            with wave.open(str(path), "rb") as sound:
                self.assertEqual(
                    (1, 2, 16000, 32000),
                    (
                        sound.getnchannels(),
                        sound.getsampwidth(),
                        sound.getframerate(),
                        sound.getnframes(),
                    ),
                )
                raw = sound.readframes(sound.getnframes())
            self.assertEqual(record["pcmSha256"], hashlib.sha256(raw).hexdigest())
            samples[name] = struct.unpack("<" + "h" * 32000, raw)

        def low(position):
            u = position % 64
            return 375 * (u if u < 16 else 32 - u if u < 48 else u - 64)

        def signal(scene, position):
            high = (0, 12000, 0, -12000)[position % 4]
            return (
                low(position)
                if scene == "low"
                else high if scene == "high" else low(position) + high
            )

        for scene in ["low", "high", "mixed"]:
            self.assertEqual(
                tuple(signal(scene, i) for i in range(32000)),
                samples[scene + "-input.wav"],
            )
            wanted = tuple(
                (sum(signal(scene, i - j) for j in range(8)) + 4) // 8
                for i in range(32000)
            )
            self.assertEqual(wanted, samples[scene + "-filtered.wav"])
        self.assertFalse(any(samples["high-filtered.wav"]))
        self.assertEqual(samples["low-filtered.wav"], samples["mixed-filtered.wav"])
        figure = (
            book.ROOT / "includes/chapters/021-fixed-point-fir/listening.tex"
        ).read_text()
        plotted = re.findall(
            r"\\addplot\[[^\]]*\] coordinates \{([^}]*)\};\s*\\addlegendentry\{([^}]*)\}",
            figure,
        )
        self.assertEqual(2, len(plotted))
        expected = {
            "High input": samples["high-input.wav"][:16],
            "Filtered": samples["high-filtered.wav"][:16],
        }
        for coordinates, label in plotted:
            points = [
                (int(x), int(y))
                for x, y in re.findall(r"\((\d+),(-?\d+)\)", coordinates)
            ]
            self.assertEqual(list(enumerate(expected[label])), points)

    def test_author_can_include_prose_between_any_two_segments(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / "main.tex").write_text(r"\input{first}\input{author}\input{second}")
            for name in ["first", "author", "second"]:
                (root / (name + ".tex")).write_text(name)
            before = {p.name: p.read_bytes() for p in root.iterdir()}
            self.assertEqual(
                ["first", "author", "second"],
                [text for _, text in book.sources(root=root)[1:]],
            )
            self.assertEqual(before, {p.name: p.read_bytes() for p in root.iterdir()})

    def test_missing_cyclic_and_escaping_include_fail(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            for text in [r"\input{missing}", r"\input{main}", r"\input{../escape}"]:
                (root / "main.tex").write_text(text)
                with self.assertRaises((ValueError, FileNotFoundError)):
                    book.sources(root=root)

    def test_stale_native_code_requires_a_fresh_ide_export(self):
        with patch.object(book, "snippets", return_value=["li $v0, 999 # changed\n"]):
            with self.assertRaisesRegex(ValueError, "Source changed"):
                book.validate()


class NativeReferenceConvergenceTests(unittest.TestCase):
    def test_unrelated_document_changes_do_not_invalidate_job(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "reading.aux").write_text("reading labels")
            unrelated = root / "other.aux"
            unrelated.write_text("old unrelated labels")
            recorder = root / "reading.fls"
            recorder.write_text("INPUT reading.aux\nOUTPUT reading.aux\n")
            before = book.reference_state(recorder, root)
            unrelated.write_text("new unrelated labels")
            self.assertEqual(before, book.reference_state(recorder, root))

    def test_recorded_chapter_and_contents_changes_invalidate_job(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            chapter = root / "chapter with spaces.aux"
            chapter.write_text("old chapter label")
            contents = root / "reading.toc"
            contents.write_text("old contents")
            recorder = root / "reading.fls"
            recorder.write_text("INPUT chapter with spaces.aux\nOUTPUT reading.toc\n")
            before = book.reference_state(recorder, root)
            chapter.write_text("new chapter label")
            changed = book.reference_state(recorder, root)
            self.assertNotEqual(before, changed)
            contents.write_text("new contents")
            self.assertNotEqual(changed, book.reference_state(recorder, root))

    def test_absolute_recorded_paths_and_duplicate_notices(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            reference = root / "reading.out"
            reference.write_text("bookmarks")
            recorder = root / "reading.fls"
            recorder.write_text("OUTPUT " + str(reference) + "\n")
            before = book.reference_state(recorder, root)
            recorder.write_text(
                "INPUT reading.out\nOUTPUT " + str(reference) + "\nINPUT font.otf\n"
            )
            self.assertEqual(before, book.reference_state(recorder, root))

    def test_missing_reference_evidence_is_not_stable(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            recorder = root / "reading.fls"
            recorder.write_text("INPUT font.otf\n")
            with self.assertRaisesRegex(ValueError, "no reference files"):
                book.reference_state(recorder, root)
            recorder.write_text("OUTPUT missing.aux\n")
            with self.assertRaises(FileNotFoundError):
                book.reference_state(recorder, root)
