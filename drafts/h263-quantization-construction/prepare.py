#!/usr/bin/env python3
"""Change the quantizer's data controls without replacing student instructions."""
import argparse
import re
from pathlib import Path
ROOT = Path(__file__).resolve().parent

def prepare(source, quant=8, intra=1, mode=1):
    if any(type(x) is not int for x in (quant, intra, mode)) or not 1 <= quant <= 31 or intra not in (0, 1) or mode not in (0, 1):
        raise ValueError('Choose quantizer 1..31 and zero/one INTRA and mode controls')
    if source.count('\n.text\n') != 1:
        raise ValueError('Keep one provided text-section boundary')
    data, instructions = source.split('\n.text\n')
    data, count = re.subn(r'^quant_request: \.dword 0, (?:[1-9]|[12][0-9]|3[01]), [01], [01]$',
                          lambda _: f'quant_request: .dword 0, {quant}, {intra}, {mode}', data, flags=re.M)
    if count != 1:
        raise ValueError('Keep one typed request declaration')
    return data + '\n.text\n' + instructions

if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--source', type=Path, default=ROOT/'main.asm')
    p.add_argument('--quant', type=int, default=8)
    p.add_argument('--intra', type=int, choices=(0,1), default=1)
    p.add_argument('--mode', type=int, choices=(0,1), default=1)
    p.add_argument('--output', type=Path, required=True)
    a = p.parse_args()
    result = prepare(a.source.read_text(), a.quant, a.intra, a.mode)
    with a.output.open('x') as out:
        out.write(result)
