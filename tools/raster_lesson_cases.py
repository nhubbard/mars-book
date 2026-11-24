"""Author public dashed-raster function cases using a closed-form cell oracle."""

from kotlin_format import formatted_kotlin
import argparse
import json
from pathlib import Path

from function_lesson_cases import bits, saved_values
from console_cases import kotlin_string
from sprite_lesson_cases import word_string

ENDPOINTS = (
    (1, 1, 6, 4),
    (6, 4, 1, 1),
    (0, 0, 2, 1),
    (2, 1, 0, 0),
    (3, 7, 3, 0),
    (4, 4, 4, 4),
    (1, 6, 3, 0),
    (0, 7, 7, 7),
    (0, 0, 7, 7),
    (7, 0, 0, 7),
    (0, 0, 1, 2),
    (7, 7, 7, 7),
    (0, 0, 4, 1),
    (4, 1, 0, 0),
    (0, 0, 1, 4),
    (1, 4, 0, 0),
)
GUARDS = ("frame_guard", "endpoints_guard", "color_guard", "count_guard")


def line_cells(ax, ay, bx, by):
    x0, y0, x1, y1 = (ax, ay, bx, by) if (ax, ay) <= (bx, by) else (bx, by, ax, ay)
    dx, dy = x1 - x0, abs(y1 - y0)
    sy = 1 if y1 >= y0 else -1
    if dx == 0 and dy == 0:
        return [(x0, y0)]
    if dx >= dy:
        return [
            (x0 + k, y0 + sy * ((2 * k * dy + dx) // (2 * dx))) for k in range(dx + 1)
        ]
    return [(x0 + (2 * k * dx + dy) // (2 * dy), y0 + sy * k) for k in range(dy + 1)]


def profile():
    cases = []
    for number, endpoints in enumerate(ENDPOINTS):
        rgb = 0 if number == 0 else ((number + 1) * 0x010307) & 0xFFFFFF
        old = [0x5A000000 | (cell * 0x010103) for cell in range(64)]
        selected = set(line_cells(*endpoints)[::2])
        expected = [
            rgb if (x, y) in selected else old[8 * y + x]
            for y in range(8)
            for x in range(8)
        ]
        setup = [
            dict(label="frame", bytes=word_string(old)),
            dict(label="endpoints", bytes=word_string(endpoints)),
            dict(label="color", bytes=word_string([rgb ^ 0xFFFFFF])),
            dict(label="line_count", bytes=word_string([0x6B000000 | number])),
        ]
        setup += [
            dict(label=label, bytes=word_string([0x12345678 ^ (number * 31 + index)]))
            for index, label in enumerate(GUARDS)
        ]
        memory = [dict(label="frame", bytes=word_string(expected)), *setup[1:]]
        saved = saved_values(0x1122334400000000 + number * 0x01010101)
        calls = [
            dict(
                entry="draw_dashed",
                returnTo=label,
                arguments={"a2": bits(rgb)},
                argumentAddresses={
                    "a0": dict(label="endpoints"),
                    "a1": dict(label="frame"),
                },
                expectedRegisters={"v0": bits(len(selected))},
                memory=memory,
            )
            for label in ("line_complete", "line_repeat")
        ]
        protected = [dict(label="endpoints", length=16), dict(label="color", length=4)]
        protected += [dict(label=label, length=4) for label in GUARDS]
        cases.append(
            dict(
                name=f"dashed {endpoints[0]},{endpoints[1]} to {endpoints[2]},{endpoints[3]}",
                instructionLimit=10000,
                calls=calls,
                initialMemory=setup,
                noStores=protected,
                initialRegisters={
                    **{reg: bits(value) for reg, value in saved.items()},
                    "sp": bits(0x7FFFEFFC - number * 4096),
                },
                preservedRegisters=list(saved),
                stackAlignment=16,
                callerStackBytes=32,
            )
        )
    return dict(formatVersion=3, cases=cases)


@formatted_kotlin
def script():
    lines = [
        "// Generated public raster checks; regenerate with tools/raster_lesson_cases.py.\n",
        "// Byte strings encode the book environment’s little-endian words.\n\n",
    ]
    for case in profile()["cases"]:
        lines.append(
            f'case({kotlin_string(case["name"])}, instructionLimit = 10000) {{\n'
        )
        for item in case["initialMemory"]:
            values = ", ".join(str(b) for b in bytes.fromhex(item["bytes"]))
            lines.append(f'    setBytes({kotlin_string(item["label"])}, {values})\n')
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
        lines.append(
            "    val callerBytes = readBytes(top, 32)\n    val protected = listOf(\n"
        )
        for item in case["noStores"]:
            lines.append(
                f'        traceMemoryStores({kotlin_string(item["label"])}, {item["length"]}),\n'
            )
        lines.append("    )\n")
        for call in case["calls"]:
            lines.append(
                '    setRegisterToDataAddress("a0", "endpoints")\n    setRegisterToDataAddress("a1", "frame")\n'
            )
            lines.append(
                f'    setRegister("a2", {call["arguments"]["a2"]}uL.toLong())\n'
            )
            lines.append(
                f'    callFunction("draw_dashed", returnTo = {kotlin_string(call["returnTo"])})\n'
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
                "    saved.forEach { (register, value) -> expectRegister(register, value) }\n"
            )
            lines.append(
                '    expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")\n    expectOutput("")\n'
            )
        lines.append("}\n\n")
    return "".join(lines).removesuffix("\n")


def build(directory, check=False):
    directory = Path(directory)
    files = {
        "dashed-line.cases.json": json.dumps(profile(), indent=2) + "\n",
        "dashed-line.kts": script(),
    }
    for name, content in files.items():
        path = directory / name
        if check:
            if path.read_text(encoding="utf-8") != content:
                raise ValueError(f"Stale generated raster checks: {path}")
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
