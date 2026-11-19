#!/usr/bin/env python3
"""Prepare CBOR observation data without changing the student's instruction section."""
import argparse
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent
TARGETS = ['head', 'skip', 'decode', 'bound', 'encode', 'pipeline', 'size', 'write']
CONTROLS = {'count': 'byte_count', 'capacity': 'byte_capacity', 'depth': 'inherited_depth',
            'sizeArgument': 'size_argument', 'writeMajor': 'write_major', 'writeArgument': 'write_value'}


def unsigned(value):
    if not isinstance(value, str) or re.fullmatch(r'0|[1-9][0-9]*', value) is None:
        raise ValueError('Use canonical unsigned decimal strings for controls')
    result = int(value)
    if result >= 2**64:
        raise ValueError('Control exceeds 64 bits')
    return result


def octets(value, limit):
    if not isinstance(value, str) or re.fullmatch(r'(?:[0-9a-f]{2})*', value) is None:
        raise ValueError('Use lowercase hexadecimal byte pairs')
    if len(value) > limit*2:
        raise ValueError('Fixture exceeds its arena')
    return bytes.fromhex(value)


def arena(label, raw, size):
    result = label+':\n'
    if raw:
        result += '    .byte '+','.join(str(v if v < 128 else v-256) for v in raw)+'\n'
    if len(raw) < size:
        result += '    .byte -91:'+str(size-len(raw))+'\n'
    return result+label+'_end:'


def prepare(source, recipe):
    if recipe['target'] not in TARGETS:
        raise ValueError('Unknown observation mode')
    controls = {key: unsigned(recipe[key]) for key in CONTROLS}
    if controls['writeMajor'] not in (0, 1, 2, 4, 5):
        raise ValueError('Private writer observations use a supported major type')
    values = [('source', octets(recipe['inputHex'], 1280), 1280),
              ('record', octets(recipe['recordHex'], 176), 256)]
    if source.count('\n.text\n') != 1 or len(re.findall(r'^\.text$', source, re.M)) != 1:
        raise ValueError('Keep one supplied text-section boundary')
    declarations, instructions = source.split('\n.text\n')
    for label in ['source', 'source_end', 'record', 'record_end', 'destination', 'destination_end']:
        if len(re.findall(r'^'+label+':', declarations, re.M)) != 1:
            raise ValueError('Keep unique supplied arena labels')
    for label, raw, size in values:
        declarations, changed = re.subn(r'^'+label+r':.*?^'+label+'_end:',
            lambda _: arena(label, raw, size), declarations, flags=re.M | re.S)
        if changed != 1:
            raise ValueError('Missing arena boundary')
    scalars = {CONTROLS[key]: value for key, value in controls.items()}
    scalars['mode'] = TARGETS.index(recipe['target'])
    for name, value in scalars.items():
        directive = 'word' if name == 'mode' else 'dword'
        signed = value if value < 2**63 else value-2**64
        declarations, changed = re.subn(r'^('+name+r': \.'+directive+r' )-?[0-9]+([ \t]*(?:#[^\n]*)?)$',
            lambda match: match[1]+str(signed)+match[2], declarations, flags=re.M)
        if changed != 1:
            raise ValueError('Keep unique scalar caller controls')
    return declarations+'\n.text\n'+instructions


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('recipe')
    parser.add_argument('--source', type=Path, default=ROOT/'starter/main.asm')
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    choices = [r for r in json.loads((ROOT/'inputs.json').read_text())['recipes'] if r['id'] == args.recipe]
    if len(choices) != 1:
        parser.error('Choose one exact recipe ID')
    result = prepare(args.source.read_text(), choices[0])
    with args.output.open('x') as stream:
        stream.write(result)
