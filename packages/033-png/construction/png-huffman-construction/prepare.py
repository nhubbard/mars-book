#!/usr/bin/env python3
"""Select Huffman observations without completing or rewriting student instructions."""

import argparse, importlib.util, json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "inflater_controls", ROOT.parent / "png-inflater-construction/prepare.py"
)
previous = importlib.util.module_from_spec(s)
s.loader.exec_module(previous)


def prepare(source, recipe):
    if source.count("\nbuild_tree:\n") != 1 or source.count("\ndecode_symbol:\n") != 1:
        raise ValueError("Use the attached Huffman-stage source")
    return previous.prepare(source, recipe)


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("recipe")
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    selected = [
        r
        for r in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if r["id"] == a.recipe
    ]
    if len(selected) != 1:
        p.error("Unknown Huffman recipe")
    result = prepare(a.source.read_text(), selected[0])
    with a.output.open("x") as out:
        out.write(result)


if __name__ == "__main__":
    main()
