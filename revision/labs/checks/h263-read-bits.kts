import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder

fun readerField(a: IntArray, at: Int, value: Long, little: Boolean) {
  for (j in 0..7) a[at + j] = ((value ushr (8 * (if (little) j else 7 - j))) and 255).toInt()
}

fun AssemblyCase.readerPut(label: String, a: IntArray, little: Boolean, offset: Int = 0) {
  for (i in a.indices step 4) {
    var word = 0
    for (j in 0..3) word = word or (a[i + j] shl (8 * (if (little) j else 3 - j)))
    setWord(label, word, offset + i)
  }
}

fun AssemblyCase.readField(
    capacity: Long,
    validBits: Long,
    cursor: Long,
    width: Long,
    little: Boolean,
    pattern: Int = 0,
    bad: String = "",
) {
  val metadata =
      capacity in 0L..1048576L &&
          validBits >= 0 &&
          validBits <= 8 * capacity &&
          cursor >= 0 &&
          cursor <= validBits &&
          width in 0L..32L &&
          bad.isEmpty()
  val success = metadata && width <= validBits - cursor
  val sourceOffset = 1 + pattern % 7
  val base = readDataAddress("reader_bytes", sourceOffset.toLong())
  val context = readDataAddress("reader_context")
  val firstByte = if (metadata) (cursor / 8).toInt() else 0
  val zone = (sourceOffset + firstByte - 16).coerceIn(0, 1048544) / 4 * 4
  fun value(at: Int): Int =
      when (pattern % 5) {
        0 -> 165
        1 -> 255
        2 -> 0
        3 -> (at * 73 + 19) and 255
        else -> if (at % 2 == 0) 128 else 1
      }
  val sample = IntArray(64) { value(zone + it) }
  readerPut("reader_bytes", sample, little, zone)
  val descriptor = IntArray(48) { 165 }
  readerField(descriptor, 0, if (bad == "buffer-wrap") -1L else base, little)
  readerField(descriptor, 8, capacity, little)
  readerField(descriptor, 16, validBits, little)
  readerField(descriptor, 24, cursor, little)
  readerPut("reader_context", descriptor, little)
  setRegister(
      "a0",
      when (bad) {
        "context-align" -> context + 4
        "context-wrap" -> -16L
        else -> context
      },
  )
  setRegister("a1", width)
  val stack = readDataAddress("reader_stack", 8192)
  setRegister("sp", stack)
  val names = (0..7).map { "s$it" } + listOf("fp", "gp")
  for ((j, n) in names.withIndex()) setRegister(n, 0x3141592600000000L + j * 977)
  val before = readBytes("reader_bytes", 64, zone)
  val status = readBytes("reader_status", 8)
  val result = readBytes("reader_value", 8)
  val trace =
      traceMemoryAccesses(
          readDataAddress("reader_bytes"),
          (readDataAddress("reader_value") + 8 - readDataAddress("reader_bytes")).toInt(),
          maxAccesses = 2000,
      )
  callFunction("h263_read_bits", "reader_function_return")
  trace.close()
  expectRegister("v0", if (!metadata) 1 else if (!success) 2 else 0)
  var expected = 0L
  if (success)
      for (j in 0 until width.toInt()) {
        val at = cursor + j
        val v = value(sourceOffset + (at / 8).toInt())
        expected = (expected shl 1) or ((v ushr (7 - (at % 8).toInt())) and 1).toLong()
      }
  expectRegister("v1", expected)
  expectRegister("sp", stack)
  for ((j, n) in names.withIndex()) expectRegister(n, 0x3141592600000000L + j * 977)
  expectOutput("")
  val after = descriptor.copyOf()
  if (success && width > 0) readerField(after, 24, cursor + width, little)
  expectCondition(
      readBytes("reader_context", 48).contentEquals(after),
      "Only successful nonempty read advances cursor",
  )
  expectCondition(
      readBytes("reader_bytes", 64, zone).contentEquals(before),
      "Input and selected neighbouring bytes retained",
  )
  expectCondition(
      readBytes("reader_status", 8).contentEquals(status) &&
          readBytes("reader_value", 8).contentEquals(result),
      "Caller result fields retained",
  )
  val inputStart = base + if (metadata) cursor / 8 else 0
  val inputEnd = base + if (metadata) (cursor + width + 7) / 8 else 0
  for (e in trace.snapshot()) {
    val inContext = e.address >= context && e.address + e.length <= context + 48
    val allowed =
        if (e.write)
            success &&
                width > 0 &&
                e.address >= context + 24 &&
                e.address + e.length <= context + 32
        else if (inContext)
            bad !in listOf("context-align", "context-wrap") && e.address + e.length <= context + 32
        else success && width > 0 && e.address >= inputStart && e.address + e.length <= inputEnd
    expectCondition(
        allowed,
        "Only validated context and selected field bytes are accessed; no input writes",
    )
  }
}

for (little in listOf(false, true)) for (width in 0L..32L) for (phase in 0L..7L) {
  val cursor = 8 + phase
  val valid = cursor + width + (width % 3)
  val capacity = (valid + 7) / 8
  case(
      "reader width=$width phase=$phase little=$little",
      instructionLimit = 100000,
      byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
  ) {
    readField(capacity, valid, cursor, width, little, (width.toInt() + phase.toInt()) % 5)
  }
}

for (little in listOf(false, true)) for (pattern in 0..4) case(
    "unsigned full field pattern=$pattern little=$little",
    instructionLimit = 100000,
    byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
) {
  readField(8, 64, 7, 32, little, pattern)
}

for (little in listOf(false, true)) for (width in listOf(0L, 1L, 7L, 32L)) {
  case(
      "zero storage width=$width little=$little",
      instructionLimit = 100000,
      byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
  ) {
    readField(0, 0, 0, width, little)
  }
  case(
      "last available bit width=$width little=$little",
      instructionLimit = 100000,
      byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
  ) {
    readField(2, 12, 12, width, little, 3)
  }
}

for (little in listOf(false, true)) for ((capacity, valid, cursor, width) in
    listOf(
        listOf(-1L, 0L, 0L, 0L),
        listOf(1048577L, 8L, 0L, 1L),
        listOf(0x100000002L, 12L, 3L, 9L),
        listOf(2L, 17L, 0L, 1L),
        listOf(2L, -1L, 0L, 1L),
        listOf(2L, 0x10000000cL, 3L, 9L),
        listOf(2L, 12L, 13L, 0L),
        listOf(2L, 12L, -1L, 0L),
        listOf(2L, 12L, 0x100000003L, 1L),
        listOf(2L, 12L, 3L, 33L),
        listOf(2L, 12L, 3L, -1L),
        listOf(2L, 12L, 3L, 0x100000009L),
    )) case(
    "invalid reader $capacity $valid $cursor $width little=$little",
    instructionLimit = 100000,
    byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
) {
  readField(capacity, valid, cursor, width, little, 4)
}

for (little in listOf(false, true)) for (bad in
    listOf("context-align", "context-wrap", "buffer-wrap")) case(
    "invalid reader $bad little=$little",
    instructionLimit = 100000,
    byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
) {
  readField(2, 12, 3, 0, little, 3, bad)
}

for (little in listOf(false, true)) for (cursor in listOf(8388576L, 8388607L, 8388608L)) case(
    "maximum capacity cursor=$cursor little=$little",
    instructionLimit = 100000,
    byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
) {
  readField(
      1048576,
      8388608,
      cursor,
      if (cursor == 8388576L) 32 else if (cursor == 8388607L) 1 else 0,
      little,
      1,
  )
}

for (little in listOf(false, true)) case(
    "success, failure and empty read share one cursor little=$little",
    instructionLimit = 100000,
    byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
) {
  val bytes = IntArray(64)
  bytes[0] = 186
  bytes[1] = 53
  readerPut("reader_bytes", bytes, little)
  val context = readDataAddress("reader_context")
  val input = readDataAddress("reader_bytes")
  val fields = IntArray(48) { 165 }
  readerField(fields, 0, input, little)
  readerField(fields, 8, 2, little)
  readerField(fields, 16, 12, little)
  readerField(fields, 24, 0, little)
  readerPut("reader_context", fields, little)
  val stack = readDataAddress("reader_stack", 8192)
  setRegister("sp", stack)
  var cursor = 0L
  for ((width, status, value) in
      listOf(Triple(3L, 0, 5L), Triple(9L, 0, 419L), Triple(1L, 2, 0L), Triple(0L, 0, 0L))) {
    setRegister("a0", context)
    setRegister("a1", width)
    val trace =
        traceMemoryAccesses(
            input,
            (readDataAddress("reader_value") + 8 - input).toInt(),
            maxAccesses = 2000,
        )
    callFunction("h263_read_bits", "reader_function_return")
    trace.close()
    expectRegister("v0", status)
    expectRegister("v1", value)
    expectRegister("sp", stack)
    val before = cursor
    if (status == 0) cursor += width
    for (e in trace.snapshot()) if (e.write)
        expectCondition(
            status == 0 &&
                width > 0 &&
                e.address >= context + 24 &&
                e.address + e.length <= context + 32,
            "Only successful nonempty cursor writes in reused state",
        )
    else
        expectCondition(
            e.address >= context && e.address + e.length <= context + 32 ||
                status == 0 &&
                    width > 0 &&
                    e.address >= input + before / 8 &&
                    e.address + e.length <= input + (before + width + 7) / 8,
            "Only selected field bytes read in reused state",
        )
    readerField(fields, 24, cursor, little)
    expectCondition(
        readBytes("reader_context", 48).contentEquals(fields),
        "Reused cursor and retained context",
    )
  }
  expectCondition(readBytes("reader_bytes", 64).contentEquals(bytes), "Reused input retained")
  expectOutput("")
}
