import edu.missouristate.mars.cli.grading.AssemblyCase
import java.util.Base64

val encoder = Base64.getEncoder()
val decoder = Base64.getDecoder()
val wholeEntries =
    listOf("b64_encode_scalar", "b64_encode_msa", "b64_decode_scalar", "b64_decode_msa")

fun strictDecode(input: ByteArray): ByteArray? {
  if (input.size % 4 != 0) return null
  return try {
    val raw = decoder.decode(input)
    if (encoder.encode(raw).contentEquals(input)) raw else null
  } catch (_: IllegalArgumentException) {
    null
  }
}

fun pattern(n: Int, salt: Int = 129) = ByteArray(n) { (it * 73 + salt).toByte() }

fun AssemblyCase.putArena(name: String, data: ByteArray) {
  require(data.size % 4 == 0)
  for (i in data.indices step 4) setWord(
      name,
      (0..3).fold(0) { v, j -> v or ((data[i + j].toInt() and 255) shl (8 * j)) },
      i,
  )
}

fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n

fun AssemblyCase.setup(input: ByteArray, offset: Int): List<Long> {
  val source = ByteArray(8192) { 165.toByte() }
  input.copyInto(source, offset)
  putArena("source", source)
  putArena("destination", ByteArray(8192) { 165.toByte() })
  setWord("status", -1)
  for (i in 0 until 4096 step 4) setWord("workspace", 0xa5a5a5a5.toInt(), i)
  val saved = List(9) { 0x5566778800000000L + it * 977 }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val top = readDataAddress("workspace", 4096)
  setRegister("sp", top)
  for (i in 0..9) setRegister("t$i", -999L - i)
  return saved + top
}

fun AssemblyCase.preserved(saved: List<Long>) {
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", saved[9])
  expectOutput("")
}

fun AssemblyCase.observe(
    reads: List<Pair<Long, Int>>,
    writes: List<Pair<Long, Int>>,
    action: () -> Unit,
) {
  val objects =
      listOf(
              "source" to 8192,
              "destination" to 8192,
              "mode" to 4,
              "in_place" to 4,
              "count" to 8,
              "capacity" to 8,
              "status" to 4,
              "written" to 8,
          )
          .map { (name, size) -> Triple(name, readDataAddress(name), size) }
  val old = objects.map { (name, _, size) -> readBytes(name, size) }
  val lo = objects.minOf { it.second }
  val hi = objects.maxOf { it.second + it.third }
  val trace = traceMemoryAccesses(lo, (hi - lo).toInt(), maxAccesses = 200000)
  action()
  trace.close()
  for (x in trace.snapshot()) if (
      objects.any { (_, b, n) -> x.address < b + n && b < x.address + x.length }
  )
      expectCondition(
          (if (x.write) writes else reads).any { (b, n) -> fits(x.address, x.length, b, n) },
          "Every caller access belongs to a selected readable or writable interval",
      )
  for ((i, obj) in objects.withIndex()) {
    val (name, base, size) = obj
    val after = readBytes(name, size)
    for (j in after.indices) if (writes.none { (b, n) -> fits(base + j, 1, b, n) })
        expectCondition(
            after[j] == old[i][j],
            "Caller bytes outside selected output remain unchanged",
        )
  }
}

fun AssemblyCase.runCodec(
    entry: String,
    input: ByteArray,
    index: Int = 0,
    capacity: Long = 8192,
    count: Long = input.size.toLong(),
    inPlace: Boolean = false,
    badSource: Boolean = false,
    badDestination: Boolean = false,
    measure: Boolean = false,
) {
  val encode = entry.contains("encode")
  val so = index % 19
  val dest = if (inPlace) "source" else "destination"
  val offset = if (inPlace) so else (index * 3 + 1) % 23
  val saved = setup(input, so)
  val limit = if (encode) 4096 else 5464
  val text =
      if (!encode && count >= 0 && count <= limit && count.toInt() == input.size)
          strictDecode(input)
      else null
  val want = if (encode) encoder.encode(input) else text
  val expectedStatus =
      if (count.toULong() > limit.toULong()) 1
      else if (count == 0L) 0
      else if (!encode && text == null) 2
      else if (capacity.toULong() < (want?.size ?: 0).toULong()) 3 else 0
  val expectedBytes = if (expectedStatus == 0 && count != 0L) want!! else byteArrayOf()
  setRegister("a0", if (badSource) 1 else readDataAddress("source", so.toLong()))
  setRegister("a1", count)
  setRegister("a2", if (badDestination) 1 else readDataAddress(dest, offset.toLong()))
  setRegister("a3", capacity)
  val src = readDataAddress("source", so.toLong())
  val out = readDataAddress(dest, offset.toLong())
  val readInput =
      count != 0L &&
          count.toULong() <= limit.toULong() &&
          !(encode && expectedStatus == 3) &&
          !(!encode && count.and(3) != 0L)
  val reads = if (readInput) listOf(src to count.toInt()) else emptyList()
  val writes = if (expectedBytes.isNotEmpty()) listOf(out to expectedBytes.size) else emptyList()
  val work = traceExecution()
  observe(reads + writes, writes) {
    callFunction(entry, "function_return")
    expectRegister("v0", expectedStatus)
    expectRegister("v1", expectedBytes.size)
    preserved(saved)
  }
  val instructions = work.snapshot().instructions
  work.close()
  if (expectedBytes.isNotEmpty())
      expectCondition(
          readBytes(dest, expectedBytes.size, offset)
              .contentEquals(expectedBytes.map { it.toInt() and 255 }.toIntArray()),
          "All bytes agree with independent canonical Base64",
      )
  if (measure) println("WORK,$entry,${input.size},$instructions")
}

fun AssemblyCase.runKernel(
    encode: Boolean,
    input: ByteArray,
    index: Int = 0,
    inPlace: Boolean = false,
) {
  require(input.size == if (encode) 12 else 16)
  val wanted = if (encode) encoder.encode(input) else strictDecode(input)!!
  require(wanted.size == if (encode) 16 else 12)
  val so = index % 19
  val dest = if (inPlace) "source" else "destination"
  val off = if (inPlace) so else (index * 7 + 3) % 23
  val saved = setup(input, so)
  setRegisterToDataAddress("a0", "source", so.toLong())
  setRegisterToDataAddress("a1", dest, off.toLong())
  val src = readDataAddress("source", so.toLong())
  val out = readDataAddress(dest, off.toLong())
  val trace = traceExecution()
  observe(listOf(src to input.size, out to wanted.size), listOf(out to wanted.size)) {
    callFunction(if (encode) "b64_encode12_msa" else "b64_decode16_msa", "function_return")
    expectRegister("v0", 0)
    expectRegister("v1", wanted.size)
    preserved(saved)
  }
  val instructions = trace.snapshot().instructions
  trace.close()
  expectCondition(
      readBytes(dest, wanted.size, off)
          .contentEquals(wanted.map { it.toInt() and 255 }.toIntArray()),
      "Kernel bytes match the independent transform",
  )
  if (index == 0) println("WORK,kernel,$encode,$instructions")
}

for (i in 0 until 48) {
  val raw = pattern(12, 17 + i * 11)
  case("encode twelve $i") { runKernel(true, raw, i) }
  case("decode sixteen $i") { runKernel(false, encoder.encode(raw), i, inPlace = i % 2 == 1) }
}

val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toByteArray()

for (start in 0..48 step 16) case("kernel alphabet $start") {
  runKernel(false, alphabet.copyOfRange(start, start + 16), start)
}
