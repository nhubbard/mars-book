#!/usr/bin/env python3
"""Wrap student-produced RGB8 filtered scanlines in a bounded PNG file."""

import argparse
from pathlib import Path
import struct
import zlib


def chunk(kind, payload):
    return (
        struct.pack(">I", len(payload))
        + kind
        + payload
        + struct.pack(">I", zlib.crc32(kind + payload))
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("rows", type=Path, help="binary selectors and residual bytes")
    parser.add_argument("output", type=Path)
    parser.add_argument("--width", type=int, required=True)
    parser.add_argument("--height", type=int, required=True)
    parser.add_argument(
        "--compression", choices=["stored", "fixed", "auto"], default="auto"
    )
    args = parser.parse_args()
    if not 1 <= args.width <= 512 or not 1 <= args.height <= 512:
        parser.error("width and height must each be in 1..512")
    count = args.height * (1 + 3 * args.width)
    with args.rows.open("rb") as source:
        rows = source.read(count + 1)
    if len(rows) != count:
        parser.error("supply exactly height * (1 + 3 * width) binary row bytes")
    if any(rows[y * (1 + 3 * args.width)] > 4 for y in range(args.height)):
        parser.error("every row selector must be in 0..4")
    level, strategy = {
        "stored": (0, zlib.Z_DEFAULT_STRATEGY),
        "fixed": (6, zlib.Z_FIXED),
        "auto": (6, zlib.Z_DEFAULT_STRATEGY),
    }[args.compression]
    compressor = zlib.compressobj(level, zlib.DEFLATED, 15, 8, strategy)
    stream = compressor.compress(rows) + compressor.flush()
    header = struct.pack(">IIBBBBB", args.width, args.height, 8, 2, 0, 0, 0)
    image = (
        bytes.fromhex("89504e470d0a1a0a")
        + chunk(b"IHDR", header)
        + chunk(b"IDAT", stream[:1])
        + chunk(b"IDAT", stream[1:])
        + chunk(b"IEND", b"")
    )
    if len(image) > 1048576:
        parser.error("compressed file exceeds the decoder profile")
    with args.output.open("xb") as output:
        output.write(image)
    print(f"PNG: {args.width} x {args.height}, {len(image)} bytes")


if __name__ == "__main__":
    main()
