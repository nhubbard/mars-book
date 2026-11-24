#!/usr/bin/env python3
"""Attach the next scaffold without completing or replacing a student's loader."""

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def attach(source):
    original = (ROOT.parent / "png-construction/main.asm").read_text()
    tail = "png_decode:\n" + original.split("png_decode:\n", 1)[1]
    if source.count(tail) != 1 or source.count("\n.text\n") != 1:
        raise ValueError("Keep the original two decoder stubs once; use a fresh output")
    if any(
        label in source
        for label in ("png_collect:", "collector_call:", "# BEGIN collect_")
    ):
        raise ValueError("Collector scaffold is already present or labels conflict")
    replacement = (
        (ROOT / "wrapper.asm").read_text() + "\n" + (ROOT / "collector.asm").read_text()
    )
    if "collector_capacity:" in source:
        raise ValueError("Collector controls already exist")
    result = source.replace(
        "\n.text\n", "\n.align 3\ncollector_capacity: .dword 46\n.text\n", 1
    )
    return result.replace(tail, replacement, 1)


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    text = attach(a.source.read_text())
    with a.output.open("x") as stream:
        stream.write(text)


if __name__ == "__main__":
    main()
