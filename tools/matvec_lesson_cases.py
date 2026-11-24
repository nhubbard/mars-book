"""Author finite native matrix-vector cases from the original public exercise.

This generates grading data, never publication prose or a reference implementation.
The MARS parser owns strict profile validation and the simulator executes every case.
"""

import argparse
import json
from pathlib import Path

from function_lesson_cases import bits
from partition_lesson_cases import word_bytes

SHAPES = ((0, 16), (16, 0), (1, 1), (1, 2), (2, 3), (3, 5), (15, 13), (16, 16))
PALETTE = (-32768, 32767, 0, 1, -1, 16384, -16384)
SENTINEL = 0x5A5AA5A55A5AA5A5
GUARDS = {
    "matrix_guard": 0x13579BDF,
    "matrix_end": 0x2468ACE0,
    "vector_guard": 0x31415926,
    "vector_end": 0x27182818,
    "output_end": 0x12345678,
}


def trials():
    result = []
    for seed, (m, k) in enumerate(SHAPES):
        mo = seed % (min(7, 256 - m * k) + 1)
        vo = seed % (min(7, 16 - k) + 1)
        oo = seed % (min(7, 16 - m) + 1)
        matrix, vector = [0x12345678] * 256, [0x23456789] * 16
        matrix[mo : mo + m * k] = [
            PALETTE[(index * 3 + seed) % len(PALETTE)] for index in range(m * k)
        ]
        vector[vo : vo + k] = [
            PALETTE[(index * 5 + seed + 1) % len(PALETTE)] for index in range(k)
        ]
        result.append(
            dict(
                name=f"matrix-vector {m} by {k}",
                m=m,
                k=k,
                seed=seed,
                matrixOffset=mo,
                vectorOffset=vo,
                outputOffset=oo,
                matrix=matrix,
                vector=vector,
                origin="original",
            )
        )
    for name, m, k, active_matrix, active_vector in (
        ("default direct function", 2, 3, [1, 2, 3, 4, 5, 6], [7, 8, 9]),
        ("positive 2^31", 1, 2, [-32768, -32768], [-32768, -32768]),
    ):
        result.append(
            dict(
                name=name,
                m=m,
                k=k,
                seed=0,
                matrixOffset=0,
                vectorOffset=0,
                outputOffset=0,
                matrix=active_matrix + [0x12345678] * (256 - len(active_matrix)),
                vector=active_vector + [0x23456789] * (16 - len(active_vector)),
                origin="additional",
            )
        )
    return result


def product(trial):
    k = trial["k"]
    mo = trial["matrixOffset"]
    vo = trial["vectorOffset"]
    return [
        sum(
            trial["matrix"][mo + row * k + col] * trial["vector"][vo + col]
            for col in range(k)
        )
        for row in range(trial["m"])
    ]


def doublewords(values):
    return b"".join(
        (value & ((1 << 64) - 1)).to_bytes(8, "little") for value in values
    ).hex()


def definition(trial, caller=False):
    m, k = trial["m"], trial["k"]
    oo = trial["outputOffset"]
    initial = [
        dict(label="matrix", bytes=word_bytes(trial["matrix"])),
        dict(label="vector", bytes=word_bytes(trial["vector"])),
        dict(label="output", bytes=doublewords([SENTINEL] * 16)),
        dict(label="shape", bytes=word_bytes([m, k])),
        dict(label="completed", bytes=word_bytes([-1])),
    ]
    initial += [
        dict(label=label, bytes=word_bytes([value])) for label, value in GUARDS.items()
    ]
    output = [SENTINEL] * 16
    output[oo : oo + m] = product(trial)
    expected = [dict(item) for item in initial]
    next(item for item in expected if item["label"] == "output")["bytes"] = doublewords(
        output
    )
    if caller:
        next(item for item in expected if item["label"] == "completed")["bytes"] = (
            word_bytes([m])
        )
    arguments = {
        "a0": dict(label="matrix", offset=trial["matrixOffset"] * 4),
        "a1": dict(label="vector", offset=trial["vectorOffset"] * 4),
        "a2": dict(label="output", offset=oo * 8),
        "a3": dict(label="shape"),
    }
    calls = [
        dict(
            entry="main" if caller else "matvec",
            returnTo=stop,
            arguments={},
            expectedRegisters={"v0": bits(m)},
            memory=expected,
            **({} if caller else {"argumentAddresses": arguments}),
        )
        for stop in (("done",) if caller else ("function_return", "matvec_done"))
    ]
    saved = {
        **{f"s{index}": bits(0x1234567800000000 + index * 31) for index in range(8)},
        "fp": bits(0x2345678900000011),
    }

    def window(label, length, offset=0):
        return dict(at=dict(label=label, offset=offset), length=length)

    active = window("output", m * 8, oo * 8)
    readable = [
        window("matrix", m * k * 4 if m else 0, trial["matrixOffset"] * 4),
        window("vector", k * 4 if m else 0, trial["vectorOffset"] * 4),
        active,
        window("shape", 8),
    ]
    writable = [active] + ([window("completed", 4)] if caller else [])
    return dict(
        name="default caller publishes two complete sums" if caller else trial["name"],
        instructionLimit=100000,
        calls=calls,
        initialMemory=initial,
        initialRegisters={**saved, "sp": bits(0x7FFF0000)},
        preservedRegisters=list(saved),
        stackAlignment=16,
        callerStackBytes=32,
        initializedOutput=[active],
        accessPolicies=[
            {
                "from": dict(label="matrix_guard"),
                "until": dict(label="completed", offset=4),
                "readable": readable,
                "writable": writable,
            }
        ],
    )


def profile():
    inputs = trials()
    return dict(
        formatVersion=8,
        cases=[definition(trial) for trial in inputs]
        + [definition(inputs[-2], caller=True)],
    )


def build(directory, check=False):
    directory = Path(directory)
    files = {
        "matrix-vector.cases.json": json.dumps(profile(), indent=2) + "\n",
        "caller-exit.cases.json": json.dumps(
            dict(
                formatVersion=1,
                cases=[
                    dict(
                        name="silent normal caller exit",
                        input="",
                        stdout="",
                        instructionLimit=50000,
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
                raise ValueError(f"Stale matrix-vector data: {path}")
        else:
            path.write_text(contents)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
