import edu.missouristate.mars.devices.DataCacheSimulator.Options
import edu.missouristate.mars.devices.DataCacheSimulator.Placement

val shapes=listOf(0 to 5,5 to 0,0 to 0,1 to 1,1 to 7,7 to 1,3 to 5,5 to 3,5 to 9,16 to 16)
for((number,shape) in shapes.withIndex()) case("clockwise ${shape.first} by ${shape.second}",instructionLimit=250000) {
    val(rows,columns)=shape
    val words=rows*columns
    val inShift=if(words<250) 1 else 0
    val outShift=if(words<250) 3 else 0
    val source=List(256) { index -> when(index%5) {
        0 -> Int.MIN_VALUE
        1 -> Int.MAX_VALUE
        else -> (index*0x01020409) xor 0x5aa555aa
    } }
    val untouched=0x6d3a95c7
    for(index in 0..255) {
        setWord("input",source[index],offset=4*index)
        setWord("output",untouched,offset=4*index)
    }
    setWord("copied",-1)
    val protected=listOf("input_guard","output_guard","metadata_guard").map { traceMemoryStores(it,4) }+listOf(traceMemoryStores("input",1024))
    val metadata=traceMemoryStores("copied",4)
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val caller=readBytes(top,32)
    val callerWrites=traceMemoryStores(top,32)
    val saved=(0..7).associate { "s$it" to (0x1234567800000000L+it) }+
        mapOf("fp" to 0x23456789abcdef01L,"gp" to 0x3456789abcdef012L)
    saved.forEach { (name,value) -> setRegister(name,value) }
    setRegisterToDataAddress("a0","input",offset=4L*inShift)
    setRegisterToDataAddress("a1","output",offset=4L*outShift)
    setRegister("a2",rows)
    setRegister("a3",columns)
    val trace=connectDataCache(Options(8,4,Placement.FULL,8),logAccesses=true,logLimit=1000000)
    callFunction("rotate_clockwise",returnTo=if(number%2==0) "rotate_done" else "rotate_exit")
    trace.close()
    expectRegister("v0",words)
    expectRegister("sp",top)
    saved.forEach { (name,value) -> expectRegister(name,value) }
    expectCondition(readBytes(top,32).contentEquals(caller),"Caller-owned stack bytes remain intact")
    callerWrites.expectNone()
    metadata.expectNone()
    metadata.close()
    val firstIn=readDataAddress("input")
    val firstOut=readDataAddress("output")
    val visits=trace.recordedAccesses().mapNotNull { access ->
        fun touches(base:Long)=access.address<base+1024 && access.address+access.length>base
        val isInput=touches(firstIn)
        if(!isInput && !touches(firstOut)) null else {
            expectCondition(access.length==4 && access.address%4==0L,"Matrix traffic consists of aligned word notices")
            val relative=access.address-(if(isInput) firstIn+4*inShift else firstOut+4*outShift)
            expectCondition(relative>=0 && relative<4L*words,"Matrix access stays inside supplied span")
            expectCondition(access.write==!isInput,"Source is read-only and destination is write-only")
            val cell=(relative/4).toInt()
            val original=if(isInput) cell else (rows-1-cell%rows)*columns+cell/rows
            access.write to original
        }
    }
    val cells=(0 until words).toList()
    expectCondition(visits.filter { !it.first }.map { it.second }.sorted()==cells && visits.filter { it.first }.map { it.second }.sorted()==cells,"Copy each source cell exactly once")
    for(cell in cells) expectCondition(visits.indexOf(false to cell)<visits.indexOf(true to cell),"Read each cell before writing its rotated destination")
    fun verify() {
        for(index in 0..255) {
            expectWord("input",source[index],offset=4*index)
            val out=index-outShift
            val expected=if(out in 0 until words) source[inShift+(rows-1-out%rows)*columns+out/rows] else untouched
            expectWord("output",expected,offset=4*index)
        }
        protected.forEach { it.expectNone() }
    }
    verify()
    runToExit()
    verify()
    expectWord("copied",if(number%2==0) words else -1)
    expectOutput("")
}
