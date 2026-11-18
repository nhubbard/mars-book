#!/usr/bin/env python3
"""Prepare exact FIR observation inputs in a separate assembly file."""
import argparse
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parent

def sample_bytes(samples):
    if not all(isinstance(v, int) and -32768 <= v <= 32767 for v in samples):
        raise ValueError('Use signed sixteen-bit sample and coefficient codes')
    return [byte for value in samples for byte in (value % 256, (value % 65536) // 256)]

def byte_assembly(values):
    signed = [v if v < 128 else v - 256 for v in values]
    return ''.join('    .byte ' + ','.join(map(str, signed[i:i+16])) + '\n'
                   for i in range(0, len(signed), 16))

def prepare(source, recipe):
    samples, taps = recipe['input'], recipe['taps']
    if not 7 <= len(samples) <= 71 or len(taps) != 8:
        raise ValueError('Input includes seven history samples and 0..64 new samples; use eight taps')
    for name, size, values in [('source', 192, sample_bytes(samples)),
                               ('coeff', 64, sample_bytes(taps)), ('output', 192, [165] * 192)]:
        begin, end = name + ':', name + '_end:'
        if source.count(begin) != 1 or source.count(end) != 1:
            raise ValueError('Expected one complete arena named ' + name)
        a = source.index(begin) + len(begin)
        b = source.index(end, a)
        source = source[:a] + '\n' + byte_assembly(values + [0] * (size - len(values))) + source[b:]
    line = 'count: .word 16\n'
    if source.count(line) != 1:
        raise ValueError('Use a fresh cumulative program with its default count line')
    return source.replace(line, 'count: .word ' + str(len(samples) - 7) + '\n')

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('recipe')
    parser.add_argument('--source', type=Path, default=ROOT / 'starter/main.asm')
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    recipes = json.loads((ROOT / 'inputs.json').read_text())['recipes']
    selected = [r for r in recipes if r['id'] == args.recipe]
    if len(selected) != 1:
        parser.error('Choose a recipe ID from inputs.json')
    if args.output.resolve() == args.source.resolve():
        parser.error('Write a separate observation file to preserve your source')
    with args.output.open('x') as output:
        output.write(prepare(args.source.read_text(), selected[0]))
    print(args.output)

if __name__ == '__main__':
    main()
