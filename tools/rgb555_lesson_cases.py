"""Author native RGB555 checks from the existing public count/offset construction.

Generate bounded grading data only. The original script remains separately usable;
this tool neither writes publication prose nor supplies an independent solution.
"""

import argparse
import json
from pathlib import Path

from function_lesson_cases import bits
from partition_lesson_cases import word_bytes

DEFAULT = [
    123,
    45,
    67,
    0,
    0,
    0,
    255,
    255,
    255,
    128,
    64,
    247,
    7,
    3,
    7,
    8,
    4,
    8,
    248,
    252,
    248,
    31,
    127,
    223,
]
GUARDS = {
    "rgb_guard": 0x13579BDF,
    "rgb_end": 0x2468ACE0,
    "packed_guard": 0x31415926,
    "packed_end": 0x27182818,
}


def packed_bytes(source, count, offset):
    result = []
    for pixel in range(count):
        r, g, b = source[offset + pixel * 3 : offset + pixel * 3 + 3]
        word = (r // 8) * 1024 + (g // 8) * 32 + b // 8
        result.extend([word & 255, word >> 8])
    return result


def trials():
    result = []
    for count in range(33):
        offset = (count * 11) % 32
        seed = count
        destination = (offset * 7 + seed // 32 + 1) % 32
        source = [(index * 37 + seed * 19) & 255 for index in range(128)]
        expected = packed_bytes(source, count, offset)
        output = [(index * 17 + seed * 11) & 255 for index in range(96)]
        output[destination : destination + len(expected)] = [
            byte ^ 255 for byte in expected
        ]
        result.append(
            dict(
                name=f"rgb555 count {count}",
                count=count,
                offset=offset,
                seed=seed,
                destinationOffset=destination,
                input=source,
                output=output,
                expected=expected,
            )
        )
    return result


def definition(trial, caller=False):
    count = trial["count"]
    first = trial["offset"]
    last = trial["destinationOffset"]
    initial = [
        dict(label="rgb", bytes=bytes(trial["input"]).hex()),
        dict(label="packed", bytes=bytes(trial["output"]).hex()),
        dict(label="count", bytes=word_bytes([8])),
        dict(label="completed", bytes=word_bytes([-1])),
    ]
    initial += [
        dict(label=label, bytes=word_bytes([value])) for label, value in GUARDS.items()
    ]
    final_output = list(trial["output"])
    final_output[last : last + len(trial["expected"])] = trial["expected"]
    memory = [dict(row) for row in initial]
    next(row for row in memory if row["label"] == "packed")["bytes"] = bytes(
        final_output
    ).hex()
    if caller:
        next(row for row in memory if row["label"] == "completed")["bytes"] = (
            word_bytes([8])
        )
    calls = [
        dict(
            entry="main" if caller else "pack_rgb555",
            returnTo=continuation,
            arguments={} if caller else {"a2": bits(count)},
            expectedRegisters={"v0": bits(count)},
            memory=memory,
            **(
                {}
                if caller
                else {
                    "argumentAddresses": {
                        "a0": dict(label="rgb", offset=first),
                        "a1": dict(label="packed", offset=last),
                    }
                }
            ),
        )
        for continuation in (
            ("done",) if caller else ("function_return", "pack555_done")
        )
    ]
    saved = {
        **{f"s{index}": bits(0x1234567800000000 + index * 31) for index in range(8)},
        "fp": bits(0x2345678900000011),
    }
    source = dict(at=dict(label="rgb", offset=first), length=count * 3)
    output = dict(at=dict(label="packed", offset=last), length=count * 2)
    readable = [source, output] + (
        [dict(at=dict(label="count"), length=4)] if caller else []
    )
    writable = [output] + (
        [dict(at=dict(label="completed"), length=4)] if caller else []
    )
    return dict(
        name=trial["name"],
        instructionLimit=100000 if caller else 200000,
        calls=calls,
        initialMemory=initial,
        initialRegisters={
            **saved,
            "sp": bits(0x7FFF0000 if trial["seed"] % 2 == 0 else 0x7FFE8000),
        },
        preservedRegisters=list(saved),
        stackAlignment=16,
        callerStackBytes=32,
        accessPolicies=[
            {
                "from": dict(label="rgb_guard"),
                "until": dict(label="completed", offset=4),
                "readable": readable,
                "writable": writable,
            }
        ],
    )


def profile():
    source = DEFAULT + [0] * 104
    expected = packed_bytes(source, 8, 0)
    output = [(index * 17) & 255 for index in range(96)]
    output[:16] = [byte ^ 255 for byte in expected]
    caller = dict(
        name="default caller publishes eight RGB555 pixels",
        count=8,
        offset=0,
        seed=0,
        destinationOffset=0,
        input=source,
        output=output,
        expected=expected,
    )
    return dict(
        formatVersion=8,
        cases=[definition(trial) for trial in trials()]
        + [definition(caller, caller=True)],
    )


def build(directory, check=False):
    directory = Path(directory)
    files = {
        "rgb555.cases.json": json.dumps(profile(), indent=2) + "\n",
        "caller-exit.cases.json": json.dumps(
            dict(
                formatVersion=1,
                cases=[
                    dict(
                        name="silent RGB555 caller exit",
                        input="",
                        stdout="",
                        instructionLimit=100000,
                    )
                ],
            ),
            indent=2,
        )
        + "\n",
    }
    if not check:
        directory.mkdir(parents=True, exist_ok=True)
    for name, contents in files.items():
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != contents:
                raise ValueError(f"Stale RGB555 data: {path}")
        else:
            path.write_text(contents)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
