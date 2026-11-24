// Exact execution history; application-side fixture setup does not enter the trace.
val standard =
    listOf(
        0x3f,
        0x06,
        0x5b,
        0x4f,
        0x66,
        0x6d,
        0x7d,
        0x07,
        0x7f,
        0x6f,
        0x77,
        0x7c,
        0x39,
        0x5e,
        0x79,
        0x71,
    )

for ((first, count) in listOf(0 to 16, 14 to 2, 16 to 0)) {
  val font = standard
  case("window $first/$count", instructionLimit = 1000) {
    val io = traceMemoryStores(0xffff0000L, 0x10000)
    val protected =
        listOf(
            traceMemoryStores("glyphs", 16),
            traceMemoryStores("glyph_guard", 4),
            traceMemoryStores("first_digit", 4),
            traceMemoryStores("digit_count", 4),
            traceMemoryStores("inputs_guard", 4),
        )
    setWord("first_digit", first)
    setWord("digit_count", count)
    for (word in 0 until 4) {
      var value = 0
      for (byte in 0 until 4) value = value or (font[word * 4 + byte] shl (byte * 8))
      setWord("glyphs", value, offset = word * 4)
    }
    setWord("glyph_guard", 0x13579bdf xor first)
    setWord("inputs_guard", 0x2468ace0 xor count)
    runToExit()
    io.expectByteStores(0xffff0010L, *font.subList(first, first + count).toIntArray())
    protected.forEach { it.expectNone() }
    expectBytes("glyphs", *font.toIntArray())
    expectWord("first_digit", first)
    expectWord("digit_count", count)
    expectWord("glyph_guard", 0x13579bdf xor first)
    expectWord("inputs_guard", 0x2468ace0 xor count)
    expectOutput("")
  }
}
