// Exact exercise policy; final encoder level selection is assessed separately.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import kotlin.math.abs

data class QuantInput(val q:Long=8,val intra:Long=1,val mode:Long=1,val pattern:String="boundary",val bad:String="")
fun AssemblyCase.quant64(offset:Int,value:Long,little:Boolean){
    setWord("h263Quant_request",(if(little)value else value ushr 32).toInt(),offset)
    setWord("h263Quant_request",(if(little)value ushr 32 else value).toInt(),offset+4)
}
fun AssemblyCase.checkQuant(c:QuantInput,little:Boolean){
    val metadataGood=c.bad.isEmpty()||c.bad.startsWith("domain")
    val good=c.bad.isEmpty();val input=IntArray(64){i->when(c.pattern){
        "zero"->0;"positive"->2047;"negative"->-2048
        "dc-low"->if(i==0)3 else 0;"dc-high"->if(i==0)2047 else 0
        "dc-round"->if(i==0)1020 else 0
        "ramp"->(i*719+137)%4096-2048
        else->intArrayOf(1024,15,16,-16,31,32,-32,2047,-2048,0,1,-1,126,127,-127,-128)[i%16]}}
    if(c.bad=="domain-high")input[63]=2048
    if(c.bad=="domain-low")input[63]=-2049
    val bytes=ByteArray(160){165.toByte()}
    for(i in 0..63){bytes[18+2*i]=(if(little)input[i]else input[i] ushr 8).toByte();bytes[19+2*i]=(if(little)input[i] ushr 8 else input[i]).toByte()}
    for(i in bytes.indices step 4){var value=0;for(j in 0..3)value=value or((bytes[i+j].toInt()and 255)shl(8*(if(little)j else 3-j)));setWord("h263Quant_input",value,i)}
    val base=readDataAddress("h263Quant_input")+18
    quant64(0,when(c.bad){"input-align"->base+1;"input-wrap"->-2L;else->base},little)
    quant64(8,c.q,little);quant64(16,c.intra,little);quant64(24,c.mode,little)
    val before=readBytes("h263Quant_output",288);val requestBefore=readBytes("h263Quant_request",32)
    val saved=(0..7).map{"s"+it}+listOf("fp","gp")
    for((i,name)in saved.withIndex())setRegister(name,0x1234567800000000L+i)
    setRegister("sp",readDataAddress("h263Quant_stack",16400));val stack=readRegister("sp")
    val reads=traceMemoryAccesses(readDataAddress("h263Quant_input"),160)
    val writes=traceMemoryAccesses(readDataAddress("h263Quant_output"),288)
    setRegister("a0",when(c.bad){"request-align"->readDataAddress("h263Quant_request")+1;"request-wrap"->-8L;else->readDataAddress("h263Quant_request")})
    setRegister("a1",when(c.bad){"output-align"->readDataAddress("h263Quant_output")+24;"output-wrap"->-16L;else->readDataAddress("h263Quant_output")+16})
    callFunction("h263_quantize8","h263Quant_return")
    expectRegister("v0",if(good)0 else if(metadataGood)2 else 1)
    if(good){
        val levels=IntArray(64);val rec=IntArray(64)
        for(i in 0..63){val value=input[i];if(c.intra==1L&&i==0){val dc=Math.floorDiv(value+4,8).coerceIn(1,254);levels[i]=if(dc==128)255 else dc;rec[i]=8*dc}
            else{val magnitude=(abs(value)/(2*c.q.toInt())).coerceAtMost(127);val level=if(value<0)-magnitude else magnitude;levels[i]=level
                val raw=if(level==0)0 else(c.q.toInt()*(2*magnitude+1)-(if(c.q%2==0L)1 else 0))*(if(level<0)-1 else 1);rec[i]=raw.coerceIn(-2048,2047)}}
        val output=readBytes("h263Quant_output",256,16)
        for(i in 0..127){val bits=if(little)output[2*i]or(output[2*i+1]shl 8)else(output[2*i]shl 8)or output[2*i+1]
            expectCondition(bits.toShort().toInt()==if(i<64)levels[i]else rec[i-64],"Every level and reconstructed coefficient follows the exercise policy")}
        expectRegister("v1",levels.drop(if(c.intra==1L)1 else 0).count{it!=0}.toLong())
    }else{expectRegister("v1",0);expectCondition(readBytes("h263Quant_output",288).contentEquals(before),"All output retained after rejection")}
    for(e in reads.snapshot())expectCondition(metadataGood&&!e.write&&e.address>=base&&e.address+e.length<=base+128,"Only active halfword input reads after valid metadata")
    for(e in writes.snapshot())expectCondition(!e.write||(good&&e.address>=readDataAddress("h263Quant_output")+16&&e.address+e.length<=readDataAddress("h263Quant_output")+272),"Only successful result payload writes")
    expectCondition(readBytes("h263Quant_input",160).contentEquals(bytes.map{it.toInt()and 255}.toIntArray()),"Input and guards unchanged")
    expectCondition(readBytes("h263Quant_request",32).contentEquals(requestBefore),"Request unchanged")
    expectCondition(readBytes("h263Quant_output",16).contentEquals(before.copyOfRange(0,16))&&readBytes("h263Quant_output",16,272).contentEquals(before.copyOfRange(272,288)),"Output guards unchanged")
    expectCondition(readBytes("h263Quant_stack",16).all{it==165}&&readBytes("h263Quant_stack",16,16400).all{it==165},"Stack guards unchanged")
    for((i,name)in saved.withIndex())expectRegister(name,0x1234567800000000L+i);expectRegister("sp",stack)
}
for(little in listOf(false,true))for(mode in 0L..1L){
    for(q in listOf(1L,8L,31L))for(intra in 0L..1L)for(pattern in listOf("zero","boundary","ramp","dc-round","dc-low","dc-high")){
        case("Quant q=$q intra=$intra mode=$mode pattern=$pattern little=$little",instructionLimit=30000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){checkQuant(QuantInput(q,intra,mode,pattern),little)}
    }
    for(bad in listOf("input-align","input-wrap","request-align","request-wrap","output-align","output-wrap","domain-high","domain-low")){
        case("Quant $bad mode=$mode little=$little",instructionLimit=30000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){checkQuant(QuantInput(mode=mode,bad=bad),little)}
    }
    for((name,values)in listOf("q" to listOf(0L,32L,4294967304L),"intra" to listOf(2L,4294967297L),"mode" to listOf(2L,4294967297L)))for(value in values){
        case("Quant invalid $name=$value baseMode=$mode little=$little",instructionLimit=30000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){
            checkQuant(QuantInput(q=if(name=="q")value else 8,intra=if(name=="intra")value else 1,mode=if(name=="mode")value else mode,bad=name),little)}
    }
}
