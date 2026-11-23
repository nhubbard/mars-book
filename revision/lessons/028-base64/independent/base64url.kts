import edu.missouristate.mars.cli.grading.AssemblyCase
import java.util.Base64

val encoder=Base64.getUrlEncoder().withoutPadding()
fun AssemblyCase.verifyUrl(input:ByteArray,capacity:Long,seed:Int,count:Long=input.size.toLong(),badSource:Boolean=false,badDestination:Boolean=false) {
 val wanted=encoder.encode(input)
 val status=if(count.toULong()>64uL)1 else if(count==0L)0 else if(capacity.toULong()<wanted.size.toULong())3 else 0
 val result=if(status==0&&count!=0L)wanted else byteArrayOf()
 val so=seed%32;val outOffset=(seed*7+3)%32
 fun fill(label:String,offset:Int,values:ByteArray){val data=IntArray(192){(it*31+seed*17+193)and 255};for(i in values.indices)data[offset+i]=values[i].toInt()and 255;for(i in 0 until 192 step 4){var word=0;for(j in 0..3)word=word or(data[i+j]shl(8*j));setWord(label,word,i)}}
 fill("source",so,input);fill("destination",outOffset,byteArrayOf())
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size)
 val src=readDataAddress("source")+so;val dest=readDataAddress("destination")+outOffset
 val saved=List(9){0x5566778800000000L+it*977};for(i in 0..7)setRegister("s$i",saved[i]);setRegister("fp",saved[8]);val stack=readDataAddress("workspace",256);setRegister("sp",stack)
 setRegister("a0",if(badSource)1 else src);setRegister("a1",count);setRegister("a2",if(badDestination)1 else dest);setRegister("a3",capacity)
 val accesses=traceMemoryAccesses(root,size,maxAccesses=20000)
 callFunction("b64url_encode",if(seed%2==0)"function_return"else"done");accesses.close()
 expectRegister("v0",status);expectRegister("v1",result.size);expectRegister("sp",stack);expectRegister("fp",saved[8]);for(i in 0..7)expectRegister("s$i",saved[i])
 expectCondition(readBytes("destination",result.size,outOffset).contentEquals(result.map{it.toInt()and 255}.toIntArray()),"Exact URL-safe unpadded encoding")
 val after=readBytes("source_guard",size)
 fun fits(address:Long,length:Int,base:Long,bytes:Int)=address>=base&&length<=bytes&&address-base<=bytes-length
 for(a in accesses.snapshot())expectCondition(if(a.write)fits(a.address,a.length,dest,result.size)else fits(a.address,a.length,dest,result.size)||(status==0&&count>0&&fits(a.address,a.length,src,input.size)),"Selected slices only; failure or empty input has no caller access")
 for(i in before.indices)if(root+i !in dest until dest+result.size)expectCondition(before[i]==after[i],"Preserve every caller byte outside exact result, including no terminator or padding writes")
 expectOutput("")
}
for(n in 0..64)for(kind in 0..4)case("URL length=$n capacity-kind=$kind",instructionLimit=1000000){
 val raw=ByteArray(n){((it*73+n*11+kind*17)and 255).toByte()};val needed=encoder.encode(raw).size
 val capacity=when(kind){0->0L;1->maxOf(0,needed-1).toLong();2->needed.toLong();3->192L;else->Long.MIN_VALUE}
 verifyUrl(raw,capacity,n*5+kind)
}
for(b in 0..255)case("URL every one-byte input $b",instructionLimit=1000000){verifyUrl(byteArrayOf(b.toByte()),2,b)}
for((i,text)in listOf("","f","fo","foo","foob","fooba","foobar").withIndex())case("URL familiar text $i",instructionLimit=1000000){verifyUrl(text.toByteArray(),192,i)}
for((i,count)in listOf(65L,-1L,Long.MIN_VALUE).withIndex())case("URL unsigned count $count",instructionLimit=1000000){verifyUrl(byteArrayOf(),0,i,count,true,true)}
case("URL empty without pointer access",instructionLimit=1000000){verifyUrl(byteArrayOf(),0,0,0,true,true)}
case("URL capacity before reading source",instructionLimit=1000000){verifyUrl(byteArrayOf(1,2,3),0,1,3,true,true)}
case("URL caller publishes six characters",instructionLimit=1000000){
 val before=readBytes("source",192);runUntil("url_ready");expectRegister("v0",0);expectRegister("v1",6);expectBytes("destination",45,95,95,95,95,119);expectWord("status",-1);expectWord("written",-1);expectWord("written",-1,4)
 runUntil("results_ready");expectWord("status",0);expectWord("written",6);expectWord("written",0,4)
 expectCondition(before.contentEquals(readBytes("source",192)),"Input unchanged");runToExit();expectOutput("")
}
