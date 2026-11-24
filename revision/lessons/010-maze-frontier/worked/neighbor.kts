case("all cell and direction combinations preserve coordinate geometry", instructionLimit = 30000) {
  val saved =
      (0..7).associate { "s$it" to (0x1122334400000000L + it) } +
          mapOf("fp" to 0x123456789abcdef0L, "gp" to 0x23456789abcdef01L)
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  saved.forEach { (name, value) -> setRegister(name, value) }
  val caller = readBytes(top, 32)
  val protected =
      listOf(
          traceMemoryStores("dx", 4),
          traceMemoryStores("dy", 4),
          traceMemoryStores("answer", 4),
          traceMemoryStores(top, 32),
      )
  for (cell in 0..63) for (direction in 0..3) {
    val (dr, dc) = listOf(-1 to 0, 0 to 1, 1 to 0, 0 to -1)[direction]
    val row = cell / 8 + dr
    val col = cell % 8 + dc
    val expected = if (row in 0..7 && col in 0..7) 8 * row + col else -1
    setRegister("a0", cell)
    setRegister("a1", direction)
    callFunction(
        "neighbor_id",
        returnTo = if (direction % 2 == 0) "neighbor_done" else "neighbor_exit",
    )
    expectRegister("v0", expected)
    expectRegister("sp", top)
    saved.forEach { (name, value) -> expectRegister(name, value) }
    expectCondition(readBytes(top, 32).contentEquals(caller), "Caller stack remains intact")
  }
  protected.forEach { it.expectNone() }
  expectOutput("")
}

case("complete caller rejects east of cell seven", instructionLimit = 1000) {
  runToExit()
  expectWord("answer", -1)
  expectOutput("")
}
