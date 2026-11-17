import edu.missouristate.mars.cli.grading.MemoryStoreTrace

// Scatter contributions into an unpadded board; independent of the assembly's halo loads.
fun evolve(old: List<Int>): List<Int> {
    val neighbors = IntArray(64)
    for (y in 0 until 8) for (x in 0 until 8) if (old[y * 8 + x] != 0) {
        for (dy in -1..1) for (dx in -1..1) {
            val nx = x + dx
            val ny = y + dy
            if ((dx != 0 || dy != 0) && nx in 0..7 && ny in 0..7) neighbors[ny * 8 + nx]++
        }
    }
    return List(64) { index -> if (neighbors[index] == 3 || (old[index] == 1 && neighbors[index] == 2)) 1 else 0 }
}
fun pattern(vararg live: Pair<Int, Int>): List<Int> = List(64) { index -> if (index % 8 to index / 8 in live) 1 else 0 }
fun padded(cells: List<Int>): IntArray = IntArray(100) { index ->
    val x = index % 10 - 1
    val y = index / 10 - 1
    if (x in 0..7 && y in 0..7) cells[y * 8 + x] else 0
}

val fixtures = listOf(
    pattern(2 to 3, 3 to 3, 4 to 3) to 2,
    pattern(3 to 3, 4 to 3, 3 to 4, 4 to 4) to 3,
    pattern(0 to 0, 1 to 0, 0 to 1) to 1,
    pattern(7 to 7) to 0,
    List(64) { 1 } to 1,
)

for ((caseIndex, fixture) in fixtures.withIndex()) {
    val (initial, steps) = fixture
    case("fixture $caseIndex generations $steps", instructionLimit = 100000) {
        expectDataAddress("frame", 0x10010000L)
        expectDataAddress("frame_guard", 0x10010100L)
        val protected = mutableListOf(traceMemoryStores("seed", 64), traceMemoryStores("generations", 4))
        val guards = listOf("frame_guard", "seed_guard", "board_a_guard", "board_b_guard", "generations_guard", "generation_guard")
        for ((index, guard) in guards.withIndex()) {
            protected += traceMemoryStores(guard, 4)
            setWord(guard, 0x13579bdf xor (caseIndex * 131 + index))
        }
        for (name in listOf("board_a", "board_b")) {
            protected += traceMemoryStores(name, 10)
            protected += traceMemoryStores(name, 10, offset = 90)
            for (row in 1..8) {
                protected += traceMemoryStores(name, 1, offset = row * 10)
                protected += traceMemoryStores(name, 1, offset = row * 10 + 9)
            }
            val poison = padded(List(64) { 0x7f })
            for (word in 0 until 25) {
                var value = 0
                for (byte in 0 until 4) value = value or (poison[word * 4 + byte] shl (8 * byte))
                setWord(name, value, offset = word * 4)
            }
        }
        for (word in 0 until 16) {
            var value = 0
            for (byte in 0 until 4) value = value or (initial[word * 4 + byte] shl (8 * byte))
            setWord("seed", value, offset = word * 4)
        }
        setWord("generations", steps)
        setWord("generation", -1)
        for (cell in 0 until 64) setWord("frame", 0x12345678, offset = cell * 4)
        var current = initial
        var previous = initial
        var stableSource: MemoryStoreTrace? = null
        for (g in 0..steps) {
            runUntil("generation_ready")
            stableSource?.expectNone()
            stableSource?.close()
            expectWord("generation", g)
            val source = if (g % 2 == 0) "board_a" else "board_b"
            val destination = if (g % 2 == 0) "board_b" else "board_a"
            expectBytes(source, *padded(current))
            if (g > 0) expectBytes(destination, *padded(previous))
            for (cell in 0 until 64) expectWord("frame", if (current[cell] == 1) 0xffffff else 0, offset = cell * 4)
            stableSource = traceMemoryStores(source, 100)
            if (g < steps) {
                previous = current
                current = evolve(current)
                step()
            }
        }
        runToExit()
        stableSource?.expectNone()
        stableSource?.close()
        expectWord("generation", steps)
        expectBytes(if (steps % 2 == 0) "board_a" else "board_b", *padded(current))
        if (steps > 0) expectBytes(if (steps % 2 == 0) "board_b" else "board_a", *padded(previous))
        for (cell in 0 until 64) expectWord("frame", if (current[cell] == 1) 0xffffff else 0, offset = cell * 4)
        expectBytes("seed", *initial.toIntArray())
        expectWord("generations", steps)
        for ((index, guard) in guards.withIndex()) expectWord(guard, 0x13579bdf xor (caseIndex * 131 + index))
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}
