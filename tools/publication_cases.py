"""Bounded data for continuous public-marker observations; never generates prose."""
from dataclasses import dataclass
import re

MAX_BYTES = 1024 * 1024


@dataclass(frozen=True)
class DataAddress:
    label: str
    offset: int = 0


@dataclass(frozen=True)
class LabelMemory:
    label: str
    bytes: str
    offset: int = 0
    repeat: int = 1

    @property
    def length(self):
        return len(self.bytes) // 2 * self.repeat


@dataclass(frozen=True)
class AddressMemory:
    at: DataAddress
    value: DataAddress


@dataclass(frozen=True)
class Window:
    at: DataAddress
    length: int


@dataclass(frozen=True)
class Policy:
    first: DataAddress
    until: DataAddress
    readable: tuple[Window, ...]
    writable: tuple[Window, ...]
    max_accesses: int = 65536
    check_reads: bool = True


@dataclass(frozen=True)
class State:
    memory: tuple[LabelMemory, ...]
    address_memory: tuple[AddressMemory, ...]
    access_policies: tuple[Policy, ...]
    label: str | None = None
    expected_registers: tuple[tuple[str, str], ...] = ()

    @property
    def length(self):
        return sum(item.length for item in self.memory) + 8 * len(self.address_memory)


@dataclass(frozen=True)
class Publication:
    label: str
    initial_memory: tuple[LabelMemory, ...]
    snapshots: tuple[State, ...]
    final_state: State


def fields(value, allowed, required=None):
    if not isinstance(value, dict) or not (allowed if required is None else required) <= set(value) <= allowed:
        raise ValueError('Unsupported or missing publication fields')


def integer(value, minimum, maximum):
    if type(value) is not int or not minimum <= value <= maximum:
        raise ValueError('Expected a bounded exact publication integer')
    return value


def label(value):
    if not isinstance(value, str) or len(value) > 128 or re.fullmatch(r'[A-Za-z_.$][A-Za-z0-9_.$]*', value) is None:
        raise ValueError('Expected a public assembly label')
    return value


def rows(value, maximum=64):
    if not isinstance(value, list) or len(value) > maximum:
        raise ValueError('Too many or malformed publication rows')
    return value


def address(value):
    fields(value, {'label', 'offset'}, {'label'})
    return DataAddress(label(value['label']), integer(value.get('offset', 0), -MAX_BYTES, MAX_BYTES))


def memory(value):
    result = []
    for item in rows(value):
        fields(item, {'label', 'bytes', 'offset', 'repeat'}, {'label', 'bytes'})
        at = address({key: item[key] for key in ['label', 'offset'] if key in item})
        raw = item['bytes']
        if not isinstance(raw, str) or re.fullmatch(r'(?:[0-9a-fA-F]{2})+', raw) is None:
            raise ValueError('Publication memory needs nonempty hexadecimal byte pairs')
        repeat = integer(item.get('repeat', 1), 1, MAX_BYTES)
        result.append(LabelMemory(at.label, raw, at.offset, repeat))
    if sum(item.length for item in result) > MAX_BYTES:
        raise ValueError('Publication memory exceeds one MiB')
    return tuple(result)


def windows(value):
    result = []
    for item in rows(value):
        fields(item, {'at', 'length'})
        result.append(Window(address(item['at']), integer(item['length'], 0, MAX_BYTES)))
    return tuple(result)


def policies(value):
    result = []
    for item in rows(value):
        fields(item, {'from', 'until', 'readable', 'writable', 'maxAccesses', 'checkReads'}, {'from', 'until'})
        readable, writable = windows(item.get('readable', [])), windows(item.get('writable', []))
        check = item.get('checkReads', True)
        if type(check) is not bool or not check and readable:
            raise ValueError('Store-only policies cannot silently ignore declared read windows')
        if len(readable) + len(writable) > 64 or sum(w.length for w in readable + writable) > MAX_BYTES:
            raise ValueError('Publication access windows exceed bounds')
        result.append(Policy(address(item['from']), address(item['until']), readable, writable,
                             integer(item.get('maxAccesses', 65536), 1, 1000000), check))
    if sum(p.max_accesses for p in result) > 64 * 65536 or sum(w.length for p in result for w in p.readable + p.writable) > MAX_BYTES:
        raise ValueError('Combined publication access policy budget exceeded')
    return tuple(result)


def registers(value):
    if not isinstance(value,dict) or len(value)>32:raise ValueError('Declare at most 32 register expectations')
    names=['zero','at','v0','v1','a0','a1','a2','a3',*[f't{i}' for i in range(8)],*[f's{i}' for i in range(8)],'t8','t9','k0','k1','gp','sp','fp','ra']
    seen=set();result=[]
    for key,raw in value.items():
        if not isinstance(key,str) or len(key)>32:raise ValueError('Invalid register name')
        name=key.removeprefix('$')
        index=names.index(name) if name in names else int(name) if re.fullmatch(r'0|[1-9][0-9]?',name) else -1
        if not 0<=index<32 or index in seen:raise ValueError('Unknown or duplicate integer register')
        if not isinstance(raw,str) or re.fullmatch(r'0x[0-9a-fA-F]{16}',raw) is None:raise ValueError('Expected exactly 64 register bits')
        seen.add(index);result.append((key,raw))
    return tuple(result)


def state(value,version=7):
    required={'memory','addressMemory','accessPolicies'}
    fields(value, required | ({'label','expectedRegisters'} if version>=8 else set()), required)
    pointers = []
    for item in rows(value['addressMemory']):
        fields(item, {'at', 'value'})
        pointers.append(AddressMemory(address(item['at']), address(item['value'])))
    contents = memory(value['memory'])
    if len(contents) + len(pointers) > 64:
        raise ValueError('Declare at most 64 publication results')
    return State(contents, tuple(pointers), policies(value['accessPolicies']),
                 label(value['label']) if 'label' in value else None, registers(value.get('expectedRegisters',{})))


def parse(value,version=7):
    fields(value, {'label', 'initialMemory', 'snapshots', 'finalState'})
    initial = memory(value['initialMemory'])
    snapshots = tuple(state(item,version) for item in rows(value['snapshots'], 256))
    final = state(value['finalState'],version)
    if final.label is not None:raise ValueError('Final publication state observes termination, not another marker')
    if sum(item.length for item in initial) + sum(item.length for item in snapshots) + final.length > MAX_BYTES:
        raise ValueError('Combined publication setup/results exceed one MiB')
    return Publication(label(value['label']), initial, snapshots, final)


IMPORTS = ('ConsolePublicationCheck', 'FunctionMemoryCheck', 'FunctionAddressMemoryCheck',
           'FunctionDataAddress', 'FunctionDataWindow', 'FunctionAccessPolicyCheck')


def render(value, quote):
    def sequence(items):
        return 'listOf(' + ', '.join(items) + ')'
    def at(item):
        return f'FunctionDataAddress({quote(item.label)}, {item.offset})'
    def mem(item):
        return f'FunctionMemoryCheck({quote(item.label)}, {quote(item.bytes)}, {item.offset}, {item.repeat})'
    def window(item):
        return f'FunctionDataWindow({at(item.at)}, {item.length})'
    def policy(item):
        return (f'FunctionAccessPolicyCheck({at(item.first)}, {at(item.until)}, '
                f'readable = {sequence(window(w) for w in item.readable)}, '
                f'writable = {sequence(window(w) for w in item.writable)}, '
                f'maxAccesses = {item.max_accesses}, checkReads = {str(item.check_reads).lower()})')
    def snapshot(item):
        pointers = sequence(f'FunctionAddressMemoryCheck({at(p.at)}, {at(p.value)})' for p in item.address_memory)
        return (f'ConsolePublicationCheck.State(memory = {sequence(mem(m) for m in item.memory)}, '
                f'addressMemory = {pointers}, accessPolicies = {sequence(policy(p) for p in item.access_policies)}'
                + (f', label = {quote(item.label)}' if item.label is not None else '')
                + (', expectedRegisters = mapOf(' + ', '.join(f'{quote(name)} to {bits}uL.toLong()' for name,bits in item.expected_registers) + ')' if item.expected_registers else '') + ')')
    return (f'ConsolePublicationCheck({quote(value.label)}, {sequence(mem(m) for m in value.initial_memory)}, '
            f'{sequence(snapshot(s) for s in value.snapshots)}, {snapshot(value.final_state)})')
