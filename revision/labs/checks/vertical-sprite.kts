// Public formative contract: independent temporaries, labels and frame layouts are allowed.
val fixtures = listOf(
    listOf(0x16,0x3c,0x7e,0xdb,0xff,0x24,0x42,0x81),
    List(8) { 128 ushr it },
    listOf(0,255,0,255,0x01,0x80,0x7f,0xfe),
)
for ((index,rows) in fixtures.withIndex()) {
    case("vertical fixture $index",instructionLimit=10000) {
        for (group in 0..1) {
            var word=0
            for (byte in 0..3) word=word or (rows[group*4+byte] shl (8*byte))
            setWord("sprite",word,offset=group*4)
        }
        for (cell in 0 until 128) setWord("frame",0x5a000000+cell,offset=4*cell)
        val frameGuard=0x13579bdf xor index
        val rowGuard=0x2468ace0 xor index
        setWord("frame_guard",frameGuard)
        setWord("rows_guard",rowGuard)
        val top=readRegister("sp") and -16L
        setRegister("sp",top)
        val kept=(0..7).associate { "s$it" to (0x1122334400000000L+it) } +
            mapOf("fp" to 0x123456789abcdef0L,"gp" to 0x23456789abcdef01L)
        kept.forEach { (reg,value) -> setRegister(reg,value) }
        val caller=readBytes(top,32)
        setRegisterToDataAddress("a0","sprite")
        setRegisterToDataAddress("a1","frame")
        callFunction("draw_vertical",returnTo="after_vertical")
        for (y in 0 until 8) for (x in 0 until 16) {
            val cell=y*16+x
            val expected=if (x<8) 0x5a000000+cell
                else if ((rows[7-y] and (128 ushr (x-8)))!=0) 0xffffff else 0
            expectWord("frame",expected,offset=4*cell)
        }
        expectBytes("sprite",*rows.toIntArray())
        expectWord("frame_guard",frameGuard)
        expectWord("rows_guard",rowGuard)
        expectRegister("sp",top)
        kept.forEach { (reg,value) -> expectRegister(reg,value) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller-owned stack bytes changed")
        expectOutput("")
    }
}
