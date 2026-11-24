#!/usr/bin/env python3
"""Carry four completed dependency groups into the student's picture working source."""

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parent
MARKER = (
    "# Replace these four dependency groups with your own completed entries and data."
)
ENTRIES = (
    "h263_search16",
    "h263_predict_mb",
    "h263_encode_block8",
    "h263_emit_macroblock",
)


def component(source, entry):
    if source.startswith(".text\n"):
        source = "\n" + source
    if source.count("\n.text\n") != 1 or source.count("\n" + entry + ":\n") != 1:
        raise ValueError("Keep one text boundary and one completed dependency entry")
    data, code = source.split("\n.text\n")
    return data, entry + ":\n" + code.split(entry + ":\n", 1)[1]


def compose(source, dependencies):
    if (
        len(dependencies) != 4
        or source.count("\n.text\n") != 1
        or source.count(MARKER) != 1
    ):
        raise ValueError(
            "Supply the picture working format and all four dependency groups"
        )
    data, code = source.split("\n.text\n")
    code = code.split(MARKER, 1)[0]
    if code.count("\nh263_encode_picture:\n") != 1:
        raise ValueError("Keep one picture entry")
    parts = [component(s, e) for s, e in zip(dependencies, ENTRIES)]
    return (
        data
        + "\n"
        + "\n".join(d for d, c in parts)
        + "\n.text\n"
        + code
        + "\n"
        + "\n".join(c for d, c in parts)
    )


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    for name in ("search", "prediction", "block", "syntax"):
        p.add_argument("--" + name, type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    text = compose(
        a.source.read_text(),
        [
            getattr(a, n).read_text()
            for n in ("search", "prediction", "block", "syntax")
        ],
    )
    with a.output.open("x") as out:
        out.write(text)
