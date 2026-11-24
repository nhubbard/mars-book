#!/usr/bin/env python3
"""Reproduce the book's public FIR listening scenes from a student's MARS program."""

import argparse
import hashlib
import json
from pathlib import Path
import struct
import subprocess
import wave

SCENES = ("low", "high", "mixed")
TRACKS = ("input", "filtered")
FRAMES = 32000
RATE = 16000
BLOCK = 64


def require(condition, message):
    if not condition:
        raise ValueError(message)


def signal(scene, position):
    phase = position % 64
    level = phase if phase < 16 else 32 - phase if phase < 48 else phase - 64
    low = 375 * level
    high = (0, 12000, 0, -12000)[position % 4]
    return low if scene == "low" else high if scene == "high" else low + high


def read_transcript(text):
    streams = {(scene, track): bytearray() for scene in SCENES for track in TRACKS}
    ended = set()
    passed = set()
    for line in text.splitlines():
        if line.startswith("FIR,"):
            fields = line.split(",")
            require(len(fields) == 5, "Malformed FIR sample record")
            _, scene, track, offset, encoded = fields
            require(
                (scene, track) in streams and scene not in ended,
                "Unexpected scene or track",
            )
            target = streams[(scene, track)]
            raw = bytes.fromhex(encoded)
            require(
                int(offset) == len(target) // 2,
                "Missing, repeated or reordered sample block",
            )
            require(
                len(raw) == BLOCK * 2 and len(target) + len(raw) <= FRAMES * 2,
                "Wrong sample block size",
            )
            target.extend(raw)
        elif line.startswith("FIR-END,"):
            fields = line.split(",")
            require(len(fields) == 4, "Malformed scene terminator")
            _, scene, frames, rate = fields
            require(
                scene in SCENES and scene not in ended,
                "Repeated or unknown scene terminator",
            )
            require(
                (int(frames), int(rate)) == (FRAMES, RATE),
                "Wrong frame count or sample rate",
            )
            require(
                all(len(streams[(scene, track)]) == FRAMES * 2 for track in TRACKS),
                "Scene ended before all samples arrived",
            )
            ended.add(scene)
        elif line.startswith("PASS listening "):
            scene = line.removeprefix("PASS listening ")
            require(
                scene in SCENES and scene not in passed,
                "Unknown or repeated listening result",
            )
            passed.add(scene)
        elif line.startswith("FAIL "):
            raise ValueError("The submitted program failed a listening check: " + line)
    require(
        ended == set(SCENES) and passed == set(SCENES), "Incomplete listening checks"
    )
    return {key: bytes(value) for key, value in streams.items()}


def validate_samples(streams):
    for scene in SCENES:
        original = tuple(signal(scene, i) for i in range(FRAMES))
        filtered = tuple(
            (sum(signal(scene, i - tap) for tap in range(8)) + 4) // 8
            for i in range(FRAMES)
        )
        for track, values in [("input", original), ("filtered", filtered)]:
            require(
                streams[(scene, track)] == struct.pack("<" + "h" * FRAMES, *values),
                scene + "/" + track + " differs from the public integer oracle",
            )
    require(not any(streams[("high", "filtered")]), "High-only output must be zero")
    require(
        streams[("mixed", "filtered")] == streams[("low", "filtered")],
        "Filtered mixed and low scenes must agree",
    )


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def render(material, cli, java, source, output):
    check = material / "checks/listening.kts"
    result = subprocess.run(
        [java, "-jar", str(cli), "grade", str(check), str(source)],
        capture_output=True,
        text=True,
    )
    if result.returncode:
        raise RuntimeError(
            "MARS listening checks failed:\n" + result.stdout + result.stderr
        )
    streams = read_transcript(result.stdout)
    validate_samples(streams)
    output.mkdir(parents=True, exist_ok=True)
    files = {}
    for scene in SCENES:
        for track in TRACKS:
            raw = streams[(scene, track)]
            path = output / (scene + "-" + track + ".wav")
            with wave.open(str(path), "wb") as sound:
                sound.setnchannels(1)
                sound.setsampwidth(2)
                sound.setframerate(RATE)
                sound.writeframes(raw)
            with wave.open(str(path), "rb") as sound:
                require(
                    (
                        sound.getnchannels(),
                        sound.getsampwidth(),
                        sound.getframerate(),
                        sound.getnframes(),
                    )
                    == (1, 2, RATE, FRAMES),
                    "WAV metadata did not round-trip",
                )
                require(
                    sound.readframes(FRAMES) == raw, "WAV samples did not round-trip"
                )
            files[path.name] = {
                "sha256": digest(path),
                "pcmSha256": hashlib.sha256(raw).hexdigest(),
                "frames": FRAMES,
                "sampleRate": RATE,
                "channels": 1,
                "sampleBits": 16,
                "seconds": 2,
            }
    receipt = {
        "status": "Student program samples verified; live audio-device behavior not tested",
        "sourceSha256": digest(source),
        "cliSha256": digest(cli),
        "rendererSha256": digest(Path(__file__)),
        "checksSha256": digest(check),
        "files": files,
        "tapsOldestFirst": [4096] * 8,
        "rounding": "nearest, ties toward positive infinity, once after exact accumulation",
        "scenes": {
            scene: {
                "frames": FRAMES,
                "sampleRate": RATE,
                "history": "Seven preceding samples from the same periodic signal; no zero-start transient",
            }
            for scene in SCENES
        },
    }
    (output / "receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
    return receipt


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--material", type=Path, required=True)
    parser.add_argument("--cli", type=Path, required=True)
    parser.add_argument("--java", default="java")
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    receipt = render(
        args.material.resolve(),
        args.cli.resolve(),
        args.java,
        args.source.resolve(),
        args.output.resolve(),
    )
    print("Verified and wrote", len(receipt["files"]), "mono WAV files")


if __name__ == "__main__":
    main()
