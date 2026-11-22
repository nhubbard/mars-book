import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import java.lang.Long.compareUnsigned
import kotlin.math.abs

// Forward RGB8 filtering contract. No reference instruction sequence is required.
val filterObjects=listOf("filter_settings" to 32,"current_rgb" to 1552,
 "previous_rgb" to 1552,"filtered_row" to 1556,"filter_status" to 4,"filter_count" to 8)
fun forwardPrediction(kind:Int,a:Int,b:Int,c:Int):Int=when(kind){
 0->0;1->a;2->b;3->(a+b)/2
 else->{val p=a+b-c;val da=abs(p-a);val db=abs(p-b);val dc=abs(p-c)
  if(da<=db && da<=dc)a else if(db<=dc)b else c}
}
fun AssemblyCase.filterPut(label:String,bytes:IntArray,little:Boolean){
 require(bytes.size%4==0)
 for(i in bytes.indices step 4){var word=0;for(j in 0..3)word=word or(bytes[i+j]shl(8*(if(little)j else 3-j)));setWord(label,word,i)}
}
fun filterField(bytes:IntArray,at:Int,value:Long,little:Boolean){
 for(j in 0..7)bytes[at+j]=((value ushr(8*(if(little)j else 7-j)))and 255).toInt()
}
fun filterInside(address:Long,length:Int,base:Long,size:Int)=address>=base && length<=size && address-base<=size-length
fun AssemblyCase.forwardFilter(width:Long,kind:Long,little:Boolean,top:Boolean=false,seed:Int=0,
 capacity:Long=if(width in 1L..512L)1+3*width else 0,tie:Triple<Int,Int,Int>?=null,sameInput:Boolean=false){
 val n=if(width in 1L..512L)(3*width).toInt()else 0
 val valid=width in 1L..512L && kind in 0L..4L && compareUnsigned(capacity,1L+n)>=0
 val current=IntArray(n){(it*71+it*it*13+seed*17)and 255}
 val previous=IntArray(n){(it*43+255-seed*29)and 255}
 if(tie!=null){require(n==6);for(c in 0..2){current[c]=tie.first;previous[c]=tie.third;previous[3+c]=tie.second;current[3+c]=201}}
 val currentOffset=1+seed%7;val previousOffset=7-seed%7;val outputOffset=1+seed%5
 val inputBytes=IntArray(1552){165};for(j in current.indices)inputBytes[currentOffset+j]=current[j]
 val priorBytes=IntArray(1552){165};for(j in previous.indices)priorBytes[previousOffset+j]=previous[j]
 filterPut("current_rgb",inputBytes,little);filterPut("previous_rgb",priorBytes,little)
 val settings=IntArray(32){165};filterField(settings,0,width,little);filterField(settings,8,kind,little);filterField(settings,16,capacity,little);filterPut("filter_settings",settings,little)
 val previousUsed=valid && !top && kind>=2
 val currentAddress=readDataAddress("current_rgb",currentOffset.toLong())
 val previousAddress=if(sameInput)currentAddress else readDataAddress("previous_rgb",previousOffset.toLong())
 val outputAddress=readDataAddress("filtered_row",outputOffset.toLong())
 setRegister("a0",if(valid)currentAddress else 1)
 setRegister("a1",if(top)0 else if(previousUsed)previousAddress else 1)
 setRegister("a2",if(valid)outputAddress else 1);setRegisterToDataAddress("a3","filter_settings")
 val stack=readDataAddress("filter_workspace",4096);setRegister("sp",stack)
 val saved=List(9){0x3141592600000000L+it*991};for(j in 0..7)setRegister("s$j",saved[j]);setRegister("fp",saved[8])
 val before=filterObjects.map{(label,size)->readBytes(label,size)}
 val objects=filterObjects.map{(label,size)->Triple(label,readDataAddress(label),size)}
 val low=objects.minOf{it.second};val high=objects.maxOf{it.second+it.third}
 val trace=traceMemoryAccesses(low,(high-low).toInt(),maxAccesses=100000)
 callFunction("png_filter_scanline","filter_function_return");trace.close()
 expectRegister("v0",if(valid)0 else 1);expectRegister("v1",if(valid)n+1 else 0)
 expectRegister("sp",stack);for(j in 0..7)expectRegister("s$j",saved[j]);expectRegister("fp",saved[8]);expectOutput("")
 val result=IntArray(if(valid)n+1 else 0)
 if(valid){result[0]=kind.toInt();for(j in current.indices){val a=if(j>=3)current[j-3]else 0;val b=if(previousUsed)(if(sameInput)current[j]else previous[j])else 0;val c=if(previousUsed && j>=3)(if(sameInput)current[j-3]else previous[j-3])else 0;result[j+1]=(current[j]-forwardPrediction(kind.toInt(),a,b,c))and 255}}
 val prefix=readDataAddress("filter_settings")
 for(event in trace.snapshot())if(objects.any{(_,base,size)->event.address<base+size && base<event.address+event.length}){
  val allowed=if(event.write)valid && filterInside(event.address,event.length,outputAddress,n+1)
   else filterInside(event.address,event.length,prefix,24) || valid &&
    (filterInside(event.address,event.length,currentAddress,n) || previousUsed && filterInside(event.address,event.length,previousAddress,n))
  expectCondition(allowed,"Access only settings, selected RGB inputs and selected output")
 }
 for((j,obj)in objects.withIndex()){val(label,_,size)=obj;val expected=before[j].copyOf();if(valid && label=="filtered_row")for(k in result.indices)expected[outputOffset+k]=result[k];expectCondition(readBytes(label,size).contentEquals(expected),"Exact forward residuals and retained caller object $label")}
 // An independent inverse recurrence must recover the original samples, not merely a checksum.
 if(valid){val observed=readBytes(outputAddress,n+1);val recovered=IntArray(n);for(j in recovered.indices){val a=if(j>=3)recovered[j-3]else 0;val b=if(previousUsed)(if(sameInput)current[j]else previous[j])else 0;val c=if(previousUsed && j>=3)(if(sameInput)current[j-3]else previous[j-3])else 0;recovered[j]=(observed[j+1]+forwardPrediction(observed[0],a,b,c))and 255};expectCondition(recovered.contentEquals(current),"Inverse recurrence recovers each original RGB sample")}
}

// Public cases.
for(little in listOf(false,true))for(width in listOf(1L,2L,5L,15L,16L,17L,31L,32L,33L,511L,512L))for(kind in 0L..4L)for(top in listOf(false,true))
 case("forward $width filter $kind top $top little $little",byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){forwardFilter(width,kind,little,top,(width+kind).toInt()%23)}
for(little in listOf(false,true))for((width,kind,capacity)in listOf(Triple(0L,0L,4L),Triple(513L,0L,1540L),Triple(0x100000001L,0L,7L),Triple(-1L,0L,-1L),Triple(2L,5L,7L),Triple(2L,0x100000000L,7L),Triple(2L,-1L,7L),Triple(2L,3L,6L)))
 case("invalid $width $kind $capacity little $little",byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){forwardFilter(width,kind,little,capacity=capacity)}
for(little in listOf(false,true))for(kind in 0L..4L){
 case("unsigned capacity filter $kind little $little",byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){forwardFilter(2,kind,little,capacity=Long.MIN_VALUE)}
 case("coincident read-only inputs filter $kind little $little",byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){forwardFilter(17,kind,little,sameInput=true)}
}
for(little in listOf(false,true))case("repeated filters little $little",byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){for(kind in listOf(4L,0L,3L,1L,2L,4L))forwardFilter(17,kind,little,kind%2L==0L,kind.toInt())}

for(little in listOf(false,true))for(tie in listOf(Triple(255,0,85),Triple(0,255,170),Triple(0,3,1),Triple(3,0,2)))
 case("Paeth tie $tie little $little",byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){forwardFilter(2,4,little,tie=tie)}
