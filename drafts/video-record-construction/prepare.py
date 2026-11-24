#!/usr/bin/env python3
"""Prepare record/frame data without replacing any student instruction text."""

import argparse, importlib.util, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "record_observation_inputs", ROOT / "fixtures.py"
)
fixtures = importlib.util.module_from_spec(s)
s.loader.exec_module(fixtures)


def unsigned(value):
    if type(value) is not int or not 0 <= value < 2**64:
        raise ValueError("Use unsigned integer controls")
    return value


def prepare(source, recipe):
    if source.count("\n.text\n") != 1 or source.count("\nvideo_apply_record:\n") != 1:
        raise ValueError("Keep one supplied record entry and text section")
    m = fixtures.material(
        recipe["fixture"], recipe["index"], recipe.get("case", "valid")
    )
    raw = m["record"]
    data, code = source.split("\n.text\n")
    bytes_list = [165] * 3 + list(raw)
    decl = (
        "record_data: .byte "
        + ", ".join(str(v if v < 128 else v - 256) for v in bytes_list)
        + "\n    .byte -91:"
        + str(28752 - len(bytes_list))
        + "\n.align 4\n"
    )
    data, n = re.subn(
        r"^record_data:.*?(?=^frame_words:)", lambda _: decl, data, flags=re.M | re.S
    )
    if n != 1:
        raise ValueError("Keep the record and frame arena labels")
    values = [-1515870811] + [
        int.from_bytes(m["before"][i : i + 3], "big") for i in range(0, 12288, 3)
    ]
    lines = ["frame_words:"]
    for start in range(0, len(values), 16):
        lines.append(
            "    .word " + ", ".join(str(v) for v in values[start : start + 16])
        )
    lines.extend(["    .byte -91:60", ".align 3"])
    data, n = re.subn(
        r"^frame_words:.*?(?=^record_context:)",
        lambda _: "\n".join(lines) + "\n",
        data,
        flags=re.M | re.S,
    )
    if n != 1:
        raise ValueError("Keep the frame and context arena labels")
    fields = [
        unsigned(recipe.get("capacity", 16384)),
        unsigned(recipe.get("sequence", m["index"])),
        unsigned(recipe.get("period", m["period"])),
        0xA5A5A5A5A5A5A5A5,
        0xA5A5A5A5A5A5A5A5,
        0xA5A5A5A5A5A5A5A5,
    ]
    signed = lambda v: v if v < 2**63 else v - 2**64
    data, n = re.subn(
        r"^record_context: \.dword [^\n]+$",
        lambda _: "record_context: .dword " + ", ".join(str(signed(v)) for v in fields),
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep one native record context")
    count = unsigned(recipe.get("declaredCount", len(raw)))
    data, n = re.subn(
        r"^record_count: \.dword -?[0-9]+$",
        lambda _: f"record_count: .dword {signed(count)}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep one native record count")
    return data + "\n.text\n" + code


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("recipe")
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    found = [
        r
        for r in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if r["id"] == a.recipe
    ]
    if len(found) != 1:
        p.error("Unknown record recipe")
    result = prepare(a.source.read_text(), found[0])
    with a.output.open("x") as out:
        out.write(result)


if __name__ == "__main__":
    main()
