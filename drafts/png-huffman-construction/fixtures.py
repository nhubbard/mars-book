#!/usr/bin/env python3
"""Encode small named DEFLATE observations; no decompressor or assembly solution."""

import argparse, importlib.util, struct, zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent
ORDER = (16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15)
FIXED = [8] * 144 + [9] * 112 + [7] * 24 + [8] * 8
CL = [0] * 19
for symbol, length in ((0, 1), (1, 2), (2, 3), (16, 4), (17, 5), (18, 5)):
    CL[symbol] = length
NAMES = (
    "fixed-literals",
    "fixed-match",
    "dynamic-literals",
    "repeat16",
    "repeat17",
    "repeat18",
    "bad-distance",
    "reserved-literal",
    "reserved-distance",
    "bad-repeat",
    "bad-tree",
    "missing-end",
    "bad-adler",
    "mixed",
)


class Writer:
    def __init__(self):
        self.bits = []

    def field(self, value, count):
        self.bits.extend((value >> i) & 1 for i in range(count))

    def code(self, symbol, lengths):
        counts = [lengths.count(i) for i in range(16)]
        next_code = [0] * 16
        for n in range(1, 16):
            next_code[n] = (next_code[n - 1] + (counts[n - 1] if n > 1 else 0)) * 2
        for value, length in enumerate(lengths):
            if length:
                code = next_code[length]
                next_code[length] += 1
                if value == symbol:
                    self.bits.extend((code >> i) & 1 for i in range(length - 1, -1, -1))
                    return
        raise ValueError("Cannot encode absent symbol")

    def bytes(self):
        raw = bytearray((len(self.bits) + 7) // 8)
        for i, bit in enumerate(self.bits):
            raw[i // 8] |= bit << (i % 8)
        return bytes(raw)


def chunk(kind, data):
    return (
        struct.pack(">I", len(data))
        + kind
        + data
        + struct.pack(">I", zlib.crc32(kind + data))
    )


def material(name):
    if name not in NAMES:
        raise ValueError("Unknown Huffman fixture")
    spec = importlib.util.spec_from_file_location(
        "huffman_pixels", ROOT.parent / "png-fixtures/fixture.py"
    )
    f = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(f)
    worked = f.material()
    plain = worked["filtered"] if name in ("fixed-literals", "mixed") else bytes(35)
    w = Writer()
    if name in (
        "fixed-literals",
        "fixed-match",
        "bad-distance",
        "reserved-literal",
        "reserved-distance",
        "mixed",
    ):
        w.field(1, 1)
        w.field(1, 2)
        if name in ("fixed-literals", "mixed"):
            for value in (plain[7:] if name == "mixed" else plain):
                w.code(value, FIXED)
        elif name == "reserved-literal":
            w.code(286, FIXED)
        else:
            w.code(0, FIXED)
            w.code(272, FIXED)
            w.field(3, 2)
            w.code(
                (
                    30
                    if name == "reserved-distance"
                    else 1 if name == "bad-distance" else 0
                ),
                [5] * 32,
            )
        w.code(256, FIXED)
        body = w.bytes()
        if name == "mixed":
            body = bytes((0,)) + struct.pack("<HH", 7, 65528) + plain[:7] + body
    else:
        lengths = [0] * 258
        lengths[0] = 1
        lengths[256] = lengths[257] = 2
        if name == "bad-tree":
            lengths[1] = 1
        if name == "missing-end":
            lengths[256] = 0
            lengths[257] = 1
        distance = [1]
        w.field(1, 1)
        w.field(2, 2)
        w.field(1, 5)
        w.field(0, 5)
        w.field(14, 4)
        for symbol in ORDER[:18]:
            w.field(CL[symbol], 3)
        vector = lengths + distance
        if name == "repeat16":
            tokens = [1, 0, (16, 3)] + [0] * 248 + [2, 2, 1]
        elif name == "repeat17":
            tokens = [1, (17, 7)] + [0] * 245 + [2, 2, 1]
        elif name == "repeat18":
            tokens = [1, (18, 127)] + [0] * 117 + [2, 2, 1]
        elif name == "bad-repeat":
            tokens = [(16, 0)]
        else:
            tokens = vector
        for token in tokens:
            if isinstance(token, int):
                w.code(token, CL)
            else:
                w.code(token[0], CL)
                w.field(token[1], {16: 2, 17: 3, 18: 7}[token[0]])
        if name not in ("bad-repeat", "bad-tree", "missing-end"):
            for value in plain:
                w.code(value, lengths)
            w.code(256, lengths)
        body = w.bytes()
    stream = bytes.fromhex("7801") + body + struct.pack(">I", zlib.adler32(plain))
    if name == "bad-adler":
        stream = stream[:-1] + bytes((stream[-1] ^ 1,))
    png = (
        worked["png"][:33]
        + chunk(b"IDAT", stream[:1])
        + chunk(b"IDAT", stream[1:])
        + chunk(b"IEND", b"")
    )
    return dict(png=png, zlib=stream, plain=plain)


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("name", choices=NAMES)
    p.add_argument("output", type=Path)
    p.add_argument("--kind", choices=("png", "zlib"), default="png")
    a = p.parse_args()
    raw = material(a.name)[a.kind]
    with a.output.open("xb") as out:
        out.write(raw)


if __name__ == "__main__":
    main()
