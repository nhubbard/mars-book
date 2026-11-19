#!/usr/bin/env python3
"""Build public CBOR final feedback from an independently authored host model."""
from pathlib import Path
import importlib.util
import itertools
import json

ROOT = Path(__file__).resolve().parents[1]
MODEL = ROOT/'tools/cbor-feedback/model.kts'
DEST = ROOT/'drafts/cbor-construction/checks'


def cases():
    groups = {name: [] for name in ['head', 'skip', 'decode', 'format', 'bound', 'encode', 'caller']}
    def add(group, title, body):
        groups[group].append('case('+json.dumps(title)+', instructionLimit=2000000) { '+body+' }\n')
    literal = lambda n: str(n)+'uL.toLong()' if n >= 2**63 else str(n)+'L'
    for initial in range(256):
        add('head', f'initial byte {initial:02x}', f'headCheck(bytes("{initial:02x}80ff0123456789ab"),index={initial})')
    for selector, width in [(24,1),(25,2),(26,4),(27,8)]:
        for size in range(1, width+1):
            raw = bytes([selector])+bytes(range(1, size))
            add('head', f'truncated width {width} available {size}', f'headCheck(bytes("{raw.hex()}"))')
    for count in [0, 1, 8, 9, 2**32, 2**63, 2**64-1]:
        add('head', f'full-width available {count}', f'headCheck(bytes("1b80ff0123456789ab"),{literal(count)})')
    for depth in range(9):
        for expression in ['bytes("17ff")', 'bytes("80")', 'bytes("8180")']:
            add('skip', f'inherited depth {depth} '+expression, f'skipCheck({expression},{depth}L)')
    for raw in ['4280ff','a10000','a20000180000','a100a10000','a100a20000180000',
                'a800000100020003000400050006000700','a10800','5821'+'00'*33,
                '9841'+'00'*65,'a9000001000200030004000500060007000800',
                '3bffffffffffffffff','9f00ff','d9d9f7a0']:
        add('skip', 'item '+raw[:24], f'skipCheck(bytes("{raw}"))')
    add('skip', 'array at maximum count', 'skipCheck(bytes("9840"+"00".repeat(64)))')
    add('skip', 'byte string at maximum count', 'skipCheck(bytes("5820"+"80".repeat(32)))')
    for count, depth in [(0,0),(1025,0),(1025,9),(2**32,0),(2**63,0),(2**64-1,0),(1,9),(1,2**63),(1,2**64-1)]:
        add('skip', f'early bounds {count} {depth}', f'skipCheck(bytes("00"),{literal(depth)},{literal(count)})')
    spec = importlib.util.spec_from_file_location('cbor_public_fixed', ROOT/'drafts/cbor-fixtures/fixture.py')
    fixed = importlib.util.module_from_spec(spec); spec.loader.exec_module(fixed)
    for name in ['trace','canonical','empty','maximum','budget']:
        raw = fixed.material(name)[0]
        add('decode', name, f'decodeCheck(bytes("{raw.hex()}"),index={len(raw)%17})')
        add('skip', name, f'skipCheck(bytes("{raw.hex()}ff"))' if len(raw)<1024 else f'skipCheck(bytes("{raw.hex()}"))')
    ticks = [0,1,23,24,255,256,65535,65536,2**32-1,2**32,2**63,2**64-1]
    for i, (tick, length, count) in enumerate(itertools.product(ticks, [0,32], [0,1,64])):
        add('decode', f'boundary record {tick} {length} {count}', f'decodeCheck(preferred(example({tick}uL,{length},{count})),index={i})')
    fields = ['00'+'1b0102030405060708', '014300ff41', '028a001718182037381818ff190100197fff397fff']
    for order in itertools.permutations(range(3)):
        raw = 'a4'+'03a201824280ff210080'+''.join(fields[i] for i in order)
        add('decode', 'required key order '+str(order), f'decodeCheck(bytes("{raw}"))')
    full = 'a8'+''.join(fields)+ '033bffffffffffffffff'+'049840'+'00'*64+'055820'+'ff'*32+'06a800000100020003000400050006000700'+'07'+'81'*6+'80'
    add('decode','all root keys and deepest empty container',f'decodeCheck(bytes("{full}"))')
    for size in [0,1,6,7,8,15,34,40,51]:
        add('format', f'truncated trace {size}', f'decodeCheck(traceInput.copyOf({size}))')
    add('format', 'trailing byte', 'decodeCheck(traceInput+byteArrayOf(0))')
    for raw in ['a3000001400380','a3002001400280','a3000001600280','a3000001400200',
                'a3000001400281198000','a3000001400281398000','a3000001400281420000',
                'a4000001400280180000','a400000140028003a20000180000',
                'a400000140028003'+'81'*7+'80', 'a30000015821'+'00'*33+'0280',
                'a300000140029841'+'00'*65, 'a200000140','a9000001400280030004000500060007000800',
                '9f0001400280ff','a300000140028003','83004080']:
        add('format', 'reject '+raw[:24], f'decodeCheck(bytes("{raw}"))')
    for count in [0,1,6,1025,2**32,2**63,2**64-1]:
        add('format', f'early decode {count}', f'decodeCheck(byteArrayOf(),{literal(count)})')
    records = [(t,l,n) for t,l,n in itertools.product(ticks,[0,3,32],[0,1,10,64])]
    for i,(tick,length,count) in enumerate(records):
        add('bound',f'exact size {tick} {length} {count}',f'recordCheck(example({tick}uL,{length},{count}),index={i})')
    for i,(tick,length,count) in enumerate(records[::3]+[(2**64-1,32,64),(0,0,0)]):
        record=f'example({tick}uL,{length},{count})'
        for label,capacity in [('exact',f'preferred({record}).size.toLong()'),('short',f'preferred({record}).size.toLong()-1'),('large','Long.MIN_VALUE')]:
            add('encode', f'{label} {tick} {length} {count} observation {i}', f'recordCheck({record},{capacity},index={i})')
    for capacity in ['37L','36L','Long.MIN_VALUE']:
        add('encode','worked capacity '+capacity,'recordCheck(worked,'+capacity+')')
    for l,n in [(33,0),(0,65),(2**31,0),(0,2**31),(2**32-1,64),(32,2**32-1)]:
        add('bound',f'invalid lengths {l} {n}',f'recordCheck(worked,badLength={l}L to {n}L)')
        add('encode',f'invalid lengths {l} {n}',f'recordCheck(worked,0,badLength={l}L to {n}L)')
    for count in [0,1,6,7,8,2**32,2**64-1]:
        add('encode',f'empty capacity {count}',f'recordCheck(example(0uL,0,0),{literal(count)})')
    for name in ['trace','maximum','budget']:
        raw=fixed.material(name)[0]
        add('caller','pipeline '+name,f'pipelineCheck(bytes("{raw.hex()}"))')
    add('caller','exact trace capacity','pipelineCheck(traceInput,37)')
    add('caller','short trace capacity','pipelineCheck(traceInput,36)')
    add('caller','trailing input','pipelineCheck(traceInput+byteArrayOf(0))')
    add('caller','unsupported root','pipelineCheck(bytes("a3000001600280"))')
    return groups


def build():
    DEST.mkdir(parents=True, exist_ok=True)
    groups=cases();model=MODEL.read_text()
    for name, lines in groups.items():
        (DEST/(name+'.kts')).write_text(model+'\n'+''.join(lines))
    return {name:len(lines) for name,lines in groups.items()}


if __name__ == '__main__':
    print(json.dumps(build(),indent=2))
