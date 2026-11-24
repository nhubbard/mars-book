#!/usr/bin/env python3
"""Create one of two fixed PCM WAV teaching inputs without replacing existing work."""

import argparse
from pathlib import Path
import struct

TRACE_PCM = bytes.fromhex("e02e20d10000803ec0e00000ff7f0080")
TRACE = bytes.fromhex(
    "524946464a00000057415645"
    "4a554e4b03000000010203a5"
    "666d74201000000001000200803e000000fa000004001000"
    "6461746110000000e02e20d10000803ec0e00000ff7f0080"
    "6e6f746501000000427f"
)


def material(name):
    if name == "trace":
        return 16000, 2, TRACE_PCM, TRACE
    if name == "listen":
        samples = []
        for frame in range(32000):
            left_phase = frame % 80
            right_phase = frame % 64
            left = (left_phase if left_phase <= 40 else 80 - left_phase) * 400 - 8000
            right = (
                right_phase if right_phase <= 32 else 64 - right_phase
            ) * 500 - 8000
            high = 4000 if frame % 2 == 0 else -4000
            samples.extend((left + high, right - high))
        pcm = struct.pack("<" + "h" * len(samples), *samples)
        fmt = struct.pack("<HHIIHH", 1, 2, 16000, 64000, 4, 16)
        body = b"WAVEJUNK" + struct.pack("<I", 3) + bytes.fromhex("010203a5")
        body += (
            b"fmt "
            + struct.pack("<I", 16)
            + fmt
            + b"data"
            + struct.pack("<I", len(pcm))
            + pcm
        )
        return 16000, 2, pcm, b"RIFF" + struct.pack("<I", len(body)) + body
    raise ValueError("Choose trace or listen")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path)
    parser.add_argument("--fixture", choices=["trace", "listen"], default="trace")
    args = parser.parse_args()
    with args.output.open("xb") as stream:
        stream.write(material(args.fixture)[3])
