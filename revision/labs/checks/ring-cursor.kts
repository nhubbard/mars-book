for (capacity in 1..32) for (index in 0 until capacity) {
  case("cursor $index capacity $capacity", instructionLimit = 100) {
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    val kept =
        (0..7).associate { "s$it" to (0x1122334400000000L + it) } +
            mapOf("fp" to 0x123456789abcdef0L, "gp" to 0x23456789abcdef01L)
    kept.forEach { (reg, value) -> setRegister(reg, value) }
    val caller = readBytes(top, 32)
    val output = traceMemoryStores("cursor_result", 4)
    setRegister("a0", index)
    setRegister("a1", capacity)
    callFunction("advance_cursor", returnTo = "cursor_done")
    expectRegister("v0", (index + 1) % capacity)
    expectRegister("sp", top)
    kept.forEach { (reg, value) -> expectRegister(reg, value) }
    expectCondition(readBytes(top, 32).contentEquals(caller), "Caller-owned stack bytes changed")
    expectWord("cursor_result", -1)
    output.expectNone()
    expectOutput("")
  }
}

case("complete cursor caller", instructionLimit = 100) {
  runToExit()
  expectWord("cursor_result", 0)
  expectOutput("")
}
