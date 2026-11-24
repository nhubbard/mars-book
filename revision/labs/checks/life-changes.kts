// Scatter old live-cell contributions; independent of the assembly's eight halo loads.
fun evolve(old: List<Int>): List<Int> {
  val counts = IntArray(64)
  for (y in 0 until 8) for (x in 0 until 8) if (old[8 * y + x] == 1) {
    for (dy in -1..1) for (dx in -1..1) {
      val nx = x + dx
      val ny = y + dy
      if ((dx != 0 || dy != 0) && nx in 0..7 && ny in 0..7) counts[8 * ny + nx]++
    }
  }
  return List(64) { cell -> if (counts[cell] == 3 || old[cell] == 1 && counts[cell] == 2) 1 else 0 }
}

fun pattern(vararg live: Pair<Int, Int>): List<Int> =
    List(64) { if (it % 8 to it / 8 in live) 1 else 0 }

fun padded(cells: List<Int>): IntArray =
    IntArray(100) { index ->
      val x = index % 10 - 1
      val y = index / 10 - 1
      if (x in 0..7 && y in 0..7) cells[8 * y + x] else 0
    }

val fixtures =
    listOf(
        List(64) { 0 },
        List(64) { 1 },
        pattern(2 to 3, 3 to 3, 4 to 3),
        pattern(3 to 3, 4 to 3, 3 to 4, 4 to 4),
        pattern(0 to 0, 1 to 0, 0 to 1),
        pattern(7 to 7, 6 to 7, 7 to 6, 0 to 0),
        List(64) { if ((it * 37 + 13) % 17 < 8) 1 else 0 },
    )

for ((number, old) in fixtures.withIndex()) {
  case("changed cells fixture $number", instructionLimit = 50000) {
    val source = padded(old)
    val destination = padded(List(64) { 0x7f })
    for ((name, bytes) in listOf("board_a" to source, "board_b" to destination)) {
      for (word in 0 until 25) {
        var value = 0
        for (byte in 0..3) value = value or (bytes[4 * word + byte] shl (8 * byte))
        setWord(name, value, offset = 4 * word)
      }
    }
    val guards =
        listOf(
            "frame_guard",
            "seed_guard",
            "board_a_guard",
            "board_b_guard",
            "generations_guard",
            "generation_guard",
        )
    guards.forEachIndexed { index, name -> setWord(name, 0x12345678 xor (31 * number + index)) }
    val protected = mutableListOf(traceMemoryStores("board_a", 100))
    protected += guards.map { traceMemoryStores(it, 4) }
    protected += traceMemoryStores("board_b", 10)
    protected += traceMemoryStores("board_b", 10, offset = 90)
    for (row in 1..8) {
      protected += traceMemoryStores("board_b", 1, offset = 10 * row)
      protected += traceMemoryStores("board_b", 1, offset = 10 * row + 9)
    }
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    val caller = readBytes(top, 32)
    val kept =
        (0..7).associate { "s$it" to (0x1122334400000000L + it) } +
            mapOf("fp" to 0x123456789abcdef0L, "gp" to 0x23456789abcdef01L)
    kept.forEach { (reg, value) -> setRegister(reg, value) }
    setRegisterToDataAddress("a0", "board_a")
    setRegisterToDataAddress("a1", "board_b")
    callFunction("life_changes", returnTo = "generation_built")
    val next = evolve(old)
    expectBytes("board_a", *source)
    expectBytes("board_b", *padded(next))
    expectRegister("v0", old.zip(next).count { (a, b) -> a != b })
    guards.forEachIndexed { index, name -> expectWord(name, 0x12345678 xor (31 * number + index)) }
    protected.forEach { it.expectNone() }
    kept.forEach { (reg, value) -> expectRegister(reg, value) }
    expectRegister("sp", top)
    expectCondition(readBytes(top, 32).contentEquals(caller), "Caller-owned stack bytes changed")
    expectOutput("")
  }
}
