#!/usr/bin/env python3
"""Execute the literal revised memory listing and its stated boundary experiments."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'revision/main/addresses-and-memory.tex'
MASK64 = (1 << 64) - 1
BASE = 0x10010000
COLOR = 0x00336699
GUARD = 0x13579bdf


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def signed_word(value):
    return (value if value < 0x80000000 else value - (1 << 32)) & MASK64


def expected(blue=255, guard=GUARD):
    memory = struct.pack('<II', (COLOR & ~255) | (blue & 255), guard) + bytes(8)
    registers = dict(t0=BASE, t1=COLOR, t2=COLOR, t3=153,
                     t4=(-103 & MASK64), t5=blue & MASK64,
                     t6=(COLOR & ~255) | (blue & 255), t7=signed_word(guard))
    return memory, registers


def verify(java, cli, output):
    source_bytes = SOURCE.read_bytes()
    blocks = re.findall(r'\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}', source_bytes.decode('utf-8'), re.S)
    if len(blocks) != 1:
        raise ValueError('Expected one complete memory program')
    listing = blocks[0]
    if (ROOT/'revision/labs/files/memory-pixel.asm').read_text() != listing:
        raise ValueError('Downloadable program differs from the native listing')
    output.mkdir(parents=True, exist_ok=False)
    (output/'chapter-source.tex').write_bytes(source_bytes)
    runs = []

    def run(name, source):
        asm = output/(name+'.asm'); asm.write_text(source)
        dump = output/(name+'.bin')
        command = [str(java), '-jar', str(cli), '--start-at-main', '--max-steps', '1000',
                   '--dump', '.data', 'Binary', str(dump)]
        for n in range(8):
            command += ['--register', 't'+str(n)]
        command.append(str(asm))
        result = subprocess.run(command, capture_output=True, text=True, timeout=15)
        registers = {m[1]: int(m[2],16) for m in re.finditer(r'^\$(t[0-7])\t0x([0-9a-f]{16})$', result.stdout,re.M)}
        if result.returncode or result.stderr or len(registers) != 8 or result.stdout.split('\n$t0\t')[0]:
            raise AssertionError((name,result.returncode,result.stdout,result.stderr))
        data = dump.read_bytes()
        if len(data) < 16 or any(data[16:]):
            raise AssertionError('Unexpected write outside the complete declared data image: '+name)
        runs.append(dict(name=name,sourceSha256=sha(asm),dataSha256=sha(dump),dataBytes=len(data),
                         command=command,exitCode=result.returncode,stdout=result.stdout,stderr=result.stderr,
                         registers=registers,firstSixteenBytes=data[:16].hex()))
        return data[:16], registers

    if run('literal-final',listing) != expected():
        raise AssertionError('Literal program differs from the chapter')
    for label in ('store_color','inspect_initial','change_blue','inspect_final'):
        fragment = listing.partition(label+':\n')[0] + '    li $v0, 10\n    syscall\n'
        memory, registers = run('boundary-'+label, fragment)
        desired_memory = struct.pack('<II',0 if label=='store_color' else COLOR,GUARD)+bytes(8)
        desired_registers = dict(t0=BASE,t1=COLOR,t2=0,t3=0,t4=0,t5=0,t6=0,t7=0)
        if label in ('change_blue','inspect_final'):
            desired_registers.update(t2=COLOR,t3=153,t4=(-103 & MASK64),t5=255)
        if label=='inspect_final':
            desired_memory=expected()[0]
        if (memory,registers)!=(desired_memory,desired_registers):
            raise AssertionError('Boundary differs: '+label)
    for blue in range(256):
        variant=listing.replace('li $t5, 0xff',f'li $t5, {blue}')
        if run('blue-'+str(blue),variant)!=expected(blue):
            raise AssertionError('Byte replacement differs: '+str(blue))
    for guard in (0,0xffffffff,0x80000000,0xa55aa55a):
        variant=listing.replace('0x13579bdf',f'0x{guard:08x}')
        if run('guard-'+str(guard),variant)!=expected(guard=guard):
            raise AssertionError('Guard changed')
    if run('wide-byte-source',listing.replace('li $t5, 0xff','li $t5, 0x1234ff'))!=expected(0x1234ff):
        raise AssertionError('Byte store failed to select low bits')
    faults = {
        'wrong-byte-offset':listing.replace('sb $t5, 0($t0)','sb $t5, 1($t0)'),
        'wrong-store-width':listing.replace('sb $t5, 0($t0)','sw $t5, 0($t0)'),
        'neighbor-overwrite':listing.replace('sb $t5, 0($t0)','sb $t5, 4($t0)'),
        'unaligned-overlap':listing.replace('sb $t5, 0($t0)','sw $t5, 1($t0)'),
        'signed-component':listing.replace('lbu $t3','lb $t3'),
        'address-for-contents':listing.replace('lw $t6, 0($t0)','move $t6, $t0'),
    }
    for name,variant in faults.items():
        if variant==listing or run(name,variant)==expected():
            raise AssertionError('Fault escaped the full memory/register contract: '+name)
    # Bind the exact teaching predictions for the four store faults, not just inequality.
    by_name={r['name']:r for r in runs}
    for name,word,guard in [('wrong-byte-offset',0x0033ff99,GUARD),
                            ('wrong-store-width',0xff,GUARD),
                            ('neighbor-overwrite',COLOR,0x13579bff),
                            ('unaligned-overlap',0xff99,0x13579b00)]:
        assert by_name[name]['firstSixteenBytes']==(struct.pack('<II',word,guard)+bytes(8)).hex()
    receipt=dict(sourceSha256=hashlib.sha256(source_bytes).hexdigest(),listingSha256=hashlib.sha256(listing.encode()).hexdigest(),
                 cliSha256=sha(cli),verifierSha256=sha(Path(__file__)),literalCases=1,prefixBoundaryCases=4,
                 byteReplacementCases=256,alternateGuardCases=4,wideByteSourceCases=1,assembledFaultsRejected=6,runs=runs,
                 scope='CLI execution of the literal listing, four terminated prefixes, byte/guard variants and six assembled faults. Complete declared data and zero dump tail, full t0..t7 and silent normal exit. Not an IDE capture, full write-history proof or student-solution grader.')
    (output/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print('Passed literal listing, four boundaries, 256 byte replacements, four guards and byte truncation; six assembled faults exposed.')


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    for name in ('java','cli','output'):
        parser.add_argument('--'+name,type=Path,required=True)
    args=parser.parse_args()
    verify(args.java.resolve(),args.cli.resolve(),args.output.resolve())
