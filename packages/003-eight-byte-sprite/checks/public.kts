fun reverseByte(value: Int): Int = (0..7).fold(0) { result, bit ->
    result or (((value ushr bit) and 1) shl (7 - bit))
}
val fixtures = listOf(
    listOf(0x16, 0x3c, 0x7e, 0xdb, 0xff, 0x24, 0x42, 0x81),
    List(8) { 1 shl it },
)
for ((index, rows) in fixtures.withIndex()) {
    case("fixture $index", instructionLimit = 10000) {
        expectDataAddress("frame", 0x10010000L)
        expectDataAddress("frame_guard", 0x10010200L)
        for (group in 0..1) {
            val word = (0..3).fold(0) { result, i ->
                result or (rows[group * 4 + i] shl (8 * i))
            }
            setWord("sprite", word, offset = group * 4)
        }
        for (cell in 0 until 128) setWord("frame", 0x12345678, offset = cell * 4)
        val frameCanary = 0x13579bdf xor index
        val rowsCanary = 0x2468ace0 xor index
        setWord("frame_guard", frameCanary)
        setWord("rows_guard", rowsCanary)
        runToExit()
        expectBytes("sprite", *rows.toIntArray())
        expectBytes("mirrored_rows", *rows.map(::reverseByte).toIntArray())
        for (y in 0 until 8) for (x in 0 until 8) {
            val normal = if ((rows[y] and (128 ushr x)) != 0) 0xffffff else 0
            val mirror = if ((rows[y] and (1 shl x)) != 0) 0xffffff else 0
            expectWord("frame", normal, offset = (y * 16 + x) * 4)
            expectWord("frame", mirror, offset = (y * 16 + x + 8) * 4)
        }
        expectWord("frame_guard", frameCanary)
        expectWord("rows_guard", rowsCanary)
        expectOutput("")
    }
}
