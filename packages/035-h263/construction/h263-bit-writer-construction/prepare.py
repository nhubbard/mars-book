#!/usr/bin/env python3
"""Change two supplied fields while retaining every student instruction."""

import argparse
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def prepare(source, first=5, first_width=3, second=19, second_width=5, capacity=8):
    for value, width in [(first, first_width), (second, second_width)]:
        if (
            type(value) is not int
            or type(width) is not int
            or not 0 <= width <= 32
            or not 0 <= value < (1 << width)
        ):
            raise ValueError("Use unsigned fields fitting widths zero through 32")
    if type(capacity) is not int or not 0 <= capacity <= 8:
        raise ValueError("Choose capacity 0..8 for the provided eight-byte arena")
    if source.count("\n.text\n") != 1:
        raise ValueError("Keep one provided text-section boundary")
    data, code = source.split("\n.text\n")
    for label, values in [
        ("writer_first", (first, first_width)),
        ("writer_second", (second, second_width)),
    ]:
        data, n = re.subn(
            r"^" + label + r": \.dword [0-9]+, [0-9]+$",
            lambda _, label=label, values=values: f"{label}: .dword {values[0]}, {values[1]}",
            data,
            flags=re.M,
        )
        if n != 1:
            raise ValueError("Keep one typed declaration per field")
    data, n = re.subn(
        r"^writer_context: \.dword 0, [0-8], 0$",
        lambda _: f"writer_context: .dword 0, {capacity}, 0",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep one provided context declaration")
    return data + "\n.text\n" + code


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    for name, value in [
        ("first", 5),
        ("first-width", 3),
        ("second", 19),
        ("second-width", 5),
        ("capacity", 8),
    ]:
        p.add_argument("--" + name, type=int, default=value)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    result = prepare(
        a.source.read_text(),
        a.first,
        a.first_width,
        a.second,
        a.second_width,
        a.capacity,
    )
    with a.output.open("x") as out:
        out.write(result)
