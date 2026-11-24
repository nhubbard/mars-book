#!/usr/bin/env python3
"""Run the actual MARS QOI file pipeline and verify decoded RGBA and header flags."""

import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import struct
import subprocess
from revision_qoi_format import decode, END

ROOT = Path(__file__).resolve().parents[1]


def run(
    java,
    cli,
    output,
    fixture="trace",
    width=9,
    height=10,
    channels=4,
    colorspace=0,
    source=None,
    mirror=False,
):
    if (
        not 1 <= width <= 64
        or not 1 <= height <= 64
        or channels not in (3, 4)
        or colorspace not in (0, 1)
    ):
        raise ValueError("Use dimensions 1..64, channels 3/4 and colorspace 0/1")
    if fixture == "pattern":
        rgba = bytes((i * 73 + i // 7 * 19) & 255 for i in range(4 * width * height))
        data = (
            b"qoif"
            + struct.pack(">IIBB", width, height, channels, colorspace)
            + b"".join(b"\xff" + rgba[i : i + 4] for i in range(0, len(rgba), 4))
            + END
        )
    else:
        path = ROOT / "packages/030-qoi/fixtures/fixture.py"
        spec = importlib.util.spec_from_file_location("qoi_public_fixture", path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        width, height, rgba, data = module.material(fixture)
        data = data[:12] + bytes((channels, colorspace)) + data[14:]
    image = decode(data)
    if image.rgba != rgba:
        raise AssertionError("Input fixture and strict host decoder disagree")
    expected = (
        b"".join(
            rgba[4 * (y * width + width - 1 - x) : 4 * (y * width + width - x)]
            for y in range(height)
            for x in range(width)
        )
        if mirror
        else rgba
    )
    source = source or ROOT / "revision/labs/files/qoi-codec.asm"
    text = source.read_text()
    selector = "mode: .word 0" if text.count("mode: .word 0") == 1 else "mode: .word 7"
    if text.count(selector) != 1:
        raise ValueError("Use an unchanged worked or independent caller")
    if mirror:
        call = "decode_call:\n    balc qoi_decode\n"
        if text.count(call) != 1 or "qoi_decode_mirrored:" not in text:
            raise ValueError("Mirroring needs the completed independent source")
        text = text.replace(call, "decode_call:\n    balc qoi_decode_mirrored\n", 1)
    output.mkdir(parents=True, exist_ok=False)
    (output / "input.qoi").write_bytes(data)
    (output / "output.qoi").write_bytes(b"old:" * 8192)
    caller = output / "pipeline.asm"
    caller.write_text(text.replace(selector, "mode: .word 6", 1))
    command = [
        str(java.resolve()),
        "-jar",
        str(cli.resolve()),
        "--warnings-as-errors",
        "--start-at-main",
        "--max-steps",
        "10000000",
        str(caller.resolve()),
    ]
    result = subprocess.run(
        command, cwd=output, capture_output=True, text=True, timeout=180
    )
    (output / "stdout.txt").write_text(result.stdout)
    (output / "stderr.txt").write_text(result.stderr)
    if result.returncode or result.stdout:
        raise AssertionError(
            (result.returncode, result.stdout[-1000:], result.stderr[-1000:])
        )
    actual_data = (output / "output.qoi").read_bytes()
    actual = decode(actual_data)
    if (
        actual.width,
        actual.height,
        actual.channels,
        actual.colorspace,
        actual.rgba,
    ) != (width, height, channels, colorspace, expected):
        raise AssertionError("Output dimensions, flags or complete RGBA disagree")
    if (output / "input.qoi").read_bytes() != data:
        raise AssertionError("Input file changed")
    (output / "decoded.rgba").write_bytes(actual.rgba)
    rgb = bytes(
        c for i in range(0, len(actual.rgba), 4) for c in actual.rgba[i : i + 3]
    )
    (output / "rgb-preview.ppm").write_bytes(
        f"P6\n{width} {height}\n255\n".encode() + rgb
    )
    sha = lambda f: hashlib.sha256(f.read_bytes()).hexdigest()
    receipt = dict(
        command=command,
        workingDirectory=str(output.resolve()),
        width=width,
        height=height,
        channels=channels,
        colorspace=colorspace,
        mirrored=mirror,
        sourceSha256=sha(source),
        callerSha256=sha(caller),
        cliSha256=sha(cli),
        toolSha256=sha(Path(__file__)),
        modelSha256=sha(ROOT / "tools/revision_qoi_format.py"),
        inputSha256=sha(output / "input.qoi"),
        outputSha256=sha(output / "output.qoi"),
        rgbaSha256=sha(output / "decoded.rgba"),
        outputBytes=len(actual_data),
        scope="Actual host-file load/decode/encode/save with strict host RGBA and flag comparison and truncation of longer old output. RGB preview ignores alpha. No virtual I/O, native IDE or elapsed-time claim.",
    )
    (output / "receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(
        f"Verified {width} by {height}: {len(actual_data)} QOI bytes and {len(actual.rgba)} exact RGBA bytes"
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "cli", "output"):
        parser.add_argument("--" + name, type=Path, required=True)
    parser.add_argument(
        "--fixture", choices=("trace", "sprite", "pattern"), default="trace"
    )
    parser.add_argument("--width", type=int, default=9)
    parser.add_argument("--height", type=int, default=10)
    parser.add_argument("--channels", type=int, default=4)
    parser.add_argument("--colorspace", type=int, default=0)
    parser.add_argument("--source", type=Path)
    parser.add_argument("--mirror", action="store_true")
    run(**vars(parser.parse_args()))
