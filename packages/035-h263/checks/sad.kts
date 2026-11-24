// Public completed-SAD observations; no source matching or exact-count acceptance.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import kotlin.math.abs

fun AssemblyCase.put(name: String, bytes: ByteArray, little: Boolean) {
  for (i in bytes.indices step 4) {
    var value = 0
    for (j in 0..3) value =
        value or ((bytes[i + j].toInt() and 255) shl (8 * (if (little) j else 3 - j)))
    if (value != -1515870811) setWord(name, value, i)
  }
}

fun AssemblyCase.check(
    entry: String,
    leftHex: String,
    rightHex: String,
    stride: Long,
    other: Long,
    alias: Int,
    status: Int,
    wrap: Int,
    little: Boolean,
) {
  val a = ByteArray(61488) { 165.toByte() }
  val b = ByteArray(61488) { 165.toByte() }
  val activeA = leftHex.chunked(2).map { it.toInt(16).toByte() }
  val activeB = rightHex.chunked(2).map { it.toInt(16).toByte() }
  if (status == 0)
      for (y in 0..15) for (x in 0..15) {
        a[17 + y * stride.toInt() + x] = activeA[16 * y + x]
        b[31 + y * other.toInt() + x] = activeB[16 * y + x]
      }
  put("h263Check_left", a, little)
  put("h263Check_right", b, little)
  val base = readDataAddress("h263Check_left")
  val left = if (wrap == 1) -8L else base + 17
  val right =
      if (wrap == 2) -8L
      else if (alias >= 0) base + 17 + alias else readDataAddress("h263Check_right") + 31
  val expected =
      if (status != 0) 0L
      else
          (0..15).sumOf { y ->
            (0..15).sumOf { x ->
              abs(
                      (a[17 + y * stride.toInt() + x].toInt() and 255) -
                          (if (alias >= 0) a[17 + alias + y * other.toInt() + x].toInt() and 255
                          else b[31 + y * other.toInt() + x].toInt() and 255),
                  )
                  .toLong()
            }
          }
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, name) in saved.withIndex()) setRegister(name, 0x1234567800000000L + i)
  setRegister("sp", readDataAddress("h263Check_stack", 16400))
  val stack = readRegister("sp")
  val accesses = traceMemoryAccesses(base, 122976)
  val work = traceExecution()
  setRegister("a0", left)
  setRegister("a1", right)
  setRegister("a2", stride)
  setRegister("a3", other)
  callFunction(entry, "h263Check_return")
  expectRegister("v0", status)
  expectRegister("v1", expected)
  for ((i, name) in saved.withIndex()) expectRegister(name, 0x1234567800000000L + i)
  expectRegister("sp", stack)
  fun inside(address: Long, count: Int, start: Long, rowStride: Long): Boolean {
    if (address < start || rowStride <= 0) return false
    val relative = address - start
    return relative / rowStride < 16 && relative % rowStride + count <= 16
  }
  for (event in accesses.snapshot()) expectCondition(
      status == 0 &&
          !event.write &&
          (inside(event.address, event.length, left, stride) ||
              inside(event.address, event.length, right, other)),
      "Read only active samples; never padding, a seventeenth byte or output memory",
  )
  if (status == 1)
      expectCondition(accesses.snapshot().isEmpty(), "Rejected arguments do not read either input")
  expectCondition(
      readBytes("h263Check_left", 61488).contentEquals(a.map { it.toInt() and 255 }.toIntArray()),
      "Left source and all guards unchanged",
  )
  expectCondition(
      readBytes("h263Check_right", 61488).contentEquals(b.map { it.toInt() and 255 }.toIntArray()),
      "Right source and all guards unchanged",
  )
  expectCondition(
      readBytes("h263Check_stack", 16).all { it == 165 } &&
          readBytes("h263Check_stack", 16, 16400).all { it == 165 },
      "Private stack outer guards retained",
  )
  if (status == 0 && stride == 176L && other == 176L && alias == -1)
      println("SAD_WORK entry=$entry sum=$expected instructions=${work.snapshot().instructions}")
}

case("SAD 0 h263_sad16 little=False", instructionLimit = 100000, byteOrder = ByteOrder.BIG_ENDIAN) {
  check(
      "h263_sad16",
      "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 0 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 0 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case(
    "SAD 0 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case("SAD 1 h263_sad16 little=False", instructionLimit = 100000, byteOrder = ByteOrder.BIG_ENDIAN) {
  check(
      "h263_sad16",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 1 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 1 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case(
    "SAD 1 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case("SAD 2 h263_sad16 little=False", instructionLimit = 100000, byteOrder = ByteOrder.BIG_ENDIAN) {
  check(
      "h263_sad16",
      "00ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff00",
      "ff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ff",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 2 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "00ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff00",
      "ff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ff",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 2 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "00ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff00",
      "ff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ff",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case(
    "SAD 2 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "00ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff00",
      "ff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ffff00ff00ff00ff00ff00ff00ff00ff0000ff00ff00ff00ff00ff00ff00ff00ff",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case("SAD 3 h263_sad16 little=False", instructionLimit = 100000, byteOrder = ByteOrder.BIG_ENDIAN) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 3 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      16L,
      16L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 3 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case(
    "SAD 3 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      16L,
      16L,
      -1,
      0,
      0,
      true,
  )
}

case(
    "SAD 14 h263_sad16 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      176L,
      176L,
      0,
      0,
      0,
      false,
  )
}

case(
    "SAD 14 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      176L,
      176L,
      0,
      0,
      0,
      false,
  )
}

case(
    "SAD 14 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      176L,
      176L,
      0,
      0,
      0,
      true,
  )
}

case(
    "SAD 14 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      176L,
      176L,
      0,
      0,
      0,
      true,
  )
}

case(
    "SAD 16 h263_sad16 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16",
      "38383838383838384444444444444444383838383838383844444444444444443838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      "c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      176L,
      176L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 16 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "38383838383838384444444444444444383838383838383844444444444444443838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      "c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      176L,
      176L,
      -1,
      0,
      0,
      false,
  )
}

case(
    "SAD 16 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "38383838383838384444444444444444383838383838383844444444444444443838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      "c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      176L,
      176L,
      -1,
      0,
      0,
      true,
  )
}

case(
    "SAD 16 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "38383838383838384444444444444444383838383838383844444444444444443838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c83838c8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c85c5cc8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      "c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8c8",
      176L,
      176L,
      -1,
      0,
      0,
      true,
  )
}

case(
    "SAD 17 h263_sad16 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      0L,
      16L,
      -1,
      1,
      0,
      false,
  )
}

case(
    "SAD 17 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      0L,
      16L,
      -1,
      1,
      0,
      false,
  )
}

case(
    "SAD 17 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      0L,
      16L,
      -1,
      1,
      0,
      true,
  )
}

case(
    "SAD 17 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      0L,
      16L,
      -1,
      1,
      0,
      true,
  )
}

case(
    "SAD 18 h263_sad16 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      15L,
      16L,
      -1,
      1,
      0,
      false,
  )
}

case(
    "SAD 18 h263_sad16_msa little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      15L,
      16L,
      -1,
      1,
      0,
      false,
  )
}

case(
    "SAD 18 h263_sad16 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      15L,
      16L,
      -1,
      1,
      0,
      true,
  )
}

case(
    "SAD 18 h263_sad16_msa little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  check(
      "h263_sad16_msa",
      "001f3e5d7c9bbad9f81736557493b2d111304f6e8daccbea0928476685a4c3e22241607f9ebddcfb1a39587796b5d4f333527190afceed0c2b4a6988a7c6e504446382a1c0dffe1d3c5b7a99b8d7f615557493b2d1f00f2e4d6c8baac9e807266685a4c3e201203f5e7d9cbbdaf918377796b5d4f31231506f8eadcceb0a294888a7c6e504234261809fbeddfc1b3a5999b8d7f61534537291b0cfee0d2c4b6aaac9e80726456483a2c1e0ff1e3d5c7bbbdaf91837567594b3d2f1102f4e6d8ccceb0a29486786a5c4e30221405f7e9dddfc1b3a597897b6d5f4133251708faeee0d2c4b6a89a8c7e60524436281a0bfff1e3d5c7b9ab9d8f71635547392b1d0",
      "05121f2c394653606d7a8794a1aebbc834414e5b6875828f9ca9b6c3d0ddeaf763707d8a97a4b1becbd8e5f2ff0c1926929facb9c6d3e0edfa0714212e3b4855c1cedbe8f5020f1c293643505d6a7784f0fd0a1724313e4b5865727f8c99a6b31f2c394653606d7a8794a1aebbc8d5e24e5b6875828f9ca9b6c3d0ddeaf704117d8a97a4b1becbd8e5f2ff0c19263340acb9c6d3e0edfa0714212e3b4855626fdbe8f5020f1c293643505d6a7784919e0a1724313e4b5865727f8c99a6b3c0cd394653606d7a8794a1aebbc8d5e2effc6875828f9ca9b6c3d0ddeaf704111e2b97a4b1becbd8e5f2ff0c192633404d5ac6d3e0edfa0714212e3b4855626f7c89",
      15L,
      16L,
      -1,
      1,
      0,
      true,
  )
}
