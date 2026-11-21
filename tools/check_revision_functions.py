#!/usr/bin/env python3
"""Execute the chapter's literal function composition and equivalent implementations."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT/'revision/main/functions-and-stack.tex'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify(java, cli, output, alternatives):
    raw = SOURCE.read_bytes()
    blocks = re.findall(r'\\begin\{minted\}\{mars-idea\}\n(.*?)\\end\{minted\}', raw.decode(), re.S)
    if len(blocks) != 3:
        raise ValueError('Expected leaf, pair and driver listings')
    listing = blocks[2]+'\n'+blocks[1]+'\n'+blocks[0]
    if listing != (ROOT/'revision/labs/files/reverse-rows.asm').read_text():
        raise ValueError('Downloadable program differs from the literal chapter composition')
    output.mkdir(parents=True, exist_ok=False)
    (output/'chapter-source.tex').write_bytes(raw)
    assignment = r'''
fun reverse(value: Long): Long = (Integer.reverse(value.toInt() and 255) ushr 24).toLong()
val kept = (0..7).associate { "s$it" to (0x1122334400000000L + it * 0x01010101L) } +
    mapOf("fp" to 0x0123456789abcdefL, "gp" to 0x023456789abcdef0L)
case("leaf: all bytes with three upper-bit patterns", instructionLimit=100000) {
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    kept.forEach { (reg, value) -> setRegister(reg, value) }
    val dataWrites = traceMemoryStores("input_rows",20)
    val stackWrites = traceMemoryStores(top-128,160)
    for (upper in listOf(0L,0x1234567800000000L,-256L)) for (byte in 0..255) {
        setRegister("a0",upper or byte.toLong())
        setRegister("v0",-1L)
        setRegister("t1",-1L)
        callFunction("reverse_byte",returnTo="after_pair")
        expectRegister("v0",reverse(byte.toLong()))
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
    }
    dataWrites.expectNone()
    stackWrites.expectNone()
    expectOutput("")
}
case("pair: byte coverage, edge pairs and full-width preservation", instructionLimit=100000) {
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    kept.forEach { (reg,value) -> setRegister(reg,value) }
    val callerBytes=readBytes(top,32)
    val dataWrites=traceMemoryStores("input_rows",20)
    val edges=listOf(0L,1L,2L,0x16L,0x68L,0x80L,0x81L,0xfeL,0xffL)
    val pairs=(0..255).map { it.toLong() to ((73*it+19) and 255).toLong() } +
        edges.flatMap { a -> edges.map { b -> a to b } } +
        listOf(-1L to Long.MIN_VALUE, Long.MAX_VALUE to -256L,
            0x1122334455667716L to 0x123456789abcde81L, Long.MIN_VALUE to Long.MAX_VALUE)
    for ((a,b) in pairs) {
        setRegister("a0",a)
        setRegister("a1",b)
        callFunction("reverse_pair",returnTo="after_pair")
        expectRegister("v0",reverse(a))
        expectRegister("v1",reverse(b))
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        expectCondition(readBytes(top,32).contentEquals(callerBytes),"Caller-owned stack bytes changed")
    }
    dataWrites.expectNone()
    expectOutput("")
}
for (byte in 0..255) {
    case("complete driver byte $byte",instructionLimit=1000) {
        val second=(73*byte+19) and 255
        val a=byte or 0xa5a50000.toInt()
        val b=second or 0x5a5a0000
        setWord("input_rows",a)
        setWord("input_rows",b,offset=4)
        setRegister("s1",0x123456789abcdef0L)
        runToExit()
        expectWord("input_rows",a)
        expectWord("input_rows",b,offset=4)
        expectWord("results",reverse(a.toLong()).toInt())
        expectWord("results",reverse(b.toLong()).toInt(),offset=4)
        expectWord("rows_guard",0x13579bdf)
        expectRegister("s0",readDataAddress("results"))
        expectRegister("s1",0x123456789abcdef0L)
        expectRegister("sp",0x7fffeff0L)
        expectOutput("")
    }
}
'''
    checks=output/'checks.kts';checks.write_text(assignment)
    runs=[]

    def grade(name, program, accepted):
        path=output/(name+'.asm');path.write_text(program)
        command=[str(java),'-jar',str(cli),'grade',str(checks),str(path)]
        result=subprocess.run(command,capture_output=True,text=True,timeout=120)
        (output/(name+'.stdout')).write_text(result.stdout)
        (output/(name+'.stderr')).write_text(result.stderr)
        passed=sum(l.startswith('PASS ') for l in result.stdout.splitlines())
        failed=sum(l.startswith('FAIL ') for l in result.stdout.splitlines())
        if passed+failed != 258 or (accepted and (result.returncode or failed)) or (not accepted and (result.returncode==0 or not failed)):
            raise AssertionError((name,result.returncode,passed,failed,result.stderr[-2000:]))
        runs.append(dict(name=name,expectedSuccess=accepted,passed=passed,failed=failed,
                         sourceSha256=sha(path),command=command,exitCode=result.returncode))
    grade('literal',listing,True)
    for index,alternative in enumerate(alternatives):
        grade('alternative-'+str(index+1),alternative.read_text(),True)
    faults={
        'missing-link-save':listing.replace('sd $ra, 24($sp)','nop'),
        'missing-link-restore':listing.replace('ld $ra, 24($sp)','nop'),
        'missing-result-pointer-restore':listing.replace('ld $s0, 16($sp)','nop'),
        'first-result-not-retained':listing.replace('move $s1, $v0','nop'),
        'word-save-truncates-register':listing.replace('sd $s1, 8($sp)','sw $s1, 8($sp)').replace('ld $s1, 8($sp)','lw $s1, 8($sp)'),
        'wrong-frame-release':listing.replace('daddiu $sp, $sp, 32','daddiu $sp, $sp, 16'),
        'seven-bit-reversal':listing.replace('li $t1, 8','li $t1, 7'),
    }
    for name,program in faults.items():
        if program==listing:raise ValueError('Mutation was not applied: '+name)
        grade(name,program,False)
    receipt=dict(chapterSha256=hashlib.sha256(raw).hexdigest(),listingSha256=hashlib.sha256(listing.encode()).hexdigest(),
                 verifierSha256=sha(Path(__file__)),cliSha256=sha(cli),runs=runs,
                 perImplementation=dict(leafInvocations=768,pairInvocations=341,completeDriverCases=256,reportedCases=258),
                 scope='Literal composition, public function contracts, full-width preservation, observed caller stack/data guards and seven faults. Not a whole-address-space write audit, native IDE observation, or an exact implementation requirement for student work.')
    (output/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print('Passed literal and',len(alternatives),'alternatives; 768 leaf calls, 341 pair calls and 256 complete-driver cases each; seven faults rejected.')


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__)
    for name in ('java','cli','output'):p.add_argument('--'+name,type=Path,required=True)
    p.add_argument('--alternative',type=Path,action='append',default=[])
    a=p.parse_args();verify(a.java.resolve(),a.cli.resolve(),a.output.resolve(),[p.resolve() for p in a.alternative])
