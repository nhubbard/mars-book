val examples=listOf(
    emptyList(),listOf(0),listOf(Int.MIN_VALUE),listOf(0,1,Int.MAX_VALUE),
    listOf(-1,-4,Int.MIN_VALUE),listOf(2,-2,3,-4,0,1,-3,-1),
    List(8) { -2 },List(8) { 0 },listOf(3,1,2,0),listOf(-1,-4,-3,-2),
    listOf(Int.MAX_VALUE,Int.MIN_VALUE,-1,0,1,Int.MIN_VALUE,Int.MAX_VALUE,-2),
)
val base=listOf(3,-4,0,Int.MIN_VALUE,Int.MAX_VALUE,-1,2,-3)
val trials=examples.map { Triple(it+List(8-it.size) { index -> 0x5a000000+index },0,it.size) } +
    (0..8).flatMap { offset -> (0..8-offset).map { length -> Triple(base,offset,length) } }
for ((number,trial) in trials.withIndex()) {
    val (words,offset,length)=trial
    case("stable negative partition $number offset $offset length $length",instructionLimit=10000) {
        words.forEachIndexed { index,value -> setWord("values",value,offset=4*index) }
        val guards=listOf("values_guard","count_guard","result_guard")
        guards.forEachIndexed { index,name -> setWord(name,0x13579bdf xor (number*31+index)) }
        val protected=guards.map { traceMemoryStores(it,4) } +
            listOf(traceMemoryStores("count",4),traceMemoryStores("negative_count",4))
        val accesses=traceMemoryAccesses("values",36)
        val top=readRegister("sp") and -16L
        setRegister("sp",top)
        val caller=readBytes(top,32)
        val callerStores=traceMemoryStores(top,32)
        val kept=(0..7).associate { "s$it" to (0x1122334400000000L+it) } +
            mapOf("fp" to 0x123456789abcdef0L,"gp" to 0x23456789abcdef01L)
        kept.forEach { (reg,value) -> setRegister(reg,value) }
        setRegisterToDataAddress("a0","values",offset=4L*offset)
        setRegister("a1",length)
        callFunction("partition_negative",returnTo=if (number%2==0) "partition_done" else "partition_exit")
        val input=words.subList(offset,offset+length)
        val expected=words.take(offset)+input.filter { it<0 }+input.filter { it>=0 }+words.drop(offset+length)
        expected.forEachIndexed { index,value -> expectWord("values",value,offset=4*index) }
        expectRegister("v0",input.count { it<0 })
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller-owned stack bytes changed")
        callerStores.expectNone()
        val begin=readDataAddress("values")+4*offset
        accesses.expectReadsWithin(begin,4*length)
        expectCondition(accesses.snapshot().filter { it.write }.all { it.address>=begin && it.address+it.length<=begin+4*length },
            "Every array store stays within the active prefix")
        guards.forEachIndexed { index,name -> expectWord(name,0x13579bdf xor (number*31+index)) }
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}
