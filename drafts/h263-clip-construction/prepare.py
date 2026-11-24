#!/usr/bin/env python3
"""Create a new generated-input run directory, retaining the student's instructions."""

import argparse
import importlib.util
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "clip_input_fixture", ROOT.parent / "h263-fixtures/fixture.py"
)
FIXTURE = importlib.util.module_from_spec(s)
s.loader.exec_module(FIXTURE)


def quote(path):
    text = str(path)
    if any(ord(c) < 32 or ord(c) > 126 for c in text):
        raise ValueError("Use printable ASCII for this assembly path fixture")
    return text.replace("\\", "\\\\").replace('"', '\\"')


def prepare(source, folder, frames=8, tick_step=6, quant=8, mode=1):
    FIXTURE.validate(frames, tick_step)
    if (
        type(quant) is not int
        or not 1 <= quant <= 31
        or type(mode) is not int
        or mode not in (0, 1)
    ):
        raise ValueError("Choose quantizer 1..31 and mode 0/1")
    if source.count("\n.text\n") != 1:
        raise ValueError("Keep one text boundary")
    folder = folder.resolve()
    paths = [folder / "clip.yuv", folder / "encoded.263", folder / "reconstructed.yuv"]
    data, code = source.split("\n.text\n")
    for name, path in zip(("input", "stream", "reconstruction"), paths):
        encoded = quote(path)
        if len(str(path)) > 4095:
            raise ValueError("Paths must fit 4095 bytes plus NUL")
        replacement = f'clip_{name}_path:\n    .asciiz "{encoded}"\n    .space {4095-len(str(path))}\nclip_{name}_path_end:'
        data, n = re.subn(
            r"^clip_" + name + r"_path:.*?^clip_" + name + r"_path_end:",
            lambda _: replacement,
            data,
            flags=re.M | re.S,
        )
        if n != 1:
            raise ValueError("Keep unique path-data boundaries")
    data, n = re.subn(
        r"^clip_request: \.dword 0,0,0,[0-9]+,[0-9]+,[0-9]+,[01],0,0,0,0,0,0$",
        f"clip_request: .dword 0,0,0,{frames},{tick_step},{quant},{mode},0,0,0,0,0,0",
        data,
        flags=re.M,
    )
    if n != 1:
        raise ValueError("Keep the typed clip request")
    return data + "\n.text\n" + code


def write(source, folder, frames=8, tick_step=6, quant=8, mode=1):
    text = prepare(source, folder, frames, tick_step, quant, mode)
    metadata = FIXTURE.write(folder, frames, tick_step)
    with (folder / "main.asm").open("x") as out:
        out.write(text)
    return metadata


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", type=Path, default=ROOT / "main.asm")
    p.add_argument("--output-dir", type=Path, required=True)
    p.add_argument("--frames", type=int, default=8)
    p.add_argument("--tick-step", type=int, default=6)
    p.add_argument("--quant", type=int, default=8)
    p.add_argument("--mode", type=int, default=1)
    a = p.parse_args()
    try:
        write(
            a.source.read_text(), a.output_dir, a.frames, a.tick_step, a.quant, a.mode
        )
    except (ValueError, FileExistsError) as e:
        p.error(str(e))
    print(
        "Prepared",
        a.output_dir / "main.asm",
        "and generated raw clip; existing directories are refused",
    )
