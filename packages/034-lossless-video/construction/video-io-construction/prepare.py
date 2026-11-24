#!/usr/bin/env python3
"""Prepare file-loader path/capacity controls without editing student instructions."""

import argparse, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def prepare(source, recipe):
    if source.count("\n.text\n") != 1 or source.count("\nvideo_load:\n") != 1:
        raise ValueError("Keep one loader entry and text section")
    cap = recipe["capacity"]
    if type(cap) is not int or not 0 <= cap < 2**64:
        raise ValueError("Use an unsigned 64-bit capacity")
    path = recipe.get("path", "animation.mlv")
    if not isinstance(path, str) or "\0" in path:
        raise ValueError("Use a NUL-free path")
    raw = path.encode("utf-8") + b"\0"
    if len(raw) > 128:
        raise ValueError("Path exceeds its 128-byte arena")
    data, code = source.split("\n.text\n")
    decl = (
        "io_path: .byte "
        + ", ".join(str(v if v < 128 else v - 256) for v in raw)
        + "\n    .space "
        + str(128 - len(raw))
        + "\n"
    )
    data, n = re.subn(
        r"^io_path:.*?(?=^io_path_end:)", lambda _: decl, data, flags=re.M | re.S
    )
    if n != 1:
        raise ValueError("Keep the path arena labels")
    data, n = re.subn(
        r"^io_capacity: \.dword -?[0-9]+$",
        lambda _: f"io_capacity: .dword {cap if cap<2**63 else cap-2**64}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep one loader capacity")
    return data + "\n.text\n" + code


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("recipe")
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    matches = [
        r
        for r in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if r["id"] == a.recipe
    ]
    if len(matches) != 1:
        p.error("Unknown loader recipe")
    result = prepare(a.source.read_text(), matches[0])
    with a.output.open("x") as out:
        out.write(result)
