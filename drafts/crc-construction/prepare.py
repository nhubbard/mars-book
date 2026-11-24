#!/usr/bin/env python3
"""Prepare CRC observation sources without overwriting cumulative student work."""

import argparse, json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def words(kind, values):
    return "".join(
        "    ."
        + kind
        + " "
        + ",".join(
            str(v if kind != "byte" or v < 128 else v - 256) for v in values[i : i + 8]
        )
        + "\n"
        for i in range(0, len(values), 8)
    )


def crc_table():
    values = []
    for index in range(256):
        state = index
        for _ in range(8):
            state = (state >> 1) ^ (0xEDB88320 if state & 1 else 0)
        values.append(state)
    return values


def prepare(source, recipe, fixture_path=None):
    target = recipe["target"]
    data = recipe["input"]
    seed = recipe["seed"]
    capacity = recipe["capacity"]
    method = recipe["method"]
    if (
        target not in ["bit", "init", "table", "file"]
        or not isinstance(seed, int)
        or not 0 <= seed <= 0xFFFFFFFF
        or not isinstance(capacity, int)
        or not 1 <= capacity <= 256
        or method not in [0, 1]
    ):
        raise ValueError("Use a declared CRC target, raw seed, capacity and method")
    if len(data) > 4096 or any(
        not isinstance(v, int) or not 0 <= v <= 255 for v in data
    ):
        raise ValueError("Input must contain at most 4096 octets")
    values = data + [165] * (4160 - len(data))
    entries = (
        crc_table() + [0x6A6B6C6D] * 16 if target == "table" else [0x6A6B6C6D] * 272
    )
    for start, end, kind, items in [
        ("data:", "data_end:", "byte", values),
        ("crc_table:", "table_end:", "word", entries),
    ]:
        if source.count(start) != 1 or source.count(end) != 1:
            raise ValueError("Retain complete supplied arenas")
        a = source.index(start) + len(start)
        b = source.index(end, a)
        source = source[:a] + "\n" + words(kind, items) + source[b:]
    defaults = {
        "input_count": "9",
        "initial_state": "0xffffffff",
        "exercise": "0",
        "buffer_capacity": "7",
        "method": "0",
    }
    selected = {
        "input_count": str(len(data)),
        "initial_state": hex(seed),
        "exercise": str(["bit", "init", "table", "file"].index(target)),
        "buffer_capacity": str(capacity),
        "method": str(method),
    }
    for name, value in defaults.items():
        old = name + ": .word " + value + "\n"
        if source.count(old) != 1:
            raise ValueError("Retain default " + name + " in cumulative source")
        source = source.replace(old, name + ": .word " + selected[name] + "\n", 1)
    if fixture_path is not None:
        if target != "file":
            raise ValueError(
                "A host file fixture is only meaningful for file observations"
            )
        encoded = str(fixture_path.resolve()).encode("ascii")
        if len(encoded) > 127 or b"\0" in encoded:
            raise ValueError("Use an ASCII fixture path of at most 127 bytes")
        a = source.index("path:") + len("path:")
        b = source.index(".align 6\nscratch:", a)
        source = (
            source[:a]
            + "\n"
            + words("byte", list(encoded) + [0] + [0] * (127 - len(encoded)))
            + source[b:]
        )
    return source


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("recipe")
    parser.add_argument("--source", type=Path, default=ROOT / "main.asm")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--fixture-output", type=Path)
    args = parser.parse_args()
    candidates = [
        q
        for q in json.loads((ROOT / "inputs.json").read_text())["recipes"]
        if q["id"] == args.recipe
    ]
    if len(candidates) != 1:
        parser.error("Choose an exact recipe ID")
    recipe = candidates[0]
    targets = [args.output] + ([args.fixture_output] if args.fixture_output else [])
    if len({path.resolve() for path in targets}) != len(targets) or any(
        path.resolve() == args.source.resolve() for path in targets
    ):
        parser.error("Source and output paths must be distinct")
    if any(path.exists() for path in targets):
        parser.error("Output files must not already exist")
    try:
        text = prepare(args.source.read_text(), recipe, args.fixture_output)
    except (ValueError, UnicodeError) as failure:
        parser.error(str(failure))
    created = []
    try:
        with args.output.open("x") as stream:
            created.append(args.output)
            stream.write(text)
        if args.fixture_output:
            with args.fixture_output.open("xb") as stream:
                created.append(args.fixture_output)
                stream.write(bytes(recipe["input"]))
    except BaseException:
        for path in created:
            path.unlink()
        raise
    print(args.output)
    if args.fixture_output:
        print(args.fixture_output)
    if recipe["target"] == "file":
        print(
            "Forced short reads, missing-file cases and injected errors are CLI fixture controls; the emitted host file contains only the recipe bytes."
        )


if __name__ == "__main__":
    main()
