import edu.missouristate.mars.cli.grading.AssemblyCase
import java.util.Random
import kotlin.random.Random as KotlinRandom
import javax.crypto.Cipher
import javax.crypto.spec.ChaCha20ParameterSpec
import javax.crypto.spec.SecretKeySpec

val wordMask=0xffffffffL
fun rotateWord(x:Long,n:Int)=((x shl n) or (x ushr(32-n))) and wordMask
fun quarterModel(input:LongArray):LongArray {
 var a=input[0];var b=input[1];var c=input[2];var d=input[3]
 a=(a+b) and wordMask;d=rotateWord(d xor a,16)
 c=(c+d) and wordMask;b=rotateWord(b xor c,12)
 a=(a+b) and wordMask;d=rotateWord(d xor a,8)
 c=(c+d) and wordMask;b=rotateWord(b xor c,7)
 return longArrayOf(a,b,c,d)
}
fun blockModel(input:LongArray):LongArray {
 val work=input.copyOf()
 val selections=arrayOf(intArrayOf(0,4,8,12),intArrayOf(1,5,9,13),intArrayOf(2,6,10,14),intArrayOf(3,7,11,15),intArrayOf(0,5,10,15),intArrayOf(1,6,11,12),intArrayOf(2,7,8,13),intArrayOf(3,4,9,14))
 repeat(10){for(indices in selections){val next=quarterModel(LongArray(4){work[indices[it]]});for(i in 0..3)work[indices[i]]=next[i]}}
 return LongArray(16){(input[it]+work[it]) and wordMask}
}
fun wordBytes(words:LongArray)=ByteArray(words.size*4){i->(words[i/4] ushr (8*(i%4))).toByte()}
fun decodeWords(bytes:ByteArray)=LongArray(bytes.size/4){i->(0..3).fold(0L){v,j->v or ((bytes[4*i+j].toLong() and 255) shl(8*j))}}
fun initialState(key:ByteArray,nonce:ByteArray,counter:Long)=longArrayOf(0x61707865,0x3320646e,0x79622d32,0x6b206574)+decodeWords(key)+longArrayOf(counter)+decodeWords(nonce)
fun jdkCipher(state:LongArray,input:ByteArray):ByteArray {
 val cipher=Cipher.getInstance("ChaCha20");cipher.init(Cipher.ENCRYPT_MODE,SecretKeySpec(wordBytes(state.sliceArray(4..11)),"ChaCha20"),ChaCha20ParameterSpec(wordBytes(state.sliceArray(13..15)),state[12].toInt()));return cipher.doFinal(input)
}
fun byteInts(bytes:ByteArray)=bytes.map{it.toInt() and 255}.toIntArray()
fun pattern(n:Int,salt:Int=129)=ByteArray(n){(it*73+salt).toByte()}
fun AssemblyCase.putBytes(label:String,bytes:ByteArray,offset:Int=0){require(offset%4==0 && bytes.size%4==0);for((i,w)in decodeWords(bytes).withIndex())setWord(label,w.toInt(),offset+4*i)}
fun AssemblyCase.putWords(label:String,words:LongArray,offset:Int=0)=putBytes(label,wordBytes(words),offset)
fun AssemblyCase.initialize():List<Long>{
 for((label,n)in listOf("source" to 4160,"destination" to 4160,"state" to 128,"quad" to 96))putBytes(label,ByteArray(n){165.toByte()})
 setWord("mode",0);setWord("in_place",0);setWord("length",0);setWord("length",0,4);setWord("status",-1)
 val saved=List(9){0x3141592600000000L+it*977}
 for(i in 0..7)setRegister("s$i",saved[i]);setRegister("fp",saved[8]);setRegister("sp",0x7fff0000L)
 for(i in 0..9)setRegister("t$i",-999L-i)
 return saved
}
fun AssemblyCase.assertPreserved(saved:List<Long>){for(i in 0..7)expectRegister("s$i",saved[i]);expectRegister("fp",saved[8]);expectRegister("sp",0x7fff0000L)}
fun inside(a:Long,n:Int,b:Long,size:Int)=a>=b && n<=size && a-b<=size.toLong()-n
fun AssemblyCase.callerObjects()=listOf("source" to 4160,"destination" to 4160,"state" to 128,"quad" to 96,"mode" to 4,"in_place" to 4,"length" to 8,"status" to 4).map{(name,n)->Triple(name,readDataAddress(name),n)}
fun AssemblyCase.observeContract(reads:List<Pair<Long,Int>>,writes:List<Pair<Long,Int>>,action:()->Unit){
 val objects=callerObjects();val previous=objects.map{(name,_,n)->readBytes(name,n)};val root=readDataAddress("source");val size=(readDataAddress("status")+4-root).toInt();val trace=traceMemoryAccesses(root,size,maxAccesses=250000)
 action();trace.close()
 // Candidate-owned constants and workspace can lie between the supplied caller objects.
 // Enforce accesses to the supplied objects, without assigning their gaps to the caller.
 for(e in trace.snapshot())if(objects.any{(_,b,n)->e.address<b+n && b<e.address+e.length})expectCondition((if(e.write)writes else reads).any{(b,n)->inside(e.address,e.length,b,n)},"Access to a caller object belongs to the selected readable or writable bytes")
 for((i,obj)in objects.withIndex()){
  val(name,base,n)=obj;val after=readBytes(name,n)
  for(j in after.indices)if(writes.none{(b,s)->inside(base+j,1,b,s)})expectCondition(after[j]==previous[i][j],"Caller bytes outside writable intervals remain unchanged")
 }
}
fun AssemblyCase.invoke(entry:String,saved:List<Long>,status:Int=0){callFunction(entry,"function_return");expectRegister("v0",status);assertPreserved(saved)}
fun AssemblyCase.runQuarter(input:LongArray,positions:List<Int>,saved:List<Long>,caller:Boolean=false){
 require(positions.size==4 && positions.distinct().size==4 && positions.all{it%4==0 && it in 0..92})
 for(i in 0..3){setWord("quad",input[i].toInt(),positions[i]);setRegisterToDataAddress("a$i","quad",positions[i].toLong())}
 if(caller){setWord("mode",0);startAt("main");runUntil("quarter_call")}
 val intervals=positions.map{readDataAddress("quad",it.toLong()) to 4}
 observeContract(intervals,intervals){if(caller){runUntil("main_done");expectRegister("v0",0);assertPreserved(saved)}else invoke("chacha_quarter",saved)}
 for((i,w)in quarterModel(input).withIndex())expectWord("quad",w.toInt(),positions[i])
}
fun AssemblyCase.checkQuarter(input:LongArray,positions:List<Int>,caller:Boolean=false)=runQuarter(input,positions,initialize(),caller)
fun AssemblyCase.runBlock(input:LongArray,entry:String,stateOffset:Int,outOffset:Int,saved:List<Long>,caller:Boolean=false){
 putWords("state",input,stateOffset);setRegisterToDataAddress("a0","state",stateOffset.toLong());setRegisterToDataAddress("a1","destination",outOffset.toLong())
 if(caller){setWord("mode",if(entry=="chacha_block_scalar")1 else 2);startAt("main");runUntil(if(entry=="chacha_block_scalar")"scalar_call" else "msa_call")}
 val src=readDataAddress("state",stateOffset.toLong());val dst=readDataAddress("destination",outOffset.toLong())
 observeContract(listOf(src to 64,dst to 64),listOf(dst to 64)){if(caller){runUntil("main_done");expectRegister("v0",0);assertPreserved(saved)}else invoke(entry,saved)}
 expectCondition(readBytes("destination",64,outOffset).contentEquals(byteInts(wordBytes(blockModel(input)))),"All output bytes equal the independent raw-block calculation")
}
fun AssemblyCase.checkBlock(input:LongArray,entry:String,stateOffset:Int,outOffset:Int,caller:Boolean=false)=runBlock(input,entry,stateOffset,outOffset,initialize(),caller)
fun AssemblyCase.runXor(state:LongArray,input:ByteArray,requested:Long,inPlace:Boolean,stateOffset:Int,sourceOffset:Int,outOffset:Int,saved:List<Long>,caller:Boolean=false){
 putWords("state",state,stateOffset)
 val source=ByteArray(4160){165.toByte()};input.copyInto(source,sourceOffset);putBytes("source",source)
 val dstLabel=if(inPlace)"source" else "destination";val dstOffset=if(inPlace)sourceOffset else outOffset
 setRegisterToDataAddress("a0","state",stateOffset.toLong());setRegisterToDataAddress("a1","source",sourceOffset.toLong());setRegisterToDataAddress("a2",dstLabel,dstOffset.toLong());setRegister("a3",requested)
 val status=if(requested<0 || requested>4096)1 else if(requested>0 && state[12]+(requested+63)/64-1>wordMask)2 else 0
 if(caller){setWord("mode",3);setWord("in_place",if(inPlace)1 else 0);setWord("length",requested.toInt());setWord("length",(requested ushr 32).toInt(),4);startAt("main");runUntil("stream_call")}
 val st=readDataAddress("state",stateOffset.toLong());val src=readDataAddress("source",sourceOffset.toLong());val dst=readDataAddress(dstLabel,dstOffset.toLong());val n=if(requested in 0..4096)requested.toInt() else 0
 val reads=if(status==1 || requested==0L)emptyList() else listOf(st to 64)+if(status==2)emptyList() else listOf(src to n,dst to n)
 val writes=if(status==0 && n>0)listOf(dst to n) else emptyList()
 observeContract(reads,writes){if(caller){runUntil("main_done");expectRegister("v0",status);assertPreserved(saved)}else invoke("chacha_xor",saved,status)}
 if(status==0 && n>0)expectCondition(readBytes(dstLabel,n,dstOffset).contentEquals(byteInts(jdkCipher(state,input.copyOf(n)))),"Ciphertext equals JDK ChaCha20 for the exact key,nonce,counter and bytes")
}
fun AssemblyCase.checkXor(state:LongArray,n:Int,inPlace:Boolean,stateOffset:Int=12,sourceOffset:Int=3,outOffset:Int=7,caller:Boolean=false)=runXor(state,pattern(n),n.toLong(),inPlace,stateOffset,sourceOffset,outOffset,initialize(),caller)
fun AssemblyCase.checkRejectedCount(count:Long){val saved=initialize();for(i in 0..2)setRegister("a$i",1);setRegister("a3",count);observeContract(emptyList(),emptyList()){invoke("chacha_xor",saved,1)}}
fun AssemblyCase.checkEmptyPointers(){val saved=initialize();for(i in 0..2)setRegister("a$i",1);setRegister("a3",0);observeContract(emptyList(),emptyList()){invoke("chacha_xor",saved)}}
val publicKey=ByteArray(32){it.toByte()}
val publicNonce=byteArrayOf(0,0,0,9,0,0,0,74,0,0,0,0)
val publicState=initialState(publicKey,publicNonce,1)
val blockInputs=listOf(publicState,LongArray(16),LongArray(16){wordMask},LongArray(16){(it.toLong()*0x1234567+0x80000000) and wordMask})
for(entry in listOf("chacha_block_scalar"))for((i,input)in blockInputs.withIndex())case("$entry state $i",instructionLimit=100000){checkBlock(input,entry,4*i,4+12*i)}
