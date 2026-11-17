for ((length, clock) in listOf(16 to 64, 1 to 64, 63 to 17)) {
    case("side $length clock $clock", instructionLimit = 100000) {
        val bot = connectMarsBot(clock)
        val inputWrites = listOf(traceMemoryStores("side", 4), traceMemoryStores("side_guard", 4))
        val xWrites = traceMemoryStores(0xffff8030L, 4)
        val yWrites = traceMemoryStores(0xffff8040L, 4)
        val guard = 0x13579bdf xor (length * 97 + clock)
        setWord("side", length)
        setWord("side_guard", guard)
        runToExit()
        val l = length.toDouble()
        bot.expectState(0.0, 0.0, 0, moving = false, penDown = false)
        bot.expectTrail(0.0 to 0.0, l to 0.0, l to l, 0.0 to l, 0.0 to 0.0)
        inputWrites.forEach { it.expectNone() }
        xWrites.expectNone()
        yWrites.expectNone()
        expectWord("side", length)
        expectWord("side_guard", guard)
        expectOutput("")
    }
}
