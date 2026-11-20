// Accept accurate alternate transforms; require exact consistency within the local loop.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.PI

data class BlockInput(val intra:Long=0,val mode:Long=1,val q:Long=8,val stride:Long=8,val predStride:Long=8,val pattern:String="flat",val bad:String="")
val blockBasis=Array(8){k->DoubleArray(8){n->if(k==0)1/sqrt(8.0)else .5*cos((2*n+1)*k*PI/16)}}
fun blockTransform(input:IntArray,inverse:Boolean):IntArray{
    val m=Array(8){k->DoubleArray(8){n->if(inverse)blockBasis[n][k]else blockBasis[k][n]}}
    val first=Array(8){y->DoubleArray(8){u->(0..7).sumOf{x->input[y*8+x]*m[u][x]}}}
    return IntArray(64){i->floor((0..7).sumOf{y->first[y][i%8]*m[i/8][y]}+.5).toInt().coerceIn(if(inverse)-256 else -2048,if(inverse)255 else 2047)}
}
fun blockLevel(c:Int,q:Int,dc:Boolean):Int{
    if(dc){val d=Math.floorDiv(c+4,8).coerceIn(1,254);return if(d==128)255 else d}
    val m=(abs(c)/(2*q)).coerceAtMost(127);return if(c<0)-m else m
}
fun blockRec(level:Int,q:Int,dc:Boolean):Int{
    if(dc)return 8*(if(level==255)128 else level)
    if(level==0)return 0
    return ((q*(2*abs(level)+1)-(if(q%2==0)1 else 0))*(if(level<0)-1 else 1)).coerceIn(-2048,2047)
}
fun AssemblyCase.blockPut(name:String,bytes:ByteArray,little:Boolean){
    for(i in bytes.indices step 4){var v=0;for(j in 0..3)v=v or((bytes[i+j].toInt()and 255)shl(8*(if(little)j else 3-j)));if(v!=-1515870811)setWord(name,v,i)}
}
fun AssemblyCase.block64(offset:Int,value:Long,little:Boolean){
    setWord("h263Block_request",(if(little)value else value ushr 32).toInt(),offset)
    setWord("h263Block_request",(if(little)value ushr 32 else value).toInt(),offset+4)
}
fun AssemblyCase.checkBlock(c:BlockInput,little:Boolean){
    val good=c.bad.isEmpty();val intra=c.intra==1L
    val current=IntArray(64){i->when(c.pattern){"zero"->0;"maximum"->255;"negative"->0;"ramp"->(47*i+23)%256;"checker","clip"->if((i/8+i%8)%2==0)0 else 255;else->130}}
    val prediction=IntArray(64){i->when(c.pattern){"zero"->0;"maximum"->0;"negative"->255;"ramp"->(31*i+57)%256;"clip"->128;"checker"->if((i/8+i%8)%2==0)255 else 0;else->128}}
    val input=ByteArray(28720){165.toByte()};val pred=ByteArray(28720){165.toByte()}
    val actualStride=if(c.stride in 8..4096)c.stride.toInt()else 8
    val actualPred=if(c.predStride in 8..4096)c.predStride.toInt()else 8
    for(i in 0..63){input[19+i/8*actualStride+i%8]=current[i].toByte();pred[21+i/8*actualPred+i%8]=prediction[i].toByte()}
    blockPut("h263Block_current",input,little);blockPut("h263Block_prediction",pred,little)
    val sourceBase=readDataAddress("h263Block_current")+19;val predBase=readDataAddress("h263Block_prediction")+21
    val fields=listOf(if(c.bad=="current-wrap")-4L else sourceBase,c.stride,
        if(intra){if(c.bad=="intra-prediction")predBase else 0L}else if(c.bad=="prediction-wrap")-4L else predBase,
        if(intra){if(c.bad=="intra-stride")8L else 0L}else c.predStride,c.q,c.intra,c.mode,if(c.bad=="reserved")1L else 0L)
    for((i,v)in fields.withIndex())block64(i*8,v,little)
    val requestBefore=readBytes("h263Block_request",64);val outBefore=readBytes("h263Block_output",480)
    val saved=(0..7).map{"s"+it}+listOf("fp","gp");for((i,n)in saved.withIndex())setRegister(n,0x1234567800000000L+i)
    setRegister("sp",readDataAddress("h263Block_stack",32784));val stack=readRegister("sp")
    val cr=traceMemoryAccesses(readDataAddress("h263Block_current"),28720);val pr=traceMemoryAccesses(readDataAddress("h263Block_prediction"),28720);val ow=traceMemoryAccesses(readDataAddress("h263Block_output"),480)
    setRegister("a0",when(c.bad){"request-align"->readDataAddress("h263Block_request")+1;"request-wrap"->-8L;else->readDataAddress("h263Block_request")})
    setRegister("a1",when(c.bad){"output-align"->readDataAddress("h263Block_output")+24;"output-wrap"->-16L;else->readDataAddress("h263Block_output")+16})
    callFunction("h263_encode_block8","h263Block_return")
    expectRegister("v0",if(good)0 else 1)
    if(good){
        val out=readBytes("h263Block_output",448,16)
        fun half(i:Int):Int=(if(little)out[2*i]or(out[2*i+1]shl 8)else(out[2*i]shl 8)or out[2*i+1]).toShort().toInt()
        val levels=IntArray(64){half(it)};val rec=IntArray(64){half(64+it)};val spatial=IntArray(64){half(128+it)}
        val forward=blockTransform(IntArray(64){current[it]-(if(intra)0 else prediction[it])},false)
        for(i in 0..63){val dc=intra&&i==0;val allowed=(-1..1).map{d->blockLevel((forward[i]+d).coerceIn(-2048,2047),c.q.toInt(),dc)}
            expectCondition(levels[i]in allowed,"Level can result from a forward coefficient within the permitted accuracy")
            expectCondition(rec[i]==blockRec(levels[i],c.q.toInt(),dc),"Inverse quantization follows the actual selected level exactly")}
        val inverse=blockTransform(rec,true)
        for(i in 0..63){expectCondition(spatial[i]in -256..255&&abs(spatial[i]-inverse[i])<=1,"Inverse is accurate for the actually reconstructed coefficient block")
            expectCondition(out[384+i]==(spatial[i]+if(intra)0 else prediction[i]).coerceIn(0,255),"Pixels are exact clipped sums from the returned signed inverse")}
        expectRegister("v1",levels.drop(if(intra)1 else 0).count{it!=0}.toLong())
    }else{expectRegister("v1",0);expectCondition(readBytes("h263Block_output",480).contentEquals(outBefore),"Invalid metadata retains all output")}
    for(e in cr.snapshot())expectCondition(good&&!e.write&&(0 until e.length).all{i->val at=e.address-sourceBase+i;at>=0&&at/c.stride<8&&at%c.stride<8},"Current reads stay in eight active bytes per row")
    for(e in pr.snapshot())expectCondition(good&&!intra&&!e.write&&(0 until e.length).all{i->val at=e.address-predBase+i;at>=0&&at/c.predStride<8&&at%c.predStride<8},"Prediction is read only for valid INTER and within its own rows")
    for(e in ow.snapshot())expectCondition(!e.write||(good&&e.address>=readDataAddress("h263Block_output")+16&&e.address+e.length<=readDataAddress("h263Block_output")+464),"Only successful complete result payload stores")
    expectCondition(readBytes("h263Block_current",28720).contentEquals(input.map{it.toInt()and 255}.toIntArray())&&readBytes("h263Block_prediction",28720).contentEquals(pred.map{it.toInt()and 255}.toIntArray()),"Inputs and guards unchanged")
    expectCondition(readBytes("h263Block_request",64).contentEquals(requestBefore),"Request unchanged")
    expectCondition(readBytes("h263Block_output",16).contentEquals(outBefore.copyOfRange(0,16))&&readBytes("h263Block_output",16,464).contentEquals(outBefore.copyOfRange(464,480)),"Output guards unchanged")
    expectCondition(readBytes("h263Block_stack",16).all{it==165}&&readBytes("h263Block_stack",16,32784).all{it==165},"Stack guards unchanged")
    for((i,n)in saved.withIndex())expectRegister(n,0x1234567800000000L+i);expectRegister("sp",stack)
}
for(little in listOf(false,true))for(mode in 0L..1L){
    for(intra in 0L..1L)for(pattern in listOf("flat","zero","maximum","negative","ramp","checker","clip")){
        case("Block intra=$intra mode=$mode $pattern little=$little",instructionLimit=200000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){checkBlock(BlockInput(intra=intra,mode=mode,pattern=pattern),little)}
    }
    for(intra in 0L..1L)for(stride in listOf(9L,4096L)){
        case("Block padded intra=$intra mode=$mode stride=$stride little=$little",instructionLimit=200000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){checkBlock(BlockInput(intra=intra,mode=mode,stride=stride,predStride=if(stride==9L)17 else 4093,pattern="ramp"),little)}
    }
    for(bad in listOf("current-wrap","prediction-wrap","request-align","request-wrap","output-align","output-wrap","reserved","intra-prediction","intra-stride")){
        case("Block invalid $bad mode=$mode little=$little",instructionLimit=200000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){checkBlock(BlockInput(intra=if(bad.startsWith("intra-"))1 else 0,mode=mode,bad=bad),little)}
    }
    for((name,value)in listOf("q" to 0L,"q" to 32L,"q" to 4294967304L,"intra" to 4294967296L,"mode" to 4294967297L,"stride" to 7L,"stride" to 4097L,"predStride" to 7L,"predStride" to 4294967304L)){
        case("Block invalid $name=$value mode=$mode little=$little",instructionLimit=200000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){checkBlock(BlockInput(q=if(name=="q")value else 8,intra=if(name=="intra")value else 0,mode=if(name=="mode")value else mode,stride=if(name=="stride")value else 8,predStride=if(name=="predStride")value else 8,bad=name),little)}
    }
}
