import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder

val boundsObjects=listOf("bounds_previous" to 16416,"bounds_current" to 16416,
 "bounds_settings" to 32,"bounds_result" to 64,"bounds_status" to 4,"bounds_count" to 8)
fun boundsField(b:IntArray,at:Int,value:Long,little:Boolean,width:Int=8){for(j in 0 until width)b[at+j]=((value ushr(8*(if(little)j else width-1-j)))and 255).toInt()}
fun AssemblyCase.boundsPut(label:String,b:IntArray,little:Boolean){for(i in b.indices step 4){var v=0;for(j in 0..3)v=v or(b[i+j]shl(8*(if(little)j else 3-j)));setWord(label,v,i)}}
fun boundsInside(address:Long,length:Int,base:Long,size:Int)=address>=base && length<=size && address-base<=size-length
fun AssemblyCase.changedBounds(width:Long,height:Long,little:Boolean,pattern:Int=0,alias:Boolean=false,bad:String=""){
 val valid=width in 1L..64L && height in 1L..64L && bad.isEmpty()
 val n=if(width in 1L..64L && height in 1L..64L)(width*height).toInt()else 0
 val changed=if(alias)emptySet()else when(pattern){
  0->emptySet();1->setOf(0);2->setOf(n-1);3->(0 until n).filter{it%3==1 || it==n-1}.toSet()
  4->(0 until n).toSet();5->setOf((width.toInt()-1).coerceAtLeast(0),width.toInt()).filter{it in 0 until n}.toSet()
  6->setOf(325,326,389,390).filter{it in 0 until n}.toSet()
  else->(0 until n).filter{(it*17+pattern)%23==0}.toSet()
 }
 val prior=IntArray(n){0x89abcdef.toInt() xor(it*977+pattern*73)}
 val current=IntArray(n){prior[it] xor(if(it in changed)if(it%2==0)0x80000000.toInt()else 0x00102040 else 0)}
 val inputOffset=4*(1+pattern%3);val currentOffset=4*(3-pattern%3);val outputOffset=8*(pattern%3)
 val source=IntArray(16416){165};val right=IntArray(16416){165}
 for(j in 0 until n){boundsField(source,inputOffset+4*j,prior[j].toLong(),little,4);boundsField(right,currentOffset+4*j,current[j].toLong(),little,4)}
 boundsPut("bounds_previous",source,little);boundsPut("bounds_current",right,little)
 val descriptor=IntArray(32){165};boundsField(descriptor,0,width,little);boundsField(descriptor,8,height,little);boundsPut("bounds_settings",descriptor,little)
 val left=readDataAddress("bounds_previous",inputOffset.toLong());val rightAddress=if(alias)left else readDataAddress("bounds_current",currentOffset.toLong());val output=readDataAddress("bounds_result",outputOffset.toLong());val settings=readDataAddress("bounds_settings")
 setRegister("a0",if(n==0)1 else left+if(bad=="left")1 else 0)
 setRegister("a1",if(n==0)1 else rightAddress+if(bad=="right")1 else 0)
 setRegister("a2",output+if(bad=="output")4 else 0);setRegister("a3",settings+if(bad=="settings")4 else 0)
 val stack=readDataAddress("bounds_workspace",8192);setRegister("sp",stack)
 val saved=List(9){0x3141592600000000L+it*977};for(j in 0..7)setRegister("s$j",saved[j]);setRegister("fp",saved[8])
 val objects=boundsObjects.map{(label,size)->Triple(label,readDataAddress(label),size)};val before=boundsObjects.map{(label,size)->readBytes(label,size)};val lo=objects.minOf{it.second};val hi=objects.maxOf{it.second+it.third}
 val trace=traceMemoryAccesses(lo,(hi-lo).toInt(),maxAccesses=100000)
 callFunction("video_change_bounds","bounds_function_return");trace.close()
 expectRegister("v0",if(valid)0 else 1);expectRegister("v1",if(valid)changed.size else 0);expectRegister("sp",stack)
 for(j in 0..7)expectRegister("s$j",saved[j]);expectRegister("fp",saved[8]);expectOutput("")
 val rectangle=if(changed.isEmpty())listOf(0L,0L,0L,0L)else listOf(changed.minOf{it%width.toInt()}.toLong(),changed.minOf{it/width.toInt()}.toLong(),1L+changed.maxOf{it%width.toInt()},1L+changed.maxOf{it/width.toInt()})
 for(event in trace.snapshot())if(objects.any{(_,base,size)->event.address<base+size && base<event.address+event.length}){
  val allowed=if(event.write)valid && boundsInside(event.address,event.length,output,32)
   else bad!="settings" && boundsInside(event.address,event.length,settings,16) || valid &&(boundsInside(event.address,event.length,left,4*n)||boundsInside(event.address,event.length,rightAddress,4*n))
  expectCondition(allowed,"Access only admitted native settings, input words and result interval")
 }
 for((j,obj)in objects.withIndex()){val(label,_,size)=obj;val expected=before[j].copyOf();if(valid && label=="bounds_result")for(k in 0..3)boundsField(expected,outputOffset+8*k,rectangle[k],little);expectCondition(readBytes(label,size).contentEquals(expected),"Minimal exclusive bounds and retained caller object $label")}
}

// Public cases.
val boundsDimensions=listOf(1L to 1L,1L to 64L,64L to 1L,2L to 2L,3L to 5L,15L to 17L,16L to 16L,17L to 15L,31L to 33L,63L to 64L,64L to 64L)
for(little in listOf(false,true))for((width,height)in boundsDimensions)for(pattern in 0..5)
 case("bounds $width x $height pattern $pattern little $little",instructionLimit=1000000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){changedBounds(width,height,little,pattern)}
for(little in listOf(false,true))for((width,height)in boundsDimensions)
 case("same inputs $width x $height little $little",instructionLimit=1000000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){changedBounds(width,height,little,4,alias=true)}
for(little in listOf(false,true))for((width,height)in listOf(0L to 1L,1L to 0L,65L to 1L,1L to 65L,0x100000001L to 1L,1L to 0x100000001L,-1L to 1L,1L to -1L))
 case("invalid dimensions $width $height little $little",instructionLimit=1000000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){changedBounds(width,height,little)}
for(little in listOf(false,true))for(bad in listOf("left","right","output","settings"))
 case("misaligned $bad little $little",instructionLimit=1000000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){changedBounds(3,5,little,4,bad=bad)}
for(little in listOf(false,true)){
 case("moving patch little $little",instructionLimit=1000000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){changedBounds(64,64,little,6)}
 case("repeated calls little $little",instructionLimit=1000000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){for(pattern in listOf(4,0,2,1,5,3))changedBounds(17,15,little,pattern)}
}
