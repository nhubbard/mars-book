#!/usr/bin/env python3
"""Install transform input data without altering student instructions or tables."""

import argparse
import importlib.util
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location(
    "transform_fixture", ROOT.parent / "h263-fixtures/fixture.py"
)
fixture = importlib.util.module_from_spec(spec)
spec.loader.exec_module(fixture)


def samples(direction, pattern):
    if (
        type(direction) is not int
        or direction not in (0, 1)
        or pattern not in ("flat", "worked")
    ):
        raise ValueError("Choose direction zero/one and flat/worked input")
    if direction:
        return [
            (
                1024
                if i == 0
                else (
                    64
                    if pattern == "worked" and i == 1
                    else (
                        -32
                        if pattern == "worked" and i == 8
                        else (
                            16
                            if pattern == "worked" and i == 9
                            else -8 if pattern == "worked" and i == 63 else 0
                        )
                    )
                )
            )
            for i in range(64)
        ]
    if pattern == "flat":
        return [128] * 64
    current = fixture.frame(1)
    reference = fixture.frame(0)
    return [
        current[(32 + y) * 176 + 16 + x] - reference[(30 + y) * 176 + 14 + x]
        for y in range(8)
        for x in range(8)
    ]


def prepare(source, mode, direction=0, pattern="flat"):
    if type(mode) is not int or mode not in (0, 1):
        raise ValueError("Choose scalar zero or MSA one")
    values = samples(direction, pattern)
    if source.count("\n.text\n") != 1:
        raise ValueError("Keep the one provided text-section boundary")
    data, instructions = source.split("\n.text\n")
    if (
        len(re.findall(r"^transform_input:", data, re.M)) != 1
        or len(re.findall(r"^transform_input_end:", data, re.M)) != 1
    ):
        raise ValueError("Keep unique input boundaries")
    declaration = (
        "transform_input:\n"
        + "\n".join(
            "    .half " + ",".join(map(str, values[i : i + 8]))
            for i in range(0, 64, 8)
        )
        + "\ntransform_input_end:"
    )
    data, count = re.subn(
        r"^transform_input:.*?^transform_input_end:",
        lambda _: declaration,
        data,
        flags=re.M | re.S,
    )
    if count != 1:
        raise ValueError("Keep the provided input arena")
    data, count = re.subn(
        r"^transform_request: \.dword 0, 16, [01], [01], 0, 0$",
        lambda _: "transform_request: .dword 0, 16, "
        + str(direction)
        + ", "
        + str(mode)
        + ", 0, 0",
        data,
        flags=re.M,
    )
    if count != 1:
        raise ValueError("Keep the typed request fields")
    return data + "\n.text\n" + instructions


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    p.add_argument("--mode", type=int, choices=(0, 1), default=0)
    p.add_argument("--direction", type=int, choices=(0, 1), default=0)
    p.add_argument("--pattern", choices=("flat", "worked"), default="flat")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    result = prepare(a.source.read_text(), a.mode, a.direction, a.pattern)
    with a.output.open("x") as out:
        out.write(result)
