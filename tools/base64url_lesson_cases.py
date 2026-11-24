"""Preserve the public unpadded URL-safe encoder corpus in bounded native profiles."""

import argparse
import base64
import json
from pathlib import Path
from function_lesson_cases import bits
from partition_lesson_cases import word_bytes

DEFAULT = [251, 255, 255, 255]
CONTROL = {
    "source_guard": 0x13579BDF,
    "source_end": 0x2468ACE0,
    "destination_guard": 0x31415926,
    "destination_end": 0x27182818,
    "status": -1,
    "caller_end": 0x11223344,
}


def encode(raw):
    return list(base64.urlsafe_b64encode(bytes(raw)).rstrip(b"="))


def integer_bytes(value, width=8):
    return (value & ((1 << (8 * width)) - 1)).to_bytes(width, "little").hex()


def trials():
    rows = []
    for n in range(65):
        for kind in range(5):
            raw = [(i * 73 + n * 11 + kind * 17) & 255 for i in range(n)]
            needed = len(encode(raw))
            capacity = [0, max(0, needed - 1), needed, 192, 1 << 63][kind]
            rows.append(
                dict(
                    name=f"URL length={n} capacity-kind={kind}",
                    raw=raw,
                    capacity=capacity,
                    seed=n * 5 + kind,
                    count=n,
                    badSource=False,
                    badDestination=False,
                    group="short" if n < 32 else "long",
                )
            )
    rows += [
        dict(
            name=f"URL every one-byte input {b}",
            raw=[b],
            capacity=2,
            seed=b,
            count=1,
            badSource=False,
            badDestination=False,
            group="bytes",
        )
        for b in range(256)
    ]
    rows += [
        dict(
            name=f"URL familiar text {i}",
            raw=list(text.encode()),
            capacity=192,
            seed=i,
            count=len(text),
            badSource=False,
            badDestination=False,
            group="edges",
        )
        for i, text in enumerate(["", "f", "fo", "foo", "foob", "fooba", "foobar"])
    ]
    rows += [
        dict(
            name=f"URL unsigned count {value}",
            raw=[],
            capacity=0,
            seed=i,
            count=value,
            badSource=True,
            badDestination=True,
            group="edges",
        )
        for i, value in enumerate([65, -1, -(1 << 63)])
    ]
    rows += [
        dict(
            name="URL empty without pointer access",
            raw=[],
            capacity=0,
            seed=0,
            count=0,
            badSource=True,
            badDestination=True,
            group="edges",
        ),
        dict(
            name="URL capacity before reading source",
            raw=[1, 2, 3],
            capacity=0,
            seed=1,
            count=3,
            badSource=True,
            badDestination=True,
            group="edges",
        ),
    ]
    return rows


def outcome(trial):
    needed = encode(trial["raw"])
    count = trial["count"] & ((1 << 64) - 1)
    capacity = trial["capacity"] & ((1 << 64) - 1)
    status = (
        1 if count > 64 else 0 if count == 0 else 3 if capacity < len(needed) else 0
    )
    return status, needed if status == 0 and count > 0 else []


def construction(trial):
    seed = trial["seed"]
    so = seed % 32
    dest = (seed * 7 + 3) % 32
    source = [(i * 31 + seed * 17 + 193) & 255 for i in range(192)]
    source[so : so + len(trial["raw"])] = trial["raw"]
    output = [(i * 31 + seed * 17 + 193) & 255 for i in range(192)]
    status, result = outcome(trial)
    return dict(
        sourceOffset=so,
        destinationOffset=dest,
        source=source,
        destination=output,
        status=status,
        result=result,
    )


def memory(source, destination):
    return (
        [
            dict(label="source", bytes=bytes(source).hex()),
            dict(label="destination", bytes=bytes(destination).hex()),
        ]
        + [
            dict(label=name, bytes=word_bytes([value]))
            for name, value in CONTROL.items()
        ]
        + [
            dict(label="count", bytes=integer_bytes(4)),
            dict(label="capacity", bytes=integer_bytes(6)),
            dict(label="written", bytes=integer_bytes(-1)),
        ]
    )


def saved():
    preserved = {
        **{f"s{i}": bits(0x5566778800000000 + i * 977) for i in range(8)},
        "fp": bits(0x5566778800000000 + 8 * 977),
    }
    return {**preserved, "v0": bits(0x12345678), "v1": bits(0x23456789)}, list(
        preserved
    )


def policy(so, n, dest, size):
    return {
        "from": dict(label="source_guard"),
        "until": dict(label="caller_end", offset=4),
        "readable": [
            dict(at=dict(label="source", offset=so), length=n),
            dict(at=dict(label="destination", offset=dest), length=size),
        ],
        "writable": [dict(at=dict(label="destination", offset=dest), length=size)],
    }


def invocation(trial, so, dest, initial):
    status, result = outcome(trial)
    arguments = {"a1": bits(trial["count"]), "a3": bits(trial["capacity"])}
    addresses = {}
    for reg, label, offset, bad in [
        ("a0", "source", so, trial["badSource"]),
        ("a2", "destination", dest, trial["badDestination"]),
    ]:
        if bad:
            arguments[reg] = bits(1)
        else:
            addresses[reg] = dict(label=label, offset=offset)
    return dict(
        entry="b64url_encode",
        returnTo="function_return" if trial["seed"] % 2 == 0 else "done",
        arguments=arguments,
        argumentAddresses=addresses,
        expectedRegisters={"v0": bits(status), "v1": bits(len(result))},
        memory=initial,
        accessPolicies=[
            policy(
                so,
                len(trial["raw"]) if status == 0 and trial["count"] != 0 else 0,
                dest,
                len(result),
            )
        ],
    )


def definition(trial):
    row = construction(trial)
    initial = memory(row["source"], row["destination"])
    output = list(row["destination"])
    output[row["destinationOffset"] : row["destinationOffset"] + len(row["result"])] = (
        row["result"]
    )
    final = memory(row["source"], output)
    call = invocation(trial, row["sourceOffset"], row["destinationOffset"], final)
    registers, preserved = saved()
    return dict(
        name=trial["name"],
        instructionLimit=1000000,
        calls=[call],
        initialMemory=initial,
        initialRegisters=registers,
        initialRegisterAddresses={"sp": dict(label="workspace", offset=256)},
        preservedRegisters=preserved,
        stackAlignment=16,
        callerStackBytes=32,
        noExecution=[{"from": "function_return", "until": "done"}],
        accessPolicies=call["accessPolicies"],
    )


def caller_case():
    case = definition(trials()[0])
    case["name"] = "URL caller publishes six characters"
    source = DEFAULT + [165] * 188
    destination = [165] * 192
    initial = memory(source, destination)
    destination[:6] = encode(DEFAULT)
    before = memory(source, destination)
    after = [dict(row) for row in before]
    for row in after:
        if row["label"] == "status":
            row["bytes"] = word_bytes([0])
        if row["label"] == "written":
            row["bytes"] = integer_bytes(6)
    p = policy(0, 4, 0, 6)
    p["readable"] += [
        dict(at=dict(label=name), length=8) for name in ["count", "capacity"]
    ]
    p["writable"] += [
        dict(at=dict(label="status"), length=4),
        dict(at=dict(label="written"), length=8),
    ]
    expected = {"v0": bits(0), "v1": bits(6)}
    case.update(
        initialMemory=initial,
        accessPolicies=[p],
        calls=[
            dict(
                entry="main",
                returnTo="url_ready",
                arguments={},
                expectedRegisters=expected,
                memory=before,
                accessPolicies=[{**p, "writable": p["writable"][:1]}],
            ),
            dict(
                entry="url_ready",
                returnTo="results_ready",
                arguments={},
                expectedRegisters=expected,
                memory=after,
                accessPolicies=[{**p, "readable": [], "writable": p["writable"][1:]}],
            ),
        ],
    )
    return case


def reuse_case():
    case = definition(trials()[0])
    case["name"] = "capacity retry empty bad count and new tail reset results"
    source = [(i * 11 + 37) & 255 for i in range(192)]
    source[3:7] = DEFAULT
    source[57] = 77
    output = [(i * 19 + 107) & 255 for i in range(192)]
    initial = memory(source, output)
    selections = [
        (DEFAULT, 0, 4, 3, 7, False),
        (DEFAULT, 6, 4, 3, 7, False),
        ([], 0, 0, 101, 137, True),
        ([], 0, 65, 101, 137, True),
        ([77], 2, 1, 57, 113, False),
    ]
    calls = []
    for index, (raw, capacity, count, so, dest, bad) in enumerate(selections):
        trial = dict(
            raw=raw,
            capacity=capacity,
            count=count,
            badSource=bad,
            badDestination=bad,
            seed=index,
        )
        status, result = outcome(trial)
        output[dest : dest + len(result)] = result
        calls.append(invocation(trial, so, dest, memory(source, output)))
    case.update(
        initialMemory=initial,
        calls=calls,
        accessPolicies=[
            {
                **policy(0, 0, 0, 0),
                "readable": [
                    w for c in calls for w in c["accessPolicies"][0]["readable"]
                ],
                "writable": [
                    w for c in calls for w in c["accessPolicies"][0]["writable"]
                ],
            }
        ],
    )
    return case


def corpus():
    rows = {name: [] for name in ["short", "long", "bytes", "edges"]}
    for trial in trials():
        rows[trial["group"]].append(definition(trial))
    rows["edges"] += [caller_case(), reuse_case()]
    return rows


def profiles():
    return {
        f"{group}-{start//10:02d}": dict(
            formatVersion=10, cases=rows[start : start + 10]
        )
        for group, rows in corpus().items()
        for start in range(0, len(rows), 10)
    }


def build(directory, check=False):
    directory = Path(directory)
    files = {
        f"base64url-{name}.cases.json": json.dumps(profile, separators=(",", ":"))
        + "\n"
        for name, profile in profiles().items()
    }
    files["caller-exit.cases.json"] = (
        json.dumps(
            dict(
                formatVersion=1,
                cases=[
                    dict(
                        name="silent URL caller exit",
                        input="",
                        stdout="",
                        instructionLimit=1000000,
                    )
                ],
            ),
            indent=2,
        )
        + "\n"
    )
    if not check:
        directory.mkdir(parents=True, exist_ok=True)
    for name, text in files.items():
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale URL profile: {path}")
        else:
            path.write_text(text)
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
