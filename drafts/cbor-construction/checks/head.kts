import edu.missouristate.mars.cli.grading.AssemblyCase

// Host observations for the published bounded profile. No MIPS implementation is supplied.
data class CborHead(val kind:Int,val argument:ULong,val width:Int)
data class Telemetry(val ticks:ULong,val device:ByteArray,val readings:IntArray)
fun bytes(hex:String)=hex.chunked(2).map{it.toInt(16).toByte()}.toByteArray()
fun little(value:Long,width:Int)=ByteArray(width){(value ushr (8*it)).toByte()}
fun unsignedField(raw:ByteArray,at:Int,width:Int):ULong=(0 until width).fold(0uL){value,i->value or ((raw[at+i].toULong() and 255uL) shl (8*i))}
fun headAt(raw:ByteArray,position:Int=0):CborHead?=runCatching{
 require(position in raw.indices)
 val initial=raw[position].toInt() and 255;val kind=initial ushr 5;val info=initial and 31
 require(kind in listOf(0,1,2,4,5)&&info<28)
 if(info<24)CborHead(kind,info.toULong(),1) else{
  val count=1 shl (info-24);require(raw.size-position-1>=count)
  var argument=0uL;for(i in 1..count)argument=(argument shl 8) or (raw[position+i].toULong() and 255uL)
  CborHead(kind,argument,count+1)
 }
}.getOrNull()
// Return a byte cursor, retaining independent duplicate-key identity at each map.
fun valueEnd(raw:ByteArray,start:Int,open:Int):Int{
 val h=requireNotNull(headAt(raw,start));var cursor=start+h.width
 when(h.kind){
  0,1->return cursor
  2->{require(h.argument<=32uL&&h.argument<=(raw.size-cursor).toULong());return cursor+h.argument.toInt()}
  4->{require(open<8&&h.argument<=64uL);repeat(h.argument.toInt()){cursor=valueEnd(raw,cursor,open+1)}}
  5->{require(open<8&&h.argument<=8uL);var seen=0
   repeat(h.argument.toInt()){
    val key=requireNotNull(headAt(raw,cursor));require(key.kind==0&&key.argument<8uL)
    val bit=1 shl key.argument.toInt();require((seen and bit)==0);seen=seen or bit;cursor+=key.width
    cursor=valueEnd(raw,cursor,open+1)
   }
  }
 }
 return cursor
}
fun skipped(raw:ByteArray,depth:Long=0):Int?=runCatching{
 require(raw.size<=1024&&depth.toULong()<=8uL);valueEnd(raw,0,depth.toInt())
}.getOrNull()
fun telemetry(raw:ByteArray):Telemetry?=runCatching{
 require(raw.size in 7..1024);require(valueEnd(raw,0,0)==raw.size)
 val root=requireNotNull(headAt(raw));require(root.kind==5&&root.argument in 3uL..8uL)
 var cursor=root.width;var ticks:ULong?=null;var device:ByteArray?=null;var readings:IntArray?=null
 repeat(root.argument.toInt()){
  val key=requireNotNull(headAt(raw,cursor));cursor+=key.width
  val h=requireNotNull(headAt(raw,cursor))
  when(key.argument){
   0uL->{require(h.kind==0);ticks=h.argument;cursor+=h.width}
   1uL->{require(h.kind==2);val at=cursor+h.width;device=raw.copyOfRange(at,at+h.argument.toInt());cursor=at+h.argument.toInt()}
   2uL->{require(h.kind==4);cursor+=h.width;readings=IntArray(h.argument.toInt()){
    val sample=requireNotNull(headAt(raw,cursor));require(sample.kind in 0..1&&sample.argument<=32767uL);cursor+=sample.width
    if(sample.kind==0)sample.argument.toInt()else -1-sample.argument.toInt()
   }}
   else->cursor=valueEnd(raw,cursor,1)
  }
 }
 require(cursor==raw.size);Telemetry(requireNotNull(ticks),requireNotNull(device),requireNotNull(readings))
}.getOrNull()
fun headBytes(kind:Int,argument:ULong):ByteArray{
 require(kind in listOf(0,1,2,4,5))
 if(argument<24uL)return byteArrayOf(((kind shl 5) or argument.toInt()).toByte())
 val width=when{argument<=255uL->1;argument<=65535uL->2;argument<=4294967295uL->4;else->8}
 val selector=when(width){1->24;2->25;4->26;else->27}
 return byteArrayOf(((kind shl 5) or selector).toByte())+ByteArray(width){(argument shr (8*(width-1-it))).toByte()}
}
fun validRecord(record:Telemetry)=record.device.size<=32&&record.readings.size<=64&&record.readings.all{it in -32768..32767}
fun preferred(record:Telemetry):ByteArray{
 require(validRecord(record))
 var output=byteArrayOf(0xa3.toByte(),0)+headBytes(0,record.ticks)+byteArrayOf(1)+headBytes(2,record.device.size.toULong())+record.device+byteArrayOf(2)+headBytes(4,record.readings.size.toULong())
 for(sample in record.readings)output+=headBytes(if(sample<0)1 else 0,(if(sample<0)-1-sample else sample).toULong())
 return output
}
fun flat(record:Telemetry):ByteArray{
 require(validRecord(record));val image=ByteArray(176){165.toByte()}
 little(record.ticks.toLong(),8).copyInto(image);little(record.device.size.toLong(),4).copyInto(image,8);little(record.readings.size.toLong(),4).copyInto(image,12);record.device.copyInto(image,16)
 for(i in record.readings.indices)little(record.readings[i].toLong(),2).copyInto(image,48+2*i)
 return image
}
val traceInput=bytes("b80403a201824280ff21008002980a00181718182037381818ff190100197fff397fff01580300ff4118001b0102030405060708")
val worked=Telemetry(0x0102030405060708uL,bytes("00ff41"),intArrayOf(0,23,24,-1,-24,-25,255,256,32767,-32768))
fun example(ticks:ULong=0uL,length:Int=3,count:Int=10):Telemetry{
 val pattern=intArrayOf(0,23,24,-1,-24,-25,255,256,32767,-32768)
 return Telemetry(ticks,ByteArray(length){(it*97+13).toByte()},IntArray(count){pattern[it%pattern.size]})
}
val arenas=linkedMapOf("source" to 1280,"record" to 256,"destination" to 320,"workspace" to 8192)
fun AssemblyCase.store(label:String,raw:ByteArray){require(raw.size%4==0);for(i in raw.indices step 4){var word=0;for(j in 0..3)word=word or ((raw[i+j].toInt() and 255) shl (8*j));setWord(label,word,i)}}
fun AssemblyCase.install(label:String,raw:ByteArray=byteArrayOf(),offset:Int=0){val value=ByteArray(arenas.getValue(label)){165.toByte()};raw.copyInto(value,offset);store(label,value)}
fun AssemblyCase.arrange():List<Long>{
 for(name in arenas.keys)install(name)
 val saved=List(9){0x3456789a00000000L+it*733};for(i in 0..7)setRegister("s$i",saved[i]);setRegister("fp",saved[8]);val top=readDataAddress("workspace",8192);setRegister("sp",top);return saved+top
}
fun AssemblyCase.retained(saved:List<Long>){for(i in 0..7)expectRegister("s$i",saved[i]);expectRegister("fp",saved[8]);expectRegister("sp",saved[9]);expectOutput("")}
fun AssemblyCase.image(label:String,raw:ByteArray=byteArrayOf(),offset:Int=0){val expected=ByteArray(arenas.getValue(label)){165.toByte()};raw.copyInto(expected,offset);val actual=readBytes(label,expected.size);val at=actual.indices.firstOrNull{actual[it]!=(expected[it].toInt() and 255)};expectCondition(at==null,"$label differs at byte $at; check active fields and preserved tails")}
fun fits(address:Long,length:Int,base:Long,size:Int)=length<=size&&address>=base&&address-base<=size-length
fun AssemblyCase.observe(reads:List<Pair<Long,Int>>,writes:List<Pair<Long,Int>>,action:()->Unit){
 val low=readDataAddress("source");val top=readDataAddress("workspace",8192);val objects=listOf("source","record","destination").map{readDataAddress(it) to arenas.getValue(it)};val access=traceMemoryAccesses(low,(top-low).toInt(),maxAccesses=500000)
 val files=provideFiles(emptyMap());try{action()}finally{access.close()}
 for(item in access.snapshot()){
  val declared=(if(item.write)writes else reads).any{(base,size)->fits(item.address,item.length,base,size)}
  val caller=objects.any{(base,size)->item.address<base+size&&base<item.address+item.length}
  expectCondition(!caller||declared,"Execution access crosses an active caller interval, publishes on failure, or reads an identifier before preflight")
 }
 files.expectAllClosed();expectCondition(files.snapshot().isEmpty(),"Memory entries do not perform file I/O")
}
fun AssemblyCase.finish(saved:List<Long>,status:Int,count:Long){expectRegister("v0",status);expectRegister("v1",count);retained(saved)}
fun selected(raw:ByteArray,count:Long):ByteArray=ByteArray(count.toInt()){if(it<raw.size)raw[it]else 165.toByte()}
fun AssemblyCase.headCheck(raw:ByteArray,count:Long=raw.size.toLong(),index:Int=0){
 val saved=arrange();val offset=index%17;install("source",raw,offset);val logical=if(count.toULong()<raw.size.toULong())raw.copyOf(count.toInt())else raw;val h=if(count==0L)null else headAt(logical)
 val extent=if(h!=null)h.width else if(count==0L)0 else 1;val pointer=readDataAddress("source",offset.toLong());setRegister("a0",if(count==0L)0x123456780001L else pointer);setRegister("a1",count);setRegister("a2",-1L);setRegister("a3",-1L)
 observe(listOf(pointer to extent),emptyList()){callFunction("cbor_head","function_return");finish(saved,if(h==null)1 else 0,h?.width?.toLong()?:0L);expectRegister("a2",h?.kind?:0);expectRegister("a3",h?.argument?.toLong()?:0L)}
 image("source",raw,offset);image("record");image("destination")
}
fun AssemblyCase.skipCheck(raw:ByteArray,depth:Long=0,count:Long=raw.size.toLong(),index:Int=0){
 val saved=arrange();val offset=index%17;install("source",raw,offset);val excessive=count.toULong()>1024uL;val early=excessive||depth.toULong()>8uL||count==0L;val logical=if(excessive)byteArrayOf()else selected(raw,count);val consumed=if(early)null else skipped(logical,depth);val status=if(excessive)2 else if(consumed==null)1 else 0
 val pointer=readDataAddress("source",offset.toLong());setRegister("a0",if(early)0x123456780001L else pointer);setRegister("a1",count);setRegister("a2",depth)
 observe(if(early)emptyList()else listOf(pointer to (consumed?:logical.size)),emptyList()){callFunction("cbor_skip","function_return");finish(saved,status,consumed?.toLong()?:0L)}
 image("source",raw,offset);image("record");image("destination")
}
fun AssemblyCase.decodeCheck(raw:ByteArray,count:Long=raw.size.toLong(),index:Int=0){
 val saved=arrange();val offset=index%17;val recordOffset=index%3*8;install("source",raw,offset);val excessive=count.toULong()>1024uL;val early=excessive||count<7L;val decoded=if(early)null else telemetry(selected(raw,count));val status=if(excessive)2 else if(decoded==null)1 else 0
 val input=readDataAddress("source",offset.toLong());val output=readDataAddress("record",recordOffset.toLong());setRegister("a0",if(early)0x123456780001L else input);setRegister("a1",count);setRegister("a2",if(early)0x123456780003L else output)
 val writes=if(decoded==null)emptyList()else listOf(output to 16,output+16 to decoded.device.size,output+48 to decoded.readings.size*2)
 observe(if(early)emptyList()else listOf(input to count.toInt()),writes){callFunction("telemetry_decode","function_return");finish(saved,status,if(status==0)count else 0L)}
 image("source",raw,offset);image("record",if(decoded==null)byteArrayOf()else flat(decoded),recordOffset);image("destination")
}
fun AssemblyCase.recordCheck(record:Telemetry,capacity:Long?=null,index:Int=0,badLength:Pair<Long,Long>?=null){
 val saved=arrange();val offset=index%3*8;val destinationOffset=index%19;val raw=flat(record);if(badLength!=null){little(badLength.first,4).copyInto(raw,8);little(badLength.second,4).copyInto(raw,12)};install("record",raw,offset)
 val accepted=badLength==null;val encoded=if(accepted)preferred(record)else byteArrayOf();val enough=capacity!=null&&capacity.toULong()>=encoded.size.toULong()&&accepted
 val pointer=readDataAddress("record",offset.toLong());val output=readDataAddress("destination",destinationOffset.toLong());setRegister("a0",pointer);setRegister("a1",if(enough)output else 0x123456780005L);setRegister("a2",capacity?:0)
 val reads=mutableListOf(pointer to 16);if(accepted)reads+=pointer+48 to 2*record.readings.size;if(enough)reads+=pointer+16 to record.device.size
 val status=if(!accepted)1 else if(capacity!=null&&!enough)2 else 0
 observe(reads,if(enough)listOf(output to encoded.size)else emptyList()){callFunction(if(capacity==null)"telemetry_bound"else"telemetry_encode","function_return");finish(saved,status,if(status==0)encoded.size.toLong()else 0L)}
 image("record",raw,offset);image("source");image("destination",if(enough)encoded else byteArrayOf(),destinationOffset)
}
fun AssemblyCase.pipelineCheck(raw:ByteArray,capacity:Long=241){
 val saved=arrange();install("source",raw);setWord("mode",5);store("byte_count",little(raw.size.toLong(),8));store("byte_capacity",little(capacity,8))
 val h=headAt(raw);val consumed=skipped(raw);val decoded=telemetry(raw);val encoded=decoded?.let(::preferred)
 val status=when{h==null->1;raw.size>1024->2;consumed==null||decoded==null->1;capacity.toULong()<encoded!!.size.toULong()->2;else->0}
 val files=provideFiles(emptyMap());startAt("main");runUntil("results_ready");finish(saved,status,if(status==0)encoded!!.size.toLong()else 0L)
 image("source",raw);image("record",decoded?.let(::flat)?:byteArrayOf());image("destination",if(status==0)encoded!!else byteArrayOf())
 files.expectAllClosed();expectCondition(files.snapshot().isEmpty(),"Pipeline performs no file I/O");runToExit();expectWord("status",status);expectWord("count",if(status==0)encoded!!.size else 0);expectWord("count",0,4)
}

case("initial byte 00", instructionLimit=2000000) { headCheck(bytes("0080ff0123456789ab"),index=0) }
case("initial byte 01", instructionLimit=2000000) { headCheck(bytes("0180ff0123456789ab"),index=1) }
case("initial byte 02", instructionLimit=2000000) { headCheck(bytes("0280ff0123456789ab"),index=2) }
case("initial byte 03", instructionLimit=2000000) { headCheck(bytes("0380ff0123456789ab"),index=3) }
case("initial byte 04", instructionLimit=2000000) { headCheck(bytes("0480ff0123456789ab"),index=4) }
case("initial byte 05", instructionLimit=2000000) { headCheck(bytes("0580ff0123456789ab"),index=5) }
case("initial byte 06", instructionLimit=2000000) { headCheck(bytes("0680ff0123456789ab"),index=6) }
case("initial byte 07", instructionLimit=2000000) { headCheck(bytes("0780ff0123456789ab"),index=7) }
case("initial byte 08", instructionLimit=2000000) { headCheck(bytes("0880ff0123456789ab"),index=8) }
case("initial byte 09", instructionLimit=2000000) { headCheck(bytes("0980ff0123456789ab"),index=9) }
case("initial byte 0a", instructionLimit=2000000) { headCheck(bytes("0a80ff0123456789ab"),index=10) }
case("initial byte 0b", instructionLimit=2000000) { headCheck(bytes("0b80ff0123456789ab"),index=11) }
case("initial byte 0c", instructionLimit=2000000) { headCheck(bytes("0c80ff0123456789ab"),index=12) }
case("initial byte 0d", instructionLimit=2000000) { headCheck(bytes("0d80ff0123456789ab"),index=13) }
case("initial byte 0e", instructionLimit=2000000) { headCheck(bytes("0e80ff0123456789ab"),index=14) }
case("initial byte 0f", instructionLimit=2000000) { headCheck(bytes("0f80ff0123456789ab"),index=15) }
case("initial byte 10", instructionLimit=2000000) { headCheck(bytes("1080ff0123456789ab"),index=16) }
case("initial byte 11", instructionLimit=2000000) { headCheck(bytes("1180ff0123456789ab"),index=17) }
case("initial byte 12", instructionLimit=2000000) { headCheck(bytes("1280ff0123456789ab"),index=18) }
case("initial byte 13", instructionLimit=2000000) { headCheck(bytes("1380ff0123456789ab"),index=19) }
case("initial byte 14", instructionLimit=2000000) { headCheck(bytes("1480ff0123456789ab"),index=20) }
case("initial byte 15", instructionLimit=2000000) { headCheck(bytes("1580ff0123456789ab"),index=21) }
case("initial byte 16", instructionLimit=2000000) { headCheck(bytes("1680ff0123456789ab"),index=22) }
case("initial byte 17", instructionLimit=2000000) { headCheck(bytes("1780ff0123456789ab"),index=23) }
case("initial byte 18", instructionLimit=2000000) { headCheck(bytes("1880ff0123456789ab"),index=24) }
case("initial byte 19", instructionLimit=2000000) { headCheck(bytes("1980ff0123456789ab"),index=25) }
case("initial byte 1a", instructionLimit=2000000) { headCheck(bytes("1a80ff0123456789ab"),index=26) }
case("initial byte 1b", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),index=27) }
case("initial byte 1c", instructionLimit=2000000) { headCheck(bytes("1c80ff0123456789ab"),index=28) }
case("initial byte 1d", instructionLimit=2000000) { headCheck(bytes("1d80ff0123456789ab"),index=29) }
case("initial byte 1e", instructionLimit=2000000) { headCheck(bytes("1e80ff0123456789ab"),index=30) }
case("initial byte 1f", instructionLimit=2000000) { headCheck(bytes("1f80ff0123456789ab"),index=31) }
case("initial byte 20", instructionLimit=2000000) { headCheck(bytes("2080ff0123456789ab"),index=32) }
case("initial byte 21", instructionLimit=2000000) { headCheck(bytes("2180ff0123456789ab"),index=33) }
case("initial byte 22", instructionLimit=2000000) { headCheck(bytes("2280ff0123456789ab"),index=34) }
case("initial byte 23", instructionLimit=2000000) { headCheck(bytes("2380ff0123456789ab"),index=35) }
case("initial byte 24", instructionLimit=2000000) { headCheck(bytes("2480ff0123456789ab"),index=36) }
case("initial byte 25", instructionLimit=2000000) { headCheck(bytes("2580ff0123456789ab"),index=37) }
case("initial byte 26", instructionLimit=2000000) { headCheck(bytes("2680ff0123456789ab"),index=38) }
case("initial byte 27", instructionLimit=2000000) { headCheck(bytes("2780ff0123456789ab"),index=39) }
case("initial byte 28", instructionLimit=2000000) { headCheck(bytes("2880ff0123456789ab"),index=40) }
case("initial byte 29", instructionLimit=2000000) { headCheck(bytes("2980ff0123456789ab"),index=41) }
case("initial byte 2a", instructionLimit=2000000) { headCheck(bytes("2a80ff0123456789ab"),index=42) }
case("initial byte 2b", instructionLimit=2000000) { headCheck(bytes("2b80ff0123456789ab"),index=43) }
case("initial byte 2c", instructionLimit=2000000) { headCheck(bytes("2c80ff0123456789ab"),index=44) }
case("initial byte 2d", instructionLimit=2000000) { headCheck(bytes("2d80ff0123456789ab"),index=45) }
case("initial byte 2e", instructionLimit=2000000) { headCheck(bytes("2e80ff0123456789ab"),index=46) }
case("initial byte 2f", instructionLimit=2000000) { headCheck(bytes("2f80ff0123456789ab"),index=47) }
case("initial byte 30", instructionLimit=2000000) { headCheck(bytes("3080ff0123456789ab"),index=48) }
case("initial byte 31", instructionLimit=2000000) { headCheck(bytes("3180ff0123456789ab"),index=49) }
case("initial byte 32", instructionLimit=2000000) { headCheck(bytes("3280ff0123456789ab"),index=50) }
case("initial byte 33", instructionLimit=2000000) { headCheck(bytes("3380ff0123456789ab"),index=51) }
case("initial byte 34", instructionLimit=2000000) { headCheck(bytes("3480ff0123456789ab"),index=52) }
case("initial byte 35", instructionLimit=2000000) { headCheck(bytes("3580ff0123456789ab"),index=53) }
case("initial byte 36", instructionLimit=2000000) { headCheck(bytes("3680ff0123456789ab"),index=54) }
case("initial byte 37", instructionLimit=2000000) { headCheck(bytes("3780ff0123456789ab"),index=55) }
case("initial byte 38", instructionLimit=2000000) { headCheck(bytes("3880ff0123456789ab"),index=56) }
case("initial byte 39", instructionLimit=2000000) { headCheck(bytes("3980ff0123456789ab"),index=57) }
case("initial byte 3a", instructionLimit=2000000) { headCheck(bytes("3a80ff0123456789ab"),index=58) }
case("initial byte 3b", instructionLimit=2000000) { headCheck(bytes("3b80ff0123456789ab"),index=59) }
case("initial byte 3c", instructionLimit=2000000) { headCheck(bytes("3c80ff0123456789ab"),index=60) }
case("initial byte 3d", instructionLimit=2000000) { headCheck(bytes("3d80ff0123456789ab"),index=61) }
case("initial byte 3e", instructionLimit=2000000) { headCheck(bytes("3e80ff0123456789ab"),index=62) }
case("initial byte 3f", instructionLimit=2000000) { headCheck(bytes("3f80ff0123456789ab"),index=63) }
case("initial byte 40", instructionLimit=2000000) { headCheck(bytes("4080ff0123456789ab"),index=64) }
case("initial byte 41", instructionLimit=2000000) { headCheck(bytes("4180ff0123456789ab"),index=65) }
case("initial byte 42", instructionLimit=2000000) { headCheck(bytes("4280ff0123456789ab"),index=66) }
case("initial byte 43", instructionLimit=2000000) { headCheck(bytes("4380ff0123456789ab"),index=67) }
case("initial byte 44", instructionLimit=2000000) { headCheck(bytes("4480ff0123456789ab"),index=68) }
case("initial byte 45", instructionLimit=2000000) { headCheck(bytes("4580ff0123456789ab"),index=69) }
case("initial byte 46", instructionLimit=2000000) { headCheck(bytes("4680ff0123456789ab"),index=70) }
case("initial byte 47", instructionLimit=2000000) { headCheck(bytes("4780ff0123456789ab"),index=71) }
case("initial byte 48", instructionLimit=2000000) { headCheck(bytes("4880ff0123456789ab"),index=72) }
case("initial byte 49", instructionLimit=2000000) { headCheck(bytes("4980ff0123456789ab"),index=73) }
case("initial byte 4a", instructionLimit=2000000) { headCheck(bytes("4a80ff0123456789ab"),index=74) }
case("initial byte 4b", instructionLimit=2000000) { headCheck(bytes("4b80ff0123456789ab"),index=75) }
case("initial byte 4c", instructionLimit=2000000) { headCheck(bytes("4c80ff0123456789ab"),index=76) }
case("initial byte 4d", instructionLimit=2000000) { headCheck(bytes("4d80ff0123456789ab"),index=77) }
case("initial byte 4e", instructionLimit=2000000) { headCheck(bytes("4e80ff0123456789ab"),index=78) }
case("initial byte 4f", instructionLimit=2000000) { headCheck(bytes("4f80ff0123456789ab"),index=79) }
case("initial byte 50", instructionLimit=2000000) { headCheck(bytes("5080ff0123456789ab"),index=80) }
case("initial byte 51", instructionLimit=2000000) { headCheck(bytes("5180ff0123456789ab"),index=81) }
case("initial byte 52", instructionLimit=2000000) { headCheck(bytes("5280ff0123456789ab"),index=82) }
case("initial byte 53", instructionLimit=2000000) { headCheck(bytes("5380ff0123456789ab"),index=83) }
case("initial byte 54", instructionLimit=2000000) { headCheck(bytes("5480ff0123456789ab"),index=84) }
case("initial byte 55", instructionLimit=2000000) { headCheck(bytes("5580ff0123456789ab"),index=85) }
case("initial byte 56", instructionLimit=2000000) { headCheck(bytes("5680ff0123456789ab"),index=86) }
case("initial byte 57", instructionLimit=2000000) { headCheck(bytes("5780ff0123456789ab"),index=87) }
case("initial byte 58", instructionLimit=2000000) { headCheck(bytes("5880ff0123456789ab"),index=88) }
case("initial byte 59", instructionLimit=2000000) { headCheck(bytes("5980ff0123456789ab"),index=89) }
case("initial byte 5a", instructionLimit=2000000) { headCheck(bytes("5a80ff0123456789ab"),index=90) }
case("initial byte 5b", instructionLimit=2000000) { headCheck(bytes("5b80ff0123456789ab"),index=91) }
case("initial byte 5c", instructionLimit=2000000) { headCheck(bytes("5c80ff0123456789ab"),index=92) }
case("initial byte 5d", instructionLimit=2000000) { headCheck(bytes("5d80ff0123456789ab"),index=93) }
case("initial byte 5e", instructionLimit=2000000) { headCheck(bytes("5e80ff0123456789ab"),index=94) }
case("initial byte 5f", instructionLimit=2000000) { headCheck(bytes("5f80ff0123456789ab"),index=95) }
case("initial byte 60", instructionLimit=2000000) { headCheck(bytes("6080ff0123456789ab"),index=96) }
case("initial byte 61", instructionLimit=2000000) { headCheck(bytes("6180ff0123456789ab"),index=97) }
case("initial byte 62", instructionLimit=2000000) { headCheck(bytes("6280ff0123456789ab"),index=98) }
case("initial byte 63", instructionLimit=2000000) { headCheck(bytes("6380ff0123456789ab"),index=99) }
case("initial byte 64", instructionLimit=2000000) { headCheck(bytes("6480ff0123456789ab"),index=100) }
case("initial byte 65", instructionLimit=2000000) { headCheck(bytes("6580ff0123456789ab"),index=101) }
case("initial byte 66", instructionLimit=2000000) { headCheck(bytes("6680ff0123456789ab"),index=102) }
case("initial byte 67", instructionLimit=2000000) { headCheck(bytes("6780ff0123456789ab"),index=103) }
case("initial byte 68", instructionLimit=2000000) { headCheck(bytes("6880ff0123456789ab"),index=104) }
case("initial byte 69", instructionLimit=2000000) { headCheck(bytes("6980ff0123456789ab"),index=105) }
case("initial byte 6a", instructionLimit=2000000) { headCheck(bytes("6a80ff0123456789ab"),index=106) }
case("initial byte 6b", instructionLimit=2000000) { headCheck(bytes("6b80ff0123456789ab"),index=107) }
case("initial byte 6c", instructionLimit=2000000) { headCheck(bytes("6c80ff0123456789ab"),index=108) }
case("initial byte 6d", instructionLimit=2000000) { headCheck(bytes("6d80ff0123456789ab"),index=109) }
case("initial byte 6e", instructionLimit=2000000) { headCheck(bytes("6e80ff0123456789ab"),index=110) }
case("initial byte 6f", instructionLimit=2000000) { headCheck(bytes("6f80ff0123456789ab"),index=111) }
case("initial byte 70", instructionLimit=2000000) { headCheck(bytes("7080ff0123456789ab"),index=112) }
case("initial byte 71", instructionLimit=2000000) { headCheck(bytes("7180ff0123456789ab"),index=113) }
case("initial byte 72", instructionLimit=2000000) { headCheck(bytes("7280ff0123456789ab"),index=114) }
case("initial byte 73", instructionLimit=2000000) { headCheck(bytes("7380ff0123456789ab"),index=115) }
case("initial byte 74", instructionLimit=2000000) { headCheck(bytes("7480ff0123456789ab"),index=116) }
case("initial byte 75", instructionLimit=2000000) { headCheck(bytes("7580ff0123456789ab"),index=117) }
case("initial byte 76", instructionLimit=2000000) { headCheck(bytes("7680ff0123456789ab"),index=118) }
case("initial byte 77", instructionLimit=2000000) { headCheck(bytes("7780ff0123456789ab"),index=119) }
case("initial byte 78", instructionLimit=2000000) { headCheck(bytes("7880ff0123456789ab"),index=120) }
case("initial byte 79", instructionLimit=2000000) { headCheck(bytes("7980ff0123456789ab"),index=121) }
case("initial byte 7a", instructionLimit=2000000) { headCheck(bytes("7a80ff0123456789ab"),index=122) }
case("initial byte 7b", instructionLimit=2000000) { headCheck(bytes("7b80ff0123456789ab"),index=123) }
case("initial byte 7c", instructionLimit=2000000) { headCheck(bytes("7c80ff0123456789ab"),index=124) }
case("initial byte 7d", instructionLimit=2000000) { headCheck(bytes("7d80ff0123456789ab"),index=125) }
case("initial byte 7e", instructionLimit=2000000) { headCheck(bytes("7e80ff0123456789ab"),index=126) }
case("initial byte 7f", instructionLimit=2000000) { headCheck(bytes("7f80ff0123456789ab"),index=127) }
case("initial byte 80", instructionLimit=2000000) { headCheck(bytes("8080ff0123456789ab"),index=128) }
case("initial byte 81", instructionLimit=2000000) { headCheck(bytes("8180ff0123456789ab"),index=129) }
case("initial byte 82", instructionLimit=2000000) { headCheck(bytes("8280ff0123456789ab"),index=130) }
case("initial byte 83", instructionLimit=2000000) { headCheck(bytes("8380ff0123456789ab"),index=131) }
case("initial byte 84", instructionLimit=2000000) { headCheck(bytes("8480ff0123456789ab"),index=132) }
case("initial byte 85", instructionLimit=2000000) { headCheck(bytes("8580ff0123456789ab"),index=133) }
case("initial byte 86", instructionLimit=2000000) { headCheck(bytes("8680ff0123456789ab"),index=134) }
case("initial byte 87", instructionLimit=2000000) { headCheck(bytes("8780ff0123456789ab"),index=135) }
case("initial byte 88", instructionLimit=2000000) { headCheck(bytes("8880ff0123456789ab"),index=136) }
case("initial byte 89", instructionLimit=2000000) { headCheck(bytes("8980ff0123456789ab"),index=137) }
case("initial byte 8a", instructionLimit=2000000) { headCheck(bytes("8a80ff0123456789ab"),index=138) }
case("initial byte 8b", instructionLimit=2000000) { headCheck(bytes("8b80ff0123456789ab"),index=139) }
case("initial byte 8c", instructionLimit=2000000) { headCheck(bytes("8c80ff0123456789ab"),index=140) }
case("initial byte 8d", instructionLimit=2000000) { headCheck(bytes("8d80ff0123456789ab"),index=141) }
case("initial byte 8e", instructionLimit=2000000) { headCheck(bytes("8e80ff0123456789ab"),index=142) }
case("initial byte 8f", instructionLimit=2000000) { headCheck(bytes("8f80ff0123456789ab"),index=143) }
case("initial byte 90", instructionLimit=2000000) { headCheck(bytes("9080ff0123456789ab"),index=144) }
case("initial byte 91", instructionLimit=2000000) { headCheck(bytes("9180ff0123456789ab"),index=145) }
case("initial byte 92", instructionLimit=2000000) { headCheck(bytes("9280ff0123456789ab"),index=146) }
case("initial byte 93", instructionLimit=2000000) { headCheck(bytes("9380ff0123456789ab"),index=147) }
case("initial byte 94", instructionLimit=2000000) { headCheck(bytes("9480ff0123456789ab"),index=148) }
case("initial byte 95", instructionLimit=2000000) { headCheck(bytes("9580ff0123456789ab"),index=149) }
case("initial byte 96", instructionLimit=2000000) { headCheck(bytes("9680ff0123456789ab"),index=150) }
case("initial byte 97", instructionLimit=2000000) { headCheck(bytes("9780ff0123456789ab"),index=151) }
case("initial byte 98", instructionLimit=2000000) { headCheck(bytes("9880ff0123456789ab"),index=152) }
case("initial byte 99", instructionLimit=2000000) { headCheck(bytes("9980ff0123456789ab"),index=153) }
case("initial byte 9a", instructionLimit=2000000) { headCheck(bytes("9a80ff0123456789ab"),index=154) }
case("initial byte 9b", instructionLimit=2000000) { headCheck(bytes("9b80ff0123456789ab"),index=155) }
case("initial byte 9c", instructionLimit=2000000) { headCheck(bytes("9c80ff0123456789ab"),index=156) }
case("initial byte 9d", instructionLimit=2000000) { headCheck(bytes("9d80ff0123456789ab"),index=157) }
case("initial byte 9e", instructionLimit=2000000) { headCheck(bytes("9e80ff0123456789ab"),index=158) }
case("initial byte 9f", instructionLimit=2000000) { headCheck(bytes("9f80ff0123456789ab"),index=159) }
case("initial byte a0", instructionLimit=2000000) { headCheck(bytes("a080ff0123456789ab"),index=160) }
case("initial byte a1", instructionLimit=2000000) { headCheck(bytes("a180ff0123456789ab"),index=161) }
case("initial byte a2", instructionLimit=2000000) { headCheck(bytes("a280ff0123456789ab"),index=162) }
case("initial byte a3", instructionLimit=2000000) { headCheck(bytes("a380ff0123456789ab"),index=163) }
case("initial byte a4", instructionLimit=2000000) { headCheck(bytes("a480ff0123456789ab"),index=164) }
case("initial byte a5", instructionLimit=2000000) { headCheck(bytes("a580ff0123456789ab"),index=165) }
case("initial byte a6", instructionLimit=2000000) { headCheck(bytes("a680ff0123456789ab"),index=166) }
case("initial byte a7", instructionLimit=2000000) { headCheck(bytes("a780ff0123456789ab"),index=167) }
case("initial byte a8", instructionLimit=2000000) { headCheck(bytes("a880ff0123456789ab"),index=168) }
case("initial byte a9", instructionLimit=2000000) { headCheck(bytes("a980ff0123456789ab"),index=169) }
case("initial byte aa", instructionLimit=2000000) { headCheck(bytes("aa80ff0123456789ab"),index=170) }
case("initial byte ab", instructionLimit=2000000) { headCheck(bytes("ab80ff0123456789ab"),index=171) }
case("initial byte ac", instructionLimit=2000000) { headCheck(bytes("ac80ff0123456789ab"),index=172) }
case("initial byte ad", instructionLimit=2000000) { headCheck(bytes("ad80ff0123456789ab"),index=173) }
case("initial byte ae", instructionLimit=2000000) { headCheck(bytes("ae80ff0123456789ab"),index=174) }
case("initial byte af", instructionLimit=2000000) { headCheck(bytes("af80ff0123456789ab"),index=175) }
case("initial byte b0", instructionLimit=2000000) { headCheck(bytes("b080ff0123456789ab"),index=176) }
case("initial byte b1", instructionLimit=2000000) { headCheck(bytes("b180ff0123456789ab"),index=177) }
case("initial byte b2", instructionLimit=2000000) { headCheck(bytes("b280ff0123456789ab"),index=178) }
case("initial byte b3", instructionLimit=2000000) { headCheck(bytes("b380ff0123456789ab"),index=179) }
case("initial byte b4", instructionLimit=2000000) { headCheck(bytes("b480ff0123456789ab"),index=180) }
case("initial byte b5", instructionLimit=2000000) { headCheck(bytes("b580ff0123456789ab"),index=181) }
case("initial byte b6", instructionLimit=2000000) { headCheck(bytes("b680ff0123456789ab"),index=182) }
case("initial byte b7", instructionLimit=2000000) { headCheck(bytes("b780ff0123456789ab"),index=183) }
case("initial byte b8", instructionLimit=2000000) { headCheck(bytes("b880ff0123456789ab"),index=184) }
case("initial byte b9", instructionLimit=2000000) { headCheck(bytes("b980ff0123456789ab"),index=185) }
case("initial byte ba", instructionLimit=2000000) { headCheck(bytes("ba80ff0123456789ab"),index=186) }
case("initial byte bb", instructionLimit=2000000) { headCheck(bytes("bb80ff0123456789ab"),index=187) }
case("initial byte bc", instructionLimit=2000000) { headCheck(bytes("bc80ff0123456789ab"),index=188) }
case("initial byte bd", instructionLimit=2000000) { headCheck(bytes("bd80ff0123456789ab"),index=189) }
case("initial byte be", instructionLimit=2000000) { headCheck(bytes("be80ff0123456789ab"),index=190) }
case("initial byte bf", instructionLimit=2000000) { headCheck(bytes("bf80ff0123456789ab"),index=191) }
case("initial byte c0", instructionLimit=2000000) { headCheck(bytes("c080ff0123456789ab"),index=192) }
case("initial byte c1", instructionLimit=2000000) { headCheck(bytes("c180ff0123456789ab"),index=193) }
case("initial byte c2", instructionLimit=2000000) { headCheck(bytes("c280ff0123456789ab"),index=194) }
case("initial byte c3", instructionLimit=2000000) { headCheck(bytes("c380ff0123456789ab"),index=195) }
case("initial byte c4", instructionLimit=2000000) { headCheck(bytes("c480ff0123456789ab"),index=196) }
case("initial byte c5", instructionLimit=2000000) { headCheck(bytes("c580ff0123456789ab"),index=197) }
case("initial byte c6", instructionLimit=2000000) { headCheck(bytes("c680ff0123456789ab"),index=198) }
case("initial byte c7", instructionLimit=2000000) { headCheck(bytes("c780ff0123456789ab"),index=199) }
case("initial byte c8", instructionLimit=2000000) { headCheck(bytes("c880ff0123456789ab"),index=200) }
case("initial byte c9", instructionLimit=2000000) { headCheck(bytes("c980ff0123456789ab"),index=201) }
case("initial byte ca", instructionLimit=2000000) { headCheck(bytes("ca80ff0123456789ab"),index=202) }
case("initial byte cb", instructionLimit=2000000) { headCheck(bytes("cb80ff0123456789ab"),index=203) }
case("initial byte cc", instructionLimit=2000000) { headCheck(bytes("cc80ff0123456789ab"),index=204) }
case("initial byte cd", instructionLimit=2000000) { headCheck(bytes("cd80ff0123456789ab"),index=205) }
case("initial byte ce", instructionLimit=2000000) { headCheck(bytes("ce80ff0123456789ab"),index=206) }
case("initial byte cf", instructionLimit=2000000) { headCheck(bytes("cf80ff0123456789ab"),index=207) }
case("initial byte d0", instructionLimit=2000000) { headCheck(bytes("d080ff0123456789ab"),index=208) }
case("initial byte d1", instructionLimit=2000000) { headCheck(bytes("d180ff0123456789ab"),index=209) }
case("initial byte d2", instructionLimit=2000000) { headCheck(bytes("d280ff0123456789ab"),index=210) }
case("initial byte d3", instructionLimit=2000000) { headCheck(bytes("d380ff0123456789ab"),index=211) }
case("initial byte d4", instructionLimit=2000000) { headCheck(bytes("d480ff0123456789ab"),index=212) }
case("initial byte d5", instructionLimit=2000000) { headCheck(bytes("d580ff0123456789ab"),index=213) }
case("initial byte d6", instructionLimit=2000000) { headCheck(bytes("d680ff0123456789ab"),index=214) }
case("initial byte d7", instructionLimit=2000000) { headCheck(bytes("d780ff0123456789ab"),index=215) }
case("initial byte d8", instructionLimit=2000000) { headCheck(bytes("d880ff0123456789ab"),index=216) }
case("initial byte d9", instructionLimit=2000000) { headCheck(bytes("d980ff0123456789ab"),index=217) }
case("initial byte da", instructionLimit=2000000) { headCheck(bytes("da80ff0123456789ab"),index=218) }
case("initial byte db", instructionLimit=2000000) { headCheck(bytes("db80ff0123456789ab"),index=219) }
case("initial byte dc", instructionLimit=2000000) { headCheck(bytes("dc80ff0123456789ab"),index=220) }
case("initial byte dd", instructionLimit=2000000) { headCheck(bytes("dd80ff0123456789ab"),index=221) }
case("initial byte de", instructionLimit=2000000) { headCheck(bytes("de80ff0123456789ab"),index=222) }
case("initial byte df", instructionLimit=2000000) { headCheck(bytes("df80ff0123456789ab"),index=223) }
case("initial byte e0", instructionLimit=2000000) { headCheck(bytes("e080ff0123456789ab"),index=224) }
case("initial byte e1", instructionLimit=2000000) { headCheck(bytes("e180ff0123456789ab"),index=225) }
case("initial byte e2", instructionLimit=2000000) { headCheck(bytes("e280ff0123456789ab"),index=226) }
case("initial byte e3", instructionLimit=2000000) { headCheck(bytes("e380ff0123456789ab"),index=227) }
case("initial byte e4", instructionLimit=2000000) { headCheck(bytes("e480ff0123456789ab"),index=228) }
case("initial byte e5", instructionLimit=2000000) { headCheck(bytes("e580ff0123456789ab"),index=229) }
case("initial byte e6", instructionLimit=2000000) { headCheck(bytes("e680ff0123456789ab"),index=230) }
case("initial byte e7", instructionLimit=2000000) { headCheck(bytes("e780ff0123456789ab"),index=231) }
case("initial byte e8", instructionLimit=2000000) { headCheck(bytes("e880ff0123456789ab"),index=232) }
case("initial byte e9", instructionLimit=2000000) { headCheck(bytes("e980ff0123456789ab"),index=233) }
case("initial byte ea", instructionLimit=2000000) { headCheck(bytes("ea80ff0123456789ab"),index=234) }
case("initial byte eb", instructionLimit=2000000) { headCheck(bytes("eb80ff0123456789ab"),index=235) }
case("initial byte ec", instructionLimit=2000000) { headCheck(bytes("ec80ff0123456789ab"),index=236) }
case("initial byte ed", instructionLimit=2000000) { headCheck(bytes("ed80ff0123456789ab"),index=237) }
case("initial byte ee", instructionLimit=2000000) { headCheck(bytes("ee80ff0123456789ab"),index=238) }
case("initial byte ef", instructionLimit=2000000) { headCheck(bytes("ef80ff0123456789ab"),index=239) }
case("initial byte f0", instructionLimit=2000000) { headCheck(bytes("f080ff0123456789ab"),index=240) }
case("initial byte f1", instructionLimit=2000000) { headCheck(bytes("f180ff0123456789ab"),index=241) }
case("initial byte f2", instructionLimit=2000000) { headCheck(bytes("f280ff0123456789ab"),index=242) }
case("initial byte f3", instructionLimit=2000000) { headCheck(bytes("f380ff0123456789ab"),index=243) }
case("initial byte f4", instructionLimit=2000000) { headCheck(bytes("f480ff0123456789ab"),index=244) }
case("initial byte f5", instructionLimit=2000000) { headCheck(bytes("f580ff0123456789ab"),index=245) }
case("initial byte f6", instructionLimit=2000000) { headCheck(bytes("f680ff0123456789ab"),index=246) }
case("initial byte f7", instructionLimit=2000000) { headCheck(bytes("f780ff0123456789ab"),index=247) }
case("initial byte f8", instructionLimit=2000000) { headCheck(bytes("f880ff0123456789ab"),index=248) }
case("initial byte f9", instructionLimit=2000000) { headCheck(bytes("f980ff0123456789ab"),index=249) }
case("initial byte fa", instructionLimit=2000000) { headCheck(bytes("fa80ff0123456789ab"),index=250) }
case("initial byte fb", instructionLimit=2000000) { headCheck(bytes("fb80ff0123456789ab"),index=251) }
case("initial byte fc", instructionLimit=2000000) { headCheck(bytes("fc80ff0123456789ab"),index=252) }
case("initial byte fd", instructionLimit=2000000) { headCheck(bytes("fd80ff0123456789ab"),index=253) }
case("initial byte fe", instructionLimit=2000000) { headCheck(bytes("fe80ff0123456789ab"),index=254) }
case("initial byte ff", instructionLimit=2000000) { headCheck(bytes("ff80ff0123456789ab"),index=255) }
case("truncated width 1 available 1", instructionLimit=2000000) { headCheck(bytes("18")) }
case("truncated width 2 available 1", instructionLimit=2000000) { headCheck(bytes("19")) }
case("truncated width 2 available 2", instructionLimit=2000000) { headCheck(bytes("1901")) }
case("truncated width 4 available 1", instructionLimit=2000000) { headCheck(bytes("1a")) }
case("truncated width 4 available 2", instructionLimit=2000000) { headCheck(bytes("1a01")) }
case("truncated width 4 available 3", instructionLimit=2000000) { headCheck(bytes("1a0102")) }
case("truncated width 4 available 4", instructionLimit=2000000) { headCheck(bytes("1a010203")) }
case("truncated width 8 available 1", instructionLimit=2000000) { headCheck(bytes("1b")) }
case("truncated width 8 available 2", instructionLimit=2000000) { headCheck(bytes("1b01")) }
case("truncated width 8 available 3", instructionLimit=2000000) { headCheck(bytes("1b0102")) }
case("truncated width 8 available 4", instructionLimit=2000000) { headCheck(bytes("1b010203")) }
case("truncated width 8 available 5", instructionLimit=2000000) { headCheck(bytes("1b01020304")) }
case("truncated width 8 available 6", instructionLimit=2000000) { headCheck(bytes("1b0102030405")) }
case("truncated width 8 available 7", instructionLimit=2000000) { headCheck(bytes("1b010203040506")) }
case("truncated width 8 available 8", instructionLimit=2000000) { headCheck(bytes("1b01020304050607")) }
case("full-width available 0", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),0L) }
case("full-width available 1", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),1L) }
case("full-width available 8", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),8L) }
case("full-width available 9", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),9L) }
case("full-width available 4294967296", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),4294967296L) }
case("full-width available 9223372036854775808", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),9223372036854775808uL.toLong()) }
case("full-width available 18446744073709551615", instructionLimit=2000000) { headCheck(bytes("1b80ff0123456789ab"),18446744073709551615uL.toLong()) }
