#!/usr/bin/env python3
"""Execute the chapter's literal listings; compare output and full register values."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'revision/main/integer-representation.tex'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify(java, cli, output):
    blocks = re.findall(r'\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}', SOURCE.read_text(), re.S)
    if len(blocks) != 2:
        raise ValueError('Expected the complete inspector and word-boundary experiment')
    for name, block in zip(('byte-inspector.asm', 'word-boundary.asm'), blocks):
        if (ROOT / 'revision/labs/files' / name).read_text() != block:
            raise ValueError('Downloadable source differs from literal listing: '+name)
    output.mkdir(parents=True, exist_ok=False)
    runs = []

    def run(name, source, value=None):
        path = output / (name + '.asm')
        path.write_text(source)
        command = [str(java), '-jar', str(cli), '--max-steps', '1000', '--start-at-main',
                   '--register', 't0', '--register', 't1', '--register', 't2', str(path)]
        result = subprocess.run(command, input='' if value is None else str(value)+'\n',
                                capture_output=True, text=True, timeout=15)
        registers = dict((m[1], int(m[2], 16)) for m in
                         re.finditer(r'^\$(t[012])\t0x([0-9a-f]{16})$', result.stdout, re.M))
        console = result.stdout.split('\n$t0\t')[0]
        record = dict(name=name, input=value, sourceSha256=sha(path), command=command,
                      exitCode=result.returncode, stdout=result.stdout, stderr=result.stderr,
                      registers=registers)
        runs.append(record)
        if result.returncode != 0 or result.stderr or len(registers) != 3:
            raise AssertionError(record)
        return console, registers

    def expected(value):
        byte = value & 255
        signed = byte if byte < 128 else byte - 256
        return ('Enter an integer from 0 to 255: Unsigned byte: '+str(byte)+'\nSigned byte: '+str(signed)+'\n',
                {'t0': byte, 't1': signed & ((1 << 64)-1), 't2': 0})

    # Execute the listing unchanged in a fresh machine for every admitted byte.
    for value in range(256):
        actual = run('byte-'+str(value), blocks[0], value)
        if actual != expected(value):
            raise AssertionError((value, actual, expected(value)))
    # These are observations of masking outside the precondition, not input validation.
    for value in (-1, 256, 511):
        if run('outside-'+str(value), blocks[0], value) != expected(value):
            raise AssertionError('Outside-range mask observation differs')
    console, registers = run('word-boundary', blocks[1])
    if console or registers != dict(t0=0x7fffffff, t1=0xffffffff80000000, t2=0x80000000):
        raise AssertionError((console, registers))

    faults = {
        'missing-sign-extension': blocks[0].replace('seb $t1, $t0', 'move $t1, $t0'),
        'wrong-output-register': blocks[0].replace('move $a0, $t1', 'move $a0, $t0'),
        'lost-sign-bit': blocks[0].replace('0xff', '0x7f'),
    }
    for name, source in faults.items():
        if source == blocks[0] or run(name, source, 150) == expected(150):
            raise AssertionError('Fault was not exposed: '+name)
    wrong_width = blocks[1].replace('daddiu $t2', 'addiu $t2')
    if run('wrong-arithmetic-width', wrong_width)[1]['t2'] == 0x80000000:
        raise AssertionError('Word/64-bit width fault was not exposed')
    receipt = dict(admittedByteCases=256, outsideRangeObservations=3, wordBoundaryCases=1,
                   assembledFaultsRejected=4, sourceSha256=sha(SOURCE), cliSha256=sha(cli),
                   verifierSha256=sha(Path(__file__)), runs=runs,
                   scope='Literal native LaTeX listings, exact console output and full final register values. Fresh CLI sessions; no IDE breakpoint, student usability, or unimplemented independent-variation claim.')
    (output / 'receipt.json').write_text(json.dumps(receipt, indent=2)+'\n')
    print('Passed all 256 byte inputs, three outside-range observations and word boundary; four assembled faults exposed.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java', 'cli', 'output'):
        parser.add_argument('--'+name, type=Path, required=True)
    args = parser.parse_args()
    verify(args.java.resolve(), args.cli.resolve(), args.output.resolve())
