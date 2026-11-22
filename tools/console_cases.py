#!/usr/bin/env python3
"""Validate public console-case data and render checks for the existing MARS grading DSL.

This generates executable check files only. It never generates publication prose.
MARS executes the student's program; Python does not emulate MIPS instructions.
"""
import argparse
from dataclasses import dataclass
import json
from pathlib import Path

MAX_BYTES = 1024 * 1024
MAX_CASES = 256
MAX_CASE_INSTRUCTIONS = 1_000_000
MAX_TOTAL_INSTRUCTIONS = 10_000_000
GENERATED = '// Generated public behavior checks; edit the adjacent .cases.json and regenerate.\n'


@dataclass(frozen=True)
class ConsoleCase:
    name: str
    input: str
    stdout: str
    instruction_limit: int


def _object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError('Duplicate console-case field: ' + key)
        result[key] = value
    return result


def _constant(value):
    raise ValueError('Invalid JSON numeric constant: ' + value)


def parse(data):
    if len(data) > MAX_BYTES:
        raise ValueError('Console-case data exceeds 1 MiB')
    try:
        root = json.loads(data.decode('utf-8'), object_pairs_hook=_object, parse_constant=_constant)
    except RecursionError as error:
        raise ValueError('Console-case JSON is nested too deeply') from error
    if not isinstance(root, dict) or set(root) != {'formatVersion', 'cases'}:
        raise ValueError('Expected exactly formatVersion and cases')
    if type(root['formatVersion']) is not int or root['formatVersion'] != 1:
        raise ValueError('Unsupported console-case format')
    rows = root['cases']
    if not isinstance(rows, list) or not 1 <= len(rows) <= MAX_CASES:
        raise ValueError('Declare one through 256 console cases')
    cases, names = [], set()
    for row in rows:
        if not isinstance(row, dict) or set(row) != {'name', 'input', 'stdout', 'instructionLimit'}:
            raise ValueError('Unsupported or missing console-case fields')
        name = row['name']
        if not isinstance(name, str) or not name.strip() or len(name) > 128 or not name.isprintable():
            raise ValueError('Case names must be printable nonblank single-line text up to 128 characters')
        if name in names:
            raise ValueError('Case names must be unique')
        names.add(name)
        for field in ['input', 'stdout']:
            value = row[field]
            if not isinstance(value, str) or len(value.encode('utf-8')) > 64 * 1024:
                raise ValueError('Console-case text must be UTF-8 and at most 64 KiB per field')
        limit = row['instructionLimit']
        if type(limit) is not int or not 1 <= limit <= MAX_CASE_INSTRUCTIONS:
            raise ValueError('Case instruction limit must be an integer from 1 through 1000000')
        cases.append(ConsoleCase(name, row['input'], row['stdout'], limit))
    if sum(case.instruction_limit for case in cases) > MAX_TOTAL_INSTRUCTIONS:
        raise ValueError('Combined case instruction limits exceed 10000000')
    return tuple(cases)


def load(path):
    with path.open('rb') as source:
        return parse(source.read(MAX_BYTES + 1))


def kotlin_string(value):
    """Escape data as one Kotlin string, including interpolation and JSON-only escapes."""
    escaped = []
    special = {'"': '\\"', '\\': '\\\\', '$': '\\$', '\n': '\\n', '\r': '\\r', '\t': '\\t', '\b': '\\b'}
    for char in value:
        if char in special:
            escaped.append(special[char])
        elif 32 <= ord(char) <= 126:
            escaped.append(char)
        elif ord(char) <= 0xffff:
            escaped.append(f'\\u{ord(char):04x}')
        else:
            offset = ord(char) - 0x10000
            escaped.append(f'\\u{0xd800 + (offset >> 10):04x}\\u{0xdc00 + (offset & 1023):04x}')
    return '"' + ''.join(escaped) + '"'


def render(cases):
    lines = [GENERATED, '// Each case runs in a fresh simulator; labels and scratch registers are not checked.\n\n']
    for case in cases:
        lines.append(f'case({kotlin_string(case.name)}, input = {kotlin_string(case.input)}, instructionLimit = {case.instruction_limit}) {{\n')
        lines.append('    runToExit()\n')
        lines.append(f'    expectOutput({kotlin_string(case.stdout)})\n')
        lines.append('}\n\n')
    return ''.join(lines).removesuffix('\n')


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('cases', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--check', action='store_true', help='Check the generated file without writing it')
    args = parser.parse_args(argv)
    try:
        if args.output.suffix != '.kts' or args.output.is_symlink() or args.output.resolve() == args.cases.resolve():
            raise ValueError('Choose a distinct regular .kts output file')
        content = render(load(args.cases))
        if args.check:
            if args.output.read_text(encoding='utf-8') != content:
                raise ValueError('Generated public checks differ from their console-case data')
        else:
            if args.output.exists() and not args.output.read_text(encoding='utf-8').startswith(GENERATED):
                raise ValueError('Refusing to overwrite a handwritten check file')
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(content, encoding='utf-8', newline='\n')
    except (ValueError, OSError) as error:
        parser.error(str(error))
    print(args.output)
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
