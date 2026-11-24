#!/usr/bin/env python3
"""Prepare a new run, execute the student's actual MIPS encoder and independently check it."""

import argparse
import hashlib
import importlib.util
import json
import subprocess
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


def run(source, cli, java, output, frames=8, tick_step=6, quant=8, mode=1):
    prepare = load("clip_run_prepare", ROOT / "prepare.py")
    prepare.write(source.read_text(), output, frames, tick_step, quant, mode)
    program = (output / "main.asm").resolve()
    command = [
        str(java),
        "-jar",
        str(cli.resolve()),
        "ae1",
        "se1",
        "we",
        "ic",
        str(program),
    ]
    start = time.monotonic()
    r = subprocess.run(command, capture_output=True, text=True)
    elapsed = time.monotonic() - start
    (output / "run.txt").write_text(r.stdout + r.stderr)
    if r.returncode:
        raise ValueError("MIPS encoder failed; inspect " + str(output / "run.txt"))
    numbers = [int(line) for line in r.stdout.splitlines() if line.isdigit()]
    if len(numbers) != 2 or numbers[0] != frames:
        raise ValueError(
            "The ordinary caller must print the accepted picture count, followed by CLI instruction count"
        )
    checker = load(
        "clip_run_stream_check",
        ROOT.parent / "h263-picture-construction/check_stream.py",
    )
    result = checker.check(
        output / "encoded.263",
        output / "clip.yuv",
        output / "reconstructed.yuv",
        frames,
        tick_step,
    )
    result.update(
        guestInstructions=numbers[-1],
        freshCliWallSeconds=elapsed,
        timingScope="Fresh whole CLI invocation: JVM startup, assembly, simulation, real file operations and shutdown; host load uncontrolled",
        javaExitCode=r.returncode,
        command=command,
        sourceSha256=hashlib.sha256(program.read_bytes()).hexdigest(),
        cliSha256=hashlib.sha256(cli.read_bytes()).hexdigest(),
    )
    with (output / "verification.json").open("x") as out:
        json.dump(result, out, indent=2, allow_nan=False)
        out.write("\n")
    return result


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("source", type=Path)
    p.add_argument("--cli", type=Path, required=True)
    p.add_argument("--java", default="java")
    p.add_argument("--output-dir", type=Path, required=True)
    for name, value in [("frames", 8), ("tick-step", 6), ("quant", 8), ("mode", 1)]:
        p.add_argument("--" + name, type=int, default=value)
    a = p.parse_args()
    if not a.source.is_file() or not a.cli.is_file():
        p.error("Supply an existing completed source and CLI archive")
    try:
        r = run(
            a.source,
            a.cli,
            a.java,
            a.output_dir,
            a.frames,
            a.tick_step,
            a.quant,
            a.mode,
        )
    except (ValueError, FileExistsError) as e:
        p.error(str(e))
    print(
        "Verified",
        r["frames"],
        "QCIF pictures;",
        r["encodedBytes"],
        "bytes;",
        r["guestInstructions"],
        "guest instructions",
    )
