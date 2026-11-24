import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation

fun octets(s: String) = s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun field(b: IntArray, at: Int, v: Long, little: Boolean, n: Int = 8) {
  for (j in 0 until n) b[at + j] = ((v ushr (8 * (if (little) j else n - 1 - j))) and 255).toInt()
}

fun AssemblyCase.put(label: String, b: IntArray, little: Boolean) {
  for (i in b.indices step 4) {
    var v = 0
    for (j in 0..3) v = v or (b[i + j] shl (8 * (if (little) j else 3 - j)))
    setWord(label, v, i)
  }
}

fun AssemblyCase.seed(label: String, raw: ByteArray, little: Boolean, offset: Int = 3): IntArray {
  val b = IntArray(65568) { 165 }
  for (i in raw.indices) b[offset + i] = raw[i].toInt() and 255
  put(label, b, little)
  return b
}

fun AssemblyCase.saved(): Long {
  val top = readDataAddress("pngCheck_stack", 32768)
  setRegister("sp", top)
  for (i in 0..7) setRegister("s$i", 0x1234567800000000L + i * 991)
  setRegister("fp", 0x2345678900000000L)
  return top
}

fun AssemblyCase.retained(top: Long, status: Int, count: Int) {
  expectRegister("v0", status)
  expectRegister("v1", count)
  expectRegister("sp", top)
  for (i in 0..7) expectRegister("s$i", 0x1234567800000000L + i * 991)
  expectRegister("fp", 0x2345678900000000L)
}

fun AssemblyCase.collect(
    raw: ByteArray,
    w: Int,
    h: Int,
    joined: ByteArray,
    status: Int,
    little: Boolean,
    capacity: Int = 65536,
    count: Long = raw.size.toLong(),
) {
  val input = seed("pngCheck_source", raw, little)
  seed("pngCheck_compressed", byteArrayOf(), little)
  val descriptor = IntArray(128) { 165 }
  field(descriptor, 0, capacity.toLong(), little)
  put("pngCheck_context", descriptor, little)
  val source = readDataAddress("pngCheck_source", 3)
  val dest = readDataAddress("pngCheck_compressed", 3)
  val context = readDataAddress("pngCheck_context")
  val top = saved()
  setRegister("a0", if (count < 8 || count > 1048576) 0x123456780001L else source)
  setRegister("a1", count)
  setRegister("a2", dest)
  setRegister("a3", context)
  val reads = traceMemoryAccesses("pngCheck_source", 65568)
  val stores = traceMemoryAccesses("pngCheck_compressed", 65568)
  val outputs = traceMemoryAccesses("pngCheck_context", 128)
  callFunction("png_collect", "pngCheck_return")
  reads.close()
  stores.close()
  outputs.close()
  retained(top, status, if (status == 0) joined.size else 0)
  reads.expectReadsWithin(source, if (count in 8..1048576) raw.size else 0)
  expectCondition(reads.snapshot().none { it.write }, "Input is immutable")
  for (a in stores.snapshot()) if (a.write)
      expectCondition(
          a.address >= dest && a.address + a.length <= dest + capacity,
          "Joined bytes stay in capacity",
      )
  if (status == 0) {
    for ((i, v) in listOf(w, h, joined.size, h * (1 + 3 * w)).withIndex()) field(
        descriptor,
        8 + 8 * i,
        v.toLong(),
        little,
    )
    expectCondition(
        readBytes(dest, joined.size).contentEquals(joined.map { it.toInt() and 255 }.toIntArray()),
        "Exact joined stream",
    )
  } else
      expectCondition(
          outputs.snapshot().none { it.write },
          "Failed collection publishes no descriptor fields",
      )
  expectCondition(
      readBytes("pngCheck_source", 65568).contentEquals(input),
      "Every source/guard byte retained",
  )
  expectCondition(
      readBytes("pngCheck_context", 128).contentEquals(descriptor),
      "Exact descriptor publication and retained inputs/suffix",
  )
}

fun AssemblyCase.inflate(
    raw: ByteArray,
    plain: ByteArray,
    status: Int,
    little: Boolean,
    expected: Long = plain.size.toLong(),
    count: Long = raw.size.toLong(),
) {
  val input = seed("pngCheck_source", raw, little)
  seed("pngCheck_rows", byteArrayOf(), little)
  val source = readDataAddress("pngCheck_source", 3)
  val dest = readDataAddress("pngCheck_rows", 3)
  val top = saved()
  val early = count < 6 || count > 1048576 || expected < 0 || expected > 786944
  setRegister("a0", if (early) 0x123456780001L else source)
  setRegister("a1", count)
  setRegister("a2", dest)
  setRegister("a3", expected)
  val reads = traceMemoryAccesses("pngCheck_source", 65568, maxAccesses = 1000000)
  val stores = traceMemoryAccesses("pngCheck_rows", 65568, maxAccesses = 1000000)
  callFunction("png_inflate", "pngCheck_return")
  reads.close()
  stores.close()
  retained(top, status, if (status == 0) plain.size else 0)
  reads.expectReadsWithin(source, if (early) 0 else raw.size)
  expectCondition(reads.snapshot().none { it.write }, "Stream is immutable")
  for (a in stores.snapshot()) if (a.write)
      expectCondition(
          a.address >= dest && a.address + a.length <= dest + expected,
          "Inflated writes fit selected output window",
      )
  expectCondition(
      readBytes("pngCheck_source", 65568).contentEquals(input),
      "Input and guards retained",
  )
  if (status == 0)
      expectCondition(
          readBytes(dest, plain.size).contentEquals(plain.map { it.toInt() and 255 }.toIntArray()),
          "Exact inflated bytes and count",
      )
}

fun cooked(filtered: ByteArray, rgb: ByteArray, w: Int, h: Int): ByteArray {
  val out = filtered.copyOf()
  for (y in 0 until h) rgb.copyInto(out, y * (1 + 3 * w) + 1, 3 * w * y, 3 * w * (y + 1))
  return out
}

fun AssemblyCase.rows(
    filtered: ByteArray,
    rgb: ByteArray,
    w: Int,
    h: Int,
    status: Int,
    msa: Boolean,
    little: Boolean,
    count: Long = filtered.size.toLong(),
) {
  val before = seed("pngCheck_source", filtered, little)
  val source = readDataAddress("pngCheck_source", 3)
  val top = saved()
  setRegister("a0", source)
  setRegister("a1", count)
  setRegister("a2", w)
  setRegister("a3", h)
  val access = traceMemoryAccesses("pngCheck_source", 65568, maxAccesses = 1000000)
  callFunction(if (msa) "png_unfilter_msa" else "png_unfilter", "pngCheck_return")
  access.close()
  retained(top, status, if (status == 0) filtered.size else 0)
  access.expectReadsWithin(source, filtered.size)
  if (status == 0) {
    val wanted = cooked(filtered, rgb, w, h)
    for (i in wanted.indices) before[3 + i] = wanted[i].toInt() and 255
  } else
      expectCondition(
          access.snapshot().none { it.write },
          "All selectors validate before any sample changes",
      )
  expectCondition(
      readBytes("pngCheck_source", 65568).contentEquals(before),
      "Exact samples, retained selectors and guard bytes",
  )
}

fun AssemblyCase.pack(
    filtered: ByteArray,
    rgb: ByteArray,
    w: Int,
    h: Int,
    stride: Int,
    capacity: Int,
    status: Int,
    msa: Boolean,
    little: Boolean,
    count: Long = filtered.size.toLong(),
    misaligned: Boolean = false,
) {
  val samples = cooked(filtered, rgb, w, h)
  val before = seed("pngCheck_source", samples, little)
  val frame = seed("pngCheck_frame", byteArrayOf(), little, 4)
  val descriptor = IntArray(128) { 165 }
  for ((i, v) in listOf(w, h, stride, capacity).withIndex()) field(
      descriptor,
      8 * i,
      v.toLong(),
      little,
  )
  put("pngCheck_context", descriptor, little)
  val source = readDataAddress("pngCheck_source", 3)
  val dest = readDataAddress("pngCheck_frame", if (misaligned) 5 else 4)
  val ctx = readDataAddress("pngCheck_context")
  val top = saved()
  setRegister("a0", source)
  setRegister("a1", count)
  setRegister("a2", dest)
  setRegister("a3", ctx)
  val access = traceMemoryAccesses("pngCheck_source", 65568, maxAccesses = 1000000)
  val writes = traceMemoryAccesses("pngCheck_frame", 65568, maxAccesses = 1000000)
  callFunction(if (msa) "png_blit_msa" else "png_blit", "pngCheck_return")
  access.close()
  writes.close()
  retained(top, status, if (status == 0) 4 * w * h else 0)
  access.expectReadsWithin(source, samples.size)
  expectCondition(access.snapshot().none { it.write }, "Packing retains RGB samples")
  if (status == 0) {
    for (y in 0 until h) for (x in 0 until w) {
      val at = 3 * (y * w + x)
      val word =
          ((rgb[at].toInt() and 255) shl 16) or
              ((rgb[at + 1].toInt() and 255) shl 8) or
              (rgb[at + 2].toInt() and 255)
      field(frame, 4 + 4 * (y * stride + x), word.toLong(), little, 4)
    }
  } else
      expectCondition(
          writes.snapshot().none { it.write },
          "Packing failure performs no framebuffer stores",
      )
  for (a in writes.snapshot()) {
    val at = a.address - dest
    expectCondition(
        if (a.write) at >= 0 && at / (4 * stride) < h && at % (4 * stride) + a.length <= 4 * w
        else at >= 0 && at + a.length <= capacity,
        "Stores fit active rows; reads fit caller capacity",
    )
  }
  expectCondition(
      readBytes("pngCheck_frame", 65568).contentEquals(frame),
      "Exact native colors and retained row padding/guards",
  )
  expectCondition(readBytes("pngCheck_source", 65568).contentEquals(before), "Source is unchanged")
  expectCondition(
      readBytes("pngCheck_context", 128).contentEquals(descriptor),
      "Packing descriptor retained",
  )
}

fun AssemblyCase.decode(
    raw: ByteArray,
    rgb: ByteArray,
    w: Int,
    h: Int,
    status: Int,
    msa: Boolean,
    little: Boolean,
    stride: Int = w + 3,
    compcap: Int = 65536,
    rowcap: Int = 65536,
    framecap: Int = if (w > 0 && h > 0) 4 * ((h - 1) * stride + w) else 65536,
    reuse: Boolean = false,
) {
  val input = seed("pngCheck_source", raw, little)
  val frame =
      if (reuse) readBytes("pngCheck_frame", 65568)
      else seed("pngCheck_frame", byteArrayOf(), little, 4)
  seed("pngCheck_compressed", byteArrayOf(), little)
  seed("pngCheck_rows", byteArrayOf(), little)
  val source = readDataAddress("pngCheck_source", 3)
  val dest = readDataAddress("pngCheck_frame", 4)
  val compressed = readDataAddress("pngCheck_compressed", 3)
  val rows = readDataAddress("pngCheck_rows", 3)
  val context = readDataAddress("pngCheck_context")
  val fields = if (reuse) readBytes("pngCheck_context", 128) else IntArray(128) { 165 }
  for ((i, v) in
      listOf(
              stride.toLong(),
              framecap.toLong(),
              compressed,
              compcap.toLong(),
              rows,
              rowcap.toLong(),
          )
          .withIndex()) field(fields, 8 * i, v, little)
  put("pngCheck_context", fields, little)
  val top = saved()
  setRegister("a0", source)
  setRegister("a1", raw.size)
  setRegister("a2", dest)
  setRegister("a3", context)
  val src = traceMemoryAccesses("pngCheck_source", 65568, maxAccesses = 1000000)
  val output = traceMemoryAccesses("pngCheck_frame", 65568, maxAccesses = 1000000)
  val meta = traceMemoryAccesses("pngCheck_context", 128, maxAccesses = 1000000)
  val comp = traceMemoryAccesses("pngCheck_compressed", 65568, maxAccesses = 1000000)
  val row = traceMemoryAccesses("pngCheck_rows", 65568, maxAccesses = 1000000)
  callFunction(if (msa) "png_decode_msa" else "png_decode", "pngCheck_return")
  for (t in listOf(src, output, meta, comp, row)) t.close()
  retained(top, status, if (status == 0) 4 * w * h else 0)
  src.expectReadsWithin(source, raw.size)
  expectCondition(src.snapshot().none { it.write }, "PNG source immutable")
  if (status == 0) {
    for (y in 0 until h) for (x in 0 until w) {
      val at = 3 * (y * w + x)
      val word =
          ((rgb[at].toInt() and 255) shl 16) or
              ((rgb[at + 1].toInt() and 255) shl 8) or
              (rgb[at + 2].toInt() and 255)
      field(frame, 4 + 4 * (y * stride + x), word.toLong(), little, 4)
    }
    for ((i, v) in listOf(w, h, 4 * w * h).withIndex()) field(
        fields,
        48 + 8 * i,
        v.toLong(),
        little,
    )
  } else {
    expectCondition(
        output.snapshot().none { it.write },
        "Rejected content never stores to framebuffer",
    )
    expectCondition(meta.snapshot().none { it.write }, "Failure never publishes context")
  }
  for (a in output.snapshot()) {
    val at = a.address - dest
    expectCondition(
        if (a.write) at >= 0 && at / (4 * stride) < h && at % (4 * stride) + a.length <= 4 * w
        else at >= 0 && at + a.length <= framecap,
        "Stores fit active rows; reads fit caller capacity",
    )
  }
  for (a in comp.snapshot()) if (a.write)
      expectCondition(
          a.address >= compressed && a.address + a.length <= compressed + compcap,
          "Compressed scratch bounds",
      )
  for (a in row.snapshot()) expectCondition(
      a.address >= rows && a.address + a.length <= rows + rowcap,
      "Row scratch bounds",
  )
  expectCondition(
      readBytes("pngCheck_frame", 65568).contentEquals(frame),
      "Exact framebuffer or earlier retained image",
  )
  expectCondition(
      readBytes("pngCheck_context", 128).contentEquals(fields),
      "Exact published fields and retained caller state",
  )
  expectCondition(readBytes("pngCheck_source", 65568).contentEquals(input), "Input guards retained")
}

fun AssemblyCase.loader(
    payload: ByteArray,
    capacity: Long,
    status: Int,
    little: Boolean,
    maximumRead: Int = 7,
    failure: Int? = null,
    missing: Boolean = false,
) {
  val files =
      provideFiles(
          if (missing) emptyMap() else mapOf("image.png" to payload),
          maxReadSize = maximumRead,
          failAfterBytes = failure,
          firstDescriptor = 17,
      )
  val before = seed("pngCheck_source", byteArrayOf(), little)
  val dest = readDataAddress("pngCheck_source", 3)
  val top = saved()
  setRegister(
      "a0",
      if (capacity <= 0 || capacity > 1048576) 0x123456780001L
      else readDataAddress("pngCheck_path"),
  )
  setRegister("a1", dest)
  setRegister("a2", capacity)
  val writes = traceMemoryAccesses("pngCheck_source", 65568, maxAccesses = 1000000)
  callFunction("png_load", "pngCheck_return")
  writes.close()
  retained(top, status, if (status == 0) payload.size else 0)
  files.expectAllClosed()
  for (a in writes.snapshot()) if (a.write)
      expectCondition(
          a.address >= dest && a.address + a.length <= dest + capacity,
          "File writes fit capacity; probe stays private",
      )
  val calls = files.snapshot()
  if (capacity <= 0 || capacity > 1048576)
      expectCondition(calls.isEmpty(), "Reject count before opening")
  else {
    expectCondition(calls.count { it.operation == Operation.OPEN } == 1, "Open once")
    expectCondition(
        calls.count { it.operation == Operation.CLOSE } == (if (missing) 0 else 1),
        "Close exactly the acquired descriptor",
    )
    expectCondition(
        calls
            .filter { it.operation == Operation.READ }
            .all { it.descriptor == 17 && it.requestedBytes > 0 },
        "Use owned descriptor and real read requests",
    )
  }
  if (status == 0)
      expectCondition(
          readBytes(dest, payload.size)
              .contentEquals(payload.map { it.toInt() and 255 }.toIntArray()),
          "Exact loaded file bytes",
      )
}
