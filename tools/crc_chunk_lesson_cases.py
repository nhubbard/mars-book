"""Preserve the public bounded CRC chunk corpus in native function data.

CRC values come from Python zlib, separately compared with the original JDK
oracle. This author emits finite checks; it does not interpret assembly.
"""
import argparse
import json
from pathlib import Path
import zlib
from function_lesson_cases import bits

MASK = (1 << 64) - 1
LENGTHS = [0, 1, 2, 3, 7, 8, 9, 15, 16, 31, 32, 255, 256, 4084]
TYPES = [b'IEND', b'tEXt', b'IDAT', bytes([0, 128, 255, 35])]


def little(value, width=4):
    return (value & ((1 << (width * 8)) - 1)).to_bytes(width, 'little')


def raw_update(data, seed=0):
    for byte in data:
        seed ^= byte
        for _ in range(8): seed = (seed >> 1) ^ (0xedb88320 if seed & 1 else 0)
    return seed


TABLE = [raw_update(bytes([n])) for n in range(256)]


def chunk(kind, payload, damage=False):
    checksum = zlib.crc32(kind + payload) ^ int(damage)
    return len(payload).to_bytes(4, 'big') + kind + payload + checksum.to_bytes(4, 'big')


def trials():
    rows = []
    for j, n in enumerate(LENGTHS):
        for k, kind in enumerate(TYPES):
            for damage in [False, True]:
                raw = chunk(kind, bytes((x * 73 + n) % 256 for x in range(n)), damage)
                rows.append(dict(name=f'chunk payload {n} type {k} checksum mismatch {str(damage).lower()}',
                                 raw=raw, selected=len(raw), offset=(j * 7 + k) % 32,
                                 tableOffset=((j + k) % 16) * 4, limit=1000000,
                                 group='short' if n <= 31 else 'long'))
    for count in [*range(12), 4097, -(1 << 63), -1]:
        rows.append(dict(name=f'rejected count {count}', raw=bytes(12), selected=count,
                         offset=3, tableOffset=12, limit=100000, group='rejections'))
    for n in [0, 1, 2, 7, 4084, 4085, 0x80000000, 0xffffffff]:
        rows.append(dict(name=f'mismatched unsigned length {n}', raw=n.to_bytes(4, 'big')+b'IEND'+bytes([165])*8,
                         selected=16, offset=3, tableOffset=12, limit=100000, group='rejections'))
    return rows


def outcome(trial):
    count = trial['selected'] & MASK
    valid_count = 12 <= count <= 4096
    declared = int.from_bytes(trial['raw'][:4], 'big') if valid_count else 0
    valid = valid_count and declared == count - 12
    actual = zlib.crc32(trial['raw'][4:count-4]) if valid else 0
    stored = int.from_bytes(trial['raw'][count-4:count], 'big') if valid else 0
    return dict(status=2 if not valid else 0 if actual == stored else 1,
                actual=actual, validCount=valid_count, valid=valid)


def construction(trial):
    source = bytearray([165]*4160); offset = trial['offset']
    source[offset:offset+len(trial['raw'])] = trial['raw']
    table = b''.join(little(TABLE[j-trial['tableOffset']//4] if 0 <= j-trial['tableOffset']//4 < 256 else 0x6a6b6c6d)
                     for j in range(272))
    return dict(source=bytes(source), table=table, **outcome(trial))


def window(label, offset, length):
    return dict(at=dict(label=label, offset=offset), length=length)


def policy(readable, writable=()):
    return {'from': dict(label='data'), 'until': dict(label='file_status', offset=4),
            'readable': list(readable), 'writable': list(writable), 'maxAccesses': 200000}


def preserved():
    return {**{f's{n}': bits(0x56789abc00000000 + n * 997) for n in range(8)},
            'fp': bits(0x56789abc00000000 + 8 * 997)}


def base(name, limit, initial, calls, restrictions):
    saved = preserved()
    return dict(name=name, instructionLimit=limit, initialMemory=initial, calls=calls,
                initialRegisters={**saved, 'sp': bits(0x7fff0000), **{f't{n}': bits(-1234-n) for n in range(10)}},
                preservedRegisters=list(saved), stackAlignment=16, callerStackBytes=0,
                accessPolicies=restrictions,
                noExecution=[{'from': 'results_ready', 'until': 'function_return'}])


def invocation(trial, result):
    addresses = {}
    arguments = {'a1': bits(trial['selected'])}
    if result['validCount']: addresses['a0'] = dict(label='data', offset=trial['offset'])
    else: arguments['a0'] = bits(1)
    if result['valid']: addresses['a2'] = dict(label='crc_table', offset=trial['tableOffset'])
    else: arguments['a2'] = bits(1)
    readable = ([window('data', trial['offset'], trial['selected']), window('crc_table', trial['tableOffset'], 1024)] if result['valid']
                else [window('data', trial['offset'], 4)] if result['validCount'] else [])
    return dict(entry='crc32_check_chunk', returnTo='function_return', arguments=arguments,
                argumentAddresses=addresses, expectedRegisters={'v0': bits(result['actual']), 'v1': bits(result['status'])},
                memory=[], accessPolicies=[policy(readable)])


def definition(trial):
    result = construction(trial); call = invocation(trial, result)
    return base(trial['name'], trial['limit'],
                [dict(label='data', bytes=result['source'].hex()), dict(label='crc_table', bytes=result['table'].hex())],
                [call], call['accessPolicies'])


def caller_case():
    raw = chunk(b'IEND', b''); source = raw + bytes([165])*(4160-len(raw))
    old_table = little(0x6a6b6c6d)*272; prepared = b''.join(little(n) for n in TABLE)+little(0x6a6b6c6d)*16
    initial = [dict(label='data', bytes=source.hex()), dict(label='crc_table', bytes=old_table.hex())]
    for label, value in [('input_count', 12), ('exercise', 4), ('raw_result', -1), ('file_status', -1)]:
        initial.append(dict(label=label, bytes=little(value).hex()))
    reads = [window('data', 0, 12), window('crc_table', 0, 1024), window('exercise', 0, 4), window('input_count', 0, 4)]
    table_write = window('crc_table', 0, 1024)
    publication = [window('raw_result', 0, 4), window('file_status', 0, 4)]
    expected = {'v0': bits(zlib.crc32(b'IEND')), 'v1': bits(0)}
    first = dict(entry='main', returnTo='chunk_done', arguments={}, expectedRegisters=expected,
                 memory=[dict(label='data', bytes=source.hex()), dict(label='crc_table', bytes=prepared.hex()),
                         dict(label='raw_result', bytes=little(-1).hex()), dict(label='file_status', bytes=little(-1).hex())],
                 accessPolicies=[policy(reads, [table_write])])
    second = dict(entry='chunk_done', returnTo='results_ready', arguments={}, expectedRegisters=expected,
                  memory=[dict(label='raw_result', bytes=little(zlib.crc32(b'IEND')).hex()), dict(label='file_status', bytes=little(0).hex())],
                  accessPolicies=[policy([], publication)])
    return base('ordinary independent caller', 100000, initial, [first, second], [policy(reads, [table_write]+publication)])


def reuse_case():
    source = bytearray([165]*4160)
    specifications = [(bytes(12), 11, 3), (chunk(b'IEND', b''), 12, 67),
                      ((7).to_bytes(4, 'big')+b'IEND'+bytes([165])*8, 16, 131),
                      (chunk(TYPES[3], b'\x00\x80\xff', True), 15, 195),
                      (chunk(TYPES[3], b'\x00\x80\xff'), 15, 259)]
    calls = []
    for raw, count, offset in specifications:
        source[offset:offset+len(raw)] = raw
        trial = dict(raw=raw, selected=count, offset=offset, tableOffset=0)
        calls.append(invocation(trial, outcome(trial)))
    initial = [dict(label='data', bytes=source.hex()), dict(label='crc_table', bytes=b''.join(little(n) for n in TABLE).hex()+little(0x6a6b6c6d).hex()*16)]
    all_reads = [w for call in calls for w in call['accessPolicies'][0]['readable']]
    return base('count rejection valid retry length mismatch checksum mismatch and retry', 1000000, initial, calls, [policy(all_reads)])


def corpus():
    rows = {name: [] for name in ['short', 'long', 'rejections', 'state']}
    for trial in trials(): rows[trial['group']].append(definition(trial))
    rows['state'] = [caller_case(), reuse_case()]
    return rows


def profiles():
    return {f'{group}-{start//10:02d}': dict(formatVersion=12, cases=cases[start:start+10])
            for group, cases in corpus().items() for start in range(0, len(cases), 10)}


def build(directory, check=False):
    directory = Path(directory)
    files = {f'crc-chunk-{name}.cases.json': json.dumps(profile, separators=(',', ':'))+'\n' for name, profile in profiles().items()}
    files['caller-exit.cases.json'] = json.dumps(dict(formatVersion=1, cases=[dict(name='silent CRC chunk caller exit', input='', stdout='', instructionLimit=100000)]), separators=(',', ':'))+'\n'
    if not check: directory.mkdir(parents=True, exist_ok=True)
    for name, text in files.items():
        path = directory/name
        if check:
            if not path.is_file() or path.read_text() != text: raise ValueError(f'Stale CRC profile: {path}')
        else: path.write_text(text)
    return files


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument('directory', type=Path); parser.add_argument('--check', action='store_true')
    args = parser.parse_args(); build(args.directory, args.check)
