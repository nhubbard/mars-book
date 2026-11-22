#!/usr/bin/env python3
"""Run the complete MARS PPM pipeline in a new directory and verify actual host-file bytes."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def run(java, cli, output, width=2, height=2, commented=False, source=None, negative=False):
    if not 1 <= width <= 64 or not 1 <= height <= 64:
        raise ValueError('Choose width and height from one through 64')
    source = source or ROOT / 'revision/labs/files/ppm-codec.asm'
    text = source.read_text()
    mode = 'mode: .word 0' if text.count('mode: .word 0') == 1 else 'mode: .word 6'
    if text.count(mode) != 1:
        raise ValueError('Use the supplied worked caller or independent starter harness')
    if negative:
        call = 'encode_call:\n    balc ppm_encode\n'
        if text.count(call) != 1 or 'ppm_encode_negative:' not in text:
            raise ValueError('Negative output needs the completed independent source')
        text = text.replace(call, 'encode_call:\n    balc ppm_encode_negative\n', 1)
    output.mkdir(parents=True, exist_ok=False)
    raw = bytes.fromhex('ff0000 00ff00 0000ff ffffff') if (width, height) == (2, 2) else bytes((i * 73 + 129) & 255 for i in range(3 * width * height))
    result_raster = bytes(value ^ 255 for value in raw) if negative else raw
    canonical = f'P6\n{width} {height}\n255\n'.encode('ascii') + result_raster
    header = f'P6#input comment\n{width}\t{height}\n255\n'.encode('ascii') if commented else f'P6\n{width} {height}\n255\n'.encode('ascii')
    (output / 'input.ppm').write_bytes(header + raw)
    (output / 'output.ppm').write_bytes(b'old:' * 4096)
    caller = output / 'pipeline.asm'
    caller.write_text(text.replace(mode, 'mode: .word 5', 1))
    command = [str(java.resolve()), '-jar', str(cli.resolve()), '--warnings-as-errors', '--max-steps', '3000000', str(caller.resolve())]
    result = subprocess.run(command, cwd=output, capture_output=True, text=True, timeout=180)
    (output / 'stdout.txt').write_text(result.stdout)
    (output / 'stderr.txt').write_text(result.stderr)
    if result.returncode or result.stdout or (output / 'output.ppm').read_bytes() != canonical:
        raise AssertionError((result.returncode, result.stdout[-2000:], result.stderr[-2000:]))
    sha = lambda f: hashlib.sha256(f.read_bytes()).hexdigest()
    receipt = dict(command=command, workingDirectory=str(output.resolve()), width=width, height=height, commentedInput=commented, complementaryOutput=negative,
                   sourceSha256=sha(source), callerSha256=sha(caller), cliSha256=sha(cli), toolSha256=sha(Path(__file__)),
                   inputSha256=sha(output / 'input.ppm'), outputSha256=sha(output / 'output.ppm'), outputBytes=len(canonical),
                   scope='Actual host-file open/read/decode/encode/write/close path. Exact canonical bytes and truncation of a longer old output are checked. No in-memory I/O fixture, host-time or native IDE claim.')
    (output / 'receipt.json').write_text(json.dumps(receipt, indent=2) + '\n')
    print(f'Verified {width} by {height}: {len(canonical)} bytes at {output.resolve() / "output.ppm"}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java', 'cli', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    parser.add_argument('--source', type=Path)
    parser.add_argument('--width', type=int, default=2)
    parser.add_argument('--height', type=int, default=2)
    parser.add_argument('--commented', action='store_true')
    parser.add_argument('--negative', action='store_true', help='Use a completed ppm-negative.asm source to produce complementary RGB')
    args = parser.parse_args()
    run(args.java, args.cli, args.output, args.width, args.height, args.commented, args.source, args.negative)
