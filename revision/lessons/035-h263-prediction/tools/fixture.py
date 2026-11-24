#!/usr/bin/env python3
"""Generate deterministic raw QCIF planes; no encoder or external image dependency."""

import argparse
import hashlib
import json
from fractions import Fraction
from pathlib import Path

WIDTH, HEIGHT = 176, 144
CHROMA_WIDTH, CHROMA_HEIGHT = 88, 72
Y_BYTES = WIDTH * HEIGHT
C_BYTES = CHROMA_WIDTH * CHROMA_HEIGHT
FRAME_BYTES = Y_BYTES + 2 * C_BYTES
CLOCK = Fraction(30000, 1001)
DEFAULT_FRAMES, DEFAULT_TICK_STEP = 8, 6


def validate(frames, tick_step):
    if type(frames) is not int or type(tick_step) is not int:
        raise ValueError("Frame count and tick step must be integers")
    if not 1 <= frames <= 299 or not 1 <= tick_step <= 255:
        raise ValueError("Use 1..299 pictures and 1..255 clock ticks per picture")
    if frames * tick_step * 1001 > 300000:
        raise ValueError(
            "The complete clip, including the last picture hold, must not exceed ten seconds"
        )


def phase(index):
    if type(index) is not int or index < 0:
        raise ValueError("Picture index must be a nonnegative integer")
    return index - index // 6


def frame(index):
    step = phase(index)
    left = 16 + 2 * (step % 56)
    top = 32 + 2 * (step % 16)
    y = bytearray(
        32 + 12 * ((x // 8 + 3 * (row // 8)) % 12)
        for row in range(HEIGHT)
        for x in range(WIDTH)
    )
    cb = bytearray([128]) * C_BYTES
    cr = bytearray([128]) * C_BYTES
    for row in range(top, top + 32):
        y[row * WIDTH + left : row * WIDTH + left + 32] = bytes([200]) * 32
    for row in range(top // 2, top // 2 + 16):
        start = row * CHROMA_WIDTH + left // 2
        cb[start : start + 16] = bytes([96]) * 16
        cr[start : start + 16] = bytes([176]) * 16
    return bytes(y + cb + cr)


def metadata(frames=DEFAULT_FRAMES, tick_step=DEFAULT_TICK_STEP):
    validate(frames, tick_step)
    rate = CLOCK / tick_step
    duration = Fraction(frames, 1) / rate
    return dict(
        formatVersion=1,
        fixture="moving-square-qcif",
        width=WIDTH,
        height=HEIGHT,
        pixelFormat="yuv420p",
        planeOrder=["Y", "Cb", "Cr"],
        sampleBits=8,
        byteOrder="Not applicable to single-byte samples",
        planes=[
            dict(
                name="Y",
                offset=0,
                width=WIDTH,
                height=HEIGHT,
                stride=WIDTH,
                bytes=Y_BYTES,
            ),
            dict(
                name="Cb",
                offset=Y_BYTES,
                width=CHROMA_WIDTH,
                height=CHROMA_HEIGHT,
                stride=CHROMA_WIDTH,
                bytes=C_BYTES,
            ),
            dict(
                name="Cr",
                offset=Y_BYTES + C_BYTES,
                width=CHROMA_WIDTH,
                height=CHROMA_HEIGHT,
                stride=CHROMA_WIDTH,
                bytes=C_BYTES,
            ),
        ],
        rowOrder="Top to bottom; samples left to right; no padding or file header",
        frameBytes=FRAME_BYTES,
        frames=frames,
        bytes=frames * FRAME_BYTES,
        nominalRange=dict(Y=[16, 235], Cb=[16, 240], Cr=[16, 240]),
        chromaSampling="4:2:0",
        sampleAspectRatio=[12, 11],
        clock=dict(numerator=30000, denominator=1001),
        tickStep=tick_step,
        frameRate=dict(numerator=rate.numerator, denominator=rate.denominator),
        duration=dict(
            numerator=duration.numerator,
            denominator=duration.denominator,
            definition="One tickStep hold for every supplied picture, including the last",
        ),
        temporalReferences=[index * tick_step % 256 for index in range(frames)],
        pattern="Stationary eight-sample tiled luma; 32-by-32 bright square with two chroma planes; picture 6 repeats picture 5, then the pattern repeats every sixth picture",
        frameSha256=[
            hashlib.sha256(frame(index)).hexdigest() for index in range(frames)
        ],
    )


def write(directory, frames=DEFAULT_FRAMES, tick_step=DEFAULT_TICK_STEP):
    description = metadata(frames, tick_step)
    directory.mkdir(parents=True, exist_ok=False)
    digest = hashlib.sha256()
    with (directory / "clip.yuv").open("xb") as output:
        for index in range(frames):
            pixels = frame(index)
            output.write(pixels)
            digest.update(pixels)
    description["file"] = "clip.yuv"
    description["sha256"] = digest.hexdigest()
    (directory / "clip.json").write_text(json.dumps(description, indent=2) + "\n")
    return description


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--frames", type=int, default=DEFAULT_FRAMES)
    parser.add_argument("--tick-step", type=int, default=DEFAULT_TICK_STEP)
    args = parser.parse_args()
    try:
        result = write(args.output_dir, args.frames, args.tick_step)
    except (ValueError, FileExistsError) as error:
        parser.error(str(error))
    print(result["file"], result["bytes"], "bytes;", result["frames"], "QCIF pictures")
