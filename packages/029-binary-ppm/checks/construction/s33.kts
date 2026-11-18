import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation
import java.util.Random

data class Header(val width:Int,val height:Int,val offset:Int){val pixels get()=width*height;val dimensions get()=(height.toLong() shl 32)or width.toLong()}
fun ppm(w:Int,h:Int,raw:ByteArray)= "P6\n$w $h\n255\n".toByteArray()+raw
fun pattern(n:Int,salt:Int=129)=ByteArray(n){(it*73+salt).toByte()}
fun header(bytes:ByteArray):Header?{
 if(bytes.size !in 3..12544 || bytes[0]!=80.toByte()||bytes[1]!=54.toByte())return null
 var i=2;val end=minOf(bytes.size,256)
 fun gap():Boolean{val start=i;while(i<end){val c=bytes[i].toInt()and 255;if(c==35){while(i<end&&bytes[i]!=10.toByte())i++;if(i==end)return false;i++}else if(c==32||c in 9..13)i++ else break};return i>start&&i<end}
 fun number():Int?{if(i>=end||bytes[i].toInt() !in 49..57)return null;var n=0;while(i<end&&bytes[i].toInt() in 48..57){n=n*10+bytes[i++].toInt()-48;if(n>255)return null};return n}
 if(!gap())return null;val w=number()?:return null;if(w !in 1..64||!gap())return null;val h=number()?:return null;if(h !in 1..64||!gap())return null
 if(number()!=255||i>=end||bytes[i]!=10.toByte())return null;i++;return if(bytes.size-i==3*w*h)Header(w,h,i)else null
}
fun AssemblyCase.put(name:String,bytes:ByteArray){require(bytes.size%4==0);for(i in bytes.indices step 4)setWord(name,(0..3).fold(0){v,j->v or((bytes[i+j].toInt()and 255)shl(8*j))},i)}
fun AssemblyCase.fill(name:String,size:Int,data:ByteArray=byteArrayOf(),offset:Int=0){val b=ByteArray(size){165.toByte()};data.copyInto(b,offset);put(name,b)}
val objects=listOf("input_path" to 128,"output_path" to 128,"source" to 12576,"metadata" to 64,"framebuffer" to 16448,"destination" to 12576,"status" to 4,"count" to 8,"mode" to 4,"byte_count" to 8,"pixel_capacity" to 8,"dimensions" to 8,"byte_capacity" to 8)
fun AssemblyCase.setup(data:ByteArray=byteArrayOf(),offset:Int=0):List<Long>{for((name,size)in objects)fill(name,size);setWord("mode",0);setWord("byte_count",23);setWord("byte_count",0,4);setWord("pixel_capacity",4096);setWord("pixel_capacity",0,4);setWord("dimensions",2);setWord("dimensions",2,4);setWord("byte_capacity",12544);setWord("byte_capacity",0,4);fill("source",12576,data,offset);for(i in 0 until 4096 step 4)setWord("workspace",0xa5a5a5a5.toInt(),i);val saved=List(9){0x5566778800000000L+it*977};for(i in 0..7)setRegister("s$i",saved[i]);setRegister("fp",saved[8]);val top=readDataAddress("workspace",4096);setRegister("sp",top);return saved+top}
fun AssemblyCase.preserved(saved:List<Long>){for(i in 0..7)expectRegister("s$i",saved[i]);expectRegister("fp",saved[8]);expectRegister("sp",saved[9]);expectOutput("")}
fun fits(a:Long,n:Int,b:Long,size:Int)=a>=b&&n<=size&&a-b<=size.toLong()-n
fun AssemblyCase.observe(reads:List<Pair<Long,Int>>,writes:List<Pair<Long,Int>>,action:()->Unit){val regions=objects.map{(name,size)->Triple(name,readDataAddress(name),size)};val old=regions.map{(name,_,size)->readBytes(name,size)};val lo=regions.minOf{it.second};val hi=regions.maxOf{it.second+it.third};val trace=traceMemoryAccesses(lo,(hi-lo).toInt(),maxAccesses=200000);action();trace.close();for(x in trace.snapshot())if(regions.any{(_,b,n)->x.address<b+n&&b<x.address+x.length})expectCondition((if(x.write)writes else reads).any{(b,n)->fits(x.address,x.length,b,n)},"Every caller access belongs to the selected interval");for((i,region)in regions.withIndex()){val(name,base,size)=region;val after=readBytes(name,size);for(j in after.indices)if(writes.none{(b,n)->fits(base+j,1,b,n)})expectCondition(after[j]==old[i][j],"Caller bytes outside selected output remain unchanged")}}
fun word(n:Long,bytes:Int=4)=ByteArray(bytes){(n ushr(8*it)).toByte()}
fun pixelWords(raw:ByteArray,alpha:Int=0)=ByteArray(raw.size/3*4){j->val p=j/4;when(j%4){0->raw[3*p+2];1->raw[3*p+1];2->raw[3*p];else->alpha.toByte()}}
fun AssemblyCase.checkMemory(entry:String,data:ByteArray,index:Int=0,capacity:Long=4096,count:Long=data.size.toLong()){
 val logical=if(count.toULong()<=12544u)ByteArray(count.toInt()){if(it<data.size)data[it]else 165.toByte()}else byteArrayOf()
 val so=index%19;val outOffset=(index%7)*4;val metaOffset=(index%3)*8;val saved=setup(data,so);val h=header(logical);val status=if(count.toULong()>12544u)2 else if(h==null)1 else if(entry=="ppm_decode"&&capacity.toULong()<h.pixels.toULong())2 else 0
 val meta=if(index%2==0)"metadata"else"destination";val dest=if(entry=="ppm_parse")meta else"framebuffer";val off=if(entry=="ppm_parse")metaOffset else outOffset
 setRegisterToDataAddress("a0","source",so.toLong());setRegister("a1",count);setRegisterToDataAddress("a2",dest,off.toLong());setRegister("a3",capacity)
 val expected=if(status!=0)byteArrayOf()else if(entry=="ppm_parse")word(h!!.width.toLong())+word(h.height.toLong())+word(h.offset.toLong(),8)else pixelWords(logical.copyOfRange(h!!.offset,logical.size))
 val src=readDataAddress("source",so.toLong());val out=readDataAddress(dest,off.toLong());val reads=if(count.toULong()<=12544u&&count>=3)listOf(src to logical.size)else emptyList();val writes=if(expected.isEmpty())emptyList()else listOf(out to expected.size)
 observe(reads+writes,writes){callFunction(entry,"function_return");expectRegister("v0",status);expectRegister("v1",if(status!=0)0L else if(entry=="ppm_parse")h!!.pixels.toLong()else h!!.dimensions);preserved(saved)}
 if(expected.isNotEmpty())expectCondition(readBytes(dest,expected.size,off).contentEquals(expected.map{it.toInt()and 255}.toIntArray()),"Metadata or framebuffer agrees with independent header and RGB model")
}
fun AssemblyCase.checkEncode(w:Long,h:Long,index:Int=0,capacity:Long=12544){
 val valid=w in 1..64&&h in 1..64;val raw=if(valid)pattern((3*w*h).toInt(),17+index)else byteArrayOf();val expected=if(valid)ppm(w.toInt(),h.toInt(),raw)else byteArrayOf();val saved=setup();val so=(index%7)*4;val dest=if(index%2==0)"destination"else"source";val off=index%19;fill("framebuffer",16448,pixelWords(raw,if(index%2==0)0 else 213),so)
 val status=if(!valid)1 else if(capacity.toULong()<expected.size.toULong())2 else 0;setRegisterToDataAddress("a0","framebuffer",so.toLong());setRegister("a1",(h shl 32)or(w and 0xffffffffL));setRegisterToDataAddress("a2",dest,off.toLong());setRegister("a3",capacity)
 val src=readDataAddress("framebuffer",so.toLong());val out=readDataAddress(dest,off.toLong());val reads=if(status==0)listOf(src to raw.size/3*4,out to expected.size)else emptyList();val writes=if(status==0)listOf(out to expected.size)else emptyList()
 observe(reads,writes){callFunction("ppm_encode","function_return");expectRegister("v0",status);expectRegister("v1",if(status==0)expected.size else 0);preserved(saved)}
 if(status==0)expectCondition(readBytes(dest,expected.size,off).contentEquals(expected.map{it.toInt()and 255}.toIntArray()),"Canonical output bytes agree with independent RGB serialization")
}

fun octets(s:String)=ByteArray(s.length/2){s.substring(2*it,2*it+2).toInt(16).toByte()}
case("s33-1 offset function",instructionLimit=3000000){
 val saved=setup(octets(""),3);fill("framebuffer",16448,octets(""),4);fill("input_path",128,"input.ppm".toByteArray()+byteArrayOf(0),1);fill("output_path",128,"output.ppm".toByteArray()+byteArrayOf(0),2)
 setWord("mode",3.toInt())
 setWord("byte_count",23.toInt())
 setWord("byte_count",0.toInt(),4)
 setWord("pixel_capacity",4096.toInt())
 setWord("pixel_capacity",0.toInt(),4)
 setWord("dimensions",2.toInt())
 setWord("dimensions",2.toInt(),4)
 setWord("byte_capacity",12544.toInt())
 setWord("byte_capacity",0.toInt(),4)
 setWord("status",-1);setWord("count",-1);setWord("count",-1,4)
 val files=provideFiles(mapOf("input.ppm" to octets("50360a3220320a3235350aff000000ff000000ffffffff"),"output.ppm" to octets("6f6c64206f7574707574")),writablePaths=setOf("output.ppm"),maxReadSize=7,maxWriteSize=3,failAfterBytes=null,failWriteAfterBytes=null,firstDescriptor=9)
 setRegisterToDataAddress("a0","input_path",1L);setRegisterToDataAddress("a1","source",3L);setRegister("a2",12544L);setRegisterToDataAddress("a3","metadata",8L)
 setRegisterToTextAddress("ra","function_return");startAt("ppm_load")
 val reads:List<Pair<Long,Int>> = listOf(readDataAddress("input_path",1L) to 10);val writes:List<Pair<Long,Int>> = emptyList()
 observe(reads+writes,writes){
 runUntil("after_l_open")
 expectRegister("sp",saved[9]-128)
 expectRegister("s3",9L)
 expectRegister("s4",0L)
 expectCondition(runCatching{files.expectAllClosed()}.isFailure==true,"Checkpoint descriptor ownership")
 runUntil("function_return")
 expectRegister("v0",4);expectRegister("v1",0L);preserved(saved)
 }
 files.expectAllClosed();files.expectFile("output.ppm",octets("6f6c64206f7574707574"))
}
case("s33-1 caller",instructionLimit=3000000){
 val saved=setup(octets(""),0);fill("framebuffer",16448,octets(""),0);fill("input_path",128,"input.ppm".toByteArray()+byteArrayOf(0),0);fill("output_path",128,"output.ppm".toByteArray()+byteArrayOf(0),0)
 setWord("mode",3.toInt())
 setWord("byte_count",23.toInt())
 setWord("byte_count",0.toInt(),4)
 setWord("pixel_capacity",4096.toInt())
 setWord("pixel_capacity",0.toInt(),4)
 setWord("dimensions",2.toInt())
 setWord("dimensions",2.toInt(),4)
 setWord("byte_capacity",12544.toInt())
 setWord("byte_capacity",0.toInt(),4)
 setWord("status",-1);setWord("count",-1);setWord("count",-1,4)
 val files=provideFiles(mapOf("input.ppm" to octets("50360a3220320a3235350aff000000ff000000ffffffff"),"output.ppm" to octets("6f6c64206f7574707574")),writablePaths=setOf("output.ppm"),maxReadSize=7,maxWriteSize=3,failAfterBytes=null,failWriteAfterBytes=null,firstDescriptor=9)
 startAt("main");runUntil("load_call")
 val reads:List<Pair<Long,Int>> = listOf(readDataAddress("input_path",0L) to 10);val writes:List<Pair<Long,Int>> = emptyList()
 observe(reads+writes,writes){
 runUntil("after_l_open")
 expectRegister("sp",saved[9]-128)
 expectRegister("s3",9L)
 expectRegister("s4",0L)
 expectCondition(runCatching{files.expectAllClosed()}.isFailure==true,"Checkpoint descriptor ownership")
 runUntil("load_return")
 expectRegister("v0",4);expectRegister("v1",0L);preserved(saved)
 }
 files.expectAllClosed();files.expectFile("output.ppm",octets("6f6c64206f7574707574"))
 runToExit();expectWord("status",4);expectCondition(readBytes("count",8).contentEquals(word(0L,8).map{it.toInt()and 255}.toIntArray()),"Caller retains all64 returned bits")
}
case("s33-missing offset function",instructionLimit=3000000){
 val saved=setup(octets(""),3);fill("framebuffer",16448,octets(""),4);fill("input_path",128,"input.ppm".toByteArray()+byteArrayOf(0),1);fill("output_path",128,"output.ppm".toByteArray()+byteArrayOf(0),2)
 setWord("mode",3.toInt())
 setWord("byte_count",23.toInt())
 setWord("byte_count",0.toInt(),4)
 setWord("pixel_capacity",4096.toInt())
 setWord("pixel_capacity",0.toInt(),4)
 setWord("dimensions",2.toInt())
 setWord("dimensions",2.toInt(),4)
 setWord("byte_capacity",12544.toInt())
 setWord("byte_capacity",0.toInt(),4)
 setWord("status",-1);setWord("count",-1);setWord("count",-1,4)
 val files=provideFiles(mapOf("output.ppm" to octets("6f6c64206f7574707574")),writablePaths=setOf("output.ppm"),maxReadSize=7,maxWriteSize=3,failAfterBytes=null,failWriteAfterBytes=null,firstDescriptor=9)
 setRegisterToDataAddress("a0","input_path",1L);setRegisterToDataAddress("a1","source",3L);setRegister("a2",12544L);setRegisterToDataAddress("a3","metadata",8L)
 setRegisterToTextAddress("ra","function_return");startAt("ppm_load")
 val reads:List<Pair<Long,Int>> = listOf(readDataAddress("input_path",1L) to 10);val writes:List<Pair<Long,Int>> = emptyList()
 observe(reads+writes,writes){
 runUntil("function_return")
 expectRegister("v0",3);expectRegister("v1",0L);preserved(saved)
 }
 files.expectAllClosed();files.expectFile("output.ppm",octets("6f6c64206f7574707574"))
}
case("s33-missing caller",instructionLimit=3000000){
 val saved=setup(octets(""),0);fill("framebuffer",16448,octets(""),0);fill("input_path",128,"input.ppm".toByteArray()+byteArrayOf(0),0);fill("output_path",128,"output.ppm".toByteArray()+byteArrayOf(0),0)
 setWord("mode",3.toInt())
 setWord("byte_count",23.toInt())
 setWord("byte_count",0.toInt(),4)
 setWord("pixel_capacity",4096.toInt())
 setWord("pixel_capacity",0.toInt(),4)
 setWord("dimensions",2.toInt())
 setWord("dimensions",2.toInt(),4)
 setWord("byte_capacity",12544.toInt())
 setWord("byte_capacity",0.toInt(),4)
 setWord("status",-1);setWord("count",-1);setWord("count",-1,4)
 val files=provideFiles(mapOf("output.ppm" to octets("6f6c64206f7574707574")),writablePaths=setOf("output.ppm"),maxReadSize=7,maxWriteSize=3,failAfterBytes=null,failWriteAfterBytes=null,firstDescriptor=9)
 startAt("main");runUntil("load_call")
 val reads:List<Pair<Long,Int>> = listOf(readDataAddress("input_path",0L) to 10);val writes:List<Pair<Long,Int>> = emptyList()
 observe(reads+writes,writes){
 runUntil("load_return")
 expectRegister("v0",3);expectRegister("v1",0L);preserved(saved)
 }
 files.expectAllClosed();files.expectFile("output.ppm",octets("6f6c64206f7574707574"))
 runToExit();expectWord("status",3);expectCondition(readBytes("count",8).contentEquals(word(0L,8).map{it.toInt()and 255}.toIntArray()),"Caller retains all64 returned bits")
}
