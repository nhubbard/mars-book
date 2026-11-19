#!/usr/bin/env python3
"""Create a fixed CBOR teaching input or its flat record image at a fresh path."""
import argparse
from pathlib import Path
import struct

TICKS = 0x0102030405060708
DEVICE = bytes.fromhex('00ff41')
READINGS = (0, 23, 24, -1, -24, -25, 255, 256, 32767, -32768)
TRACE = bytes.fromhex(
    'b80403a201824280ff210080'
    '02980a00181718182037381818ff190100197fff397fff'
    '01580300ff41'
    '18001b0102030405060708')
CANONICAL = bytes.fromhex(
    'a3001b0102030405060708'
    '014300ff41'
    '028a001718182037381818ff190100197fff397fff')
EMPTY = bytes.fromhex('a3000001400280')


def image(ticks, device, readings):
    output = bytearray([0xa5]*176)
    struct.pack_into('<QII', output, 0, ticks, len(device), len(readings))
    output[16:16+len(device)] = device
    for index, value in enumerate(readings):
        struct.pack_into('<h', output, 48+2*index, value)
    return bytes(output)


def material(name):
    if name in ('trace', 'canonical'):
        return (TRACE if name == 'trace' else CANONICAL), image(TICKS, DEVICE, READINGS)
    if name == 'empty':
        return EMPTY, image(0, b'', ())
    if name == 'maximum':
        raw = bytes.fromhex('a3001bffffffffffffffff015820')+bytes(range(32))
        raw += bytes.fromhex('029840')+bytes.fromhex('197fff397fff')*32
        return raw, image(2**64-1, bytes(range(32)), (32767, -32768)*32)
    if name == 'budget':
        raw = bytes.fromhex('a4000001400280039840')
        raw += (bytes.fromhex('5820')+bytes(range(32)))*28
        raw += bytes.fromhex('5819')+bytes(25)+b'\x40'*35
        return raw, image(0, b'', ())
    raise ValueError('Choose trace, canonical, empty, maximum or budget')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('output', type=Path)
    parser.add_argument('--fixture', choices=['trace','canonical','empty','maximum','budget'], default='trace')
    parser.add_argument('--record', action='store_true', help='Write the flat 176-byte record instead of CBOR')
    args = parser.parse_args()
    raw, record = material(args.fixture)
    with args.output.open('xb') as stream:
        stream.write(record if args.record else raw)
