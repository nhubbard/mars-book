#!/usr/bin/env python3
"""Prepare PCM WAV observation data while preserving all authored instructions."""
import argparse
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent
TARGETS = ['bound', 'parse', 'filter', 'encode', 'load', 'save', 'pipeline', 'kernel']


def unsigned(value, bits=64):
    if not isinstance(value, str) or re.fullmatch(r'0|[1-9][0-9]*', value) is None:
        raise ValueError('Controls use canonical unsigned decimal strings')
    number = int(value)
    if number >= 2**bits:
        raise ValueError('Control exceeds its unsigned field width')
    return number


def octets(value, limit):
    if not isinstance(value, str) or re.fullmatch(r'(?:[0-9a-f]{2})*', value) is None:
        raise ValueError('Bytes use lowercase hexadecimal pairs')
    if len(value) > 2*limit:
        raise ValueError('Fixture exceeds its arena')
    return bytes.fromhex(value)


def signed(value, bits):
    return value if value < 2**(bits-1) else value-2**bits


def values(recipe):
    if recipe['target'] not in TARGETS:
        raise ValueError('Unknown caller mode')
    rate = unsigned(recipe['rate'], 32); channels = unsigned(recipe['channels'], 32)
    frames = unsigned(recipe['frames']); count = unsigned(recipe['count'])
    capacity = unsigned(recipe['capacity']); kernel_count = unsigned(recipe['kernelCount'])
    data = octets(recipe['fileHex'], 266272); pcm = octets(recipe['pcmHex'], 262176)
    kernel = octets(recipe['kernelHex'], 160); coeff = octets(recipe['coefficientHex'], 64)
    octets(recipe['inputFileHex'], 266240); octets(recipe['outputFileHex'], 266240)
    for key in ['inputExists', 'outputWritable']:
        if type(recipe[key]) is not bool:
            raise ValueError('File switches must be booleans')
    for key in ['readChunk', 'writeChunk']:
        if type(recipe[key]) is not int or not 1 <= recipe[key] <= 266240:
            raise ValueError('Transfer chunks must be positive bounded integers')
    if type(recipe['firstDescriptor']) is not int or not 3 <= recipe['firstDescriptor'] <= 31:
        raise ValueError('Fixture descriptor must be from three through thirty-one')
    for key in ['failReadAfter', 'failWriteAfter']:
        if recipe[key] is not None and (type(recipe[key]) is not int or not 0 <= recipe[key] <= 2**31-1):
            raise ValueError('Failure positions must be nullable nonnegative integers')
    supported = 8000 <= rate <= 48000 and channels in (1, 2) and frames <= 65536
    if supported and recipe['target'] in ['filter', 'encode']:
        selected = data if recipe['target'] == 'filter' else pcm
        if len(selected) != 2*channels*frames:
            raise ValueError('Observation must supply the complete selected PCM')
        if frames and recipe['target'] == 'filter' and len(coeff) != 16:
            raise ValueError('Nonempty filter observation needs eight coefficients')
    if recipe['target'] == 'kernel':
        if kernel_count > 64 or (kernel_count and (len(kernel) < 2*(7+kernel_count) or len(coeff) != 16)):
            raise ValueError('Kernel observation must supply its bounded window and coefficients')
    return rate, channels, frames, count, capacity, kernel_count, data, pcm, kernel, coeff


def arena(label, data, size, end):
    result = label + ':\n'
    if data:
        result += '    .byte ' + ','.join(str(signed(b, 8)) for b in data) + '\n'
    if len(data) < size:
        result += '    .byte -91:' + str(size-len(data)) + '\n'
    return result + end + ':'


def prepare(source, recipe):
    rate, channels, frames, count, capacity, kernel_count, data, pcm, kernel, coeff = values(recipe)
    if len(re.findall(r'^\.text$', source, re.M)) != 1 or source.count('\n.text\n') != 1:
        raise ValueError('Keep the single supplied text-section boundary')
    declarations, instructions = source.split('\n.text\n')
    labels = ['source', 'source_end', 'metadata', 'samples', 'samples_end',
              'coeff', 'kernel_window', 'kernel_window_end', 'kernel_output']
    for label in labels:
        if len(re.findall(r'^' + label + ':', declarations, re.M)) != 1:
            raise ValueError('Keep unique supplied arena labels')
    for label, end, contents, size in [('source', 'source_end', data, 266272),
                                      ('samples', 'samples_end', pcm, 262176),
                                      ('kernel_window', 'kernel_window_end', kernel, 160),
                                      ('coeff', 'kernel_window', coeff, 64)]:
        declarations, changed = re.subn(r'^'+label+r':.*?^'+end+':',
            lambda _: arena(label, contents, size, end), declarations, flags=re.M | re.S)
        if changed != 1:
            raise ValueError('Missing arena boundary')
    prefix = recipe['target'] in ['bound', 'filter', 'encode']
    metadata = ('metadata: .word '+str(signed(rate, 32))+','+str(signed(channels, 32))+
                '\n    .dword '+str(signed(frames, 64))+'\n    .byte -91:48\n'
                if prefix else 'metadata: .byte -91:64\n')
    declarations, changed = re.subn(r'^metadata:.*?(?=^samples:)', lambda _: metadata,
                                    declarations, flags=re.M | re.S)
    if changed != 1:
        raise ValueError('Missing metadata boundary')
    for name, number in dict(mode=TARGETS.index(recipe['target']), byte_count=count,
                             byte_capacity=capacity, kernel_count=kernel_count).items():
        directive = 'word' if name == 'mode' else 'dword'
        pattern = r'^('+name+r': \.'+directive+r' )-?[0-9]+([ \t]*(?:#[^\n]*)?)$'
        declarations, changed = re.subn(pattern,
            lambda m: m[1]+str(signed(number, 32 if name == 'mode' else 64))+m[2],
            declarations, flags=re.M)
        if changed != 1:
            raise ValueError('Keep unique scalar caller controls')
    return declarations+'\n.text\n'+instructions


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('recipe')
    parser.add_argument('--source', type=Path, default=ROOT/'main.asm')
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    recipes = json.loads((ROOT/'inputs.json').read_text())['recipes']
    selected = [q for q in recipes if q['id'] == args.recipe]
    if len(selected) != 1:
        parser.error('Choose one exact observation recipe ID')
    prepared = prepare(args.source.read_text(), selected[0])
    with args.output.open('x') as stream:
        stream.write(prepared)
