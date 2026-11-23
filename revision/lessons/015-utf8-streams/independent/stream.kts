import edu.missouristate.mars.cli.grading.AssemblyCase
import java.lang.Character
import java.nio.charset.StandardCharsets.UTF_8

fun widthOf(first:Int)=when {
    first and 0xe0==0xc0 -> 2
    first and 0xf0==0xe0 -> 3
    first and 0xf8==0xf0 -> 4
    else -> 0
}
fun prefixValue(bytes:List<Int>,width:Int):Int {
    var value=bytes.first() and ((1 shl (7-width))-1)
    for(byte in bytes.drop(1))value=(value shl 6) or (byte and 63)
    return value
}
fun possible(bytes:List<Int>,width:Int):Boolean {
    val value=prefixValue(bytes,width);val bits=6*(width-bytes.size)
    val lower=maxOf(value shl bits,when(width){2->0x80;3->0x800;else->0x10000})
    val upper=minOf(((value+1) shl bits)-1,when(width){2->0x7ff;3->0xffff;else->0x10ffff})
    return lower<=upper && (lower<0xd800 || upper>0xdfff)
}
class Model {
    var total=0;var count=0;var width=0;var start=-1;var status=0;var error=-1
    val pending=mutableListOf<Int>()
    fun fields():List<Int> {
        val remaining=if(pending.isEmpty())0 else width-pending.size
        val candidates=if(pending.isEmpty())listOf(128,191) else (128..191).filter{possible(pending+it,width)}
        return listOf(total,count,remaining,candidates.first(),candidates.last(),start,status,error)
    }
    fun feed(bytes:List<Int>,final:Boolean):Pair<Int,Int> {
        if(status!=0)return -3 to 0
        var used=0
        for(byte in bytes) {
            if(pending.isEmpty()) {
                if(byte<128) {count++;total++;used++;continue}
                val candidateWidth=widthOf(byte)
                start=total
                if(candidateWidth==0 || !possible(listOf(byte),candidateWidth)) {status=-1;error=total;break}
                width=candidateWidth;pending+=byte
            } else {
                if(byte !in 128..191 || !possible(pending+byte,width)) {status=-1;error=total;break}
                pending+=byte
                if(pending.size==width){count++;pending.clear();width=0;start=-1}
            }
            total++;used++
        }
        if(status==0 && final) {
            status=if(pending.isEmpty())1 else -2
            if(status==-2)error=total
        }
        return status to used
    }
}
data class Stream(val name:String,val prefix:List<Int>,val chunks:List<List<Int>>,val final:Boolean=true,val finalIndex:Int=-1,val prefixFinal:Boolean=false) {
    fun isFinal(index:Int)=if(finalIndex>=0)index==finalIndex else final && index==chunks.lastIndex
}
fun encode(cp:Int)=String(Character.toChars(cp)).toByteArray(UTF_8).map{it.toInt() and 255}
fun AssemblyCase.writeInput(bytes:List<Int>,offset:Int,salt:Int):List<Int> {
    val arena=MutableList(96){(it*37+salt*17+19) and 255}
    for((i,byte) in bytes.withIndex())arena[offset+i]=byte
    for(i in 0 until 24)setWord("input",(0..3).fold(0){w,k->w or (arena[i*4+k] shl (k*8))},offset=i*4)
    return arena
}
fun AssemblyCase.verifyStream(stream:Stream,index:Int) {
    val model=Model();model.feed(stream.prefix,stream.prefixFinal)
    val target=if(index%2==0)"state_a" else "state_b"
    for((i,value) in model.fields().withIndex())setWord(target,value,offset=i*4)
    val continuation=if(index%3==0)"function_return" else "done"
    val avoided=traceExecution("function_return","done")
    for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1013)
    setRegister("sp",0x7fff0000L)
    for((chunk,bytes) in stream.chunks.withIndex()) {
        val offset=(index+chunk)%8;val before=writeInput(bytes,offset,index+chunk)
        val wasTerminal=model.status!=0;val result=model.feed(bytes,stream.isFinal(chunk))
        val writes=traceMemoryAccesses("state_a",(readDataAddress("state_end")+4-readDataAddress("state_a")).toInt(),maxAccesses=100_000)
        val reads=traceMemoryAccesses("input_guard",104,maxAccesses=100_000)
        for(i in 0..9)setRegister("t$i",0x456789ab00000000L+index*131L+chunk*31L+i)
        setRegister("v0",777);setRegister("v1",888)
        setRegisterToDataAddress("a0",target);setRegisterToDataAddress("a1","input",offset.toLong());setRegister("a2",bytes.size);setRegister("a3",if(stream.isFinal(chunk))1 else 0)
        callFunction("scan_utf8",continuation)
        expectRegister("v0",result.first);expectRegister("v1",result.second)
        for((i,value) in model.fields().withIndex())expectWord(target,value,offset=i*4)
        expectBytes("input",*before.toIntArray())
        reads.expectReadsWithin(readDataAddress("input")+offset,if(wasTerminal)0 else bytes.size)
        for(access in writes.snapshot().filter{it.write}) {
            val base=readDataAddress(target)
            expectCondition(!wasTerminal && access.address>=base && access.length<=32 && access.address-base<=32-access.length,"Only the active call's selected state may be written")
        }
        expectCondition(avoided.snapshot().instructions==0L,"Return through the supplied continuation")
        for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1013)
        expectRegister("sp",0x7fff0000L)
        reads.close();writes.close()
    }
    val frozen=traceMemoryAccesses("state_a",(readDataAddress("state_end")+4-readDataAddress("state_a")).toInt())
    runToExit();expectCondition(frozen.snapshot().none{it.write},"No caller-data writes after function return");expectOutput("")
}
val streams=mutableListOf<Stream>()
val points=listOf(0,0x41,0x7f,0x80,0x7ff,0x800,0xd7ff,0xe000,0xffff,0x10000,0x10ffff,0x301,0xfeff,0x1f642)
for(cp in points) {
    val bytes=encode(cp)
    for(cut in 0..bytes.size)streams+=Stream("scalar $cp cut$cut",emptyList(),listOf(bytes.take(cut),emptyList(),bytes.drop(cut),emptyList()))
}
for(first in 0..255)streams+=Stream("one byte $first",emptyList(),listOf(listOf(first)))
val prefixes=listOf(emptyList(),listOf(0xc2),listOf(0xe0),listOf(0xed),listOf(0xf0),listOf(0xf4),listOf(0xf0,0x90),listOf(0xf4,0x8f,0xbf))
for(prefix in prefixes)for(byte in listOf(0,0x41,0x7f,0x80,0x8f,0x90,0x9f,0xa0,0xbf,0xc0,0xc2,0xff))
    streams+=Stream("prefix $prefix next$byte",listOf(0x41,0)+prefix,listOf(listOf(byte),listOf(0x41),emptyList()))
val fixture=listOf(0x41,0xe2,0x82,0xac,0xf0,0x9f,0x99,0x82,0,0xc3,0xa9)
for(cut in 0..fixture.size)streams+=Stream("mixed cut$cut",emptyList(),listOf(fixture.take(cut),fixture.drop(cut)))
for(prefix in listOf(listOf(0xc0),listOf(0xed,0xa0),listOf(0x41,0x80),listOf(0xf4,0x90)))
    streams+=Stream("latched $prefix",prefix,listOf(fixture,emptyList()))
streams+=Stream("no finalization",emptyList(),listOf(listOf(0xf0),emptyList(),listOf(0x90)),false)
streams+=Stream("full ASCII chunk",emptyList(),listOf((0 until 64).toList(),emptyList()))
streams+=Stream("finished initial state",listOf(0x41),listOf(fixture,emptyList()),prefixFinal=true)
streams+=Stream("truncated initial state",listOf(0xe2,0x82),listOf(fixture,emptyList()),prefixFinal=true)
streams+=Stream("finish then reuse",emptyList(),listOf(listOf(0x41),fixture,emptyList()),finalIndex=0)
streams+=Stream("truncate then reuse",emptyList(),listOf(listOf(0xe0),fixture,emptyList()),finalIndex=0)
streams+=Stream("stream maximum",List(4090){0x41},listOf(encode(0x1f642)+listOf(0,0x41)))
streams+=Stream("pending at maximum",List(4095){0x41},listOf(listOf(0xf0),emptyList()))
for((index,stream) in streams.withIndex())case(stream.name,instructionLimit=50000){verifyStream(stream,index)}
