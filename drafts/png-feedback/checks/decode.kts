import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation
import java.nio.ByteOrder

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

val pngData0 = octets("")
val pngData1 = octets("0b")
val pngData2 =
    octets(
        "0b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085",
    )
val pngData3 =
    octets(
        "0b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b30557a9fc4e90e33587da2c7ec11365b80a5caef14395e83a8cdf2173c6186abd0f51a3f6489aed3f81d42678cb1d6fb20456a8fb4d9fe23486d92b7dc01264b7095badf04294e7398bde2072c51769bc0e50a2f54799ec3e80d32577ca1c6eb10355a7fa4c9ee13385d82a7ccf1163b6085aacff4193e6388add2f71c41668bb0d5fa1f44698eb3d8fd22476c91b6db00254a6f94b9de03284d7297bce1062b50759abfe4092e53789dc2e70c31567ba0c5ea0f34597ea3c8ed12375c81a6cbf0153a5f84a9cef3183d6287acd1f61b40658aafd4f91e43688db2d7fc21466b90b5daff24496e93b8dd02274c7196bbe0052a4f7499bee3082d52779cc1e60b",
    )
val pngData4 = octets("41")
val pngData5 = octets("4142434445")
val pngData6 =
    octets(
        "89504e470d0a1a0a0000000d49484452000000010000000508020000000be611c800000001494441547876e684e60000001e4944415401011400ebff000061c202353535016acb2c036a9b4b043535353413051de7a3647e0000000049454e44ae426082",
    )
val pngData7 = octets("7801011400ebff000061c202353535016acb2c036a9b4b043535353413051d")
val pngData8 =
    octets(
        "89504e470d0a1a0a0000000d49484452000000010000000508020000000be611c800000001494441547876e684e60000001a49444154016360483cc4646a6aca98755a87396bb6370b900d003413051d19bdbe5e0000000049454e44ae426082",
    )
val pngData9 = octets("78016360483cc4646a6aca98755a87396bb6370b900d003413051d")
val pngData10 =
    octets(
        "89504e470d0a1a0a0000000d49484452000000010000000508020000000be611c800000001494441547876e684e60000001a494441549c6360483cc4646a6aca98755a87396bb6370b900d003413051d798a30d40000000049454e44ae426082",
    )
val pngData11 = octets("789c6360483cc4646a6aca98755a87396bb6370b900d003413051d")
val pngData12 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e60000002d4944415401012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023ff6743c170000000049454e44ae426082",
    )
val pngData13 =
    octets(
        "7801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f",
    )
val pngData14 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e600000021494441540163e01291d330b2616205034620474e4e8e995f4452484888052208002e0c023fde3eba940000000049454e44ae426082",
    )
val pngData15 = octets("780163e01291d330b2616205034620474e4e8e995f4452484888052208002e0c023f")
val pngData16 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e600000021494441549c63e01291d330b2616205034620474e4e8e995f4452484888052208002e0c023ffec5e9150000000049454e44ae426082",
    )
val pngData17 = octets("789c63e01291d330b2616205034620474e4e8e995f4452484888052208002e0c023f")
val pngData18 =
    octets(
        "89504e470d0a1a0a0000000d494844520000000f000000050802000000152f217b00000001494441547876e684e6000000f0494441540101e60019ff000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c8023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63604353535292929292929382929292939292929292929293c292929293e292929292929294029292929422929292974c63b5b365081690000000049454e44ae426082",
    )
val pngData19 =
    octets(
        "780101e60019ff000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c8023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63604353535292929292929382929292939292929292929293c292929293e292929292929294029292929422929292974c63b5b",
    )
val pngData20 =
    octets(
        "89504e470d0a1a0a0000000d494844520000000f000000050802000000152f217b00000001494441547876e684e60000009e49444154016360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd0493a9a9a9999999b9b9b9858585a5a5a5959595b5b5b58d8d8dadadad9d9d9dbdbdbd838383a3a3a3939393b3b33363d6691d75a20173d66c6ffdf5fa06606008021b8dc0c0180cb69880802918986d336301529a606001222c216c4d1b106107e53880082710010074c63b5bce0b78240000000049454e44ae426082",
    )
val pngData21 =
    octets(
        "78016360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd0493a9a9a9999999b9b9b9858585a5a5a5959595b5b5b58d8d8dadadad9d9d9dbdbdbd838383a3a3a3939393b3b33363d6691d75a20173d66c6ffdf5fa06606008021b8dc0c0180cb69880802918986d336301529a606001222c216c4d1b106107e53880082710010074c63b5b",
    )
val pngData22 =
    octets(
        "89504e470d0a1a0a0000000d494844520000000f000000050802000000152f217b00000001494441547876e684e60000009e494441549c6360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd0493a9a9a9999999b9b9b9858585a5a5a5959595b5b5b58d8d8dadadad9d9d9dbdbdbd838383a3a3a3939393b3b33363d6691d75a20173d66c6ffdf5fa06606008021b8dc0c0180cb69880802918986d336301529a606001222c216c4d1b106107e53880082710010074c63b5b9afb62060000000049454e44ae426082",
    )
val pngData23 =
    octets(
        "789c6360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd0493a9a9a9999999b9b9b9858585a5a5a5959595b5b5b58d8d8dadadad9d9d9dbdbdbd838383a3a3a3939393b3b33363d6691d75a20173d66c6ffdf5fa06606008021b8dc0c0180cb69880802918986d336301529a606001222c216c4d1b106107e53880082710010074c63b5b",
    )
val pngData24 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000010000000050802000000c38afb1e00000001494441547876e684e6000000ff494441540101f5000aff000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343444444016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63636363604353535292929292929382929292939292929292929293c292929293e2929292929292940292929294229292929292929e4533f5d6d3c9cd90000000049454e44ae426082",
    )
val pngData25 =
    octets(
        "780101f5000aff000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343444444016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63636363604353535292929292929382929292939292929292929293c292929293e2929292929292940292929294229292929292929e4533f5d",
    )
val pngData26 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000010000000050802000000c38afb1e00000001494441547876e684e6000000a649444154016360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cf5b265353533333337373730b0b0b4b4b4b2b2b2b6b6b6b1b1b1b5b5b5b3b3b3b7b7b7b070707474747272727676767171717c6acd33aeaa400e6acd9defaebf50dc0c01004361a818131186c310101533030db06748a190b90a509061620c212c2d6b4011176508e038870827200e4533f5d44f5b9800000000049454e44ae426082",
    )
val pngData27 =
    octets(
        "78016360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cf5b265353533333337373730b0b0b4b4b4b2b2b2b6b6b6b1b1b1b5b5b5b3b3b3b7b7b7b070707474747272727676767171717c6acd33aeaa400e6acd9defaebf50dc0c01004361a818131186c310101533030db06748a190b90a509061620c212c2d6b4011176508e038870827200e4533f5d",
    )
val pngData28 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000010000000050802000000c38afb1e00000001494441547876e684e6000000a6494441549c6360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cf5b265353533333337373730b0b0b4b4b4b2b2b2b6b6b6b1b1b1b5b5b5b3b3b3b7b7b7b070707474747272727676767171717c6acd33aeaa400e6acd9defaebf50dc0c01004361a818131186c310101533030db06748a190b90a509061620c212c2d6b4011176508e038870827200e4533f5de4096f390000000049454e44ae426082",
    )
val pngData29 =
    octets(
        "789c6360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cf5b265353533333337373730b0b0b4b4b4b2b2b2b6b6b6b1b1b1b5b5b5b3b3b3b7b7b7b070707474747272727676767171717c6acd33aeaa400e6acd9defaebf50dc0c01004361a818131186c310101533030db06748a190b90a509061620c212c2d6b4011176508e038870827200e4533f5d",
    )
val pngData30 =
    octets(
        "89504e470d0a1a0a0000000d49484452000000110000000508020000002c48902000000001494441547876e684e60000010e4944415401010401fbfe000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced50b112023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343444444454545016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63636363637373704353535292929292929382929292939292929292929293c292929293e29292929292929402929292942292929292929294529292bd342f0d8fd332f0000000049454e44ae426082",
    )
val pngData31 =
    octets(
        "7801010401fbfe000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced50b112023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343444444454545016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63636363637373704353535292929292929382929292939292929292929293c292929293e29292929292929402929292942292929292929294529292bd342f0",
    )
val pngData32 =
    octets(
        "89504e470d0a1a0a0000000d49484452000000110000000508020000002c48902000000001494441547876e684e6000000b049444154016360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cfdb808d424ca6a6a6666666e6e6e6161616969696565656d6d6d6363636b6b6b6767676f6f6f60e0e0e8e8e8e4e4e4ececece2e2e2eaeaeae8c59a775d44904cc59b3bdf5d7eb1b808121086c3402036330d8620202a66060b6cd0ce222162047130c2c40842584ad690322eca01c0710e104e5b86a6a02002bd342f0a7d8094f0000000049454e44ae426082",
    )
val pngData33 =
    octets(
        "78016360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cfdb808d424ca6a6a6666666e6e6e6161616969696565656d6d6d6363636b6b6b6767676f6f6f60e0e0e8e8e8e4e4e4ececece2e2e2eaeaeae8c59a775d44904cc59b3bdf5d7eb1b808121086c3402036330d8620202a66060b6cd0ce222162047130c2c40842584ad690322eca01c0710e104e5b86a6a02002bd342f0",
    )
val pngData34 =
    octets(
        "89504e470d0a1a0a0000000d49484452000000110000000508020000002c48902000000001494441547876e684e6000000b0494441549c6360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cfdb808d424ca6a6a6666666e6e6e6161616969696565656d6d6d6363636b6b6b6767676f6f6f60e0e0e8e8e8e4e4e4ececece2e2e2eaeaeae8c59a775d44904cc59b3bdf5d7eb1b808121086c3402036330d8620202a66060b6cd0ce222162047130c2c40842584ad690322eca01c0710e104e5b86a6a02002bd342f05d4943640000000049454e44ae426082",
    )
val pngData35 =
    octets(
        "789c6360483ca4daf6dc6b354ffe05c3295fc3764a55dfb35fc09c7254a3f395ef3afea2cb26d37f44ee91ad7be8b4982dfd8476cfdb808d424ca6a6a6666666e6e6e6161616969696565656d6d6d6363636b6b6b6767676f6f6f60e0e0e8e8e8e4e4e4ececece2e2e2eaeaeae8c59a775d44904cc59b3bdf5d7eb1b808121086c3402036330d8620202a66060b6cd0ce222162047130c2c40842584ad690322eca01c0710e104e5b86a6a02002bd342f0",
    )
val pngData36 = octets("89")
val pngData37 = octets("89504e470d0a1a")
val pngData38 = octets("89504e470d0a1a0a")
val pngData39 = octets("89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aa")
val pngData40 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e60000002d4944415401012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023ff6743c170000000049454e44ae4260",
    )
val pngData41 =
    octets(
        "88504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e60000002d4944415401012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023ff6743c170000000049454e44ae426082",
    )
val pngData42 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e1d1aacb00000001494441547876e684e60000002d4944415401012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023ff6743c170000000049454e44ae426082",
    )
val pngData43 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e60000002d4944415401012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023ff6743c170000000049454e44ae42608278",
    )
val pngData44 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb0000000d4948445200000002000000050802000000e0d1aacb0000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData45 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e600000000744558749642c5850000002d4944415401012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023ff6743c170000000049454e44ae426082",
    )
val pngData46 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb0000000049454e44ae426082",
    )
val pngData47 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb000000004944415435af061e0000000049454e44ae426082",
    )
val pngData48 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb0000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000149454e44788fc4b6ef",
    )
val pngData49 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb0000000074524e5336b970cc0000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData50 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb000000006163544c49f2afe80000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData51 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb000000006663544cd42597510000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData52 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb0000000066644154f5b3ff960000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData53 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb000000004a554e4b306e718d0000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData54 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000000613143442d9259920000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData55 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb0000000061626364ed82cd110000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData56 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000201000000050802000000538aa8090000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData57 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000000802000000b01c3b780000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData58 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000051002000000b04176880000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData59 =
    octets(
        "89504e470d0a1a0a0000000d49484452000000020000000508000000004ad862400000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData60 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802010000e113c0fc0000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData61 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000100f9ca9b8a0000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData62 =
    octets(
        "89504e470d0a1a0a0000000d494844520000000200000005080200000197d69a5d0000002e494441547801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f6b8b96c20000000049454e44ae426082",
    )
val pngData63 = octets("000061c202353535016acb2c036a9b4b04353535")
val pngData64 = octets("000a141e28323c0205050505050501141e281e1e1e030f141912121204050505050505")
val pngData65 =
    octets(
        "000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c8023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63604353535292929292929382929292939292929292929293c292929293e2929292929292940292929294229292929",
    )
val pngData66 =
    octets(
        "000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343444444016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63636363604353535292929292929382929292939292929292929293c292929293e2929292929292940292929294229292929292929",
    )
val pngData67 =
    octets(
        "000061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced50b112023535353636363737373838383939393a3a3a3b3b3b3c3c3c3d3d3d3e3e3e3f3f3f404040414141424242434343444444454545016acb2c272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727272727036a9b4b2faf2f3030303030303131313131b1323232323232333333333333b4343434343435353535353536b63636363637373704353535292929292929382929292939292929292929293c292929293e2929292929292940292929294229292929292929452929",
    )
val pngData68 =
    octets("780163e01291d330b261626565656565651491d390939363e617911412126261656565656505002e0c023f")
val pngData69 = octets("780163200c0000230001")
val pngData70 = octets("0000000000000000000000000000000000000000000000000000000000000000000000")
val pngData71 =
    octets(
        "78010dc0d906000000c0a000000000000000000000000000000000000000000000000000000000000000005b000000000800230001",
    )
val pngData72 =
    octets(
        "78010dc0d906000000c0a0dc000000000000000000000000000000000000000000000000000000000000005b000000000800230001",
    )
val pngData73 =
    octets(
        "78010dc0d906000000c0a0de010000000000000000000000000000000000000000000000000000000000c016000000000200230001",
    )
val pngData74 = octets("78010dc0d906000000c0a0fe1f00000000000000000000000000006c010000002000230001")
val pngData75 = octets("780163200c0100230001")
val pngData76 = octets("78011b030000230001")
val pngData77 = octets("78016320fc0000230001")
val pngData78 = octets("78010dc0d906000000c0a00300230001")
val pngData79 =
    octets(
        "78010dc0d906000000c0a00200000000000000000000000000000000000000000000000000000000000000b60000230001",
    )
val pngData80 =
    octets(
        "78010dc0d906000000c0a000000000000000000000000000000000000000000000000000000000000000000a00230001",
    )
val pngData81 =
    octets(
        "78010dc0d906000000c0a000000000000000000000000000000000000000000000000000000000000000005b000000000800230000",
    )
val pngData82 =
    octets(
        "7801000700f8ff000a141e28323c63626565656565651491d390939363e617911412126261656565656505002e0c023f",
    )
val pngData83 = octets("78")
val pngData84 = octets("7801012300")
val pngData85 =
    octets(
        "7801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c02",
    )
val pngData86 = octets("789c73040000420042")
val pngData87 = octets("789c030000000001")
val pngData88 =
    octets(
        "7801012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212040505050505052e0c023f78",
    )
val pngData89 = octets("0061c23596f76acb2c9f0061d43596")
val pngData90 = octets("0a141e28323c0f19232d3741141e28323c4619232d37414b1e28323c4650")
val pngData91 =
    octets(
        "0061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c83596f75bbc1d81e243a70869cd2e8ff354b5197adb3fa00165c6278bec4db11273d73899fd5ebf2384e549aa0b6acb2c91f253b8197adf40a10667c82d8eef54b5167bdc3da20364c92a8bf051b21778d93e9f0065c6278ced4e9f0061c72889ef50b11778d93fa00167c8298ff051b71879df40a10768c92f90f157b8197fe041a70869cf3091d43596fd5ebf2687e84fb01178d93aa10263ca2b8cf354b51c7dde45a6076ecf3097f859c02182e94aab1273d4",
    )
val pngData92 =
    octets(
        "0061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced3596f75bbc1d81e243a70869cd2e8ff354b5197adb3fa00165c6278bec4db11273d73899fd5ebf2384e549aa0b6fd0316acb2c91f253b8197adf40a10667c82d8eef54b5167bdc3da20364c92a8bf051b21778d93e9f0065c6278ced4eb314759f0061c72889ef50b11778d93fa00167c8298ff051b71879df40a10768c92f90f157b8197fe041a70869cf3091f758b9d43596fd5ebf2687e84fb01178d93aa10263ca2b8cf354b51c7dde45a6076ecf3097f859c02182e94aab1273d43b9cfd",
    )
val pngData93 =
    octets(
        "0061c22586e74aab0c6fd03194f556b91a7bde3fa00364c52889ea4dae0f72d33497f859bc1d7ee142a30667c82b8ced50b1123596f75bbc1d81e243a70869cd2e8ff354b5197adb3fa00165c6278bec4db11273d73899fd5ebf2384e549aa0b6fd03195f6576acb2c91f253b8197adf40a10667c82d8eef54b5167bdc3da20364c92a8bf051b21778d93e9f0065c6278ced4eb31475da3b9c9f0061c72889ef50b11778d93fa00167c8298ff051b71879df40a10768c92f90f157b8197fe041a70869cf3091f758b91f80e1d43596fd5ebf2687e84fb01178d93aa10263ca2b8cf354b51c7dde45a6076ecf3097f859c02182e94aab1273d43b9cfd64c526",
    )
val pngData94 = octets("000a141e28323c0205050505050501141e281e1e1e030f141912121205050505050505")
val pngData95 =
    octets(
        "89504e470d0a1a0a0000000d4948445200000002000000050802000000e0d1aacb00000001494441547876e684e60000002d4944415401012300dcff000a141e28323c0205050505050501141e281e1e1e030f1419121212050505050505052e130240e03a6b370000000049454e44ae426082",
    )

case(
    "1x5-stored msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData6, pngData89, 1, 5, 0, false, false)
}

case(
    "1x5-fixed msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData8, pngData89, 1, 5, 0, false, false)
}

case(
    "1x5-auto msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData10, pngData89, 1, 5, 0, false, false)
}

case(
    "2x5-stored msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 0, false, false)
}

case(
    "2x5-fixed msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData14, pngData90, 2, 5, 0, false, false)
}

case(
    "2x5-auto msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData16, pngData90, 2, 5, 0, false, false)
}

case(
    "15x5-stored msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData18, pngData91, 15, 5, 0, false, false)
}

case(
    "15x5-fixed msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData20, pngData91, 15, 5, 0, false, false)
}

case(
    "15x5-auto msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData22, pngData91, 15, 5, 0, false, false)
}

case(
    "16x5-stored msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData24, pngData92, 16, 5, 0, false, false)
}

case(
    "16x5-fixed msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData26, pngData92, 16, 5, 0, false, false)
}

case(
    "16x5-auto msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData28, pngData92, 16, 5, 0, false, false)
}

case(
    "17x5-stored msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData30, pngData93, 17, 5, 0, false, false)
}

case(
    "17x5-fixed msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData32, pngData93, 17, 5, 0, false, false)
}

case(
    "17x5-auto msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData34, pngData93, 17, 5, 0, false, false)
}

case(
    "truncated-0 msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData0, pngData0, 2, 5, 1, false, false)
}

case(
    "truncated-1 msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData36, pngData0, 2, 5, 1, false, false)
}

case(
    "truncated-7 msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData37, pngData0, 2, 5, 1, false, false)
}

case(
    "truncated-8 msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData38, pngData0, 2, 5, 1, false, false)
}

case(
    "truncated-32 msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData39, pngData0, 2, 5, 1, false, false)
}

case(
    "truncated-114 msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData40, pngData0, 2, 5, 1, false, false)
}

case(
    "signature msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData41, pngData0, 2, 5, 1, false, false)
}

case(
    "outer-crc msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData42, pngData0, 2, 5, 1, false, false)
}

case(
    "file-suffix msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData43, pngData0, 2, 5, 1, false, false)
}

case(
    "duplicate-header msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData44, pngData0, 2, 5, 1, false, false)
}

case(
    "split-IDAT-run msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData45, pngData0, 2, 5, 1, false, false)
}

case(
    "no-IDAT msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData46, pngData0, 2, 5, 1, false, false)
}

case(
    "empty-IDAT msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData47, pngData0, 2, 5, 1, false, false)
}

case(
    "IEND-payload msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData48, pngData0, 2, 5, 1, false, false)
}

case(
    "unsupported-tRNS msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData49, pngData0, 2, 5, 1, false, false)
}

case(
    "unsupported-acTL msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData50, pngData0, 2, 5, 1, false, false)
}

case(
    "unsupported-fcTL msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData51, pngData0, 2, 5, 1, false, false)
}

case(
    "unsupported-fdAT msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData52, pngData0, 2, 5, 1, false, false)
}

case(
    "unsupported-JUNK msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData53, pngData0, 2, 5, 1, false, false)
}

case(
    "unsupported-a1CD msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData54, pngData0, 2, 5, 1, false, false)
}

case(
    "unsupported-abcd msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData55, pngData0, 2, 5, 1, false, false)
}

case(
    "header-(513, 5, 8, 2, 0, 0, 0) msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData56, pngData0, 2, 5, 1, false, false)
}

case(
    "header-(2, 0, 8, 2, 0, 0, 0) msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData57, pngData0, 2, 5, 1, false, false)
}

case(
    "header-(2, 5, 16, 2, 0, 0, 0) msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData58, pngData0, 2, 5, 1, false, false)
}

case(
    "header-(2, 5, 8, 0, 0, 0, 0) msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData59, pngData0, 2, 5, 1, false, false)
}

case(
    "header-(2, 5, 8, 2, 1, 0, 0) msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData60, pngData0, 2, 5, 1, false, false)
}

case(
    "header-(2, 5, 8, 2, 0, 1, 0) msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData61, pngData0, 2, 5, 1, false, false)
}

case(
    "header-(2, 5, 8, 2, 0, 0, 1) msa=False little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData62, pngData0, 2, 5, 1, false, false)
}

case(
    "late-filterFalse little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData95, pngData90, 2, 5, 1, false, false)
}

case(
    "short-rowsFalse little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, false, false, rowcap = 34)
}

case(
    "short-compressedFalse little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, false, false, compcap = 45)
}

case(
    "short-frameFalse little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, false, false, framecap = 87)
}

case(
    "1x5-stored msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData6, pngData89, 1, 5, 0, true, false)
}

case(
    "1x5-fixed msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData8, pngData89, 1, 5, 0, true, false)
}

case(
    "1x5-auto msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData10, pngData89, 1, 5, 0, true, false)
}

case(
    "2x5-stored msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 0, true, false)
}

case(
    "2x5-fixed msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData14, pngData90, 2, 5, 0, true, false)
}

case(
    "2x5-auto msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData16, pngData90, 2, 5, 0, true, false)
}

case(
    "15x5-stored msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData18, pngData91, 15, 5, 0, true, false)
}

case(
    "15x5-fixed msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData20, pngData91, 15, 5, 0, true, false)
}

case(
    "15x5-auto msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData22, pngData91, 15, 5, 0, true, false)
}

case(
    "16x5-stored msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData24, pngData92, 16, 5, 0, true, false)
}

case(
    "16x5-fixed msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData26, pngData92, 16, 5, 0, true, false)
}

case(
    "16x5-auto msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData28, pngData92, 16, 5, 0, true, false)
}

case(
    "17x5-stored msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData30, pngData93, 17, 5, 0, true, false)
}

case(
    "17x5-fixed msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData32, pngData93, 17, 5, 0, true, false)
}

case(
    "17x5-auto msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData34, pngData93, 17, 5, 0, true, false)
}

case(
    "truncated-0 msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData0, pngData0, 2, 5, 1, true, false)
}

case(
    "truncated-1 msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData36, pngData0, 2, 5, 1, true, false)
}

case(
    "truncated-7 msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData37, pngData0, 2, 5, 1, true, false)
}

case(
    "truncated-8 msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData38, pngData0, 2, 5, 1, true, false)
}

case(
    "truncated-32 msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData39, pngData0, 2, 5, 1, true, false)
}

case(
    "truncated-114 msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData40, pngData0, 2, 5, 1, true, false)
}

case(
    "signature msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData41, pngData0, 2, 5, 1, true, false)
}

case(
    "outer-crc msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData42, pngData0, 2, 5, 1, true, false)
}

case(
    "file-suffix msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData43, pngData0, 2, 5, 1, true, false)
}

case(
    "duplicate-header msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData44, pngData0, 2, 5, 1, true, false)
}

case(
    "split-IDAT-run msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData45, pngData0, 2, 5, 1, true, false)
}

case(
    "no-IDAT msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData46, pngData0, 2, 5, 1, true, false)
}

case(
    "empty-IDAT msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData47, pngData0, 2, 5, 1, true, false)
}

case(
    "IEND-payload msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData48, pngData0, 2, 5, 1, true, false)
}

case(
    "unsupported-tRNS msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData49, pngData0, 2, 5, 1, true, false)
}

case(
    "unsupported-acTL msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData50, pngData0, 2, 5, 1, true, false)
}

case(
    "unsupported-fcTL msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData51, pngData0, 2, 5, 1, true, false)
}

case(
    "unsupported-fdAT msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData52, pngData0, 2, 5, 1, true, false)
}

case(
    "unsupported-JUNK msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData53, pngData0, 2, 5, 1, true, false)
}

case(
    "unsupported-a1CD msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData54, pngData0, 2, 5, 1, true, false)
}

case(
    "unsupported-abcd msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData55, pngData0, 2, 5, 1, true, false)
}

case(
    "header-(513, 5, 8, 2, 0, 0, 0) msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData56, pngData0, 2, 5, 1, true, false)
}

case(
    "header-(2, 0, 8, 2, 0, 0, 0) msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData57, pngData0, 2, 5, 1, true, false)
}

case(
    "header-(2, 5, 16, 2, 0, 0, 0) msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData58, pngData0, 2, 5, 1, true, false)
}

case(
    "header-(2, 5, 8, 0, 0, 0, 0) msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData59, pngData0, 2, 5, 1, true, false)
}

case(
    "header-(2, 5, 8, 2, 1, 0, 0) msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData60, pngData0, 2, 5, 1, true, false)
}

case(
    "header-(2, 5, 8, 2, 0, 1, 0) msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData61, pngData0, 2, 5, 1, true, false)
}

case(
    "header-(2, 5, 8, 2, 0, 0, 1) msa=True little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData62, pngData0, 2, 5, 1, true, false)
}

case(
    "late-filterTrue little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData95, pngData90, 2, 5, 1, true, false)
}

case("short-rowsTrue little=False", instructionLimit = 30000000, byteOrder = ByteOrder.BIG_ENDIAN) {
  decode(pngData12, pngData90, 2, 5, 1, true, false, rowcap = 34)
}

case(
    "short-compressedTrue little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, true, false, compcap = 45)
}

case(
    "short-frameTrue little=False",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, true, false, framecap = 87)
}

case(
    "1x5-stored msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData6, pngData89, 1, 5, 0, false, true)
}

case(
    "1x5-fixed msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData8, pngData89, 1, 5, 0, false, true)
}

case(
    "1x5-auto msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData10, pngData89, 1, 5, 0, false, true)
}

case(
    "2x5-stored msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 0, false, true)
}

case(
    "2x5-fixed msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData14, pngData90, 2, 5, 0, false, true)
}

case(
    "2x5-auto msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData16, pngData90, 2, 5, 0, false, true)
}

case(
    "15x5-stored msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData18, pngData91, 15, 5, 0, false, true)
}

case(
    "15x5-fixed msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData20, pngData91, 15, 5, 0, false, true)
}

case(
    "15x5-auto msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData22, pngData91, 15, 5, 0, false, true)
}

case(
    "16x5-stored msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData24, pngData92, 16, 5, 0, false, true)
}

case(
    "16x5-fixed msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData26, pngData92, 16, 5, 0, false, true)
}

case(
    "16x5-auto msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData28, pngData92, 16, 5, 0, false, true)
}

case(
    "17x5-stored msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData30, pngData93, 17, 5, 0, false, true)
}

case(
    "17x5-fixed msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData32, pngData93, 17, 5, 0, false, true)
}

case(
    "17x5-auto msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData34, pngData93, 17, 5, 0, false, true)
}

case(
    "truncated-0 msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData0, pngData0, 2, 5, 1, false, true)
}

case(
    "truncated-1 msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData36, pngData0, 2, 5, 1, false, true)
}

case(
    "truncated-7 msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData37, pngData0, 2, 5, 1, false, true)
}

case(
    "truncated-8 msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData38, pngData0, 2, 5, 1, false, true)
}

case(
    "truncated-32 msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData39, pngData0, 2, 5, 1, false, true)
}

case(
    "truncated-114 msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData40, pngData0, 2, 5, 1, false, true)
}

case(
    "signature msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData41, pngData0, 2, 5, 1, false, true)
}

case(
    "outer-crc msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData42, pngData0, 2, 5, 1, false, true)
}

case(
    "file-suffix msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData43, pngData0, 2, 5, 1, false, true)
}

case(
    "duplicate-header msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData44, pngData0, 2, 5, 1, false, true)
}

case(
    "split-IDAT-run msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData45, pngData0, 2, 5, 1, false, true)
}

case(
    "no-IDAT msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData46, pngData0, 2, 5, 1, false, true)
}

case(
    "empty-IDAT msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData47, pngData0, 2, 5, 1, false, true)
}

case(
    "IEND-payload msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData48, pngData0, 2, 5, 1, false, true)
}

case(
    "unsupported-tRNS msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData49, pngData0, 2, 5, 1, false, true)
}

case(
    "unsupported-acTL msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData50, pngData0, 2, 5, 1, false, true)
}

case(
    "unsupported-fcTL msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData51, pngData0, 2, 5, 1, false, true)
}

case(
    "unsupported-fdAT msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData52, pngData0, 2, 5, 1, false, true)
}

case(
    "unsupported-JUNK msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData53, pngData0, 2, 5, 1, false, true)
}

case(
    "unsupported-a1CD msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData54, pngData0, 2, 5, 1, false, true)
}

case(
    "unsupported-abcd msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData55, pngData0, 2, 5, 1, false, true)
}

case(
    "header-(513, 5, 8, 2, 0, 0, 0) msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData56, pngData0, 2, 5, 1, false, true)
}

case(
    "header-(2, 0, 8, 2, 0, 0, 0) msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData57, pngData0, 2, 5, 1, false, true)
}

case(
    "header-(2, 5, 16, 2, 0, 0, 0) msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData58, pngData0, 2, 5, 1, false, true)
}

case(
    "header-(2, 5, 8, 0, 0, 0, 0) msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData59, pngData0, 2, 5, 1, false, true)
}

case(
    "header-(2, 5, 8, 2, 1, 0, 0) msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData60, pngData0, 2, 5, 1, false, true)
}

case(
    "header-(2, 5, 8, 2, 0, 1, 0) msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData61, pngData0, 2, 5, 1, false, true)
}

case(
    "header-(2, 5, 8, 2, 0, 0, 1) msa=False little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData62, pngData0, 2, 5, 1, false, true)
}

case(
    "late-filterFalse little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData95, pngData90, 2, 5, 1, false, true)
}

case(
    "short-rowsFalse little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, false, true, rowcap = 34)
}

case(
    "short-compressedFalse little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, false, true, compcap = 45)
}

case(
    "short-frameFalse little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, false, true, framecap = 87)
}

case(
    "1x5-stored msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData6, pngData89, 1, 5, 0, true, true)
}

case(
    "1x5-fixed msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData8, pngData89, 1, 5, 0, true, true)
}

case(
    "1x5-auto msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData10, pngData89, 1, 5, 0, true, true)
}

case(
    "2x5-stored msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 0, true, true)
}

case(
    "2x5-fixed msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData14, pngData90, 2, 5, 0, true, true)
}

case(
    "2x5-auto msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData16, pngData90, 2, 5, 0, true, true)
}

case(
    "15x5-stored msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData18, pngData91, 15, 5, 0, true, true)
}

case(
    "15x5-fixed msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData20, pngData91, 15, 5, 0, true, true)
}

case(
    "15x5-auto msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData22, pngData91, 15, 5, 0, true, true)
}

case(
    "16x5-stored msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData24, pngData92, 16, 5, 0, true, true)
}

case(
    "16x5-fixed msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData26, pngData92, 16, 5, 0, true, true)
}

case(
    "16x5-auto msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData28, pngData92, 16, 5, 0, true, true)
}

case(
    "17x5-stored msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData30, pngData93, 17, 5, 0, true, true)
}

case(
    "17x5-fixed msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData32, pngData93, 17, 5, 0, true, true)
}

case(
    "17x5-auto msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData34, pngData93, 17, 5, 0, true, true)
}

case(
    "truncated-0 msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData0, pngData0, 2, 5, 1, true, true)
}

case(
    "truncated-1 msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData36, pngData0, 2, 5, 1, true, true)
}

case(
    "truncated-7 msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData37, pngData0, 2, 5, 1, true, true)
}

case(
    "truncated-8 msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData38, pngData0, 2, 5, 1, true, true)
}

case(
    "truncated-32 msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData39, pngData0, 2, 5, 1, true, true)
}

case(
    "truncated-114 msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData40, pngData0, 2, 5, 1, true, true)
}

case(
    "signature msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData41, pngData0, 2, 5, 1, true, true)
}

case(
    "outer-crc msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData42, pngData0, 2, 5, 1, true, true)
}

case(
    "file-suffix msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData43, pngData0, 2, 5, 1, true, true)
}

case(
    "duplicate-header msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData44, pngData0, 2, 5, 1, true, true)
}

case(
    "split-IDAT-run msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData45, pngData0, 2, 5, 1, true, true)
}

case(
    "no-IDAT msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData46, pngData0, 2, 5, 1, true, true)
}

case(
    "empty-IDAT msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData47, pngData0, 2, 5, 1, true, true)
}

case(
    "IEND-payload msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData48, pngData0, 2, 5, 1, true, true)
}

case(
    "unsupported-tRNS msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData49, pngData0, 2, 5, 1, true, true)
}

case(
    "unsupported-acTL msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData50, pngData0, 2, 5, 1, true, true)
}

case(
    "unsupported-fcTL msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData51, pngData0, 2, 5, 1, true, true)
}

case(
    "unsupported-fdAT msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData52, pngData0, 2, 5, 1, true, true)
}

case(
    "unsupported-JUNK msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData53, pngData0, 2, 5, 1, true, true)
}

case(
    "unsupported-a1CD msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData54, pngData0, 2, 5, 1, true, true)
}

case(
    "unsupported-abcd msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData55, pngData0, 2, 5, 1, true, true)
}

case(
    "header-(513, 5, 8, 2, 0, 0, 0) msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData56, pngData0, 2, 5, 1, true, true)
}

case(
    "header-(2, 0, 8, 2, 0, 0, 0) msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData57, pngData0, 2, 5, 1, true, true)
}

case(
    "header-(2, 5, 16, 2, 0, 0, 0) msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData58, pngData0, 2, 5, 1, true, true)
}

case(
    "header-(2, 5, 8, 0, 0, 0, 0) msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData59, pngData0, 2, 5, 1, true, true)
}

case(
    "header-(2, 5, 8, 2, 1, 0, 0) msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData60, pngData0, 2, 5, 1, true, true)
}

case(
    "header-(2, 5, 8, 2, 0, 1, 0) msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData61, pngData0, 2, 5, 1, true, true)
}

case(
    "header-(2, 5, 8, 2, 0, 0, 1) msa=True little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData62, pngData0, 2, 5, 1, true, true)
}

case(
    "late-filterTrue little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData95, pngData90, 2, 5, 1, true, true)
}

case(
    "short-rowsTrue little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, true, true, rowcap = 34)
}

case(
    "short-compressedTrue little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, true, true, compcap = 45)
}

case(
    "short-frameTrue little=True",
    instructionLimit = 30000000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  decode(pngData12, pngData90, 2, 5, 1, true, true, framecap = 87)
}
