#!/usr/bin/env python3
"""Measure the public scalar matrix kernels with explicit cold/warm/replay/launch scopes."""

import argparse
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def measure(java, cli, output):
    source = ROOT / "revision/labs/files/matrix-measure.asm"
    output.mkdir(parents=True, exist_ok=False)
    checks = output / "measure.kts"
    checks.write_text(
        """import edu.missouristate.mars.devices.DataCacheSimulator.Options
import edu.missouristate.mars.devices.DataCacheSimulator.Placement

for((m,k,n) in listOf(Triple(0,3,2),Triple(2,3,0),Triple(2,0,2),Triple(2,3,2),Triple(8,8,8),Triple(16,16,16),Triple(15,13,11)))
for(tile in listOf(1,4,8))for(method in 0..1)for(launch in listOf(false,true))
case("measure $m $k $n tile$tile method$method launch$launch",instructionLimit=1000000) {
    for(i in 0 until 272) {
        setWord("left",i%11-5,offset=i*4)
        setWord("right",i%7-3,offset=i*4)
        setWord("product",0x5a5aa5a5,offset=i*8)
        setWord("product",0x5a5aa5a5,offset=i*8+4)
    }
    for((index,value) in listOf(m,k,n,tile).withIndex())setWord("shape",value,offset=index*4)
    setWord("method",method);setWord("completed",-1)
    setRegister("sp",0x7fff0000L)
    val full=connectDataCache(Options(16,4,Placement.FULL,16),logAccesses=true,logLimit=1000000)
    val direct=connectDataCache(Options(16,4))
    val two=connectDataCache(Options(16,4,Placement.SET_ASSOCIATIVE,2))
    val first=readDataAddress("left");val second=readDataAddress("right")
    val product=readDataAddress("product");val descriptor=readDataAddress("shape")
    val entry=if(method==0)"matmul_naive" else "matmul_tiled"
    val phases=if(launch)listOf("launch")else listOf("cold","warm","replay")
    for(phase in phases) {
        if(phase=="replay"){full.reset();direct.reset();two.reset()}
        val before=listOf(full.snapshot(),direct.snapshot(),two.snapshot())
        val start=if(phase=="warm")before[0].accesses.toInt()else 0
        val execution=traceExecution()
        if(launch)runUntil("matmul_done")else {
            setRegisterToDataAddress("a0","left");setRegisterToDataAddress("a1","right")
            setRegisterToDataAddress("a2","product");setRegisterToDataAddress("a3","shape")
            callFunction(entry,"matmul_done")
        }
        val count=execution.snapshot().instructions;execution.close()
        expectRegister("v0",m*n);expectRegister("sp",0x7fff0000L);expectWord("completed",-1)
        for(i in 0 until m)for(j in 0 until n) {
            val expected=(0 until k).sumOf { q -> ((i*k+q)%11-5).toLong()*((q*n+j)%7-3) }
            expectWord("product",expected.toInt(),offset=(i*n+j)*8)
            expectWord("product",(expected ushr 32).toInt(),offset=(i*n+j)*8+4)
        }
        for(i in m*n until 272) {
            expectWord("product",0x5a5aa5a5,offset=i*8)
            expectWord("product",0x5a5aa5a5,offset=i*8+4)
        }
        val after=listOf(full.snapshot(),direct.snapshot(),two.snapshot())
        val accesses=after[0].accesses-before[0].accesses
        expectCondition(after.indices.all{after[it].accesses-before[it].accesses==accesses},"All placements observe the same native notices")
        val log=full.recordedAccesses().drop(start)
        expectCondition(log.size.toLong()==accesses && log.all{it.length==4},"Complete native word-notice log")
        fun inside(address:Long,base:Long,bytes:Int)=address>=base && address<base+bytes
        val groups=linkedMapOf("left" to 0,"right" to 0,"productRead" to 0,"productWrite" to 0,"descriptor" to 0,"stack" to 0,"other" to 0)
        for(a in log) {
            val group=when {
                inside(a.address,first,m*k*4)->"left"
                inside(a.address,second,k*n*4)->"right"
                inside(a.address,product,m*n*8)->if(a.write)"productWrite"else"productRead"
                inside(a.address,descriptor,16)->"descriptor"
                a.address in 0x7ffeffb0L until 0x7fff0000L->"stack"
                else->"other"
            }
            groups[group]=groups.getValue(group)+1
        }
        val cells=m*n;val nonempty=m>0 && n>0
        val segments=if(k==0)0 else (k+tile-1)/tile
        val expected=40L+if(nonempty)2L*cells+2L*cells*k+(if(method==1)4L*cells*segments else 0L)else 0L
        expectCondition(accesses==expected+(if(launch)1 else 0),"Exact notice accounting for these implementations and scope")
        expectCondition(groups.getValue("stack")==36 && groups.getValue("descriptor")==4,"Include full frame/descriptor overhead")
        expectCondition(groups.getValue("other")==if(launch)1 else 0,"Only launch scope reads method")
        println("MEASURE|$m|$k|$n|$tile|$method|$phase|$count|$accesses|${after[0].misses-before[0].misses}|${after[1].misses-before[1].misses}|${after[2].misses-before[2].misses}|${groups.values.joinToString("|")}")
    }
    full.close();direct.close();two.close();expectOutput("")
}
"""
    )
    command = [str(java), "-jar", str(cli), "grade", str(checks), str(source)]
    result = subprocess.run(command, capture_output=True, text=True, timeout=300)
    (output / "stdout.txt").write_text(result.stdout)
    (output / "stderr.txt").write_text(result.stderr)
    keys = [
        "m",
        "k",
        "n",
        "tile",
        "method",
        "scope",
        "instructions",
        "accesses",
        "fullMisses",
        "directMisses",
        "twoWayMisses",
        "leftNotices",
        "rightNotices",
        "productReadNotices",
        "productWriteNotices",
        "descriptorNotices",
        "stackNotices",
        "otherNotices",
    ]
    rows = []
    for line in result.stdout.splitlines():
        if line.startswith("MEASURE|"):
            values = line.split("|")[1:]
            rows.append(
                {
                    key: value if key == "scope" else int(value)
                    for key, value in zip(keys, values)
                }
            )
    if (
        result.returncode
        or len(rows) != 168
        or sum(line.startswith("PASS ") for line in result.stdout.splitlines()) != 84
    ):
        raise AssertionError(
            (result.returncode, result.stdout[-2000:], result.stderr[-2000:])
        )
    for row in rows:
        if row["scope"] == "replay":
            cold = next(
                x
                for x in rows
                if x["scope"] == "cold"
                and all(x[k] == row[k] for k in ("m", "k", "n", "tile", "method"))
            )
            assert all(
                row[k] == cold[k] for k in keys if k != "scope"
            ), "Reset must reproduce the cold measurement"
    sha = lambda f: hashlib.sha256(f.read_bytes()).hexdigest()
    receipt = dict(
        sourceSha256=sha(source),
        cliSha256=sha(cli),
        toolSha256=sha(Path(__file__)),
        command=command,
        rows=rows,
        scope="Kernel cold/warm/reset replay measures entry through return before caller store; launch starts at main through that same boundary. Completed instructions and native data notices include frame/descriptor traffic. Cold equal-capacity sixteen-line four-word LRU full/direct/two-way caches. External preparation/inspection, assembly, startup host time, instruction fetch and hardware cycles excluded. Warm means a second call in this same stopped session without reset. Exact counts are properties of this source, not free-writing grading requirements.",
    )
    (output / "measurements.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(
        json.dumps(
            [
                x
                for x in rows
                if x["scope"] == "cold"
                and (x["m"], x["k"], x["n"]) in ((2, 3, 2), (16, 16, 16), (15, 13, 11))
            ],
            indent=2,
        )
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "cli", "output"):
        parser.add_argument("--" + name, type=Path, required=True)
    args = parser.parse_args()
    measure(args.java.resolve(), args.cli.resolve(), args.output.resolve())
