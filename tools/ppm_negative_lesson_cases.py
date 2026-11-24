"""Author bounded native checks for the published complementary PPM encoder.

This is an independent host oracle and data author, not an assembly interpreter.
The original public Kotlin checks remain the source of the finite input corpus.
"""

import argparse
import json
from pathlib import Path
from function_lesson_cases import bits

ARENAS = {
    "input_path": 128,
    "output_path": 128,
    "source": 12576,
    "metadata": 64,
    "framebuffer": 16448,
    "destination": 12576,
    "status": 4,
    "count": 8,
    "mode": 4,
    "byte_count": 8,
    "pixel_capacity": 8,
    "dimensions": 8,
    "byte_capacity": 8,
}
MASK = (1 << 64) - 1


def integer_bytes(value, width=8):
    return (value & ((1 << (width * 8)) - 1)).to_bytes(width, "little")


def trials():
    rows = []
    for w in [1, 2, 3, 9, 10, 16, 31, 64]:
        for h in [1, 2, 9, 10, 64]:
            size = len(f"P6\n{w} {h}\n255\n") + 3 * w * h
            for kind, cap in enumerate([0, size - 1, size, -(1 << 63)]):
                rows.append(
                    dict(
                        name=f"negative {w} by {h} capacity-kind {kind}",
                        width=w,
                        height=h,
                        index=w * 3 + h * 7 + kind,
                        capacity=cap,
                        limit=3000000,
                        group="single-digit-width" if w < 10 else "two-digit-width",
                    )
                )
    for axis, values in [
        ("width", [0, 65, 0xFFFFFFFF]),
        ("height", [0, 65, 0x80000000]),
    ]:
        for value in values:
            rows.append(
                dict(
                    name=f"negative invalid {axis} {value}",
                    width=value if axis == "width" else 2,
                    height=value if axis == "height" else 2,
                    index=0,
                    capacity=12544,
                    limit=100000,
                    group="invalid",
                )
            )
    return rows


def construction(trial):
    w, h, index, cap = (trial[k] for k in ["width", "height", "index", "capacity"])
    valid = 1 <= w <= 64 and 1 <= h <= 64
    pixels = [
        ((j * 104729 + index * 8191) ^ 0xA57F3C19) & 0xFFFFFFFF
        for j in range(w * h if valid else 0)
    ]
    raw = b"".join(integer_bytes(pixel, 4) for pixel in pixels)
    encoded = (
        (
            f"P6\n{w} {h}\n255\n".encode("ascii")
            + bytes(
                255 - ((pixel >> shift) & 255)
                for pixel in pixels
                for shift in [16, 8, 0]
            )
        )
        if valid
        else b""
    )
    status = 1 if not valid else 2 if cap & MASK < len(encoded) else 0
    return dict(
        sourceOffset=index % 8 * 4,
        destinationOffset=(index * 7 + 1) % 32,
        raw=raw,
        result=encoded if status == 0 else b"",
        status=status,
    )


def seeded():
    return {
        **{label: bytes([0xA5]) * size for label, size in ARENAS.items()},
        "workspace": bytes([0xA5]) * 4096,
    }


def memory(arenas):
    return [dict(label=label, bytes=data.hex()) for label, data in arenas.items()]


def window(label, offset, size):
    return dict(at=dict(label=label, offset=offset), length=size)


def policies(readable, writable):
    # Watch each original arena separately: workspace and inter-object padding
    # were not caller regions in the original trace and must remain available.
    return [
        dict(
            from_=dict(label=label),
            until=dict(label=label, offset=size),
            readable=readable + writable,
            writable=writable,
        )
        for label, size in ARENAS.items()
    ]


def normalized_policies(readable, writable):
    rows = policies(readable, writable)
    for row in rows:
        row["from"] = row.pop("from_")
    return rows


def case_base(name, limit, arenas, calls, restrictions):
    saved = {
        **{f"s{i}": bits(0x2345678900000000 + 271 * i) for i in range(8)},
        "fp": bits(0x2345678900000000 + 271 * 8),
    }
    return dict(
        name=name,
        instructionLimit=limit,
        calls=calls,
        initialMemory=memory(arenas),
        initialRegisters=saved,
        initialRegisterAddresses={"sp": dict(label="workspace", offset=4096)},
        preservedRegisters=list(saved),
        stackAlignment=16,
        callerStackBytes=32,
        accessPolicies=restrictions,
        noExecution=[{"from": "function_return", "until": "ppm_parse"}],
    )


def definition(trial):
    row = construction(trial)
    so, dest, raw, result = (
        row[k] for k in ["sourceOffset", "destinationOffset", "raw", "result"]
    )
    arenas = seeded()
    source = bytearray(arenas["framebuffer"])
    source[so : so + len(raw)] = raw
    arenas["framebuffer"] = bytes(source)
    success = row["status"] == 0
    restrictions = normalized_policies(
        [window("framebuffer", so, len(raw))] if success else [],
        [window("destination", dest, len(result))] if success else [],
    )
    args = {
        "a1": bits((trial["height"] << 32) | (trial["width"] & 0xFFFFFFFF)),
        "a3": bits(trial["capacity"]),
    }
    addresses = (
        {
            "a0": dict(label="framebuffer", offset=so),
            "a2": dict(label="destination", offset=dest),
        }
        if success
        else {}
    )
    if not success:
        args.update(a0=bits(1), a2=bits(1))
    call = dict(
        entry="ppm_encode_negative",
        returnTo="function_return",
        arguments=args,
        argumentAddresses=addresses,
        expectedRegisters={"v0": bits(row["status"]), "v1": bits(len(result))},
        memory=(
            [dict(label="destination", offset=dest, bytes=result.hex())]
            if result
            else []
        ),
        accessPolicies=restrictions,
    )
    return case_base(trial["name"], trial["limit"], arenas, [call], restrictions)


def caller_case():
    arenas = seeded()
    rgb = bytes([255, 0, 0, 0, 255, 0, 0, 0, 255, 255, 255, 255])
    source = b"P6\n2 2\n255\n" + rgb
    arenas["source"] = source + arenas["source"][len(source) :]
    frame = b"".join(
        bytes([rgb[i + 2], rgb[i + 1], rgb[i], 0]) for i in range(0, len(rgb), 3)
    )
    arenas["framebuffer"] = frame + arenas["framebuffer"][len(frame) :]
    for label, text in [
        ("input_path", b"input.ppm\0"),
        ("output_path", b"output.ppm\0"),
    ]:
        arenas[label] = text + bytes(ARENAS[label] - len(text))
    for label, value in [
        ("status", -1),
        ("count", -1),
        ("mode", 6),
        ("byte_count", 23),
        ("pixel_capacity", 4096),
        ("dimensions", (2 << 32) | 2),
        ("byte_capacity", 12544),
    ]:
        arenas[label] = integer_bytes(value, ARENAS[label])
    arenas["workspace"] = bytes(4096)
    output = b"P6\n2 2\n255\n" + bytes(255 - b for b in rgb)
    reads = [
        window(label, 0, size)
        for label, size in [
            ("mode", 4),
            ("dimensions", 8),
            ("byte_capacity", 8),
            ("framebuffer", 16),
        ]
    ]
    writes = [window("destination", 0, 23)]
    publish = [window("status", 0, 4), window("count", 0, 8)]
    expected = {"v0": bits(0), "v1": bits(23)}
    calls = [
        dict(
            entry="main",
            returnTo="negative_return",
            arguments={},
            expectedRegisters=expected,
            memory=[dict(label="destination", bytes=output.hex())],
            accessPolicies=normalized_policies(reads, writes),
        ),
        dict(
            entry="negative_return",
            returnTo="function_return",
            arguments={},
            expectedRegisters=expected,
            memory=[
                dict(label="status", bytes=integer_bytes(0, 4).hex()),
                dict(label="count", bytes=integer_bytes(23).hex()),
            ],
            accessPolicies=normalized_policies([], publish),
        ),
    ]
    case = case_base(
        "negative caller publishes complementary image",
        3000000,
        arenas,
        calls,
        normalized_policies(reads, writes + publish),
    )
    # The caller publishes into objects immediately above its workspace.
    # Its callee is checked separately; these bytes are caller-owned results.
    case["callerStackBytes"] = 0
    return case


def corpus():
    rows = {
        group: []
        for group in ["single-digit-width", "two-digit-width", "invalid", "caller"]
    }
    for trial in trials():
        rows[trial["group"]].append(definition(trial))
    rows["caller"].append(caller_case())
    return rows


def profiles():
    return {
        f"{group}-{start//3:02d}": dict(formatVersion=11, cases=rows[start : start + 3])
        for group, rows in corpus().items()
        for start in range(0, len(rows), 3)
    }


def build(directory, check=False):
    directory = Path(directory)
    files = {
        f"ppm-negative-{name}.cases.json": json.dumps(profile, separators=(",", ":"))
        + "\n"
        for name, profile in profiles().items()
    }
    if not check:
        directory.mkdir(parents=True, exist_ok=True)
    for name, text in files.items():
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale PPM profile: {path}")
        else:
            path.write_text(text)
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
