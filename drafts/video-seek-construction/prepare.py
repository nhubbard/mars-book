#!/usr/bin/env python3
"""Change seek target/capacities while preserving student instruction text."""

import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def prepare(source, recipe):
    if source.count("\n.text\n") != 1 or source.count("\nvideo_seek:\n") != 1:
        raise ValueError("Keep one seeker and text section")
    values = [
        recipe.get("target", 6),
        recipe.get("frameCapacity", 16384),
        recipe.get("scratchCapacity", 16384),
    ]
    if any(type(n) is not int or not 0 <= n < 2**64 for n in values):
        raise ValueError("Use unsigned 64-bit target and capacities")
    signed = lambda n: n if n < 2**63 else n - 2**64
    data, code = source.split("\n.text\n")
    target, frame, scratch = values
    data, n = re.subn(
        r"^seek_target: \.dword -?[0-9]+$",
        lambda _: f"seek_target: .dword {signed(target)}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep the target control")
    data, n = re.subn(
        r"^seek_context: \.dword [^\n]+$",
        lambda _: f"seek_context: .dword 0, 0, 0, 0, {signed(frame)}, 0, {signed(scratch)}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep the seek context input declaration")
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
        p.error("Unknown seek recipe")
    result = prepare(a.source.read_text(), matches[0])
    with a.output.open("x") as out:
        out.write(result)
