"""Preserve all public saturated-subtraction trials as bounded native function data.

The native reader validates profiles. This tool authors finite checking data;
it does not generate publication prose or supply the student's implementation.
"""

import argparse
import json
from pathlib import Path

from function_lesson_cases import bits
from partition_lesson_cases import word_bytes

NAMES = ("pcm_a", "pcm_b", "pcm_c", "pcm_d")
LEFT = (-32768, 32767, 30000, -30000, 0, 32767, -32768, 1234, 30000)
RIGHT = (0, 0, -10000, 10000, -1, -1, 1, 5678, -10000)
GUARDS = {
    "pcm_a_guard": 0x13579BDF,
    "pcm_a_end": 0x2468ACE0,
    "pcm_b_guard": 0x31415926,
    "pcm_b_end": 0x27182818,
    "pcm_c_guard": 0x11223344,
    "pcm_c_end": 0x22334455,
    "pcm_d_guard": 0x33445566,
    "pcm_d_end": 0x44556677,
    "caller_end": 0x55667788,
}


def encoded(samples):
    return [byte for value in samples for byte in (value & 255, (value >> 8) & 255)]


def trials():
    result = []
    for n in range(65):
        for mode in range(5):
            seed = n * 5 + mode
            result.append(
                dict(
                    name=f"difference n={n} alias={mode}",
                    left=[LEFT[(index + seed) % 9] for index in range(n)],
                    right=[RIGHT[(index * 5 + seed) % 9] for index in range(n)],
                    mode=mode,
                    seed=seed,
                )
            )
    for index, (a, b) in enumerate(
        [
            (-32768, 32767),
            (32767, -32768),
            (30000, -2767),
            (-30000, 2768),
            (30000, -2768),
            (-30000, 2769),
        ]
    ):
        result.append(
            dict(
                name=f"difference boundary {index}",
                left=[a],
                right=[b],
                mode=0,
                seed=index + 1,
            )
        )
    return result


def construction(trial):
    mode = trial["mode"]
    seed = trial["seed"]
    count = len(trial["left"])
    left = NAMES[seed % 4]
    right = left if mode >= 3 else NAMES[(seed + 1) % 4]
    first = (seed * 3) % 32
    second = first if mode >= 3 else (seed * 5 + 1) % 32
    dest = left if mode in (1, 4) else right if mode == 2 else NAMES[(seed + 2) % 4]
    last = first if mode in (1, 4) else second if mode == 2 else (seed * 7 + 2) % 32
    a = trial["left"]
    b = a if mode >= 3 else trial["right"]
    exact = [x - y for x, y in zip(a, b)]
    expected = [max(-32768, min(32767, value)) for value in exact]
    arenas = {name: [0] * 192 for name in NAMES}
    arenas["pcm_a"][:18] = encoded(LEFT)
    arenas["pcm_b"][:18] = encoded(RIGHT)

    def fill(label, offset, values):
        image = [(index * 31 + seed * 17 + 193) & 255 for index in range(192)]
        image[offset : offset + len(values)] = values
        arenas[label] = image

    fill(left, first, encoded(a))
    if right != left:
        fill(right, second, encoded(b))
    if mode in (0, 3):
        fill(dest, last, [byte ^ 255 for byte in encoded(expected)])
    return dict(
        count=count,
        seed=seed,
        mode=mode,
        left=left,
        right=right,
        dest=dest,
        firstOffset=first,
        secondOffset=second,
        destOffset=last,
        arenas=arenas,
        expected=encoded(expected),
        clips=sum(value < -32768 or value > 32767 for value in exact),
    )


def definition(trial):
    row = construction(trial)
    n = row["count"]
    seed = row["seed"]
    initial = [
        dict(label=name, bytes=bytes(row["arenas"][name]).hex()) for name in NAMES
    ]
    initial += [
        dict(label=name, bytes=word_bytes([value]))
        for name, value in {
            **GUARDS,
            "count": 9,
            "processed": 99,
            "clipped": 99,
        }.items()
    ]
    final = [dict(memory) for memory in initial]
    output = bytearray.fromhex(
        next(memory["bytes"] for memory in final if memory["label"] == row["dest"])
    )
    output[row["destOffset"] : row["destOffset"] + n * 2] = bytes(row["expected"])
    next(memory for memory in final if memory["label"] == row["dest"])[
        "bytes"
    ] = output.hex()
    arguments = {
        key: dict(label=row[name], offset=row[offset])
        for key, name, offset in [
            ("a0", "left", "firstOffset"),
            ("a1", "right", "secondOffset"),
            ("a2", "dest", "destOffset"),
        ]
    }
    windows = [dict(at=at, length=n * 2) for at in arguments.values()]
    # Exact aliases are one permission window, not permission to cross adjacent slices.
    readable = list(
        {
            (window["at"]["label"], window["at"]["offset"]): window
            for window in windows
        }.values()
    )
    saved = {
        **{f"s{index}": bits(0x1234567800000000 + index * 31) for index in range(8)},
        "fp": bits(0x2345678900000011),
    }
    return dict(
        name=trial["name"],
        instructionLimit=100000,
        calls=[
            dict(
                entry="subtract_pcm16",
                returnTo="function_return" if seed % 2 == 0 else "done",
                arguments={"a3": bits(n)},
                argumentAddresses=arguments,
                expectedRegisters={"v0": bits(n), "v1": bits(row["clips"])},
                memory=final,
            )
        ],
        initialMemory=initial,
        initialRegisters={
            **saved,
            "sp": bits(0x7FFF0000 if seed % 2 == 0 else 0x7FFE8000),
            "v0": bits(99),
            "v1": bits(99),
        },
        preservedRegisters=list(saved),
        stackAlignment=16,
        callerStackBytes=32,
        accessPolicies=[
            {
                "from": dict(label="pcm_a_guard"),
                "until": dict(label="caller_end", offset=4),
                "readable": readable,
                "writable": [windows[2]],
            }
        ],
    )


def caller_case():
    row = construction(dict(left=list(LEFT), right=list(RIGHT), mode=0, seed=0))
    case = definition(
        dict(
            name="default nine-sample caller publishes five clips",
            left=list(LEFT),
            right=list(RIGHT),
            mode=0,
            seed=0,
        )
    )
    arenas = {name: [0] * 192 for name in NAMES}
    arenas["pcm_a"][:18] = encoded(LEFT)
    arenas["pcm_b"][:18] = encoded(RIGHT)
    initial = case["initialMemory"]
    for memory in initial:
        if memory["label"] in arenas:
            memory["bytes"] = bytes(arenas[memory["label"]]).hex()
    output = bytearray(192)
    output[:18] = bytes(row["expected"])
    before = [dict(memory) for memory in initial]
    next(memory for memory in before if memory["label"] == "pcm_c")[
        "bytes"
    ] = output.hex()
    published = [dict(memory) for memory in before]
    for memory in published:
        if memory["label"] == "processed":
            memory["bytes"] = word_bytes([9])
        if memory["label"] == "clipped":
            memory["bytes"] = word_bytes([5])
    case["calls"] = [
        dict(
            entry="main",
            returnTo="subtract_ready",
            arguments={},
            expectedRegisters={"v0": bits(9), "v1": bits(5)},
            memory=before,
        ),
        dict(
            entry="subtract_ready",
            returnTo="results_ready",
            arguments={},
            expectedRegisters={"v0": bits(9), "v1": bits(5)},
            memory=published,
        ),
    ]
    case["instructionLimit"] = 100000
    case["accessPolicies"][0].update(
        readable=[
            dict(at=dict(label=name), length=18) for name in ["pcm_a", "pcm_b", "pcm_c"]
        ]
        + [dict(at=dict(label="count"), length=4)],
        writable=[
            dict(at=dict(label="pcm_c"), length=18),
            dict(at=dict(label="processed"), length=4),
            dict(at=dict(label="clipped"), length=4),
        ],
    )
    return case


def reuse_case():
    trial = dict(
        name="reused clipping count across vector scalar and empty calls",
        left=[0, 32767, -32768, 30000, -30000, 1234, 30000, -30000],
        right=[0, -1, 1, -10000, 10000, 5678, -10000, 10000],
        mode=0,
        seed=40,
    )
    case = definition(trial)
    original = case["calls"][0]
    row = construction(trial)
    assert row["clips"] > 0
    first = trial["left"][0] - trial["right"][0]
    assert -32768 <= first <= 32767
    calls = []
    for index, n in enumerate([8, 1, 8, 0]):
        call = {
            **original,
            "arguments": {"a3": bits(n)},
            "returnTo": "function_return" if index % 2 == 0 else "done",
            "expectedRegisters": {
                "v0": bits(n),
                "v1": bits(row["clips"] if n == 8 else 0),
            },
        }
        calls.append(call)
    case["calls"] = calls
    case["instructionLimit"] = 400000
    return case


def profiles():
    groups = {f"alias-{mode}": [] for mode in range(5)}
    for trial in trials():
        groups[f'alias-{trial["mode"]}'].append(definition(trial))
    groups["alias-0"].append(reuse_case())
    groups["alias-0"].append(caller_case())
    return {name: dict(formatVersion=8, cases=cases) for name, cases in groups.items()}


def build(directory, check=False):
    directory = Path(directory)
    files = {
        f"pcm-subtract-{name}.cases.json": json.dumps(profile, separators=(",", ":"))
        + "\n"
        for name, profile in profiles().items()
    }
    files["caller-exit.cases.json"] = (
        json.dumps(
            dict(
                formatVersion=1,
                cases=[
                    dict(
                        name="silent subtraction caller exit",
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
                raise ValueError(f"Stale PCM profile: {path}")
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
