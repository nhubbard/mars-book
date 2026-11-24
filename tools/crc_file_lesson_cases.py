"""Author the published CRC file cases with native copied read fixtures."""

import argparse
import json
from pathlib import Path
import zlib
from crc_chunk_lesson_cases import base, bits, little, window

REGIONS = {
    "data": 4160,
    "data_end": 4,
    "crc_table": 1088,
    "table_end": 4,
    "path": 128,
    "scratch": 320,
    "scratch_end": 4,
    "input_count": 4,
    "initial_state": 4,
    "exercise": 4,
    "buffer_capacity": 4,
    "method": 4,
    "raw_result": 4,
    "file_crc": 4,
    "file_status": 4,
}


def trials():
    rows = []
    for method in range(2):
        for capacity in [1, 7, 256]:
            for index, raw in enumerate([b"", bytes([0, 128, 255]), b"123456789"]):
                rows.append(
                    dict(
                        name=f"file {method} capacity {capacity} sample {index}",
                        raw=raw,
                        capacity=capacity,
                        method=method,
                        maxRead=3,
                        failAt=None,
                        missing=False,
                        limit=2000000,
                    )
                )
    for method in range(2):
        for failure in [0, 1, 9]:
            rows.append(
                dict(
                    name=f"read error {method} {failure}",
                    raw=b"123456789",
                    capacity=7,
                    method=method,
                    maxRead=3,
                    failAt=failure,
                    missing=False,
                    limit=2000000,
                )
            )
    for method in range(2):
        rows.append(
            dict(
                name=f"missing {method}",
                raw=b"",
                capacity=7,
                method=method,
                maxRead=0x7FFFFFFF,
                failAt=None,
                missing=True,
                limit=100000,
            )
        )
    return rows


def construction(trial):
    cap, method, raw = (trial[k] for k in ["capacity", "method", "raw"])
    filename = f"input-{cap}-{method}-{len(raw)}.bin"
    path_offset = 1 + (cap + method) % 16
    scratch_offset = (cap + method) % 32
    path = bytearray([165] * 128)
    name = filename.encode() + b"\0"
    path[path_offset : path_offset + len(name)] = name
    error = trial["failAt"] is not None and trial["failAt"] <= len(raw)
    return dict(
        filename=filename,
        pathOffset=path_offset,
        scratchOffset=scratch_offset,
        path=bytes(path),
        status=1 if trial["missing"] else 2 if error else 0,
        checksum=0 if trial["missing"] or error else zlib.crc32(raw),
        accepted=0 if trial["missing"] else trial["failAt"] if error else len(raw),
    )


def policies(trial, row):
    workspace = [
        window("scratch", row["scratchOffset"], trial["capacity"]),
        window("crc_table", 0, 1024),
    ]
    read = workspace + [window("path", row["pathOffset"], len(row["filename"]) + 1)]
    # The original trace restricts every write but permits unnamed padding reads.
    # The global store-only policy and per-object read policies express that
    # without inventing a fixed total data span or requiring a particular layout.
    global_policy = {
        "from": dict(label="data"),
        "until": dict(label="file_status", offset=4),
        "readable": [],
        "writable": workspace,
        "maxAccesses": 100000,
        "checkReads": False,
    }
    return [global_policy] + [
        {
            "from": dict(label=label),
            "until": dict(label=label, offset=size),
            "readable": read,
            "writable": workspace,
            "maxAccesses": 100000,
        }
        for label, size in REGIONS.items()
    ]


def definition(trial):
    row = construction(trial)
    initial = [
        dict(label="path", bytes=row["path"].hex()),
        dict(label="scratch", bytes=(bytes([165]) * 320).hex()),
        dict(label="crc_table", bytes=(little(0x6A6B6C6D) * 272).hex()),
    ]
    for label, value in [
        ("exercise", 3),
        ("method", trial["method"]),
        ("buffer_capacity", trial["capacity"]),
        ("file_crc", -1),
        ("file_status", -1),
    ]:
        initial.append(dict(label=label, bytes=little(value).hex()))
    call = dict(
        entry="crc32_file",
        returnTo="function_return",
        arguments={"a2": bits(trial["capacity"]), "a3": bits(trial["method"])},
        argumentAddresses={
            "a0": dict(label="path", offset=row["pathOffset"]),
            "a1": dict(label="scratch", offset=row["scratchOffset"]),
        },
        expectedRegisters={"v0": bits(row["checksum"]), "v1": bits(row["status"])},
        memory=[],
        readFile=True,
    )
    case = base(trial["name"], trial["limit"], initial, [call], policies(trial, row))
    fixture = dict(
        path=row["filename"],
        bytes=trial["raw"].hex(),
        maxRequest=trial["capacity"],
        maxReadSize=min(trial["maxRead"], 1048576),
        firstDescriptor=19,
        missing=trial["missing"],
    )
    if trial["failAt"] is not None:
        fixture["failAfterBytes"] = trial["failAt"]
    case["fileRead"] = fixture
    return case


def repeated_methods():
    trial = next(
        t
        for t in trials()
        if t["method"] == 0 and t["capacity"] == 7 and t["raw"] == b"123456789"
    )
    case = definition(trial)
    first = case["calls"][0]
    second = {**first, "arguments": {**first["arguments"], "a3": bits(1)}}
    case.update(
        name="bit then table reopen the same fixture independently",
        instructionLimit=3000000,
        calls=[first, second],
    )
    return case


def profiles():
    cases = [definition(t) for t in trials()] + [repeated_methods()]
    return {
        f"file-{start//4:02d}": dict(formatVersion=13, cases=cases[start : start + 4])
        for start in range(0, len(cases), 4)
    }


def build(directory, check=False):
    directory = Path(directory)
    files = {
        f"crc-{name}.cases.json": json.dumps(profile, separators=(",", ":")) + "\n"
        for name, profile in profiles().items()
    }
    if not check:
        directory.mkdir(parents=True, exist_ok=True)
    for name, text in files.items():
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale CRC file profile: {path}")
        else:
            path.write_text(text)
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
