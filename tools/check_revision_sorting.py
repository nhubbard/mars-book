#!/usr/bin/env python3
"""Qualify the worked sorting publication states, bounded accesses and call contracts."""
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
    program = ROOT / 'revision/labs/files/sorting-costs.asm'
    chapter = ROOT / 'revision/main/algorithms-and-cost.tex'
    source = program.read_text()
    snippets = re.findall(r'\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}', chapter.read_text(), re.S)
    if len(snippets) != 2 or not all(source.count(part) == 1 for part in snippets):
        raise ValueError('Printed sorting fragments differ from the complete public source')
    output.mkdir(parents=True, exist_ok=False)
    (output / 'chapter-source.tex').write_bytes(chapter.read_bytes())
    oracle = (ROOT / 'packages/009-sorting-visualizer/checks/public.kts').read_text().split('val fixtures =', 1)[0]
    whole = (ROOT / 'packages/009-sorting-visualizer/checks/public.kts').read_text().split('for ((fixtureIndex, input) in fixtures.withIndex())', 1)[1]
    whole = 'for ((fixtureIndex, input) in fixtures.withIndex())' + whole
    whole = whole.replace('val states = exchanges(input, method)', 'val arrayAccesses=traceMemoryAccesses("values",36)\n        val states = exchanges(input, method)')
    whole = whole.replace('        protected.forEach { it.expectNone() }', '        arrayAccesses.expectReadsWithin(readDataAddress("values"),4*input.size)\n        protected.forEach { it.expectNone() }')
    common = 'import kotlin.random.Random\n\n' + oracle + r'''
val examples=listOf((-4..3).toList(),(3 downTo -4).toList(),listOf(2,-2,3,-4,0,1,-3,-1),
    listOf(0,-1,0,-1),listOf(0,0,-4,3,3,-4,0,0),List(8) { -1 },List(8) { 3 },List(8) { -4 })
fun words(length: Int): List<List<Int>> = if (length==0) listOf(emptyList()) else words(length-1).flatMap { prefix -> listOf(-4,0,3).map { prefix+it } }
'''
    fixtures = r'''
val random=Random(0x534f5254)
val fixtures=(0..5).flatMap(::words)+(0 until 64).map { List(8) { random.nextInt(-4,4) } }+examples
'''
    direct = r'''
case("both sorting functions retain saved state on every valid subspan",instructionLimit=2000000) {
    val initial=listOf(2,-2,3,-4,0,1,-3,-1)
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val kept=(0..7).associate { "s$it" to (0x1122334400000000L+it) } +
        mapOf("fp" to 0x123456789abcdef0L,"gp" to 0x23456789abcdef01L)
    kept.forEach { (reg,value) -> setRegister(reg,value) }
    val caller=readBytes(top,32)
    for (offset in 0..8) for (length in 0..8-offset) for (method in 0..1) {
        initial.forEachIndexed { index,value -> setWord("values",value,offset=4*index) }
        setWord("phase",99)
        setWord("swap_count",0)
        val input=initial.drop(offset).take(length)
        val array=traceMemoryAccesses("values",36)
        val begin=readDataAddress("values")+4*offset
        val callerStores=traceMemoryStores(top,32)
        setRegisterToDataAddress("a0","values",offset=4L*offset)
        setRegister("a1",length)
        callFunction(if (method==0) "insertion_sort" else "selection_sort",returnTo="done")
        val expected=initial.take(offset)+input.sorted()+initial.drop(offset+length)
        expected.forEachIndexed { index,value -> expectWord("values",value,offset=4*index) }
        expectWord("swap_count",exchanges(input,method).size-1)
        array.expectReadsWithin(begin,4*length)
        expectCondition(array.snapshot().filter { it.write }.all { it.address>=begin && it.address+it.length<=begin+4*length },"Sorting stores only its passed span")
        array.close()
        callerStores.expectNone()
        callerStores.close()
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller-owned stack bytes changed")
    }
    expectOutput("")
}
case("renderer handles every subspan and each bar height independently",instructionLimit=1000000) {
    val patterns=(-4..3).map { value -> List(8) { value } }+listOf(examples[0],examples[2])
    val readonly=listOf("values" to 32,"values_guard" to 4,"count" to 4,"algorithm" to 4,"phase" to 4,"swap_count" to 4,"frame_guard" to 4)
        .map { (name,size) -> traceMemoryStores(name,size) }
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val kept=(0..7).associate { "s$it" to (0x2233445500000000L+it) } +
        mapOf("fp" to 0x123456789abcdef0L,"gp" to 0x23456789abcdef01L)
    kept.forEach { (reg,value) -> setRegister(reg,value) }
    for (initial in patterns) for (offset in 0..8) for (length in 0..8-offset) {
        initial.forEachIndexed { index,value -> setWord("values",value,offset=4*index) }
        for (cell in 0 until 64) setWord("frame",0x12345678,offset=4*cell)
        setWord("phase",99)
        setWord("swap_count",77)
        val reads=traceMemoryAccesses("values",36)
        val pixels=traceMemoryAccesses("frame",260)
        setRegisterToDataAddress("a0","values",offset=4L*offset)
        setRegister("a1",length)
        callFunction("render",returnTo="done")
        val input=initial.drop(offset).take(length)
        for (y in 0 until 8) for (x in 0 until 8) expectWord("frame",pixel(input,x,y),offset=4*(8*y+x))
        reads.expectReadsWithin(readDataAddress("values")+4*offset,4*length)
        reads.close()
        val base=readDataAddress("frame")
        val stores=pixels.snapshot().filter { it.write }
        expectCondition(stores.size==64 && stores.all { it.length==4 && it.address>=base && it.address<=base+252 } && stores.map { it.address }.toSet().size==64,"One bounded word store per frame cell")
        pixels.close()
        expectWord("phase",99)
        expectWord("swap_count",77)
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
    }
    readonly.forEach { it.expectNone() }
    expectOutput("")
}
'''
    (output / 'checks.kts').write_text(common + fixtures + whole + direct)
    (output / 'fault-checks.kts').write_text(common + 'val fixtures=examples+listOf(emptyList(),listOf(-4))\n' + whole)
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
        total = 874 if accepted else 20
        if passed+failed != total or (accepted and (result.returncode or failed)) or (not accepted and (result.returncode == 0 or not failed)):
            raise AssertionError((name, result.returncode, passed, failed, result.stdout[-5000:], result.stderr[-2000:]))
        runs.append(dict(name=name, accepted=accepted, passed=passed, failed=failed, command=command, sourceSha256=sha(path)))

    grade('literal', source, True)
    for index, path in enumerate(alternatives):
        grade('alternative-' + str(index + 1), path.read_text(), True)
    exchange = '''    sw $t1, -4($t0)
    sw $t2, 0($t0)
insertion_exchanged:
    move $a0, $s0
    move $a1, $s1
    balc publish_exchange'''
    premature = '''    move $a0, $s0
    move $a1, $s1
    balc publish_exchange
    sw $t1, -4($t0)
    sw $t2, 0($t0)
insertion_exchanged:'''
    faults = {
        'unsigned-keys': source.replace('bgec $t1, $t2, insertion_next', 'bgeuc $t1, $t2, insertion_next'),
        'last-equal-minimum': source.replace('bgec $t4, $t2, selection_scan_next', 'bltc $t2, $t4, selection_scan_next'),
        'lost-exchange-word': source.replace('    sw $t2, 0($t0)', '    nop'),
        'early-exchange-publication': source.replace(exchange, premature),
        'renderer-extra-word': source.replace('bgec $t1, $a1, render_store', 'bltc $a1, $t1, render_store'),
        'skipped-black-pixel': source.replace('    sw $t4, 0($t2)', '    beqzc $t4, black_skipped\n    nop\n    sw $t4, 0($t2)\nblack_skipped:'),
        'outer-bound-overread': source.replace('bgec $s2, $s1, insertion_return', 'bltc $s1, $s2, insertion_return'),
    }
    for name, text in faults.items():
        if text == source:
            raise ValueError('Mutation did not apply: ' + name)
        grade(name, text, False)
    (output / 'receipt.json').write_text(json.dumps(dict(chapterSha256=sha(chapter), sourceSha256=sha(program), checkerSha256=sha(Path(__file__)), cliSha256=sha(cli), runs=runs, perImplementation=dict(programFixtures=872, directSortCalls=90, directRenderCalls=450, reportedCases=874), scope='All ternary prefixes through length five, sampled eight-word patterns, exact published exchange states and independent subspans/heights. Not every eight-word input or whole-address-space audit. Source-specific author labels are not required by the independent partition exercise.'), indent=2) + '\n')
    print('Passed literal and', len(alternatives), 'alternatives: 872 visual runs, 90 direct sort calls and 450 render calls each; seven faults rejected.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java', 'cli', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    parser.add_argument('--alternative', type=Path, action='append', default=[])
    args = parser.parse_args()
    verify(args.java.resolve(), args.cli.resolve(), args.output.resolve(), [path.resolve() for path in args.alternative])
