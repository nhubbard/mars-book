"""Author the sprite unit's finite public function-memory cases and matching CLI checks."""

from kotlin_format import formatted_kotlin
import argparse
import json
from pathlib import Path

from function_lesson_cases import bits, saved_values
from console_cases import kotlin_string

PATTERNS = (
    ("asymmetric sprite", (0x16, 0x3C, 0x7E, 0xDB, 0xFF, 0x24, 0x42, 0x81)),
    ("single-bit diagonal", tuple(128 >> row for row in range(8))),
    ("mixed edge bits", (0, 255, 0, 255, 1, 128, 127, 254)),
    ("all black", (0,) * 8),
    ("all white", (255,) * 8),
)


def byte_string(values):
    return bytes(values).hex()


def word_string(values):
    return b"".join(value.to_bytes(4, "little") for value in values).hex()


def expected_frame(rows, initial):
    return [
        (
            initial[y * 16 + x]
            if x < 8
            else (0xFFFFFF if rows[7 - y] & (128 >> (x - 8)) else 0)
        )
        for y in range(8)
        for x in range(16)
    ]


def profile():
    cases = []
    for index, (name, rows) in enumerate(PATTERNS):
        initial = [0x5A000000 + cell + index * 256 for cell in range(128)]
        expected = expected_frame(rows, initial)
        guards = [
            ("frame_guard", word_string([0x13579BDF ^ index])),
            ("rows_guard", word_string([0x2468ACE0 ^ index])),
        ]
        setup = [
            dict(label="frame", bytes=word_string(initial)),
            dict(label="sprite", bytes=byte_string(rows)),
        ] + [dict(label=n, bytes=b) for n, b in guards]
        memory = [
            dict(label="frame", bytes=word_string(expected)),
            dict(label="sprite", bytes=byte_string(rows)),
        ] + [dict(label=n, bytes=b) for n, b in guards]
        saved = saved_values(0x1122334400000000 + index * 0x01010101)
        calls = [
            dict(
                entry="draw_vertical",
                returnTo=label,
                arguments={},
                expectedRegisters={},
                argumentAddresses={
                    "a0": dict(label="sprite"),
                    "a1": dict(label="frame"),
                },
                memory=memory,
            )
            for label in ("after_vertical", "after_repeat")
        ]
        cases.append(
            dict(
                name=name,
                instructionLimit=20000,
                calls=calls,
                initialMemory=setup,
                initialRegisters={
                    **{r: bits(v) for r, v in saved.items()},
                    "sp": bits(0x7FFFEFFC - index * 512),
                },
                preservedRegisters=list(saved),
                stackAlignment=16,
                callerStackBytes=32,
            )
        )
    return dict(formatVersion=2, cases=cases)


@formatted_kotlin
def script():
    lines = [
        "// Generated public checks; regenerate with tools/sprite_lesson_cases.py.\n",
        "// Byte strings use the book environment’s little-endian word layout.\n\n",
    ]
    for case in profile()["cases"]:
        lines.append(
            f'case({kotlin_string(case["name"])}, instructionLimit = 20000) {{\n'
        )
        for memory in case["initialMemory"]:
            values = ", ".join(str(v) for v in bytes.fromhex(memory["bytes"]))
            lines.append(f'    setBytes({kotlin_string(memory["label"])}, {values})\n')
        for register, value in case["initialRegisters"].items():
            lines.append(
                f"    setRegister({kotlin_string(register)}, {value}uL.toLong())\n"
            )
        lines.append(
            '    val top = readRegister("sp") and -16L\n    setRegister("sp", top)\n'
        )
        lines.append(
            '    val saved = listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp").associateWith(::readRegister)\n'
        )
        lines.append("    val callerBytes = readBytes(top, 32)\n")
        for call in case["calls"]:
            lines.append(
                '    setRegisterToDataAddress("a0", "sprite")\n    setRegisterToDataAddress("a1", "frame")\n'
            )
            lines.append(
                f'    callFunction("draw_vertical", returnTo = {kotlin_string(call["returnTo"])})\n'
            )
            for memory in call["memory"]:
                values = ", ".join(str(v) for v in bytes.fromhex(memory["bytes"]))
                lines.append(
                    f'    expectBytes({kotlin_string(memory["label"])}, {values})\n'
                )
            lines.append(
                '    expectRegister("sp", top)\n    saved.forEach { (register, value) -> expectRegister(register, value) }\n'
            )
            lines.append(
                '    expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")\n    expectOutput("")\n'
            )
        lines.append("}\n\n")
    return "".join(lines).removesuffix("\n")


def build(directory, check=False):
    directory = Path(directory)
    files = {
        "vertical-sprite.cases.json": json.dumps(profile(), indent=2) + "\n",
        "vertical-sprite.kts": script(),
    }
    for name, content in files.items():
        path = directory / name
        if check:
            if path.read_text(encoding="utf-8") != content:
                raise ValueError(f"Stale generated sprite checks: {path}")
        else:
            directory.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8", newline="\n")
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-directory", type=Path, required=True)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.output_directory, args.check)
