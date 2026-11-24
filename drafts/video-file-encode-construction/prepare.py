#!/usr/bin/env python3
"""Prepare packed source frames, native controls and a bounded output path."""

import argparse, importlib.util, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location("file_encode_input", ROOT / "fixtures.py")
fixtures = importlib.util.module_from_spec(s)
s.loader.exec_module(fixtures)


def prepare(source, recipe):
    if source.count("\n.text\n") != 1 or source.count("\nvideo_encode_file:\n") != 1:
        raise ValueError("Keep one file encoder and text section")
    m = fixtures.material(recipe.get("fixture", "moving"))
    frames = m["frames"]
    values = [
        recipe.get(k, d)
        for k, d in (
            ("sourceCount", len(frames) * 16384),
            ("capacity", 7344152),
            ("frameCount", len(frames)),
            ("period", m["period"]),
            ("duration", m["duration"]),
            ("previousCapacity", 16384),
            ("scratchCapacity", 16384),
            ("target", min(6, len(frames) - 1)),
        )
    ]
    if any(type(v) is not int or not 0 <= v < 2**64 for v in values):
        raise ValueError("Use unsigned 64-bit controls")
    count, cap, n, period, duration, pcap, xcap, target = [
        v if v < 2**63 else v - 2**64 for v in values
    ]
    data, code = source.split("\n.text\n")
    data, changed = re.subn(
        r"^file_encode_frames:.*?(?=^file_encode_frames_end:)",
        lambda _: fixtures.declaration(frames),
        data,
        flags=re.M | re.S,
    )
    if changed != 1:
        raise ValueError("Keep the source arena markers")
    for label, value in (
        ("file_encode_source_count", count),
        ("file_encode_target", target),
    ):
        data, changed = re.subn(
            r"^" + label + r": \.dword -?[0-9]+$",
            lambda _: label + ": .dword " + str(value),
            data,
            flags=re.M,
        )
        if changed != 1:
            raise ValueError("Keep the count/target controls")
    data, changed = re.subn(
        r"^file_encode_context: \.dword [^\n]+$",
        lambda _: f"file_encode_context: .dword {cap}, {n}, {period}, {duration}, 0, {pcap}, 0, {xcap}",
        data,
        flags=re.M,
    )
    if changed != 1:
        raise ValueError("Keep the file context")
    path = recipe.get("path", "animation.mlv")
    if not isinstance(path, str) or "\0" in path:
        raise ValueError("Use a NUL-free path")
    raw = path.encode("utf-8") + b"\0"
    if len(raw) > 128:
        raise ValueError("Path exceeds 128-byte arena")
    decl = (
        "file_encode_path: .byte "
        + ", ".join(str(v if v < 128 else v - 256) for v in raw)
        + "\n    .space "
        + str(128 - len(raw))
        + "\n"
    )
    data, changed = re.subn(
        r"^file_encode_path:.*?(?=^file_encode_path_end:)",
        lambda _: decl,
        data,
        flags=re.M | re.S,
    )
    if changed != 1:
        raise ValueError("Keep the path arena markers")
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
        p.error("Unknown file-encoder recipe")
    result = prepare(a.source.read_text(), matches[0])
    with a.output.open("x") as out:
        out.write(result)
