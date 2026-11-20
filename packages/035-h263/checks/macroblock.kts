// Six-block results are checked sample by sample, with independent plane geometry.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import java.lang.Math.floorDiv
import kotlin.math.abs

data class MacroInput(val x:Long=16,val y:Long=32,val dx:Long=-2,val dy:Long=-2,val layout:String="tight",val pattern:String="mixed",val alias:Boolean=false,val bad:String="")
fun AssemblyCase.put(name:String,bytes:ByteArray,little:Boolean){
    for(i in bytes.indices step 4){var value=0;for(j in 0..3)value=value or((bytes[i+j].toInt()and 255)shl(8*(if(little)j else 3-j)));if(value!=-1515870811)setWord(name,value,i)}
}
fun AssemblyCase.put64(name:String,offset:Int,value:Long,little:Boolean){
    setWord(name,(if(little)value else value ushr 32).toInt(),offset)
    setWord(name,(if(little)value ushr 32 else value).toInt(),offset+4)
}
fun AssemblyCase.checkMacro(c:MacroInput,mode:Int,little:Boolean){
    val success=c.bad.isEmpty();val good=success||c.bad=="dependency"
    val strides=when(c.layout){"wide"->MutableList(6){4096L};"independent"->mutableListOf(193L,101L,107L,211L,113L,127L);"padded"->mutableListOf(192L,96L,104L,208L,112L,120L);else->mutableListOf(176L,88L,88L,176L,88L,88L)}
    val originalStrides=strides.toList()
    val arrays=List(6){index->ByteArray(if(index%3==0)585952 else 290944){165.toByte()}}
    val width=listOf(176,88,88,176,88,88);val height=listOf(144,72,72,144,72,72)
    val qx=floorDiv(c.dx,2L);val qy=floorDiv(c.dy,2L)
    val px=((c.dx%2+2)%2).toInt();val py=((c.dy%2+2)%2).toInt()
    if(good)for(plane in 0..2){
        val cx=(if(plane==0)c.x else c.x/2).toInt();val cy=(if(plane==0)c.y else c.y/2).toInt()
        val rx=(if(plane==0)c.x+c.dx else c.x/2+qx).toInt();val ry=(if(plane==0)c.y+c.dy else c.y/2+qy).toInt()
        val size=if(plane==0)16 else 8
        for(y in 0 until size)for(x in 0 until size){
            val value=when(c.pattern){"positive"->255;"negative"->0;"equal"->73;else->(37*(cx+x)+19*(cy+y)+41*plane+3)%256}
            arrays[plane][31+(cy+y)*strides[plane].toInt()+cx+x]=value.toByte()
        }
        for(y in 0 until size+(if(plane==0)0 else py))for(x in 0 until size+(if(plane==0)0 else px)){
            val value=when(c.pattern){"positive"->0;"negative"->255;"equal"->73;else->(17*(rx+x)+31*(ry+y)+67*plane+11)%256}
            arrays[plane+3][31+(ry+y)*strides[plane+3].toInt()+rx+x]=value.toByte()
        }
    }
    for(index in 0..5)put("h263Macro_plane$index",arrays[index],little)
    val bases=(0..5).map{readDataAddress("h263Macro_plane$it")+31}.toMutableList()
    if(c.alias)for(index in 0..2)bases[index]=bases[index+3]
    when(c.bad){
        "last-stride"->strides[5]=0;"short-y"->strides[0]=175;"short-cb"->strides[1]=87
        "large-cr"->strides[2]=4097;"wide-stride"->strides[3]=4294967472L;"negative-stride"->strides[4]=-1
        "current-wrap"->bases[0]=-16L;"current-cr-wrap"->bases[2]=-16L;"reference-wrap"->bases[3]=-16L;"reference-cr-wrap"->bases[5]=-16L
    }
    val fields=bases+strides+listOf(c.x,c.y,c.dx,c.dy,when(c.bad){"mode"->2L;"wide-mode"->4294967296L;else->mode.toLong()},if(c.bad=="reserved")1L else 0L)
    for((index,value)in fields.withIndex())put64("h263Macro_request",index*8,value,little)
    val beforeRequest=readBytes("h263Macro_request",144);val beforeOutput=readBytes("h263Macro_output",1184)
    val saved=(0..7).map{"s"+it}+listOf("fp","gp");for((i,name)in saved.withIndex())setRegister(name,0x1234567800000000L+i)
    setRegister("sp",readDataAddress("h263Macro_stack",16400));val stack=readRegister("sp")
    val traces=(0..5).map{traceMemoryAccesses(readDataAddress("h263Macro_plane$it"),arrays[it].size)}
    val writes=traceMemoryAccesses(readDataAddress("h263Macro_output"),1184);val work=traceExecution()
    val request=when(c.bad){"request-align"->readDataAddress("h263Macro_request")+1;"request-wrap"->-8L;else->readDataAddress("h263Macro_request")}
    val output=when(c.bad){"output-align"->readDataAddress("h263Macro_output")+24;"output-wrap"->-16L;else->readDataAddress("h263Macro_output")+16}
    setRegister("a0",request);setRegister("a1",output);callFunction("h263_predict_mb","h263Macro_return")
    expectRegister("v0",if(c.bad=="dependency")2 else if(success)0 else 1)
    if(success){
        val actual=readBytes("h263Macro_output",1152,16);var total=0L;val perBlock=mutableListOf<Long>()
        for(block in 0..5){
            val plane=if(block<4)0 else block-3;val sizeX=if(block<4)8*(block%2)else 0;val sizeY=if(block<4)8*(block/2)else 0
            val cx=(if(plane==0)c.x else c.x/2).toInt()+sizeX;val cy=(if(plane==0)c.y else c.y/2).toInt()+sizeY
            val rx=cx+(if(plane==0)c.dx else qx).toInt();val ry=cy+(if(plane==0)c.dy else qy).toInt()
            val phaseX=if(plane==0)0 else px;val phaseY=if(plane==0)0 else py;var sad=0L
            for(y in 0..7)for(x in 0..7){
                val inputArray=if(c.alias)arrays[plane+3]else arrays[plane]
                val input=inputArray[31+(cy+y)*strides[plane].toInt()+cx+x].toInt()and 255
                val neighbours=(0..phaseY).flatMap{v->(0..phaseX).map{u->arrays[plane+3][31+(ry+y+v)*strides[plane+3].toInt()+rx+x+u].toInt()and 255}}
                val predicted=(neighbours.sum()+neighbours.size/2)/neighbours.size;val residual=input-predicted
                val at=block*192+y*8+x;val halfAt=block*192+64+2*(y*8+x)
                val bits=if(little)actual[halfAt]or(actual[halfAt+1]shl 8)else(actual[halfAt]shl 8)or actual[halfAt+1]
                expectCondition(actual[at]==predicted,"Every block uses its own plane, origin and phase")
                expectCondition(bits.toShort().toInt()==residual,"Signed residual at the specified six-block offset")
                expectCondition(actual[at]+bits.toShort().toInt()==input,"Each unquantized output reconstructs its own current sample")
                sad+=abs(residual)
            }
            perBlock.add(sad);total+=sad
        }
        expectRegister("v1",total)
        println("MACRO_WORK mode=$mode x=${c.x} y=${c.y} dx=${c.dx} dy=${c.dy} layout=${c.layout} total=$total blocks=$perBlock instructions=${work.snapshot().instructions}")
    }else{expectRegister("v1",0);expectCondition(readBytes("h263Macro_output",1184).contentEquals(beforeOutput),"No output published for a rejected request or failed dependency")}
    fun fits(address:Long,length:Int,start:Long,stride:Long,w:Int,h:Int):Boolean{
        if(address<start||stride<=0)return false
        return(0 until length).all{i->val at=address-start+i;at/stride<h&&at%stride<w}
    }
    fun permitted(address:Long,length:Int):Boolean{
        for(plane in 0..2){
            val cx=if(plane==0)c.x else c.x/2;val cy=if(plane==0)c.y else c.y/2
            val rx=cx+if(plane==0)c.dx else qx;val ry=cy+if(plane==0)c.dy else qy
            val size=if(plane==0)16 else 8
            if(fits(address,length,bases[plane]+cy*strides[plane]+cx,strides[plane],size,size))return true
            if(fits(address,length,bases[plane+3]+ry*strides[plane+3]+rx,strides[plane+3],size+(if(plane==0)0 else px),size+(if(plane==0)0 else py)))return true
        }
        return false
    }
    for(trace in traces)for(event in trace.snapshot())expectCondition(good&&!event.write&&permitted(event.address,event.length),"Only current macroblock samples and selected reference halos are read")
    for(event in writes.snapshot())expectCondition(!event.write||(success&&event.address>=output&&event.address+event.length<=output+1152),"Only successful result bytes may be written")
    for(index in 0..5){
        val count=31+(height[index]-1)*originalStrides[index].toInt()+width[index]+8
        expectCondition(readBytes("h263Macro_plane$index",count).contentEquals(arrays[index].take(count).map{it.toInt()and 255}.toIntArray()),"Plane $index and its guards remain unchanged")
    }
    expectCondition(readBytes("h263Macro_request",144).contentEquals(beforeRequest),"Request unchanged")
    expectCondition(readBytes("h263Macro_output",16).contentEquals(beforeOutput.copyOfRange(0,16))&&readBytes("h263Macro_output",16,1168).contentEquals(beforeOutput.copyOfRange(1168,1184)),"Output outer guards unchanged")
    expectCondition(readBytes("h263Macro_stack",16).all{it==165}&&readBytes("h263Macro_stack",16,16400).all{it==165},"Private stack outer guards unchanged")
    for((i,name)in saved.withIndex())expectRegister(name,0x1234567800000000L+i);expectRegister("sp",stack)
}
case("Macroblock vector--2--2 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=-2,dy=-2),0,false)}
case("Macroblock vector--2--2 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=-2,dy=-2),1,false)}
case("Macroblock vector--2--2 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=-2,dy=-2),0,true)}
case("Macroblock vector--2--2 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=-2,dy=-2),1,true)}
case("Macroblock vector--1--1 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=-1,dy=-1),0,false)}
case("Macroblock vector--1--1 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=-1,dy=-1),1,false)}
case("Macroblock vector--1--1 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=-1,dy=-1),0,true)}
case("Macroblock vector--1--1 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=-1,dy=-1),1,true)}
case("Macroblock vector-0-0 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=0,dy=0),0,false)}
case("Macroblock vector-0-0 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=0,dy=0),1,false)}
case("Macroblock vector-0-0 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=0,dy=0),0,true)}
case("Macroblock vector-0-0 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=0,dy=0),1,true)}
case("Macroblock vector-1-1 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=1,dy=1),0,false)}
case("Macroblock vector-1-1 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=1,dy=1),1,false)}
case("Macroblock vector-1-1 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=1,dy=1),0,true)}
case("Macroblock vector-1-1 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=1,dy=1),1,true)}
case("Macroblock vector-2-2 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=2,dy=2),0,false)}
case("Macroblock vector-2-2 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=2,dy=2),1,false)}
case("Macroblock vector-2-2 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=2,dy=2),0,true)}
case("Macroblock vector-2-2 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=2,dy=2),1,true)}
case("Macroblock corner-0-0 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=0,y=0,dx=2,dy=2),0,false)}
case("Macroblock corner-0-0 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=0,y=0,dx=2,dy=2),1,false)}
case("Macroblock corner-0-0 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=0,y=0,dx=2,dy=2),0,true)}
case("Macroblock corner-0-0 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=0,y=0,dx=2,dy=2),1,true)}
case("Macroblock corner-160-128 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=160,y=128,dx=-2,dy=-2),0,false)}
case("Macroblock corner-160-128 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=160,y=128,dx=-2,dy=-2),1,false)}
case("Macroblock corner-160-128 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=160,y=128,dx=-2,dy=-2),0,true)}
case("Macroblock corner-160-128 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=160,y=128,dx=-2,dy=-2),1,true)}
case("Macroblock corner-0-128 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=0,y=128,dx=1,dy=-1),0,false)}
case("Macroblock corner-0-128 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=0,y=128,dx=1,dy=-1),1,false)}
case("Macroblock corner-0-128 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=0,y=128,dx=1,dy=-1),0,true)}
case("Macroblock corner-0-128 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=0,y=128,dx=1,dy=-1),1,true)}
case("Macroblock corner-160-0 mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=160,y=0,dx=-1,dy=1),0,false)}
case("Macroblock corner-160-0 mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=160,y=0,dx=-1,dy=1),1,false)}
case("Macroblock corner-160-0 mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=160,y=0,dx=-1,dy=1),0,true)}
case("Macroblock corner-160-0 mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=160,y=0,dx=-1,dy=1),1,true)}
case("Macroblock padded mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(layout="padded"),0,false)}
case("Macroblock padded mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(layout="padded"),1,false)}
case("Macroblock padded mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(layout="padded"),0,true)}
case("Macroblock padded mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(layout="padded"),1,true)}
case("Macroblock alias mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=0,dy=0,alias=true),0,false)}
case("Macroblock alias mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(dx=0,dy=0,alias=true),1,false)}
case("Macroblock alias mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=0,dy=0,alias=true),0,true)}
case("Macroblock alias mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(dx=0,dy=0,alias=true),1,true)}
case("Macroblock positive mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(pattern="positive"),0,false)}
case("Macroblock positive mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(pattern="positive"),1,false)}
case("Macroblock positive mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(pattern="positive"),0,true)}
case("Macroblock positive mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(pattern="positive"),1,true)}
case("Macroblock negative mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(pattern="negative"),0,false)}
case("Macroblock negative mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(pattern="negative"),1,false)}
case("Macroblock negative mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(pattern="negative"),0,true)}
case("Macroblock negative mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(pattern="negative"),1,true)}
case("Macroblock unaligned-position mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=17,bad="position"),0,false)}
case("Macroblock unaligned-position mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=17,bad="position"),1,false)}
case("Macroblock unaligned-position mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=17,bad="position"),0,true)}
case("Macroblock unaligned-position mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=17,bad="position"),1,true)}
case("Macroblock invalid-last-plane mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(bad="last-stride"),0,false)}
case("Macroblock invalid-last-plane mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(bad="last-stride"),1,false)}
case("Macroblock invalid-last-plane mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(bad="last-stride"),0,true)}
case("Macroblock invalid-last-plane mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(bad="last-stride"),1,true)}
case("Macroblock outside-picture mode=0 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=0,dx=-1,bad="position"),0,false)}
case("Macroblock outside-picture mode=1 little=False",instructionLimit=100000,byteOrder=ByteOrder.BIG_ENDIAN){checkMacro(MacroInput(x=0,dx=-1,bad="position"),1,false)}
case("Macroblock outside-picture mode=0 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=0,dx=-1,bad="position"),0,true)}
case("Macroblock outside-picture mode=1 little=True",instructionLimit=100000,byteOrder=ByteOrder.LITTLE_ENDIAN){checkMacro(MacroInput(x=0,dx=-1,bad="position"),1,true)}
