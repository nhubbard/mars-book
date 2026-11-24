"""Generate native rotation contracts without prescribing a traversal schedule."""

import argparse
import json
from pathlib import Path

from function_lesson_cases import bits, saved_values
from partition_lesson_cases import word_bytes

SHAPES = (
    (0, 5),
    (5, 0),
    (0, 0),
    (1, 1),
    (1, 7),
    (7, 1),
    (3, 5),
    (5, 3),
    (5, 9),
    (16, 16),
    (16, 15),
    (15, 16),
    (15, 13),
    (13, 15),
    (4, 4),
    (3, 4),
    (4, 3),
    (4, 5),
    (5, 4),
    (0, 16),
    (16, 0),
)
GUARDS = ("input_guard", "output_guard", "metadata_guard")
UNTOUCHED = 0x6D3A95C7


def signed(value):
    value &= 0xFFFFFFFF
    return value - (1 << 32) if value >= 1 << 31 else value


def source_words(number):
    return [
        (
            -2147483648
            if index % 5 == 0
            else (
                2147483647
                if index % 5 == 1
                else signed((index * 0x01020409) ^ (0x5AA555AA + number))
            )
        )
        for index in range(256)
    ]


def mapping(rows, columns):
    return [j * rows + (rows - 1 - i) for i in range(rows) for j in range(columns)]


def rotate(words, rows, columns, in_shift, out_shift):
    result = [UNTOUCHED] * 256
    for cell, destination in enumerate(mapping(rows, columns)):
        result[out_shift + destination] = words[in_shift + cell]
    return result


def case(number, rows, columns, caller=False):
    count = rows * columns
    in_shift = 0 if caller or count >= 250 else 1
    out_shift = 0 if caller or count >= 250 else 3
    source = list(range(256)) if caller else source_words(number)
    initial = [
        dict(label="input", bytes=word_bytes(source)),
        dict(label="output", bytes=word_bytes([UNTOUCHED] * 256)),
        dict(label="copied", bytes=word_bytes([-1 if caller else 0x5A000000 + number])),
    ]
    initial += [
        dict(label=label, bytes=word_bytes([0x13579BDF ^ (number * 31 + index)]))
        for index, label in enumerate(GUARDS)
    ]
    expected = [
        dict(label="input", bytes=word_bytes(source)),
        dict(
            label="output",
            bytes=word_bytes(rotate(source, rows, columns, in_shift, out_shift)),
        ),
        dict(label="copied", bytes=word_bytes([count])) if caller else initial[2],
        *initial[3:],
    ]
    calls = (
        [
            dict(
                entry="main",
                returnTo="rotate_exit",
                arguments={},
                expectedRegisters={"v0": bits(count)},
                memory=expected,
            )
        ]
        if caller
        else [
            dict(
                entry="rotate_clockwise",
                returnTo=continuation,
                arguments={"a2": bits(rows), "a3": bits(columns)},
                argumentAddresses={
                    "a0": dict(label="input", offset=4 * in_shift),
                    "a1": dict(label="output", offset=4 * out_shift),
                },
                expectedRegisters={"v0": bits(count)},
                memory=expected,
            )
            for continuation in ("rotate_done", "rotate_exit")
        ]
    )
    kept = saved_values(0x1122334400000000 + number * 0x01010101)
    result = dict(
        name=(
            "complete rotation caller result"
            if caller
            else f"rotation {rows} by {columns} at words {in_shift} and {out_shift}"
        ),
        instructionLimit=100000,
        calls=calls,
        initialMemory=initial,
        noStores=[
            dict(label="input", length=1024),
            *[dict(label=label, length=4) for label in GUARDS],
            *([] if caller else [dict(label="copied", length=4)]),
        ],
        accessBounds=[
            dict(
                label=label,
                length=1024,
                permittedOffset=4 * shift,
                permittedLength=4 * count,
            )
            for label, shift in [("input", in_shift), ("output", out_shift)]
        ],
        transfers=[
            dict(
                source=dict(label="input", offset=4 * in_shift),
                destination=dict(label="output", offset=4 * out_shift),
                destinationIndices=mapping(rows, columns),
            )
        ],
        protectCallerStack=True,
        preservedRegisters=list(kept),
        stackAlignment=16,
        callerStackBytes=32,
    )
    if not caller:
        result["initialRegisters"] = {
            **{r: bits(v) for r, v in kept.items()},
            "sp": bits(0x7FFFEFFC - number * 4096),
        }
    return result


def profile():
    return dict(
        formatVersion=5,
        cases=[case(n, *shape) for n, shape in enumerate(SHAPES)]
        + [case(len(SHAPES), 3, 5, caller=True)],
    )


def build(directory, check=False):
    directory = Path(directory)
    files = {
        "matrix-rotate.cases.json": json.dumps(profile(), separators=(",", ":")) + "\n",
        "caller-exit.cases.json": json.dumps(
            dict(
                formatVersion=1,
                cases=[
                    dict(
                        name="silent normal rotation caller",
                        input="",
                        stdout="",
                        instructionLimit=10000,
                    )
                ],
            ),
            indent=2,
        )
        + "\n",
    }
    for name, text in files.items():
        if len(text.encode()) > 1048576:
            raise ValueError("Native rotation profile exceeds one MiB")
        path = directory / name
        if check:
            if path.read_text() != text:
                raise ValueError(f"Stale rotation profile: {path}")
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
