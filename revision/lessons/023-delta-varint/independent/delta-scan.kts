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

fun AssemblyCase.verifyScan(input: List<Int>, capacity: Int, seed: Int) {
  val expected = decodedBytes(input, capacity)
  val offset = seed % 32
  val data = IntArray(192) { (it * 31 + seed * 17 + 193) and 255 }
  for (j in input.indices) data[offset + j] = input[j]
  for (j in 0 until 192 step 4) {
    var word = 0
    for (k in 0..3) word = word or (data[j + k] shl (k * 8))
    setWord("stream", word, offset = j)
  }
  val root = readDataAddress("stream_guard")
  val size = (readDataAddress("caller_end") + 4 - root).toInt()
  val before = readBytes("stream_guard", size)
  val pointer = readDataAddress("stream") + offset
  val trace = traceMemoryAccesses(root, size, maxAccesses = 20000)
  for (j in 0..7) setRegister("s$j", 0x56789abc00000000L + j * 509)
  setRegister("fp", 0x2345678900000055L)
  setRegister("sp", 0x7fff0000L)
  setRegisterToDataAddress("a0", "stream", offset.toLong())
  setRegister("a1", input.size)
  setRegister("a2", capacity)
  setRegister("a3", 99)
  callFunction("scan_delta", if (seed % 2 == 0) "function_return" else "done")
  trace.close()
  expectRegister("v0", expected.inputUsed)
  expectRegister("v1", expected.values.size)
  expectRegister("a2", expected.values.lastOrNull() ?: 0)
  expectRegister("a3", expected.status)
  expectRegister("sp", 0x7fff0000L)
  expectRegister("fp", 0x2345678900000055L)
  for (j in 0..7) expectRegister("s$j", 0x56789abc00000000L + j * 509)
  expectCondition(
      before.contentEquals(readBytes("stream_guard", size)),
      "Scanner leaves every caller byte unchanged",
  )
  for (event in trace.snapshot()) expectCondition(
      !event.write &&
          capacity > 0 &&
          event.address >= pointer &&
          event.length <= input.size &&
          event.address - pointer <= input.size - event.length,
      "Read only selected input; empty or zero-limit input has no access; no transient caller writes",
  )
  expectOutput("")
}

val datasets =
    listOf(
        emptyList(),
        listOf(0),
        listOf(-1),
        listOf(1000, 1002, 1001, 1001, 20000),
        listOf(Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE),
        List(32) { 1000 },
        List(32) { it - 16 },
        List(32) { if (it == 16) 1000000 else 0 },
    )

for ((j, values) in datasets.withIndex()) for (limit in 0..32) case(
    "scan sequence $j limit=$limit",
    instructionLimit = 1000000,
) {
  verifyScan(encodedReadings(values, 160).values, limit, j * 33 + limit)
}

val bad =
    listOf(
        listOf(128),
        listOf(128, 0),
        listOf(129, 0),
        listOf(128, 128, 128, 128, 32),
        listOf(128, 128, 128, 128, 128),
        listOf(255, 255, 255, 255, 31),
        listOf(254, 255, 255, 255, 15, 2),
        listOf(2, 128, 0),
    )

for ((j, bytes) in bad.withIndex()) for (limit in 0..3) case(
    "scan rejected candidate $j limit=$limit",
    instructionLimit = 1000000,
) {
  verifyScan(bytes, limit, j * 4 + limit)
}

case("default scanner publishes last reading", instructionLimit = 1000000) {
  val original = readBytes("stream", 192)
  runUntil("scan_ready")
  expectRegister("v0", 8)
  expectRegister("v1", 5)
  expectRegister("a2", 20000)
  expectRegister("a3", 0)
  for (label in listOf("input_used", "reading_count", "last_reading", "status")) expectWord(
      label,
      -1,
  )
  runUntil("results_ready")
  expectWord("input_used", 8)
  expectWord("reading_count", 5)
  expectWord("last_reading", 20000)
  expectWord("status", 0)
  expectCondition(
      original.contentEquals(readBytes("stream", 192)),
      "Ordinary caller retains stream",
  )
  runToExit()
  expectOutput("")
}
