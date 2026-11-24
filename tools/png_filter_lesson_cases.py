"""Exact public PNG RGB8 forward-filter cases for native function assessments.

The complete decoder remains a supplied worked example. These profiles retain
both native settings byte orders and all 266 original independent filter cases.
"""

import argparse
import json
from pathlib import Path
from function_lesson_cases import bits

OBJECTS = dict(
    filter_settings=32,
    current_rgb=1552,
    previous_rgb=1552,
    filtered_row=1556,
    filter_status=4,
    filter_count=8,
)
WIDTHS = [1, 2, 5, 15, 16, 17, 31, 32, 33, 511, 512]
KINDS = ["none", "sub", "up", "average", "paeth"]
TIES = [(255, 0, 85), (0, 255, 170), (0, 3, 1), (3, 0, 2)]
MASK = (1 << 64) - 1


def address(label, offset=0):
    return dict(label=label, offset=offset)


def window(label, offset, length):
    return dict(at=address(label, offset), length=length)


def memory(label, raw, offset=0):
    return dict(label=label, bytes=bytes(raw).hex(), offset=offset)


def predictor(kind, a, b, c):
    if kind == 0:
        return 0
    if kind == 1:
        return a
    if kind == 2:
        return b
    if kind == 3:
        return (a + b) // 2
    p = a + b - c
    distances = [abs(p - a), abs(p - b), abs(p - c)]
    return [a, b, c][distances.index(min(distances))]


def inverse(filtered, previous):
    kind = filtered[0]
    raw = []
    for j, value in enumerate(filtered[1:]):
        a = raw[j - 3] if j >= 3 else 0
        b = previous[j] if previous is not None else 0
        c = previous[j - 3] if previous is not None and j >= 3 else 0
        raw.append((value + predictor(kind, a, b, c)) & 255)
    return bytes(raw)


def invocation(
    width, kind, little, top=False, seed=0, capacity=None, tie=None, same=False
):
    n = 3 * width if 1 <= width <= 512 else 0
    if capacity is None:
        capacity = n + 1 if n else 0
    valid = 1 <= width <= 512 and 0 <= kind <= 4 and (capacity & MASK) >= n + 1
    current = [(j * 71 + j * j * 13 + seed * 17) & 255 for j in range(n)]
    previous = [(j * 43 + 255 - seed * 29) & 255 for j in range(n)]
    if tie is not None:
        assert n == 6
        for channel in range(3):
            current[channel] = tie[0]
            previous[channel] = tie[2]
            previous[3 + channel] = tie[1]
            current[3 + channel] = 201
    start = 1 + seed % 7
    prior = 7 - seed % 7
    output = 1 + seed % 5
    input_bytes = bytearray([165]) * 1552
    input_bytes[start : start + n] = bytes(current)
    prior_bytes = bytearray([165]) * 1552
    prior_bytes[prior : prior + n] = bytes(previous)
    settings = (
        b"".join(
            (v & MASK).to_bytes(8, "little" if little else "big")
            for v in [width, kind, capacity]
        )
        + bytes([165]) * 8
    )
    used = valid and not top and kind >= 2
    arguments = {}
    pointers = dict(a3=address("filter_settings"))
    if valid:
        pointers.update(
            a0=address("current_rgb", start), a2=address("filtered_row", output)
        )
    else:
        arguments.update(a0=bits(1), a2=bits(1))
    if top:
        arguments["a1"] = bits(0)
    elif used:
        pointers["a1"] = (
            address("current_rgb", start) if same else address("previous_rgb", prior)
        )
    else:
        arguments["a1"] = bits(1)
    reads = [window("filter_settings", 0, 24)]
    if valid:
        reads.append(window("current_rgb", start, n))
    if used:
        reads.append(
            window("current_rgb", start, n)
            if same
            else window("previous_rgb", prior, n)
        )
    result = bytearray()
    if valid:
        result.append(kind)
        for j, value in enumerate(current):
            a = current[j - 3] if j >= 3 else 0
            b = (current[j] if same else previous[j]) if used else 0
            c = (current[j - 3] if same else previous[j - 3]) if used and j >= 3 else 0
            result.append((value - predictor(kind, a, b, c)) & 255)
        # Exact equality with this result implies the original inverse assertion.
        prior_row = bytes(current if same else previous) if used else None
        assert inverse(result, prior_row) == bytes(current)
    return dict(
        entry="png_filter_scanline",
        returnTo="filter_function_return",
        arguments=arguments,
        argumentAddresses=pointers,
        expectedRegisters=dict(
            v0=bits(0 if valid else 1), v1=bits(n + 1 if valid else 0)
        ),
        setupMemory=[
            memory("current_rgb", input_bytes),
            memory("previous_rgb", prior_bytes),
            memory("filter_settings", settings),
        ],
        memory=[memory("filtered_row", result, output)] if valid else [],
        callerAccess=dict(
            objects=[window(k, 0, size) for k, size in OBJECTS.items()],
            readable=reads,
            writable=[window("filtered_row", output, n + 1)] if valid else [],
            maxAccesses=100000,
        ),
    )


def case(name, calls, little):
    saved = {f"s{i}": bits(0x3141592600000000 + 991 * i) for i in range(8)} | dict(
        fp=bits(0x3141592600000000 + 991 * 8)
    )
    return dict(
        name=name,
        calls=calls,
        instructionLimit=100000,
        byteOrder="little" if little else "big",
        initialRegisters=saved,
        preservedRegisters=list(saved),
        initialRegisterAddresses=dict(sp=address("filter_workspace", 4096)),
        stackAlignment=8,
        callerStackBytes=0,
    )


def cases():
    result = []
    for little in [False, True]:
        for width in WIDTHS:
            for kind in range(5):
                for top in [False, True]:
                    name = f"forward {width} filter {kind} top {str(top).lower()} little {str(little).lower()}"
                    result.append(
                        case(
                            name,
                            [invocation(width, kind, little, top, (width + kind) % 23)],
                            little,
                        )
                    )
    invalid = [
        (0, 0, 4),
        (513, 0, 1540),
        (0x100000001, 0, 7),
        (-1, 0, -1),
        (2, 5, 7),
        (2, 0x100000000, 7),
        (2, -1, 7),
        (2, 3, 6),
    ]
    for little in [False, True]:
        for width, kind, capacity in invalid:
            result.append(
                case(
                    f"invalid {width} {kind} {capacity} little {str(little).lower()}",
                    [invocation(width, kind, little, capacity=capacity)],
                    little,
                )
            )
    for little in [False, True]:
        for kind in range(5):
            result.append(
                case(
                    f"unsigned capacity filter {kind} little {str(little).lower()}",
                    [invocation(2, kind, little, capacity=-(1 << 63))],
                    little,
                )
            )
            result.append(
                case(
                    f"coincident read-only inputs filter {kind} little {str(little).lower()}",
                    [invocation(17, kind, little, same=True)],
                    little,
                )
            )
    for little in [False, True]:
        result.append(
            case(
                f"repeated filters little {str(little).lower()}",
                [
                    invocation(17, kind, little, kind % 2 == 0, kind)
                    for kind in [4, 0, 3, 1, 2, 4]
                ],
                little,
            )
        )
    for little in [False, True]:
        for tie in TIES:
            result.append(
                case(
                    f"Paeth tie {tie} little {str(little).lower()}",
                    [invocation(2, 4, little, tie=tie)],
                    little,
                )
            )
    return result


def profiles():
    groups = {kind: [] for kind in KINDS} | dict(boundaries=[])
    for row in cases():
        group = (
            KINDS[int(row["name"].split()[3])]
            if row["name"].startswith("forward ")
            else "boundaries"
        )
        groups[group].append(row)
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
                raise ValueError(f"Stale PNG filter profile: {path}")
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
