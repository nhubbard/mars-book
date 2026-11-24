#!/usr/bin/env python3
"""Prepare loader controls while retaining every byte of student instructions."""

import argparse, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def unsigned(value, bits=64):
    if not isinstance(value, str) or re.fullmatch(r"0|[1-9][0-9]*", value) is None:
        raise ValueError("Use canonical unsigned decimal controls")
    result = int(value)
    if result >= 1 << bits:
        raise ValueError("Control exceeds its field width")
    return result


def prepare(source, recipe):
    capacity = unsigned(recipe["capacity"])
    mode = unsigned(recipe["mode"], 32)
    path = recipe["path"]
    if (
        not isinstance(path, str)
        or not path
        or any(ord(c) < 32 or ord(c) > 126 for c in path)
        or len(path) > 127
    ):
        raise ValueError("Use a nonempty printable ASCII path of at most 127 bytes")
    if (
        source.count("\n.text\n") != 1
        or len(re.findall(r"^\.text$", source, re.M)) != 1
    ):
        raise ValueError("Keep the one supplied text-section boundary")
    data, instructions = source.split("\n.text\n")
    raw = path.encode("ascii") + b"\0"
    declaration = "image_path:\n    .byte " + ",".join(str(v) for v in raw) + "\n"
    if len(raw) < 128:
        declaration += "    .space " + str(128 - len(raw)) + "\n"
    declaration += "image_path_end:"
    if (
        len(re.findall(r"^image_path:", data, re.M)) != 1
        or len(re.findall(r"^image_path_end:", data, re.M)) != 1
    ):
        raise ValueError("Keep unique path arena labels")
    data, n = re.subn(
        r"^image_path:.*?^image_path_end:",
        lambda _: declaration,
        data,
        flags=re.M | re.S,
    )
    if n != 1:
        raise ValueError("Keep path arena boundaries")
    for label, directive, value, bits in (
        ("file_capacity", "dword", capacity, 64),
        ("vector_mode", "word", mode, 32),
    ):
        signed = value if value < 1 << (bits - 1) else value - (1 << bits)
        data, n = re.subn(
            r"^(" + label + r": \." + directive + r" )-?[0-9]+([ \t]*(?:#[^\n]*)?)$",
            lambda m: m[1] + str(signed) + m[2],
            data,
            flags=re.M,
        )
        if n != 1:
            raise ValueError("Keep unique typed caller controls")
    return data + "\n.text\n" + instructions


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("recipe")
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    recipes = [
        q
        for q in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if q["id"] == a.recipe
    ]
    if len(recipes) != 1:
        p.error("Choose one exact recipe ID")
    result = prepare(a.source.read_text(), recipes[0])
    with a.output.open("x") as output:
        output.write(result)


if __name__ == "__main__":
    main()
