#!/usr/bin/env python3
"""Carry a student's completed coefficient emitter, writer, data and helpers into this stage."""

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parent
MARKER = "# Replace both dependency stubs and carry their required data/helpers."


def compose(source, dependency):
    if dependency.startswith(".text\n"):
        dependency = "\n" + dependency
    if (
        source.count("\n.text\n") != 1
        or source.count(MARKER) != 1
        or dependency.count("\n.text\n") != 1
        or dependency.count("\nh263_emit_coefficients:\n") != 1
    ):
        raise ValueError(
            "Use the provided working formats and retain the macroblock dependency marker"
        )
    data, code = source.split("\n.text\n")
    dd, dc = dependency.split("\n.text\n")
    return (
        data
        + "\n"
        + dd
        + "\n.text\n"
        + code.split(MARKER, 1)[0]
        + "\nh263_emit_coefficients:\n"
        + dc.split("h263_emit_coefficients:\n", 1)[1]
    )


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    p.add_argument("--coefficients", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    result = compose(a.source.read_text(), a.coefficients.read_text())
    with a.output.open("x") as out:
        out.write(result)
