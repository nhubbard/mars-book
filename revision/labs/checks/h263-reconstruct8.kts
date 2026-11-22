import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder

val reconObjects=listOf("recon_prediction" to 28720,"recon_inverse" to 28752,
 "recon_output" to 28720,"recon_settings" to 40,"recon_status" to 8,"recon_count" to 8)
fun reconField(a:IntArray,at:Int,value:Long,little:Boolean,width:Int=8){for(j in 0 until width)a[at+j]=((value ushr(8*(if(little)j else width-1-j)))and 255).toInt()}
fun AssemblyCase.reconPut(label:String,a:IntArray,little:Boolean){for(i in a.indices step 4){var v=0;for(j in 0..3)v=v or(a[i+j]shl(8*(if(little)j else 3-j)));setWord(label,v,i)}}
fun reconWindow(address:Long,length:Int,base:Long,stride:Long,width:Int):Boolean{
 if(address<base)return false
 val offset=address-base
 return offset/stride<8 && offset%stride+length<=width
}
fun AssemblyCase.reconstruct(strides:List<Long>,little:Boolean,pattern:Int=0,alias:Boolean=false,bad:String=""){
 val metadataGood=strides[0]in 8L..4096L && strides[1]in 16L..4096L && strides[1]%2==0L && strides[2]in 8L..4096L && bad !in listOf("settings-align","settings-wrap","inverse-align","prediction-wrap","inverse-wrap","output-wrap")
 val sampleGood=!bad.startsWith("sample-");val good=metadataGood&&sampleGood
 val actual=listOf(if(strides[0]in 8L..4096L)strides[0].toInt()else 8,if(strides[1]in 16L..4096L&&strides[1]%2==0L)strides[1].toInt()else 16,if(strides[2]in 8L..4096L)strides[2].toInt()else 8)
 val po=3+pattern%4;val ro=2+2*(pattern%3);val oo=1+pattern%3
 val prediction=IntArray(64){j->when(pattern){0->128;1->0;2->255;else->(j*73+pattern*17)and 255}}
 val values=listOf(-256,-129,-128,-127,-1,0,1,126,127,128,254,255)
 val inverse=IntArray(64){j->when(pattern){0->listOf(-129,-128,-127,0,126,127,128,255)[j%8];1->-256;2->255;else->values[(j*7+pattern)%values.size]}}
 if(bad.startsWith("sample-")){val at=if(bad.endsWith("first"))0 else 63;inverse[at]=if(bad.contains("low"))-257 else 256}
 val left=IntArray(28720){165};val residual=IntArray(28752){165}
 for(j in 0..63){left[po+j/8*actual[0]+j%8]=prediction[j];reconField(residual,ro+j/8*actual[1]+2*(j%8),inverse[j].toLong(),little,2)}
 reconPut("recon_prediction",left,little);reconPut("recon_inverse",residual,little)
 val settings=IntArray(40){165};for(j in 0..2)reconField(settings,8*j,strides[j],little);reconPut("recon_settings",settings,little)
 val predictionAddress=readDataAddress("recon_prediction",po.toLong());val inverseAddress=readDataAddress("recon_inverse",ro.toLong());val outputAddress=if(alias)predictionAddress else readDataAddress("recon_output",oo.toLong());val settingsAddress=readDataAddress("recon_settings")
 setRegister("a0",if(bad=="prediction-wrap")-4L else predictionAddress)
 setRegister("a1",if(bad=="inverse-wrap")-2L else inverseAddress+if(bad=="inverse-align")1 else 0)
 setRegister("a2",if(bad=="output-wrap")-4L else outputAddress)
 setRegister("a3",if(bad=="settings-wrap")-16L else settingsAddress+if(bad=="settings-align")4 else 0)
 val stack=readDataAddress("recon_stack",8192);setRegister("sp",stack)
 val saved=List(10){0x3141592600000000L+977*it};val names=(0..7).map{"s$it"}+listOf("fp","gp");for(j in names.indices)setRegister(names[j],saved[j])
 val objects=reconObjects.map{(n,size)->Triple(n,readDataAddress(n),size)};val before=reconObjects.map{(n,size)->readBytes(n,size)};val lo=objects.minOf{it.second};val hi=objects.maxOf{it.second+it.third};val trace=traceMemoryAccesses(lo,(hi-lo).toInt(),maxAccesses=10000)
 callFunction("h263_reconstruct8","recon_function_return");trace.close()
 expectRegister("v0",if(!metadataGood)1 else if(!sampleGood)2 else 0)
 val sums=IntArray(64){prediction[it]+inverse[it]};val clipped=sums.count{it<0||it>255}
 expectRegister("v1",if(good)clipped else 0);expectRegister("sp",stack);for(j in names.indices)expectRegister(names[j],saved[j]);expectOutput("")
 for(e in trace.snapshot())if(objects.any{(_,base,size)->e.address<base+size&&base<e.address+e.length}){
  val allowed=if(e.write)good&&reconWindow(e.address,e.length,outputAddress,strides[2],8)
  else (bad !in listOf("settings-align","settings-wrap") && e.address>=settingsAddress && e.address-settingsAddress+e.length<=24) || metadataGood&&(reconWindow(e.address,e.length,predictionAddress,strides[0],8)||reconWindow(e.address,e.length,inverseAddress,strides[1],16))
  expectCondition(allowed,"Only admitted settings, eight active samples per row and successful output writes")
 }
 for((k,obj)in objects.withIndex()){
  val(label,_,size)=obj;val expected=before[k].copyOf()
  if(good&&label==(if(alias)"recon_prediction"else"recon_output")){val offset=if(alias)po else oo;for(j in 0..63)expected[offset+j/8*actual[2]+j%8]=sums[j].coerceIn(0,255)}
  expectCondition(readBytes(label,size).contentEquals(expected),"Exact clipped samples and retained caller bytes: $label")
 }
}

val reconStrides=listOf(listOf(8L,16L,8L),listOf(9L,18L,11L),listOf(31L,34L,17L),listOf(4096L,4096L,4096L),listOf(4093L,4094L,4091L))
for(little in listOf(false,true))for(strides in reconStrides)for(pattern in 0..5)
 case("reconstruct strides=$strides pattern=$pattern little=$little",instructionLimit=100000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){reconstruct(strides,little,pattern)}
for(little in listOf(false,true))for(stride in listOf(8L,9L,4096L))for(pattern in 0..3)
 case("reconstruct exact prediction alias stride=$stride pattern=$pattern little=$little",instructionLimit=100000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){reconstruct(listOf(stride,18L,stride),little,pattern,alias=true)}
for(little in listOf(false,true))for((field,value)in listOf(0 to 7L,0 to 4097L,0 to 0x100000008L,0 to -1L,1 to 15L,1 to 17L,1 to 4098L,1 to 0x100000010L,2 to 7L,2 to 4097L,2 to 0x100000008L,2 to -1L))
 case("reconstruct invalid stride $field $value little=$little",instructionLimit=100000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){val s=mutableListOf(8L,16L,8L);s[field]=value;reconstruct(s,little,3)}
for(little in listOf(false,true))for(bad in listOf("settings-align","settings-wrap","inverse-align","prediction-wrap","inverse-wrap","output-wrap","sample-low-first","sample-high-first","sample-low-last","sample-high-last"))
 case("reconstruct invalid $bad little=$little",instructionLimit=100000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){reconstruct(listOf(9L,18L,11L),little,3,bad=bad)}
for(little in listOf(false,true))case("reconstruct repeated results little=$little",instructionLimit=100000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){for(pattern in listOf(2,1,0,5,3))reconstruct(listOf(9L,18L,11L),little,pattern)}

for(little in listOf(false,true))for(bad in listOf("sample-low-first","sample-high-first","sample-low-last","sample-high-last"))
 case("invalid reconstruction retains aliased prediction $bad little=$little",instructionLimit=100000,byteOrder=if(little)ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN){reconstruct(listOf(9L,18L,9L),little,3,alias=true,bad=bad)}
