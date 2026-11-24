#!/usr/bin/env python3
"""Change packed input and native playback controls without rewriting instructions."""

import argparse, importlib.util, json, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "previous_file_prepare", ROOT.parent / "video-file-encode-construction/prepare.py"
)
previous = importlib.util.module_from_spec(s)
s.loader.exec_module(previous)


def prepare(source, recipe):
    if source.count("\nvideo_play:\n") != 1:
        raise ValueError("Keep one playback entry")
    source = previous.prepare(source, recipe)
    data, code = source.split("\n.text\n")
    values = [
        recipe.get(k, d)
        for k, d in (
            ("imageCapacity", 16384),
            ("directoryCapacity", 2048),
            ("playbackScratchCapacity", 16384),
            ("start", 0),
        )
    ]
    if any(type(v) is not int or not 0 <= v < 2**64 for v in values):
        raise ValueError("Use unsigned 64-bit controls")
    image, directory, scratch, start = [v if v < 2**63 else v - 2**64 for v in values]
    data, n = re.subn(
        r"^playback_context: \.dword [^\n]+$",
        lambda _: f"playback_context: .dword {image}, 0, {directory}, 0, {scratch}, {start}",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep the playback context")
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
        p.error("Unknown playback recipe")
    result = prepare(a.source.read_text(), matches[0])
    with a.output.open("x") as out:
        out.write(result)
