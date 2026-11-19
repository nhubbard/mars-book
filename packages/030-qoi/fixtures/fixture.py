#!/usr/bin/env python3
"""Write the fixed QOI teaching fixtures; this is not an assembly codec."""
import argparse
from pathlib import Path

ROWS = bytes.fromhex('16 3c 7e db ff 24 42 81')
TRACE = bytes.fromhex('716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001')
SPRITE = bytes.fromhex('716f696600000008000000080400c2553526c035c126c235c126c43526c03526c03526c835c02635c02635c12635c226352635c4260000000000000001')
TRACE_RGBA = bytes.fromhex('000000ff ff0000ff ff0000ff 000000ff 010101ff 070605ff 07060580 010101ff')


def material(name):
    if name == 'trace':
        return 8, 1, TRACE_RGBA, TRACE
    if name == 'sprite':
        pixels = bytes(component for row in ROWS for shift in range(7, -1, -1)
                       for component in ((255, 255, 255, 255) if row & (1 << shift) else (0, 0, 0, 255)))
        return 8, 8, pixels, SPRITE
    raise ValueError('Choose trace or sprite')


def fixture(name, format='qoi'):
    width, height, pixels, encoded = material(name)
    if format == 'qoi':
        return encoded
    if format == 'ppm':
        return f'P6\n{width} {height}\n255\n'.encode('ascii') + bytes(
            component for i in range(0, len(pixels), 4) for component in pixels[i:i+3])
    raise ValueError('Choose qoi or ppm')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('output', type=Path)
    parser.add_argument('--fixture', choices=['trace', 'sprite'], default='sprite')
    parser.add_argument('--format', choices=['qoi', 'ppm'], default='qoi')
    args = parser.parse_args()
    with args.output.open('xb') as stream:
        stream.write(fixture(args.fixture, args.format))
