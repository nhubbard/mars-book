"""Bounded native assessments for the original public SHA-256 interface grid.

This public host model specifies results, not a student implementation. Context
workspace, reserved bytes and inactive buffer contents deliberately remain free.
Native LaTeX remains the publication authority.
"""

import argparse
import hashlib
import json
from pathlib import Path
from function_lesson_cases import bits

WORD = (1 << 32) - 1
IV = [
    0x6A09E667,
    0xBB67AE85,
    0x3C6EF372,
    0xA54FF53A,
    0x510E527F,
    0x9B05688C,
    0x1F83D9AB,
    0x5BE0CD19,
]
CONSTANTS = [
    0x428A2F98,
    0x71374491,
    0xB5C0FBCF,
    0xE9B5DBA5,
    0x3956C25B,
    0x59F111F1,
    0x923F82A4,
    0xAB1C5ED5,
    0xD807AA98,
    0x12835B01,
    0x243185BE,
    0x550C7DC3,
    0x72BE5D74,
    0x80DEB1FE,
    0x9BDC06A7,
    0xC19BF174,
    0xE49B69C1,
    0xEFBE4786,
    0x0FC19DC6,
    0x240CA1CC,
    0x2DE92C6F,
    0x4A7484AA,
    0x5CB0A9DC,
    0x76F988DA,
    0x983E5152,
    0xA831C66D,
    0xB00327C8,
    0xBF597FC7,
    0xC6E00BF3,
    0xD5A79147,
    0x06CA6351,
    0x14292967,
    0x27B70A85,
    0x2E1B2138,
    0x4D2C6DFC,
    0x53380D13,
    0x650A7354,
    0x766A0ABB,
    0x81C2C92E,
    0x92722C85,
    0xA2BFE8A1,
    0xA81A664B,
    0xC24B8B70,
    0xC76C51A3,
    0xD192E819,
    0xD6990624,
    0xF40E3585,
    0x106AA070,
    0x19A4C116,
    0x1E376C08,
    0x2748774C,
    0x34B0BCB5,
    0x391C0CB3,
    0x4ED8AA4A,
    0x5B9CCA4F,
    0x682E6FF3,
    0x748F82EE,
    0x78A5636F,
    0x84C87814,
    0x8CC70208,
    0x90BEFFFA,
    0xA4506CEB,
    0xBEF9A3F7,
    0xC67178F2,
]
ARENAS = {
    "data": 4160,
    "state": 96,
    "schedule": 320,
    "context": 832,
    "digest": 96,
    "path": 128,
    "scratch": 320,
}
OWNED = {
    **ARENAS,
    "data_end": 4,
    "length": 4,
    "mode": 4,
    "buffer_capacity": 4,
    "status": 4,
}


def rotate(x, count):
    return ((x >> count) | (x << (32 - count))) & WORD


def expanded(block):
    assert len(block) == 64
    words = [int.from_bytes(block[i : i + 4], "big") for i in range(0, 64, 4)]
    for i in range(16, 64):
        p, q = words[i - 15], words[i - 2]
        words.append(
            (
                words[i - 16]
                + (rotate(p, 7) ^ rotate(p, 18) ^ (p >> 3))
                + words[i - 7]
                + (rotate(q, 17) ^ rotate(q, 19) ^ (q >> 10))
            )
            & WORD
        )
    return words


def compressed(state, block):
    a, b, c, d, e, f, g, h = state
    for k, w in zip(CONSTANTS, expanded(block)):
        first = (
            h
            + (rotate(e, 6) ^ rotate(e, 11) ^ rotate(e, 25))
            + ((e & f) ^ ((~e) & g))
            + k
            + w
        ) & WORD
        second = (
            (rotate(a, 2) ^ rotate(a, 13) ^ rotate(a, 22))
            + ((a & b) ^ (a & c) ^ (b & c))
        ) & WORD
        a, b, c, d, e, f, g, h = (
            (first + second) & WORD,
            a,
            b,
            c,
            (d + first) & WORD,
            e,
            f,
            g,
        )
    return [(x + y) & WORD for x, y in zip(state, [a, b, c, d, e, f, g, h])]


def final_from_context(state, total, tail):
    assert len(tail) == total % 64
    padded = tail + b"\x80"
    padded += bytes((56 - len(padded)) % 64) + (
        (total << 3) & ((1 << 64) - 1)
    ).to_bytes(8, "big")
    for i in range(0, len(padded), 64):
        state = compressed(state, padded[i : i + 64])
    return b"".join(x.to_bytes(4, "big") for x in state)


def pattern(count, multiplier=73, salt=128):
    return bytes((i * multiplier + salt) & 255 for i in range(count))


def words_bytes(words):
    return b"".join((x & WORD).to_bytes(4, "little") for x in words)


def addr(label, offset=0):
    return dict(label=label, offset=offset)


def window(label, offset, length):
    return dict(at=addr(label, offset), length=length)


def memory(label, raw, offset=0):
    return dict(label=label, offset=offset, bytes=bytes(raw).hex())


def arena_input(label, raw, offset=0):
    result = bytearray([165]) * ARENAS[label]
    assert 0 <= offset <= len(result) - len(raw)
    result[offset : offset + len(raw)] = raw
    return memory(label, result)


def context_result(offset, state, total, tail=b""):
    # No reserved bytes, inactive tail or workspace values are part of this result.
    result = [
        memory(
            "context",
            words_bytes(state)
            + total.to_bytes(8, "little")
            + len(tail).to_bytes(4, "little"),
            offset,
        )
    ]
    if tail:
        result.append(memory("context", tail, offset + 48))
    return result


def context_input(offset, state, total, tail):
    return context_result(offset, state, total, tail)


def call(
    entry,
    pointers,
    reads,
    writes,
    *,
    arguments=None,
    results=None,
    setup=None,
    status=0,
):
    policies = [
        dict(
            **{"from": addr(label), "until": addr(label, size)},
            readable=reads,
            writable=writes,
            maxAccesses=200000,
        )
        for label, size in OWNED.items()
    ]
    # The whole-span policy catches even same-value stores in unnamed gaps/constants.
    policies.append(
        dict(
            **{"from": addr("data"), "until": addr("status", 4)},
            readable=[],
            writable=writes,
            checkReads=False,
            maxAccesses=200000,
        )
    )
    return dict(
        entry=entry,
        returnTo="function_return",
        arguments={k: bits(v) for k, v in (arguments or {}).items()},
        argumentAddresses=pointers,
        expectedRegisters={"v0": bits(status)},
        memory=results or [],
        setupMemory=setup or [],
        accessPolicies=policies,
        accessHistory=dict(
            regions=[window("data", 0, 4160), window("status", 0, 4)],
            maxAccesses=200000,
        ),
    )


def case(name, calls, budget=100000, fixture=None, caller_status=None):
    preserved = {
        **{f"s{i}": bits(0x456789AB00000000 + i * 509) for i in range(8)},
        "fp": bits(0x456789AB00000000 + 8 * 509),
    }
    result = dict(
        name=name,
        calls=calls,
        instructionLimit=budget,
        initialRegisters={
            **preserved,
            "sp": bits(0x7FFF0000),
            **{f"t{i}": bits(-999 - i) for i in range(10)},
        },
        preservedRegisters=list(preserved),
        callerStackBytes=0,
        stackAlignment=8,
        initialMemory=[memory(k, bytes([165]) * size) for k, size in ARENAS.items()]
        + [memory("status", b"\xff" * 4)],
        exit=dict(
            code=0,
            memory=(
                []
                if caller_status is None
                else [memory("status", words_bytes([caller_status]))]
            ),
        ),
    )
    if fixture is not None:
        result["fileRead"] = fixture
    return result


def initialize(offset):
    context = window("context", offset, 368)
    return call(
        "sha256_init",
        {"a0": addr("context", offset)},
        [context],
        [context],
        results=context_result(offset, IV, 0),
    )


def update(offset, source, count, *, result=None):
    context = window("context", offset, 368)
    return call(
        "sha256_update",
        {"a0": addr("context", offset), "a1": addr("data", source)},
        [context, window("data", source, count)],
        [context],
        arguments={"a2": count},
        results=result,
    )


def finalize(offset, out, digest):
    context, destination = window("context", offset, 368), window("digest", out, 32)
    return call(
        "sha256_final",
        {"a0": addr("context", offset), "a1": addr("digest", out)},
        [context],
        [context, destination],
        results=[memory("digest", digest, out)],
    )


def stream(name, raw, chunks, offset=3, caller=False):
    if caller:
        dispatch = dict(
            entry="main",
            returnTo="memory_call",
            arguments={},
            expectedRegisters={"sp": bits(0x7FFF0000)},
            setupMemory=[
                arena_input("data", raw),
                memory("mode", words_bytes([0])),
                memory("length", words_bytes([len(raw)])),
            ],
        )
        observed = call(
            "memory_call",
            {},
            [window("context", 0, 368), window("data", 0, len(raw))],
            [window("context", 0, 368), window("digest", 0, 32)],
            results=[
                memory("digest", hashlib.sha256(raw).digest()),
                memory("status", b"\xff" * 4),
            ],
        )
        observed["returnTo"] = "main_done"
        return case(name, [dispatch, observed], 3000000, caller_status=0)
    co, out = 8 * (offset % 8), 1 + (offset * 3) % 32
    calls = [initialize(co)]
    calls[0]["setupMemory"] = [arena_input("data", raw, offset)]
    consumed = 0
    for count in chunks:
        previous = consumed
        consumed += count
        state = list(IV)
        for i in range(0, consumed // 64 * 64, 64):
            state = compressed(state, raw[i : i + 64])
        calls.append(
            update(
                co,
                offset + previous,
                count,
                result=context_result(
                    co, state, consumed, raw[consumed // 64 * 64 : consumed]
                ),
            )
        )
    assert consumed == len(raw)
    calls.append(finalize(co, out, hashlib.sha256(raw).digest()))
    return case(name, calls, 3000000)


def file_call(path, capacity, out, scratch, expected, status, setup):
    reads = [window("path", 5, len(path) + 1), window("scratch", scratch, capacity)]
    writes = [window("scratch", scratch, capacity)] + (
        [window("digest", out, 32)] if status == 0 else []
    )
    result = call(
        "sha256_file",
        {
            "a0": addr("path", 5),
            "a1": addr("scratch", scratch),
            "a3": addr("digest", out),
        },
        reads,
        writes,
        arguments={"a2": capacity},
        results=[memory("digest", expected, out)],
        setup=setup,
        status=status,
    )
    result.update(
        readFile=True, fileReadInvocation=dict(path=path, maxRequest=capacity)
    )
    return result


def file_case(
    name, raw, capacity, maximum=1048576, failure=None, missing=False, caller=False
):
    path = f"sha-{capacity}-{len(raw)}.bin"
    po, so, out = (
        (0, 0, 0) if caller else (1 + capacity % 16, capacity % 32, 1 + capacity % 32)
    )
    status = 1 if missing else 2 if failure is not None and failure <= len(raw) else 0
    fixture = dict(
        path=path,
        bytes="" if missing else raw.hex(),
        maxRequest=capacity,
        maxReadSize=maximum,
        missing=missing,
    )
    if failure is not None:
        fixture["failAfterBytes"] = failure
    setup = [arena_input("path", path.encode("ascii") + b"\0", po)]
    observed = call(
        "sha256_file",
        {"a0": addr("path", po), "a1": addr("scratch", so), "a3": addr("digest", out)},
        [window("path", po, len(path) + 1), window("scratch", so, capacity)],
        [window("scratch", so, capacity)]
        + ([window("digest", out, 32)] if status == 0 else []),
        arguments={"a2": capacity},
        results=[
            memory(
                "digest",
                hashlib.sha256(raw).digest() if status == 0 else b"\xa5" * 32,
                out,
            )
        ],
        setup=setup,
        status=status,
    )
    observed["readFile"] = True
    calls = [observed]
    if caller:
        dispatch = dict(
            entry="main",
            returnTo="file_call",
            arguments={},
            expectedRegisters={"sp": bits(0x7FFF0000)},
            setupMemory=setup
            + [
                memory("mode", words_bytes([1])),
                memory("buffer_capacity", words_bytes([capacity])),
            ],
        )
        observed.update(
            entry="file_call",
            returnTo="main_done",
            arguments={},
            argumentAddresses={},
            setupMemory=[],
        )
        observed["memory"].append(memory("status", b"\xff" * 4))
        calls = [dispatch, observed]
    return case(
        name, calls, 100000 if missing else 3000000, fixture, status if caller else None
    )


def repeated_files(first_descriptor, failure):
    first = pattern(65 if failure is None else 1, 37, 11)
    second = pattern(129)
    fixture = dict(
        path="repeat-one.bin",
        bytes=first.hex(),
        additionalFiles={"repeat-two.bin": second.hex()},
        maxRequest=7,
        maxReadSize=2,
        firstDescriptor=first_descriptor,
    )
    if failure is not None:
        fixture["failAfterBytes"] = failure
    digest = bytearray([165]) * 96
    calls = []
    for i, path in enumerate(
        ["repeat-one.bin", "repeat-two.bin", "repeat-one.bin", "missing.bin"]
    ):
        raw = second if i == 1 else first
        out, capacity = (1 if i < 2 else 41), [7, 64, 3, 1][i]
        status = (
            1 if i == 3 else 2 if failure is not None and failure <= len(raw) else 0
        )
        if status == 0:
            digest[out : out + 32] = hashlib.sha256(raw).digest()
        calls.append(
            file_call(
                path,
                capacity,
                out,
                5,
                bytes(digest[out : out + 32]),
                status,
                [arena_input("path", path.encode("ascii") + b"\0", 5)],
            )
        )
    return case(
        f'repeated files descriptor {first_descriptor} error {"null" if failure is None else failure}',
        calls,
        3000000,
        fixture,
    )


def interleaved(seed):
    a, b = pattern(129 + seed, 37, seed), pattern(65 + seed)
    raw = bytearray([165]) * 4160
    raw[: len(a)], raw[512 : 512 + len(b)] = a, b
    calls = [
        initialize(8),
        initialize(408),
        update(8, 0, 55),
        update(408, 512, 64),
        update(8, 55, len(a) - 55),
        update(408, 576, len(b) - 64),
        finalize(8, 1, hashlib.sha256(a).digest()),
        finalize(408, 41, hashlib.sha256(b).digest()),
        initialize(8),
        update(8, 0, 0),
        finalize(8, 1, hashlib.sha256(b"").digest()),
    ]
    calls[0]["setupMemory"] = [memory("data", raw)]
    return case(f"independent and reused contexts {seed}", calls, 3000000)


def groups():
    blocks = [
        bytes(64),
        b"\xff" * 64,
        pattern(64),
        b"abc\x80" + bytes(52) + bytes.fromhex("0000000000000018"),
    ]
    kernel = []
    for i, block in enumerate(blocks):
        for offset in [0, 1, 3, 15]:
            out = 4 * ((offset + 3) % 16)
            kernel.append(
                case(
                    f"schedule {i} offset {offset}",
                    [
                        call(
                            "sha256_schedule",
                            {"a0": addr("data", offset), "a1": addr("schedule", out)},
                            [window("data", offset, 64), window("schedule", out, 256)],
                            [window("schedule", out, 256)],
                            setup=[arena_input("data", block, offset)],
                            results=[
                                memory("schedule", words_bytes(expanded(block)), out)
                            ],
                        )
                    ],
                )
            )
    for i, block in enumerate(blocks):
        for seed, state in enumerate([IV, [0] * 8, [WORD] * 8]):
            offset = 1 + i * 3
            so, wo = 4 * ((offset + 1) % 16), 4 * ((offset + 5) % 16)
            kernel.append(
                case(
                    f"compression {i} seed {seed}",
                    [
                        call(
                            "sha256_compress",
                            {
                                "a0": addr("state", so),
                                "a1": addr("data", offset),
                                "a2": addr("schedule", wo),
                            },
                            [
                                window("state", so, 32),
                                window("data", offset, 64),
                                window("schedule", wo, 256),
                            ],
                            [window("state", so, 32), window("schedule", wo, 256)],
                            setup=[
                                arena_input("data", block, offset),
                                memory("state", words_bytes(state), so),
                            ],
                            results=[
                                memory(
                                    "state", words_bytes(compressed(state, block)), so
                                )
                            ],
                        )
                    ],
                )
            )
    kernel += [case(f"initialization {co}", [initialize(co)]) for co in [0, 8, 56]]
    kernel += [
        stream(f"message {n}", pattern(n), [0, n, 0], n % 16)
        for n in [0, 1, 3, 55, 56, 63, 64, 65, 119, 120, 127, 128, 129, 4096]
    ]
    state = [(0x81234567 + i * 0x11335577) & WORD for i in range(8)]
    for total in [
        0x20000037,
        0x20000038,
        0x10000003F,
        0x100000040,
        0x1000000000000003,
        0x1FFFFFFFFFFFFFFF,
    ]:
        tail = pattern(total % 64)
        final = finalize(8, 2, final_from_context(state, total, tail))
        final["setupMemory"] = context_input(8, state, total, tail)
        kernel.append(case(f"synthetic final {total}", [final], 3000000))
    for total in [0xFFFFFFF0, 0x100000030, 0x1FFFFFFFFFFFFFB0]:
        tail, raw = pattern(total % 64), pattern(40)
        combined, expected = tail + raw, list(state)
        for i in range(0, len(combined) // 64 * 64, 64):
            expected = compressed(expected, combined[i : i + 64])
        updated = update(
            24,
            3,
            len(raw),
            result=context_result(
                24, expected, total + len(raw), combined[len(combined) // 64 * 64 :]
            ),
        )
        updated["setupMemory"] = context_input(24, state, total, tail) + [
            arena_input("data", raw, 3)
        ]
        kernel.append(case(f"synthetic update {total}", [updated], 3000000))
    file_pattern = lambda n: bytes((i * 73 + i // 3 + 128) & 255 for i in range(n))
    files = [
        file_case(f"file capacity {cap} length {n}", file_pattern(n), cap)
        for cap in [1, 7, 64, 256]
        for n in [0, 3, 56, 65, 129]
    ]
    files += [
        file_case(f"short file {cap} {short} {n}", file_pattern(n), cap, short)
        for cap in [7, 64]
        for short in [1, 3]
        for n in [63, 120]
    ]
    files += [
        file_case(f"read error {cap} at {failure}", file_pattern(65), cap, 3, failure)
        for cap in [1, 7, 256]
        for failure in [0, 55, 65]
    ]
    files += [
        file_case(f"missing file {cap}", b"", cap, missing=True) for cap in [1, 7, 256]
    ]
    program = [
        stream(f"memory caller {n}", pattern(n, 37, 11), [n], caller=True)
        for n in [0, 3, 56, 64, 65, 129]
    ]
    program += [
        file_case(f"file caller {n}", pattern(n, 37, 11), 7, 3, caller=True)
        for n in [0, 3, 56, 64, 65, 129]
    ]
    reuse = []
    for n in [3, 56, 65]:
        for split in dict.fromkeys(i for i in [0, 1, 3, 55, 56, 64, n] if i <= n):
            reuse.append(
                stream(
                    f"stream split {n} at {split}",
                    pattern(n),
                    [split, 0, n - split],
                    split % 16,
                )
            )
    reuse += [interleaved(seed) for seed in range(3)]
    reuse += [
        repeated_files(first, failure) for first in [7, 31] for failure in [None, 3]
    ]
    return dict(kernel=kernel, file=files, program=program, reuse=reuse)


def profiles():
    result = {}
    for group, cases in groups().items():
        batch, part = [], 0
        for definition in cases:
            candidate = dict(formatVersion=16, cases=batch + [definition])
            if batch and (
                sum(c["instructionLimit"] for c in candidate["cases"]) > 10000000
                or len(json.dumps(candidate, separators=(",", ":")).encode()) + 1
                > 1048576
            ):
                result[f"sha-{group}-{part:03d}.cases.json"] = dict(
                    formatVersion=16, cases=batch
                )
                batch, part = [], part + 1
            batch.append(definition)
        if batch:
            result[f"sha-{group}-{part:03d}.cases.json"] = dict(
                formatVersion=16, cases=batch
            )
    return result


def build(directory, check=False):
    directory = Path(directory)
    if not check:
        directory.mkdir(parents=True, exist_ok=True)
    result = {
        name: json.dumps(profile, separators=(",", ":")) + "\n"
        for name, profile in profiles().items()
    }
    for name, text in result.items():
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale SHA-256 profile: {path}")
        else:
            path.write_text(text)
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
