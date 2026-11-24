#!/usr/bin/env python3
"""Check the literal loop through native memory traces and the existing grader."""

import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "revision/main/decisions-and-repetition.tex"
STANDARD = [
    0x3F,
    0x06,
    0x5B,
    0x4F,
    0x66,
    0x6D,
    0x7D,
    0x07,
    0x7F,
    0x6F,
    0x77,
    0x7C,
    0x39,
    0x5E,
    0x79,
    0x71,
]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify(java, cli, output, alternative=None):
    source = SOURCE.read_bytes()
    blocks = re.findall(
        r"\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}", source.decode(), re.S
    )
    if (
        len(blocks) != 2
        or (ROOT / "revision/labs/files/range-counter.asm").read_text() != blocks[1]
    ):
        raise ValueError(
            "Expected the choice fragment and literal counter matching the downloadable program"
        )
    listing = blocks[1]
    output.mkdir(parents=True, exist_ok=False)
    (output / "chapter-source.tex").write_bytes(source)
    cases = []
    for first in range(17):
        for count in range(17 - first):
            cases.append((first, count, STANDARD))
    # Full byte domain and repeated values, using varied valid/empty windows.
    fonts = [[byte] * 16 for byte in (0, 0x3F, 0xFF)]
    fonts += [list(range(start, start + 16)) for start in range(0, 256, 16)]
    windows = [(0, 16), (0, 0), (8, 0), (16, 0), (14, 2), (3, 5)]
    for font in fonts:
        for first, count in windows:
            cases.append((first, count, font))
    assignment = (
        """var caseNumber = 0
for ((first, count, font) in listOf(
"""
        + ",\n".join(
            f"    Triple({first}, {count}, listOf(" + ",".join(map(str, font)) + "))"
            for first, count, font in cases
        )
        + """
)) {
    case("${caseNumber++}: range $first/$count font ${font.first()}", instructionLimit = 1000) {
        val io = traceMemoryStores(0xffff0000L, 0x10000)
        val writes = traceMemoryStores("glyphs", 32)
        val reads = traceMemoryAccesses("glyphs", 20)
        setWord("first_digit", first)
        setWord("digit_count", count)
        for (word in 0 until 4) {
            var value = 0
            for (byte in 0 until 4) value = value or (font[word*4+byte] shl (byte*8))
            setWord("glyphs", value, offset=word*4)
        }
        val tableGuard = 0x13579bdf xor first
        val inputGuard = 0x2468ace0 xor count
        setWord("glyph_guard", tableGuard)
        setWord("inputs_guard", inputGuard)
        runToExit()
        io.expectByteStores(0xffff0010L, *font.subList(first,first+count).toIntArray())
        writes.expectNone()
        val expected = (first until first+count).map { readDataAddress("glyphs", it.toLong()) to 1 }
        val actual = reads.snapshot().filter { !it.write }.map { it.address to it.length }
        expectCondition(actual == expected, "Expected one bounded byte read per requested glyph, or none for an empty range")
        expectBytes("glyphs", *font.toIntArray())
        expectWord("glyph_guard", tableGuard)
        expectWord("first_digit", first)
        expectWord("digit_count", count)
        expectWord("inputs_guard", inputGuard)
        expectOutput("")
    }
}
"""
    )
    checks = output / "checks.kts"
    checks.write_text(assignment)
    runs = []

    def grade(name, program, success):
        asm = output / (name + ".asm")
        asm.write_text(program)
        command = [str(java), "-jar", str(cli), "grade", str(checks), str(asm)]
        result = subprocess.run(command, capture_output=True, text=True, timeout=120)
        (output / (name + ".stdout")).write_text(result.stdout)
        (output / (name + ".stderr")).write_text(result.stderr)
        passed = sum(line.startswith("PASS ") for line in result.stdout.splitlines())
        failed = sum(line.startswith("FAIL ") for line in result.stdout.splitlines())
        if (
            passed + failed != len(cases)
            or (success and (result.returncode != 0 or failed))
            or (not success and (result.returncode == 0 or not failed))
        ):
            raise AssertionError(
                (name, result.returncode, passed, failed, result.stderr[-2000:])
            )
        runs.append(
            dict(
                name=name,
                expectedSuccess=success,
                passed=passed,
                failed=failed,
                exitCode=result.returncode,
                sourceSha256=sha(asm),
                stdoutSha256=sha(output / (name + ".stdout")),
                stderrSha256=sha(output / (name + ".stderr")),
                command=command,
            )
        )

    grade("literal", listing, True)
    if alternative:
        grade("alternative", alternative.read_text(), True)
    faults = {
        "wrong-stride": listing.replace("daddiu $t0, $t0, 1", "daddiu $t0, $t0, 2"),
        "wrong-port": listing.replace("0x00000000ffff0010", "0x00000000ffff0011"),
        "wrong-width": listing.replace("sb $t3, 0($t7)", "sw $t3, 0($t7)"),
        "no-decrement": listing.replace("addiu $t1, $t1, -1", "nop"),
        "load-before-test": listing.replace("loop:\n", "loop:\n    lbu $t3, 0($t0)\n"),
        "forbidden-slot": listing.replace("    nop\n", "    bc done\n"),
    }
    for name, program in faults.items():
        if program == listing:
            raise ValueError("Fault was not applied: " + name)
        grade(name, program, False)
    probes = []
    for count in (0, 16):
        program = faults["forbidden-slot"].replace(
            "digit_count:  .word 16", f"digit_count:  .word {count}"
        )
        asm = output / f"forbidden-count-{count}.asm"
        asm.write_text(program)
        command = [
            str(java),
            "-jar",
            str(cli),
            "--start-at-main",
            "--max-steps",
            "1000",
            str(asm),
        ]
        result = subprocess.run(command, capture_output=True, text=True, timeout=15)
        assert (
            (result.returncode == 0 and not result.stderr)
            if count == 0
            else (result.returncode != 0 and "forbidden slot" in result.stderr)
        )
        probes.append(
            dict(
                count=count,
                exitCode=result.returncode,
                stdout=result.stdout,
                stderr=result.stderr,
                command=command,
                sourceSha256=sha(asm),
            )
        )
    choices = []
    for flag in (0, 5, -1):
        program = (
            ".text\n.globl main\nmain:\n    li $t0, "
            + str(flag)
            + "\n"
            + blocks[0]
            + "    li $v0, 10\n    syscall\n"
        )
        asm = output / f"choice-{flag}.asm"
        asm.write_text(program)
        command = [
            str(java),
            "-jar",
            str(cli),
            "--start-at-main",
            "--max-steps",
            "1000",
            "--register",
            "t1",
            str(asm),
        ]
        result = subprocess.run(command, capture_output=True, text=True, timeout=15)
        expected = 7 if flag == 0 else 9
        if (
            result.returncode
            or result.stderr
            or result.stdout != f"\n$t1\t0x{expected:016x}\n"
        ):
            raise AssertionError(
                (flag, result.returncode, result.stdout, result.stderr)
            )
        choices.append(
            dict(
                flag=flag,
                result=expected,
                exitCode=result.returncode,
                stdout=result.stdout,
                stderr=result.stderr,
                sourceSha256=sha(asm),
                command=command,
            )
        )
    receipt = dict(
        sourceSha256=hashlib.sha256(source).hexdigest(),
        listingSha256=hashlib.sha256(listing.encode()).hexdigest(),
        verifierSha256=sha(Path(__file__)),
        cliSha256=sha(cli),
        checksSha256=sha(checks),
        legalRangeCases=153,
        extraFontWindowCases=len(cases) - 153,
        casesPerProgram=len(cases),
        runs=runs,
        forbiddenSlotProbes=probes,
        choiceProbes=choices,
        scope="Existing native grader observes exact ordered byte-port transactions, exact glyph-read addresses/widths including empty ranges, protected data writes and final contents, silence and normal bounded exit. No native IDE pause/repaint or all possible table combinations claimed.",
    )
    (output / "receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(
        f"Passed {len(cases)} cases per accepted program; rejected six assembled faults; taken/not-taken forbidden-slot probes agree."
    )


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "cli", "output"):
        p.add_argument("--" + name, type=Path, required=True)
    p.add_argument("--alternative", type=Path)
    a = p.parse_args()
    verify(
        a.java.resolve(),
        a.cli.resolve(),
        a.output.resolve(),
        a.alternative.resolve() if a.alternative else None,
    )
