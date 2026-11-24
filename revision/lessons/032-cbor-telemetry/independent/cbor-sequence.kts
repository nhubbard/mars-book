import edu.missouristate.mars.cli.grading.AssemblyCase

// Host observations for the published bounded profile. No MIPS implementation is supplied.
data class CborHead(val kind: Int, val argument: ULong, val width: Int)

data class Telemetry(val ticks: ULong, val device: ByteArray, val readings: IntArray)

fun bytes(hex: String) = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun little(value: Long, width: Int) = ByteArray(width) { (value ushr (8 * it)).toByte() }

fun unsignedField(raw: ByteArray, at: Int, width: Int): ULong =
    (0 until width).fold(0uL) { value, i ->
      value or ((raw[at + i].toULong() and 255uL) shl (8 * i))
    }

fun headAt(raw: ByteArray, position: Int = 0): CborHead? = runCatching {
  require(position in raw.indices)
  val initial = raw[position].toInt() and 255
  val kind = initial ushr 5
  val info = initial and 31
  require(kind in listOf(0, 1, 2, 4, 5) && info < 28)
  if (info < 24) CborHead(kind, info.toULong(), 1)
  else {
    val count = 1 shl (info - 24)
    require(raw.size - position - 1 >= count)
    var argument = 0uL
    for (i in 1..count) argument = (argument shl 8) or (raw[position + i].toULong() and 255uL)
    CborHead(kind, argument, count + 1)
  }
}
    .getOrNull()

// Return a byte cursor, retaining independent duplicate-key identity at each map.
fun valueEnd(raw: ByteArray, start: Int, open: Int): Int {
  val h = requireNotNull(headAt(raw, start))
  var cursor = start + h.width
  when (h.kind) {
    0,
    1 -> return cursor
    2 -> {
      require(h.argument <= 32uL && h.argument <= (raw.size - cursor).toULong())
      return cursor + h.argument.toInt()
    }
    4 -> {
      require(open < 8 && h.argument <= 64uL)
      repeat(h.argument.toInt()) { cursor = valueEnd(raw, cursor, open + 1) }
    }
    5 -> {
      require(open < 8 && h.argument <= 8uL)
      var seen = 0
      repeat(h.argument.toInt()) {
        val key = requireNotNull(headAt(raw, cursor))
        require(key.kind == 0 && key.argument < 8uL)
        val bit = 1 shl key.argument.toInt()
        require((seen and bit) == 0)
        seen = seen or bit
        cursor += key.width
        cursor = valueEnd(raw, cursor, open + 1)
      }
    }
  }
  return cursor
}

fun skipped(raw: ByteArray, depth: Long = 0): Int? = runCatching {
  require(raw.size <= 1024 && depth.toULong() <= 8uL)
  valueEnd(raw, 0, depth.toInt())
}
    .getOrNull()

fun telemetry(raw: ByteArray): Telemetry? = runCatching {
  require(raw.size in 7..1024)
  require(valueEnd(raw, 0, 0) == raw.size)
  val root = requireNotNull(headAt(raw))
  require(root.kind == 5 && root.argument in 3uL..8uL)
  var cursor = root.width
  var ticks: ULong? = null
  var device: ByteArray? = null
  var readings: IntArray? = null
  repeat(root.argument.toInt()) {
    val key = requireNotNull(headAt(raw, cursor))
    cursor += key.width
    val h = requireNotNull(headAt(raw, cursor))
    when (key.argument) {
      0uL -> {
        require(h.kind == 0)
        ticks = h.argument
        cursor += h.width
      }
      1uL -> {
        require(h.kind == 2)
        val at = cursor + h.width
        device = raw.copyOfRange(at, at + h.argument.toInt())
        cursor = at + h.argument.toInt()
      }
      2uL -> {
        require(h.kind == 4)
        cursor += h.width
        readings =
            IntArray(h.argument.toInt()) {
              val sample = requireNotNull(headAt(raw, cursor))
              require(sample.kind in 0..1 && sample.argument <= 32767uL)
              cursor += sample.width
              if (sample.kind == 0) sample.argument.toInt() else -1 - sample.argument.toInt()
            }
      }
      else -> cursor = valueEnd(raw, cursor, 1)
    }
  }
  require(cursor == raw.size)
  Telemetry(requireNotNull(ticks), requireNotNull(device), requireNotNull(readings))
}
    .getOrNull()

fun headBytes(kind: Int, argument: ULong): ByteArray {
  require(kind in listOf(0, 1, 2, 4, 5))
  if (argument < 24uL) return byteArrayOf(((kind shl 5) or argument.toInt()).toByte())
  val width =
      when {
        argument <= 255uL -> 1
        argument <= 65535uL -> 2
        argument <= 4294967295uL -> 4
        else -> 8
      }
  val selector =
      when (width) {
        1 -> 24
        2 -> 25
        4 -> 26
        else -> 27
      }
  return byteArrayOf(((kind shl 5) or selector).toByte()) +
      ByteArray(width) { (argument shr (8 * (width - 1 - it))).toByte() }
}

fun validRecord(record: Telemetry) =
    record.device.size <= 32 &&
        record.readings.size <= 64 &&
        record.readings.all { it in -32768..32767 }

fun preferred(record: Telemetry): ByteArray {
  require(validRecord(record))
  var output =
      byteArrayOf(0xa3.toByte(), 0) +
          headBytes(0, record.ticks) +
          byteArrayOf(1) +
          headBytes(2, record.device.size.toULong()) +
          record.device +
          byteArrayOf(2) +
          headBytes(4, record.readings.size.toULong())
  for (sample in record.readings) output +=
      headBytes(if (sample < 0) 1 else 0, (if (sample < 0) -1 - sample else sample).toULong())
  return output
}

fun flat(record: Telemetry): ByteArray {
  require(validRecord(record))
  val image = ByteArray(176) { 165.toByte() }
  little(record.ticks.toLong(), 8).copyInto(image)
  little(record.device.size.toLong(), 4).copyInto(image, 8)
  little(record.readings.size.toLong(), 4).copyInto(image, 12)
  record.device.copyInto(image, 16)
  for (i in record.readings.indices) little(record.readings[i].toLong(), 2)
      .copyInto(image, 48 + 2 * i)
  return image
}

val traceInput =
    bytes(
        "b80403a201824280ff21008002980a00181718182037381818ff190100197fff397fff01580300ff4118001b0102030405060708",
    )
val worked =
    Telemetry(
        0x0102030405060708uL,
        bytes("00ff41"),
        intArrayOf(0, 23, 24, -1, -24, -25, 255, 256, 32767, -32768),
    )

fun example(ticks: ULong = 0uL, length: Int = 3, count: Int = 10): Telemetry {
  val pattern = intArrayOf(0, 23, 24, -1, -24, -25, 255, 256, 32767, -32768)
  return Telemetry(
      ticks,
      ByteArray(length) { (it * 97 + 13).toByte() },
      IntArray(count) { pattern[it % pattern.size] },
  )
}

val arenas =
    linkedMapOf("source" to 1280, "record" to 256, "destination" to 320, "workspace" to 8192)

fun AssemblyCase.store(label: String, raw: ByteArray) {
  require(raw.size % 4 == 0)
  for (i in raw.indices step 4) {
    var word = 0
    for (j in 0..3) word = word or ((raw[i + j].toInt() and 255) shl (8 * j))
    setWord(label, word, i)
  }
}

fun AssemblyCase.install(label: String, raw: ByteArray = byteArrayOf(), offset: Int = 0) {
  val value = ByteArray(arenas.getValue(label)) { 165.toByte() }
  raw.copyInto(value, offset)
  store(label, value)
}

fun AssemblyCase.arrange(): List<Long> {
  for (name in arenas.keys) install(name)
  val saved = List(9) { 0x3456789a00000000L + it * 733 }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val top = readDataAddress("workspace", 8192)
  setRegister("sp", top)
  return saved + top
}

fun AssemblyCase.retained(saved: List<Long>) {
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", saved[9])
  expectOutput("")
}

fun AssemblyCase.image(label: String, raw: ByteArray = byteArrayOf(), offset: Int = 0) {
  val expected = ByteArray(arenas.getValue(label)) { 165.toByte() }
  raw.copyInto(expected, offset)
  val actual = readBytes(label, expected.size)
  val at = actual.indices.firstOrNull { actual[it] != (expected[it].toInt() and 255) }
  expectCondition(at == null, "$label differs at byte $at; check active fields and preserved tails")
}

fun fits(address: Long, length: Int, base: Long, size: Int) =
    length <= size && address >= base && address - base <= size - length

fun AssemblyCase.observe(
    reads: List<Pair<Long, Int>>,
    writes: List<Pair<Long, Int>>,
    action: () -> Unit,
) {
  val low = readDataAddress("source")
  val top = readDataAddress("workspace", 8192)
  val objects =
      listOf("source", "record", "destination").map { readDataAddress(it) to arenas.getValue(it) }
  val access = traceMemoryAccesses(low, (top - low).toInt(), maxAccesses = 500000)
  val files = provideFiles(emptyMap())
  try {
    action()
  } finally {
    access.close()
  }
  for (item in access.snapshot()) {
    val declared =
        (if (item.write) writes else reads).any { (base, size) ->
          fits(item.address, item.length, base, size)
        }
    val caller = objects.any { (base, size) ->
      item.address < base + size && base < item.address + item.length
    }
    expectCondition(
        !caller || declared,
        "Execution access crosses an active caller interval, publishes on failure, or reads an identifier before preflight",
    )
  }
  files.expectAllClosed()
  expectCondition(files.snapshot().isEmpty(), "Memory entries do not perform file I/O")
}

fun AssemblyCase.finish(saved: List<Long>, status: Int, count: Long) {
  expectRegister("v0", status)
  expectRegister("v1", count)
  retained(saved)
}

fun selected(raw: ByteArray, count: Long): ByteArray =
    ByteArray(count.toInt()) { if (it < raw.size) raw[it] else 165.toByte() }

fun AssemblyCase.headCheck(raw: ByteArray, count: Long = raw.size.toLong(), index: Int = 0) {
  val saved = arrange()
  val offset = index % 17
  install("source", raw, offset)
  val logical = if (count.toULong() < raw.size.toULong()) raw.copyOf(count.toInt()) else raw
  val h = if (count == 0L) null else headAt(logical)
  val extent = if (h != null) h.width else if (count == 0L) 0 else 1
  val pointer = readDataAddress("source", offset.toLong())
  setRegister("a0", if (count == 0L) 0x123456780001L else pointer)
  setRegister("a1", count)
  setRegister("a2", -1L)
  setRegister("a3", -1L)
  observe(listOf(pointer to extent), emptyList()) {
    callFunction("cbor_head", "function_return")
    finish(saved, if (h == null) 1 else 0, h?.width?.toLong() ?: 0L)
    expectRegister("a2", h?.kind ?: 0)
    expectRegister("a3", h?.argument?.toLong() ?: 0L)
  }
  image("source", raw, offset)
  image("record")
  image("destination")
}

fun AssemblyCase.skipCheck(
    raw: ByteArray,
    depth: Long = 0,
    count: Long = raw.size.toLong(),
    index: Int = 0,
) {
  val saved = arrange()
  val offset = index % 17
  install("source", raw, offset)
  val excessive = count.toULong() > 1024uL
  val early = excessive || depth.toULong() > 8uL || count == 0L
  val logical = if (excessive) byteArrayOf() else selected(raw, count)
  val consumed = if (early) null else skipped(logical, depth)
  val status = if (excessive) 2 else if (consumed == null) 1 else 0
  val pointer = readDataAddress("source", offset.toLong())
  setRegister("a0", if (early) 0x123456780001L else pointer)
  setRegister("a1", count)
  setRegister("a2", depth)
  observe(if (early) emptyList() else listOf(pointer to (consumed ?: logical.size)), emptyList()) {
    callFunction("cbor_skip", "function_return")
    finish(saved, status, consumed?.toLong() ?: 0L)
  }
  image("source", raw, offset)
  image("record")
  image("destination")
}

fun AssemblyCase.decodeCheck(raw: ByteArray, count: Long = raw.size.toLong(), index: Int = 0) {
  val saved = arrange()
  val offset = index % 17
  val recordOffset = index % 3 * 8
  install("source", raw, offset)
  val excessive = count.toULong() > 1024uL
  val early = excessive || count < 7L
  val decoded = if (early) null else telemetry(selected(raw, count))
  val status = if (excessive) 2 else if (decoded == null) 1 else 0
  val input = readDataAddress("source", offset.toLong())
  val output = readDataAddress("record", recordOffset.toLong())
  setRegister("a0", if (early) 0x123456780001L else input)
  setRegister("a1", count)
  setRegister("a2", if (early) 0x123456780003L else output)
  val writes =
      if (decoded == null) emptyList()
      else
          listOf(
              output to 16,
              output + 16 to decoded.device.size,
              output + 48 to decoded.readings.size * 2,
          )
  observe(if (early) emptyList() else listOf(input to count.toInt()), writes) {
    callFunction("telemetry_decode", "function_return")
    finish(saved, status, if (status == 0) count else 0L)
  }
  image("source", raw, offset)
  image("record", if (decoded == null) byteArrayOf() else flat(decoded), recordOffset)
  image("destination")
}

fun AssemblyCase.recordCheck(
    record: Telemetry,
    capacity: Long? = null,
    index: Int = 0,
    badLength: Pair<Long, Long>? = null,
) {
  val saved = arrange()
  val offset = index % 3 * 8
  val destinationOffset = index % 19
  val raw = flat(record)
  if (badLength != null) {
    little(badLength.first, 4).copyInto(raw, 8)
    little(badLength.second, 4).copyInto(raw, 12)
  }
  install("record", raw, offset)
  val accepted = badLength == null
  val encoded = if (accepted) preferred(record) else byteArrayOf()
  val enough = capacity != null && capacity.toULong() >= encoded.size.toULong() && accepted
  val pointer = readDataAddress("record", offset.toLong())
  val output = readDataAddress("destination", destinationOffset.toLong())
  setRegister("a0", pointer)
  setRegister("a1", if (enough) output else 0x123456780005L)
  setRegister("a2", capacity ?: 0)
  val reads = mutableListOf(pointer to 16)
  if (accepted) reads += pointer + 48 to 2 * record.readings.size
  if (enough) reads += pointer + 16 to record.device.size
  val status = if (!accepted) 1 else if (capacity != null && !enough) 2 else 0
  observe(reads, if (enough) listOf(output to encoded.size) else emptyList()) {
    callFunction(if (capacity == null) "telemetry_bound" else "telemetry_encode", "function_return")
    finish(saved, status, if (status == 0) encoded.size.toLong() else 0L)
  }
  image("record", raw, offset)
  image("source")
  image("destination", if (enough) encoded else byteArrayOf(), destinationOffset)
}

fun AssemblyCase.pipelineCheck(raw: ByteArray, capacity: Long = 241) {
  val saved = arrange()
  install("source", raw)
  setWord("mode", 5)
  store("byte_count", little(raw.size.toLong(), 8))
  store("byte_capacity", little(capacity, 8))
  val h = headAt(raw)
  val consumed = skipped(raw)
  val decoded = telemetry(raw)
  val encoded = decoded?.let(::preferred)
  val status =
      when {
        h == null -> 1
        raw.size > 1024 -> 2
        consumed == null || decoded == null -> 1
        capacity.toULong() < encoded!!.size.toULong() -> 2
        else -> 0
      }
  val files = provideFiles(emptyMap())
  startAt("main")
  runUntil("results_ready")
  finish(saved, status, if (status == 0) encoded!!.size.toLong() else 0L)
  image("source", raw)
  image("record", decoded?.let(::flat) ?: byteArrayOf())
  image("destination", if (status == 0) encoded!! else byteArrayOf())
  files.expectAllClosed()
  expectCondition(files.snapshot().isEmpty(), "Pipeline performs no file I/O")
  runToExit()
  expectWord("status", status)
  expectWord("count", if (status == 0) encoded!!.size else 0)
  expectWord("count", 0, 4)
}

// Sequence recognition uses the local supported-item model, not the telemetry schema.
fun sequenceCount(raw: ByteArray, depth: Long): Int? = runCatching {
  var cursor = 0
  var items = 0
  while (cursor < raw.size) {
    cursor = valueEnd(raw, cursor, depth.toInt())
    items++
  }
  require(cursor == raw.size)
  items
}
    .getOrNull()

fun AssemblyCase.sequenceCheck(
    raw: ByteArray,
    depth: Long = 0,
    count: Long = raw.size.toLong(),
    offset: Int = 0,
) {
  val saved = arrange()
  install("source", raw, offset)
  val excessive = count.toULong() > 1024uL
  val badDepth = depth.toULong() > 8uL
  val early = excessive || badDepth || count == 0L
  val logical = if (excessive) byteArrayOf() else selected(raw, count)
  val items = if (excessive || badDepth) null else sequenceCount(logical, depth)
  val status = if (excessive) 2 else if (items == null) 1 else 0
  val pointer = readDataAddress("source", offset.toLong())
  setRegister("a0", if (early) 0x123456780001L else pointer)
  setRegister("a1", count)
  setRegister("a2", depth)
  observe(if (early) emptyList() else listOf(pointer to logical.size), emptyList()) {
    callFunction("cbor_sequence_count", "function_return")
    finish(saved, status, items?.toLong() ?: 0L)
  }
  image("source", raw, offset)
  image("record")
  image("destination")
}

case("empty depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 0uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 0uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 0uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 0uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 0uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 0uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 1", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 1uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 1uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 1uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 1uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 1uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 1uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 2", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 2uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 2uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 2uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 2uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 2uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 2uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 3", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 3uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 3uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 3uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 3uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 3uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 3uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 4", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 4uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 4uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 4uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 4uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 4uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 4uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 5", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 5uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 5uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 5uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 5uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 5uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 5uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 6", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 6uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 6uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 6uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 6uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 6uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 6uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 7", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 7uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 7uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 7uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 7uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 7uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 7uL.toLong(), 6uL.toLong(), 0)
}

case("empty depth 8", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 8uL.toLong(), 0uL.toLong(), 0)
}

case("integer depth 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 8uL.toLong(), 1uL.toLong(), 0)
}

case("two integers depth 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("0020"), 8uL.toLong(), 2uL.toLong(), 0)
}

case("empty siblings depth 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("80a0"), 8uL.toLong(), 2uL.toLong(), 0)
}

case("nested siblings depth 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("818080"), 8uL.toLong(), 3uL.toLong(), 0)
}

case("separate map masks depth 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("a10000a10000"), 8uL.toLong(), 6uL.toLong(), 0)
}

case("1 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(bytes("00"), 0uL.toLong(), 1uL.toLong(), 1)
}

case("2 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(bytes("0000"), 0uL.toLong(), 2uL.toLong(), 2)
}

case("7 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(bytes("00000000000000"), 0uL.toLong(), 7uL.toLong(), 7)
}

case("23 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0000000000000000000000000000000000000000000000"),
      0uL.toLong(),
      23uL.toLong(),
      6,
  )
}

case("24 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("000000000000000000000000000000000000000000000000"),
      0uL.toLong(),
      24uL.toLong(),
      7,
  )
}

case("31 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00000000000000000000000000000000000000000000000000000000000000"),
      0uL.toLong(),
      31uL.toLong(),
      14,
  )
}

case("63 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      63uL.toLong(),
      12,
  )
}

case("64 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      64uL.toLong(),
      13,
  )
}

case("255 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      255uL.toLong(),
      0,
  )
}

case("256 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      256uL.toLong(),
      1,
  )
}

case("1023 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      1023uL.toLong(),
      3,
  )
}

case("1024 one-byte items", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      1024uL.toLong(),
      4,
  )
}

case("complete item 0 and final integer", instructionLimit = 4000000) {
  sequenceCheck(bytes("0000"), 0uL.toLong(), 2uL.toLong(), 0)
}

case("truncation item 0 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 0)
}

case("complete item 1 and final integer", instructionLimit = 4000000) {
  sequenceCheck(bytes("181800"), 0uL.toLong(), 3uL.toLong(), 1)
}

case("truncation item 1 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 1)
}

case("truncation item 1 at 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("00181b"), 0uL.toLong(), 3uL.toLong(), 1)
}

case("complete item 2 and final integer", instructionLimit = 4000000) {
  sequenceCheck(bytes("1bffffffffffffffff00"), 0uL.toLong(), 10uL.toLong(), 2)
}

case("truncation item 2 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 2)
}

case("truncation item 2 at 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b1b"), 0uL.toLong(), 3uL.toLong(), 2)
}

case("truncation item 2 at 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("001bff1b"), 0uL.toLong(), 4uL.toLong(), 2)
}

case("truncation item 2 at 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("001bffff1b"), 0uL.toLong(), 5uL.toLong(), 2)
}

case("truncation item 2 at 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("001bffffff1b"), 0uL.toLong(), 6uL.toLong(), 2)
}

case("truncation item 2 at 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("001bffffffff1b"), 0uL.toLong(), 7uL.toLong(), 2)
}

case("truncation item 2 at 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("001bffffffffff1b"), 0uL.toLong(), 8uL.toLong(), 2)
}

case("truncation item 2 at 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("001bffffffffffff1b"), 0uL.toLong(), 9uL.toLong(), 2)
}

case("truncation item 2 at 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("001bffffffffffffff1b"), 0uL.toLong(), 10uL.toLong(), 2)
}

case("complete item 3 and final integer", instructionLimit = 4000000) {
  sequenceCheck(bytes("3bffffffffffffffff00"), 0uL.toLong(), 10uL.toLong(), 3)
}

case("truncation item 3 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 3)
}

case("truncation item 3 at 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("003b1b"), 0uL.toLong(), 3uL.toLong(), 3)
}

case("truncation item 3 at 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("003bff1b"), 0uL.toLong(), 4uL.toLong(), 3)
}

case("truncation item 3 at 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("003bffff1b"), 0uL.toLong(), 5uL.toLong(), 3)
}

case("truncation item 3 at 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("003bffffff1b"), 0uL.toLong(), 6uL.toLong(), 3)
}

case("truncation item 3 at 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("003bffffffff1b"), 0uL.toLong(), 7uL.toLong(), 3)
}

case("truncation item 3 at 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("003bffffffffff1b"), 0uL.toLong(), 8uL.toLong(), 3)
}

case("truncation item 3 at 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("003bffffffffffff1b"), 0uL.toLong(), 9uL.toLong(), 3)
}

case("truncation item 3 at 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("003bffffffffffffff1b"), 0uL.toLong(), 10uL.toLong(), 3)
}

case("complete item 4 and final integer", instructionLimit = 4000000) {
  sequenceCheck(bytes("4200ff00"), 0uL.toLong(), 4uL.toLong(), 4)
}

case("truncation item 4 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 4)
}

case("truncation item 4 at 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("00421b"), 0uL.toLong(), 3uL.toLong(), 4)
}

case("truncation item 4 at 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("0042001b"), 0uL.toLong(), 4uL.toLong(), 4)
}

case("complete item 5 and final integer", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "98400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      67uL.toLong(),
      5,
  )
}

case("truncation item 5 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 5)
}

case("truncation item 5 at 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("00981b"), 0uL.toLong(), 3uL.toLong(), 5)
}

case("truncation item 5 at 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("0098401b"), 0uL.toLong(), 4uL.toLong(), 5)
}

case("truncation item 5 at 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("009840001b"), 0uL.toLong(), 5uL.toLong(), 5)
}

case("truncation item 5 at 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("00984000001b"), 0uL.toLong(), 6uL.toLong(), 5)
}

case("truncation item 5 at 5", instructionLimit = 4000000) {
  sequenceCheck(bytes("0098400000001b"), 0uL.toLong(), 7uL.toLong(), 5)
}

case("truncation item 5 at 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("009840000000001b"), 0uL.toLong(), 8uL.toLong(), 5)
}

case("truncation item 5 at 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("00984000000000001b"), 0uL.toLong(), 9uL.toLong(), 5)
}

case("truncation item 5 at 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("0098400000000000001b"), 0uL.toLong(), 10uL.toLong(), 5)
}

case("truncation item 5 at 9", instructionLimit = 4000000) {
  sequenceCheck(bytes("009840000000000000001b"), 0uL.toLong(), 11uL.toLong(), 5)
}

case("truncation item 5 at 10", instructionLimit = 4000000) {
  sequenceCheck(bytes("00984000000000000000001b"), 0uL.toLong(), 12uL.toLong(), 5)
}

case("truncation item 5 at 11", instructionLimit = 4000000) {
  sequenceCheck(bytes("0098400000000000000000001b"), 0uL.toLong(), 13uL.toLong(), 5)
}

case("truncation item 5 at 12", instructionLimit = 4000000) {
  sequenceCheck(bytes("009840000000000000000000001b"), 0uL.toLong(), 14uL.toLong(), 5)
}

case("truncation item 5 at 13", instructionLimit = 4000000) {
  sequenceCheck(bytes("00984000000000000000000000001b"), 0uL.toLong(), 15uL.toLong(), 5)
}

case("truncation item 5 at 14", instructionLimit = 4000000) {
  sequenceCheck(bytes("0098400000000000000000000000001b"), 0uL.toLong(), 16uL.toLong(), 5)
}

case("truncation item 5 at 15", instructionLimit = 4000000) {
  sequenceCheck(bytes("009840000000000000000000000000001b"), 0uL.toLong(), 17uL.toLong(), 5)
}

case("truncation item 5 at 16", instructionLimit = 4000000) {
  sequenceCheck(bytes("00984000000000000000000000000000001b"), 0uL.toLong(), 18uL.toLong(), 5)
}

case("truncation item 5 at 17", instructionLimit = 4000000) {
  sequenceCheck(bytes("0098400000000000000000000000000000001b"), 0uL.toLong(), 19uL.toLong(), 5)
}

case("truncation item 5 at 18", instructionLimit = 4000000) {
  sequenceCheck(bytes("009840000000000000000000000000000000001b"), 0uL.toLong(), 20uL.toLong(), 5)
}

case("truncation item 5 at 19", instructionLimit = 4000000) {
  sequenceCheck(bytes("00984000000000000000000000000000000000001b"), 0uL.toLong(), 21uL.toLong(), 5)
}

case("truncation item 5 at 20", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0098400000000000000000000000000000000000001b"),
      0uL.toLong(),
      22uL.toLong(),
      5,
  )
}

case("truncation item 5 at 21", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("009840000000000000000000000000000000000000001b"),
      0uL.toLong(),
      23uL.toLong(),
      5,
  )
}

case("truncation item 5 at 22", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00984000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      24uL.toLong(),
      5,
  )
}

case("truncation item 5 at 23", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0098400000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      25uL.toLong(),
      5,
  )
}

case("truncation item 5 at 24", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("009840000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      26uL.toLong(),
      5,
  )
}

case("truncation item 5 at 25", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00984000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      27uL.toLong(),
      5,
  )
}

case("truncation item 5 at 26", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0098400000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      28uL.toLong(),
      5,
  )
}

case("truncation item 5 at 27", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("009840000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      29uL.toLong(),
      5,
  )
}

case("truncation item 5 at 28", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00984000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      30uL.toLong(),
      5,
  )
}

case("truncation item 5 at 29", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0098400000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      31uL.toLong(),
      5,
  )
}

case("truncation item 5 at 30", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("009840000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      32uL.toLong(),
      5,
  )
}

case("truncation item 5 at 31", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00984000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      33uL.toLong(),
      5,
  )
}

case("truncation item 5 at 32", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0098400000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      34uL.toLong(),
      5,
  )
}

case("truncation item 5 at 33", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("009840000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      35uL.toLong(),
      5,
  )
}

case("truncation item 5 at 34", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00984000000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      36uL.toLong(),
      5,
  )
}

case("truncation item 5 at 35", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0098400000000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      37uL.toLong(),
      5,
  )
}

case("truncation item 5 at 36", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("009840000000000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      38uL.toLong(),
      5,
  )
}

case("truncation item 5 at 37", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00984000000000000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      39uL.toLong(),
      5,
  )
}

case("truncation item 5 at 38", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("0098400000000000000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      40uL.toLong(),
      5,
  )
}

case("truncation item 5 at 39", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("009840000000000000000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      41uL.toLong(),
      5,
  )
}

case("truncation item 5 at 40", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("00984000000000000000000000000000000000000000000000000000000000000000000000000000001b"),
      0uL.toLong(),
      42uL.toLong(),
      5,
  )
}

case("truncation item 5 at 41", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      43uL.toLong(),
      5,
  )
}

case("truncation item 5 at 42", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      44uL.toLong(),
      5,
  )
}

case("truncation item 5 at 43", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      45uL.toLong(),
      5,
  )
}

case("truncation item 5 at 44", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      46uL.toLong(),
      5,
  )
}

case("truncation item 5 at 45", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      47uL.toLong(),
      5,
  )
}

case("truncation item 5 at 46", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      48uL.toLong(),
      5,
  )
}

case("truncation item 5 at 47", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      49uL.toLong(),
      5,
  )
}

case("truncation item 5 at 48", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      50uL.toLong(),
      5,
  )
}

case("truncation item 5 at 49", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      51uL.toLong(),
      5,
  )
}

case("truncation item 5 at 50", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      52uL.toLong(),
      5,
  )
}

case("truncation item 5 at 51", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      53uL.toLong(),
      5,
  )
}

case("truncation item 5 at 52", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      54uL.toLong(),
      5,
  )
}

case("truncation item 5 at 53", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      55uL.toLong(),
      5,
  )
}

case("truncation item 5 at 54", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      56uL.toLong(),
      5,
  )
}

case("truncation item 5 at 55", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      57uL.toLong(),
      5,
  )
}

case("truncation item 5 at 56", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      58uL.toLong(),
      5,
  )
}

case("truncation item 5 at 57", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      59uL.toLong(),
      5,
  )
}

case("truncation item 5 at 58", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      60uL.toLong(),
      5,
  )
}

case("truncation item 5 at 59", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      61uL.toLong(),
      5,
  )
}

case("truncation item 5 at 60", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      62uL.toLong(),
      5,
  )
}

case("truncation item 5 at 61", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      63uL.toLong(),
      5,
  )
}

case("truncation item 5 at 62", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      64uL.toLong(),
      5,
  )
}

case("truncation item 5 at 63", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "009840000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      65uL.toLong(),
      5,
  )
}

case("truncation item 5 at 64", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "00984000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      66uL.toLong(),
      5,
  )
}

case("truncation item 5 at 65", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001b",
      ),
      0uL.toLong(),
      67uL.toLong(),
      5,
  )
}

case("complete item 6 and final integer", instructionLimit = 4000000) {
  sequenceCheck(bytes("a2008001a000"), 0uL.toLong(), 6uL.toLong(), 6)
}

case("truncation item 6 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 6)
}

case("truncation item 6 at 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("00a21b"), 0uL.toLong(), 3uL.toLong(), 6)
}

case("truncation item 6 at 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("00a2001b"), 0uL.toLong(), 4uL.toLong(), 6)
}

case("truncation item 6 at 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("00a200801b"), 0uL.toLong(), 5uL.toLong(), 6)
}

case("truncation item 6 at 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("00a20080011b"), 0uL.toLong(), 6uL.toLong(), 6)
}

case("complete item 7 and final integer", instructionLimit = 4000000) {
  sequenceCheck(bytes("8181a1000000"), 0uL.toLong(), 6uL.toLong(), 7)
}

case("truncation item 7 at 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("001b"), 0uL.toLong(), 2uL.toLong(), 7)
}

case("truncation item 7 at 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("00811b"), 0uL.toLong(), 3uL.toLong(), 7)
}

case("truncation item 7 at 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("0081811b"), 0uL.toLong(), 4uL.toLong(), 7)
}

case("truncation item 7 at 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("008181a11b"), 0uL.toLong(), 5uL.toLong(), 7)
}

case("truncation item 7 at 4", instructionLimit = 4000000) {
  sequenceCheck(bytes("008181a1001b"), 0uL.toLong(), 6uL.toLong(), 7)
}

case("invalid suffix 0", instructionLimit = 4000000) {
  sequenceCheck(bytes("00ff"), 0uL.toLong(), 2uL.toLong(), 0)
}

case("invalid suffix 1", instructionLimit = 4000000) {
  sequenceCheck(bytes("009fff"), 0uL.toLong(), 3uL.toLong(), 1)
}

case("invalid suffix 2", instructionLimit = 4000000) {
  sequenceCheck(bytes("00a20000180000"), 0uL.toLong(), 7uL.toLong(), 2)
}

case("invalid suffix 3", instructionLimit = 4000000) {
  sequenceCheck(bytes("00a10800"), 0uL.toLong(), 4uL.toLong(), 3)
}

case("invalid suffix 4", instructionLimit = 4000000) {
  sequenceCheck(
      bytes("005821000000000000000000000000000000000000000000000000000000000000000000"),
      0uL.toLong(),
      36uL.toLong(),
      4,
  )
}

case("invalid suffix 5", instructionLimit = 4000000) {
  sequenceCheck(
      bytes(
          "0098410000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      ),
      0uL.toLong(),
      68uL.toLong(),
      5,
  )
}

case("invalid suffix 6", instructionLimit = 4000000) {
  sequenceCheck(bytes("00f4"), 0uL.toLong(), 2uL.toLong(), 6)
}

case("invalid suffix 7", instructionLimit = 4000000) {
  sequenceCheck(bytes("00d90000"), 0uL.toLong(), 4uL.toLong(), 7)
}

case("invalid suffix 8", instructionLimit = 4000000) {
  sequenceCheck(bytes("006000"), 0uL.toLong(), 3uL.toLong(), 8)
}

case("invalid suffix 9", instructionLimit = 4000000) {
  sequenceCheck(bytes("001c"), 0uL.toLong(), 2uL.toLong(), 9)
}

case("invalid suffix 10", instructionLimit = 4000000) {
  sequenceCheck(bytes("0081"), 0uL.toLong(), 2uL.toLong(), 10)
}

case("invalid suffix 11", instructionLimit = 4000000) {
  sequenceCheck(bytes("00a100"), 0uL.toLong(), 3uL.toLong(), 11)
}

case("early count 1025 depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 0uL.toLong(), 1025uL.toLong(), 0)
}

case("early count 1025 depth 9", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9uL.toLong(), 1025uL.toLong(), 0)
}

case("early count 4294967296 depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 0uL.toLong(), 4294967296uL.toLong(), 0)
}

case("early count 4294967296 depth 9", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9uL.toLong(), 4294967296uL.toLong(), 0)
}

case("early count 9223372036854775808 depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 0uL.toLong(), 9223372036854775808uL.toLong(), 0)
}

case("early count 9223372036854775808 depth 9", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9uL.toLong(), 9223372036854775808uL.toLong(), 0)
}

case("early count 18446744073709551615 depth 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 0uL.toLong(), 18446744073709551615uL.toLong(), 0)
}

case("early count 18446744073709551615 depth 9", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9uL.toLong(), 18446744073709551615uL.toLong(), 0)
}

case("early depth 9 count 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9uL.toLong(), 0uL.toLong(), 0)
}

case("early depth 9 count 1", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9uL.toLong(), 1uL.toLong(), 0)
}

case("early depth 9223372036854775808 count 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9223372036854775808uL.toLong(), 0uL.toLong(), 0)
}

case("early depth 9223372036854775808 count 1", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 9223372036854775808uL.toLong(), 1uL.toLong(), 0)
}

case("early depth 18446744073709551615 count 0", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 18446744073709551615uL.toLong(), 0uL.toLong(), 0)
}

case("early depth 18446744073709551615 count 1", instructionLimit = 4000000) {
  sequenceCheck(bytes(""), 18446744073709551615uL.toLong(), 1uL.toLong(), 0)
}

case("selected prefix excludes unsupported suffix", instructionLimit = 4000000) {
  sequenceCheck(bytes("001818ff"), 0uL.toLong(), 3uL.toLong(), 0)
}

case("selected zero excludes real input", instructionLimit = 4000000) {
  sequenceCheck(bytes("ff"), 0uL.toLong(), 0uL.toLong(), 0)
}

case("sibling keeps inherited depth after helper overwrites a2", instructionLimit = 4000000) {
  sequenceCheck(bytes("808180"), depth = 7)
}
