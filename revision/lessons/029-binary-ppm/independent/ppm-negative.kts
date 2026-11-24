import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation

// This host oracle describes the published teaching subset, not a simulator routine.
data class ImageSpec(val width: Int, val height: Int, val start: Int) {
  val pixels
    get() = width * height

  val packed
    get() = (height.toLong() shl 32) or width.toLong()
}

val gap = "(?:[ \\t\\n\\x0B\\f\\r]|#[^\\n]*\\n)+"
val prefix = Regex("\\AP6${gap}([1-9][0-9]?)${gap}([1-9][0-9]?)${gap}255\\n")

fun inspect(bytes: ByteArray): ImageSpec? {
  if (bytes.size !in 3..12544) return null
  val match = prefix.find(bytes.toString(Charsets.ISO_8859_1)) ?: return null
  val width = match.groupValues[1].toInt()
  val height = match.groupValues[2].toInt()
  val start = match.range.last + 1
  return if (
      width in 1..64 && height in 1..64 && start <= 256 && bytes.size - start == 3 * width * height
  )
      ImageSpec(width, height, start)
  else null
}

fun samples(n: Int) = ByteArray(n) { ((it * 37 + 10) and 255).toByte() }

fun image(width: Int, height: Int) =
    "P6\n$width $height\n255\n".toByteArray() + samples(3 * width * height)

fun little(value: Long, width: Int) = ByteArray(width) { (value ushr (8 * it)).toByte() }

fun colors(rgb: ByteArray) =
    ByteArray(rgb.size / 3 * 4) {
      if (it % 4 == 3) 0 else rgb[it / 4 * 3 + 2 - it % 4]
    }

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
        "source" to 12576,
        "metadata" to 64,
        "framebuffer" to 16448,
        "destination" to 12576,
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
  store("workspace", ByteArray(4096) { 0xa5.toByte() })
  val saved = List(9) { 0x2345678900000000L + 271L * it }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val stack = readDataAddress("workspace", 4096)
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
) {
  val saved = begin()
  seed("source", data, 3)
  val logical =
      if (count.toULong() <= 12544uL)
          ByteArray(count.toInt()) {
            if (it < data.size) data[it] else 0xa5.toByte()
          }
      else byteArrayOf()
  val spec = inspect(logical)
  val status =
      if (count.toULong() > 12544uL) 2
      else if (spec == null) 1
      else if (entry == "ppm_decode" && cap.toULong() < spec.pixels.toULong()) 2 else 0
  val target = if (entry == "ppm_parse") "metadata" else "framebuffer"
  val expected =
      if (status != 0) byteArrayOf()
      else if (entry == "ppm_parse")
          little(spec!!.width.toLong(), 4) +
              little(spec.height.toLong(), 4) +
              little(spec.start.toLong(), 8)
      else colors(logical.copyOfRange(spec!!.start, logical.size))
  setRegisterToDataAddress("a0", "source", 3)
  setRegister("a1", count)
  setRegisterToDataAddress("a2", target, 8)
  setRegister("a3", cap)
  val reads =
      if (count in 3..12544) listOf(readDataAddress("source", 3) to logical.size) else emptyList()
  val writes = if (status == 0) listOf(readDataAddress(target, 8) to expected.size) else emptyList()
  owned(reads, writes) {
    callFunction(entry, "function_return")
    finished(
        saved,
        status,
        if (status != 0) 0 else if (entry == "ppm_parse") spec!!.pixels.toLong() else spec!!.packed,
    )
  }
  if (status == 0) bytesEqual(target, expected, 8)
}

fun AssemblyCase.encode(width: Long, height: Long, cap: Long = 12544) {
  val saved = begin()
  val valid = width in 1..64 && height in 1..64
  val rgb = if (valid) samples((3 * width * height).toInt()) else byteArrayOf()
  val pixels = colors(rgb)
  for (i in 3 until pixels.size step 4) pixels[i] = 0xe7.toByte()
  seed("framebuffer", pixels, 4)
  val expected = if (valid) image(width.toInt(), height.toInt()) else byteArrayOf()
  val status = if (!valid) 1 else if (cap.toULong() < expected.size.toULong()) 2 else 0
  setRegisterToDataAddress("a0", "framebuffer", 4)
  setRegister("a1", (height shl 32) or (width and 0xffffffffL))
  setRegisterToDataAddress("a2", "destination", 5)
  setRegister("a3", cap)
  owned(
      if (status == 0) listOf(readDataAddress("framebuffer", 4) to pixels.size) else emptyList(),
      if (status == 0) listOf(readDataAddress("destination", 5) to expected.size) else emptyList(),
  ) {
    callFunction("ppm_encode", "function_return")
    finished(saved, status, if (status == 0) expected.size.toLong() else 0)
  }
  if (status == 0) bytesEqual("destination", expected, 5)
}

fun AssemblyCase.transfer(
    load: Boolean,
    data: ByteArray,
    cap: Long = 12544,
    chunk: Int = 2,
    failAt: Int? = null,
    allowed: Boolean = true,
    selected: Long = data.size.toLong(),
) {
  val saved = begin()
  val name = "student.ppm"
  val previous = "existing result".toByteArray()
  seed("input_path", name.toByteArray() + byteArrayOf(0), 1)
  seed("output_path", name.toByteArray() + byteArrayOf(0), 1)
  if (!load) seed("source", data, 3)
  val logical =
      if (selected.toULong() <= 12544uL)
          ByteArray(selected.toInt()) {
            if (it < data.size) data[it] else 0xa5.toByte()
          }
      else byteArrayOf()
  val validCap = cap.toULong() in 1uL..12544uL
  val header = inspect(if (load) data else logical)
  val accepted =
      if (load) {
        if (!validCap || !allowed) 0 else minOf(data.size, cap.toInt(), failAt ?: Int.MAX_VALUE)
      } else if (header == null || !allowed) 0 else minOf(logical.size, failAt ?: Int.MAX_VALUE)
  val status =
      if (load) {
        if (!validCap) 2
        else if (!allowed || failAt != null && failAt <= minOf(data.size, cap.toInt())) 3
        else if (data.size > cap) 2 else if (header == null) 1 else 0
      } else if (selected.toULong() > 12544uL) 2
      else if (header == null) 1
      else if (!allowed || failAt != null && failAt < logical.size) 3 else 0
  val files =
      provideFiles(
          if (load) {
            if (allowed) mapOf(name to data) else emptyMap()
          } else mapOf(name to previous),
          writablePaths = if (!load && allowed) setOf(name) else emptySet(),
          maxReadSize = chunk,
          maxWriteSize = chunk,
          failAfterBytes = if (load) failAt else null,
          failWriteAfterBytes = if (load) null else failAt,
          firstDescriptor = 19,
      )
  if (load) {
    setRegisterToDataAddress("a0", "input_path", 1)
    setRegisterToDataAddress("a1", "source", 3)
    setRegister("a2", cap)
    setRegisterToDataAddress("a3", "metadata", 8)
  } else {
    setRegisterToDataAddress("a0", "output_path", 1)
    setRegisterToDataAddress("a1", "source", 3)
    setRegister("a2", selected)
  }
  val accesses = mutableListOf<Pair<Long, Int>>()
  val writes = mutableListOf<Pair<Long, Int>>()
  if (load && validCap || !load && header != null)
      accesses += readDataAddress(if (load) "input_path" else "output_path", 1) to name.length + 1
  if (load) {
    if (accepted > 0) writes += readDataAddress("source", 3) to accepted
    if (status == 0) writes += readDataAddress("metadata", 8) to 16
  } else if (selected in 3..12544)
      accesses += readDataAddress("source", 3) to logical.size + if (header != null) 1 else 0
  owned(accesses, writes) {
    callFunction(if (load) "ppm_load" else "ppm_save", "function_return")
    finished(
        saved,
        status,
        if (status == 0) (if (load) data.size else logical.size).toLong() else 0,
    )
  }
  files.expectAllClosed()
  expectCondition(
      files.snapshot().filter { it.operation == Operation.CLOSE }.all { it.result == 0 },
      "No invalid or duplicate close",
  )
  if (load) {
    if (accepted > 0) bytesEqual("source", data.copyOf(accepted), 3)
    if (status == 0)
        bytesEqual(
            "metadata",
            little(header!!.width.toLong(), 4) +
                little(header.height.toLong(), 4) +
                little(header.start.toLong(), 8),
            8,
        )
  } else
      files.expectFile(name, if (header != null && allowed) logical.copyOf(accepted) else previous)
  if (load && !validCap || !load && header == null)
      expectCondition(files.snapshot().isEmpty(), "Bounds or format failure precedes opening files")
}

fun AssemblyCase.checkNegative(w: Long, h: Long, index: Int = 0, capacity: Long = 12544) {
  val saved = begin()
  val valid = w in 1..64 && h in 1..64
  val n = if (valid) (w * h).toInt() else 0
  val pixels = IntArray(n) { j -> ((j * 104729L + index * 8191L).toInt() xor 0xa57f3c19.toInt()) }
  val raw = ByteArray(n * 4) { j -> (pixels[j / 4] ushr (8 * (j % 4))).toByte() }
  val offset = index % 8 * 4
  seed("framebuffer", raw, offset)
  val expected =
      if (valid)
          "P6\n$w $h\n255\n".toByteArray() +
              ByteArray(n * 3) { j ->
                (255 - ((pixels[j / 3] ushr (16 - 8 * (j % 3))) and 255)).toByte()
              }
      else byteArrayOf()
  val status = if (!valid) 1 else if (capacity.toULong() < expected.size.toULong()) 2 else 0
  val outOffset = (index * 7 + 1) % 32
  val dest = readDataAddress("destination", outOffset.toLong())
  val input = readDataAddress("framebuffer", offset.toLong())
  setRegister("a0", if (status == 0) input else 1)
  setRegister("a1", (h shl 32) or (w and 0xffffffffL))
  setRegister("a2", if (status == 0) dest else 1)
  setRegister("a3", capacity)
  val reads = if (status == 0) listOf(input to raw.size) else emptyList()
  val writes = if (status == 0) listOf(dest to expected.size) else emptyList()
  owned(reads, writes) {
    callFunction("ppm_encode_negative", "function_return")
    finished(saved, status, if (status == 0) expected.size.toLong() else 0)
  }
  if (status == 0) bytesEqual("destination", expected, outOffset)
}

for (w in listOf(1L, 2L, 3L, 9L, 10L, 16L, 31L, 64L)) for (h in
    listOf(1L, 2L, 9L, 10L, 64L)) for (kind in 0..3) case(
    "negative $w by $h capacity-kind $kind",
    instructionLimit = 3000000,
) {
  val size = "P6\n$w $h\n255\n".length + 3 * (w * h).toInt()
  checkNegative(
      w,
      h,
      (w * 3 + h * 7 + kind).toInt(),
      when (kind) {
        0 -> 0L
        1 -> (size - 1).toLong()
        2 -> size.toLong()
        else -> Long.MIN_VALUE
      },
  )
}

for (w in listOf(0L, 65L, 0xffffffffL)) case("negative invalid width $w") { checkNegative(w, 2) }

for (h in listOf(0L, 65L, 0x80000000L)) case("negative invalid height $h") { checkNegative(2, h) }

case("negative caller publishes complementary image", instructionLimit = 3000000) {
  val before = readBytes("framebuffer", 16448)
  runUntil("negative_return")
  expectRegister("v0", 0)
  expectRegister("v1", 23)
  val bytes = "P6\n2 2\n255\n".toByteArray() + byteArrayOf(0, -1, -1, -1, 0, -1, -1, -1, 0, 0, 0, 0)
  expectCondition(
      readBytes("destination", 23).contentEquals(bytes.map { it.toInt() and 255 }.toIntArray()),
      "Canonical cyan, magenta, yellow and black raster",
  )
  expectCondition(
      before.contentEquals(readBytes("framebuffer", 16448)),
      "Source including alpha remains unchanged",
  )
  runToExit()
  expectWord("status", 0)
  expectWord("count", 23)
  expectWord("count", 0, 4)
  expectOutput("")
}
