// Completed bounded search: mathematical result, boundary accesses and calling convention.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class SearchInput(val x:Long=16,val y:Long=32,val radius:Long=2,val stride:Long=176,val referenceStride:Long=176,val pattern:String="shift",val dx:Int=-2,val dy:Int=-2,val bad:String="",val alias:Boolean=false)
fun AssemblyCase.put(name:String,bytes:ByteArray,little:Boolean){
    for(i in bytes.indices step 4){var value=0;for(j in 0..3)value=value or((bytes[i+j].toInt()and 255)shl(8*(if(little)j else 3-j)));if(value!=-1515870811)setWord(name,value,i)}
}
fun AssemblyCase.put64(name:String,offset:Int,value:Long,little:Boolean){
    setWord(name,(if(little)value else value ushr 32).toInt(),offset)
    setWord(name,(if(little)value ushr 32 else value).toInt(),offset+4)
}
fun AssemblyCase.read64(name:String,offset:Int,little:Boolean):Long{
    val bytes=readBytes(name,8,offset);var result=0L
    for(i in bytes.indices)result=result or(bytes[i].toLong()shl(8*(if(little)i else 7-i)))
    return result
}
fun AssemblyCase.checkSearch(c:SearchInput,mode:Int,little:Boolean){
    val current=ByteArray(61488){165.toByte()};val reference=ByteArray(585952){165.toByte()}
    val good=c.bad.isEmpty()
    if(good){
        for(y in 0..143)for(x in 0..175){
            val sample=when(c.pattern){"flat"->73;"maximum"->255;"checker"->255*((x+y)%2);else->(37*x+19*y+(x*y)%251)%256}
            reference[31+y*c.referenceStride.toInt()+x]=sample.toByte()
        }
        for(y in 0..15)for(x in 0..15){
            val sample=when(c.pattern){"maximum"->0;"checker"->255*((x+y+1)%2);else->reference[31+(c.y.toInt()+c.dy+y)*c.referenceStride.toInt()+c.x.toInt()+c.dx+x].toInt()and 255}
            current[17+y*c.stride.toInt()+x]=sample.toByte()
        }
    }
    put("h263Motion_current",current,little);put("h263Motion_reference",reference,little)
    val refBase=readDataAddress("h263Motion_reference")+31
    val currentBase=if(c.alias)refBase else readDataAddress("h263Motion_current")+17
    val fields=listOf(if(c.bad=="current-wrap")-8L else currentBase,if(c.bad=="reference-wrap")-8L else refBase,c.stride,c.referenceStride,c.x,c.y,c.radius,if(c.bad=="mode")2L else mode.toLong())
    for((index,value)in fields.withIndex())put64("h263Motion_request",8*index,value,little)
    val request=readBytes("h263Motion_request",64)
    val outputBefore=readBytes("h263Motion_output",56)
    val saved=(0..7).map{"s"+it}+listOf("fp","gp")
    for((index,name)in saved.withIndex())setRegister(name,0x1234567800000000L+index)
    setRegister("sp",readDataAddress("h263Motion_stack",16400));val stack=readRegister("sp")
    val readsA=traceMemoryAccesses(readDataAddress("h263Motion_current"),61488)
    val readsB=traceMemoryAccesses(readDataAddress("h263Motion_reference"),585952)
    val writes=traceMemoryAccesses(readDataAddress("h263Motion_output"),56)
    val work=traceExecution()
    val requestPointer=when(c.bad){"request-align"->readDataAddress("h263Motion_request")+1;"request-wrap"->-8L;else->readDataAddress("h263Motion_request")}
    val outputPointer=when(c.bad){"output-align"->readDataAddress("h263Motion_output")+17;"output-wrap"->-8L;else->readDataAddress("h263Motion_output")+16}
    setRegister("a0",requestPointer);setRegister("a1",outputPointer)
    callFunction("h263_search16","h263Motion_return")
    expectRegister("v0",if(good)0 else 1)
    var lowX=0;var highX=0;var lowY=0;var highY=0
    if(good){
        lowX=max(-c.radius.toInt(),-c.x.toInt());highX=min(c.radius.toInt(),160-c.x.toInt())
        lowY=max(-c.radius.toInt(),-c.y.toInt());highY=min(c.radius.toInt(),128-c.y.toInt())
        // Score every admitted window directly; rank the centre first, then (dy,dx).
        val candidates=(lowY..highY).flatMap{dy->(lowX..highX).map{dx->Pair(dx,dy)}}
        fun score(p:Pair<Int,Int>):Long=(0..15).sumOf{y->(0..15).sumOf{x->
            val a=if(c.alias)reference[31+y*c.stride.toInt()+x]else current[17+y*c.stride.toInt()+x]
            val b=reference[31+(c.y.toInt()+p.second+y)*c.referenceStride.toInt()+c.x.toInt()+p.first+x]
            abs((a.toInt()and 255)-(b.toInt()and 255)).toLong()}}
        val selected=candidates.minWith(compareBy<Pair<Int,Int>>{score(it)}.thenBy{if(it.first==0&&it.second==0)0 else 1}.thenBy{it.second}.thenBy{it.first})
        expectRegister("v1",candidates.size)
        expectCondition(read64("h263Motion_output",16,little)==selected.first.toLong(),"Signed horizontal displacement")
        expectCondition(read64("h263Motion_output",24,little)==selected.second.toLong(),"Signed vertical displacement")
        expectCondition(read64("h263Motion_output",32,little)==score(selected),"Exact best SAD")
        println("MOTION_WORK mode=$mode x=${c.x} y=${c.y} radius=${c.radius} pattern=${c.pattern} count=${candidates.size} dx=${selected.first} dy=${selected.second} sad=${score(selected)} instructions=${work.snapshot().instructions}")
    }else{expectRegister("v1",0);expectCondition(readBytes("h263Motion_output",56).contentEquals(outputBefore),"Failure leaves output unpublished")}
    fun inside(address:Long,count:Int,start:Long,stride:Long,width:Int,height:Int):Boolean{
        if(address<start||stride<=0)return false
        val relative=address-start;return relative/stride<height&&relative%stride+count<=width
    }
    val searchStart=refBase+(c.y+lowY)*c.referenceStride+c.x+lowX
    for(event in readsA.snapshot()+readsB.snapshot())expectCondition(good&&!event.write&&(inside(event.address,event.length,currentBase,c.stride,16,16)||inside(event.address,event.length,searchStart,c.referenceStride,16+highX-lowX,16+highY-lowY)),"Read only current samples or the admitted reference union; no input writes")
    for(event in writes.snapshot())expectCondition(!event.write||(good&&event.address>=outputPointer&&event.address+event.length<=outputPointer+24),"Publish only the 24-byte successful result")
    expectCondition(readBytes("h263Motion_current",61488).contentEquals(current.map{it.toInt()and 255}.toIntArray()),"Current source/guards unchanged")
    expectCondition(readBytes("h263Motion_reference",585952).contentEquals(reference.map{it.toInt()and 255}.toIntArray()),"Reference source/guards unchanged")
    expectCondition(readBytes("h263Motion_request",64).contentEquals(request),"Request unchanged")
    expectCondition(readBytes("h263Motion_output",16).contentEquals(outputBefore.copyOfRange(0,16))&&readBytes("h263Motion_output",16,40).contentEquals(outputBefore.copyOfRange(40,56)),"Output outer guards retained")
    expectCondition(readBytes("h263Motion_stack",16).all{it==165}&&readBytes("h263Motion_stack",16,16400).all{it==165},"Private stack outer guards retained")
    for((index,name)in saved.withIndex())expectRegister(name,0x1234567800000000L+index)
    expectRegister("sp",stack)
}
case("Search flat mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(pattern="flat",dx=0,dy=0),0,false)}
case("Search flat mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(pattern="flat",dx=0,dy=0),1,false)}
case("Search flat mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(pattern="flat",dx=0,dy=0),0,true)}
case("Search flat mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(pattern="flat",dx=0,dy=0),1,true)}
case("Search maximum mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(pattern="maximum",dx=0,dy=0),0,false)}
case("Search maximum mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(pattern="maximum",dx=0,dy=0),1,false)}
case("Search maximum mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(pattern="maximum",dx=0,dy=0),0,true)}
case("Search maximum mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(pattern="maximum",dx=0,dy=0),1,true)}
case("Search radius-zero mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(radius=0,dx=0,dy=0),0,false)}
case("Search radius-zero mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(radius=0,dx=0,dy=0),1,false)}
case("Search radius-zero mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(radius=0,dx=0,dy=0),0,true)}
case("Search radius-zero mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(radius=0,dx=0,dy=0),1,true)}
case("Search tie-away-centre mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(pattern="checker",dx=0,dy=0),0,false)}
case("Search tie-away-centre mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(pattern="checker",dx=0,dy=0),1,false)}
case("Search tie-away-centre mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(pattern="checker",dx=0,dy=0),0,true)}
case("Search tie-away-centre mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(pattern="checker",dx=0,dy=0),1,true)}
case("Search move--2--2 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=-2,dy=-2),0,false)}
case("Search move--2--2 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=-2,dy=-2),1,false)}
case("Search move--2--2 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=-2,dy=-2),0,true)}
case("Search move--2--2 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=-2,dy=-2),1,true)}
case("Search move-2--2 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=2,dy=-2),0,false)}
case("Search move-2--2 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=2,dy=-2),1,false)}
case("Search move-2--2 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=2,dy=-2),0,true)}
case("Search move-2--2 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=2,dy=-2),1,true)}
case("Search move--2-2 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=-2,dy=2),0,false)}
case("Search move--2-2 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=-2,dy=2),1,false)}
case("Search move--2-2 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=-2,dy=2),0,true)}
case("Search move--2-2 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=-2,dy=2),1,true)}
case("Search move-2-2 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=2,dy=2),0,false)}
case("Search move-2-2 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(dx=2,dy=2),1,false)}
case("Search move-2-2 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=2,dy=2),0,true)}
case("Search move-2-2 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(dx=2,dy=2),1,true)}
case("Search corner-0-0 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=0,y=0,dx=2,dy=2),0,false)}
case("Search corner-0-0 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=0,y=0,dx=2,dy=2),1,false)}
case("Search corner-0-0 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=0,y=0,dx=2,dy=2),0,true)}
case("Search corner-0-0 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=0,y=0,dx=2,dy=2),1,true)}
case("Search corner-160-128 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=160,y=128,dx=-2,dy=-2),0,false)}
case("Search corner-160-128 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=160,y=128,dx=-2,dy=-2),1,false)}
case("Search corner-160-128 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=160,y=128,dx=-2,dy=-2),0,true)}
case("Search corner-160-128 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=160,y=128,dx=-2,dy=-2),1,true)}
case("Search corner-0-128 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=0,y=128,dx=2,dy=-2),0,false)}
case("Search corner-0-128 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=0,y=128,dx=2,dy=-2),1,false)}
case("Search corner-0-128 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=0,y=128,dx=2,dy=-2),0,true)}
case("Search corner-0-128 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=0,y=128,dx=2,dy=-2),1,true)}
case("Search corner-160-0 mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=160,y=0,dx=-2,dy=2),0,false)}
case("Search corner-160-0 mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(x=160,y=0,dx=-2,dy=2),1,false)}
case("Search corner-160-0 mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=160,y=0,dx=-2,dy=2),0,true)}
case("Search corner-160-0 mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(x=160,y=0,dx=-2,dy=2),1,true)}
case("Search bad-stride mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(stride=15,bad="stride"),0,false)}
case("Search bad-stride mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(stride=15,bad="stride"),1,false)}
case("Search bad-stride mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(stride=15,bad="stride"),0,true)}
case("Search bad-stride mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(stride=15,bad="stride"),1,true)}
case("Search bad-mode mode=0 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(bad="mode"),0,false)}
case("Search bad-mode mode=1 little=False",instructionLimit=200000,byteOrder=ByteOrder.BIG_ENDIAN){checkSearch(SearchInput(bad="mode"),1,false)}
case("Search bad-mode mode=0 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(bad="mode"),0,true)}
case("Search bad-mode mode=1 little=True",instructionLimit=200000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkSearch(SearchInput(bad="mode"),1,true)}
