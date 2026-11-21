#!/usr/bin/env python3
"""Qualify the revised Life source, snapshot boundaries and padded-board helpers."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def verify(java, cli, output, alternatives):
    chapter = ROOT / 'revision/main/state-and-buffers.tex'
    program = ROOT / 'revision/labs/files/life-buffers.asm'
    source = program.read_text()
    blocks = re.findall(r'\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}', chapter.read_text(), re.S)
    if len(blocks) != 3 or not all(source.count(block) == 1 for block in blocks):
        raise ValueError('Every printed helper/core/driver fragment must occur exactly in the complete program')
    output.mkdir(parents=True, exist_ok=False)
    (output / 'chapter-source.tex').write_bytes(chapter.read_bytes())
    oracle = (ROOT / 'revision/labs/checks/life-changes.kts').read_text().split('val fixtures=', 1)[0]
    common = '''import edu.missouristate.mars.cli.grading.MemoryStoreTrace
import kotlin.random.Random

''' + oracle + r'''
val examples=listOf(List(64) { 0 },List(64) { 1 },pattern(2 to 3,3 to 3,4 to 3),
    pattern(3 to 3,4 to 3,3 to 4,4 to 4),pattern(0 to 0,1 to 0,0 to 1),
    pattern(0 to 0,1 to 1,2 to 1,0 to 2,1 to 2))
fun localPattern(mask: Int): List<Int> = List(64) { cell ->
    val x=cell%8-2
    val y=cell/8-2
    if (x in 0..2 && y in 0..2) (mask ushr (3*y+x)) and 1 else 0
}
'''
    whole = r'''
for ((caseIndex,fixture) in fixtures.withIndex()) {
    val (initial,steps)=fixture
    case("board $caseIndex generations $steps",instructionLimit=100000) {
        val protected=mutableListOf(traceMemoryStores("seed",64),traceMemoryStores("generations",4))
        val guards=listOf("frame_guard","seed_guard","board_a_guard","board_b_guard","generations_guard","generation_guard")
        guards.forEachIndexed { index,name ->
            setWord(name,0x12345678 xor (131*caseIndex+index))
            protected+=traceMemoryStores(name,4)
        }
        for (name in listOf("board_a","board_b")) {
            val poison=padded(List(64) { 0x7f })
            for (word in 0 until 25) {
                var value=0
                for (byte in 0..3) value=value or (poison[4*word+byte] shl (8*byte))
                setWord(name,value,offset=4*word)
            }
            protected+=traceMemoryStores(name,10)
            protected+=traceMemoryStores(name,10,offset=90)
            for (row in 1..8) {
                protected+=traceMemoryStores(name,1,offset=10*row)
                protected+=traceMemoryStores(name,1,offset=10*row+9)
            }
        }
        for (word in 0 until 16) {
            var value=0
            for (byte in 0..3) value=value or (initial[4*word+byte] shl (8*byte))
            setWord("seed",value,offset=4*word)
        }
        setWord("generations",steps)
        setWord("generation",-1)
        for (cell in 0 until 64) setWord("frame",0x12340000+cell,offset=4*cell)
        var current=initial
        var previous=initial
        var stable: MemoryStoreTrace?=null
        for (g in 0..steps) {
            runUntil("generation_ready")
            stable?.expectNone()
            stable?.close()
            val active=if (g%2==0) "board_a" else "board_b"
            val older=if (g%2==0) "board_b" else "board_a"
            expectWord("generation",g)
            expectBytes(active,*padded(current))
            if (g>0) expectBytes(older,*padded(previous))
            for (cell in 0 until 64) expectWord("frame",if (current[cell]==1) 0xffffff else 0,offset=4*cell)
            stable=traceMemoryStores(active,100)
            if (g<steps) {
                previous=current
                current=evolve(current)
                step()
            }
        }
        runToExit()
        stable?.expectNone()
        stable?.close()
        expectWord("generation",steps)
        expectWord("generations",steps)
        expectBytes("seed",*initial.toIntArray())
        guards.forEachIndexed { index,name -> expectWord(name,0x12345678 xor (131*caseIndex+index)) }
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}
'''
    direct = r'''
case("update calls preserve full saved state and write every interior once",instructionLimit=1000000) {
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val kept=(0..7).associate { "s$it" to (0x1122334400000000L+it) } +
        mapOf("fp" to 0x123456789abcdef0L,"gp" to 0x23456789abcdef01L)
    kept.forEach { (reg,value) -> setRegister(reg,value) }
    val caller=readBytes(top,32)
    for (old in examples+(0 until 16).map { localPattern(it*31) }) {
        for ((name,bytes) in listOf("board_a" to padded(old),"board_b" to padded(List(64) { 0x7f }))) {
            for (word in 0 until 25) {
                var value=0
                for (byte in 0..3) value=value or (bytes[4*word+byte] shl (8*byte))
                setWord(name,value,offset=4*word)
            }
        }
        val stable=traceMemoryStores("board_a",100)
        val written=traceMemoryAccesses("board_b",100)
        setRegisterToDataAddress("a0","board_a")
        setRegisterToDataAddress("a1","board_b")
        callFunction("life_step",returnTo="generation_built")
        val next=evolve(old)
        expectBytes("board_a",*padded(old))
        expectBytes("board_b",*padded(next))
        expectRegister("v0",next.sum())
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller-owned stack bytes changed")
        stable.expectNone()
        stable.close()
        val base=readDataAddress("board_b")
        val expected=(0 until 8).flatMap { y -> (0 until 8).map { x -> base+10*(y+1)+(x+1) } }.toSet()
        val stores=written.snapshot().filter { it.write }
        expectCondition(stores.size==64 && stores.all { it.length==1 } && stores.map { it.address }.toSet()==expected,
            "Expected exactly one byte store per destination interior, no halo stores")
        written.close()
    }
    expectOutput("")
}
case("neighbor helper: all local states and every board position",instructionLimit=100000) {
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val kept=(0..7).associate { "s$it" to (0x2233445500000000L+it) }
    kept.forEach { (reg,value) -> setRegister(reg,value) }
    val cases=(0 until 512).map { Triple(localPattern(it),3 to 3,Integer.bitCount(it and 0x1ef)) } +
        (0 until 64).map { cell ->
            val x=cell%8
            val y=cell/8
            Triple(List(64) { 1 },x to y,(if (x in 1..6) 3 else 2)*(if (y in 1..6) 3 else 2)-1)
        }
    for ((old,point,expected) in cases) {
        val bytes=padded(old)
        for (word in 0 until 25) {
            var value=0
            for (byte in 0..3) value=value or (bytes[4*word+byte] shl (8*byte))
            setWord("board_a",value,offset=4*word)
        }
        val offset=10*(point.second+1)+(point.first+1)
        val base=readDataAddress("board_a")
        val accesses=traceMemoryAccesses("board_a",100)
        setRegisterToDataAddress("a0","board_a",offset=offset.toLong())
        callFunction("count_neighbors",returnTo="generation_built")
        expectRegister("v0",expected)
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        val observed=accesses.snapshot()
        val wanted=listOf(-11,-10,-9,-1,1,9,10,11).map { base+offset+it }.toSet()
        expectCondition(observed.size==8 && observed.all { !it.write && it.length==1 } && observed.map { it.address }.toSet()==wanted,
            "Expected eight bounded neighbor reads excluding the center")
        expectBytes("board_a",*bytes)
        accesses.close()
    }
    expectOutput("")
}
'''
    all_fixtures = r'''
val random=Random(0x4d415253)
val fixtures=(0 until 512).map { localPattern(it) to 1 } +
    (0 until 64).map { cell -> pattern(cell%8 to cell/8) to 2 } +
    (0 until 64).map { index -> List(64) { if (random.nextInt(64)<(index%8+1)*8) 1 else 0 } to 4 } +
    examples.flatMap { board -> (0..4).map { board to it } }
'''
    (output / 'checks.kts').write_text(common + all_fixtures + whole + direct)
    (output / 'fault-checks.kts').write_text(common + 'val fixtures=examples.flatMap { board -> (0..4).map { board to it } }\n' + whole)
    runs = []

    def grade(name, text, accepted):
        path = output / (name + '.asm')
        path.write_text(text)
        checks = output / ('checks.kts' if accepted else 'fault-checks.kts')
        command = [str(java), '-jar', str(cli), 'grade', str(checks), str(path)]
        result = subprocess.run(command, capture_output=True, text=True, timeout=300)
        (output / (name + '.stdout')).write_text(result.stdout)
        (output / (name + '.stderr')).write_text(result.stderr)
        passed = sum(line.startswith('PASS ') for line in result.stdout.splitlines())
        failed = sum(line.startswith('FAIL ') for line in result.stdout.splitlines())
        total = 672 if accepted else 30
        if passed+failed != total or (accepted and (result.returncode or failed)) or (not accepted and (result.returncode == 0 or not failed)):
            raise AssertionError((name, result.returncode, passed, failed, result.stdout[-4000:], result.stderr[-2000:]))
        runs.append(dict(name=name, accepted=accepted, passed=passed, failed=failed, command=command, sourceSha256=sha(path)))

    grade('literal', source, True)
    for index, path in enumerate(alternatives):
        grade('alternative-' + str(index + 1), path.read_text(), True)
    faults = {
        'in-place': source.replace('    move $a1, $s1\n    balc life_step', '    move $a1, $s0\n    balc life_step'),
        'missing-swap': source.replace('    move $s0, $s1\n    move $s1, $t0', '    nop\n    nop'),
        'skipped-dead-store': source.replace('    sb $t1, 0($s1)', '    beqzc $t1, dead_store_skipped\n    nop\n    sb $t1, 0($s1)\ndead_store_skipped:'),
        'center-counted': source.replace('    lbu $v0, -11($a0)', '    lbu $v0, -11($a0)\n    lbu $t0, 0($a0)\n    addu $v0, $v0, $t0'),
        'wrong-row-gap': source.replace('daddiu $s0, $s0, 2', 'daddiu $s0, $s0, 1').replace('daddiu $s1, $s1, 2', 'daddiu $s1, $s1, 1'),
        'stale-black-pixel': source.replace('    sw $t4, 0($t1)', '    beqzc $t4, black_store_skipped\n    nop\n    sw $t4, 0($t1)\nblack_store_skipped:'),
        'early-publication': source.replace('generation_ready:\n', 'ready_after_render:\n').replace('render_generation:\n', 'render_generation:\ngeneration_ready:\n'),
    }
    for name, text in faults.items():
        if text == source:
            raise ValueError('Mutation did not apply: ' + name)
        grade(name, text, False)
    (output / 'receipt.json').write_text(json.dumps(dict(chapterSha256=sha(chapter), listingSha256=sha(program), checkerSha256=sha(Path(__file__)), cliSha256=sha(cli), runs=runs, perImplementation=dict(generationFixtures=670, directUpdateCalls=22, directNeighborCalls=576, reportedCases=672), scope='All 512 local binary states, finite-edge positions, sampled boards and requested generations 0..4. Temporal source preservation, padding, complete destination and rendering; not all 2^64 boards or a whole-address-space access audit. Direct helper/store-footprint qualification is stricter than the independent student contract.'), indent=2) + '\n')
    print('Passed literal and', len(alternatives), 'alternatives: 670 generation fixtures, 22 update calls and 576 neighbor calls each; seven faults rejected.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java', 'cli', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    parser.add_argument('--alternative', type=Path, action='append', default=[])
    args = parser.parse_args()
    verify(args.java.resolve(), args.cli.resolve(), args.output.resolve(), [path.resolve() for path in args.alternative])
