#!/usr/bin/env python3
"""Generate packed source frames; the student program creates the encoded file."""

import argparse, importlib.util, json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "video_teaching_pixels", ROOT.parent / "video-fixtures/fixture.py"
)
video = importlib.util.module_from_spec(s)
s.loader.exec_module(video)
NAMES = (
    "moving",
    "still",
    "all-change",
    "single",
    "max-count",
    "invalid-first",
    "invalid-middle",
    "invalid-last",
)


def material(name="moving"):
    if name not in NAMES:
        raise ValueError("Unknown source animation")
    base = video.material(
        "still"
        if name in ("still", "single", "max-count")
        else "all-change" if name == "all-change" else "moving"
    )
    frames = [
        [int.from_bytes(rgb[i : i + 3], "big") for i in range(0, len(rgb), 3)]
        for rgb in base["frames"]
    ]
    if name == "single":
        frames = frames[:1]
    if name == "max-count":
        frames = [list(frames[0]) for _ in range(256)]
        frames[-1][-1] ^= 0x8000
    if name.startswith("invalid-"):
        frame = (
            0
            if name == "invalid-first"
            else len(frames) // 2 if name == "invalid-middle" else len(frames) - 1
        )
        pixel = 0 if frame == 0 else 4095
        frames[frame][pixel] |= 0x1000000
    return dict(frames=frames, period=64 if name == "max-count" else 4, duration=40)


def declaration(frames):
    flat = [v for f in frames for v in f]
    return (
        "file_encode_frames: .word -1515870811\n"
        + "".join(
            "    .word "
            + ", ".join(str(v if v < 2**31 else v - 2**32) for v in flat[i : i + 16])
            + "\n"
            for i in range(0, len(flat), 16)
        )
        + f"    .space {4194304-4*len(flat)}\n    .byte -91:60\n"
    )


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("fixture", choices=NAMES)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    m = material(a.fixture)
    with a.output.open("x") as out:
        json.dump(dict(fixture=a.fixture, **m), out, indent=2)
        out.write("\n")
