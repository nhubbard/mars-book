#!/usr/bin/env python3
"""Prepare source colors and encoder controls without replacing instructions."""

import argparse, importlib.util, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("full_encode_input", ROOT / "fixtures.py")
fixtures = importlib.util.module_from_spec(spec)
spec.loader.exec_module(fixtures)


def prepare(source, recipe):
    if source.count("\n.text\n") != 1 or source.count("\nvideo_encode_full:\n") != 1:
        raise ValueError("Keep one full encoder and text section")
    values = [
        recipe.get(k, d)
        for k, d in (
            ("sourceCount", 16384),
            ("capacity", 12304),
            ("sequence", 0),
            ("period", 4),
            ("previousCapacity", 16384),
        )
    ]
    if any(type(v) is not int or not 0 <= v < 2**64 for v in values):
        raise ValueError("Use unsigned 64-bit counts and sequence fields")
    signed = lambda n: n if n < 2**63 else n - 2**64
    count, cap, seq, period, previous = map(signed, values)
    data, code = source.split("\n.text\n")
    data, n = re.subn(
        r"^encode_source:.*?(?=^encode_source_end:)",
        lambda _: fixtures.declaration(recipe.get("fixture", "gradient")),
        data,
        flags=re.M | re.S,
    )
    if n != 1:
        raise ValueError("Keep the source arena markers")
    data, n = re.subn(
        r"^encode_source_count: \.dword -?[0-9]+$",
        lambda _: f"encode_source_count: .dword {count}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep the source count control")
    data, n = re.subn(
        r"^encode_context: \.dword [^\n]+$",
        lambda _: f"encode_context: .dword {cap}, {seq}, {period}, 0, {previous}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep the encoder context input fields")
    return data + "\n.text\n" + code


if __name__ == "__main__":
    p = argparse.ArgumentParser()
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
        p.error("Unknown full-encoder recipe")
    result = prepare(a.source.read_text(), matches[0])
    with a.output.open("x") as out:
        out.write(result)
