"""Generate public Life function cases from a scatter-based neighbor oracle."""

from kotlin_format import formatted_kotlin
import argparse
import json
from pathlib import Path

from console_cases import kotlin_string
from function_lesson_cases import bits, saved_values
from sprite_lesson_cases import word_string


def pattern(*live):
    return [int((cell % 8, cell // 8) in live) for cell in range(64)]


PATTERNS = (
    ("empty", [0] * 64),
    ("full", [1] * 64),
    ("oscillator", pattern((2, 3), (3, 3), (4, 3))),
    ("stable block", pattern((3, 3), (4, 3), (3, 4), (4, 4))),
    ("upper-left edge", pattern((0, 0), (1, 0), (0, 1))),
    ("separate corners", pattern((7, 7), (6, 7), (7, 6), (0, 0))),
    ("asymmetric", [int((cell * 37 + 13) % 17 < 8) for cell in range(64)]),
)
GUARDS = (
    "frame_guard",
    "seed_guard",
    "board_a_guard",
    "board_b_guard",
    "generations_guard",
    "generation_guard",
)


def evolve(old):
    counts = [0] * 64
    for cell, alive in enumerate(old):
        if not alive:
            continue
        x, y = cell % 8, cell // 8
        for dy in (-1, 0, 1):
            for dx in (-1, 0, 1):
                nx, ny = x + dx, y + dy
                if (dx or dy) and 0 <= nx < 8 and 0 <= ny < 8:
                    counts[8 * ny + nx] += 1
    return [int(n == 3 or n == 2 and alive == 1) for alive, n in zip(old, counts)]


def padded(cells):
    board = [0] * 100
    for cell, value in enumerate(cells):
        board[11 + 10 * (cell // 8) + cell % 8] = value
    return bytes(board)


def protected_regions(source, destination):
    regions = [dict(label=source, length=100)]
    regions += [dict(label=label, length=4) for label in GUARDS]
    regions += [dict(label=destination, length=10, offset=offset) for offset in (0, 90)]
    regions += [
        dict(label=destination, length=1, offset=10 * row + column)
        for row in range(1, 9)
        for column in (0, 9)
    ]
    return regions


def profile():
    cases = []
    for number, (name, old) in enumerate(PATTERNS):
        next_board = evolve(old)
        changes = sum(a != b for a, b in zip(old, next_board))
        for reverse in (False, True):
            source, destination = (
                ("board_b", "board_a") if reverse else ("board_a", "board_b")
            )
            initial = [
                dict(label=source, bytes=padded(old).hex()),
                dict(
                    label=destination,
                    bytes=padded(
                        [1 + (cell * 31 + number * 17) % 255 for cell in range(64)]
                    ).hex(),
                ),
            ]
            initial += [
                dict(
                    label=label,
                    bytes=word_string(
                        [0x12345678 ^ (31 * number + index + int(reverse) * 4096)]
                    ),
                )
                for index, label in enumerate(GUARDS)
            ]
            memory = [
                dict(label=source, bytes=padded(old).hex()),
                dict(label=destination, bytes=padded(next_board).hex()),
                *initial[2:],
            ]
            calls = [
                dict(
                    entry="life_changes",
                    returnTo=label,
                    arguments={},
                    argumentAddresses={
                        "a0": dict(label=source),
                        "a1": dict(label=destination),
                    },
                    expectedRegisters={"v0": bits(changes)},
                    memory=memory,
                )
                for label in ("generation_built", "life_done")
            ]
            kept = saved_values(
                0x1122334400000000 + number * 0x01010101 + int(reverse) * 0x100000001
            )
            cases.append(
                dict(
                    name=name + (" B to A" if reverse else " A to B"),
                    instructionLimit=50000,
                    calls=calls,
                    initialMemory=initial,
                    noStores=protected_regions(source, destination),
                    initialRegisters={
                        **{reg: bits(value) for reg, value in kept.items()},
                        "sp": bits(0x7FFFEFFC - (2 * number + int(reverse)) * 4096),
                    },
                    preservedRegisters=list(kept),
                    stackAlignment=16,
                    callerStackBytes=32,
                )
            )
    return dict(formatVersion=3, cases=cases)


@formatted_kotlin
def script():
    lines = [
        "// Generated public Life checks; regenerate with tools/life_lesson_cases.py.\n",
        "// Setup word byte strings target the book little-endian environment.\n\n",
    ]
    for case in profile()["cases"]:
        lines.append(
            f'case({kotlin_string(case["name"])}, instructionLimit = 50000) {{\n'
        )
        for item in case["initialMemory"]:
            values = ", ".join(str(b) for b in bytes.fromhex(item["bytes"]))
            lines.append(f'    setBytes({kotlin_string(item["label"])}, {values})\n')
        for reg, value in case["initialRegisters"].items():
            lines.append(f"    setRegister({kotlin_string(reg)}, {value}uL.toLong())\n")
        lines.append(
            '    val top = readRegister("sp") and -16L\n    setRegister("sp", top)\n'
        )
        lines.append(
            '    val saved = listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp").associateWith(::readRegister)\n'
        )
        lines.append(
            "    val callerBytes = readBytes(top, 32)\n    val protected = listOf(\n"
        )
        for item in case["noStores"]:
            lines.append(
                f'        traceMemoryStores({kotlin_string(item["label"])}, {item["length"]}, offset = {item.get("offset",0)}),\n'
            )
        lines.append("    )\n")
        for call in case["calls"]:
            for reg, address in call["argumentAddresses"].items():
                lines.append(
                    f'    setRegisterToDataAddress({kotlin_string(reg)}, {kotlin_string(address["label"])})\n'
                )
            lines.append(
                f'    callFunction("life_changes", returnTo = {kotlin_string(call["returnTo"])})\n'
            )
            lines.append(
                f'    expectRegister("v0", {int(call["expectedRegisters"]["v0"],16)})\n'
            )
            for item in call["memory"]:
                values = ", ".join(str(b) for b in bytes.fromhex(item["bytes"]))
                lines.append(
                    f'    expectBytes({kotlin_string(item["label"])}, {values})\n'
                )
            lines.append(
                '    protected.forEach { it.expectNone() }\n    expectRegister("sp", top)\n'
            )
            lines.append(
                "    saved.forEach { (reg, value) -> expectRegister(reg, value) }\n"
            )
            lines.append(
                '    expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")\n    expectOutput("")\n'
            )
        lines.append("}\n\n")
    return "".join(lines).removesuffix("\n")


def build(directory, check=False):
    directory = Path(directory)
    files = {
        "life-changes.cases.json": json.dumps(profile(), indent=2) + "\n",
        "life-changes.kts": script(),
    }
    for name, text in files.items():
        path = directory / name
        if check:
            if path.read_text(encoding="utf-8") != text:
                raise ValueError(f"Stale generated Life checks: {path}")
        else:
            directory.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8", newline="\n")
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-directory", type=Path, required=True)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.output_directory, args.check)
