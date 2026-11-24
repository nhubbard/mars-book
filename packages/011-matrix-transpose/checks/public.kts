import edu.missouristate.mars.devices.DataCacheSimulator.Options
import edu.missouristate.mars.devices.DataCacheSimulator.Placement

// Public behavioral checks: no reference register allocation, private labels, or exact miss counts.
val shapes =
    listOf(0 to 5, 5 to 0, 1 to 1, 1 to 7, 7 to 1, 3 to 5, 5 to 3, 4 to 4, 5 to 9, 16 to 16)

for ((rows, columns) in shapes) for (tiled in listOf(false, true)) {
  case("${rows}x$columns ${if (tiled) "tiled" else "ordinary"}", instructionLimit = 250000) {
    val words = rows * columns
    val inShift = if (words < 250) 1 else 0
    val outShift = if (words < 250) 3 else 0
    val sourceWords =
        IntArray(256) { index ->
          when (index % 5) {
            0 -> Int.MIN_VALUE
            1 -> Int.MAX_VALUE
            else -> (index * 0x01020409) xor 0x5aa555aa
          }
        }
    val untouched = 0x6d3a95c7
    for (index in 0 until 256) {
      setWord("input", sourceWords[index], offset = index * 4)
      setWord("output", untouched, offset = index * 4)
    }
    val protected = mutableListOf(traceMemoryStores("input", 1024))
    if (outShift != 0) protected += traceMemoryStores("output", outShift * 4)
    if (outShift + words < 256)
        protected +=
            traceMemoryStores(
                "output",
                (256 - outShift - words) * 4,
                offset = (outShift + words) * 4,
            )
    for (guard in listOf("input_guard", "output_guard", "metadata_guard")) protected +=
        traceMemoryStores(guard, 4)
    val saved = (0..7).map { 0x3456789a00000000L + it * 1009L }
    for (index in 0..7) setRegister("s$index", saved[index])
    val stack = 0x7fff0000L
    setRegister("sp", stack)
    setRegisterToDataAddress("a0", "input", inShift * 4L)
    setRegisterToDataAddress("a1", "output", outShift * 4L)
    setRegister("a2", rows)
    setRegister("a3", columns)
    setRegisterToTextAddress("ra", "transpose_done")
    startAt(if (tiled) "transpose_tiled" else "transpose_naive")
    val data =
        connectDataCache(Options(8, 4, Placement.FULL, 8), logAccesses = true, logLimit = 1_000_000)
    runUntil("transpose_done")
    data.close()
    expectRegister("v0", words)
    expectRegister("sp", stack)
    for (index in 0..7) expectRegister("s$index", saved[index])

    // Decode only notices overlapping the two full allocations, including inactive words.
    // Auxiliary/stack traffic outside them remains permitted and still affects cache counts.
    val allocationIn = readDataAddress("input")
    val allocationOut = readDataAddress("output")
    val visits =
        data.recordedAccesses().mapNotNull { access ->
          fun touches(base: Long) =
              access.address < base + 1024 && access.address + access.length > base
          val input = touches(allocationIn)
          if (!input && !touches(allocationOut)) null
          else {
            expectCondition(
                access.length == 4 && access.address % 4L == 0L,
                "Matrix traffic must use aligned word notices",
            )
            val relative =
                access.address -
                    (if (input) allocationIn + inShift * 4 else allocationOut + outShift * 4)
            expectCondition(
                relative >= 0 && relative < words * 4L,
                "Matrix access outside active window",
            )
            expectCondition(
                access.write == !input,
                "Input is read-only and output is write-only during transpose",
            )
            val position = (relative / 4).toInt()
            val sourceCell = if (input) position else (position % rows) * columns + position / rows
            access.write to sourceCell
          }
        }
    val expectedCells = (0 until words).toList()
    val loads = visits.filter { !it.first }.map { it.second }
    val stores = visits.filter { it.first }.map { it.second }
    expectCondition(
        loads.sorted() == expectedCells && stores.sorted() == expectedCells,
        "Read and write each active matrix cell exactly once",
    )
    for (cell in expectedCells) expectCondition(
        visits.indexOf(false to cell) < visits.indexOf(true to cell),
        "Read each cell before its corresponding write",
    )
    if (!tiled) {
      expectCondition(
          loads == expectedCells && stores == expectedCells,
          "Ordinary traversal must follow source row-major order for both reads and writes",
      )
    } else {
      // Consecutive events for a tile form one group. Tile groups can occur in any order.
      val tileGroups = mutableListOf<MutableList<Pair<Boolean, Int>>>()
      fun tile(cell: Int) = (cell / columns / 4) to (cell % columns / 4)
      for (visit in visits) {
        if (tileGroups.isEmpty() || tile(tileGroups.last().last().second) != tile(visit.second))
            tileGroups.add(mutableListOf())
        tileGroups.last().add(visit)
      }
      val seen = mutableSetOf<Pair<Int, Int>>()
      for (group in tileGroups) {
        val coordinate = tile(group.first().second)
        expectCondition(seen.add(coordinate), "Finish a tile without leaving and revisiting it")
        val cells = expectedCells.filter { tile(it) == coordinate }
        expectCondition(
            group.filter { !it.first }.map { it.second }.sorted() == cells &&
                group.filter { it.first }.map { it.second }.sorted() == cells,
            "Finish every read and write of the clipped tile before entering the next tile",
        )
      }
    }
    fun outputAndProtection() {
      for (index in 0 until 256) {
        expectWord("input", sourceWords[index], offset = index * 4)
        val at = index - outShift
        val expected =
            if (at in 0 until words) sourceWords[inShift + (at % rows) * columns + at / rows]
            else untouched
        expectWord("output", expected, offset = index * 4)
      }
      protected.forEach { it.expectNone() }
    }
    outputAndProtection()
    runToExit()
    outputAndProtection()
    expectWord("copied", words)
    expectOutput("")
  }
}
