import kotlin.math.abs

// Closed-form nearest-cell oracle; independent of the running error recurrence.
fun lineCells(ax: Int, ay: Int, bx: Int, by: Int): List<Pair<Int, Int>> {
  val (x0, y0, x1, y1) =
      if (ax < bx || ax == bx && ay <= by) listOf(ax, ay, bx, by) else listOf(bx, by, ax, ay)
  val dx = x1 - x0
  val dy = abs(y1 - y0)
  val sy = if (y1 >= y0) 1 else -1
  if (dx == 0 && dy == 0) return listOf(x0 to y0)
  return if (dx >= dy) (0..dx).map { k -> x0 + k to y0 + sy * ((2 * k * dy + dx) / (2 * dx)) }
  else (0..dy).map { k -> x0 + (2 * k * dx + dy) / (2 * dy) to y0 + sy * k }
}

val fixtures =
    listOf(
        listOf(1, 1, 6, 4),
        listOf(6, 4, 1, 1),
        listOf(0, 0, 2, 1),
        listOf(2, 1, 0, 0),
        listOf(3, 7, 3, 0),
        listOf(4, 4, 4, 4),
        listOf(1, 6, 3, 0),
        listOf(0, 7, 7, 7),
        listOf(0, 0, 7, 7),
        listOf(7, 0, 0, 7),
        listOf(0, 0, 1, 2),
        listOf(7, 7, 7, 7),
    )

for ((number, line) in fixtures.withIndex()) {
  val (ax, ay, bx, by) = line
  case("dashed $ax,$ay to $bx,$by", instructionLimit = 10000) {
    val rgb = if (number == 0) 0 else ((number + 1) * 0x010307) and 0xffffff
    val old = List(64) { 0x5a000000 or (it * 0x010103) }
    line.forEachIndexed { index, value -> setWord("endpoints", value, offset = index * 4) }
    setWord("color", rgb)
    for (cell in 0 until 64) setWord("frame", old[cell], offset = 4 * cell)
    val guardNames = listOf("frame_guard", "endpoints_guard", "color_guard", "count_guard")
    val guardValues = guardNames.mapIndexed { index, _ -> 0x12345678 xor (number * 31 + index) }
    guardNames.forEachIndexed { index, name -> setWord(name, guardValues[index]) }
    val protected =
        listOf(traceMemoryStores("endpoints", 16), traceMemoryStores("color", 4)) +
            guardNames.map { traceMemoryStores(it, 4) }
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    val caller = readBytes(top, 32)
    val kept =
        (0..7).associate { "s$it" to (0x1122334400000000L + it) } +
            mapOf("fp" to 0x123456789abcdef0L, "gp" to 0x23456789abcdef01L)
    kept.forEach { (reg, value) -> setRegister(reg, value) }
    setRegisterToDataAddress("a0", "endpoints")
    setRegisterToDataAddress("a1", "frame")
    setRegister("a2", rgb)
    callFunction("draw_dashed", returnTo = "line_complete")
    val selected = lineCells(ax, ay, bx, by).filterIndexed { index, _ -> index % 2 == 0 }.toSet()
    for (y in 0 until 8) for (x in 0 until 8) {
      val cell = 8 * y + x
      expectWord("frame", if (x to y in selected) rgb else old[cell], offset = 4 * cell)
    }
    expectRegister("v0", selected.size)
    line.forEachIndexed { index, value -> expectWord("endpoints", value, offset = index * 4) }
    expectWord("color", rgb)
    guardNames.forEachIndexed { index, name -> expectWord(name, guardValues[index]) }
    protected.forEach { it.expectNone() }
    kept.forEach { (reg, value) -> expectRegister(reg, value) }
    expectRegister("sp", top)
    expectCondition(readBytes(top, 32).contentEquals(caller), "Caller-owned stack bytes changed")
    expectOutput("")
  }
}
