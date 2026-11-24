"""Data-only public RSA checks; arbitrary-precision arithmetic supplies results.

This specifies the seven public interfaces, not an assembly implementation.
Native LaTeX remains the publication authority.
"""

import argparse
import json
import math
from pathlib import Path
from function_lesson_cases import bits

MAX32, MAX64, MAX128 = (1 << 32) - 1, (1 << 64) - 1, (1 << 128) - 1
ENTRIES = ["gcd64", "pow32", "mul128", "addmod128", "reduce128", "mulmod128", "pow128"]
ARENAS = dict(
    left=128, right=128, modulus=128, exponent=128, output=256, workspace=1024
)
OBJECTS = {k: size for k, size in ARENAS.items() if k != "workspace"} | dict(
    mode=4, status=4
)


def addr(label, offset=0):
    return dict(label=label, offset=offset)


def window(label, offset, length):
    return dict(at=addr(label, offset), length=length)


def memory(label, value, length=16, offset=0):
    raw = value.to_bytes(length, "little") if isinstance(value, int) else bytes(value)
    return dict(label=label, offset=offset, bytes=raw.hex())


def expected(target, a, b=0, n=1):
    if target == "gcd64":
        return math.gcd(a, b)
    if target == "mul128":
        return a * b
    if n == 0:
        return None
    if target in ["pow32", "pow128"]:
        return pow(a, b, n)
    if target == "addmod128":
        return (a + b) % n
    if target == "reduce128":
        return a % n
    if target == "mulmod128":
        return a * b % n
    raise ValueError(target)


def arithmetic(target, a, b=0, n=1, *, index=0, alias=0, caller=False, shared=False):
    assert target in ENTRIES and alias in range(4)
    ao, bo, no, oo = (
        (0, 0, 0, 0)
        if caller
        else (
            4 * (index % 17),
            4 * ((index + 3) % 17),
            4 * ((index + 6) % 17),
            4 * ((index + 9) % 25),
        )
    )
    second = (
        "left"
        if shared or alias == 3
        else "exponent" if target == "pow128" else "right"
    )
    so = ao if shared or alias == 3 else bo
    destination = "left" if alias in [1, 3] else "right" if alias == 2 else "output"
    do = ao if alias in [1, 3] else bo if alias == 2 else oo
    setup = [
        memory("left", a, offset=ao),
        memory("exponent" if target == "pow128" else "right", b, offset=bo),
        memory("modulus", n, offset=no),
    ]
    args, pointers = {}, {}
    if target in ["gcd64", "pow32"]:
        args = dict(a0=bits(a), a1=bits(b))
        if target == "pow32":
            args["a2"], pointers["a3"] = bits(n), addr(destination, do)
    else:
        pointers = dict(
            a0=addr("left", ao),
            a1=addr("modulus", no) if target == "reduce128" else addr(second, so),
            a2=(
                addr(destination, do)
                if target in ["mul128", "reduce128"]
                else addr("modulus", no)
            ),
        )
        if target not in ["mul128", "reduce128"]:
            pointers["a3"] = addr(destination, do)
    valid = target in ["gcd64", "mul128"] or n != 0
    reads = (
        []
        if target in ["gcd64", "pow32"]
        else (
            [window("modulus", no, 16)]
            if not valid
            else (
                [window("left", ao, 16), window(second, so, 16)]
                if target == "mul128"
                else (
                    [window("left", ao, 16), window("modulus", no, 16)]
                    if target == "reduce128"
                    else [
                        window("left", ao, 16),
                        window(second, so, 16),
                        window("modulus", no, 16),
                    ]
                )
            )
        )
    )
    count = 4 if target == "pow32" else 32 if target == "mul128" else 16
    writes = [window(destination, do, count)] if valid and target != "gcd64" else []
    result = expected(target, a, a if shared or alias == 3 else b, n)
    registers = dict(v0=bits(result if target == "gcd64" else 0 if valid else 1))
    if target == "gcd64":
        registers["v1"] = bits(0)
    call = dict(
        entry="rsa_" + target,
        returnTo="function_return",
        arguments=args,
        argumentAddresses=pointers,
        expectedRegisters=registers,
        memory=[memory(destination, result, count, do)] if writes else [],
        setupMemory=setup,
        callerAccess=dict(
            objects=[window(k, 0, size) for k, size in OBJECTS.items()],
            readable=reads + writes,
            writable=writes,
            maxAccesses=2000000,
        ),
    )
    if caller:
        dispatch = dict(
            entry="main",
            returnTo=target + "_call",
            arguments={},
            expectedRegisters={},
            expectedAddresses={"sp": addr("workspace", 1024)},
            setupMemory=setup + [memory("mode", ENTRIES.index(target), 4)],
        )
        call.update(
            entry=target + "_call",
            returnTo=target + "_return",
            arguments={},
            argumentAddresses={},
            setupMemory=[],
        )
        return [dispatch, call]
    return [call]


def invalid(target):
    args = {f"a{i}": bits(1) for i in range(4)}
    pointers, reads = {}, []
    if target == "pow32":
        args["a2"] = bits(0)
    else:
        key = "a1" if target == "reduce128" else "a2"
        del args[key]
        pointers[key], reads = addr("modulus"), [window("modulus", 0, 16)]
    return dict(
        entry="rsa_" + target,
        returnTo="function_return",
        arguments=args,
        argumentAddresses=pointers,
        expectedRegisters=dict(v0=bits(1)),
        setupMemory=[memory("modulus", 0)],
        callerAccess=dict(
            objects=[window(k, 0, size) for k, size in OBJECTS.items()],
            readable=reads,
            writable=[],
            maxAccesses=2000000,
        ),
    )


def case(name, calls, budget=100000, *, poison=False, caller_status=None):
    saved = {f"s{i}": bits(0x3141592600000000 + 977 * i) for i in range(8)} | dict(
        fp=bits(0x3141592600000000 + 977 * 8)
    )
    result = dict(
        name=name,
        calls=calls,
        instructionLimit=budget,
        initialRegisters=saved | {f"t{i}": bits(-999 - i) for i in range(10)},
        preservedRegisters=list(saved),
        callerStackBytes=0,
        stackAlignment=8,
        initialMemory=[memory(k, bytes([165]) * size) for k, size in ARENAS.items()]
        + [memory("mode", 0, 4), memory("status", 0xFFFFFFFF, 4)],
    )
    if poison:
        result["initialRegisterAddresses"] = dict(sp=addr("workspace", 1024))
    else:
        result["initialRegisters"]["sp"] = bits(0x7FFF0000)
    if caller_status is not None:
        result["exit"] = dict(code=0, memory=[memory("status", caller_status, 4)])
    return result


def groups():
    grid = {
        k: []
        for k in [
            "gcd",
            "word",
            "product",
            "add",
            "reduction",
            "modproduct",
            "power",
            "caller",
            "reuse",
        ]
    }

    def add(
        group,
        name,
        target,
        a=0,
        b=0,
        n=1,
        index=0,
        alias=0,
        budget=100000,
        shared=False,
        caller=False,
    ):
        grid[group].append(
            case(
                name,
                arithmetic(
                    target,
                    a,
                    b,
                    n,
                    index=index,
                    alias=alias,
                    shared=shared,
                    caller=caller,
                ),
                budget,
                poison=index % 2 == 1 or caller,
                caller_status=(
                    (0 if n or target in ["gcd64", "mul128"] else 1) if caller else None
                ),
            )
        )

    def bad(group, name, target):
        grid[group].append(case(name, [invalid(target)], poison=True))

    pairs = [
        (0, 0),
        (0, MAX64),
        (MAX64, 0),
        (1, MAX64),
        (3120, 17),
        (1 << 63, MAX64),
        (MAX64, MAX64),
        (48, 18),
    ]
    for i, (a, b) in enumerate(pairs):
        add("gcd", f"public gcd {i}", "gcd64", a, b, index=i)
    for n in [0, 1, 3233, MAX32]:
        for e in [0, 1, 2, 17, MAX32]:
            add(
                "word",
                f"public scalar power {n} {e}",
                "pow32",
                MAX32 - 1,
                e,
                n,
                index=e & 15,
            )
    bad("word", "scalar invalid output on zero modulus", "pow32")
    add(
        "word",
        "unsigned accumulator product keeps its high bit",
        "pow32",
        MAX32 - 4 - 60000,
        3,
        MAX32 - 4,
        index=7,
    )
    for i, a in enumerate([0, 1, MAX32, 1 << 64, 1 << 127, MAX128]):
        for b in [1, MAX128]:
            add("product", f"public full product {i} {b}", "mul128", a, b, index=i)
    add(
        "product",
        "shared full product input",
        "mul128",
        MAX128,
        MAX128,
        index=5,
        shared=True,
    )
    for i, n in enumerate([1, 17, 1 << 32, 1 << 127, MAX128 - 158]):
        for alias in range(4):
            add(
                "add",
                f"public modular addition {i} alias{alias}",
                "addmod128",
                n - 1,
                n - 1,
                n,
                index=i * 4 + alias,
                alias=alias,
            )
    add("add", "modular sum equals modulus", "addmod128", 13, 4, 17, index=7)
    bad("add", "modular addition invalid operands on zero modulus", "addmod128")
    for i, n in enumerate([1, 5, 1 << 32, 1 << 127, MAX128]):
        for j, x in enumerate([0, n - 1, MAX128]):
            add(
                "reduction",
                f"public reduction {i} {j} {x}",
                "reduce128",
                x,
                n=n,
                index=i * 3 + (x & 7),
                budget=1000000,
            )
    add("reduction", "equal input and modulus", "reduce128", MAX128, n=MAX128, index=9)
    bad("reduction", "reduction invalid value on zero modulus", "reduce128")
    for i, n in enumerate([1, 17, 1 << 127, MAX128]):
        for b in [0, 1, MAX128]:
            add(
                "modproduct",
                f"public modular product {i} {b}",
                "mulmod128",
                MAX128,
                b,
                n,
                index=i * 3,
                budget=2000000,
            )
    add(
        "modproduct",
        "shared modular product input",
        "mulmod128",
        MAX128,
        MAX128,
        3233,
        index=7,
        shared=True,
        budget=2000000,
    )
    bad("modproduct", "modular product invalid inputs on zero modulus", "mulmod128")
    for i, e in enumerate([0, 1, 17, 2753, 1 << 127, MAX128]):
        add(
            "power",
            f"public full exponent {i}",
            "pow128",
            65,
            e,
            MAX128 - 158,
            index=i,
            budget=20000000,
        )
    add("power", "toy RSA encrypt", "pow128", 65, 17, 3233, index=7, budget=20000000)
    add(
        "power", "toy RSA decrypt", "pow128", 2790, 2753, 3233, index=8, budget=20000000
    )
    add("power", "unit modulus zero exponent", "pow128", MAX128, 0, 1, index=9)
    add("power", "zero base zero exponent", "pow128", 0, 0, 17, index=10)
    bad("power", "power invalid inputs on zero modulus", "pow128")
    for target in ENTRIES:
        add(
            "caller",
            f"public caller {target}",
            target,
            13,
            11,
            17,
            caller=True,
            budget=20000000,
        )
    for target in ["pow32", "addmod128", "reduce128", "mulmod128", "pow128"]:
        add(
            "caller",
            f"public caller zero modulus {target}",
            target,
            0,
            0,
            0,
            caller=True,
        )
    grid["reuse"] = [
        case(
            "reused modular multiplier",
            arithmetic("mulmod128", MAX128, 17, 3233, index=3)
            + arithmetic("mulmod128", 19, 1 << 127, MAX128, index=8)
            + arithmetic("mulmod128", MAX128, 17, 3233, index=3),
            20000000,
            poison=True,
        ),
        case(
            "reused power with moving objects",
            arithmetic("pow128", 65, 17, 3233, index=3)
            + arithmetic("pow128", pow(65, 17, 3233), 2753, 3233, index=8)
            + arithmetic("pow128", 65, 17, 3233, index=3),
            40000000,
            poison=True,
        ),
        case(
            "error then success without a new session",
            arithmetic("pow128", 0, 0, 0, index=1)
            + arithmetic("pow128", 65, 17, 3233, index=4),
            20000000,
            poison=True,
        ),
        case(
            "interleaved independent arithmetic",
            arithmetic("mul128", MAX128, MAX128, index=3)
            + arithmetic("addmod128", MAX128 - 1, MAX128 - 1, MAX128, index=6, alias=1)
            + arithmetic("reduce128", MAX128, n=17, index=11),
            20000000,
            poison=True,
        ),
    ]
    return grid


def profiles():
    result = {}
    for group, cases in groups().items():
        batch, budget, index = [], 0, 0
        for c in cases:
            if batch and budget + c["instructionLimit"] > 40000000:
                result[f"{group}-{index:02d}.json"] = dict(
                    formatVersion=18, cases=batch
                )
                batch, budget, index = [], 0, index + 1
            batch.append(c)
            budget += c["instructionLimit"]
        if batch:
            result[f"{group}-{index:02d}.json"] = dict(formatVersion=18, cases=batch)
    return result


def build(directory, check=False):
    root = Path(directory)
    if not check:
        root.mkdir(parents=True, exist_ok=True)
    result = profiles()
    for name, data in result.items():
        path = root / name
        text = json.dumps(data, separators=(",", ":")) + "\n"
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale RSA profile: {path}")
        else:
            path.write_text(text)
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    result = build(args.directory, args.check)
    print(
        f'{len(result)} RSA profiles; {sum(len(p["cases"]) for p in result.values())} cases'
    )
