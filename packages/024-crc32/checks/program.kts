import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation
import java.util.zip.CRC32

// Public arithmetic oracle, authored from the published raw-state contract.
val crcMask=0xffffffffL
fun expectedUpdate(input:List<Int>,seed:Long):Long = input.fold(seed){state,byte->
 (1..8).fold(state xor byte.toLong()){remainder,_->(remainder ushr 1) xor (if(remainder and 1L==1L)0xedb88320L else 0L)}
}
fun expectedChecksum(input:List<Int>):Long=CRC32().apply{update(input.map{it.toByte()}.toByteArray())}.value
val crcWords=List(256){expectedUpdate(listOf(it),0)}
fun within(address:Long,width:Int,base:Long,size:Int)=address>=base && width.toLong()<=size && address-base<=size.toLong()-width
fun AssemblyCase.preserveSetup():List<Long>{
 val values=List(9){0x56789abc00000000L+it*997}
 for(index in 0..7)setRegister("s$index",values[index]);setRegister("fp",values[8]);setRegister("sp",0x7fff0000L)
 for(index in 0..9)setRegister("t$index",-1234L-index)
 return values
}
fun AssemblyCase.checkPreserved(values:List<Long>){for(index in 0..7)expectRegister("s$index",values[index]);expectRegister("fp",values[8]);expectRegister("sp",0x7fff0000L)}
fun AssemblyCase.callerRegions()=listOf("data" to 4160,"data_end" to 4,"crc_table" to 1088,"table_end" to 4,"path" to 128,"scratch" to 320,"scratch_end" to 4,"input_count" to 4,"initial_state" to 4,"exercise" to 4,"buffer_capacity" to 4,"method" to 4,"raw_result" to 4,"file_crc" to 4,"file_status" to 4).map{(label,size)->readDataAddress(label) to size}
fun AssemblyCase.checkUpdate(input:List<Int>,seed:Long,method:Int,caller:Boolean=false,finish:Boolean=true,continuation:String="function_return"){
 val offset=if(caller)0 else ((seed+input.size)%16).toInt();val tableOffset=if(caller)0 else 4*((seed+3*input.size)%16).toInt()
 val memory=MutableList(4160){165};input.forEachIndexed{index,byte->memory[offset+index]=byte}
 for(index in 0 until 1040)setWord("data",(0..3).fold(0){word,j->word or(memory[4*index+j] shl(8*j))},offset=4*index)
 for(index in 0 until 272)setWord("crc_table",if(method==1 && index-tableOffset/4 in crcWords.indices)crcWords[index-tableOffset/4].toInt() else 0x6a6b6c6d,offset=4*index)
 setWord("exercise",if(method==0)0 else 2);setWord("input_count",input.size);setWord("initial_state",seed.toInt());setWord("raw_result",-1)
 val saved=preserveSetup()
 if(caller)runUntil("kernel_call") else {setRegisterToDataAddress("a0","data",offset.toLong());setRegister("a1",input.size);setRegister("a2",seed);setRegisterToDataAddress("a3","crc_table",tableOffset.toLong())}
 val base=readDataAddress("data");val size=(readDataAddress("file_status")+4-base).toInt();val before=readBytes("data",size);val source=readDataAddress("data",offset.toLong());val table=readDataAddress("crc_table",tableOffset.toLong());val owned=callerRegions();val accesses=traceMemoryAccesses(base,size,maxAccesses=100000)
 if(caller)runUntil("kernel_done") else callFunction(if(method==0)"crc32_bit_update" else "crc32_table_update",continuation)
 accesses.close();val expected=expectedUpdate(input,seed);expectRegister("v0",expected);checkPreserved(saved)
 if(seed==crcMask)expectCondition(expected xor crcMask==expectedChecksum(input),"Raw definition agrees with independent JDK checksum")
 expectCondition(readBytes("data",size).contentEquals(before),"Updates preserve every caller byte")
 for(event in accesses.snapshot()){
  val known=owned.any{(start,n)->event.address<start+n && event.address+event.length>start}
  expectCondition(!event.write && (within(event.address,event.length,source,input.size) || (method==1 && input.isNotEmpty() && within(event.address,event.length,table,1024)) || !known),"Only logical source/table reads; an empty update reads neither")
 }
 if(finish){if(caller){runUntil("results_ready");expectWord("raw_result",expected.toInt())};runToExit();expectOutput("")}
}
fun AssemblyCase.checkTable(offset:Int=0){
 for(index in 0 until 272)setWord("crc_table",0x6a6b6c6d,offset=4*index)
 val saved=preserveSetup();setRegisterToDataAddress("a0","crc_table",offset.toLong())
 val base=readDataAddress("data");val size=(readDataAddress("file_status")+4-base).toInt();val before=readBytes("data",size);val table=readDataAddress("crc_table",offset.toLong());val owned=callerRegions();val accesses=traceMemoryAccesses(base,size,maxAccesses=100000)
 callFunction("crc32_table_init","function_return");accesses.close();expectRegister("v0",256);checkPreserved(saved)
 val initialized=BooleanArray(1024)
 for(event in accesses.snapshot()){
  val output=within(event.address,event.length,table,1024);val known=owned.any{(start,n)->event.address<start+n && event.address+event.length>start}
  expectCondition(if(event.write)output else output || !known,"Only the selected table may change")
  if(output)for(index in (event.address-table).toInt() until (event.address-table).toInt()+event.length)if(event.write)initialized[index]=true else expectCondition(initialized[index],"Old table contents cannot seed an entry")
 }
 expectCondition(initialized.all{it},"Store every table byte")
 for(index in 0 until 272)expectWord("crc_table",if(index-offset/4 in crcWords.indices)crcWords[index-offset/4].toInt() else 0x6a6b6c6d,offset=4*index)
 val after=readBytes("data",size);for(index in before.indices)if(base+index !in table until table+1024)expectCondition(before[index]==after[index],"Other caller bytes unchanged")
 runToExit();expectOutput("")
}
fun AssemblyCase.checkFile(input:List<Int>,capacity:Int,method:Int,maxRead:Int=Int.MAX_VALUE,failAt:Int?=null,missing:Boolean=false,caller:Boolean=false){
 val files=provideFiles(if(missing)emptyMap() else mapOf("fixture.bin" to input.map{it.toByte()}.toByteArray()),maxReadSize=maxRead,failAfterBytes=failAt)
 val status=if(missing)1 else if(failAt!=null && failAt<=input.size)2 else 0;val expected=if(status==0)expectedChecksum(input) else 0L
 val offset=if(caller)0 else (capacity+method)%32
 for(index in 0 until 80)setWord("scratch",0xa5a5a5a5.toInt(),offset=4*index)
 for(index in 0 until 272)setWord("crc_table",0x6a6b6c6d,offset=4*index)
 setWord("exercise",3);setWord("method",method);setWord("buffer_capacity",capacity);setWord("file_crc",-1);setWord("file_status",-1)
 val saved=preserveSetup()
 if(caller)runUntil("file_call") else {setRegisterToDataAddress("a0","path");setRegisterToDataAddress("a1","scratch",offset.toLong());setRegister("a2",capacity);setRegister("a3",method)}
 val base=readDataAddress("data");val size=(readDataAddress("file_status")+4-base).toInt();val before=readBytes("data",size);val scratch=readDataAddress("scratch",offset.toLong());val table=readDataAddress("crc_table");val path=readDataAddress("path");val owned=callerRegions();val accesses=traceMemoryAccesses(base,size,maxAccesses=100000)
 if(caller)runUntil("file_done") else callFunction("crc32_file","function_return")
 accesses.close();expectRegister("v0",expected);expectRegister("v1",status);checkPreserved(saved);files.expectAllClosed()
 val events=files.snapshot();val opens=events.filter{it.operation==Operation.OPEN};val reads=events.filter{it.operation==Operation.READ};val closes=events.filter{it.operation==Operation.CLOSE}
 expectCondition(opens.size==1 && opens[0].path=="fixture.bin" && opens[0].flags==0,"Open the supplied path read-only")
 if(missing)expectCondition(opens[0].result==-1 && reads.isEmpty() && closes.isEmpty(),"Failed open creates no descriptor")
 else {
  val descriptor=opens[0].descriptor
  expectCondition(descriptor>=3 && closes.size==1 && closes[0].descriptor==descriptor && closes[0].result==0,"Close the acquired descriptor once")
  expectCondition(events.first().operation==Operation.OPEN && events.last().operation==Operation.CLOSE && events.drop(1).dropLast(1).all{it.operation==Operation.READ},"Reads occur between open and close")
  expectCondition(reads.isNotEmpty() && reads.last().result==if(status==0)0 else -1,"EOF and error remain distinct")
  expectCondition(reads.all{it.descriptor==descriptor && it.requestedBytes in 1..capacity && it.result<=minOf(it.requestedBytes,maxRead)},"Positive bounded read requests; smaller equivalent requests allowed")
  expectCondition(reads.dropLast(1).all{it.result>0} && reads.sumOf{maxOf(0,it.result)}==if(status==0)input.size else failAt!!,"Consume the complete available prefix")
 }
 for(event in accesses.snapshot()){
  val workspace=within(event.address,event.length,scratch,capacity) || within(event.address,event.length,table,1024);val known=owned.any{(start,n)->event.address<start+n && event.address+event.length>start}
  expectCondition(if(event.write)workspace else workspace || within(event.address,event.length,path,12) || !known,"Only scratch/table workspace and the read-only path may be used")
 }
 val after=readBytes("data",size);for(index in before.indices)if(base+index !in scratch until scratch+capacity && base+index !in table until table+1024)expectCondition(before[index]==after[index],"Other caller bytes unchanged")
 if(caller){expectWord("file_crc",-1);expectWord("file_status",-1);runUntil("results_ready");expectWord("file_crc",expected.toInt());expectWord("file_status",status)}
 runToExit();expectOutput("");files.expectAllClosed();expectCondition(files.snapshot()==events,"The caller performs no further file operations")
}
for(method in 0..1)for((index,input)in listOf(emptyList(),listOf(0,128,255),"123456789".map{it.code}).withIndex())case("caller kernel $method sample $index",instructionLimit=1000000){checkUpdate(input,crcMask,method,caller=true)}
for(method in 0..1)for((index,input)in listOf(emptyList(),listOf(0,128,255),"123456789".map{it.code}).withIndex())case("caller file $method sample $index",instructionLimit=1000000){checkFile(input,7,method,3,caller=true)}
for(method in 0..1)case("caller read error $method",instructionLimit=1000000){checkFile("123456789".map{it.code},7,method,3,4,caller=true)}
