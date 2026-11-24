#!/usr/bin/env python3
"""Select stored-stage observation controls while preserving all instruction edits."""

import argparse, importlib.util, json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location(
    "previous_collector_controls", ROOT.parent / "png-collector-construction/prepare.py"
)
previous = importlib.util.module_from_spec(spec)
spec.loader.exec_module(previous)


def prepare(source, recipe):
    if source.count("\npng_inflate:\n") != 1:
        raise ValueError("Use the attached inflater-stage source")
    return previous.prepare(source, recipe)


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("recipe")
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    recipes = json.loads((ROOT / "inputs.json").read_text())["recipes"]
    selected = [r for r in recipes if r["id"] == a.recipe]
    if len(selected) != 1:
        p.error("Unknown stored-stage recipe")
    text = prepare(a.source.read_text(), selected[0])
    with a.output.open("x") as out:
        out.write(text)


if __name__ == "__main__":
    main()
