#!/usr/bin/env python3
"""Select a real QCIF search example without changing student instruction text."""

import argparse
import importlib.util
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location(
    "motion_worked_video", ROOT.parent / "h263-fixtures/fixture.py"
)
fixture = importlib.util.module_from_spec(spec)
spec.loader.exec_module(fixture)


def prepare(source, mode):
    if type(mode) is not int or mode not in (0, 1):
        raise ValueError("Choose scalar mode zero or MSA mode one")
    if source.count("\n.text\n") != 1:
        raise ValueError("Keep the one supplied text-section boundary")
    data, instructions = source.split("\n.text\n")
    current = fixture.frame(1)
    reference = fixture.frame(0)
    start = 32 * 176 + 16
    for label, pixels in [
        ("motion_current", current[start : start + 2656]),
        ("motion_reference", reference[:25344]),
    ]:
        raw = bytes([165] * 16) + pixels + bytes([165] * 16)
        declaration = (
            label
            + ":\n"
            + "\n".join(
                "    .byte "
                + ",".join(str(v if v < 128 else v - 256) for v in raw[at : at + 32])
                for at in range(0, len(raw), 32)
            )
            + "\n"
            + label
            + "_end:"
        )
        if (
            len(re.findall(r"^" + label + ":", data, re.M)) != 1
            or len(re.findall(r"^" + label + "_end:", data, re.M)) != 1
        ):
            raise ValueError("Keep unique supplied input arena boundaries")
        data, n = re.subn(
            r"^" + label + r":.*?^" + label + "_end:",
            lambda _: declaration,
            data,
            flags=re.M | re.S,
        )
        if n != 1:
            raise ValueError("Keep the supplied input arena")
    pattern = (
        r"^motion_request:\n    \.dword 0, 0, 176, 176\n    \.dword 16, 32, 2, [01]$"
    )
    replacement = (
        "motion_request:\n    .dword 0, 0, 176, 176\n    .dword 16, 32, 2, " + str(mode)
    )
    data, n = re.subn(pattern, lambda _: replacement, data, flags=re.M)
    if n != 1:
        raise ValueError("Keep the typed worked request fields")
    return data + "\n.text\n" + instructions


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=ROOT / "main.asm")
    parser.add_argument("--mode", type=int, choices=(0, 1), default=0)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    result = prepare(args.source.read_text(), args.mode)
    with args.output.open("x") as stream:
        stream.write(result)
