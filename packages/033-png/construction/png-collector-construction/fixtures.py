#!/usr/bin/env python3
"""Make named, reproducible chunk observations at fresh paths."""

import argparse, importlib.util, struct, zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent
NAMES = (
    "worked",
    "palette",
    "metadata",
    "bad-crc",
    "idat-gap",
    "critical",
    "reserved",
    "huge-length",
    "bad-width",
)


def material(name):
    spec = importlib.util.spec_from_file_location(
        "png_collector_input", ROOT.parent / "png-fixtures/fixture.py"
    )
    f = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(f)
    raw = f.material()["png"]

    def chunk(kind, payload):
        return (
            struct.pack(">I", len(payload))
            + kind
            + payload
            + struct.pack(">I", zlib.crc32(kind + payload))
        )

    head = raw[:33]
    tail = raw[33:]
    if name == "worked":
        return raw
    if name == "palette":
        return head + chunk(b"PLTE", bytes((10, 20, 30))) + tail
    if name == "metadata":
        return head + chunk(b"tEXt", b"Note\0class") + tail
    if name == "bad-crc":
        return raw[:-1] + bytes((raw[-1] ^ 1,))
    if name == "idat-gap":
        return raw[:46] + chunk(b"tEXt", b"Note\0gap") + raw[46:]
    if name == "critical":
        return head + chunk(b"ABCD", b"") + tail
    if name == "reserved":
        return head + chunk(b"abcd", b"") + tail
    if name == "huge-length":
        return raw[:8] + bytes.fromhex("80000000") + raw[12:]
    if name == "bad-width":
        return raw[:8] + chunk(b"IHDR", bytes(4) + raw[20:29]) + tail
    raise ValueError("Unknown collector fixture")


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("name", choices=NAMES)
    p.add_argument("output", type=Path)
    a = p.parse_args()
    data = material(a.name)
    with a.output.open("xb") as out:
        out.write(data)


if __name__ == "__main__":
    main()
