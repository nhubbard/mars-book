import hashlib
import io
import json
from pathlib import Path
import tempfile
import unittest
import wave
from wav_reverse_lesson_cases import (
    OBJECTS,
    TRACE,
    build,
    cases,
    description,
    expanded,
    filtered_trace,
    memory,
    patterned,
    pipeline,
    profiles,
    reverse_frames,
    reversal,
)


class WavReverseLessonCasesTest(unittest.TestCase):
    def test_exact_full_grid_and_same_session_reuse_remain_intact(self):
        rows = cases()
        self.assertEqual(len(rows), 35)
        self.assertEqual(len({c["name"] for c in rows}), 35)
        self.assertEqual(sum(len(c["calls"]) for c in rows), 40)
        self.assertEqual([c["instructionLimit"] for c in rows[24:32]], [100000] * 8)
        self.assertTrue(
            all(c["instructionLimit"] == 10000000 for c in rows[:24] + rows[32:])
        )
        reused = rows[-2]
        self.assertEqual(len(reused["calls"]), 6)
        self.assertEqual(
            reused["calls"][1]["arguments"],
            dict(a0="0x0000000000000001", a2="0x0000000000000001"),
        )
        self.assertEqual(
            reused["calls"][3]["expectedRegisters"]["v0"], "0x0000000000000001"
        )
        for c in rows[:-1]:
            self.assertEqual(len(c["preservedRegisters"]), 9)
            self.assertEqual(c["stackAlignment"], 8)
            self.assertEqual(c["callerStackBytes"], 0)
            for call in c["calls"]:
                self.assertEqual(
                    {
                        r["at"]["label"]: r["length"]
                        for r in call["callerAccess"]["objects"]
                    },
                    OBJECTS,
                )
                self.assertEqual(call["callerAccess"]["maxAccesses"], 1000000)
                self.assertNotIn("workspace", OBJECTS)

    def test_frame_order_preserves_channel_order_for_disjoint_and_unaligned_aliases(
        self,
    ):
        raw = bytes.fromhex("e02e20d10000803ec0e00000ff7f0080")
        self.assertEqual(
            reverse_frames(raw, 4), bytes.fromhex("ff7f0080c0e000000000803ee02e20d1")
        )
        self.assertEqual(reverse_frames(reverse_frames(raw, 4), 4), raw)
        for channels in [1, 2]:
            for alias in [False, True]:
                c = reversal(16000, channels, 7, alias, 9)
                result = c["memory"][0]
                self.assertEqual(
                    expanded(result),
                    reverse_frames(patterned(14 * channels, 9), 2 * channels),
                )
                self.assertEqual(result["offset"], 5 if alias else 3)
                self.assertEqual(c["argumentAddresses"]["a0"]["offset"], 5)
        zero = reversal(16000, 2, 0, False, 3)
        self.assertEqual(zero["memory"], [])
        self.assertEqual(zero["expectedRegisters"]["v1"], "0x0000000000000000")
        bad = reversal(16000, 1, -(1 << 63), False, 5)
        self.assertEqual(bad["callerAccess"]["writable"], [])
        self.assertEqual(
            expanded(bad["setupMemory"][-1]), description(16000, 1, -(1 << 63))
        )

    def test_maximum_case_keeps_every_byte_with_lossless_compact_patterns(self):
        c = cases()[-3]
        self.assertEqual(c["name"], "maximum stereo in-place reversal")
        call = c["calls"][0]
        self.assertEqual(
            call["argumentAddresses"]["a0"], call["argumentAddresses"]["a2"]
        )
        self.assertEqual(call["expectedRegisters"]["v1"], "0x0000000000010000")
        source = call["setupMemory"][1]
        self.assertEqual(len(expanded(source)), 262144)
        self.assertEqual(len(expanded(call["memory"][0])), 262144)
        self.assertEqual(
            expanded(call["memory"][0]), reverse_frames(expanded(source), 4)
        )
        self.assertGreater(source["repeat"], 1)
        for p in profiles().values():
            self.assertLessEqual(
                sum(c["instructionLimit"] for c in p["cases"]), 40000000
            )
            self.assertLessEqual(
                len(json.dumps(p, separators=(",", ":")).encode()) + 1, 1048576
            )
        self.assertEqual(len(profiles()), 7)

    def test_complete_filtered_reversed_file_agrees_with_independent_wave_reader(self):
        pcm = filtered_trace()
        self.assertEqual(
            [
                int.from_bytes(pcm[i : i + 2], "little", signed=True)
                for i in range(0, 16, 2)
            ],
            [1500, -1500, 1500, 500, 500, 500, 4596, -3596],
        )
        c = pipeline()
        output = bytes.fromhex(c["calls"][0]["expectedFiles"]["output.wav"])
        with wave.open(io.BytesIO(output), "rb") as f:
            self.assertEqual(
                (f.getnchannels(), f.getsampwidth(), f.getframerate(), f.getnframes()),
                (2, 2, 16000, 4),
            )
            self.assertEqual(f.readframes(4), reverse_frames(pcm, 4))
        with wave.open(io.BytesIO(TRACE), "rb") as f:
            self.assertEqual(f.getnframes(), 4)
        self.assertEqual(len(output), 60)
        self.assertEqual(c["fileSystem"]["maxReadSize"], 7)
        self.assertEqual(c["fileSystem"]["maxWriteSize"], 5)
        self.assertEqual(c["fileSystem"]["firstDescriptor"], 19)
        self.assertEqual(c["calls"][0]["entry"], "main")
        self.assertEqual(c["calls"][0]["returnTo"], "results_ready")
        self.assertEqual(c["exit"]["memory"][1]["bytes"], "3c00000000000000")

    def test_source_binding_and_repeatable_generation(self):
        root = Path(__file__).resolve().parents[1]
        manifest = json.loads(
            Path(__file__).with_name("wav_reverse_original_check.json").read_text()
        )
        self.assertEqual(
            hashlib.sha256((root / manifest["source"]).read_bytes()).hexdigest(),
            manifest["sha256"],
        )
        self.assertEqual(
            expanded(memory("source", b"\x00\x31" * 2048)), b"\x00\x31" * 2048
        )
        with tempfile.TemporaryDirectory() as directory:
            result = build(directory)
            self.assertEqual(result, build(directory, True))
            (Path(directory) / next(iter(result))).write_text("{}")
            with self.assertRaisesRegex(ValueError, "Stale WAV reversal profile"):
                build(directory, True)


if __name__ == "__main__":
    unittest.main()
