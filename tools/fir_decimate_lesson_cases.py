"""Retain the complete public FIR decimation corpus as bounded native grading data."""

import argparse
import json
from pathlib import Path

from function_lesson_cases import bits
from partition_lesson_cases import word_bytes

NAMES = ("source", "coeff", "output")
SIZES = {"source": 192, "coeff": 64, "output": 192}
SOURCE = [0] * 7 + [16000] + [0] * 15
TAPS = [4096] * 8
GUARDS = {
    "source_guard": 0x13579BDF,
    "source_end": 0x2468ACE0,
    "coeff_guard": 0x31415926,
    "coeff_end": 0x27182818,
    "output_guard": 0x11223344,
    "output_end": 0x22334455,
    "caller_end": 0x55667788,
}


class JavaRandom:
    """The pinned public script calls nextInt(65536), a power-of-two bound."""

    def __init__(self, seed):
        self.state = (seed ^ 0x5DEECE66D) & ((1 << 48) - 1)

    def signed_halfword(self):
        self.state = (self.state * 0x5DEECE66D + 11) & ((1 << 48) - 1)
        return (self.state >> 32) - 32768


def encoded(values):
    return [byte for value in values for byte in (value & 255, (value >> 8) & 255)]


def numerators(trial):
    return [
        sum(trial["input"][2 * i + j] * trial["taps"][j] for j in range(8))
        for i in range(trial["count"])
    ]


def quantized(trial):
    return [(value + 16384) // 32768 for value in numerators(trial)]


def results(trial):
    return [max(-32768, min(32767, value)) for value in quantized(trial)]


def clips(trial):
    return sum(value < -32768 or value > 32767 for value in quantized(trial))


def trials():
    rows = []
    for n in range(33):
        for variant in range(4):
            length = 7 if n == 0 else 2 * n + 6
            if variant == 0:
                random = JavaRandom(21162026 + n * 97 + variant)
                source = [random.signed_halfword() for _ in range(length)]
                taps = [random.signed_halfword() for _ in range(8)]
            elif variant == 1:
                source = [
                    16000 if i == 7 else -8000 if i == 9 else 0 for i in range(length)
                ]
                taps = [1024 * (i + 1) for i in range(8)]
            elif variant == 2:
                source = [-32768] * length
                taps = [-32768] * 8
            else:
                source = [-32768 if i % 2 == 0 else 32767 for i in range(length)]
                taps = [32767 if i % 2 == 0 else -32768 for i in range(8)]
            rows.append(
                dict(
                    name=f"decimate count={n} variant={variant}",
                    input=source,
                    taps=taps,
                    count=n,
                    index=n * 4 + variant,
                    group=f"family-{variant}",
                )
            )
    for i, c in enumerate([16383, 16384, 16385, -16383, -16384, -16385]):
        rows.append(
            dict(
                name=f"signed rounding {c}",
                input=[1] + [0] * 7,
                taps=[c] + [0] * 7,
                count=1,
                index=i + 1,
                group="family-0",
            )
        )
    for i, (source, taps) in enumerate(
        [
            ([32767, 1, 1] + [0] * 5, [32767, 32767, 1] + [0] * 5),
            ([-32768, 3] + [0] * 6, [32767, -16384] + [0] * 6),
            ([1] * 8, [16384] * 8),
        ]
    ):
        rows.append(
            dict(
                name=f"round once and clip after rounding {i}",
                input=source,
                taps=taps,
                count=1,
                index=i + 3,
                group="family-0",
            )
        )
    return rows


def construction(trial):
    index = trial["index"]
    source = "source" if index % 2 == 0 else "output"
    dest = "output" if index % 2 == 0 else "source"
    so = index % 32
    co = (index * 7 + 1) % 32
    oo = (index * 13 + 3) % 32
    arenas = {name: [0] * size for name, size in SIZES.items()}
    arenas["source"][:46] = encoded(SOURCE)
    arenas["coeff"][:16] = encoded(TAPS)

    def prepare(name, offset, values, poison=False):
        data = [(i * 31 + index * 17 + 193) & 255 for i in range(SIZES[name])]
        data[offset : offset + len(values)] = [
            value ^ 255 if poison else value for value in values
        ]
        arenas[name] = data

    expected = encoded(results(trial))
    prepare(source, so, encoded(trial["input"]))
    prepare("coeff", co, encoded(trial["taps"]))
    prepare(dest, oo, expected, True)
    return dict(
        index=index,
        count=trial["count"],
        source=source,
        dest=dest,
        sourceOffset=so,
        coeffOffset=co,
        outputOffset=oo,
        expected=expected,
        clips=clips(trial),
        arenas=arenas,
    )


def initial_memory(arenas):
    return [
        dict(label=name, bytes=bytes(image).hex()) for name, image in arenas.items()
    ] + [
        dict(label=name, bytes=word_bytes([value]))
        for name, value in {
            **GUARDS,
            "count": 8,
            "processed": 99,
            "clipped": 99,
        }.items()
    ]


def saved_state(index):
    saved = {
        **{f"s{i}": bits(0x3456789000000000 + i * 1009) for i in range(8)},
        "fp": bits(0x2345678900000033),
    }
    return {
        **saved,
        "sp": bits(0x7FFF0000 if index % 2 == 0 else 0x7FFE8000),
        "v0": bits(0x12345678),
        "v1": bits(0x23456789),
    }, list(saved)


def definition(trial):
    row = construction(trial)
    n = row["count"]
    initial = initial_memory(row["arenas"])
    final = [dict(memory) for memory in initial]
    output = bytearray.fromhex(
        next(memory["bytes"] for memory in final if memory["label"] == row["dest"])
    )
    output[row["outputOffset"] : row["outputOffset"] + 2 * n] = bytes(row["expected"])
    next(memory for memory in final if memory["label"] == row["dest"])[
        "bytes"
    ] = output.hex()
    addresses = {
        "a0": dict(label=row["source"], offset=row["sourceOffset"]),
        "a1": dict(label="coeff", offset=row["coeffOffset"]),
        "a2": dict(label=row["dest"], offset=row["outputOffset"]),
    }
    registers, preserved = saved_state(row["index"])
    readable = [
        dict(at=addresses["a0"], length=0 if n == 0 else 2 * len(trial["input"])),
        dict(at=addresses["a1"], length=0 if n == 0 else 16),
        dict(at=addresses["a2"], length=2 * n),
    ]
    return dict(
        name=trial["name"],
        instructionLimit=100000,
        calls=[
            dict(
                entry="decimate_q15",
                returnTo="function_return" if row["index"] % 2 == 0 else "done",
                arguments={"a3": bits(n)},
                argumentAddresses=addresses,
                expectedRegisters={"v0": bits(n), "v1": bits(row["clips"])},
                memory=final,
            )
        ],
        initialMemory=initial,
        initialRegisters=registers,
        preservedRegisters=preserved,
        stackAlignment=16,
        callerStackBytes=32,
        noExecution=[{"from": "function_return", "until": "done"}],
        accessPolicies=[
            {
                "from": dict(label="source_guard"),
                "until": dict(label="caller_end", offset=4),
                "readable": readable,
                "writable": [readable[-1]],
            }
        ],
    )


def caller_case():
    trial = dict(
        name="default caller publishes eight outputs",
        input=SOURCE[:22],
        taps=TAPS,
        count=8,
        index=0,
    )
    case = definition(trial)
    arenas = {name: [0] * size for name, size in SIZES.items()}
    arenas["source"][:46] = encoded(SOURCE)
    arenas["coeff"][:16] = encoded(TAPS)
    initial = initial_memory(arenas)
    before = [dict(memory) for memory in initial]
    output = bytearray(192)
    output[:16] = bytes(encoded(results(trial)))
    next(memory for memory in before if memory["label"] == "output")[
        "bytes"
    ] = output.hex()
    after = [dict(memory) for memory in before]
    for memory in after:
        if memory["label"] == "processed":
            memory["bytes"] = word_bytes([8])
        if memory["label"] == "clipped":
            memory["bytes"] = word_bytes([0])
    case["initialMemory"] = initial
    case["calls"] = [
        dict(
            entry="main",
            returnTo="decimate_ready",
            arguments={},
            expectedRegisters={"v0": bits(8), "v1": bits(0)},
            memory=before,
        ),
        dict(
            entry="decimate_ready",
            returnTo="results_ready",
            arguments={},
            expectedRegisters={"v0": bits(8), "v1": bits(0)},
            memory=after,
        ),
    ]
    case["accessPolicies"][0].update(
        readable=[
            dict(at=dict(label="source"), length=44),
            dict(at=dict(label="coeff"), length=16),
            dict(at=dict(label="output"), length=16),
            dict(at=dict(label="count"), length=4),
        ],
        writable=[
            dict(at=dict(label="output"), length=16),
            dict(at=dict(label="processed"), length=4),
            dict(at=dict(label="clipped"), length=4),
        ],
    )
    caller_policy = case["accessPolicies"][0]
    case["calls"][0]["accessPolicies"] = [
        {**caller_policy, "writable": [dict(at=dict(label="output"), length=16)]}
    ]
    case["calls"][1]["accessPolicies"] = [
        {
            "from": caller_policy["from"],
            "until": caller_policy["until"],
            "readable": [],
            "writable": [
                dict(at=dict(label="processed"), length=4),
                dict(at=dict(label="clipped"), length=4),
            ],
        }
    ]
    return case


def split_case(trial, cut):
    row = construction(trial)
    n = trial["count"]
    case = definition(trial)
    arenas = {name: [0] * size for name, size in SIZES.items()}
    so, co, whole, split = 13, 7, 3, 99
    arenas["source"][so : so + len(trial["input"]) * 2] = encoded(trial["input"])
    arenas["coeff"][co : co + 16] = encoded(trial["taps"])
    expected = encoded(results(trial))
    arenas["output"] = [(i * 17 + 19) & 255 for i in range(192)]
    for offset in [whole, split]:
        arenas["output"][offset : offset + 2 * n] = [byte ^ 255 for byte in expected]
    initial = initial_memory(arenas)
    current = bytearray(arenas["output"])
    calls = []
    ranges = [(0, n, whole), (0, cut, split), (cut, n - cut, split + 2 * cut)]
    for index, (first, count, dest) in enumerate(ranges):
        current[dest : dest + 2 * count] = bytes(
            expected[first * 2 : (first + count) * 2]
        )
        memory = [dict(entry) for entry in initial]
        next(entry for entry in memory if entry["label"] == "output")[
            "bytes"
        ] = current.hex()
        clip = sum(
            value < -32768 or value > 32767
            for value in quantized(trial)[first : first + count]
        )
        source_window = dict(
            at=dict(label="source", offset=so + first * 4),
            length=0 if count == 0 else 4 * count + 12,
        )
        coeff_window = dict(
            at=dict(label="coeff", offset=co), length=0 if count == 0 else 16
        )
        output_window = dict(at=dict(label="output", offset=dest), length=count * 2)
        invocation_policy = {
            "from": dict(label="source_guard"),
            "until": dict(label="caller_end", offset=4),
            "readable": [source_window, coeff_window, output_window],
            "writable": [output_window],
        }
        calls.append(
            dict(
                entry="decimate_q15",
                returnTo="function_return" if index % 2 == 0 else "done",
                arguments={"a3": bits(count)},
                argumentAddresses={
                    "a0": dict(label="source", offset=so + first * 4),
                    "a1": dict(label="coeff", offset=co),
                    "a2": dict(label="output", offset=dest),
                },
                expectedRegisters={"v0": bits(count), "v1": bits(clip)},
                memory=memory,
                accessPolicies=[invocation_policy],
            )
        )
    case.update(
        name=f'split {cut} family {trial["group"]}',
        instructionLimit=300000,
        initialMemory=initial,
        calls=calls,
    )
    output_windows = [
        dict(at=dict(label="output", offset=offset), length=n * 2)
        for offset in [whole, split]
    ]
    case["accessPolicies"][0].update(
        readable=[
            dict(at=dict(label="source", offset=so), length=len(trial["input"]) * 2),
            dict(at=dict(label="coeff", offset=co), length=16),
        ]
        + output_windows,
        writable=output_windows,
    )
    return case


def profiles():
    groups = {f"family-{n}": [] for n in range(4)}
    all_trials = trials()
    for trial in all_trials:
        groups[trial["group"]].append(definition(trial))
    impulse = next(
        trial
        for trial in all_trials
        if trial["count"] == 32 and trial["group"] == "family-1"
    )
    minimum = next(
        trial
        for trial in all_trials
        if trial["count"] == 32 and trial["group"] == "family-2"
    )
    groups["family-1"] += [split_case(impulse, cut) for cut in [0, 5, 32]]
    groups["family-2"].append(split_case(minimum, 5))
    groups["family-0"].append(caller_case())
    return {name: dict(formatVersion=9, cases=cases) for name, cases in groups.items()}


def build(directory, check=False):
    directory = Path(directory)
    files = {
        f"fir-decimate-{name}.cases.json": json.dumps(profile, separators=(",", ":"))
        + "\n"
        for name, profile in profiles().items()
    }
    files["caller-exit.cases.json"] = (
        json.dumps(
            dict(
                formatVersion=1,
                cases=[
                    dict(
                        name="silent decimation caller exit",
                        input="",
                        stdout="",
                        instructionLimit=100000,
                    )
                ],
            ),
            indent=2,
        )
        + "\n"
    )
    for name, text in files.items():
        if len(text.encode()) > 1048576:
            raise ValueError(f"Native profile exceeds one MiB: {name}")
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale FIR profile: {path}")
        else:
            directory.mkdir(parents=True, exist_ok=True)
            path.write_text(text)
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
