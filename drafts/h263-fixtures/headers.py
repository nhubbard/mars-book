#!/usr/bin/env python3
"""Inspect only baseline picture headers, not macroblock or coefficient conformance."""

import argparse
import json
from pathlib import Path


def bits(data, start, width):
    if start < 0 or width < 0 or start + width > len(data) * 8:
        raise ValueError("Truncated picture header")
    value = 0
    for offset in range(start, start + width):
        value = (value << 1) | ((data[offset // 8] >> (7 - offset % 8)) & 1)
    return value


def inspect(data):
    positions = [
        at
        for at in range(len(data) - 2)
        if data[at : at + 2] == b"\0\0" and data[at + 2] & 252 == 128
    ]
    if not positions or positions[0] != 0:
        raise ValueError("The elementary stream must start with a picture start code")
    result = []
    for at in positions:
        start = at * 8
        ptype = bits(data, start + 30, 13)
        if ptype >> 11 != 2:
            raise ValueError("Invalid baseline marker/id bits")
        if (ptype >> 5) & 7 != 2:
            raise ValueError("Only baseline QCIF headers are accepted here")
        if ptype & 15:
            raise ValueError(
                "Optional baseline coding modes are outside this exercise profile"
            )
        if (ptype >> 8) & 7:
            raise ValueError(
                "Display/freeze indicators are outside this exercise profile"
            )
        quant = bits(data, start + 43, 5)
        if quant == 0 or bits(data, start + 48, 1) or bits(data, start + 49, 1):
            raise ValueError(
                "Invalid quantizer or unsupported multipoint/extra information"
            )
        result.append(
            dict(
                offset=at,
                temporalReference=bits(data, start + 22, 8),
                pictureType="P" if (ptype >> 4) & 1 else "I",
                quantizer=quant,
            )
        )
    if result[0]["pictureType"] != "I":
        raise ValueError("The first picture must be independently coded")
    return result


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("stream", type=Path)
    a = p.parse_args()
    try:
        print(
            json.dumps(
                dict(
                    scope="Baseline picture headers only; no macroblock/transform conformance claim",
                    pictures=inspect(a.stream.read_bytes()),
                ),
                indent=2,
            )
        )
    except ValueError as error:
        p.error(str(error))
