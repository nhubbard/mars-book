val positions=(-10..10).toList()+listOf(Int.MIN_VALUE,Int.MIN_VALUE+1,-1000,1000,Int.MAX_VALUE-7,Int.MAX_VALUE-1,Int.MAX_VALUE)
for(x in positions)for(source in 0..8)for(frame in 0..8)case("clip $x/$source/$frame",instructionLimit=1000) {
    val left=maxOf(0L,x.toLong());val right=minOf(frame.toLong(),x.toLong()+source)
    val expected=if(left<right)left to right else 0L to 0L
    val before=readBytes("position",20)
    val writes=traceMemoryStores("position",20)
    val sp=0x7fff0000L
    setRegister("sp",sp)
    for(i in 0..7)setRegister("s$i",0x1234567800000000L+i*31)
    setRegister("fp",0x2345678900000011L)
    setRegister("a0",x);setRegister("a1",source);setRegister("a2",frame)
    val continuation=if((source+frame)%2==0)"function_return" else "done"
    callFunction("clip_interval",continuation)
    expectRegister("v0",expected.first);expectRegister("v1",expected.second)
    expectRegister("sp",sp);expectRegister("fp",0x2345678900000011L)
    for(i in 0..7)expectRegister("s$i",0x1234567800000000L+i*31)
    expectBytes("position",*before.map{it.toInt() and 255}.toIntArray());writes.expectNone()
    runToExit();expectOutput("")
}
case("default caller publishes the half-open interval",instructionLimit=1000) {
    runUntil("interval_ready");expectRegister("v0",0);expectRegister("v1",3)
    runToExit();expectWord("clipped_left",0);expectWord("clipped_right",3);expectOutput("")
}
