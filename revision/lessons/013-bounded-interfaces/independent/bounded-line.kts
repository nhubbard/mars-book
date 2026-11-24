data class LineInput(val name: String, val bytes: List<Int>, val count: Int, val offset: Int = 0)

val inputs = mutableListOf<LineInput>()

for (length in listOf(0, 1, 2, 3, 7, 31, 32)) {
  val bytes = List(length) { (it * 129 + 65) and 255 } + listOf(10, 0x42, 0xff)
  for (n in 0..length + 1) inputs += LineInput("prefix $length/$n", bytes, n, n % 8)
  inputs += LineInput("suffix $length", bytes, bytes.size, 7)
}

for (byte in 0..255) inputs += LineInput("one byte $byte", listOf(byte, 10, 99), 3, byte % 8)

for (n in listOf(0, 1, 31, 32, 33, 34, 80, 256)) inputs +=
    LineInput("no delimiter n$n", List(256) { 65 }, n)

for (length in listOf(31, 32, 33, 64)) for (offset in 0..7) inputs +=
    LineInput("limit $length offset$offset", List(length) { 65 } + 10, length + 1, offset)

for ((number, input) in inputs.withIndex()) case(input.name, instructionLimit = 5000) {
  val bytes = List(input.offset) { 0xa5 } + input.bytes
  val arena = bytes + List(256 - bytes.size) { (it * 37 + 19) and 255 }
  for (word in 0..63) setWord(
      "input",
      (0..3).fold(0) { value, k -> value or (arena[4 * word + k] shl (8 * k)) },
      offset = 4 * word,
  )
  val target = if (number % 2 == 0) "line_a" else "line_b"
  val other = if (target == "line_a") "line_b" else "line_a"
  for (index in 0..1) {
    setWord(target, 0x5a123400 + index, offset = 4 * index)
    setWord(other, 0x6b234500 + index, offset = 4 * index)
  }
  val prefix = input.bytes.take(minOf(input.count, 33))
  val delimiter = prefix.indexOf(10)
  val result = if (delimiter >= 0) delimiter + 1 else if (input.count < 33) -1 else -2
  val permitted = if (result > 0) result else minOf(input.count, 33)
  val before = readBytes(target, 8)
  val sourceBefore = readBytes("input_guard", 264)
  val protected =
      listOf(traceMemoryStores("input_guard", 264), traceMemoryStores(other, 8)) +
          listOf("line_a_guard", "line_b_guard", "available", "status", "state_end").map {
            traceMemoryStores(it, 4)
          } +
          listOf(traceMemoryStores("payload_address", 8))
  val output = traceMemoryStores(target, 8)
  val reads = traceMemoryAccesses("input_guard", 264, maxAccesses = 100000)
  val writes =
      traceMemoryAccesses(
          "input_guard",
          (readDataAddress("state_end") + 4 - readDataAddress("input_guard")).toInt(),
          maxAccesses = 100000,
      )
  val saved =
      (0..7).associate { "s$it" to (0x1122334400000000L + it) } +
          mapOf("fp" to 0x23456789abcdef01L, "gp" to 0x3456789abcdef012L)
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val caller = readBytes(top, 32)
  val callerWrites = traceMemoryStores(top, 32)
  saved.forEach { (name, value) -> setRegister(name, value) }
  setRegisterToDataAddress("a0", "input", offset = input.offset.toLong())
  setRegister("a1", input.count)
  setRegisterToDataAddress("a2", target)
  val continuation = if (number % 2 == 0) "line_function_return" else "line_done"
  val avoided = traceExecution("line_function_return", "line_done")
  callFunction("read_line", returnTo = continuation)
  expectCondition(avoided.snapshot().instructions == 0L, "Return to the supplied continuation")
  expectRegister("v0", result)
  expectRegister("v1", if (result > 0) readDataAddress("input") + input.offset else 0L)
  expectRegister("sp", top)
  saved.forEach { (name, value) -> expectRegister(name, value) }
  expectCondition(
      readBytes(top, 32).contentEquals(caller),
      "Caller-owned stack bytes remain intact",
  )
  callerWrites.expectNone()
  reads.expectReadsWithin(readDataAddress("input") + input.offset, permitted)
  if (result < 0) {
    output.expectNone()
    expectBytes(target, *before)
  } else {
    expectWord(target, result - 1)
    expectWord(target, input.bytes.take(result - 1).sum() and 255, offset = 4)
  }
  fun verifyProtection() {
    protected.forEach { it.expectNone() }
    expectBytes("input_guard", *sourceBefore)
    val first = readDataAddress(target)
    for (access in writes.snapshot().filter { it.write }) expectCondition(
        result > 0 &&
            access.address >= first &&
            access.length <= 8 &&
            access.address - first <= 8 - access.length,
        "Only the selected output record is written, and only on success",
    )
  }
  verifyProtection()
  runToExit()
  verifyProtection()
  expectOutput("")
}

case("complete default caller retains zero and high payload bytes", instructionLimit = 5000) {
  val readonly =
      listOf(
          traceMemoryStores("input_guard", 264),
          traceMemoryStores("line_b", 8),
          traceMemoryStores("available", 4),
      )
  val reads = traceMemoryAccesses("input_guard", 264)
  runUntil("line_ready")
  expectRegister("v0", 4)
  expectRegister("v1", readDataAddress("input"))
  expectWord("line_a", 3)
  expectWord("line_a", 193, offset = 4)
  runToExit()
  expectWord("status", 4)
  val pointer =
      readBytes("payload_address", 8).withIndex().fold(0L) { value, (i, b) ->
        value or (b.toLong() shl (8 * i))
      }
  expectCondition(pointer == readDataAddress("input"), "Caller retains the full payload pointer")
  reads.expectReadsWithin(readDataAddress("input"), 4)
  readonly.forEach { it.expectNone() }
  expectOutput("")
}
