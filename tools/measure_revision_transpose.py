#!/usr/bin/env python3
"""Measure the exact public transpose program with cold, equal-capacity MARS cache models."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def measure(java, cli, output):
    source = ROOT / 'revision/labs/files/matrix-locality.asm'
    output.mkdir(parents=True, exist_ok=False)
    checks = output / 'measure.kts'
    checks.write_text('''import edu.missouristate.mars.devices.DataCacheSimulator.Options
import edu.missouristate.mars.devices.DataCacheSimulator.Placement

for((rows,cols) in listOf(0 to 5,3 to 5,4 to 4,8 to 8,16 to 16,15 to 13)) for(method in 0..1) case("measure $rows by $cols method $method",instructionLimit=250000) {
    for(index in 0..255) {
        setWord("input",index,offset=4*index)
        setWord("output",0x12345678,offset=4*index)
    }
    val readonly=listOf(traceMemoryStores("input",1024),traceMemoryStores("input_guard",4),traceMemoryStores("output_guard",4),traceMemoryStores("copied",4),traceMemoryStores("metadata_guard",4))
    setRegisterToDataAddress("a0","input")
    setRegisterToDataAddress("a1","output")
    setRegister("a2",rows)
    setRegister("a3",cols)
    setRegisterToTextAddress("ra","transpose_done")
    startAt(if(method==0) "transpose_naive" else "transpose_tiled")
    val instructions=traceExecution()
    val full=connectDataCache(Options(8,4,Placement.FULL,8),logAccesses=true,logLimit=1000000)
    val direct=connectDataCache(Options(8,4))
    val two=connectDataCache(Options(8,4,Placement.SET_ASSOCIATIVE,2))
    runUntil("transpose_done")
    val f=full.snapshot();val d=direct.snapshot();val t=two.snapshot()
    expectRegister("v0",rows*cols)
    for(i in 0 until rows) for(j in 0 until cols) expectWord("output",i*cols+j,offset=4*(j*rows+i))
    for(index in rows*cols until 256) expectWord("output",0x12345678,offset=4*index)
    readonly.forEach { it.expectNone() }
    expectCondition(f.accesses==2L*rows*cols && d.accesses==f.accesses && t.accesses==f.accesses,"These leaf implementations have exactly two native word notices per cell and no auxiliary traffic")
    val input=readDataAddress("input");val output=readDataAddress("output")
    println("MEASURE|$rows|$cols|$method|${instructions.snapshot().instructions}|${f.accesses}|${f.misses}|${d.misses}|${t.misses}|$input|$output")
    for(access in full.recordedAccesses()) println("ACCESS|$rows|$cols|$method|${access.number}|${access.address}|${access.write}|${access.hit}")
    full.close();direct.close();two.close()
    expectOutput("")
}
''')
    command = [str(java), '-jar', str(cli), 'grade', str(checks), str(source)]
    result = subprocess.run(command, capture_output=True, text=True, timeout=240)
    (output / 'stdout.txt').write_text(result.stdout)
    (output / 'stderr.txt').write_text(result.stderr)
    rows = []
    for line in result.stdout.splitlines():
        if not line.startswith('MEASURE|'):
            continue
        keys = ['rows', 'columns', 'method', 'instructions', 'accesses', 'fullMisses', 'directMisses', 'twoWayMisses', 'inputAddress', 'outputAddress']
        rows.append(dict(zip(keys, map(int, line.split('|')[1:]))))
    if result.returncode or len(rows) != 12 or sum(line.startswith('PASS ') for line in result.stdout.splitlines()) != 12:
        raise AssertionError((result.returncode, result.stdout[-3000:], result.stderr[-2000:]))
    sha = lambda f: hashlib.sha256(f.read_bytes()).hexdigest()
    receipt = dict(sourceSha256=sha(source), cliSha256=sha(cli), toolSha256=sha(Path(__file__)), command=command, rows=rows,
                   scope='Cold at function entry; stop at transpose_done before the caller count store. Core completed instructions and native data notices, including auxiliary traffic if present. Eight lines, four words each, LRU, full/direct/two-way at equal 128-byte capacity. Instruction fetch, external inspection, assembly, startup, elapsed host time and hardware cycles excluded. Source is fixed; this is not a universal student instruction/miss requirement.')
    (output / 'measurements.json').write_text(json.dumps(receipt, indent=2) + '\n')
    print(json.dumps(rows, indent=2))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java', 'cli', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    args = parser.parse_args()
    measure(args.java.resolve(), args.cli.resolve(), args.output.resolve())
