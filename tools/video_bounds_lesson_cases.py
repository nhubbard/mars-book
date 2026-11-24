"""Exact native profiles for the existing 182 changed-pixel bounds cases.

This specifies inputs, exclusive bounds and ownership; it provides no assembly
implementation. The complete MLV1 codec remains a separate worked example.
"""

import argparse
import json
from pathlib import Path
from function_lesson_cases import bits

OBJECTS = dict(
    bounds_previous=16416,
    bounds_current=16416,
    bounds_settings=32,
    bounds_result=64,
    bounds_status=4,
    bounds_count=8,
)
DIMENSIONS = [
    (1, 1),
    (1, 64),
    (64, 1),
    (2, 2),
    (3, 5),
    (15, 17),
    (16, 16),
    (17, 15),
    (31, 33),
    (63, 64),
    (64, 64),
]
MASK = (1 << 64) - 1


def address(label, offset=0):
    return dict(label=label, offset=offset)


def window(label, offset, length):
    return dict(at=address(label, offset), length=length)


def native(value, width, little):
    return (value & ((1 << (8 * width)) - 1)).to_bytes(
        width, "little" if little else "big"
    )


def expanded(row):
    return bytes.fromhex(row["bytes"]) * row.get("repeat", 1)


def memory(label, raw, offset=0):
    raw = bytes(raw)
    result = dict(label=label, offset=offset, bytes=raw.hex())
    if len(raw) > 1 and raw == raw[:1] * len(raw):
        result.update(bytes=raw[:1].hex(), repeat=len(raw))
    return result


def padded(label, offset, raw):
    # Disjoint ranges retain every original guard without repeating long A5 tails.
    result = []
    if offset:
        result.append(memory(label, bytes([165]) * offset))
    if raw:
        result.append(memory(label, raw, offset))
    end = offset + len(raw)
    if end < OBJECTS[label]:
        result.append(memory(label, bytes([165]) * (OBJECTS[label] - end), end))
    return result


def changed_indices(width, height, pattern, alias=False):
    n = width * height if 1 <= width <= 64 and 1 <= height <= 64 else 0
    if alias or pattern == 0:
        return set()
    if pattern == 1:
        return {0}
    if pattern == 2:
        return {n - 1}
    if pattern == 3:
        return {i for i in range(n) if i % 3 == 1 or i == n - 1}
    if pattern == 4:
        return set(range(n))
    if pattern == 5:
        return {i for i in [max(width - 1, 0), width] if 0 <= i < n}
    if pattern == 6:
        return {i for i in [325, 326, 389, 390] if 0 <= i < n}
    return {i for i in range(n) if (i * 17 + pattern) % 23 == 0}


def rectangle(indices, width):
    if not indices:
        return [0, 0, 0, 0]
    xs = [i % width for i in indices]
    ys = [i // width for i in indices]
    return [min(xs), min(ys), 1 + max(xs), 1 + max(ys)]


def invocation(width, height, little, pattern=0, alias=False, bad=""):
    valid = 1 <= width <= 64 and 1 <= height <= 64 and not bad
    n = width * height if 1 <= width <= 64 and 1 <= height <= 64 else 0
    changed = changed_indices(width, height, pattern, alias)
    prior = [0x89ABCDEF ^ (i * 977 + pattern * 73) for i in range(n)]
    current = [
        v ^ (0x80000000 if i % 2 == 0 else 0x00102040) if i in changed else v
        for i, v in enumerate(prior)
    ]
    left = 4 * (1 + pattern % 3)
    right = 4 * (3 - pattern % 3)
    output = 8 * (pattern % 3)
    setup = padded(
        "bounds_previous", left, b"".join(native(v, 4, little) for v in prior)
    )
    setup += padded(
        "bounds_current", right, b"".join(native(v, 4, little) for v in current)
    )
    setup.append(
        memory(
            "bounds_settings",
            native(width, 8, little) + native(height, 8, little) + bytes([165]) * 16,
        )
    )
    pointers = dict(
        a0=address("bounds_previous", left + (bad == "left")),
        a1=(
            address("bounds_previous", left + (bad == "right"))
            if alias
            else address("bounds_current", right + (bad == "right"))
        ),
        a2=address("bounds_result", output + (4 if bad == "output" else 0)),
        a3=address("bounds_settings", 4 if bad == "settings" else 0),
    )
    reads = [window("bounds_settings", 0, 16)] if bad != "settings" else []
    if valid:
        reads += [
            window("bounds_previous", left, 4 * n),
            window(
                "bounds_previous" if alias else "bounds_current",
                left if alias else right,
                4 * n,
            ),
        ]
    result = (
        b"".join(native(v, 8, little) for v in rectangle(changed, width))
        if valid
        else b""
    )
    return dict(
        entry="video_change_bounds",
        returnTo="bounds_function_return",
        arguments={},
        argumentAddresses=pointers,
        expectedRegisters=dict(
            v0=bits(0 if valid else 1), v1=bits(len(changed) if valid else 0)
        ),
        setupMemory=setup,
        memory=[memory("bounds_result", result, output)] if valid else [],
        callerAccess=dict(
            objects=[window(k, 0, size) for k, size in OBJECTS.items()],
            readable=reads,
            writable=[window("bounds_result", output, 32)] if valid else [],
            maxAccesses=100000,
        ),
    )


def case(name, calls, little):
    saved = {f"s{i}": bits(0x3141592600000000 + 977 * i) for i in range(8)} | dict(
        fp=bits(0x3141592600000000 + 977 * 8)
    )
    return dict(
        name=name,
        calls=calls,
        instructionLimit=1000000,
        byteOrder="little" if little else "big",
        initialRegisters=saved,
        preservedRegisters=list(saved),
        initialRegisterAddresses=dict(sp=address("bounds_workspace", 8192)),
        stackAlignment=16,
        callerStackBytes=0,
    )


def cases():
    result = []
    for little in [False, True]:
        for width, height in DIMENSIONS:
            for pattern in range(6):
                result.append(
                    case(
                        f"bounds {width} x {height} pattern {pattern} little {str(little).lower()}",
                        [invocation(width, height, little, pattern)],
                        little,
                    )
                )
    for little in [False, True]:
        for width, height in DIMENSIONS:
            result.append(
                case(
                    f"same inputs {width} x {height} little {str(little).lower()}",
                    [invocation(width, height, little, 4, alias=True)],
                    little,
                )
            )
    for little in [False, True]:
        for width, height in [
            (0, 1),
            (1, 0),
            (65, 1),
            (1, 65),
            (0x100000001, 1),
            (1, 0x100000001),
            (-1, 1),
            (1, -1),
        ]:
            result.append(
                case(
                    f"invalid dimensions {width} {height} little {str(little).lower()}",
                    [invocation(width, height, little)],
                    little,
                )
            )
    for little in [False, True]:
        for bad in ["left", "right", "output", "settings"]:
            result.append(
                case(
                    f"misaligned {bad} little {str(little).lower()}",
                    [invocation(3, 5, little, 4, bad=bad)],
                    little,
                )
            )
    for little in [False, True]:
        result.append(
            case(
                f"moving patch little {str(little).lower()}",
                [invocation(64, 64, little, 6)],
                little,
            )
        )
        result.append(
            case(
                f"repeated calls little {str(little).lower()}",
                [invocation(17, 15, little, p) for p in [4, 0, 2, 1, 5, 3]],
                little,
            )
        )
    return result


def profiles():
    groups = {f"pattern-{p}": [] for p in range(6)} | dict(aliases=[], boundaries=[])
    for row in cases():
        name = row["name"]
        key = (
            "pattern-" + name.split()[5]
            if name.startswith("bounds ")
            else "aliases" if name.startswith("same inputs ") else "boundaries"
        )
        groups[key].append(row)
    return {
        name + ".json": dict(formatVersion=21, cases=rows)
        for name, rows in groups.items()
    }


def build(directory, check=False):
    root = Path(directory)
    if not check:
        root.mkdir(parents=True, exist_ok=True)
    result = profiles()
    for name, data in result.items():
        text = json.dumps(data, separators=(",", ":")) + "\n"
        path = root / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale video bounds profile: {path}")
        else:
            path.write_text(text)
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    result = build(args.directory, args.check)
    print(
        len(result), "profiles;", sum(len(p["cases"]) for p in result.values()), "cases"
    )
