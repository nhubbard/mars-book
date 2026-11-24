import edu.missouristate.mars.cli.grading.AssemblyCase

// Public oracle derived from the published format; it does not call either assembly codec.
data class WireResult(val inputUsed: Int, val values: List<Int>, val status: Int)

fun unsignedGroups(value: Long): List<Int> {
  require(value in 0..8589934591L)
  val digits = mutableListOf<Int>()
  var rest = value
  do {
    digits.add((rest and 127L).toInt())
    rest = rest ushr 7
  } while (rest != 0L)
  return digits.mapIndexed { index, digit -> digit + if (index < digits.lastIndex) 128 else 0 }
}

fun encodedReadings(readings: List<Int>, capacity: Int): WireResult {
  var predecessor = 0L
  val bytes = mutableListOf<Int>()
  for (index in readings.indices) {
    if (bytes.size == capacity) return WireResult(index, bytes, 1)
    val delta = readings[index].toLong() - predecessor
    val mapped = if (delta < 0) 2 * (-delta) - 1 else 2 * delta
    val item = unsignedGroups(mapped)
    if (item.size > capacity - bytes.size) return WireResult(index, bytes, 1)
    bytes.addAll(item)
    predecessor = readings[index].toLong()
  }
  return WireResult(readings.size, bytes, 0)
}

fun decodedBytes(bytes: List<Int>, capacity: Int): WireResult {
  val readings = mutableListOf<Int>()
  var committed = 0
  var predecessor = 0L
  while (committed < bytes.size) {
    if (readings.size == capacity) return WireResult(committed, readings, 1)
    // Inspect a bounded candidate before folding its digits in reverse significance order.
    val candidate = bytes.drop(committed).take(5)
    val terminal = candidate.indexOfFirst { it < 128 }
    if (terminal < 0) return WireResult(committed, readings, 2)
    val item = candidate.take(terminal + 1)
    if ((item.size == 5 && item.last() >= 32) || (item.size > 1 && item.last() == 0))
        return WireResult(committed, readings, 2)
    val unsigned = item.asReversed().fold(0L) { value, byte -> value * 128 + (byte and 127) }
    val delta = if (unsigned and 1L == 0L) unsigned / 2 else -(unsigned + 1) / 2
    val value = predecessor + delta
    if (value !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
        return WireResult(committed, readings, 3)
    readings.add(value.toInt())
    predecessor = value
    committed += item.size
  }
  return WireResult(committed, readings, 0)
}

fun AssemblyCase.checkWire(
    input: List<Int>,
    capacity: Int,
    decode: Boolean,
    caller: Boolean = false,
    finish: Boolean = true,
    continuation: String = "function_return",
) {
  val expected = if (decode) decodedBytes(input, capacity) else encodedReadings(input, capacity)
  val wordOffset = if (caller) 0 else (input.size + capacity) % 5
  val byteOffset = if (caller) 0 else (input.size + 3 * capacity) % 8
  val initialWords = MutableList(40) { 0x6a6b6c6d }
  val initialBytes = MutableList(192) { 165 }
  input.forEachIndexed { index, value ->
    if (decode) initialBytes[byteOffset + index] = value
    else initialWords[wordOffset + index] = value
  }
  initialWords.forEachIndexed { index, value -> setWord("words", value, offset = index * 4) }
  initialBytes.chunked(4).forEachIndexed { index, bytes ->
    setWord(
        "stream",
        bytes.foldIndexed(0) { j, word, byte -> word or (byte shl (8 * j)) },
        offset = index * 4,
    )
  }
  setWord("method", if (decode) 1 else 0)
  setWord(if (decode) "byte_count" else "word_count", input.size)
  setWord(if (decode) "word_capacity" else "byte_capacity", capacity)
  listOf("input_used", "output_used", "status").forEach { setWord(it, -1) }
  val saved = List(9) { 0x56789abc00000000L + it * 509 }
  for (index in 0..7) setRegister("s$index", saved[index])
  setRegister("fp", saved[8])
  setRegister("sp", 0x7fff0000L)
  // Poison caller-saved working state; no predecessor survives from a preceding call.
  for (index in 0..9) setRegister("t$index", -777L - index)
  if (caller) runUntil("codec_call")
  else {
    setRegisterToDataAddress(
        "a0",
        if (decode) "stream" else "words",
        if (decode) byteOffset.toLong() else wordOffset * 4L,
    )
    setRegister("a1", input.size)
    setRegisterToDataAddress(
        "a2",
        if (decode) "words" else "stream",
        if (decode) wordOffset * 4L else byteOffset.toLong(),
    )
    setRegister("a3", capacity)
  }
  val source =
      readDataAddress(
          if (decode) "stream" else "words",
          if (decode) byteOffset.toLong() else wordOffset * 4L,
      )
  val destination =
      readDataAddress(
          if (decode) "words" else "stream",
          if (decode) wordOffset * 4L else byteOffset.toLong(),
      )
  val readable = if (capacity == 0) 0 else input.size * (if (decode) 1 else 4)
  val writable = expected.values.size * (if (decode) 4 else 1)
  val root = readDataAddress("words")
  val extent = (readDataAddress("control_end") + 4 - root).toInt()
  val before = readBytes("words", extent)
  val owned =
      listOf(
              "words" to 160,
              "words_end" to 4,
              "stream" to 192,
              "stream_end" to 4,
              "word_count" to 4,
              "byte_capacity" to 4,
              "byte_count" to 4,
              "word_capacity" to 4,
              "method" to 4,
              "input_used" to 4,
              "output_used" to 4,
              "status" to 4,
              "control_end" to 4,
          )
          .map { (label, length) -> readDataAddress(label) to length }
  fun inside(address: Long, length: Int, base: Long, size: Int) =
      address >= base && length.toLong() <= size && address - base <= size.toLong() - length
  val trace = traceMemoryAccesses(root, extent, maxAccesses = 100000)
  if (caller) runUntil("codec_done")
  else callFunction(if (decode) "delta_decode" else "delta_encode", continuation)
  trace.close()
  expectRegister("v0", expected.inputUsed)
  expectRegister("v1", expected.values.size)
  expectRegister("a3", expected.status)
  expectRegister("sp", 0x7fff0000L)
  expectRegister("fp", saved[8])
  for (index in 0..7) expectRegister("s$index", saved[index])
  val initialized = BooleanArray(writable)
  for (event in trace.snapshot()) {
    val output = inside(event.address, event.length, destination, writable)
    val inputRead = inside(event.address, event.length, source, readable)
    val callerObject = owned.any { (base, size) ->
      event.address < base + size && event.address + event.length > base
    }
    expectCondition(
        if (event.write) output else output || inputRead || !callerObject,
        "Access only the selected input and committed output; private constants are readable",
    )
    if (output)
        for (index in
            (event.address - destination).toInt() until
                (event.address - destination).toInt() + event.length) {
          if (event.write) initialized[index] = true
          else
              expectCondition(initialized[index], "Read output only after this call initialized it")
        }
  }
  expectCondition(initialized.all { it }, "Write every committed output byte")
  fun verifyData(afterCaller: Boolean) {
    initialWords.forEachIndexed { index, value ->
      expectWord(
          "words",
          if (decode && index - wordOffset in expected.values.indices)
              expected.values[index - wordOffset]
          else value,
          offset = 4 * index,
      )
    }
    initialBytes.forEachIndexed { index, value ->
      expectBytes(
          "stream",
          if (!decode && index - byteOffset in expected.values.indices)
              expected.values[index - byteOffset]
          else value,
          offset = index,
      )
    }
    val stores =
        listOf(
            "input_used" to expected.inputUsed,
            "output_used" to expected.values.size,
            "status" to expected.status,
        )
    stores.forEach { (label, value) -> expectWord(label, if (afterCaller) value else -1) }
    val callerStores = stores.map { readDataAddress(it.first) }
    val after = readBytes("words", extent)
    for (index in before.indices) if (
        root + index !in destination until destination + writable &&
            callerStores.none { root + index in it until it + 4 }
    )
        expectCondition(before[index] == after[index], "Keep all other caller bytes unchanged")
  }
  verifyData(false)
  if (finish) {
    runUntil("results_ready")
    verifyData(true)
    runToExit()
    verifyData(true)
    expectOutput("")
  }
}

for ((index, input) in
    listOf(
            emptyList(),
            listOf(1000, 1002, 1001, 1001, 20000),
            List(32) { if (it % 2 == 0) Int.MIN_VALUE else Int.MAX_VALUE },
            listOf(0, 64, -64, 8192, -8192),
        )
        .withIndex()) case("same session composition $index", instructionLimit = 1000000) {
  checkWire(input, 160, false, finish = false)
  val wire =
      readBytes("stream", encodedReadings(input, 160).values.size, offset = (input.size + 480) % 8)
          .toList()
  expectCondition(
      decodedBytes(wire, 32).values == input,
      "Decode the actual encoder output in the same execution session",
  )
  checkWire(wire, 32, true, finish = false, continuation = "codec_done")
  checkWire(listOf(128, 0), 0, true, finish = false)
  checkWire(emptyList(), 0, true, finish = false, continuation = "codec_done")
  checkWire(listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE), 160, false)
}
