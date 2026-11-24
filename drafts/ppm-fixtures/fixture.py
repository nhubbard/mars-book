#!/usr/bin/env python3
"""Produce the two-by-two binary fixture described in the native PPM reading."""

import argparse
from pathlib import Path


def ppm_bytes(width, height, raster):
    if (
        type(width) is not int
        or type(height) is not int
        or not 1 <= width <= 64
        or not 1 <= height <= 64
    ):
        raise ValueError("Choose dimensions from 1 through 64")
    if len(raster) != 3 * width * height:
        raise ValueError("Supply exactly three bytes per pixel")
    return f"P6\n{width} {height}\n255\n".encode("ascii") + bytes(raster)


def example():
    return ppm_bytes(2, 2, bytes.fromhex("ff0000 00ff00 0000ff ffffff"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    with args.output.open("xb") as stream:
        stream.write(example())
