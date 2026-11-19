#!/usr/bin/env python3
"""Prepare QOI observation data without modifying authored algorithm regions."""
import argparse
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent
TARGETS = ['bound', 'validate', 'decode', 'encode', 'load', 'save', 'pipeline']


def unsigned(value, bits=64):
    if not isinstance(value, str) or re.fullmatch(r'0|[1-9][0-9]*', value) is None:
        raise ValueError('Controls use canonical unsigned decimal strings')
    number = int(value)
    if not 0 <= number < 2**bits:
        raise ValueError('Control outside its unsigned field width')
    return number


def octets(value, limit):
    if not isinstance(value, str) or re.fullmatch(r'(?:[0-9a-f]{2})*', value) is None:
        raise ValueError('Bytes use lowercase hexadecimal pairs')
    data = bytes.fromhex(value)
    if len(data) > limit:
        raise ValueError('Fixture exceeds its arena')
    return data


def signed(number, bits):
    return number if number < 2**(bits-1) else number - 2**bits


def values(recipe):
    if recipe['target'] not in TARGETS:
        raise ValueError('Unknown caller mode')
    fields = [unsigned(recipe[name], 32) for name in ['width', 'height', 'channels', 'colorspace']]
    count = unsigned(recipe['count']); capacity = unsigned(recipe['capacity'])
    data = octets(recipe['fileHex'], 20544); rgba = octets(recipe['rgbaHex'], 16448)
    octets(recipe['inputFileHex'], 20544); octets(recipe['outputFileHex'], 20544)
    for key in ['inputExists', 'outputWritable', 'callerOnly']:
        if type(recipe[key]) is not bool:
            raise ValueError('Fixture switches are booleans')
    for key in ['readChunk', 'writeChunk']:
        if type(recipe[key]) is not int or not 1 <= recipe[key] <= 20544:
            raise ValueError('Transfer chunk must be a positive bounded integer')
    if type(recipe['firstDescriptor']) is not int or not 3 <= recipe['firstDescriptor'] <= 31:
        raise ValueError('Descriptor must be from three through thirty-one')
    for key in ['failReadAfter', 'failWriteAfter']:
        if recipe[key] is not None and (type(recipe[key]) is not int or not 0 <= recipe[key] <= 2**31-1):
            raise ValueError('Failure positions are nullable nonnegative integers')
    if recipe['target'] == 'encode' and (not 1 <= fields[0] <= 64 or not 1 <= fields[1] <= 64 or len(rgba) != 4*fields[0]*fields[1]):
        raise ValueError('Encoder fixture must supply all selected RGBA words')
    return fields, count, capacity, data, rgba


def arena(label, data, size, end):
    first = label + ':\n'
    if data:
        first += '    .byte ' + ','.join(str(signed(b, 8)) for b in data) + '\n'
    if len(data) < size:
        first += '    .byte -91:' + str(size-len(data)) + '\n'
    return first + end + ':'


def prepare(source, recipe):
    fields, count, capacity, data, rgba = values(recipe)
    for label in ['source', 'source_end', 'metadata', 'framebuffer', 'framebuffer_end']:
        if len(re.findall(r'^' + label + ':', source, re.M)) != 1:
            raise ValueError('Arena labels must be unique')
    words = bytes(component for i in range(0, len(rgba), 4) for component in (rgba[i+2], rgba[i+1], rgba[i], rgba[i+3]))
    source, changed = re.subn(r'^source:.*?^source_end:', lambda _: arena('source', data, 20544, 'source_end'), source, flags=re.M | re.S)
    if changed != 1: raise ValueError('Missing source arena')
    source, changed = re.subn(r'^framebuffer:.*?^framebuffer_end:', lambda _: arena('framebuffer', words, 16448, 'framebuffer_end'), source, flags=re.M | re.S)
    if changed != 1: raise ValueError('Missing framebuffer arena')
    metadata = 'metadata: .word ' + ','.join(str(signed(n, 32)) for n in fields) + '\n    .byte -91:48\n' if recipe['target'] == 'encode' else 'metadata: .byte -91:64\n'
    source, changed = re.subn(r'^metadata:.*?(?=^framebuffer:)', lambda _: metadata, source, flags=re.M | re.S)
    if changed != 1: raise ValueError('Missing metadata arena')
    controls = dict(mode=TARGETS.index(recipe['target']), dimensions=(fields[1] << 32) | fields[0], byte_count=count,
                    pixel_capacity=capacity if recipe['target'] == 'decode' else 4096,
                    byte_capacity=capacity if recipe['target'] in ['encode', 'load', 'pipeline'] else 20502)
    for name, number in controls.items():
        directive = 'word' if name == 'mode' else 'dword'
        pattern = r'^(' + name + r': \.' + directive + r' )-?[0-9]+([ \t]*(?:#[^\n]*)?)$'
        source, changed = re.subn(pattern, lambda m: m[1] + str(signed(number, 32 if name == 'mode' else 64)) + m[2], source, flags=re.M)
        if changed != 1: raise ValueError('Caller controls must have unique scalar declarations')
    return source


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('recipe')
    parser.add_argument('--source', type=Path, default=ROOT/'main.asm')
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    recipes = json.loads((ROOT/'inputs.json').read_text())['recipes']
    selected = [recipe for recipe in recipes if recipe['id'] == args.recipe]
    if len(selected) != 1:
        parser.error('Choose one exact observation recipe ID')
    result = prepare(args.source.read_text(), selected[0])
    with args.output.open('x') as stream:
        stream.write(result)
