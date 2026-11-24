from pathlib import Path
import unittest
import wave

import fir_listening


class FirListeningTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        folder = Path(__file__).resolve().parents[1] / "book/media/021-fixed-point-fir"
        cls.raw = {}
        lines = []
        for scene in fir_listening.SCENES:
            for track in fir_listening.TRACKS:
                with wave.open(
                    str(folder / (scene + "-" + track + ".wav")), "rb"
                ) as sound:
                    cls.raw[(scene, track)] = sound.readframes(sound.getnframes())
            for start in range(0, 32000, 64):
                for track in fir_listening.TRACKS:
                    block = cls.raw[(scene, track)][start * 2 : (start + 64) * 2]
                    lines.append(f"FIR,{scene},{track},{start},{block.hex()}")
            lines.extend([f"FIR-END,{scene},32000,16000", f"PASS listening {scene}"])
        cls.transcript = "\n".join(lines)

    def test_published_samples_round_trip_through_public_protocol(self):
        decoded = fir_listening.read_transcript(self.transcript)
        self.assertEqual(self.raw, decoded)
        fir_listening.validate_samples(decoded)

    def test_reordered_unknown_and_short_blocks_are_rejected(self):
        variants = [
            self.transcript.replace("FIR,low,input,0,", "FIR,low,input,64,", 1),
            self.transcript.replace("FIR,low,input,0,", "FIR,low,unknown,0,", 1),
            self.transcript.replace(
                self.transcript.splitlines()[0], "FIR,low,input,0,0000", 1
            ),
        ]
        for text in variants:
            with self.subTest(text=text[:50]), self.assertRaises(ValueError):
                fir_listening.read_transcript(text)

    def test_incomplete_or_repeated_results_are_rejected(self):
        variants = [
            self.transcript.replace("FIR-END,mixed,32000,16000", ""),
            self.transcript.replace("PASS listening mixed", ""),
            self.transcript + "\nPASS listening low",
            self.transcript + "\nFIR-END,low,32000,16000",
            self.transcript + "\nFAIL listening mixed",
        ]
        for text in variants:
            with self.subTest(tail=text[-70:]), self.assertRaises(ValueError):
                fir_listening.read_transcript(text)

    def test_wrong_frame_metadata_is_rejected(self):
        with self.assertRaises(ValueError):
            fir_listening.read_transcript(
                self.transcript.replace(
                    "FIR-END,low,32000,16000", "FIR-END,low,32000,8000"
                )
            )

    def test_one_changed_simulator_sample_prevents_publication(self):
        streams = dict(self.raw)
        altered = bytearray(streams[("mixed", "filtered")])
        altered[120] ^= 1
        streams[("mixed", "filtered")] = bytes(altered)
        with self.assertRaisesRegex(ValueError, "mixed/filtered"):
            fir_listening.validate_samples(streams)
