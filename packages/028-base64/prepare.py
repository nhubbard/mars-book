#!/usr/bin/env python3
"""Prepare an exact Base64 observation without changing authored instructions."""

import argparse, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
TARGETS = [
    "digit",
    "validate",
    "encode_scalar",
    "decode_scalar",
    "encode12_msa",
    "decode16_msa",
    "encode_msa",
    "decode_msa",
]


def values(q):
    if q["target"] not in TARGETS:
        raise ValueError("Choose a declared entry")
    if not isinstance(q["inputHex"], str) or not re.fullmatch(
        r"(?:[0-9a-f]{2})*", q["inputHex"]
    ):
        raise ValueError("Use canonical hexadecimal octets")
    data = bytes.fromhex(q["inputHex"])
    if len(data) > 5464:
        raise ValueError("Observation bytes exceed the input arena domain")
    for name in ["count", "capacity"]:
        if (
            not isinstance(q[name], str)
            or not re.fullmatch(r"0|[1-9][0-9]*", q[name])
            or not 0 <= int(q[name]) < 2**64
        ):
            raise ValueError("Use canonical unsigned64-bit controls")
    t = q["target"]
    n = int(q["count"])
    if type(q["inPlace"]) is not bool or q["inPlace"] and not t.startswith("decode"):
        raise ValueError("Exact in-place mode is available only for decoding")
    if t == "digit" and len(data) != 1:
        raise ValueError("Classifier observation needs one octet")
    if t == "validate" and (not data or len(data) % 4 or n != len(data)):
        raise ValueError("Validator requires a positive multiple-of-four text count")
    if t == "encode12_msa" and len(data) != 12:
        raise ValueError("Encoder kernel needs exactly twelve bytes")
    if t == "decode16_msa" and (
        len(data) != 16
        or any(
            v not in b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            for v in data
        )
    ):
        raise ValueError("Decoder kernel needs sixteen unpadded alphabet characters")
    if (
        t in ["encode_scalar", "encode_msa", "decode_scalar", "decode_msa"]
        and n <= (4096 if t.startswith("encode") else 5464)
        and n != len(data)
    ):
        raise ValueError("Bounded count must match the prepared bytes")
    return data, n, int(q["capacity"])


def prepare(source, q):
    data, n, cap = values(q)
    for name, octets in [
        ("source", data + bytes([165]) * (8192 - len(data))),
        ("destination", bytes([165]) * 8192),
    ]:
        replacement = (
            name
            + ":\n"
            + "".join(
                "    .byte "
                + ",".join(str(v if v < 128 else v - 256) for v in octets[i : i + 16])
                + "\n"
                for i in range(0, len(octets), 16)
            )
        )
        source, count = re.subn(
            r"^" + name + r":.*?(?=^" + name + "_end:)",
            lambda _: replacement,
            source,
            flags=re.M | re.S,
        )
        if count != 1:
            raise ValueError("Retain the unique " + name + " arena and its end label")
    for name, kind, old, new in [
        ("mode", "word", 2, TARGETS.index(q["target"])),
        ("in_place", "word", 0, int(q["inPlace"])),
        ("count", "dword", 3, n),
        ("capacity", "dword", 4, cap),
    ]:
        token = f"{name}: .{kind} {old}\n"
        if source.count(token) != 1:
            raise ValueError("Retain default " + name + " control")
        source = source.replace(
            token, f"{name}: .{kind} {new if new<2**63 else new-2**64}\n", 1
        )
    return source


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("recipe")
    p.add_argument("--source", type=Path, default=ROOT / "starter/main.asm")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    matches = [
        q
        for q in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if q["id"] == a.recipe
    ]
    if len(matches) != 1:
        p.error("Choose an exact recipe ID")
    if a.output.exists() or a.source.resolve() == a.output.resolve():
        p.error("Choose a distinct new output; existing work is never overwritten")
    try:
        text = prepare(a.source.read_text(), matches[0])
    except (ValueError, UnicodeError) as error:
        p.error(str(error))
    created = False
    try:
        with a.output.open("x") as f:
            created = True
            f.write(text)
    except BaseException:
        if created:
            a.output.unlink()
        raise
    print(a.output)


if __name__ == "__main__":
    main()
