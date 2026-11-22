"""Build the fifth unit's public function data and equivalent existing-DSL checks.

The native MARS reader owns format validation. This tool only authors the finite
case grid; it does not generate publication prose or emulate MIPS execution.
"""
import argparse
import json
from pathlib import Path

from console_cases import kotlin_string

MASK = (1 << 64) - 1
CONFIGURATIONS = (
    ('positive upper bits', 0x1234567800000000, 0x1122334400000000, 0x7fffeffc),
    ('sign bit set', 0x8000000000000000, 0xfedcba9800000000, 0x7fffe080),
    ('all upper bits set', 0xffffffffffffff00, 0xa5a55a5a00000000, 0x7fffd0f7),
)
GENERATED = '// Generated public function checks; regenerate with tools/function_lesson_cases.py.\n'


def bits(value):
    return f'0x{value & MASK:016x}'


def saved_values(seed):
    return {**{f's{i}': (seed + i * 0x01010101) & MASK for i in range(8)},
            'fp': seed ^ 0x0102030405060708, 'gp': seed ^ 0x1020304050607080}


def profile():
    cases = []
    for name, upper, seed, stack in CONFIGURATIONS:
        calls = []
        for byte in range(256):
            reversed_byte = int(f'{byte:08b}'[::-1], 2)
            calls.append(dict(entry='reverse_and_invert',
                              returnTo='after_invert' if byte % 2 == 0 else 'after_repeat',
                              arguments={'a0': bits(upper | byte)},
                              expectedRegisters={'v0': bits(reversed_byte ^ 255)}))
        saved = saved_values(seed)
        cases.append(dict(name=name, instructionLimit=100000, calls=calls,
                          initialRegisters={**{r: bits(v) for r, v in saved.items()}, 'sp': bits(stack)},
                          preservedRegisters=list(saved), helpers=[dict(from_='reverse_byte', until='reverse_byte_return')],
                          stackAlignment=16, callerStackBytes=32))
        cases[-1]['helpers'][0]['from'] = cases[-1]['helpers'][0].pop('from_')
    return dict(formatVersion=1, cases=cases)


def script():
    lines = [GENERATED, '// Implementation registers and frame offsets may differ. Each case owns a fresh simulator.\n\n']
    for name, upper, seed, stack in CONFIGURATIONS:
        saved = saved_values(seed)
        lines.append(f'case({kotlin_string(name)}, instructionLimit = 100000) {{\n')
        lines.append(f'    setRegister("sp", {bits(stack)}uL.toLong())\n')
        lines.append('    val top = readRegister("sp") and -16L\n    setRegister("sp", top)\n')
        lines.append('    val saved = mapOf(\n')
        lines.extend(f'        {kotlin_string(r)} to {bits(v)}uL.toLong(),\n' for r, v in saved.items())
        lines.append('    )\n    saved.forEach { (register, value) -> setRegister(register, value) }\n')
        lines.append('    val callerBytes = readBytes(top, 32)\n')
        lines.append('    val helper = traceExecution("reverse_byte", "reverse_byte_return")\n')
        lines.append('    for (input in 0..255) {\n')
        lines.append(f'        setRegister("a0", {bits(upper)}uL.toLong() or input.toLong())\n')
        lines.append('        val before = helper.snapshot().instructions\n')
        lines.append('        callFunction("reverse_and_invert", returnTo = if (input % 2 == 0) "after_invert" else "after_repeat")\n')
        lines.append('        expectCondition(helper.snapshot().instructions > before, "The provided helper was not executed")\n')
        lines.append('        expectRegister("v0", ((Integer.reverse(input) ushr 24) xor 255).toLong())\n')
        lines.append('        expectRegister("sp", top)\n')
        lines.append('        saved.forEach { (register, value) -> expectRegister(register, value) }\n')
        lines.append('        expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")\n')
        lines.append('        expectOutput("")\n    }\n}\n\n')
    return ''.join(lines).removesuffix('\n')


def build(directory, check=False):
    directory = Path(directory)
    files = {'reverse-and-invert.cases.json': json.dumps(profile(), indent=2) + '\n',
             'reverse-and-invert.kts': script()}
    for name, content in files.items():
        target = directory / name
        if check:
            if target.read_text(encoding='utf-8') != content:
                raise ValueError(f'Stale generated public function checks: {target}')
        else:
            directory.mkdir(parents=True, exist_ok=True)
            target.write_text(content, encoding='utf-8', newline='\n')
    return files


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-directory', type=Path, required=True)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    build(args.output_directory, args.check)
