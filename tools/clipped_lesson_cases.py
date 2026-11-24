"""Retain the original clipped-blitter trials as bounded native data; no prose generation."""

import argparse
from dataclasses import dataclass
import json
from pathlib import Path

from console_cases import parse as console_parse, render as console_render
from function_lesson_cases import bits
from ring_lesson_cases import address, memory, words, windows, policy


@dataclass(frozen=True)
class Shape:
    width: int
    height: int
    stride: int

    def __str__(self):
        return f"Shape(width={self.width}, height={self.height}, stride={self.stride})"


@dataclass(frozen=True)
class Trial:
    frame: Shape
    sprite: Shape
    x: int
    y: int
    key: int
    salt: int
    identical: bool = False


SHAPES = tuple(
    Shape(*s)
    for s in [
        (0, 0, 0),
        (0, 8, 0),
        (8, 0, 16),
        (1, 1, 1),
        (3, 5, 7),
        (5, 4, 7),
        (8, 6, 16),
        (8, 8, 8),
    ]
)
KEYS = (0, -1, 0x00FF00FF, 0x01FF00FF, -(1 << 31))
POSITIONS = (
    (-2, 1),
    (0, 0),
    (6, -2),
    (-1, 4),
    (8, 0),
    (0, 6),
    (-5, 0),
    (0, -4),
    (-(1 << 31), (1 << 31) - 1),
    ((1 << 31) - 1, -(1 << 31)),
)
PALETTE = (
    0,
    0x00FF00FF,
    0x01FF00FF,
    0x00FF0000,
    0x0000FF00,
    0x000000FF,
    1 << 31,
    0xFFFFFFFF,
)


def trials():
    rows = []
    for frame in SHAPES:
        for sprite in SHAPES:
            positions = [
                (-sprite.width, -sprite.height),
                (-1, -1),
                (0, 0),
                (frame.width - 1, frame.height - 1),
                (frame.width, frame.height),
                (-(1 << 31), (1 << 31) - 1),
                ((1 << 31) - 1, -(1 << 31)),
                (1, -1),
                (-1, 1),
            ]
            for x, y in positions:
                index = len(rows)
                rows.append(Trial(frame, sprite, x, y, KEYS[index % len(KEYS)], index))
    for shape in SHAPES:
        if shape.width and shape.height:
            rows.append(Trial(shape, shape, 0, 0, 0x00FF00FF, 3, True))
    return rows


def intersection(frame, sprite, x, y):
    left, top = max(0, x), max(0, y)
    right, bottom = min(frame.width, x + sprite.width), min(
        frame.height, y + sprite.height
    )
    return (left, top, right, bottom) if left < right and top < bottom else (0, 0, 0, 0)


def cells(shape, offset=0):
    return [
        offset + row * shape.stride + column
        for row in range(shape.height)
        for column in range(shape.width)
    ]


def byte_windows(label, indexes):
    return windows(label, [index * 4 + byte for index in indexes for byte in range(4)])


def copy_cells(
    frame, sprite, x, y, key, source, output, source_offset=0, frame_offset=0
):
    # Intersect half-open destination intervals independently of the historical per-source-cell loop.
    left, top, right, bottom = intersection(frame, sprite, x, y)
    written = []
    result = output.copy()
    visible = (right - left) * (bottom - top)
    for row in range(top, bottom):
        for column in range(left, right):
            value = source[source_offset + (row - y) * sprite.stride + column - x]
            if value & 0xFFFFFFFF != key & 0xFFFFFFFF:
                index = frame_offset + row * frame.stride + column
                result[index] = value
                written.append(index)
    return result, len(written), visible, written


def kernel_case(index, trial):
    t = trial
    frame = "frame_b" if index % 3 == 0 else "frame_a"
    source = "sprite_b" if index % 4 == 0 else "sprite_a"
    fd = "frame_desc_a" if index % 2 == 0 else "frame_desc_b"
    sd = "sprite_desc_b" if index % 3 == 0 else "sprite_desc_a"
    frame_offset = index % 8
    source_offset = index * 3 % 8
    input_ = [
        (0x55667788 ^ (i * 0x010103 + t.salt * 17)) & 0xFFFFFFFF for i in range(128)
    ]
    output = [
        (0x20304050 ^ (i * 0x010103 + t.salt * 31)) & 0xFFFFFFFF for i in range(128)
    ]
    for row in range(t.sprite.height):
        for column in range(t.sprite.width):
            value = (
                t.key
                if t.salt % 4 == 0
                else (
                    t.key ^ 0x01000000
                    if t.salt % 4 == 1
                    else (
                        (t.key if (column + row) % 2 == 0 else t.key ^ 0x01000000)
                        if t.salt % 4 == 2
                        else PALETTE[(column + row * 3 + t.salt) % len(PALETTE)]
                    )
                )
            )
            input_[source_offset + row * t.sprite.stride + column] = value & 0xFFFFFFFF
    if t.identical:
        left, top, right, bottom = intersection(t.frame, t.sprite, t.x, t.y)
        for row in range(top, bottom):
            for column in range(left, right):
                output[frame_offset + row * t.frame.stride + column] = input_[
                    source_offset + (row - t.y) * t.sprite.stride + column - t.x
                ]
    expected, copied, visible, written = copy_cells(
        t.frame, t.sprite, t.x, t.y, t.key, input_, output, source_offset, frame_offset
    )
    pointers = [
        dict(at=address(fd), value=address(frame, frame_offset * 4)),
        dict(at=address(sd), value=address(source, source_offset * 4)),
    ]
    dimensions = [
        words(fd, [t.frame.width, t.frame.height, t.frame.stride]) | dict(offset=8),
        words(sd, [t.sprite.width, t.sprite.height, t.sprite.stride, t.key])
        | dict(offset=8),
    ]
    initial = [words(source, input_), words(frame, output), *dimensions]
    memory_ = [words(source, input_), words(frame, expected), *dimensions]
    reads = (
        byte_windows(source, cells(t.sprite, source_offset))
        + byte_windows(frame, cells(t.frame, frame_offset))
        + [dict(at=address(fd), length=20), dict(at=address(sd), length=24)]
    )
    policies = [
        policy(
            address("bitmap_origin"),
            address("state_end", 4),
            writable=byte_windows(frame, written),
            readable=reads,
            reads=True,
        )
    ]
    call = dict(
        entry="blit_sprite",
        returnTo="function_return" if index % 2 == 0 else "done",
        arguments={
            **{f"t{i}": bits(0x456789AB00000000 + i * 17 + index) for i in range(10)},
            "a2": bits(t.x),
            "a3": bits(t.y),
        },
        argumentAddresses=dict(a0=address(fd), a1=address(sd)),
        expectedRegisters=dict(v0=bits(copied), v1=bits(visible)),
        memory=memory_,
        addressMemory=pointers,
        setupAddressMemory=pointers,
        accessPolicies=policies,
    )
    return dict(
        name=f"blit {index} {t.frame} {t.sprite} {t.x}/{t.y}",
        instructionLimit=50000,
        calls=[call],
        initialMemory=initial,
        initialRegisters={
            **{f"s{i}": bits(0x3456789000000000 + i * 1013) for i in range(8)},
            "fp": bits(0x2345678900000011),
            "sp": bits(0x7FFF0000),
        },
        preservedRegisters=[*[f"s{i}" for i in range(8)], "fp"],
        stackAlignment=16,
        callerStackBytes=0,
        noExecution=[dict(**{"from": "function_return"}, until="done")],
        exit=dict(memory=memory_, accessPolicies=policies),
    )


def default_source():
    rows = [
        (0x00FF00FF, 0x00FF0000, 0x00FF00FF, 0x0000FF00, 0x00FF00FF),
        (0x000000FF, 0, 0x00FFFF00, 0x01FF00FF, 0x00FF0000),
        (0x00FF00FF, 0x00FFFFFF, 0x0000FFFF, 0x00FF00FF, 0x0000FF00),
        (0x00FF0000, 0x00FF00FF, 0x000000FF, 0x00FFFF00, 0x00FF00FF),
    ]
    return [value for row in rows for value in (*row, 0x00111111, 0x00111111)] + [
        0
    ] * 100


def caller_profile():
    cases = []
    frame_shape = Shape(8, 6, 16)
    sprite_shape = Shape(5, 4, 7)
    source = default_source()
    pointers = [
        dict(at=address("frame_desc_a"), value=address("frame_a")),
        dict(at=address("sprite_desc_a"), value=address("sprite_a")),
    ]
    for index, (x, y) in enumerate(POSITIONS):
        initial = [
            0x00102030 if i // 16 < 6 and i % 16 < 8 else 0x00444444 for i in range(128)
        ]
        output, copied, visible, written = copy_cells(
            frame_shape, sprite_shape, x, y, 0x00FF00FF, source, initial
        )

        def state(frame, policies_, **extra):
            return dict(
                memory=[words("frame_a", frame), words("sprite_a", source)],
                addressMemory=pointers,
                accessPolicies=policies_,
                **extra,
            )

        init_policy = policy(
            address("bitmap_origin"),
            address("state_end", 4),
            writable=[
                dict(at=address("frame_a"), length=512),
                dict(at=address("frame_desc_a"), length=8),
                dict(at=address("sprite_desc_a"), length=8),
            ],
        )
        copy_policy = policy(
            address("bitmap_origin"),
            address("state_end", 4),
            writable=byte_windows("frame_a", written),
        )
        # Initialization and returned registers are distinct boundaries in one ordinary main execution.
        snapshots = [
            state(initial, [init_policy]),
            state(
                output,
                [copy_policy],
                label="blit_ready",
                expectedRegisters=dict(v0=bits(copied), v1=bits(visible)),
            ),
        ]
        final = state(
            output,
            [
                policy(
                    address("bitmap_origin"),
                    address("state_end", 4),
                    writable=[dict(at=address("copied"), length=8)],
                )
            ],
        )
        final["memory"] += [words("copied", [copied, visible])]
        cases.append(
            dict(
                name=f"program {index} {x}/{y}",
                input="",
                stdout="",
                instructionLimit=50000,
                publication=dict(
                    label="blit_call",
                    initialMemory=[words("position", [x, y])],
                    snapshots=snapshots,
                    finalState=final,
                ),
            )
        )
    return dict(formatVersion=8, cases=cases)


def build(directory, check=False):
    directory = Path(directory)
    rows = [kernel_case(i, t) for i, t in enumerate(trials())]
    assert len(rows) == 581
    files = {}
    # A geometry-first assessment can pass while the successful copy path is still unfinished.
    for kind in ["empty", "overlap"]:
        selected = [
            row
            for row in rows
            if (int(row["calls"][0]["expectedRegisters"]["v1"], 16) == 0)
            == (kind == "empty")
        ]
        assert len(selected) <= 512
        for number, start in enumerate(range(0, len(selected), 50)):
            files[f"{kind}-{number:02d}.cases.json"] = (
                json.dumps(
                    dict(formatVersion=23, cases=selected[start : start + 50]), indent=2
                )
                + "\n"
            )
    caller = caller_profile()
    files["caller.cases.json"] = json.dumps(caller, indent=2) + "\n"
    files["caller.kts"] = console_render(console_parse(json.dumps(caller).encode()))
    for name, content in files.items():
        if len(content.encode()) > 1024 * 1024:
            raise ValueError("Oversized native profile: " + name)
        target = directory / name
        if check:
            if target.read_text() != content:
                raise ValueError("Stale clipped profile: " + name)
        else:
            directory.mkdir(parents=True, exist_ok=True)
            target.write_text(content)
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-directory", type=Path, required=True)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.output_directory, args.check)
