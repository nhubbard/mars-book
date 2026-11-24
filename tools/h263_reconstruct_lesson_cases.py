"""Native contracts for all 138 original eight-by-eight reconstruction cases.

Prediction and signed inverse corrections remain separate inputs. This module
specifies exact final clipping and ownership, without an assembly solution.
"""

import argparse
import json
from pathlib import Path
from function_lesson_cases import bits

OBJECTS = dict(
    recon_prediction=28720,
    recon_inverse=28752,
    recon_output=28720,
    recon_settings=40,
    recon_status=8,
    recon_count=8,
)
STRIDES = [
    (8, 16, 8),
    (9, 18, 11),
    (31, 34, 17),
    (4096, 4096, 4096),
    (4093, 4094, 4091),
]
VALUES = [-256, -129, -128, -127, -1, 0, 1, 126, 127, 128, 254, 255]
INVALID_METADATA = [
    "settings-align",
    "settings-wrap",
    "inverse-align",
    "prediction-wrap",
    "inverse-wrap",
    "output-wrap",
]
SAMPLE_FAULTS = [
    "sample-low-first",
    "sample-high-first",
    "sample-low-last",
    "sample-high-last",
]


def address(label, offset=0):
    return dict(label=label, offset=offset)


def window(label, offset, length):
    return dict(at=address(label, offset), length=length)


def native(value, width, little):
    return (value & ((1 << (width * 8)) - 1)).to_bytes(
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


def rows_memory(label, offset, stride, rows):
    # Restore the entire original caller object with disjoint compact ranges.
    result = []
    cursor = 0
    for i, row in enumerate(rows):
        at = offset + i * stride
        if at > cursor:
            result.append(memory(label, bytes([165]) * (at - cursor), cursor))
        result.append(memory(label, row, at))
        cursor = at + len(row)
    if cursor < OBJECTS[label]:
        result.append(memory(label, bytes([165]) * (OBJECTS[label] - cursor), cursor))
    return result


def invocation(strides, little, pattern=0, alias=False, bad=""):
    ps, rs, os = strides
    metadata = (
        8 <= ps <= 4096
        and 16 <= rs <= 4096
        and rs % 2 == 0
        and 8 <= os <= 4096
        and bad not in INVALID_METADATA
    )
    samples = not bad.startswith("sample-")
    good = metadata and samples
    actual = [
        ps if 8 <= ps <= 4096 else 8,
        rs if 16 <= rs <= 4096 and rs % 2 == 0 else 16,
        os if 8 <= os <= 4096 else 8,
    ]
    po = 3 + pattern % 4
    ro = 2 + 2 * (pattern % 3)
    oo = 1 + pattern % 3
    prediction = [
        (
            128
            if pattern == 0
            else (
                0
                if pattern == 1
                else 255 if pattern == 2 else (j * 73 + pattern * 17) & 255
            )
        )
        for j in range(64)
    ]
    inverse = [
        (
            [-129, -128, -127, 0, 126, 127, 128, 255][j % 8]
            if pattern == 0
            else (
                -256
                if pattern == 1
                else 255 if pattern == 2 else VALUES[(j * 7 + pattern) % len(VALUES)]
            )
        )
        for j in range(64)
    ]
    if bad.startswith("sample-"):
        inverse[0 if bad.endswith("first") else 63] = -257 if "low" in bad else 256
    left_rows = [bytes(prediction[i : i + 8]) for i in range(0, 64, 8)]
    inverse_rows = [
        b"".join(native(v, 2, little) for v in inverse[i : i + 8])
        for i in range(0, 64, 8)
    ]
    setup = rows_memory("recon_prediction", po, actual[0], left_rows) + rows_memory(
        "recon_inverse", ro, actual[1], inverse_rows
    )
    setup.append(
        memory(
            "recon_settings",
            b"".join(native(v, 8, little) for v in strides) + bytes([165]) * 16,
        )
    )
    pointers = dict(
        a0=address("recon_prediction", po),
        a1=address("recon_inverse", ro + (bad == "inverse-align")),
        a2=address("recon_prediction", po) if alias else address("recon_output", oo),
        a3=address("recon_settings", 4 if bad == "settings-align" else 0),
    )
    arguments = {}
    for fault, key, value in [
        ("prediction-wrap", "a0", -4),
        ("inverse-wrap", "a1", -2),
        ("output-wrap", "a2", -4),
        ("settings-wrap", "a3", -16),
    ]:
        if bad == fault:
            arguments[key] = bits(value)
            pointers.pop(key)
    reads = (
        [window("recon_settings", 0, 24)]
        if bad not in ["settings-align", "settings-wrap"]
        else []
    )
    if metadata:
        reads += [window("recon_prediction", po + i * ps, 8) for i in range(8)]
        reads += [window("recon_inverse", ro + i * rs, 16) for i in range(8)]
    sums = [a + b for a, b in zip(prediction, inverse)]
    clipped = sum(v < 0 or v > 255 for v in sums)
    label = "recon_prediction" if alias else "recon_output"
    start = po if alias else oo
    results = (
        [
            memory(
                label,
                bytes(max(0, min(255, v)) for v in sums[i * 8 : i * 8 + 8]),
                start + i * actual[2],
            )
            for i in range(8)
        ]
        if good
        else []
    )
    return dict(
        entry="h263_reconstruct8",
        returnTo="recon_function_return",
        arguments=arguments,
        argumentAddresses=pointers,
        expectedRegisters=dict(
            v0=bits(0 if good else 2 if metadata else 1),
            v1=bits(clipped if good else 0),
        ),
        setupMemory=setup,
        memory=results,
        callerAccess=dict(
            objects=[window(k, 0, n) for k, n in OBJECTS.items()],
            readable=reads,
            writable=(
                [window(label, start + i * os, 8) for i in range(8)] if good else []
            ),
            maxAccesses=10000,
        ),
    )


def case(name, calls, little):
    names = [f"s{i}" for i in range(8)] + ["fp", "gp"]
    saved = {n: bits(0x3141592600000000 + 977 * i) for i, n in enumerate(names)}
    return dict(
        name=name,
        calls=calls,
        instructionLimit=100000,
        byteOrder="little" if little else "big",
        initialRegisters=saved,
        preservedRegisters=names,
        initialRegisterAddresses=dict(sp=address("recon_stack", 8192)),
        stackAlignment=16,
        callerStackBytes=0,
    )


def stride_text(values):
    return "[" + ", ".join(str(v) for v in values) + "]"


def cases():
    out = []
    for little in [False, True]:
        suffix = str(little).lower()
        for strides in STRIDES:
            for pattern in range(6):
                out.append(
                    case(
                        f"reconstruct strides={stride_text(strides)} pattern={pattern} little={suffix}",
                        [invocation(strides, little, pattern)],
                        little,
                    )
                )
    for little in [False, True]:
        for stride in [8, 9, 4096]:
            for pattern in range(4):
                out.append(
                    case(
                        f"reconstruct exact prediction alias stride={stride} pattern={pattern} little={str(little).lower()}",
                        [invocation((stride, 18, stride), little, pattern, alias=True)],
                        little,
                    )
                )
    invalid = [
        (0, 7),
        (0, 4097),
        (0, 0x100000008),
        (0, -1),
        (1, 15),
        (1, 17),
        (1, 4098),
        (1, 0x100000010),
        (2, 7),
        (2, 4097),
        (2, 0x100000008),
        (2, -1),
    ]
    for little in [False, True]:
        for field, value in invalid:
            strides = [8, 16, 8]
            strides[field] = value
            out.append(
                case(
                    f"reconstruct invalid stride {field} {value} little={str(little).lower()}",
                    [invocation(strides, little, 3)],
                    little,
                )
            )
    for little in [False, True]:
        for bad in INVALID_METADATA + SAMPLE_FAULTS:
            out.append(
                case(
                    f"reconstruct invalid {bad} little={str(little).lower()}",
                    [invocation((9, 18, 11), little, 3, bad=bad)],
                    little,
                )
            )
    for little in [False, True]:
        out.append(
            case(
                f"reconstruct repeated results little={str(little).lower()}",
                [invocation((9, 18, 11), little, p) for p in [2, 1, 0, 5, 3]],
                little,
            )
        )
    for little in [False, True]:
        for bad in SAMPLE_FAULTS:
            out.append(
                case(
                    f"invalid reconstruction retains aliased prediction {bad} little={str(little).lower()}",
                    [invocation((9, 18, 9), little, 3, alias=True, bad=bad)],
                    little,
                )
            )
    return out


def profiles():
    groups = dict(samples=[], aliases=[], metadata=[], rejection=[], reuse=[])
    for row in cases():
        name = row["name"]
        key = (
            "samples"
            if name.startswith("reconstruct strides=")
            else (
                "aliases"
                if name.startswith("reconstruct exact ")
                else (
                    "reuse"
                    if "repeated results" in name
                    else (
                        "rejection"
                        if "sample-" in name
                        or name.startswith("invalid reconstruction")
                        else "metadata"
                    )
                )
            )
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
                raise ValueError(f"Stale reconstruction profile: {path}")
        else:
            path.write_text(text)
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    r = build(args.directory, args.check)
    print(len(r), "profiles;", sum(len(v["cases"]) for v in r.values()), "cases")
