import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.TimingCheck.Event
import edu.missouristate.mars.cli.grading.TimingCheck.Operation
import java.util.zip.CRC32

fun octets(s: String) = s.chunked(2).map { it.toInt(16) }.toIntArray()

fun words(s: String) = s.chunked(8).map { it.toLong(16) }

fun field(b: IntArray, at: Int, v: Long, little: Boolean) {
  for (j in 0..7) b[at + j] = ((v ushr (8 * (if (little) j else 7 - j))) and 255).toInt()
}

fun packed(values: List<Long>, little: Boolean) =
    IntArray(4 * values.size) { i ->
      ((values[i / 4] ushr (8 * (if (little) i % 4 else 3 - i % 4))) and 255).toInt()
    }

fun unsigned(b: IntArray, at: Int, n: Int): Long {
  var v = 0L
  for (i in 0 until n) v = (v shl 8) or b[at + i].toLong()
  return v
}

fun AssemblyCase.put(name: String, b: IntArray, little: Boolean) {
  expectCondition(b.size % 4 == 0, "Whole test words")
  for (i in b.indices step 4) {
    var v = 0
    for (j in 0..3) v = v or (b[i + j] shl (8 * (if (little) j else 3 - j)))
    setWord("videoCheck_" + name, v, i)
  }
}

fun AssemblyCase.seed(
    name: String,
    data: IntArray,
    offset: Int,
    little: Boolean,
    extent: Int = data.size + offset + 64,
): IntArray {
  val b = IntArray((extent + 3) / 4 * 4) { 165 }
  data.copyInto(b, offset)
  put(name, b, little)
  return b
}

fun AssemblyCase.context(values: List<Long>, little: Boolean): IntArray {
  val b = IntArray(160) { 165 }
  for ((i, v) in values.withIndex()) field(b, i * 8, v, little)
  put("context", b, little)
  return b
}

fun AssemblyCase.address(name: String, offset: Long = 0) =
    readDataAddress("videoCheck_" + name, offset)

fun AssemblyCase.bytes(name: String, n: Int) = readBytes("videoCheck_" + name, n)

fun AssemblyCase.arguments(a: Long, b: Long, c: Long, d: Long) {
  setRegister("a0", a)
  setRegister("a1", b)
  setRegister("a2", c)
  setRegister("a3", d)
  setRegister("sp", address("stack", 32768))
  for (i in 0..7) setRegister("s$i", 0x1234567800000000L + i * 101)
  setRegister("fp", 0x2345678900000000L)
}

fun AssemblyCase.returned(status: Int, count: Long) {
  expectRegister("v0", status)
  expectRegister("v1", count)
  expectRegister("sp", address("stack", 32768))
  for (i in 0..7) expectRegister("s$i", 0x1234567800000000L + i * 101)
  expectRegister("fp", 0x2345678900000000L)
}

fun AssemblyCase.call(entry: String) {
  callFunction(entry, "videoCheck_return")
}

fun AssemblyCase.same(name: String, wanted: IntArray, why: String) {
  expectCondition(bytes(name, wanted.size).contentEquals(wanted), why)
}

fun AssemblyCase.record(
    raw: IntArray,
    previous: List<Long>,
    current: List<Long>,
    sequence: Int,
    period: Int,
) {
  expectCondition(raw.size in 20..28688, "Complete record size")
  expectCondition(raw[0] in 0..1 && raw.sliceArray(1..3).all { it == 0 }, "Kind and reserved bytes")
  expectCondition(
      unsigned(raw, 4, 4) == sequence.toLong() && unsigned(raw, 8, 4) == raw.size - 16L,
      "Sequence and exact payload",
  )
  if (sequence % period == 0) expectCondition(raw[0] == 0, "Required full frame")
  val crc = CRC32()
  crc.update(raw.take(12).map { it.toByte() }.toByteArray())
  crc.update(raw.drop(16).map { it.toByte() }.toByteArray())
  expectCondition(crc.value == unsigned(raw, 12, 4), "Host CRC over both record regions")
  val result = previous.toMutableList()
  if (raw[0] == 0) {
    expectCondition(raw.size == 12304, "Full RGB payload")
    for (i in 0 until 4096) result[i] = unsigned(raw, 16 + 3 * i, 3)
  } else {
    var at = 16
    var pixel = 0
    while (pixel < 4096) {
      expectCondition(raw.size - at >= 4, "Complete command pair")
      val skip = unsigned(raw, at, 2).toInt()
      val changed = unsigned(raw, at + 2, 2).toInt()
      at += 4
      expectCondition(skip + changed > 0 && skip <= 4096 - pixel, "Progress and skip window")
      pixel += skip
      expectCondition(
          changed <= 4096 - pixel && changed <= (raw.size - at) / 3,
          "Changed pixel/input windows",
      )
      repeat(changed) {
        result[pixel] = result[pixel] xor unsigned(raw, at, 3)
        pixel++
        at += 3
      }
    }
    expectCondition(at == raw.size, "No trailing command bytes")
  }
  expectCondition(result == current, "All reconstructed numeric colors")
}

fun AssemblyCase.file(raw: IntArray, frames: List<List<Long>>, period: Int, duration: Int) {
  expectCondition(raw.size in 12328..7344152, "Bounded complete file")
  expectCondition(
      unsigned(raw, 0, 4) == 0x4d4c5631L &&
          unsigned(raw, 4, 2) == 64L &&
          unsigned(raw, 6, 2) == 64L,
      "Magic and geometry",
  )
  expectCondition(
      unsigned(raw, 8, 4) == frames.size.toLong() &&
          unsigned(raw, 12, 2) == period.toLong() &&
          unsigned(raw, 14, 2) == 0L &&
          unsigned(raw, 16, 4) == duration.toLong() &&
          unsigned(raw, 20, 4) == 0L,
      "Global fields",
  )
  var at = 24
  var previous = List(4096) { 0L }
  for (i in frames.indices) {
    expectCondition(raw.size - at >= 16, "Complete record envelope")
    val n = unsigned(raw, at + 8, 4)
    expectCondition(n <= 28672 && n <= raw.size - at - 16L, "Bounded record payload")
    val end = at + 16 + n.toInt()
    record(raw.sliceArray(at until end), previous, frames[i], i, period)
    previous = frames[i]
    at = end
  }
  expectCondition(at == raw.size, "Exact file end")
}

fun AssemblyCase.kernel(
    left: List<Long>,
    right: List<Long>,
    alias: Int,
    msa: Boolean,
    little: Boolean,
) {
  val a = seed("a", packed(left, little), 4, little, 16448)
  val b = seed("b", packed(right, little), 4, little, 16448)
  val out = seed("out", intArrayOf(), 4, little, 16448)
  val dest =
      if (alias == 1) address("a", 4) else if (alias == 2) address("b", 4) else address("out", 4)
  arguments(address("a", 4), address("b", 4), dest, left.size.toLong())
  call(if (msa) "video_xor_words_msa" else "video_xor_words")
  val values = left.zip(right).map { it.first xor it.second }
  returned(0, values.count { it != 0L }.toLong())
  packed(values, little).copyInto(if (alias == 1) a else if (alias == 2) b else out, 4)
  same("a", a, "Left input/alias and guards")
  same("b", b, "Right input/alias and guards")
  same("out", out, "Exact XOR words and guards")
}

fun AssemblyCase.kernelAdmission(count: Long, msa: Boolean, status: Int) {
  seed("out", intArrayOf(), 4, true, 16448)
  arguments(0x123456780001L, 0x123456790001L, 0x1234567a0001L, count)
  call(if (msa) "video_xor_words_msa" else "video_xor_words")
  returned(status, 0)
}

fun AssemblyCase.apply(
    raw: IntArray,
    previous: List<Long>,
    current: List<Long>,
    sequence: Int,
    status: Int,
    little: Boolean,
) {
  val input = seed("a", raw, 3, little)
  val image = seed("out", packed(previous, little), 4, little, 16448)
  val ctx = context(listOf(16384, sequence.toLong(), 4), little)
  arguments(address("a", 3), raw.size.toLong(), address("out", 4), address("context"))
  val writes = traceMemoryStores("videoCheck_out", 16448)
  val states = traceMemoryStores("videoCheck_context", 160)
  call("video_apply_record")
  writes.close()
  states.close()
  returned(status, if (status == 0) 16384 else 0)
  if (status == 0) {
    packed(current, little).copyInto(image, 4)
    field(ctx, 8, sequence + 1L, little)
    field(ctx, 24, raw[0].toLong(), little)
    field(ctx, 32, 16384, little)
  } else {
    writes.expectNone()
    states.expectNone()
  }
  same("a", input, "Immutable record and guards")
  same("out", image, "Accepted or retained picture")
  same("context", ctx, "Exact record publication")
}

fun AssemblyCase.index(
    raw: IntArray,
    offsets: List<Long>,
    count: Int,
    status: Int,
    little: Boolean,
    capacity: Long = 2048,
) {
  val input = seed("a", raw, 3, little)
  val dir = seed("directory", intArrayOf(), 8, little, 2176)
  val work = seed("scratch", intArrayOf(), 4, little, 16448)
  val ctx = context(listOf(capacity, address("scratch", 4), 16384), little)
  arguments(address("a", 3), raw.size.toLong(), address("directory", 8), address("context"))
  val ds = traceMemoryStores("videoCheck_directory", 2176)
  val cs = traceMemoryStores("videoCheck_context", 160)
  call("video_index")
  ds.close()
  cs.close()
  returned(status, if (status == 0) 8L * count else 0)
  if (status == 0) {
    for ((i, v) in offsets.withIndex()) field(dir, 8 + 8 * i, v, little)
    for ((i, v) in
        listOf(count.toLong(), 4L, 40L, raw.size.toLong(), count.toLong()).withIndex()) field(
        ctx,
        24 + 8 * i,
        v,
        little,
    )
  } else {
    ds.expectNone()
    cs.expectNone()
  }
  same("a", input, "Immutable whole file")
  same("directory", dir, "Exact offsets or retained directory")
  same("context", ctx, "Final index metadata")
  expectCondition(
      bytes("scratch", 4).contentEquals(work.take(4).toIntArray()) &&
          readBytes(address("scratch", 16388), 60).contentEquals(work.takeLast(60).toIntArray()),
      "Scratch guards",
  )
}

fun AssemblyCase.load(
    raw: IntArray,
    capacity: Long,
    status: Int,
    little: Boolean,
    fail: Int? = null,
    missing: Boolean = false,
) {
  val files =
      provideFiles(
          if (missing) emptyMap()
          else mapOf("animation.mlv" to raw.map { it.toByte() }.toByteArray()),
          maxReadSize = 7,
          failAfterBytes = fail,
          firstDescriptor = 19,
      )
  val output = seed("out", intArrayOf(), 3, little, 65600)
  val ctx = context(emptyList(), little)
  arguments(address("path"), address("out", 3), capacity, address("context"))
  call("video_load")
  returned(status, if (status == 0) raw.size.toLong() else 0)
  files.expectAllClosed()
  val calls = files.snapshot()
  val read = calls.filter { it.operation.name == "READ" }
  expectCondition(
      read.all { it.descriptor == 19 && it.requestedBytes in 1..4096 },
      "Bounded reads on owned descriptor",
  )
  if (capacity == 0L) expectCondition(calls.isEmpty(), "Reject before opening")
  if (status == 0) {
    raw.copyInto(output, 3)
    same("out", output, "Complete load and unused guards")
    field(ctx, 0, raw.size.toLong(), little)
  } else
      expectCondition(
          bytes("out", 3).all { it == 165 } &&
              readBytes(address("out", 3 + capacity.coerceIn(0, 65536)), 61).all { it == 165 },
          "Failure cannot store outside supplied staging",
      )
  same("context", ctx, "Success-only loaded count")
}

fun AssemblyCase.seek(
    raw: IntArray,
    offsets: List<Long>,
    frames: List<List<Long>>,
    target: Long,
    status: Int,
    little: Boolean,
) {
  val input = seed("a", raw, 3, little)
  val dir = seed("directory", intArrayOf(), 8, little, 2176)
  for ((i, v) in offsets.withIndex()) field(dir, 8 + 8 * i, v, little)
  put("directory", dir, little)
  val image = seed("out", intArrayOf(), 4, little, 16448)
  seed("scratch", intArrayOf(), 4, little, 16448)
  val ctx =
      context(
          listOf(
              raw.size.toLong(),
              address("directory", 8),
              frames.size.toLong(),
              4,
              16384,
              address("scratch", 4),
              16384,
          ),
          little,
      )
  arguments(address("a", 3), target, address("out", 4), address("context"))
  val writes = traceMemoryStores("videoCheck_out", 16448)
  call("video_seek")
  writes.close()
  returned(status, if (status == 0) 16384 else 0)
  if (status == 0) {
    val n = target.toInt()
    packed(frames[n], little).copyInto(image, 4)
    val start = (0..n).last { raw[offsets[it].toInt()] == 0 }
    field(ctx, 56, target, little)
    field(ctx, 64, start.toLong(), little)
    field(ctx, 72, target - start + 1, little)
  } else writes.expectNone()
  same("a", input, "Immutable seek file")
  same("directory", dir, "Immutable seek offsets")
  same("out", image, "Requested complete image")
  same("context", ctx, "Exact seek publication")
}

fun AssemblyCase.encode(
    current: List<Long>,
    previous: List<Long>,
    sequence: Long,
    full: Boolean,
    status: Int,
    little: Boolean,
    count: Long = 16384,
    capacity: Long = 28688,
) {
  val input = seed("a", packed(current, little), 4, little, 16448)
  val old = seed("previous", packed(previous, little), 4, little, 16448)
  val output = seed("out", intArrayOf(), 3, little, 28752)
  val scratch = seed("scratch", intArrayOf(), 4, little, 16448)
  val ctx =
      context(
          if (full) listOf(capacity, sequence, 4, address("previous", 4), 16384)
          else
              listOf(
                  capacity,
                  sequence,
                  4,
                  address("previous", 4),
                  16384,
                  0,
                  0,
                  0,
                  address("scratch", 4),
                  16384,
              ),
          little,
      )
  arguments(address("a", 4), count, address("out", 3), address("context"))
  val os = traceMemoryStores("videoCheck_out", 28752)
  val ps = traceMemoryStores("videoCheck_previous", 16448)
  val cs = traceMemoryStores("videoCheck_context", 160)
  call(if (full) "video_encode_full" else "video_encode_record")
  os.close()
  ps.close()
  cs.close()
  val actual = readRegister("v0").toInt()
  expectCondition(actual in 0..1, "Completed encoding status")
  if (status >= 0) expectRegister("v0", status)
  if (actual == 0) {
    val n = readRegister("v1")
    expectCondition(n in 20..minOf(28688L, capacity), "Bounded selected record")
    val raw = readBytes(address("out", 3), n.toInt())
    record(raw, previous, current, sequence.toInt(), 4)
    if (full) expectCondition(n == 12304L && raw[0] == 0, "Full-only interface")
    raw.copyInto(output, 3)
    packed(current, little).copyInto(old, 4)
    field(ctx, 8, sequence + 1, little)
    field(ctx, 40, raw[0].toLong(), little)
    field(ctx, 48, n, little)
    returned(0, n)
  } else {
    returned(1, 0)
    os.expectNone()
    ps.expectNone()
    cs.expectNone()
  }
  same("a", input, "Immutable packed source")
  same("out", output, "Exact produced record and retained tail")
  same("previous", old, "Success-only prediction update")
  same("context", ctx, "Success-only record fields")
  expectCondition(
      bytes("scratch", 4).contentEquals(scratch.take(4).toIntArray()) &&
          readBytes(address("scratch", 16388), 60).all { it == 165 },
      "XOR work guards",
  )
}

fun AssemblyCase.encodeFile(
    frames: List<List<Long>>,
    status: Int,
    little: Boolean,
    count: Long = frames.size * 16384L,
) {
  val input = seed("a", packed(frames.flatten(), little), 4, little)
  val output = seed("out", intArrayOf(), 3, little, 262208)
  val ctx =
      context(
          listOf(
              262144,
              frames.size.toLong(),
              4,
              40,
              address("previous", 4),
              16384,
              address("scratch", 4),
              16384,
          ),
          little,
      )
  seed("previous", intArrayOf(), 4, little, 16448)
  seed("scratch", intArrayOf(), 4, little, 16448)
  arguments(address("a", 4), count, address("out", 3), address("context"))
  val cs = traceMemoryStores("videoCheck_context", 160)
  val writes = traceMemoryAccesses("videoCheck_out", 262208, maxAccesses = 2000000)
  call("video_encode_file")
  cs.close()
  writes.close()
  expectRegister("v0", status)
  val n = if (status == 0) readRegister("v1") else 0L
  returned(status, n)
  if (status == 0) {
    expectCondition(n in 12328L..262144L, "Complete file extent")
    val raw = readBytes(address("out", 3), n.toInt())
    file(raw, frames, 4, 40)
    raw.copyInto(output, 3)
    same("out", output, "Produced extent and retained tail")
    field(ctx, 64, n, little)
    field(ctx, 72, frames.size.toLong(), little)
  } else cs.expectNone()
  for (a in writes.snapshot()) expectCondition(
      a.address >= address("out", 3) && a.address + a.length <= address("out", 262147),
      "Staging capacity",
  )
  same("a", input, "Immutable complete input")
  same("context", ctx, "Final file counts only on success")
}

fun AssemblyCase.store(
    raw: IntArray,
    status: Int,
    little: Boolean,
    fail: Int? = null,
    allowed: Boolean = true,
    count: Long = raw.size.toLong(),
) {
  val before = seed("a", raw, 3, little)
  val ctx = context(emptyList(), little)
  val path = bytes("path", 128)
  val files =
      provideFiles(
          mapOf("animation.mlv" to octets("6f6c64").map { it.toByte() }.toByteArray()),
          writablePaths = if (allowed) setOf("animation.mlv") else emptySet(),
          maxWriteSize = 7,
          failWriteAfterBytes = fail,
          firstDescriptor = 19,
          maxFileSize = 262144,
      )
  arguments(address("path"), address("a", 3), count, address("context"))
  call("video_store")
  returned(status, if (status == 0) raw.size.toLong() else 0)
  files.expectAllClosed()
  val early = count !in 1L..7344152L
  val expected =
      if (early || !allowed) octets("6f6c64")
      else raw.take(if (fail == null) raw.size else minOf(raw.size, fail)).toIntArray()
  files.expectFile("animation.mlv", expected.map { it.toByte() }.toByteArray())
  if (status == 0) field(ctx, 0, raw.size.toLong(), little)
  same("a", before, "Immutable transfer bytes and lookahead guard")
  same("context", ctx, "Only final stored count changes")
  same("path", path, "Immutable path")
  expectCondition(
      files
          .snapshot()
          .filter { it.operation.name == "WRITE" }
          .all { it.descriptor == 19 && it.requestedBytes in 1..4096 },
      "Owned bounded writes",
  )
  if (early) expectCondition(files.snapshot().isEmpty(), "Invalid transfer count before opening")
}

fun AssemblyCase.play(
    raw: IntArray,
    frames: List<List<Long>>,
    start: Long,
    times: List<Long>,
    events: List<Event>,
    pictures: List<Int>,
    status: Int,
    presented: Int,
    late: Int,
    maximum: Long,
    logical: Long,
    little: Boolean,
) {
  val input = seed("a", raw, 3, little)
  val image = seed("out", intArrayOf(), 4, little, 16448)
  seed("scratch", intArrayOf(), 4, little, 16448)
  seed("directory", IntArray(64) { 255 }, 8, little, 2176)
  val ctx =
      context(
          listOf(16384, address("directory", 8), 2048, address("scratch", 4), 16384, start),
          little,
      )
  val clock = provideTiming(times)
  arguments(address("a", 3), raw.size.toLong(), address("out", 4), address("context"))
  enterFunction("video_play", "videoCheck_return")
  for ((i, e) in events.withIndex()) {
    runUntilSyscall(if (e.operation == Operation.TIME) 30 else 32)
    val picture = pictures[i]
    val wanted =
        if (picture < 0) image.sliceArray(4 until 16388) else packed(frames[picture], little)
    expectCondition(
        readBytes(address("out", 4), 16384).contentEquals(wanted),
        "Every accepted frame at public service boundary",
    )
    same("context", ctx, "No premature final counters")
    if (e.operation == Operation.SLEEP)
        expectCondition(
            readRegister("a0").toInt().toLong() == e.milliseconds,
            "Remaining 32-bit sleep argument",
        )
    step()
  }
  runUntil("videoCheck_return")
  returned(status, presented.toLong())
  expectCondition(clock.snapshot() == events, "Exact clock/wait protocol")
  if (presented > 0) packed(frames[start.toInt() + presented - 1], little).copyInto(image, 4)
  if (status == 0) {
    field(ctx, 48, presented.toLong(), little)
    field(ctx, 56, late.toLong(), little)
    field(ctx, 64, maximum, little)
    field(ctx, 72, logical, little)
  }
  same("a", input, "Immutable playback file")
  same("out", image, "Last accepted image and guards")
  same("context", ctx, "Success-only final playback result")
}

fun AssemblyCase.kernelPersistent(msa: Boolean, little: Boolean) {
  val left = listOf(1L, 0x80000000L, 0L, 0xffffffL, 5L)
  val right = listOf(7L, 0L, 0L, 0xffL, 5L)
  val a = seed("a", packed(left, little), 4, little, 16448)
  val b = seed("b", packed(right, little), 4, little, 16448)
  val entry = if (msa) "video_xor_words_msa" else "video_xor_words"
  for (values in listOf(left.zip(right).map { it.first xor it.second }, left)) {
    arguments(address("a", 4), address("b", 4), address("a", 4), 5)
    call(entry)
    returned(
        0,
        values.indices
            .count {
              (if (values === left) left.zip(right).map { it.first xor it.second } else left)[it] !=
                  right[it]
            }
            .toLong(),
    )
    packed(values, little).copyInto(a, 4)
    same("a", a, "Actual involution state")
    same("b", b, "Immutable second operand")
  }
  arguments(0x123456780001, 0x123456790001, 0x1234567a0001, 4097)
  call(entry)
  returned(1, 0)
  same("a", a, "Actual earlier output retained after rejection")
}
