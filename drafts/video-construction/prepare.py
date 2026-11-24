#!/usr/bin/env python3
"""Prepare kernel data and caller controls while retaining student instructions."""

import argparse, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def inputs(pattern, count):
    if pattern == "worked":
        if count != 5:
            raise ValueError("Worked observation has five words")
        return [0x00112233, 0x00445566, 0x00010203, 0x00ABCDEF, 0], [
            0x00112233,
            0x004455EE,
            0x00010203,
            0x00AACDEF,
            0x00FF0000,
        ]
    if pattern not in ("mixed", "same", "all"):
        raise ValueError("Unknown input pattern")
    left = [(0x10203 * i + 0x123456) & 0xFFFFFF for i in range(count)]
    right = [
        (
            v
            if pattern == "same" or (pattern == "mixed" and i % 2 == 0)
            else v ^ (0xFFFFFF if pattern == "all" else 1 << (i % 24))
        )
        for i, v in enumerate(left)
    ]
    return left, right


def prepare(source, recipe):
    if source.count("\n.text\n") != 1:
        raise ValueError("Keep one supplied text section")
    count = recipe["count"]
    mode = recipe["mode"]
    alias = recipe.get("alias", 0)
    if (
        type(count) is not int
        or not 0 <= count <= 4096
        or type(mode) is not int
        or mode not in (0, 1)
        or type(alias) is not int
        or alias not in (0, 1, 2)
    ):
        raise ValueError("Use bounded kernel controls")
    left, right = inputs(recipe["pattern"], count)
    data, code = source.split("\n.text\n")
    for label, values, next_label in [
        ("left_words", left, "right_words"),
        ("right_words", right, "out_words"),
    ]:
        words = [-1515870811, *values]
        text = (
            label
            + ": .word "
            + ", ".join(str(v) for v in words)
            + "\n    .byte -91:"
            + str(16448 - 4 * len(words))
            + "\n"
        )
        data, n = re.subn(
            r"^" + label + r":.*?(?=^" + next_label + r":)",
            lambda _: text,
            data,
            flags=re.M | re.S,
        )
        if n != 1:
            raise ValueError("Keep input arena labels")
    for label, directive, value in [
        ("kernel_count", "dword", count),
        ("kernel_mode", "word", mode),
        ("kernel_alias", "word", alias),
    ]:
        data, n = re.subn(
            r"^" + label + r": \." + directive + r" -?[0-9]+$",
            lambda _: f"{label}: .{directive} {value}",
            data,
            flags=re.M,
        )
        if n != 1:
            raise ValueError("Keep typed caller controls")
    return data + "\n.text\n" + code


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("recipe")
    p.add_argument("--source", type=Path, default=ROOT / "kernel.asm")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    found = [
        r
        for r in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if r["id"] == a.recipe
    ]
    if len(found) != 1:
        p.error("Unknown kernel recipe")
    result = prepare(a.source.read_text(), found[0])
    with a.output.open("x") as out:
        out.write(result)


if __name__ == "__main__":
    main()
