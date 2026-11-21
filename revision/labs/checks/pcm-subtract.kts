import edu.missouristate.mars.cli.grading.AssemblyCase

data class DifferenceTrial(val left:IntArray,val right:IntArray,val mode:Int=0) {
    init {require(left.size==right.size && left.size<=64 && mode in 0..4)}
}
fun AssemblyCase.verify(t:DifferenceTrial,seed:Int) {
    val names=listOf("pcm_a","pcm_b","pcm_c","pcm_d")
    val left=names[seed%4];val right=if(t.mode>=3)left else names[(seed+1)%4]
    val firstOffset=(seed*3)%32;val secondOffset=if(t.mode>=3)firstOffset else (seed*5+1)%32
    val dest=when(t.mode){1,4->left;2->right;else->names[(seed+2)%4]}
    val destOffset=when(t.mode){1,4->firstOffset;2->secondOffset;else->(seed*7+2)%32}
    val a=t.left;val b=if(t.mode>=3)a else t.right;val n=a.size
    val exact=IntArray(n){a[it]-b[it]};val expected=exact.map{it.coerceIn(-32768,32767)}
    fun encoded(samples:IntArray)=samples.flatMap{listOf(it and 255,(it ushr 8)and 255)}
    fun fill(label:String,offset:Int,values:List<Int>) {
        val bytes=IntArray(192){(it*31+seed*17+193)and 255}
        for(i in values.indices)bytes[offset+i]=values[i]
        for(i in bytes.indices step 4){var word=0;for(j in 0..3)word=word or(bytes[i+j]shl(j*8));setWord(label,word,offset=i)}
    }
    fill(left,firstOffset,encoded(a));if(right!=left)fill(right,secondOffset,encoded(b))
    if(t.mode==0 || t.mode==3)fill(dest,destOffset,encoded(expected.toIntArray()).map{it xor 255})
    val root=readDataAddress("pcm_a_guard");val size=(readDataAddress("caller_end")+4-root).toInt()
    val before=readBytes("pcm_a_guard",size);val first=readDataAddress(left)+firstOffset
    val second=readDataAddress(right)+secondOffset;val output=readDataAddress(dest)+destOffset;val length=n*2
    val history=traceMemoryAccesses(root,size,maxAccesses=20000)
    for(i in 0..7)setRegister("s$i",0x1234567800000000L+i*31)
    val sp=if(seed%2==0)0x7fff0000L else 0x7ffe8000L
    setRegister("sp",sp);setRegister("fp",0x2345678900000011L);setRegister("v0",99);setRegister("v1",99)
    setRegisterToDataAddress("a0",left,firstOffset.toLong());setRegisterToDataAddress("a1",right,secondOffset.toLong())
    setRegisterToDataAddress("a2",dest,destOffset.toLong());setRegister("a3",n)
    callFunction("subtract_pcm16",if(seed%2==0)"function_return"else"done")
    history.close();expectRegister("v0",n);expectRegister("v1",exact.count{it !in -32768..32767})
    expectRegister("sp",sp);expectRegister("fp",0x2345678900000011L)
    for(i in 0..7)expectRegister("s$i",0x1234567800000000L+i*31)
    expectCondition(readBytes(dest,length,offset=destOffset).contentEquals(encoded(expected.toIntArray()).toIntArray()),"Every output sample is the exact saturated difference A-B")
    fun fits(address:Long,count:Int,base:Long)=address>=base && count<=length && address-base<=length-count
    for(access in history.snapshot())expectCondition(if(access.write)fits(access.address,access.length,output) else fits(access.address,access.length,first)||fits(access.address,access.length,second)||fits(access.address,access.length,output),"Each caller access fits a selected input/output slice")
    val after=readBytes("pcm_a_guard",size)
    for(i in before.indices)if(root+i !in output until output+length)expectCondition(before[i]==after[i],"Preserve all other caller bytes, including nonaliased sources, metadata, guards and inactive output")
    expectOutput("")
}
val paletteA=intArrayOf(-32768,32767,30000,-30000,0,32767,-32768,1234,30000)
val paletteB=intArrayOf(0,0,-10000,10000,-1,-1,1,5678,-10000)
for(n in 0..64)for(mode in 0..4) {
    val seed=n*5+mode
    case("difference n=$n alias=$mode",instructionLimit=100000){verify(DifferenceTrial(IntArray(n){paletteA[(it+seed)%9]},IntArray(n){paletteB[(it*5+seed)%9]},mode),seed)}
}
for((index,pair)in listOf(-32768 to 32767,32767 to -32768,30000 to -2767,-30000 to 2768,30000 to -2768,-30000 to 2769).withIndex())
case("difference boundary $index",instructionLimit=100000){verify(DifferenceTrial(intArrayOf(pair.first),intArrayOf(pair.second)),index+1)}
case("default nine-sample caller publishes five clips",instructionLimit=100000) {
    val before=readBytes("pcm_a",192);val other=readBytes("pcm_b",192)
    runUntil("subtract_ready");expectRegister("v0",9);expectRegister("v1",5);expectWord("processed",99);expectWord("clipped",99)
    val expected=listOf(-32768,32767,32767,-32768,1,32767,-32768,-4444,32767)
    for((i,value)in expected.withIndex())expectBytes("pcm_c",value and 255,(value ushr 8)and 255,offset=i*2)
    runUntil("results_ready");expectWord("processed",9);expectWord("clipped",5)
    expectCondition(before.contentEquals(readBytes("pcm_a",192))&&other.contentEquals(readBytes("pcm_b",192)),"Disjoint caller inputs stay unchanged")
    runToExit();expectOutput("")
}
