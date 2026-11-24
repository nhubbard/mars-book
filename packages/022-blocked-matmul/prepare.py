#!/usr/bin/env python3
"""Prepare one published matrix observation without overwriting student source."""

import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def words(values):
    return "".join(
        "    .word " + ",".join(map(str, values[i : i + 8])) + "\n"
        for i in range(0, len(values), 8)
    )


def prepare(source, recipe):
    m, k, n, t = recipe["shape"]
    if (
        not all(isinstance(v, int) and 0 <= v <= 16 for v in (m, k, n))
        or not isinstance(t, int)
        or not 1 <= t <= 8
    ):
        raise ValueError("Use dimensions 0..16 and tile edge 1..8")
    if recipe["method"] not in (0, 1):
        raise ValueError("Choose scalar method zero or tiled method one")
    for label, size, values, active in [
        ("left", 272, recipe["left"], m * k),
        ("right", 272, recipe["right"], k * n),
    ]:
        if len(values) != active or not all(
            isinstance(v, int) and -32768 <= v <= 32767 for v in values
        ):
            raise ValueError(
                "Provide exactly the selected signed-16-bit-valued input words"
            )
        begin, end = label + ":", label + "_end:"
        if source.count(begin) != 1 or source.count(end) != 1:
            raise ValueError("Use the supplied complete arena named " + label)
        a = source.index(begin) + len(begin)
        b = source.index(end, a)
        source = (
            source[:a] + "\n" + words(values + [0] * (size - len(values))) + source[b:]
        )
    for label, old, new in [
        ("shape", "2,3,2,2", ",".join(map(str, recipe["shape"]))),
        ("method", "0", str(recipe["method"])),
    ]:
        line = label + ": .word " + old + "\n"
        if source.count(line) != 1:
            raise ValueError(
                "Prepare from a cumulative program with the default " + label + " line"
            )
        source = source.replace(line, label + ": .word " + new + "\n", 1)
    return source


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("recipe")
    parser.add_argument("--source", type=Path, default=ROOT / "starter/main.asm")
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    selected = [
        q
        for q in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if q["id"] == args.recipe
    ]
    if len(selected) != 1:
        parser.error("Choose a published recipe ID from inputs.json")
    if args.source.resolve() == args.output.resolve():
        parser.error("Write a separate observation file to preserve your source")
    text = prepare(args.source.read_text(), selected[0])
    with args.output.open("x") as output:
        output.write(text)
    print(args.output)


if __name__ == "__main__":
    main()
