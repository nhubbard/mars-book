#!/usr/bin/env python3
"""Measure completed instructions in the exact revised sorting visualization source."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def measure(java, cli, output):
    source = ROOT / 'revision/labs/files/sorting-costs.asm'
    output.mkdir(parents=True, exist_ok=False)
    checks = output / 'measure.kts'
    checks.write_text(r'''
val inputs=listOf(
    "sorted" to (-4..3).toList(),
    "reversed" to (3 downTo -4).toList(),
    "mixed" to listOf(2,-2,3,-4,0,1,-3,-1),
    "equal" to List(8) { 0 },
    "nearly" to listOf(-4,-3,-2,-1,0,1,3,2),
    "empty" to emptyList(),
    "single" to listOf(-4),
)
for ((name,input) in inputs) for (method in 0..1) {
    case("measure $name method $method",instructionLimit=100000) {
        val tail=List(8-input.size) { 0x5a000000+it }
        (input+tail).forEachIndexed { index,value -> setWord("values",value,offset=4*index) }
        setWord("count",input.size)
        setWord("algorithm",method)
        val readonly=listOf(traceMemoryStores("count",4),traceMemoryStores("algorithm",4),traceMemoryStores("values_guard",4),traceMemoryStores("frame_guard",4))
        val all=traceExecution()
        val routine=if (method==0) traceExecution("insertion_sort","insertion_sort_end") else traceExecution("selection_sort","selection_sort_end")
        val comparisons=if (method==0) traceExecution("insertion_compare","insertion_compare_end") else traceExecution("selection_compare","selection_compare_end")
        val array=traceMemoryAccesses("values",36)
        val pixels=traceMemoryAccesses("frame",260)
        runToExit()
        val expected=input.sorted()+tail
        expected.forEachIndexed { index,value -> expectWord("values",value,offset=4*index) }
        expectWord("phase",2)
        val swaps=readWord("swap_count")
        val dataWrites=array.snapshot().filter { it.write }
        val begin=readDataAddress("values")
        expectCondition(dataWrites.size==2*swaps && dataWrites.all { it.length==4 && it.address>=begin && it.address+4<=begin+4*input.size },
            "Two bounded word stores per observed exchange")
        val frame=readDataAddress("frame")
        val frameWrites=pixels.snapshot().filter { it.write }
        expectCondition(frameWrites.size==64*(swaps+2) && frameWrites.all { it.length==4 && it.address>=frame && it.address<=frame+252 },
            "Each initial/exchange/final render writes 64 bounded words")
        for (y in 0 until 8) for (x in 0 until 8) {
            val expectedPixel=if (x<input.size && y>=3-input.sorted()[x]) 0xffffff else 0
            expectWord("frame",expectedPixel,offset=4*(8*y+x))
        }
        readonly.forEach { it.expectNone() }
        val work=input.toMutableList()
        var expectedComparisons=0
        var expectedExchanges=0
        if (method==0) {
            for (right in 1 until work.size) {
                var cursor=right
                while (cursor>0) {
                    expectedComparisons++
                    if (work[cursor]>=work[cursor-1]) break
                    val value=work[cursor]
                    work[cursor]=work[cursor-1]
                    work[cursor-1]=value
                    expectedExchanges++
                    cursor--
                }
            }
        } else {
            for (left in 0 until (work.size-1).coerceAtLeast(0)) {
                var minimum=left
                for (scan in left+1 until work.size) {
                    expectedComparisons++
                    if (work[scan]<work[minimum]) minimum=scan
                }
                if (minimum!=left) {
                    val value=work[left]
                    work[left]=work[minimum]
                    work[minimum]=value
                    expectedExchanges++
                }
            }
        }
        expectCondition(comparisons.snapshot().instructions==expectedComparisons.toLong(),"Labelled native comparison count matches this algorithm's decision count")
        expectCondition(swaps==expectedExchanges,"Published count matches actual complete exchanges")
        val whole=all.snapshot()
        val local=routine.snapshot()
        println("MEASURE|$name|$method|${input.size}|${comparisons.snapshot().instructions}|$swaps|${local.instructions}|${whole.instructions}|${local.takenBranches}|${local.notTakenBranches}|${whole.takenBranches}|${whole.notTakenBranches}|${frameWrites.size}")
        expectOutput("")
    }
}
''')
    command = [str(java), '-jar', str(cli), 'grade', str(checks), str(source)]
    result = subprocess.run(command, capture_output=True, text=True, timeout=180)
    (output / 'stdout.txt').write_text(result.stdout)
    (output / 'stderr.txt').write_text(result.stderr)
    rows = []
    for line in result.stdout.splitlines():
        if not line.startswith('MEASURE|'):
            continue
        parts = line.split('|')
        keys = ['method', 'length', 'comparisons', 'exchanges', 'routineInstructions', 'wholeInstructions', 'routineTakenBranches', 'routineNotTakenBranches', 'wholeTakenBranches', 'wholeNotTakenBranches', 'frameWordStores']
        rows.append(dict(input=parts[1], **dict(zip(keys, map(int, parts[2:])))))
    if result.returncode or len(rows) != 14 or sum(line.startswith('PASS ') for line in result.stdout.splitlines()) != 14:
        raise AssertionError((result.returncode, result.stdout[-5000:], result.stderr[-2000:]))
    sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    receipt = dict(sourceSha256=sha(source), cliSha256=sha(cli), toolSha256=sha(Path(__file__)), command=command, rows=rows,
                   scope='Completed MARS64 instructions in this exact source. Routine PC intervals include prologue/epilogue, comparisons, loop control, argument preparation and calls, but exclude publish_exchange and render bodies. Whole includes caller, metadata, all rendering and exit. Assembly, session setup and host time are excluded. Not hardware cycles or universal algorithm timings.')
    (output / 'measurements.json').write_text(json.dumps(receipt, indent=2) + '\n')
    print(json.dumps(rows, indent=2))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java', 'cli', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    args = parser.parse_args()
    measure(args.java.resolve(), args.cli.resolve(), args.output.resolve())
