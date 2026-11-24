"""Author the original expression domains with an independent precedence-climbing oracle."""

import argparse
import json
from pathlib import Path

from console_cases import parse as console_parse, render as console_render
from function_lesson_cases import bits
from ring_lesson_cases import address, memory, words, policy

MINIMUM = -(1 << 31)
MAXIMUM = (1 << 31) - 1
PRECEDENCE = {43: 1, 45: 1, 42: 2, 47: 2}
KNOWN = (
    ("18 - 3 * (2 + -4)", (0, 24)),
    ("1+2*3", (0, 7)),
    ("(1+2)*3", (0, 9)),
    ("1-2-3", (0, -4)),
    ("8/3/2", (0, 1)),
    ("-7/3", (0, -2)),
    ("7/-3", (0, -2)),
    ("--5", (0, 5)),
    ("1++2", (0, 3)),
    ("-2147483648", (-2, 10)),
    ("(-2147483647-1)", (0, MINIMUM)),
    ("(-2147483647-1)/-1", (-2, 15)),
    ("1/(2-2)", (-3, 1)),
    ("1/(2-2", (-1, 6)),
    ("2147483647+1?", (-2, 10)),
    ("2147483647+1*?", (-1, 13)),
    ("1 2", (-1, 2)),
    ("", (-1, 0)),
)
PROGRAMS = (
    "18 - 3 * (2 + -4)",
    "1+2*3",
    "1-2-3",
    "8/3/2",
    "-7/3",
    "--5",
    "",
    " ",
    "1+",
    "(1",
    "1 2",
    "1/(2-2)",
    "1/(2-2",
    "2147483647+1?",
    "2147483647+1*?",
    "2147483648",
    "-2147483648",
    "(-2147483647-1)",
    "-(-2147483647-1)",
    "(-2147483647-1)/-1",
)
# New public recovery examples, independent of the instructor's private rotations.
REPEATED = (
    ("2*(3+4)", "5/0", "17-9"),
    ("2147483648", "7/-2", "(6+1)*2"),
    ("1+", "--6", "4 5"),
    ("((2))", "-(-2147483647-1)", "3+4*2"),
    ("1/(4-4", "1/(4-4)", "21/5"),
    (" 0 ", "1\x002", "-(8-3)"),
)


class Failure(Exception):
    def __init__(self, status, offset):
        self.status, self.offset = status, offset


def checked(value, offset):
    if not MINIMUM <= value <= MAXIMUM:
        raise Failure(-2, offset)
    return value


def divide(left, right):
    value = abs(left) // abs(right)
    return -value if (left < 0) != (right < 0) else value


class Model:
    def __init__(self, data):
        self.data, self.cursor = bytes(data), 0

    def space(self):
        while self.cursor < len(self.data) and self.data[self.cursor] in (
            9,
            10,
            13,
            32,
        ):
            self.cursor += 1

    def primary(self):
        self.space()
        if self.cursor == len(self.data):
            raise Failure(-1, self.cursor)
        if self.data[self.cursor] == 40:
            self.cursor += 1
            value = self.expression(1)
            self.space()
            if self.cursor == len(self.data) or self.data[self.cursor] != 41:
                raise Failure(-1, self.cursor)
            self.cursor += 1
            return value
        if not 48 <= self.data[self.cursor] <= 57:
            raise Failure(-1, self.cursor)
        value = 0
        while self.cursor < len(self.data) and 48 <= self.data[self.cursor] <= 57:
            value = checked(value * 10 + self.data[self.cursor] - 48, self.cursor)
            self.cursor += 1
        return value

    def unary(self):
        signs = []
        self.space()
        while self.cursor < len(self.data) and self.data[self.cursor] in (43, 45):
            signs.append((self.data[self.cursor], self.cursor))
            self.cursor += 1
            self.space()
        value = self.primary()
        for sign, offset in reversed(signs):
            if sign == 45:
                value = checked(-value, offset)
        return value

    def expression(self, minimum):
        left = self.unary()
        while True:
            self.space()
            op = self.data[self.cursor] if self.cursor < len(self.data) else None
            if PRECEDENCE.get(op, 0) < minimum:
                return left
            offset = self.cursor
            self.cursor += 1
            right = self.expression(PRECEDENCE[op] + 1)
            if op == 47 and right == 0:
                raise Failure(-3, offset)
            candidate = (
                left + right
                if op == 43
                else (
                    left - right
                    if op == 45
                    else left * right if op == 42 else divide(left, right)
                )
            )
            left = checked(candidate, offset)

    def result(self):
        try:
            value = self.expression(1)
            self.space()
            if self.cursor != len(self.data):
                raise Failure(-1, self.cursor)
            return 0, value
        except Failure as failure:
            return failure.status, failure.offset


def result(data):
    return Model(data.encode("utf-8") if isinstance(data, str) else data).result()


def inputs():
    rows = [s.encode("utf-8") for s, _ in KNOWN]
    extra = [
        "0",
        "00",
        "0" * 64,
        "2147483647",
        "2147483648",
        "99999999999",
        "2147483647+1",
        "-2147483647-2",
        "46340*46340",
        "46341*46341",
        "(-2147483647-1)*1",
        "(-2147483647-1)*-1",
        "-(-2147483647-1)",
        "1/0",
        "0/0",
        "1/(3-3)",
        "1/-0",
        "1/(-0)",
        "1/(1/0)",
        "1/0+?",
        "1/0*?",
        "1/0+2147483648",
        "2147483648/0",
        "(2147483647+1)*0",
        "1+",
        "()",
        "(1",
        "1)",
        "1**2",
        "2(3)",
        "1e2",
        "0x10",
        "1.2",
        "+",
        "-",
        " ",
        "\t\r\n",
        "1\x002",
        "é",
        "1+é",
        "1\x0b+2",
        "  - ( 2 +3) * + 4 \n",
    ]
    rows += [s.encode("utf-8") for s in extra]
    rows += [bytes([b]) for b in range(256)]
    rows += [(sign * n + "1").encode() for sign in ["-", "+"] for n in range(64)]
    rows += [("(" * n + "1" + ")" * n).encode() for n in range(32)]
    for n in range(32):
        rows += [("1+" * n + "1").encode(), ("1-" * n + "1").encode()]
    assert len(rows) == 540 and all(len(s) <= 64 for s in rows)
    return rows


def case_name(index, data):
    name = f"expression {index} " + data.hex()
    # Native names are bounded; the complete original bytes and stable trial index remain in the fixture.
    return (
        name
        if len(name) <= 128
        else f"expression {index} " + data[:48].hex() + f"... ({len(data)} bytes)"
    )


def arena(data, offset, salt):
    contents = [(i * 37 + salt * 17 + 128) & 255 for i in range(96)]
    contents[offset : offset + len(data)] = data
    return contents


def access(label, offset, length):
    return [
        policy(
            address("input_a_guard"),
            address("state_end", 4),
            readable=[dict(at=address(label, offset), length=length)],
            reads=True,
        )
    ]


def base(name, calls, stack=0x7FFF0000):
    return dict(
        name=name,
        instructionLimit=100000,
        calls=calls,
        initialRegisters={
            **{f"s{i}": bits(0x3456789000000000 + i * 1013) for i in range(8)},
            "fp": bits(0x2345678900000011),
            "sp": bits(stack),
        },
        preservedRegisters=[*[f"s{i}" for i in range(8)], "fp"],
        stackAlignment=16,
        callerStackBytes=0,
        noExecution=[dict(**{"from": "function_return"}, until="done")],
        exit=dict(
            memory=calls[-1]["memory"], accessPolicies=calls[-1]["accessPolicies"]
        ),
    )


def call(data, label, offset, contents, continuation, salt):
    status, value = result(data)
    return dict(
        entry="eval_expr",
        returnTo=continuation,
        arguments={
            **{f"t{i}": bits(0x456789AB00000000 + i * 17 + salt) for i in range(10)},
            "a1": bits(len(data)),
        },
        argumentAddresses=dict(a0=address(label, offset)),
        expectedRegisters=dict(v0=bits(status), v1=bits(value)),
        setupMemory=[memory(label, contents)],
        memory=[memory(label, contents)],
        accessPolicies=access(label, offset, len(data)),
    )


def kernel_case(index, data):
    label = "input_b" if index % 3 == 0 else "input_a"
    offset = index % 33
    return base(
        case_name(index, data),
        [
            call(
                data,
                label,
                offset,
                arena(data, offset, index),
                "function_return" if index % 2 == 0 else "done",
                index,
            )
        ],
        0x7FFE8000 if index % 2 == 0 else 0x7FFF0000,
    )


def repeated_cases():
    rows = []
    for index, texts in enumerate(REPEATED):
        calls = []
        for phase, text in enumerate(texts):
            data = text.encode()
            label = "input_a" if (index + phase) % 2 == 0 else "input_b"
            offset = (index * 7 + phase * 5) % 33
            contents = [(i * 29 + index * 13 + phase * 7 + 97) & 255 for i in range(96)]
            contents[offset : offset + len(data)] = data
            calls.append(
                call(
                    data,
                    label,
                    offset,
                    contents,
                    "function_return" if phase % 2 == 0 else "done",
                    index * 131 + phase * 31,
                )
            )
        rows.append(base(f"recovery sequence {index}", calls))
    return rows


def caller_profile():
    cases = []
    for index, text in enumerate(PROGRAMS):
        data = text.encode("utf-8")
        contents = list(data) + [128] * (96 - len(data))
        status, value = result(data)

        def state(memory_, policies, **extra):
            return dict(
                memory=memory_, addressMemory=[], accessPolicies=policies, **extra
            )

        original = [
            memory("input_a", contents),
            words("length", [len(data)]),
            words("status", [99, 99]),
        ]
        published = [
            memory("input_a", contents),
            words("length", [len(data)]),
            words("status", [status, value]),
        ]
        snapshots = [
            state(
                original,
                [
                    policy(
                        address("input_a_guard"),
                        address("state_end", 4),
                        readable=[dict(at=address("length"), length=4)],
                        reads=True,
                    )
                ],
                expectedRegisters=dict(a1=bits(len(data))),
            ),
            state(
                original,
                access("input_a", 0, len(data)),
                label="expression_ready",
                expectedRegisters=dict(v0=bits(status), v1=bits(value)),
            ),
            state(
                published,
                [
                    policy(
                        address("input_a_guard"),
                        address("state_end", 4),
                        writable=[dict(at=address("status"), length=8)],
                        reads=True,
                    )
                ],
                label="expression_stored",
                expectedRegisters=dict(v0=bits(status), v1=bits(value)),
            ),
        ]
        cases.append(
            dict(
                name=f"caller {index}",
                input="",
                stdout="",
                instructionLimit=100000,
                publication=dict(
                    label="expression_call",
                    initialMemory=[
                        memory("input_a", contents),
                        words("length", [len(data)]),
                    ],
                    snapshots=snapshots,
                    finalState=state(
                        published,
                        [
                            policy(
                                address("input_a_guard"),
                                address("state_end", 4),
                                reads=True,
                            )
                        ],
                    ),
                ),
            )
        )
    return dict(formatVersion=8, cases=cases)


def build(directory, check=False):
    directory = Path(directory)
    files = {}
    rows = [kernel_case(i, data) for i, data in enumerate(inputs())]
    for kind in ["values", "diagnostics"]:
        selected = [
            r
            for r in rows
            if (r["calls"][0]["expectedRegisters"]["v0"] == bits(0))
            == (kind == "values")
        ]
        assert len(selected) <= 512
        for number, start in enumerate(range(0, len(selected), 50)):
            files[f"{kind}-{number:02d}.cases.json"] = (
                json.dumps(
                    dict(formatVersion=23, cases=selected[start : start + 50]), indent=2
                )
                + "\n"
            )
    files["repeated.cases.json"] = (
        json.dumps(dict(formatVersion=23, cases=repeated_cases()), indent=2) + "\n"
    )
    caller = caller_profile()
    files["caller.cases.json"] = json.dumps(caller, indent=2) + "\n"
    files["caller.kts"] = console_render(console_parse(json.dumps(caller).encode()))
    for name, content in files.items():
        if len(content.encode()) > 1024 * 1024:
            raise ValueError("Oversized expression profile: " + name)
        target = directory / name
        if check:
            if target.read_text() != content:
                raise ValueError("Stale expression profile: " + name)
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
