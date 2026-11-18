val positions=listOf(-2 to 1,0 to 0,6 to -2,-1 to 4,8 to 0,0 to 6,-5 to 0,0 to -4,Int.MIN_VALUE to Int.MAX_VALUE,Int.MAX_VALUE to Int.MIN_VALUE)
for((index,pos) in positions.withIndex())case("program $index ${pos.first}/${pos.second}",instructionLimit=50000) {
    setWord("position",pos.first);setWord("position",pos.second,offset=4)
    runUntil("blit_call")
    val frame=MutableList(128){if(it/16<6 && it%16<8)0x00102030 else 0x00444444}
    for(i in 0 until 128)expectWord("frame_a",frame[i],offset=i*4)
    val source=List(128){readWord("sprite_a",offset=it*4)}
    val before=readBytes("bitmap_origin",(readDataAddress("state_end")+4-readDataAddress("bitmap_origin")).toInt())
    val writable=mutableSetOf<Long>();var copied=0;var visible=0
    for(y in 0 until 4)for(x in 0 until 5) {
        val dx=pos.first.toLong()+x;val dy=pos.second.toLong()+y
        if(dx in 0L until 8L && dy in 0L until 6L) {
            visible++;val color=source[y*7+x]
            if(color!=0x00ff00ff){copied++;val i=dy.toInt()*16+dx.toInt();frame[i]=color;for(b in 0..3)writable+=readDataAddress("frame_a")+i*4+b}
        }
    }
    val stores=traceMemoryAccesses("bitmap_origin",before.size,maxAccesses=10000)
    runUntil("blit_ready");expectRegister("v0",copied);expectRegister("v1",visible)
    for(i in 0 until 128){expectWord("frame_a",frame[i],offset=i*4);expectWord("sprite_a",source[i],offset=i*4)}
    for(a in stores.snapshot().filter{it.write})expectCondition((0 until a.length).all{a.address+it in writable},"Blit stores belong to visible opaque destination cells")
    val origin=readDataAddress("bitmap_origin");val after=readBytes("bitmap_origin",before.size)
    for(i in before.indices)if(origin+i !in writable)expectCondition(before[i]==after[i],"Caller bytes outside copied cells remain unchanged")
    stores.close();runToExit();expectWord("copied",copied);expectWord("visible",visible);expectOutput("")
}
