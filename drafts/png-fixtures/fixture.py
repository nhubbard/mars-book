#!/usr/bin/env python3
"""Generate bounded RGB8 PNG teaching inputs, not a decoder implementation."""

import argparse
from pathlib import Path
import struct
import zlib

SIGNATURE = bytes.fromhex("89504e470d0a1a0a")
TRACE_RGB = bytes(v for row in range(5) for v in range(10 + 5 * row, 61 + 5 * row, 10))
TRACE_FILTERED = bytes.fromhex(
    "000a141e28323c" "02050505050505" "01141e281e1e1e" "030f1419121212" "04050505050505"
)


def samples(width, height):
    if not 1 <= width <= 512 or not 1 <= height <= 512:
        raise ValueError("dimensions must lie in 1..512")
    if (width, height) == (2, 5):
        return TRACE_RGB
    return bytes(
        (x * 37 + y * 53 + c * 97 + (x * y) % 251) & 255
        for y in range(height)
        for x in range(width)
        for c in range(3)
    )


def predictor(kind, a, b, c):
    if kind == 0:
        return 0
    if kind == 1:
        return a
    if kind == 2:
        return b
    if kind == 3:
        return (a + b) // 2
    estimate = a + b - c
    distances = (abs(estimate - a), abs(estimate - b), abs(estimate - c))
    return (a, b, c)[distances.index(min(distances))]


def filtered(width, height, rgb):
    if len(rgb) != 3 * width * height:
        raise ValueError("sample count does not match dimensions")
    stride = 3 * width
    result = bytearray()
    # First five rows deliberately exercise None, Up, Sub, Average, Paeth.
    filters = (0, 2, 1, 3, 4)
    for row in range(height):
        kind = filters[row % 5]
        result.append(kind)
        for column in range(stride):
            at = row * stride + column
            a = rgb[at - 3] if column >= 3 else 0
            b = rgb[at - stride] if row else 0
            c = rgb[at - stride - 3] if row and column >= 3 else 0
            result.append((rgb[at] - predictor(kind, a, b, c)) & 255)
    return bytes(result)


def chunk(kind, payload):
    return (
        struct.pack(">I", len(payload))
        + kind
        + payload
        + struct.pack(">I", zlib.crc32(kind + payload))
    )


def material(width=2, height=5, compression="stored", split=1):
    rgb = samples(width, height)
    rows = filtered(width, height, rgb)
    strategies = {
        "stored": (0, zlib.Z_DEFAULT_STRATEGY),
        "fixed": (6, zlib.Z_FIXED),
        "auto": (6, zlib.Z_DEFAULT_STRATEGY),
    }
    level, strategy = strategies[compression]
    encoder = zlib.compressobj(level, zlib.DEFLATED, 15, 8, strategy)
    stream = encoder.compress(rows) + encoder.flush()
    if not 0 <= split <= len(stream):
        raise ValueError("IDAT split must lie within the zlib stream")
    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    png = SIGNATURE + chunk(b"IHDR", header)
    png += chunk(b"IDAT", stream[:split]) + chunk(b"IDAT", stream[split:])
    png += chunk(b"IEND", b"")
    words = b"".join(
        bytes((b, g, r, 0)) for r, g, b in zip(rgb[::3], rgb[1::3], rgb[2::3])
    )
    return {"png": png, "zlib": stream, "filtered": rows, "rgb": rgb, "words": words}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path)
    parser.add_argument("--width", type=int, default=2)
    parser.add_argument("--height", type=int, default=5)
    parser.add_argument(
        "--compression", choices=("stored", "fixed", "auto"), default="stored"
    )
    parser.add_argument("--split", type=int, default=1)
    parser.add_argument(
        "--part", choices=("png", "zlib", "filtered", "rgb", "words"), default="png"
    )
    args = parser.parse_args()
    try:
        data = material(args.width, args.height, args.compression, args.split)[
            args.part
        ]
        with args.output.open("xb") as output:
            output.write(data)
    except (ValueError, FileExistsError) as error:
        parser.exit(2, str(error) + "\n")
    print(f"{args.part}: {len(data)} bytes")


if __name__ == "__main__":
    main()
