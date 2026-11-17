import kotlin.math.abs

// An independent rational oracle: no running Bresenham error recurrence.
fun pixels(ax: Int, ay: Int, bx: Int, by: Int): Set<Pair<Int, Int>> {
    val ordered = if (ax < bx || (ax == bx && ay <= by)) listOf(ax, ay, bx, by) else listOf(bx, by, ax, ay)
    val (x0, y0, x1, y1) = ordered
    val dx = x1 - x0
    val dy = abs(y1 - y0)
    val sy = if (y1 >= y0) 1 else -1
    if (dx == 0 && dy == 0) return setOf(x0 to y0)
    return if (dx >= dy) (0..dx).map { k -> x0 + k to y0 + sy * ((2 * k * dy + dx) / (2 * dx)) }.toSet()
    else (0..dy).map { k -> x0 + (2 * k * dx + dy) / (2 * dy) to y0 + sy * k }.toSet()
}

for (line in listOf(listOf(1, 1, 6, 4), listOf(6, 4, 1, 1), listOf(0, 0, 2, 1), listOf(2, 1, 0, 0), listOf(3, 7, 3, 0), listOf(4, 4, 4, 4), listOf(1, 6, 3, 0))) {
    val (ax, ay, bx, by) = line
    case("line $ax,$ay to $bx,$by", instructionLimit = 10000) {
        expectDataAddress("frame", 0x10010000L)
        expectDataAddress("frame_guard", 0x10010100L)
        val protected = listOf(traceMemoryStores("endpoints", 16), traceMemoryStores("frame_guard", 4), traceMemoryStores("endpoints_guard", 4), traceMemoryStores("color", 4), traceMemoryStores("color_guard", 4))
        val rgb = ((ax * 239 + ay * 17 + bx * 13 + by * 97) * 0x010103) and 0xffffff
        val old = List(64) { index -> (0x55000000 or (index * 0x010307)) xor (ax shl 8) xor by }
        for ((index, value) in line.withIndex()) setWord("endpoints", value, offset = index * 4)
        setWord("color", rgb)
        setWord("frame_guard", 0x13579bdf xor ax)
        setWord("endpoints_guard", 0x2468ace0 xor ay)
        setWord("color_guard", 0x10203040 xor bx)
        for (cell in 0 until 64) setWord("frame", old[cell], offset = cell * 4)
        runToExit()
        val selected = pixels(ax, ay, bx, by)
        for (y in 0 until 8) for (x in 0 until 8) {
            expectWord("frame", if (x to y in selected) rgb else old[y * 8 + x], offset = (y * 8 + x) * 4)
        }
        for ((index, value) in line.withIndex()) expectWord("endpoints", value, offset = index * 4)
        expectWord("color", rgb)
        expectWord("frame_guard", 0x13579bdf xor ax)
        expectWord("endpoints_guard", 0x2468ace0 xor ay)
        expectWord("color_guard", 0x10203040 xor bx)
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}
