#!/usr/bin/env python3
"""Attach unfinished row reconstruction without replacing student algorithms."""

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def attach(source):
    old = (ROOT.parent / "png-inflater-construction/wrapper.asm").read_text()
    if source.count(old) != 1 or source.count("\n.text\n") != 1:
        raise ValueError(
            "Keep the supplied inflater wrapper once; reconcile altered wrappers by hand"
        )
    if source.count("\nbuild_tree:\n") != 1 or source.count("\ndecode_symbol:\n") != 1:
        raise ValueError("Attach after the Huffman stage")
    if any(x in source for x in ("png_unfilter:", "row_decode_entry:", "# BEGIN row_")):
        raise ValueError("Row stage or conflicting labels already exist")
    return (
        source.replace(old, (ROOT / "wrapper.asm").read_text(), 1)
        + "\n"
        + (ROOT / "unfilter.asm").read_text()
    )


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    text = attach(a.source.read_text())
    with a.output.open("x") as out:
        out.write(text)


if __name__ == "__main__":
    main()
