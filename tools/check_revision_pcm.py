#!/usr/bin/env python3
"""Execute the literal revised PCM listing and compare its complete data image.

This verifies the eight-sample fragment, not a completed mixer or scalar tail.
Only the listed output slice and clipping-count word may change.
"""

import argparse
import hashlib
import json
from pathlib import Path
import random
import re
import struct
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "revision/main/pcm-mixing.tex"


def program(fragment):
    rng = random.Random(20261120)
    pairs = [
        (
            [0, 30000, 32767, -30000, -32768, 30000, -30000, 32767],
            [0, 2767, 1, -2768, -1, 10000, -10000, -32768],
        )
    ]
    pairs += [
        ([a] * 8, [b] * 8)
        for a, b in [
            (0, 0),
            (32767, 0),
            (-32768, 0),
            (32767, 1),
            (-32768, -1),
            (32767, 32767),
            (-32768, -32768),
            (32767, -32768),
        ]
    ]
    pairs += [
        (
            [rng.randint(-32768, 32767) for _ in range(8)],
            [rng.randint(-32768, 32767) for _ in range(8)],
        )
        for _ in range(23)
    ]
    data = bytearray()
    expected = bytearray()
    instructions = []
    cases = []
    base = 0x10010000
    for n, (left, right) in enumerate(pairs):
        for alias in ("separate", "left", "right", "same-input"):
            # Vary byte alignment and retain guards on both sides of every slice.
            data.extend(bytes([0xA5]) * (1 + n % 4))
            a = len(data)
            data.extend(struct.pack("<8h", *left))
            data.extend(b"\x5a\xa5\x5a")
            b = len(data)
            data.extend(struct.pack("<8h", *right))
            data.extend(b"\x5a\xa5\x5a")
            c = len(data)
            data.extend(bytes([0xCC]) * 16)
            data.extend(b"\x5a\xa5\x5a")
            data.extend(bytes((-len(data)) % 8))
            count = len(data)
            data.extend(b"\xdd" * 8)
            # Keep a separate expected image; only admitted result bytes change.
            expected.extend(data[len(expected) :])
            if alias == "left":
                c = a
            elif alias == "right":
                c = b
            elif alias == "same-input":
                b = a
            actual_right = left if alias == "same-input" else pairs[n][1]
            sums = [x + y for x, y in zip(left, actual_right)]
            initial_count = n + 3
            clipped = sum(s < -32768 or s > 32767 for s in sums)
            expected[c : c + 16] = struct.pack(
                "<8h", *(min(32767, max(-32768, s)) for s in sums)
            )
            expected[count : count + 8] = struct.pack("<Q", initial_count + clipped)
            instructions += [
                f"dli $a0, {base+a}",
                f"dli $a1, {base+b}",
                f"dli $a2, {base+c}",
                f"li $v1, {initial_count}",
                fragment,
                f"dli $s0, {base+count}",
                "sd $v1, 0($s0)",
            ]
            cases.append(dict(pair=n, alias=alias, clipped=clipped))
    data.extend(bytes((-len(data)) % 4))
    expected.extend(data[len(expected) :])
    # Explicit bytes keep layout independent of assembler automatic alignment.
    declarations = [
        ".byte " + ",".join(map(str, data[i : i + 32])) for i in range(0, len(data), 32)
    ]
    source = "\n".join(
        [
            ".data",
            *declarations,
            ".text",
            ".globl main",
            "main:",
            *instructions,
            "li $v0, 10",
            "syscall",
            "",
        ]
    )
    return source, bytes(expected), cases


def verify(java, cli, output):
    text = SOURCE.read_text()
    fragments = re.findall(
        r"\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}", text, re.S
    )
    if len(fragments) != 1:
        raise ValueError("Expected one literal eight-sample fragment")
    source, expected, cases = program(fragments[0])
    output.mkdir(parents=True, exist_ok=False)
    asm = output / "pcm-listing.asm"
    asm.write_text(source)
    dump = output / "data.bin"
    command = [
        str(java),
        "-jar",
        str(cli),
        "--max-steps",
        "20000",
        "--start-at-main",
        "--dump",
        ".data",
        "Binary",
        str(dump),
        str(asm),
    ]
    result = subprocess.run(command, capture_output=True, text=True, timeout=60)
    (output / "stdout.txt").write_text(result.stdout)
    (output / "stderr.txt").write_text(result.stderr)
    if result.returncode:
        raise AssertionError(f"MARS rejected listing: {result.stderr}")
    actual = dump.read_bytes()
    if actual[: len(expected)] != expected or any(actual[len(expected) :]):
        differences = [i for i, (a, b) in enumerate(zip(actual, expected)) if a != b]
        raise AssertionError(
            f"PCM data image differs: lengths {len(actual)}/{len(expected)}, offsets {differences[:12]}"
        )
    receipt = dict(
        cases=len(cases),
        samplePairs=8 * len(cases),
        failures=0,
        scope="Literal eight-sample PCM fragment; sample results, count accumulation, full data image, guards and exact aliases. No scalar-tail or whole-mixer claim.",
        sourceSha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
        fragmentSha256=hashlib.sha256(fragments[0].encode()).hexdigest(),
        cliSha256=hashlib.sha256(cli.read_bytes()).hexdigest(),
        dataSha256=hashlib.sha256(actual).hexdigest(),
        command=command,
    )
    (output / "receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(
        f"Passed {len(cases)} groups, {8*len(cases)} sample pairs; complete data image matches."
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java", type=Path, required=True)
    parser.add_argument("--cli", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    verify(args.java.resolve(), args.cli.resolve(), args.output.resolve())
