#!/usr/bin/env python3
"""Change complete-file observation data while retaining all student instructions."""

import argparse, importlib.util, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "index_observation_inputs", ROOT / "fixtures.py"
)
fixtures = importlib.util.module_from_spec(s)
s.loader.exec_module(fixtures)
MAX_FILE = 7344152


def unsigned(v):
    if type(v) is not int or not 0 <= v < 2**64:
        raise ValueError("Use unsigned integer controls")
    return v


def prepare(source, recipe):
    if source.count("\n.text\n") != 1 or source.count("\nvideo_index:\n") != 1:
        raise ValueError("Keep one index entry and text section")
    m = fixtures.material(recipe["fixture"], recipe.get("case", "valid"))
    raw = m["file"]
    data, code = source.split("\n.text\n")
    parts = ["index_file_data: .byte -91, -91, -91"]
    for at in range(0, len(raw), 32):
        parts.append(
            "    .byte "
            + ", ".join(str(b if b < 128 else b - 256) for b in raw[at : at + 32])
        )
    parts += ["    .space " + str(MAX_FILE - len(raw)), "    .byte -91:61", ".align 3"]
    data, n = re.subn(
        r"^index_file_data:.*?(?=^index_directory:)",
        lambda _: "\n".join(parts) + "\n",
        data,
        flags=re.M | re.S,
    )
    if n != 1:
        raise ValueError("Keep file and directory arenas")
    fields = [
        unsigned(recipe.get("capacity", 2048)),
        0,
        unsigned(recipe.get("scratchCapacity", 16384)),
    ] + [0xA5A5A5A5A5A5A5A5] * 7
    signed = lambda v: v if v < 2**63 else v - 2**64
    data, n = re.subn(
        r"^index_context: \.dword [^\n]+$",
        lambda _: "index_context: .dword " + ", ".join(str(signed(v)) for v in fields),
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep one index context")
    count = unsigned(recipe.get("declaredCount", len(raw)))
    data, n = re.subn(
        r"^index_file_count: \.dword -?[0-9]+$",
        lambda _: f"index_file_count: .dword {signed(count)}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep one index count")
    return data + "\n.text\n" + code


def main():
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
        p.error("Unknown index recipe")
    result = prepare(a.source.read_text(), matches[0])
    with a.output.open("x") as out:
        out.write(result)


if __name__ == "__main__":
    main()
