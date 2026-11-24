"""Generate the complete original interpolation corpus as bounded native cases."""

from kotlin_format import formatted_kotlin
import argparse
import json
from pathlib import Path

from console_cases import kotlin_string
from function_lesson_cases import bits, saved_values
from partition_lesson_cases import word_bytes

EDGES = (
    -2147483648,
    -2147483647,
    -1025,
    -257,
    -256,
    -129,
    -128,
    -1,
    0,
    1,
    127,
    128,
    255,
    256,
    511,
    1024,
    2147483646,
    2147483647,
)
WEIGHTS = (0, 1, 63, 64, 127, 128, 129, 192, 255, 256)
LABELS = ("left_raw", "right_raw", "weight", "input_guard", "answer", "answer_guard")


def samples():
    data = json.loads(
        Path(__file__).with_name("fixed_interpolation_samples.json").read_text()
    )
    assert data["formatVersion"] == 1 and len(data["samples"]) == 128
    return [tuple(row) for row in data["samples"]]


def trials():
    return [(a, b, w) for a in EDGES for b in EDGES for w in WEIGHTS] + samples()


def interpolate(a, b, w):
    return ((256 - w) * a + w * b + 128) // 256


def initial(number, caller=False):
    values = (
        (-256, 512, 64, 0x13579BDF, -1, 0x2468ACE0)
        if caller
        else (
            0x12340000 + number,
            -0x23450000 - number,
            17 + number,
            0x13579BDF ^ number,
            0x6A000000 | number,
            0x2468ACE0 ^ number,
        )
    )
    return [
        dict(label=label, bytes=word_bytes([value]))
        for label, value in zip(LABELS, values)
    ]


def profile():
    corpus = trials()
    cases = []
    for number, offset in enumerate(range(0, len(corpus), 512)):
        kept = saved_values(0x1122334400000000 + number * 0x01010101)
        calls = [
            dict(
                entry="interpolate_raw",
                returnTo="interpolate_done" if index % 2 == 0 else "interpolate_exit",
                arguments={"a0": bits(a), "a1": bits(b), "a2": bits(w)},
                expectedRegisters={"v0": bits(interpolate(a, b, w))},
            )
            for index, (a, b, w) in enumerate(corpus[offset : offset + 512], offset)
        ]
        cases.append(
            dict(
                name=f"raw interpolation calls {offset+1} through {offset+len(calls)}",
                instructionLimit=100000,
                calls=calls,
                initialRegisters={
                    **{r: bits(v) for r, v in kept.items()},
                    "sp": bits(0x7FFFEFFC - number * 4096),
                },
                preservedRegisters=list(kept),
                stackAlignment=16,
                callerStackBytes=32,
                initialMemory=initial(number),
                noStores=[dict(label=label, length=4) for label in LABELS],
                protectCallerStack=True,
            )
        )
    setup = initial(7, caller=True)
    expected = [dict(item) for item in setup]
    next(item for item in expected if item["label"] == "answer")["bytes"] = word_bytes(
        [-64]
    )
    cases.append(
        dict(
            name="complete interpolation caller result",
            instructionLimit=1000,
            calls=[
                dict(
                    entry="main",
                    returnTo="interpolate_exit",
                    arguments={},
                    expectedRegisters={"v0": bits(-64)},
                    memory=expected,
                )
            ],
            preservedRegisters=list(saved_values(0)),
            stackAlignment=16,
            callerStackBytes=32,
            initialMemory=setup,
            noStores=[
                dict(label=label, length=4) for label in LABELS if label != "answer"
            ],
            protectCallerStack=True,
        )
    )
    return dict(formatVersion=4, cases=cases)


@formatted_kotlin
def script():
    lines = [
        "// Full original public interpolation corpus, with frozen Kotlin Random samples.\n",
        "val edges=listOf(Int.MIN_VALUE,Int.MIN_VALUE+1,-1025,-257,-256,-129,-128,-1,0,1,127,128,255,256,511,1024,Int.MAX_VALUE-1,Int.MAX_VALUE)\n",
        "val weights=listOf(0,1,63,64,127,128,129,192,255,256)\n",
        "val samples: List<Triple<Int,Int,Int>> = listOf(\n",
    ]
    for a, b, w in samples():
        lines.append(f"    Triple({a},{b},{w}),\n")
    lines.append(
        ")\nval trials=edges.flatMap { a -> edges.flatMap { b -> weights.map { w -> Triple(a,b,w) } } }+samples\n"
    )
    for number, case in enumerate(profile()["cases"][:-1]):
        lines.append(
            f'case({kotlin_string(case["name"])},instructionLimit=100000) {{\n'
        )
        for item in case["initialMemory"]:
            values = ", ".join(str(b) for b in bytes.fromhex(item["bytes"]))
            lines.append(f'    setBytes({kotlin_string(item["label"])}, {values})\n')
        for reg, value in case["initialRegisters"].items():
            lines.append(f"    setRegister({kotlin_string(reg)}, {value}uL.toLong())\n")
        lines.append(
            '    val top=readRegister("sp") and -16L\n    setRegister("sp",top)\n'
        )
        lines.append(
            '    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)\n    val caller=readBytes(top,32)\n'
        )
        lines.append(
            "    val protected=listOf("
            + ",".join(
                "traceMemoryStores(" + kotlin_string(label) + ",4)" for label in LABELS
            )
            + ",traceMemoryStores(top,32))\n"
        )
        lines.append(
            f"    for ((index,trial) in trials.chunked(512)[{number}].withIndex()) {{\n"
        )
        lines.append(
            "        val(a,b,w)=trial\n        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)\n"
        )
        lines.append(
            '        setRegister("a0",a)\n        setRegister("a1",b)\n        setRegister("a2",w)\n'
        )
        lines.append(
            '        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")\n'
        )
        lines.append(
            '        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")\n'
        )
        lines.append(
            '        expectRegister("sp",top)\n        saved.forEach { (r,v) -> expectRegister(r,v) }\n        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")\n        protected.forEach { it.expectNone() }\n        expectOutput("")\n    }\n}\n\n'
        )
    lines.append(
        'case("complete interpolation caller",instructionLimit=1000) {\n    runToExit()\n    expectWord("answer",-64)\n    expectOutput("")\n}\n'
    )
    return "".join(lines)


def build(directory, check=False):
    directory = Path(directory)
    encoded = json.dumps(profile(), separators=(",", ":")) + "\n"
    if len(encoded.encode()) > 1048576:
        raise ValueError("Interpolation profile exceeds the native data limit")
    files = {
        "fixed-interpolate.cases.json": encoded,
        "fixed-interpolate.kts": script(),
        "caller-exit.cases.json": json.dumps(
            dict(
                formatVersion=1,
                cases=[
                    dict(
                        name="silent normal interpolation caller",
                        input="",
                        stdout="",
                        instructionLimit=1000,
                    )
                ],
            ),
            indent=2,
        )
        + "\n",
    }
    for name, text in files.items():
        path = directory / name
        if check:
            if path.read_text() != text:
                raise ValueError(f"Stale generated interpolation cases: {path}")
        else:
            directory.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8", newline="\n")
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-directory", type=Path, required=True)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.output_directory, args.check)
