"""Create fixed guest bytecode fixtures without changing a student's native interpreter."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import struct

ROOT = Path(__file__).resolve().parents[1]
OPCODES = {'HALT': (0x00, 1), 'PUSH8': (0x01, 2), 'PUSH32': (0x02, 5),
           'ADD': (0x10, 1), 'SUB': (0x11, 1), 'MUL': (0x12, 1), 'DIV': (0x13, 1),
           'DUP': (0x20, 1), 'DROP': (0x21, 1), 'SWAP': (0x22, 1), 'OVER': (0x23, 1),
           'JMP': (0x30, 2), 'JZ': (0x31, 2)}


def assemble(rows):
    """Resolve byte-offset labels in small fixture descriptions; do not execute guests."""
    labels, instructions, cursor = {}, [], 0
    for row in rows:
        if not row:
            raise ValueError('An instruction description cannot be empty')
        name, *operands = row
        if name == 'LABEL':
            if len(operands) != 1 or not isinstance(operands[0], str) or not operands[0] or operands[0] in labels:
                raise ValueError('Use distinct nonempty labels')
            labels[operands[0]] = cursor
            continue
        if name not in OPCODES:
            raise ValueError('Unknown fixture opcode: ' + str(name))
        opcode, width = OPCODES[name]
        if len(operands) != (1 if width > 1 else 0):
            raise ValueError('Wrong operand count for ' + name)
        instructions.append((cursor, name, opcode, operands))
        cursor += width
    if cursor > 64:
        raise ValueError('The teaching image is limited to 64 bytes')
    starts = {at for at, *_ in instructions}
    output = bytearray()
    for _, name, opcode, operands in instructions:
        output.append(opcode)
        if name == 'PUSH8':
            value = operands[0]
            if not isinstance(value, int) or not -128 <= value <= 127:
                raise ValueError('PUSH8 needs a signed-byte value')
            output.append(value & 255)
        elif name == 'PUSH32':
            value = operands[0]
            if not isinstance(value, int) or not -(2**31) <= value < 2**31:
                raise ValueError('PUSH32 needs a signed-word value')
            output.extend(struct.pack('<i', value))
        elif name in ('JMP', 'JZ'):
            target = labels.get(operands[0])
            if target not in starts:
                raise ValueError('Branch label must name an instruction, not the image end')
            output.append(target)
    return bytes(output)


def material(name):
    if name in ('countdown', 'bad-target'):
        data = assemble([('PUSH8', 0), ('PUSH8', 3), ('LABEL', 'loop'), ('DUP',),
                         ('JZ', 'exit'), ('SWAP',), ('OVER',), ('ADD',), ('SWAP',),
                         ('PUSH8', 1), ('SUB',), ('JMP', 'loop'), ('LABEL', 'exit'),
                         ('DROP',), ('HALT',)])
        if name == 'bad-target':
            data = data[:6] + b'\x06' + data[7:]  # Deliberately target the JZ operand byte.
        return data
    programs = {
        'signed-word': [('PUSH32', -(2**31)), ('HALT',)],
        'add-overflow': [('PUSH32', 2**31-1), ('PUSH8', 1), ('ADD',), ('HALT',)],
        'zero-divisor': [('PUSH8', 7), ('PUSH8', 0), ('DIV',), ('HALT',)],
        'end-fetch': [('PUSH8', 7)],
        'self-loop': [('LABEL', 'loop'), ('JMP', 'loop')],
        'halt': [('HALT',)],
    }
    malformed = {'structural-priority': b'\x30\x01\xff', 'after-halt': b'\x00\xff',
                 'truncated-word': b'\x02\x00\x00\x00', 'empty': b''}
    if name in malformed:
        return malformed[name]
    if name not in programs:
        raise ValueError('Unknown fixture: ' + name)
    return assemble(programs[name])


RECIPES = ('countdown', 'bad-target', 'signed-word', 'add-overflow', 'zero-divisor',
           'end-fetch', 'self-loop', 'halt', 'structural-priority', 'after-halt',
           'truncated-word', 'empty')


def prepare_source(source, image, capacity, budget):
    if len(image) > 64 or not 0 <= capacity <= 8 or not 0 <= budget <= 64:
        raise ValueError('Use at most 64 bytes, capacity 0..8 and guest budget 0..64')
    native = source.split('.text', 1)
    if len(native) != 2:
        raise ValueError('Expected the supplied caller/data harness')
    block = 'code_a: ' + ('.byte ' + ','.join(str(b) for b in image) + '\n    ' if image else '') + '.space ' + str(96-len(image)) + '\ncode_a_end:'
    source, count = re.subn(r'^code_a:.*?^code_a_end:', lambda _: block, source, count=0, flags=re.M | re.S)
    if count != 1:
        raise ValueError('Expected one 96-byte code_a arena and end guard')
    for label, value in [('length', len(image)), ('fuel', budget)]:
        source, count = re.subn(r'^' + label + r':\s*\.word[^\n]*', label + ': .word ' + str(value), source, flags=re.M)
        if count != 1:
            raise ValueError('Expected one ' + label + ' control word')
    pattern = r'^(state_a:\s*\.word\s+)([^\n]+)'
    def descriptor(match):
        values = match[2].split(',')
        if len(values) != 6:
            raise ValueError('Expected the supplied six-word descriptor prefix')
        values[4] = str(capacity)
        return match[1] + ','.join(values)
    source, count = re.subn(pattern, descriptor, source, flags=re.M)
    if count != 1 or source.split('.text', 1)[1] != native[1]:
        raise ValueError('Expected unchanged native code and one state_a descriptor')
    return source


def prepare(source, output, recipe='countdown', capacity=8, budget=64):
    image = material(recipe)
    text = prepare_source(source.read_text(), image, capacity, budget)
    receipt = output.with_suffix(output.suffix + '.json')
    if output.exists() or receipt.exists():
        raise FileExistsError('Choose a fresh assembly output and receipt path')
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open('x') as stream:
        stream.write(text)
    sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    receipt.write_text(json.dumps(dict(recipe=recipe, imageHex=image.hex(), imageBytes=len(image),
                                     capacity=capacity, guestBudget=budget, sourceSha256=sha(source),
                                     outputSha256=sha(output), helperSha256=sha(Path(__file__)),
                                     scope='Fixture bytes and caller controls only; native instruction text unchanged. No VM execution or solution supplied.'), indent=2) + '\n')
    return receipt


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('recipe', choices=RECIPES)
    parser.add_argument('output', type=Path)
    parser.add_argument('--source', type=Path, default=ROOT/'revision/labs/files/bytecode-vm.asm')
    parser.add_argument('--capacity', type=int, default=8)
    parser.add_argument('--budget', type=int, default=64)
    args = parser.parse_args()
    print(prepare(args.source, args.output, args.recipe, args.capacity, args.budget))
