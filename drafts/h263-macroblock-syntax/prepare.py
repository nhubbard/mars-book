#!/usr/bin/env python3
"""Set six selected-level blocks and macroblock controls, preserving instructions/data tables."""

import argparse
import importlib.util
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("syntax_model", ROOT / "model.py")
MODEL = importlib.util.module_from_spec(spec)
spec.loader.exec_module(MODEL)


def blocks(intra, mask=0, pattern="single"):
    if (
        type(intra) is not int
        or intra not in (0, 1)
        or type(mask) is not int
        or not 0 <= mask <= 63
        or pattern not in ("single", "tail", "dense")
    ):
        raise ValueError("Choose binary INTRA, six-bit presence mask and named pattern")
    result = []
    for block in range(6):
        values = [0] * 64
        if mask & (1 << (5 - block)):
            if pattern == "dense":
                values = [1 if i % 2 == 0 else -1 for i in range(64)]
            else:
                values[63 if pattern == "tail" else 1 if intra else 0] = (
                    (-127 if block % 2 else 127)
                    if pattern == "tail"
                    else (-1 if block % 2 else 1)
                )
        if intra:
            values[0] = 255
        result.append(values)
    return result


def prepare(
    source,
    picture=0,
    intra=1,
    mask=0,
    pattern="single",
    mx=0,
    my=0,
    px=0,
    py=0,
    allow_skip=1,
    capacity=1152,
    used=3,
):
    if (
        any(
            type(v) is not int
            for v in (picture, intra, mx, my, px, py, allow_skip, capacity, used)
        )
        or picture not in (0, 1)
        or intra not in (0, 1)
        or allow_skip not in (0, 1)
        or not 0 <= capacity <= 1152
        or not 0 <= used <= capacity * 8
    ):
        raise ValueError(
            "Use binary controls, capacity 0..1152 and admitted used count"
        )
    values = blocks(intra, mask, pattern)
    MODEL.validate(
        values, bool(picture), bool(intra), (mx, my), (px, py), bool(allow_skip)
    )
    if source.count("\n.text\n") != 1:
        raise ValueError("Keep one text-section boundary")
    data, code = source.split("\n.text\n")
    flat = [v for b in values for v in b]
    replacement = (
        "syntax_levels:\n"
        + "\n".join(
            "    .half " + ",".join(map(str, flat[i : i + 8])) for i in range(0, 384, 8)
        )
        + "\nsyntax_levels_end:"
    )
    data, n = re.subn(
        r"^syntax_levels:.*?^syntax_levels_end:",
        lambda _: replacement,
        data,
        flags=re.M | re.S,
    )
    if n != 1:
        raise ValueError("Keep unique six-block input boundaries")
    for before, after in [
        (
            r"syntax_context: \.dword 0, [0-9]+, [0-9]+",
            f"syntax_context: .dword 0, {capacity}, {used}",
        ),
        (
            r"syntax_request: \.dword 0, 0, [01], [01], -?[0-9]+, -?[0-9]+, -?[0-9]+, -?[0-9]+, [01], 0",
            f"syntax_request: .dword 0, 0, {picture}, {intra}, {mx}, {my}, {px}, {py}, {allow_skip}, 0",
        ),
    ]:
        data, n = re.subn(
            "^" + before + "$", lambda _, after=after: after, data, flags=re.M
        )
        if n != 1:
            raise ValueError("Keep typed context/request declarations")
    return data + "\n.text\n" + code


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    for name, value in [
        ("picture", 0),
        ("intra", 1),
        ("mask", 0),
        ("mx", 0),
        ("my", 0),
        ("px", 0),
        ("py", 0),
        ("allow-skip", 1),
        ("capacity", 1152),
        ("used", 3),
    ]:
        p.add_argument("--" + name, type=int, default=value)
    p.add_argument("--pattern", choices=("single", "tail", "dense"), default="single")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    result = prepare(
        a.source.read_text(),
        a.picture,
        a.intra,
        a.mask,
        a.pattern,
        a.mx,
        a.my,
        a.px,
        a.py,
        a.allow_skip,
        a.capacity,
        a.used,
    )
    with a.output.open("x") as out:
        out.write(result)
