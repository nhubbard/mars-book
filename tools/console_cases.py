#!/usr/bin/env python3
"""Validate public console-case data and render checks for the existing MARS grading DSL.

This generates executable check files only. It never generates publication prose.
MARS executes the student's program; Python does not emulate MIPS instructions.
"""
import argparse
from dataclasses import dataclass
import json
import re
from pathlib import Path

MAX_BYTES = 1024 * 1024
MAX_CASES = 256
MAX_CASE_INSTRUCTIONS = 1_000_000
MAX_TOTAL_INSTRUCTIONS = 10_000_000
GENERATED = '// Generated public behavior checks; edit the adjacent .cases.json and regenerate.\n'


@dataclass(frozen=True)
class MemoryCheck:
    address: int
    bytes: str


@dataclass(frozen=True)
class StoreCheck:
    address: int
    length: int
    port: int | None = None
    bytes: str = ''


@dataclass(frozen=True)
class ReadCheck:
    address: int
    length: int
    allowed_address: int
    allowed_length: int


@dataclass(frozen=True)
class MarsBotCheck:
    instructions_per_tick: int
    x: int
    y: int
    heading: int
    moving: bool
    pen_down: bool
    trail: tuple[tuple[int, int], ...]


@dataclass(frozen=True)
class ConsoleCase:
    name: str
    input: str
    stdout: str
    instruction_limit: int
    memory: tuple[MemoryCheck, ...] = ()
    initial_memory: tuple[MemoryCheck, ...] = ()
    stores: tuple[StoreCheck, ...] = ()
    reads: tuple[ReadCheck, ...] = ()
    mars_bot: MarsBotCheck | None = None


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
    if type(root['formatVersion']) is not int or root['formatVersion'] not in (1, 2, 3, 4, 5):
        raise ValueError('Unsupported console-case format')
    rows = root['cases']
    if not isinstance(rows, list) or not 1 <= len(rows) <= MAX_CASES:
        raise ValueError('Declare one through 256 console cases')
    cases, names = [], set()
    for row in rows:
        fields = {'name', 'input', 'stdout', 'instructionLimit'}
        version = root['formatVersion']
        allowed = fields | ({'memory'} if version == 2 else {'memory', 'initialMemory', 'stores', 'reads'} if version >= 3 else set())
        if version == 5:
            allowed |= {'marsBot'}
        if not isinstance(row, dict) or not fields <= set(row) <= allowed:
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
        maximum = 3_000_000 if version >= 4 else MAX_CASE_INSTRUCTIONS
        if type(limit) is not int or not 1 <= limit <= maximum:
            raise ValueError(f'Case instruction limit must be an integer from 1 through {maximum}')
        memory = parse_memory(row['memory']) if 'memory' in row else ()
        initial = parse_memory(row['initialMemory']) if 'initialMemory' in row else ()
        stores = parse_stores(row['stores']) if 'stores' in row else ()
        reads = parse_reads(row['reads']) if 'reads' in row else ()
        if len(stores) + len(reads) > 64 or sum(len(item.bytes) // 2 for item in stores) > 65536:
            raise ValueError('Declare at most 64 trace checks and 65536 expected store bytes per case')
        bot = parse_mars_bot(row['marsBot']) if 'marsBot' in row else None
        cases.append(ConsoleCase(name, row['input'], row['stdout'], limit, memory, initial, stores, reads, bot))
    if sum(case.instruction_limit for case in cases) > MAX_TOTAL_INSTRUCTIONS:
        raise ValueError('Combined case instruction limits exceed 10000000')
    return tuple(cases)


def parse_mars_bot(row):
    fields = {'instructionsPerTick', 'x', 'y', 'heading', 'moving', 'penDown', 'trail'}
    if not isinstance(row, dict) or set(row) != fields:
        raise ValueError('MarsBot needs its movement rate, final state and complete trail')
    for field, maximum, minimum in [('instructionsPerTick', 1_000_000, 1), ('x', 511, 0), ('y', 511, 0), ('heading', 359, 0)]:
        if type(row[field]) is not int or not minimum <= row[field] <= maximum:
            raise ValueError('Invalid integral MarsBot field: ' + field)
    if type(row['moving']) is not bool or type(row['penDown']) is not bool:
        raise ValueError('MarsBot movement and pen flags must be booleans')
    trail = row['trail']
    if not isinstance(trail, list) or len(trail) == 1 or len(trail) > 256:
        raise ValueError('Choose an empty trail or two through 256 integral board vertices')
    vertices = []
    for point in trail:
        if not isinstance(point, dict) or set(point) != {'x', 'y'}:
            raise ValueError('A MarsBot vertex needs x and y only')
        if any(type(point[field]) is not int or not 0 <= point[field] <= 511 for field in ['x', 'y']):
            raise ValueError('MarsBot vertices must be integral board coordinates')
        vertices.append((point['x'], point['y']))
    return MarsBotCheck(row['instructionsPerTick'], row['x'], row['y'], row['heading'], row['moving'], row['penDown'], tuple(vertices))


def parse_memory(rows):
    if not isinstance(rows, list) or len(rows) > 64:
        raise ValueError('Declare at most 64 memory expectation ranges')
    ranges = []
    total = 0
    for row in rows:
        if not isinstance(row, dict) or set(row) != {'address', 'bytes'}:
            raise ValueError('Expected memory address and bytes only')
        address, hex_bytes = row['address'], row['bytes']
        if not isinstance(address, str) or not re.fullmatch(r'0x[0-7][0-9a-fA-F]{15}', address):
            raise ValueError('Memory addresses must be nonnegative 64-bit hexadecimal strings')
        if not isinstance(hex_bytes, str) or not re.fullmatch(r'(?:[0-9a-fA-F]{2})+', hex_bytes):
            raise ValueError('Memory expectations need nonempty hexadecimal byte pairs')
        address = int(address[2:], 16)
        length = len(hex_bytes) // 2
        total += length
        if total > MAX_BYTES or address + length - 1 > 0x7fffffffffffffff:
            raise ValueError('Memory expectations exceed 1 MiB per case or overflow their address')
        ranges.append(MemoryCheck(address, hex_bytes))
    ordered = sorted(ranges, key=lambda item: item.address)
    if any(a.address + len(a.bytes) // 2 > b.address for a, b in zip(ordered, ordered[1:])):
        raise ValueError('Memory expectation ranges must not overlap')
    return tuple(ranges)


def address(row, field):
    value = row[field]
    if not isinstance(value, str) or not re.fullmatch(r'0x[0-7][0-9a-fA-F]{15}', value):
        raise ValueError('Memory addresses must be nonnegative 64-bit hexadecimal strings')
    return int(value[2:], 16)


def integer(row, field, *, zero=False):
    value = row[field]
    if type(value) is not int or not (0 if zero else 1) <= value <= MAX_BYTES:
        raise ValueError('Expected an exact bounded integer: ' + field)
    return value


def trace_rows(rows):
    if not isinstance(rows, list) or len(rows) > 64 or any(not isinstance(row, dict) for row in rows):
        raise ValueError('Declare at most 64 trace-check objects')
    return rows


def trace_region(row):
    start, length = address(row, 'address'), integer(row, 'length')
    if start + length - 1 > 0x7fffffffffffffff:
        raise ValueError('Trace region overflows its address')
    return start, length


def parse_stores(rows):
    result = []
    for row in trace_rows(rows):
        base = {'kind', 'address', 'length'}
        kind = row.get('kind')
        if kind == 'none' and set(row) == base:
            result.append(StoreCheck(*trace_region(row)))
        elif kind == 'bytes' and set(row) == base | {'port', 'bytes'}:
            start, length = trace_region(row)
            port, hex_bytes = address(row, 'port'), row['bytes']
            if not start <= port < start + length:
                raise ValueError('Expected port must be inside the traced region')
            if not isinstance(hex_bytes, str) or len(hex_bytes) > 65536 * 2 or not re.fullmatch(r'(?:[0-9a-fA-F]{2})*', hex_bytes):
                raise ValueError('Store expectations need hexadecimal byte pairs, at most 65536 bytes')
            result.append(StoreCheck(start, length, port, hex_bytes))
        else:
            raise ValueError('Unsupported store check kind or fields')
    return tuple(result)


def parse_reads(rows):
    result = []
    for row in trace_rows(rows):
        if set(row) != {'address', 'length', 'allowedAddress', 'allowedLength'}:
            raise ValueError('A read check needs its traced and permitted regions')
        start, length = trace_region(row)
        allowed, size = address(row, 'allowedAddress'), integer(row, 'allowedLength', zero=True)
        if not start <= allowed <= start + length or size > start + length - allowed:
            raise ValueError('Permitted reads must form a subrange of the traced region')
        result.append(ReadCheck(start, length, allowed, size))
    return tuple(result)


def load(path):
    with path.open('rb') as source:
        return parse(source.read(MAX_BYTES + 1))


def kotlin_string(value):
    """Escape Kotlin data and avoid the JVM constant-pool string size limit."""
    if len(value) > 4096:
        chunks = [kotlin_string(value[start:start + 4096]) for start in range(0, len(value), 4096)]
        return 'listOf(' + ', '.join(chunks) + ').joinToString("")'
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


def byte_arguments(hex_bytes):
    if not hex_bytes:
        return ''
    if len(hex_bytes) <= 64:
        return ', ' + ', '.join('0x' + hex_bytes[start:start + 2] for start in range(0, len(hex_bytes), 2))
    return ', *(' + kotlin_string(hex_bytes) + '.chunked(2).map { it.toInt(16) }.toIntArray())'


def render(cases):
    lines = [GENERATED, '// Each case runs in a fresh simulator; labels and scratch registers are not checked.\n\n']
    for case in cases:
        lines.append(f'case({kotlin_string(case.name)}, input = {kotlin_string(case.input)}, instructionLimit = {case.instruction_limit}) {{\n')
        if case.mars_bot is not None:
            lines.append(f'    val bot = connectMarsBot({case.mars_bot.instructions_per_tick})\n')
        for initial in case.initial_memory:
            lines.append(f'    setBytes(0x{initial.address:016x}L{byte_arguments(initial.bytes)})\n')
        for index, store in enumerate(case.stores):
            lines.append(f'    val stores{index} = traceMemoryStores(0x{store.address:016x}L, {store.length})\n')
        for index, read in enumerate(case.reads):
            lines.append(f'    val reads{index} = traceMemoryAccesses(0x{read.address:016x}L, {read.length})\n')
        lines.append('    runToExit()\n')
        if case.mars_bot is not None:
            bot = case.mars_bot
            lines.append(f'    bot.expectState({bot.x}.0, {bot.y}.0, {bot.heading}, moving = {str(bot.moving).lower()}, penDown = {str(bot.pen_down).lower()})\n')
            vertices = ', '.join(f'{x}.0 to {y}.0' for x, y in bot.trail)
            lines.append(f'    bot.expectTrail({vertices})\n')
        lines.append(f'    expectOutput({kotlin_string(case.stdout)})\n')
        for memory in case.memory:
            lines.append(f'    expectBytes(0x{memory.address:016x}L{byte_arguments(memory.bytes)})\n')
        for index, store in enumerate(case.stores):
            if store.port is None:
                lines.append(f'    stores{index}.expectNone()\n')
            else:
                lines.append(f'    stores{index}.expectByteStores(0x{store.port:016x}L{byte_arguments(store.bytes)})\n')
        for index, read in enumerate(case.reads):
            lines.append(f'    reads{index}.expectReadsWithin(0x{read.allowed_address:016x}L, {read.allowed_length})\n')
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
