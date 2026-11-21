import edu.missouristate.mars.cli.grading.AssemblyCase

fun pack555(r:Int,g:Int,b:Int)=(r/8)*1024+(g/8)*32+b/8
fun AssemblyCase.verify(count:Int,offset:Int,seed:Int,bucketBase:Int?=null) {
    val input=IntArray(128){(it*37+seed*19) and 255}
    if(bucketBase!=null)for(i in 0 until count) {
        val word=bucketBase+i
        input[offset+i*3]=(word/1024)*8+7
        input[offset+i*3+1]=((word/32)%32)*8+7
        input[offset+i*3+2]=(word%32)*8+7
    }
    val destinationOffset=(offset*7+seed/32+1)%32
    val expected=(0 until count).flatMap { val at=offset+it*3;val word=pack555(input[at],input[at+1],input[at+2]);listOf(word and 255,word ushr 8) }
    val output=IntArray(96){(it*17+seed*11) and 255}
    for(i in expected.indices)output[destinationOffset+i]=expected[i] xor 255
    fun fill(label:String,bytes:IntArray) {
        for(i in bytes.indices step 4) {
            var word=0;for(j in 0..3)word=word or (bytes[i+j] shl (j*8))
            setWord(label,word,offset=i)
        }
    }
    fill("rgb",input);fill("packed",output)
    val first=readDataAddress("rgb",offset.toLong());val last=readDataAddress("packed",destinationOffset.toLong())
    val root=readDataAddress("rgb_guard");val size=(readDataAddress("completed")+4-root).toInt()
    val before=readBytes("rgb_guard",size);val accesses=traceMemoryAccesses(root,size,maxAccesses=20000)
    setRegisterToDataAddress("a0","rgb",offset.toLong());setRegisterToDataAddress("a1","packed",destinationOffset.toLong());setRegister("a2",count)
    for(i in 0..7)setRegister("s$i",0x1234567800000000L+i*31)
    val sp=if(seed%2==0)0x7fff0000L else 0x7ffe8000L
    setRegister("sp",sp);setRegister("fp",0x2345678900000011L)
    callFunction("pack_rgb555",if(seed%2==0)"function_return"else"pack555_done")
    accesses.close();expectRegister("v0",count);expectRegister("sp",sp);expectRegister("fp",0x2345678900000011L)
    for(i in 0..7)expectRegister("s$i",0x1234567800000000L+i*31)
    for(i in expected.indices)expectBytes("packed",expected[i],offset=destinationOffset+i)
    fun fits(address:Long,length:Int,base:Long,bytes:Int)=address>=base && length<=bytes && address-base<=bytes-length
    for(a in accesses.snapshot())expectCondition(if(a.write)fits(a.address,a.length,last,count*2) else fits(a.address,a.length,first,count*3)||fits(a.address,a.length,last,count*2),"Every caller access fits the selected input/output slices")
    val after=readBytes("rgb_guard",size)
    for(i in before.indices)if(root+i !in last until last+count*2)expectCondition(before[i]==after[i],"Preserve input, metadata, guards and inactive output, including transient stores")
    expectOutput("")
}
for(count in 0..32)case("rgb555 count $count",instructionLimit=100000){verify(count,(count*11)%32,count)}
case("default caller publishes eight RGB555 pixels",instructionLimit=100000) {
    val input=readBytes("rgb",24);runUntil("pack555_done");expectRegister("v0",8);expectWord("completed",-1)
    for(i in 0..7) {
        val word=pack555(input[i*3],input[i*3+1],input[i*3+2])
        expectBytes("packed",word and 255,word ushr 8,offset=i*2)
    }
    runToExit();expectWord("completed",8);expectOutput("")
}
