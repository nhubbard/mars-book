import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation

// Host-side description of the published local subset, not an assembly solution.
data class Raster(
    val width: Int,
    val height: Int,
    val channels: Int,
    val colorspace: Int,
    val rgba: ByteArray,
) {
  val pixels
    get() = width * height

  val dimensions
    get() = (height.toLong() shl 32) or width.toLong()

  val descriptor
    get() =
        listOf(width, height, channels, colorspace)
            .flatMap { n ->
              (0..3).map { (n ushr (it * 8)).toByte() }
            }
            .toByteArray()
}

fun unsigned(byte: Byte) = byte.toInt() and 255

fun hex(text: String) = text.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

val endMarker = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1)

fun inspect(data: ByteArray): Raster? = runCatching {
  require(data.size in 23..20502 && data.take(4).toByteArray().contentEquals("qoif".toByteArray()))
  fun dimension(at: Int) = (0..3).fold(0L) { n, i -> n * 256 + unsigned(data[at + i]) }
  val width = dimension(4)
  val height = dimension(8)
  val channels = unsigned(data[12])
  val colorspace = unsigned(data[13])
  require(width in 1..64 && height in 1..64 && channels in 3..4 && colorspace in 0..1)
  val pixels = (width * height).toInt()
  val output = ByteArray(pixels * 4)
  val cache = Array(64) { IntArray(4) }
  var previous = intArrayOf(0, 0, 0, 255)
  var cursor = 14
  var produced = 0
  var previousIndex = -1
  val payloadEnd = data.size - 8
  fun next(): Int {
    require(cursor < payloadEnd)
    return unsigned(data[cursor++])
  }
  while (produced < pixels) {
    val tag = next()
    var repetitions = 1
    var index = -1
    val current = previous.copyOf()
    when {
      tag == 254 -> repeat(3) { current[it] = next() }
      tag == 255 -> repeat(4) { current[it] = next() }
      tag < 64 -> {
        index = tag
        require(index != previousIndex)
        cache[index].copyInto(current)
      }
      tag < 128 ->
          repeat(3) { lane ->
            current[lane] = (previous[lane] + ((tag ushr (4 - 2 * lane)) and 3) - 2) and 255
          }
      tag < 192 -> {
        val residuals = next()
        val dg = (tag and 63) - 32
        current[0] = (previous[0] + dg + (residuals ushr 4) - 8) and 255
        current[1] = (previous[1] + dg) and 255
        current[2] = (previous[2] + dg + (residuals and 15) - 8) and 255
      }
      else -> repetitions = (tag and 63) + 1
    }
    require(repetitions <= pixels - produced)
    val slot = (current[0] * 3 + current[1] * 5 + current[2] * 7 + current[3] * 11) and 63
    cache[slot] = current.copyOf() // RUN also updates the cache.
    repeat(repetitions) {
      repeat(4) { lane -> output[4 * produced + lane] = current[lane].toByte() }
      produced++
    }
    previous = current
    previousIndex = index
  }
  require(cursor == payloadEnd && data.copyOfRange(payloadEnd, data.size).contentEquals(endMarker))
  Raster(width.toInt(), height.toInt(), channels, colorspace, output)
}
    .getOrNull()

fun literalFile(image: Raster): ByteArray {
  val header =
      "qoif".toByteArray() +
          listOf(image.width, image.height)
              .flatMap { n ->
                (3 downTo 0).map { (n ushr (8 * it)).toByte() }
              }
              .toByteArray() +
          byteArrayOf(image.channels.toByte(), image.colorspace.toByte())
  return header +
      image.rgba.asList().chunked(4).flatMap { listOf(255.toByte()) + it }.toByteArray() +
      endMarker
}

fun raster(w: Int, h: Int, channels: Int = 4, colorspace: Int = 0, uniform: Boolean = false) =
    Raster(
        w,
        h,
        channels,
        colorspace,
        ByteArray(4 * w * h) {
          if (uniform) {
            if (it % 4 == 3) 255.toByte() else 0
          } else ((it * 53 + it / 7 * 19) and 255).toByte()
        },
    )

fun words(rgba: ByteArray) =
    ByteArray(rgba.size) { i ->
      when (i % 4) {
        0 -> rgba[i + 2]
        2 -> rgba[i - 2]
        else -> rgba[i]
      }
    }

fun little64(n: Long) = ByteArray(8) { (n ushr (it * 8)).toByte() }

val traceFile = hex("716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001")
val traceRgba = hex("000000ffff0000ffff0000ff000000ff010101ff070605ff07060580010101ff")

fun AssemblyCase.store(label: String, bytes: ByteArray) {
  require(bytes.size % 4 == 0)
  for (offset in bytes.indices step 4) {
    var value = 0
    for (lane in 0..3) value = value or ((bytes[offset + lane].toInt() and 255) shl (8 * lane))
    setWord(label, value, offset)
  }
}

val arenas =
    linkedMapOf(
        "input_path" to 128,
        "output_path" to 128,
        "source" to 20544,
        "metadata" to 64,
        "framebuffer" to 16448,
        "destination" to 20544,
        "status" to 4,
        "count" to 8,
        "mode" to 4,
        "byte_count" to 8,
        "pixel_capacity" to 8,
        "dimensions" to 8,
        "byte_capacity" to 8,
    )

fun AssemblyCase.seed(label: String, bytes: ByteArray = byteArrayOf(), offset: Int = 0) {
  val contents = ByteArray(arenas.getValue(label)) { 0xa5.toByte() }
  bytes.copyInto(contents, offset)
  store(label, contents)
}

fun AssemblyCase.begin(): List<Long> {
  for (label in arenas.keys) seed(label)
  store("workspace", ByteArray(8192) { 0xa5.toByte() })
  val saved = List(9) { 0x2345678900000000L + 271L * it }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val stack = readDataAddress("workspace", 8192)
  setRegister("sp", stack)
  return saved + stack
}

fun AssemblyCase.finished(saved: List<Long>, status: Int, count: Long) {
  expectRegister("v0", status)
  expectRegister("v1", count)
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", saved[9])
  expectOutput("")
}

fun contained(address: Long, size: Int, interval: Pair<Long, Int>) =
    address >= interval.first &&
        size <= interval.second &&
        address - interval.first <= interval.second - size

fun AssemblyCase.owned(
    readable: List<Pair<Long, Int>>,
    writable: List<Pair<Long, Int>>,
    execute: () -> Unit,
) {
  val regions = arenas.map { (label, size) -> Triple(label, readDataAddress(label), size) }
  val before = regions.map { (label, _, size) -> readBytes(label, size) }
  val low = regions.minOf { it.second }
  val high = regions.maxOf { it.second + it.third }
  val trace = traceMemoryAccesses(low, (high - low).toInt(), maxAccesses = 200000)
  try {
    execute()
  } finally {
    trace.close()
  }
  for (access in trace.snapshot()) {
    if (
        regions.any { (_, base, size) ->
          access.address < base + size && base < access.address + access.length
        }
    ) {
      expectCondition(
          (if (access.write) writable else readable + writable).any {
            contained(access.address, access.length, it)
          },
          "Access stays inside the declared caller intervals",
      )
    }
  }
  for ((i, region) in regions.withIndex()) {
    val (label, base, size) = region
    val after = readBytes(label, size)
    for (j in 0 until size) if (writable.none { contained(base + j, 1, it) }) {
      expectCondition(before[i][j] == after[j], "Caller bytes outside the result are unchanged")
    }
  }
}

fun AssemblyCase.bytesEqual(label: String, expected: ByteArray, offset: Int = 0) {
  expectCondition(
      readBytes(label, expected.size, offset)
          .contentEquals(
              expected.map { it.toInt() and 255 }.toIntArray(),
          ),
      "Exact result bytes match the independent host model",
  )
}

fun AssemblyCase.memory(
    entry: String,
    data: ByteArray,
    cap: Long = 4096,
    count: Long = data.size.toLong(),
    offset: Int = 3,
) {
  val saved = begin()
  seed("source", data, offset)
  val logical =
      if (count.toULong() <= 20502uL)
          ByteArray(count.toInt()) { if (it < data.size) data[it] else 0xa5.toByte() }
      else byteArrayOf()
  val image = inspect(logical)
  val status =
      if (count.toULong() > 20502uL) 2
      else if (image == null) 1
      else if (entry == "qoi_decode" && cap.toULong() < image.pixels.toULong()) 2 else 0
  val target = if (entry == "qoi_validate") "metadata" else "framebuffer"
  val result =
      if (status != 0) byteArrayOf()
      else if (entry == "qoi_validate") image!!.descriptor else words(image!!.rgba)
  setRegisterToDataAddress("a0", "source", offset.toLong())
  setRegister("a1", count)
  setRegisterToDataAddress("a2", target, 8)
  setRegister("a3", cap)
  val reads =
      if (count in 23..20502) listOf(readDataAddress("source", offset.toLong()) to logical.size)
      else emptyList()
  owned(
      reads,
      if (status == 0) listOf(readDataAddress(target, 8) to result.size) else emptyList(),
  ) {
    callFunction(entry, "function_return")
    finished(
        saved,
        status,
        if (status != 0) 0
        else if (entry == "qoi_validate") image!!.pixels.toLong() else image!!.dimensions,
    )
  }
  if (status == 0) bytesEqual(target, result, 8)
}

fun AssemblyCase.bound(w: Long, h: Long) {
  val saved = begin()
  val valid = w in 1..64 && h in 1..64
  setRegister("a0", (h shl 32) or (w and 0xffffffffL))
  owned(emptyList(), emptyList()) {
    callFunction("qoi_bound", "function_return")
    finished(saved, if (valid) 0 else 1, if (valid) 22 + 5 * w * h else 0)
  }
}

fun AssemblyCase.encode(image: Raster, cap: Long = 20502, retain: Boolean = false): ByteArray {
  val saved = begin()
  seed("metadata", image.descriptor, 8)
  seed("framebuffer", words(image.rgba), 4)
  val valid =
      image.width in 1..64 &&
          image.height in 1..64 &&
          image.channels in 3..4 &&
          image.colorspace in 0..1
  val bound = if (valid) 22 + 5 * image.pixels else 0
  val status = if (!valid) 1 else if (cap.toULong() < bound.toULong()) 2 else 0
  setRegisterToDataAddress("a0", "framebuffer", 4)
  setRegisterToDataAddress("a1", "metadata", 8)
  setRegisterToDataAddress("a2", "destination", 5)
  setRegister("a3", cap)
  var result = byteArrayOf()
  val reads =
      listOf(readDataAddress("metadata", 8) to 16) +
          if (status == 0) listOf(readDataAddress("framebuffer", 4) to image.rgba.size)
          else emptyList()
  owned(
      reads,
      if (status == 0) listOf(readDataAddress("destination", 5) to bound) else emptyList(),
  ) {
    callFunction("qoi_encode", "function_return")
    val n = readRegister("v1")
    finished(saved, status, if (status == 0) n else 0)
    if (status == 0) {
      expectCondition(n in 23L..bound.toLong(), "Returned file length is full width and bounded")
      result = readBytes("destination", n.toInt(), 5).map { it.toByte() }.toByteArray()
      val recovered = inspect(result)
      expectCondition(
          recovered != null &&
              recovered.width == image.width &&
              recovered.height == image.height &&
              recovered.channels == image.channels &&
              recovered.colorspace == image.colorspace &&
              recovered.rgba.contentEquals(image.rgba),
          "Equivalent encoding preserves all pixels and descriptive flags",
      )
      if (image.rgba.asList().chunked(4).distinct().size == 1) {
        val black = image.rgba.take(4) == listOf(0.toByte(), 0.toByte(), 0.toByte(), 255.toByte())
        val limit = if (black) 22 + (image.pixels + 61) / 62 else 27 + (image.pixels - 1 + 61) / 62
        expectCondition(n <= limit, "Uniform raster meets the stated compression bound")
      }
    }
  }
  if (status == 0)
      bytesEqual("destination", result + ByteArray(bound - result.size) { 0xa5.toByte() }, 5)
  if (retain && status == 0)
      println("ENCODED," + result.joinToString("") { "%02x".format(unsigned(it)) })
  return result
}

fun AssemblyCase.transfer(
    load: Boolean,
    data: ByteArray,
    cap: Long = 20502,
    chunk: Int = 3,
    failure: Int? = null,
    allowed: Boolean = true,
    count: Long = data.size.toLong(),
) {
  val saved = begin()
  val name = "student.qoi"
  val old = "previous result".toByteArray()
  seed("input_path", name.toByteArray() + byteArrayOf(0), 1)
  seed("output_path", name.toByteArray() + byteArrayOf(0), 1)
  if (!load) seed("source", data, 3)
  val logical =
      if (count.toULong() <= 20502uL)
          ByteArray(count.toInt()) { if (it < data.size) data[it] else 0xa5.toByte() }
      else byteArrayOf()
  val image = inspect(if (load) data else logical)
  val validCapacity = cap.toULong() in 1uL..20502uL
  val accepted =
      if (load) {
        if (!validCapacity || !allowed) 0
        else minOf(data.size, cap.toInt(), failure ?: Int.MAX_VALUE)
      } else if (image == null || !allowed) 0 else minOf(logical.size, failure ?: Int.MAX_VALUE)
  val status =
      if (load) {
        if (!validCapacity) 2
        else if (!allowed || failure != null && failure <= minOf(data.size, cap.toInt())) 3
        else if (data.size > cap) 2 else if (image == null) 1 else 0
      } else if (count.toULong() > 20502uL) 2
      else if (image == null) 1
      else if (!allowed || failure != null && failure < logical.size) 3 else 0
  val files =
      provideFiles(
          if (load) {
            if (allowed) mapOf(name to data) else emptyMap()
          } else mapOf(name to old),
          writablePaths = if (!load && allowed) setOf(name) else emptySet(),
          maxReadSize = chunk,
          maxWriteSize = chunk,
          failAfterBytes = if (load) failure else null,
          failWriteAfterBytes = if (load) null else failure,
          firstDescriptor = 23,
      )
  if (load) {
    setRegisterToDataAddress("a0", "input_path", 1)
    setRegisterToDataAddress("a1", "source", 3)
    setRegister("a2", cap)
    setRegisterToDataAddress("a3", "metadata", 8)
  } else {
    setRegisterToDataAddress("a0", "output_path", 1)
    setRegisterToDataAddress("a1", "source", 3)
    setRegister("a2", count)
  }
  val reads = mutableListOf<Pair<Long, Int>>()
  val writes = mutableListOf<Pair<Long, Int>>()
  if (load && validCapacity) {
    reads += readDataAddress("input_path", 1) to (name.length + 1)
    if (accepted > 0) writes += readDataAddress("source", 3) to accepted
    if (status == 0) writes += readDataAddress("metadata", 8) to 16
  } else if (!load && count in 23..20502) {
    reads += readDataAddress("source", 3) to logical.size + if (image != null && allowed) 1 else 0
    if (image != null) reads += readDataAddress("output_path", 1) to (name.length + 1)
  }
  owned(reads, writes) {
    callFunction(if (load) "qoi_load" else "qoi_save", "function_return")
    finished(
        saved,
        status,
        if (status == 0) (if (load) data.size else logical.size).toLong() else 0,
    )
  }
  files.expectAllClosed()
  val events = files.snapshot()
  expectCondition(
      events.filter { it.operation == Operation.CLOSE }.all { it.result == 0 },
      "Every close is valid",
  )
  expectCondition(
      events.count { it.operation == Operation.OPEN && it.result >= 0 } ==
          events.count { it.operation == Operation.CLOSE },
      "Every acquired descriptor closes once",
  )
  if (load) {
    if (accepted > 0) bytesEqual("source", data.copyOf(accepted), 3)
    if (status == 0) bytesEqual("metadata", image!!.descriptor, 8)
  } else files.expectFile(name, if (image != null && allowed) logical.copyOf(accepted) else old)
  if (load && !validCapacity || !load && image == null)
      expectCondition(events.isEmpty(), "Reject bounds or format before opening")
}

fun AssemblyCase.caller(data: ByteArray, missing: Boolean = false) {
  val saved = begin()
  val original = "older output".toByteArray()
  for ((label, value) in
      listOf(
          "mode" to 6L,
          "dimensions" to 4294967304L,
          "byte_count" to data.size.toLong(),
          "pixel_capacity" to 4096L,
          "byte_capacity" to 20502L,
      )) {
    setWord(label, value.toInt())
    if (label != "mode") setWord(label, (value ushr 32).toInt(), 4)
  }
  seed("input_path", "input.qoi".toByteArray() + byteArrayOf(0))
  seed("output_path", "output.qoi".toByteArray() + byteArrayOf(0))
  val files =
      provideFiles(
          if (missing) mapOf("output.qoi" to original)
          else mapOf("input.qoi" to data, "output.qoi" to original),
          writablePaths = setOf("output.qoi"),
          maxReadSize = 3,
          maxWriteSize = 2,
          firstDescriptor = 23,
      )
  val image = inspect(data)
  val status = if (missing) 3 else if (image == null) 1 else 0
  startAt("main")
  runUntil("results_ready")
  val count = readRegister("v1")
  finished(saved, status, if (status == 0) count else 0)
  if (status == 0) {
    expectCondition(
        count in 23L..(22L + 5L * image!!.pixels),
        "Caller retains actual encoded byte count",
    )
    bytesEqual("metadata", image.descriptor)
    bytesEqual("framebuffer", words(image.rgba))
    val encoded = readBytes("destination", count.toInt()).map { it.toByte() }.toByteArray()
    val actual = inspect(encoded)
    expectCondition(
        actual != null &&
            actual.width == image.width &&
            actual.height == image.height &&
            actual.channels == image.channels &&
            actual.colorspace == image.colorspace &&
            actual.rgba.contentEquals(image.rgba),
        "Caller output independently reconstructs image",
    )
    files.expectFile("output.qoi", encoded)
  } else files.expectFile("output.qoi", original)
  files.expectAllClosed()
  runToExit()
  expectWord("status", status)
  bytesEqual("count", little64(if (status == 0) count else 0))
}

// Guided observations describe the published illustrated construction, not free-writing
// requirements.
case("s29 offset function", instructionLimit = 6000000) {
  val saved = begin()
  seed(
      "source",
      hex("716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001"),
      3,
  )
  seed("input_path", "input.qoi".toByteArray() + byteArrayOf(0), 1)
  seed("output_path", "output.qoi".toByteArray() + byteArrayOf(0), 2)
  setWord("mode", 2.toInt())
  setWord("dimensions", 8.toInt())
  setWord("dimensions", 1.toInt(), 4)
  setWord("byte_count", 38.toInt())
  setWord("byte_count", 0.toInt(), 4)
  setWord("pixel_capacity", 4096.toInt())
  setWord("pixel_capacity", 0.toInt(), 4)
  setWord("byte_capacity", 20502.toInt())
  setWord("byte_capacity", 0.toInt(), 4)
  val files =
      provideFiles(
          mapOf(
              "input.qoi" to
                  hex(
                      "716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001",
                  ),
              "output.qoi" to hex("6f6c64206f7574707574"),
          ),
          writablePaths = setOf("output.qoi"),
          maxReadSize = 3,
          maxWriteSize = 3,
          firstDescriptor = 19,
      )
  setRegister("a0", readDataAddress("source", 3L))
  setRegister("a1", 38L)
  setRegister("a2", readDataAddress("framebuffer", 4L))
  setRegister("a3", 4096L)
  setRegisterToTextAddress("ra", "function_return")
  startAt("qoi_decode")
  runUntil("after_d_capacity")
  expectRegister("sp", saved[9] - 128)
  expectRegister("s3", 4096L)
  expectCondition(
      readBytes(saved[9] - 48, 16)
          .contentEquals(hex("08000000010000000400000000000000").map(::unsigned).toIntArray()),
      "Private accepted descriptor at the published checkpoint",
  )
  expectCondition(
      runCatching { files.expectAllClosed() }.isFailure == false,
      "Checkpoint descriptor ownership",
  )
  runUntil("function_return")
  finished(saved, 4, 0L)
  files.expectAllClosed()
  val events = files.snapshot()
  expectCondition(
      events.filter { it.operation == Operation.CLOSE }.all { it.result == 0 },
      "No duplicate close",
  )
  expectCondition(
      events.count { it.operation == Operation.OPEN && it.result >= 0 } ==
          events.count { it.operation == Operation.CLOSE },
      "Every acquired descriptor closes once",
  )
  expectCondition(events.isEmpty(), "Memory routines perform no file operations")
  bytesEqual(
      "source",
      hex("716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001") +
          ByteArray(20503) { 165.toByte() },
      3,
  )
  bytesEqual("metadata", hex("") + ByteArray(56) { 165.toByte() }, 8)
  bytesEqual("framebuffer", hex("") + ByteArray(16444) { 165.toByte() }, 4)
  bytesEqual("destination", hex("") + ByteArray(20539) { 165.toByte() }, 5)
  files.expectFile("output.qoi", hex("6f6c64206f7574707574"))
}

case("s29 caller", instructionLimit = 6000000) {
  val saved = begin()
  seed(
      "source",
      hex("716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001"),
      0,
  )
  seed("input_path", "input.qoi".toByteArray() + byteArrayOf(0), 0)
  seed("output_path", "output.qoi".toByteArray() + byteArrayOf(0), 0)
  setWord("mode", 2.toInt())
  setWord("dimensions", 8.toInt())
  setWord("dimensions", 1.toInt(), 4)
  setWord("byte_count", 38.toInt())
  setWord("byte_count", 0.toInt(), 4)
  setWord("pixel_capacity", 4096.toInt())
  setWord("pixel_capacity", 0.toInt(), 4)
  setWord("byte_capacity", 20502.toInt())
  setWord("byte_capacity", 0.toInt(), 4)
  val files =
      provideFiles(
          mapOf(
              "input.qoi" to
                  hex(
                      "716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001",
                  ),
              "output.qoi" to hex("6f6c64206f7574707574"),
          ),
          writablePaths = setOf("output.qoi"),
          maxReadSize = 3,
          maxWriteSize = 3,
          firstDescriptor = 19,
      )
  startAt("main")
  runUntil("decode_call")
  runUntil("after_d_capacity")
  expectRegister("sp", saved[9] - 128)
  expectRegister("s3", 4096L)
  expectCondition(
      readBytes(saved[9] - 48, 16)
          .contentEquals(hex("08000000010000000400000000000000").map(::unsigned).toIntArray()),
      "Private accepted descriptor at the published checkpoint",
  )
  expectCondition(
      runCatching { files.expectAllClosed() }.isFailure == false,
      "Checkpoint descriptor ownership",
  )
  runUntil("decode_return")
  finished(saved, 4, 0L)
  files.expectAllClosed()
  val events = files.snapshot()
  expectCondition(
      events.filter { it.operation == Operation.CLOSE }.all { it.result == 0 },
      "No duplicate close",
  )
  expectCondition(
      events.count { it.operation == Operation.OPEN && it.result >= 0 } ==
          events.count { it.operation == Operation.CLOSE },
      "Every acquired descriptor closes once",
  )
  expectCondition(events.isEmpty(), "Memory routines perform no file operations")
  bytesEqual(
      "source",
      hex("716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001") +
          ByteArray(20506) { 165.toByte() },
      0,
  )
  bytesEqual("metadata", hex("") + ByteArray(64) { 165.toByte() }, 0)
  bytesEqual("framebuffer", hex("") + ByteArray(16448) { 165.toByte() }, 0)
  bytesEqual("destination", hex("") + ByteArray(20544) { 165.toByte() }, 0)
  files.expectFile("output.qoi", hex("6f6c64206f7574707574"))
  runToExit()
  expectWord("status", 4)
  bytesEqual("count", little64(0L))
}
