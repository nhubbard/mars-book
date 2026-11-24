#!/usr/bin/env python3
"""Carry students' completed prior entries into the provided block working format."""

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parent
MARKER = (
    "# Replace these stubs with your own completed prior entries and required data."
)


def component(source, entry):
    if source.startswith(".text\n"):
        source = "\n" + source
    if source.count("\n.text\n") != 1 or source.count("\n" + entry + ":\n") != 1:
        raise ValueError(
            "Use a completed provided-format source with one text boundary and entry"
        )
    data, code = source.split("\n.text\n")
    return data, entry + ":\n" + code.split(entry + ":\n", 1)[1]


def compose(source, transform, quantizer):
    if source.count("\n.text\n") != 1 or source.count(MARKER) != 1:
        raise ValueError(
            "Use the original block working format and keep its dependency marker"
        )
    data, code = source.split("\n.text\n")
    code = code.split(MARKER, 1)[0]
    if code.count("\nh263_encode_block8:\n") != 1:
        raise ValueError("Keep one block entry")
    td, tc = component(transform, "h263_transform8")
    qd, qc = component(quantizer, "h263_quantize8")
    return data + "\n" + td + "\n" + qd + "\n.text\n" + code + "\n" + tc + "\n" + qc


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    p.add_argument("--transform", type=Path, required=True)
    p.add_argument("--quantizer", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    result = compose(
        a.source.read_text(), a.transform.read_text(), a.quantizer.read_text()
    )
    with a.output.open("x") as out:
        out.write(result)
