#!/usr/bin/env python3
"""Qualify the chapter's literal raster against an independent rational oracle."""

from kotlin_format import format_kotlin
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify(java, cli, output, alternatives):
    chapter = ROOT / "revision/main/integer-algorithms.tex"
    blocks = re.findall(
        r"\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}",
        chapter.read_text(),
        re.S,
    )
    if len(blocks) != 4:
        raise ValueError(
            "Expected caller, endpoint ordering, initialization and loop listings"
        )
    literal = "\n".join((blocks[0], blocks[1] + blocks[2], blocks[3]))
    if literal != (ROOT / "revision/labs/files/integer-line.asm").read_text():
        raise ValueError("Download differs from the exact printed composition")
    output.mkdir(parents=True, exist_ok=False)
    (output / "chapter-source.tex").write_bytes(chapter.read_bytes())
    oracle = (
        (ROOT / "revision/labs/checks/dashed-line.kts")
        .read_text()
        .split("val fixtures=", 1)[0]
    )
    body = r"""
for ((number,line) in fixtures.withIndex()) {
    val (ax,ay,bx,by)=line
    case("raster $ax,$ay to $bx,$by",instructionLimit=3000) {
        val old=List(64) { (0x55000000 or (it*0x010307)) xor number }
        val rgb=if (number%19==0) 0 else ((number+1)*0x010103) and 0xffffff
        line.forEachIndexed { index,value -> setWord("endpoints",value,offset=4*index) }
        for (cell in 0 until 64) setWord("frame",old[cell],offset=4*cell)
        setWord("color",rgb)
        val guardNames=listOf("frame_guard","endpoints_guard","color_guard","count_guard")
        guardNames.forEachIndexed { index,name -> setWord(name,0x12345678 xor (number+index)) }
        val protected=listOf(traceMemoryStores("endpoints",16),traceMemoryStores("color",4)) +
            guardNames.map { traceMemoryStores(it,4) }
        val frame=readDataAddress("frame")
        val writes=traceMemoryAccesses(frame,260)
        val top=readRegister("sp") and -16L
        setRegister("sp",top)
        val caller=readBytes(top,32)
        val kept=(0..7).associate { "s$it" to (0x1122334400000000L+it) } +
            mapOf("fp" to 0x123456789abcdef0L,"gp" to 0x23456789abcdef01L)
        kept.forEach { (reg,value) -> setRegister(reg,value) }
        setRegisterToDataAddress("a0","endpoints")
        setRegisterToDataAddress("a1","frame")
        setRegister("a2",rgb)
        callFunction("draw_line",returnTo="line_complete")
        val selected=lineCells(ax,ay,bx,by)
        for (y in 0 until 8) for (x in 0 until 8) {
            val cell=8*y+x
            expectWord("frame",if (x to y in selected) rgb else old[cell],offset=4*cell)
        }
        expectRegister("v0",selected.size)
        line.forEachIndexed { index,value -> expectWord("endpoints",value,offset=4*index) }
        expectWord("color",rgb)
        guardNames.forEachIndexed { index,name -> expectWord(name,0x12345678 xor (number+index)) }
        protected.forEach { it.expectNone() }
        val stores=writes.snapshot().filter { it.write }
        expectCondition(stores.all { it.length==4 } && stores.map { it.address }==selected.map { (x,y) -> frame+4*(8*y+x) },
            "Expected one bounded word store per canonical selected cell")
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        expectRegister("sp",top)
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller-owned stack bytes changed")
        expectOutput("")
    }
}
case("complete default driver",instructionLimit=3000) {
    runToExit()
    expectWord("line_count",6)
    expectWord("frame_guard",0x13579bdf)
    expectWord("endpoints_guard",0x2468ace0)
    expectWord("color_guard",0x10203040)
    expectWord("count_guard",0x31415926)
    val selected=lineCells(1,1,6,4)
    for (y in 0 until 8) for (x in 0 until 8)
        expectWord("frame",if (x to y in selected) 0xffffff else 0,offset=4*(8*y+x))
    expectOutput("")
}
"""
    full = "val fixtures=(0 until 4096).map { n -> listOf((n ushr 9) and 7,(n ushr 6) and 7,(n ushr 3) and 7,n and 7) }\n"
    sample = "val fixtures=listOf(listOf(1,1,6,4),listOf(6,4,1,1),listOf(0,0,2,1),listOf(2,1,0,0),listOf(3,7,3,0),listOf(4,4,4,4),listOf(1,6,3,0),listOf(0,7,7,7),listOf(0,0,7,7),listOf(7,0,0,7),listOf(0,0,1,2),listOf(7,7,7,7))\n"
    (output / "checks.kts").write_text(format_kotlin(oracle + full + body))
    (output / "fault-checks.kts").write_text(format_kotlin(oracle + sample + body))
    runs = []

    def grade(name, program, accepted):
        path = output / (name + ".asm")
        path.write_text(program)
        checks = output / ("checks.kts" if accepted else "fault-checks.kts")
        command = [str(java), "-jar", str(cli), "grade", str(checks), str(path)]
        result = subprocess.run(command, capture_output=True, text=True, timeout=240)
        (output / (name + ".stdout")).write_text(result.stdout)
        (output / (name + ".stderr")).write_text(result.stderr)
        passed = sum(line.startswith("PASS ") for line in result.stdout.splitlines())
        failed = sum(line.startswith("FAIL ") for line in result.stdout.splitlines())
        total = 4097 if accepted else 13
        if (
            passed + failed != total
            or (accepted and (result.returncode or failed))
            or (not accepted and (result.returncode == 0 or not failed))
        ):
            raise AssertionError(
                (
                    name,
                    result.returncode,
                    passed,
                    failed,
                    result.stdout[-4000:],
                    result.stderr[-2000:],
                )
            )
        runs.append(
            dict(
                name=name,
                accepted=accepted,
                passed=passed,
                failed=failed,
                command=command,
                sourceSha256=sha(path),
            )
        )

    grade("literal", literal, True)
    for index, path in enumerate(alternatives):
        grade("alternative-" + str(index + 1), path.read_text(), True)
    faults = {
        "refreshed-error": literal.replace(
            "skip_x:\n", "skip_x:\n    sll $t8, $t7, 1\n"
        ),
        "exclusive-updates": literal.replace(
            "    addiu $t0, $t0, 1\nskip_x:",
            "    addiu $t0, $t0, 1\n    bc line_next\nskip_x:",
        ),
        "missing-endpoint": literal.replace(
            "line_pixel:\n",
            "line_pixel:\n    bnec $t0, $t2, endpoint_not_reached\n    nop\n    beqc $t1, $t3, line_return\n    nop\nendpoint_not_reached:\n",
        ),
        "unsigned-error": literal.replace(
            "bltc $t8, $t5, skip_x", "bltuc $t8, $t5, skip_x"
        ),
        "wrong-row-stride": literal.replace("sll $v1, $t1, 3", "sll $v1, $t1, 4"),
        "missing-count": literal.replace("addiu $v0, $v0, 1", "nop"),
        "unordered-endpoints": literal.replace(
            blocks[1], "\n".join(blocks[1].splitlines()[:5]) + "\n"
        ),
    }
    for name, program in faults.items():
        if literal == program:
            raise ValueError("Mutation did not apply: " + name)
        grade(name, program, False)
    (output / "receipt.json").write_text(
        json.dumps(
            dict(
                chapterSha256=sha(chapter),
                listingSha256=hashlib.sha256(literal.encode()).hexdigest(),
                cliSha256=sha(cli),
                checkerSha256=sha(Path(__file__)),
                runs=runs,
                perImplementation=dict(endpointTuples=4096, completeDriver=1),
                scope="Exhaustive endpoint tuples with sampled deterministic color/background values. Relevant frame/guard store histories, protected inputs, saved state and seven faults; not every color/background or a whole-address-space write audit. Author store-order qualification is not the independent student contract.",
            ),
            indent=2,
        )
        + "\n"
    )
    print(
        "Passed literal and",
        len(alternatives),
        "alternatives: all 4096 endpoint tuples and complete driver each; seven faults rejected.",
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "cli", "output"):
        parser.add_argument("--" + name, type=Path, required=True)
    parser.add_argument("--alternative", type=Path, action="append", default=[])
    args = parser.parse_args()
    verify(
        args.java.resolve(),
        args.cli.resolve(),
        args.output.resolve(),
        [path.resolve() for path in args.alternative],
    )
