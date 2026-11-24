// The oracle edits a Boolean bit sequence, independently of target masking.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder

data class WriterInput(
    val width: Long = 9,
    val value: Long = 419,
    val used: Long = 3,
    val capacity: Long = 16,
    val bad: String = "",
)

fun AssemblyCase.writer64(offset: Int, value: Long, little: Boolean) {
  setWord("h263Writer_context", (if (little) value else value ushr 32).toInt(), 16 + offset)
  setWord("h263Writer_context", (if (little) value ushr 32 else value).toInt(), 20 + offset)
}

fun AssemblyCase.checkWriter(c: WriterInput, little: Boolean) {
  val invalid = c.bad.isNotEmpty()
  val enough = c.width <= 8 * c.capacity - c.used
  val success = !invalid && enough
  val pointer = readDataAddress("h263Writer_buffer") + 17
  writer64(0, if (c.bad == "buffer-wrap") -4L else pointer, little)
  writer64(8, c.capacity, little)
  writer64(16, c.used, little)
  val contextBefore = readBytes("h263Writer_context", 56)
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, n) in saved.withIndex()) setRegister(n, 0x1234567800000000L + i)
  setRegister("sp", readDataAddress("h263Writer_stack", 16400))
  val stack = readRegister("sp")
  val bufferTrace = traceMemoryAccesses(readDataAddress("h263Writer_buffer"), 1048608)
  val contextTrace = traceMemoryAccesses(readDataAddress("h263Writer_context"), 56)
  val work = traceExecution()
  setRegister(
      "a0",
      when (c.bad) {
        "context-align" -> readDataAddress("h263Writer_context") + 17
        "context-wrap" -> -8L
        else -> readDataAddress("h263Writer_context") + 16
      },
  )
  setRegister("a1", c.value)
  setRegister("a2", c.width)
  callFunction("h263_write_bits", "h263Writer_return")
  expectRegister("v0", if (invalid) 1 else if (enough) 0 else 2)
  expectRegister("v1", if (success) c.width else 0)
  val contextAfter = readBytes("h263Writer_context", 56)
  val expectedContext = contextBefore.copyOf()
  if (success && c.width > 0) {
    val used = c.used + c.width
    for (j in 0..7) expectedContext[32 + j] =
        ((used ushr (8 * (if (little) j else 7 - j))) and 255).toInt()
  }
  expectCondition(
      contextAfter.contentEquals(expectedContext),
      "Only a successful nonempty append changes the used-bit count",
  )
  if (success && c.width > 0) {
    val first = (c.used / 8).toInt()
    val last = ((c.used + c.width - 1) / 8).toInt()
    val expected = IntArray(last - first + 1) { 165 }
    val bits = MutableList(expected.size * 8) { i -> (165 ushr (7 - i % 8)) and 1 }
    for (i in 0 until c.width.toInt()) bits[(c.used % 8).toInt() + i] =
        ((c.value ushr (c.width.toInt() - 1 - i)) and 1).toInt()
    for (i in expected.indices) expected[i] = (0..7).sumOf { j -> bits[8 * i + j] shl (7 - j) }
    expectCondition(
        readBytes("h263Writer_buffer", expected.size, 17 + first).contentEquals(expected),
        "MSB-first field bits replace exactly their own bit window",
    )
  }
  for (e in bufferTrace.snapshot()) expectCondition(
      success &&
          c.width > 0 &&
          (0 until e.length).all { i ->
            val at = e.address - pointer + i
            at >= c.used / 8 && at <= (c.used + c.width - 1) / 8
          },
      "No output access outside touched bytes; rejected/empty appends never access output",
  )
  for (e in contextTrace.snapshot()) expectCondition(
      !e.write ||
          (success &&
              c.width > 0 &&
              e.address >= readDataAddress("h263Writer_context") + 32 &&
              e.address + e.length <= readDataAddress("h263Writer_context") + 40),
      "Only the committed counter is written",
  )
  expectCondition(
      readBytes("h263Writer_buffer", 17).all { it == 165 } &&
          readBytes("h263Writer_buffer", 15, 1048593).all { it == 165 },
      "Output outer guards retained",
  )
  expectCondition(
      readBytes("h263Writer_stack", 16).all { it == 165 } &&
          readBytes("h263Writer_stack", 16, 16400).all { it == 165 },
      "Stack guards retained",
  )
  for ((i, n) in saved.withIndex()) expectRegister(n, 0x1234567800000000L + i)
  expectRegister("sp", stack)
  if (success && c.width == 32L && c.used == 3L)
      println(
          "WRITER_WORK width=32 used=3 value=${c.value} little=$little instructions=${work.snapshot().instructions}",
      )
}

for (little in listOf(false, true)) {
  for (width in listOf(0L, 1L, 7L, 8L, 9L, 22L, 32L)) for (offset in 0L..7L) {
    val value = if (width == 0L) 0 else (1L shl width.toInt()) - 1
    case(
        "Writer width=$width offset=$offset little=$little",
        instructionLimit = 10000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkWriter(WriterInput(width, value, offset), little)
    }
  }
  for (width in listOf(1L, 9L, 32L)) for (value in listOf(0L, 1L)) {
    case(
        "Writer zero/one width=$width value=$value little=$little",
        instructionLimit = 10000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkWriter(WriterInput(width, value, 5), little)
    }
  }
  for (c in
      listOf(
          WriterInput(width = 0, value = 0, used = 0, capacity = 0),
          WriterInput(width = 1, value = 1, used = 7, capacity = 1),
          WriterInput(width = 2, value = 3, used = 7, capacity = 1),
          WriterInput(width = 1, value = 1, used = 8, capacity = 1),
          WriterInput(width = 32, value = 0x89abcdefL, used = 8388576, capacity = 1048576),
      )) {
    case(
        "Writer capacity $c little=$little",
        instructionLimit = 10000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkWriter(c, little)
    }
  }
  for (c in
      listOf(
          WriterInput(bad = "context-align"),
          WriterInput(bad = "context-wrap"),
          WriterInput(bad = "buffer-wrap"),
          WriterInput(capacity = 1048577, bad = "capacity"),
          WriterInput(capacity = 4294967312L, bad = "capacity"),
          WriterInput(used = 129, bad = "used"),
          WriterInput(used = -1, bad = "used"),
          WriterInput(width = 33, bad = "width"),
          WriterInput(width = 4294967297L, bad = "width"),
          WriterInput(width = 0, value = 1, bad = "value"),
          WriterInput(width = 8, value = 256, bad = "value"),
          WriterInput(width = 32, value = 4294967296L, bad = "value"),
          WriterInput(width = 32, value = -1, bad = "value"),
      )) {
    case(
        "Writer invalid $c little=$little",
        instructionLimit = 10000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkWriter(c, little)
    }
  }
}
