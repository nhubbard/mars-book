"""Author the existing ChaCha20 interface grid as bounded native function data.

This host arithmetic model does not run MIPS or recognize implementation source.
Publication composition remains directly edited native LaTeX.
"""

import argparse
import json
from pathlib import Path
from function_lesson_cases import bits

WORD = (1 << 32) - 1
ARENAS = {
    "source": 4160,
    "destination": 4160,
    "state": 128,
    "quad": 96,
    "mode": 4,
    "in_place": 4,
    "length": 8,
    "status": 4,
}
KEY = bytes(range(32))
NONCE = bytes.fromhex("000000090000004a00000000")


def rotate(value, count):
    return ((value << count) | (value >> (32 - count))) & WORD


def quarter(words):
    a, b, c, d = words
    a = (a + b) & WORD
    d = rotate(d ^ a, 16)
    c = (c + d) & WORD
    b = rotate(b ^ c, 12)
    a = (a + b) & WORD
    d = rotate(d ^ a, 8)
    c = (c + d) & WORD
    b = rotate(b ^ c, 7)
    return [a, b, c, d]


def block(words):
    work = list(words)
    selections = [
        (0, 4, 8, 12),
        (1, 5, 9, 13),
        (2, 6, 10, 14),
        (3, 7, 11, 15),
        (0, 5, 10, 15),
        (1, 6, 11, 12),
        (2, 7, 8, 13),
        (3, 4, 9, 14),
    ]
    for _ in range(10):
        for at in selections:
            for i, v in zip(at, quarter([work[i] for i in at])):
                work[i] = v
    return [(x + y) & WORD for x, y in zip(words, work)]


def word_bytes(words):
    return b"".join((v & WORD).to_bytes(4, "little") for v in words)


def initial_state(counter=1):
    return (
        [0x61707865, 0x3320646E, 0x79622D32, 0x6B206574]
        + [int.from_bytes(KEY[i : i + 4], "little") for i in range(0, 32, 4)]
        + [counter]
        + [int.from_bytes(NONCE[i : i + 4], "little") for i in range(0, 12, 4)]
    )


def transform(state, raw):
    result = bytearray()
    for start in range(0, len(raw), 64):
        current = list(state)
        current[12] += start // 64
        if current[12] > WORD:
            raise ValueError("Counter exhaustion")
        result.extend(
            a ^ b for a, b in zip(raw[start : start + 64], word_bytes(block(current)))
        )
    return bytes(result)


def pattern(n, salt=129):
    return bytes((i * 73 + salt) & 255 for i in range(n))


def seed():
    memory = {k: bytearray([165]) * n for k, n in ARENAS.items()}
    for k in ["mode", "in_place", "length"]:
        memory[k][:] = bytes(ARENAS[k])
    memory["status"][:] = b"\xff" * 4
    return memory


def put(memory, label, raw, offset=0):
    assert 0 <= offset <= len(memory[label]) - len(raw)
    memory[label][offset : offset + len(raw)] = raw


def addr(label, offset=0):
    return dict(label=label, offset=offset)


def window(label, offset, length):
    return dict(at=addr(label, offset), length=length)


def snapshots(memory):
    return [dict(label=k, bytes=v.hex()) for k, v in memory.items()]


def saved():
    return {
        **{f"s{i}": bits(0x3141592600000000 + i * 977) for i in range(8)},
        "fp": bits(0x3141592600000000 + 8 * 977),
    }


def base_call(entry, args, pointers, reads, writes, before, after, status=0):
    return dict(
        entry=entry,
        returnTo="function_return",
        arguments={k: bits(v) for k, v in args.items()},
        argumentAddresses=pointers,
        expectedRegisters={"v0": bits(status)},
        setupMemory=snapshots(before),
        memory=snapshots(after),
        accessPolicies=[
            dict(
                **{"from": addr(k), "until": addr(k, n)},
                readable=reads,
                writable=writes,
                maxAccesses=250000,
            )
            for k, n in ARENAS.items()
        ],
        accessHistory=dict(
            regions=[window("source", 0, 4160), window("status", 0, 4)],
            maxAccesses=250000,
        ),
    )


def invocation(memory, spec, caller=False):
    kind = spec["kind"]
    args = {}
    pointers = {}
    status = 0
    if kind == "quarter":
        entry = "chacha_quarter"
        positions = spec["positions"]
        words = spec["words"]
        for i, (at, value) in enumerate(zip(positions, words)):
            put(memory, "quad", word_bytes([value]), at)
            pointers[f"a{i}"] = addr("quad", at)
        reads = writes = [window("quad", at, 4) for at in positions]
        before = {k: bytes(v) for k, v in memory.items()}
        for at, value in zip(positions, quarter(words)):
            put(memory, "quad", word_bytes([value]), at)
    elif kind == "block":
        entry = spec["entry"]
        s = spec["stateOffset"]
        d = spec["outOffset"]
        words = spec["words"]
        put(memory, "state", word_bytes(words), s)
        pointers = {"a0": addr("state", s), "a1": addr("destination", d)}
        reads = [window("state", s, 64), window("destination", d, 64)]
        writes = [window("destination", d, 64)]
        before = {k: bytes(v) for k, v in memory.items()}
        put(memory, "destination", word_bytes(block(words)), d)
    else:
        entry = "chacha_xor"
        count = spec["count"]
        args = {"a3": count}
        s = spec.get("stateOffset", 12)
        src = spec.get("sourceOffset", 3)
        out = spec.get("outOffset", 7)
        in_place = spec.get("inPlace", False)
        state = spec.get("words", initial_state())
        raw = spec.get("input", pattern(count) if 0 <= count <= 4096 else b"")
        invalid = spec.get("invalidPointers", False)
        if not invalid:
            put(memory, "state", word_bytes(state), s)
            memory["source"][:] = bytes([165]) * 4160
            put(memory, "source", raw, src)
            pointers = {
                "a0": addr("state", s),
                "a1": addr("source", src),
                "a2": addr("source", src) if in_place else addr("destination", out),
            }
        else:
            args.update(a0=1, a1=1, a2=1)
        status = (
            1
            if not 0 <= count <= 4096
            else 2 if count and state[12] + (count + 63) // 64 - 1 > WORD else 0
        )
        reads = [] if status == 1 or count == 0 else [window("state", s, 64)]
        dst = "source" if in_place else "destination"
        at = src if in_place else out
        if status == 0 and count:
            reads += [window("source", src, count), window(dst, at, count)]
        writes = [window(dst, at, count)] if status == 0 and count else []
        before = {k: bytes(v) for k, v in memory.items()}
        if status == 0 and count:
            put(memory, dst, transform(state, raw[:count]), at)
    call = base_call(entry, args, pointers, reads, writes, before, memory, status)
    if not caller:
        return [call]
    mode = {
        "chacha_quarter": 0,
        "chacha_block_scalar": 1,
        "chacha_block_msa": 2,
        "chacha_xor": 3,
    }[entry]
    controls = {"mode": word_bytes([mode])}
    if kind == "xor":
        controls.update(
            in_place=word_bytes([int(in_place)]),
            length=(count & ((1 << 64) - 1)).to_bytes(8, "little"),
        )
    for k, v in controls.items():
        before[k] = v
        memory[k][:] = v
    call["memory"] = snapshots(memory)
    call.pop("setupMemory")
    call["returnTo"] = "main_done"
    call["entry"] = {
        0: "quarter_call",
        1: "scalar_call",
        2: "msa_call",
        3: "stream_call",
    }[mode]
    # The real main dispatch runs before its function-call observation window.
    # Its setup is not hidden inside an expanded callee permission policy.
    dispatch = dict(
        entry="main",
        returnTo=call["entry"],
        arguments={},
        expectedRegisters={"sp": bits(0x7FFF0000)},
        setupMemory=snapshots(before),
    )
    return [dispatch, call]


def case(name, specs, budget=100000, caller=False):
    memory = seed()
    calls = []
    for spec in specs:
        calls.extend(invocation(memory, spec, caller))
    saved_regs = saved()
    result = dict(
        name=name,
        instructionLimit=budget,
        calls=calls,
        initialRegisters={
            **saved_regs,
            "sp": bits(0x7FFF0000),
            **{f"t{i}": bits(-999 - i) for i in range(10)},
        },
        preservedRegisters=list(saved_regs),
        callerStackBytes=0,
        stackAlignment=8,
    )
    if caller:
        result["exit"] = dict(
            code=0,
            memory=[
                dict(
                    label="status",
                    bytes=int(calls[-1]["expectedRegisters"]["v0"], 16)
                    .to_bytes(4, "little")
                    .hex(),
                )
            ],
        )
    return result


def quarter_spec(words, positions):
    return dict(kind="quarter", words=words, positions=positions)


def block_spec(words, entry, s, d):
    return dict(kind="block", words=words, entry=entry, stateOffset=s, outOffset=d)


def xor_spec(count, in_place=False, s=12, src=3, out=7, counter=1, salt=129):
    return dict(
        kind="xor",
        count=count,
        inPlace=in_place,
        stateOffset=s,
        sourceOffset=src,
        outOffset=out,
        words=initial_state(counter),
        input=pattern(count, salt) if 0 <= count <= 4096 else b"",
    )


def groups():
    qinputs = [
        [0x11111111, 0x01020304, 0x9B8D6F43, 0x01234567],
        [0] * 4,
        [WORD] * 4,
        [0x80000000, 1, 0x7FFFFFFF, 0xABCDEF01],
    ]
    layouts = [[0, 20, 40, 60], [80, 4, 56, 28], [12, 72, 0, 92]]
    result = {
        "quarter": [
            case(f"quarter {i} layout {j}", [quarter_spec(words, positions)])
            for i, words in enumerate(qinputs)
            for j, positions in enumerate(layouts)
        ]
    }
    inputs = [
        initial_state(),
        [0] * 16,
        [WORD] * 16,
        [(i * 0x1234567 + 0x80000000) & WORD for i in range(16)],
    ]
    for kind in ["scalar", "msa"]:
        entry = "chacha_block_" + kind
        result[kind] = [
            case(f"{entry} state {i}", [block_spec(words, entry, 4 * i, 4 + 12 * i)])
            for i, words in enumerate(inputs)
        ]
    result["xor"] = [
        case(
            f"XOR {n} in-place {str(ip).lower()}",
            [xor_spec(n, ip, 4 * (n % 13), n % 15, 17 + n % 19)],
            1000000,
        )
        for n in [0, 1, 15, 63, 64, 65, 128, 129, 4096]
        for ip in [False, True]
    ]
    result["xor"] += [
        case(
            f"counter {counter} count {n}",
            [xor_spec(n, False, 20, 5, 13, counter)],
            1000000,
        )
        for counter in [0, 0x7FFFFFFF, 0x80000000, 0xFFFFFFFE, WORD]
        for n in [63, 64, 65]
    ]
    result["xor"] += [
        case(
            f"invalid count {n} before pointers",
            [dict(kind="xor", count=n, invalidPointers=True)],
        )
        for n in [4097, 0x100000000, -1]
    ]
    result["xor"] += [
        case("empty before pointers", [dict(kind="xor", count=0, invalidPointers=True)])
    ]
    result["caller"] = [
        case(
            "quarter supplied caller",
            [quarter_spec(qinputs[0], layouts[0])],
            caller=True,
        )
    ]
    result["caller"] += [
        case(
            entry + " supplied caller",
            [block_spec(initial_state(), entry, 0, 0)],
            caller=True,
        )
        for entry in ["chacha_block_scalar", "chacha_block_msa"]
    ]
    result["caller"] += [
        case(f"XOR caller {n}", [xor_spec(n, False, 0, 0, 0)], 1000000, True)
        for n in [0, 1, 64, 65, 4096]
    ]
    result["caller"] += [
        case("in-place caller", [xor_spec(65, True, 0, 0, 0)], 1000000, True),
        case(
            "caller counter exhaustion",
            [xor_spec(65, False, 0, 0, 0, WORD)],
            1000000,
            True,
        ),
        case("caller count rejection", [xor_spec(4097, False, 0, 0, 0)], caller=True),
    ]
    result["reuse"] = []
    for entry in ["chacha_block_scalar", "chacha_block_msa"]:
        other = [(i * 0x19876543 + 0x81234567) & WORD for i in range(16)]
        result["reuse"].append(
            case(
                "repeated " + entry + " with two inputs",
                [
                    block_spec(initial_state(), entry, 0, 0),
                    block_spec(other, entry, 64, 80),
                    block_spec(initial_state(), entry, 0, 160),
                ],
                1000000,
            )
        )
    result["reuse"].append(
        case(
            "repeated quarter arguments",
            [
                quarter_spec([1, 2, 3, 4], [92, 4, 68, 24]),
                quarter_spec([WORD, 0, 0x80000000, 7], [0, 88, 44, 12]),
            ],
        )
    )
    for ip in [False, True]:
        specs = [
            xor_spec(
                n,
                ip,
                0 if i % 2 == 0 else 64,
                1 + i * 3,
                i * 256 + 7,
                counter,
                31 + i * 47,
            )
            for i, (counter, n) in enumerate(zip([1, 7, 1, WORD], [65, 0, 129, 64]))
        ]
        result["reuse"].append(
            case("repeated XOR state " + str(ip).lower(), specs, 1000000)
        )
    return result


def profiles():
    result = {}
    for group, cases in groups().items():
        batch = []
        part = 0
        for c in cases:
            candidate = dict(formatVersion=15, cases=batch + [c])
            if batch and (
                sum(v["instructionLimit"] for v in candidate["cases"]) > 10000000
                or len(json.dumps(candidate, separators=(",", ":")).encode()) + 1
                > 1024 * 1024
            ):
                result[f"chacha-{group}-{part:03d}.cases.json"] = dict(
                    formatVersion=15, cases=batch
                )
                batch = []
                part += 1
            batch.append(c)
        if batch:
            result[f"chacha-{group}-{part:03d}.cases.json"] = dict(
                formatVersion=15, cases=batch
            )
    return result


def build(directory, check=False):
    directory = Path(directory)
    if not check:
        directory.mkdir(parents=True, exist_ok=True)
    result = {
        name: json.dumps(value, separators=(",", ":")) + "\n"
        for name, value in profiles().items()
    }
    for name, text in result.items():
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale ChaCha20 profile: {path}")
        else:
            path.write_text(text)
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
