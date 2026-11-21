#!/usr/bin/env python3
"""Qualify the literal sprite composition through bounded native execution traces."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'revision/main/arrays-and-images.tex'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify(java,cli,output,alternatives):
    raw=SOURCE.read_bytes()
    blocks=re.findall(r'\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}',raw.decode(),re.S)
    leaf='reverse_byte:\n'+(ROOT/'revision/labs/files/reverse-rows.asm').read_text().split('reverse_byte:\n',1)[1]
    if len(blocks)!=3:raise ValueError('Expected data, row function and main listings')
    listing='\n'.join((blocks[0],blocks[2],blocks[1],leaf))
    if listing!=(ROOT/'revision/labs/files/sprite-rows.asm').read_text():raise ValueError('Download differs from literal composition and previous helper')
    output.mkdir(parents=True,exist_ok=False)
    (output/'chapter-source.tex').write_bytes(raw)
    checks=output/'checks.kts'
    checks.write_text(r'''
fun reverse(byte: Int): Int = Integer.reverse(byte) ushr 24
val fixtures=(0..255).map { byte -> List(8) { byte } } +
    (0..255).map { seed -> List(8) { row -> (seed+37*row) and 255 } } +
    listOf(List(8) { 128 ushr it },List(8) { 1 shl it },
        listOf(0x16,0x3c,0x7e,0xdb,0xff,0x24,0x42,0x81))
for ((number,rows) in fixtures.withIndex()) {
    case("sprite $number",instructionLimit=5000) {
        for (group in 0..1) {
            var word=0
            for (byte in 0..3) word=word or (rows[group*4+byte] shl (8*byte))
            setWord("sprite",word,offset=4*group)
        }
        for (cell in 0 until 128) setWord("frame",0x12340000+cell,offset=4*cell)
        val frameGuard=0x13579bdf xor number
        val rowGuard=0x2468ace0 xor number
        setWord("frame_guard",frameGuard)
        setWord("rows_guard",rowGuard)
        val frame=readDataAddress("frame")
        val source=readDataAddress("sprite")
        val mirror=readDataAddress("mirrored_rows")
        val pixels=traceMemoryAccesses("frame",516)
        val reads=traceMemoryAccesses("sprite",8)
        val reversed=traceMemoryAccesses("mirrored_rows",12)
        runToExit()
        for (y in 0 until 8) for (x in 0 until 16) {
            val bit=if (x<8) 7-x else x-8
            val expected=if ((rows[y] and (1 shl bit))!=0) 0xffffff else 0
            expectWord("frame",expected,offset=4*(y*16+x))
        }
        expectBytes("sprite",*rows.toIntArray())
        expectBytes("mirrored_rows",*rows.map(::reverse).toIntArray())
        expectWord("frame_guard",frameGuard)
        expectWord("rows_guard",rowGuard)
        val writes=pixels.snapshot().filter { it.write }
        expectCondition(writes.size==128 && writes.all { it.length==4 && it.address>=frame && it.address<=frame+508 && (it.address-frame)%4==0L } &&
            writes.map { it.address }.toSet().size==128,"Expected one in-bounds word store per framebuffer cell")
        val accesses=reads.snapshot()
        expectCondition(accesses.size==8 && accesses.all { !it.write && it.length==1 } &&
            accesses.map { it.address }==(0..7).map { source+it },"Expected eight bounded sequential source byte reads")
        val byteWrites=reversed.snapshot()
        expectCondition(byteWrites.size==8 && byteWrites.all { it.write && it.length==1 } &&
            byteWrites.map { it.address }==(0..7).map { mirror+it },"Expected eight bounded mirrored-byte stores")
        expectRegister("sp",0x7fffeff0L)
        expectOutput("")
    }
}
case("row helper: all low bytes, upper bits and valid destination offsets",instructionLimit=200000) {
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val kept=(0..7).associate { "s$it" to (0x1122334400000000L+it) } +
        mapOf("fp" to 0x123456789abcdef0L,"gp" to 0x23456789abcdef01L)
    kept.forEach { (reg,value) -> setRegister(reg,value) }
    for (upper in listOf(0L,0x1234567800000000L,-256L)) for (byte in 0..255) {
        val start=when(upper) { 0L -> 0; -256L -> 64; else -> 32 }
        for (cell in 0 until 128) setWord("frame",0x12340000+cell,offset=4*cell)
        setRegister("a0",upper or byte.toLong())
        setRegisterToDataAddress("a1","frame",offset=start.toLong())
        callFunction("draw_row",returnTo="frame_complete")
        for (cell in 0 until 128) {
            val local=cell-start/4
            val expected=if (local !in 0..7) 0x12340000+cell else if ((byte and (128 ushr local))!=0) 0xffffff else 0
            expectWord("frame",expected,offset=4*cell)
        }
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        expectRegister("sp",top)
    }
    expectOutput("")
}
''')
    runs=[]
    def grade(name,program,accepted):
        path=output/(name+'.asm');path.write_text(program)
        command=[str(java),'-jar',str(cli),'grade',str(checks),str(path)]
        result=subprocess.run(command,capture_output=True,text=True,timeout=150)
        (output/(name+'.stdout')).write_text(result.stdout);(output/(name+'.stderr')).write_text(result.stderr)
        passed=sum(l.startswith('PASS ') for l in result.stdout.splitlines());failed=sum(l.startswith('FAIL ') for l in result.stdout.splitlines())
        if passed+failed!=516 or (accepted and (result.returncode or failed)) or (not accepted and (result.returncode==0 or not failed)):
            raise AssertionError((name,result.returncode,passed,failed,result.stderr[-2000:]))
        runs.append(dict(name=name,accepted=accepted,passed=passed,failed=failed,command=command,sourceSha256=sha(path)))
    grade('literal',listing,True)
    for index,p in enumerate(alternatives):grade('alternative-'+str(index+1),p.read_text(),True)
    faults={
        'half-row-stride':listing.replace('daddiu $s2, $s2, 64','daddiu $s2, $s2, 32'),
        'byte-pixel-stride':listing.replace('daddiu $a1, $a1, 4','daddiu $a1, $a1, 1'),
        'wrong-right-offset':listing.replace('daddiu $a1, $s2, 32','daddiu $a1, $s2, 8'),
        'stale-color':listing.replace('move $t3, $zero','nop'),
        'nine-source-rows':listing.replace('li $s3, 8','li $s3, 9'),
        'wide-mirrored-store':listing.replace('sb $v0, 0($s1)','sw $v0, 0($s1)'),
        'unmirrored-right-image':listing.replace('move $a0, $v0','move $a0, $s4'),
    }
    for name,program in faults.items():
        if listing==program:raise ValueError('Mutation did not apply: '+name)
        grade(name,program,False)
    (output/'receipt.json').write_text(json.dumps(dict(chapterSha256=hashlib.sha256(raw).hexdigest(),listingSha256=hashlib.sha256(listing.encode()).hexdigest(),
        inheritedHelperSha256=hashlib.sha256(leaf.encode()).hexdigest(),cliSha256=sha(cli),checkerSha256=sha(Path(__file__)),
        perImplementation=dict(wholeSprites=515,rowHelperCalls=768,reportedCases=516),runs=runs,
        scope='Literal/equivalent programs, relevant access histories, final frame/source/mirror/guards, row-helper preservation and seven faults. Author qualification is stricter than the public independent final-state contract; not a whole-address-space audit.'),indent=2)+'\n')
    print('Passed literal and',len(alternatives),'alternatives: 515 sprite fixtures and 768 row-helper calls each; seven faults rejected.')

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__)
    for name in ('java','cli','output'):p.add_argument('--'+name,type=Path,required=True)
    p.add_argument('--alternative',type=Path,action='append',default=[])
    a=p.parse_args();verify(a.java.resolve(),a.cli.resolve(),a.output.resolve(),[p.resolve() for p in a.alternative])
