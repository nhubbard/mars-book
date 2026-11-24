fun octets(s: String) = IntArray(s.length / 2) { s.substring(2 * it, 2 * it + 2).toInt(16) }

fun arena(s: String, offset: Int = 0) =
    IntArray(8192) { 165 }.also { octets(s).copyInto(it, offset) }

case("s29-1 offset function", instructionLimit = 2000000) {
  val initialSource = arena("0196b3d3dfbf033d3dfbf65a", 3)
  val initialDestination = arena("")
  for ((label, values) in
      listOf("source" to initialSource, "destination" to initialDestination)) for (i in
      values.indices step 4) setWord(
      label,
      (0..3).fold(0) { v, j -> v or (values[i + j] shl (8 * j)) },
      i,
  )
  val saved = List(9) { 0x5566778800000000L + it * 977 }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val top = readDataAddress("workspace", 4096)
  setRegister("sp", top)
  for (i in 0 until 4096 step 4) setWord("workspace", 0xa5a5a5a5.toInt(), i)
  setWord("mode", 4.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 12.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  setRegisterToDataAddress("a0", "source", 3L)
  setRegisterToDataAddress("a1", "destination", 5L)
  setRegisterToTextAddress("ra", "function_return")
  startAt("b64_encode12_msa")
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
          .map { (label, size) -> Triple(label, readDataAddress(label), size) }
  val before = objects.map { (label, _, size) -> readBytes(label, size) }
  val lo = objects.minOf { it.second }
  val hi = objects.maxOf { it.second + it.third }
  val trace = traceMemoryAccesses(lo, (hi - lo).toInt(), maxAccesses = 200000)
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 3L) to 12)
  val writes: List<Pair<Long, Int>> = emptyList()
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  runUntil("after_enc12_upper")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 16)
  expectVectorLane("w0", 0, 8, 1L)
  expectVectorLane("w0", 1, 8, 150L)
  expectVectorLane("w0", 2, 8, 179L)
  expectVectorLane("w0", 3, 8, 211L)
  expectVectorLane("w0", 4, 8, 223L)
  expectVectorLane("w0", 5, 8, 191L)
  expectVectorLane("w0", 6, 8, 3L)
  expectVectorLane("w0", 7, 8, 61L)
  expectVectorLane("w0", 8, 8, 61L)
  expectVectorLane("w0", 9, 8, 251L)
  expectVectorLane("w0", 10, 8, 246L)
  expectVectorLane("w0", 11, 8, 90L)
  expectVectorLane("w0", 12, 8, 0L)
  expectVectorLane("w0", 13, 8, 0L)
  expectVectorLane("w0", 14, 8, 0L)
  expectVectorLane("w0", 15, 8, 0L)
  expectVectorLane("w1", 0, 8, 0L)
  expectVectorLane("w1", 1, 8, 25L)
  expectVectorLane("w1", 2, 8, 26L)
  expectVectorLane("w1", 3, 8, 51L)
  expectVectorLane("w1", 4, 8, 52L)
  expectVectorLane("w1", 5, 8, 61L)
  expectVectorLane("w1", 6, 8, 62L)
  expectVectorLane("w1", 7, 8, 63L)
  expectVectorLane("w1", 8, 8, 0L)
  expectVectorLane("w1", 9, 8, 51L)
  expectVectorLane("w1", 10, 8, 52L)
  expectVectorLane("w1", 11, 8, 61L)
  expectVectorLane("w1", 12, 8, 62L)
  expectVectorLane("w1", 13, 8, 63L)
  expectVectorLane("w1", 14, 8, 25L)
  expectVectorLane("w1", 15, 8, 26L)
  expectVectorLane("w4", 0, 8, 0L)
  expectVectorLane("w4", 1, 8, 25L)
  expectVectorLane("w4", 2, 8, 32L)
  expectVectorLane("w4", 3, 8, 57L)
  expectVectorLane("w4", 4, 8, 58L)
  expectVectorLane("w4", 5, 8, 67L)
  expectVectorLane("w4", 6, 8, 68L)
  expectVectorLane("w4", 7, 8, 69L)
  expectVectorLane("w4", 8, 8, 0L)
  expectVectorLane("w4", 9, 8, 57L)
  expectVectorLane("w4", 10, 8, 58L)
  expectVectorLane("w4", 11, 8, 67L)
  expectVectorLane("w4", 12, 8, 68L)
  expectVectorLane("w4", 13, 8, 69L)
  expectVectorLane("w4", 14, 8, 25L)
  expectVectorLane("w4", 15, 8, 32L)
  expectCondition(
      readBytes(top - 16 + 0, 16).contentEquals(octets("0196b3d3dfbf033d3dfbf65a00000000")),
      "Private phase bytes at offset0",
  )
  runUntil("function_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 4)
  expectRegister("v1", 0)
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", top)
  expectOutput("")
  trace.close()
  for (x in trace.snapshot()) if (
      objects.any { (_, b, size) -> x.address < b + size && b < x.address + x.length }
  )
      expectCondition(
          (if (x.write) writes else reads + writes).any { (b, size) ->
            fits(x.address, x.length, b, size)
          },
          "Selected caller access interval",
      )
  for ((i, obj) in objects.withIndex()) {
    val (label, base, size) = obj
    val after = readBytes(label, size)
    for (j in after.indices) if (writes.none { (b, size) -> fits(base + j, 1, b, size) })
        expectCondition(after[j] == before[i][j], "Unowned caller byte unchanged")
  }
}

case("s29-1 caller", instructionLimit = 2000000) {
  val initialSource = arena("0196b3d3dfbf033d3dfbf65a", 0)
  val initialDestination = arena("")
  for ((label, values) in
      listOf("source" to initialSource, "destination" to initialDestination)) for (i in
      values.indices step 4) setWord(
      label,
      (0..3).fold(0) { v, j -> v or (values[i + j] shl (8 * j)) },
      i,
  )
  val saved = List(9) { 0x5566778800000000L + it * 977 }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val top = readDataAddress("workspace", 4096)
  setRegister("sp", top)
  for (i in 0 until 4096 step 4) setWord("workspace", 0xa5a5a5a5.toInt(), i)
  setWord("mode", 4.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 12.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  startAt("main")
  runUntil("encode12_msa_call")
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
          .map { (label, size) -> Triple(label, readDataAddress(label), size) }
  val before = objects.map { (label, _, size) -> readBytes(label, size) }
  val lo = objects.minOf { it.second }
  val hi = objects.maxOf { it.second + it.third }
  val trace = traceMemoryAccesses(lo, (hi - lo).toInt(), maxAccesses = 200000)
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 0L) to 12)
  val writes: List<Pair<Long, Int>> = emptyList()
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  runUntil("after_enc12_upper")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 16)
  expectVectorLane("w0", 0, 8, 1L)
  expectVectorLane("w0", 1, 8, 150L)
  expectVectorLane("w0", 2, 8, 179L)
  expectVectorLane("w0", 3, 8, 211L)
  expectVectorLane("w0", 4, 8, 223L)
  expectVectorLane("w0", 5, 8, 191L)
  expectVectorLane("w0", 6, 8, 3L)
  expectVectorLane("w0", 7, 8, 61L)
  expectVectorLane("w0", 8, 8, 61L)
  expectVectorLane("w0", 9, 8, 251L)
  expectVectorLane("w0", 10, 8, 246L)
  expectVectorLane("w0", 11, 8, 90L)
  expectVectorLane("w0", 12, 8, 0L)
  expectVectorLane("w0", 13, 8, 0L)
  expectVectorLane("w0", 14, 8, 0L)
  expectVectorLane("w0", 15, 8, 0L)
  expectVectorLane("w1", 0, 8, 0L)
  expectVectorLane("w1", 1, 8, 25L)
  expectVectorLane("w1", 2, 8, 26L)
  expectVectorLane("w1", 3, 8, 51L)
  expectVectorLane("w1", 4, 8, 52L)
  expectVectorLane("w1", 5, 8, 61L)
  expectVectorLane("w1", 6, 8, 62L)
  expectVectorLane("w1", 7, 8, 63L)
  expectVectorLane("w1", 8, 8, 0L)
  expectVectorLane("w1", 9, 8, 51L)
  expectVectorLane("w1", 10, 8, 52L)
  expectVectorLane("w1", 11, 8, 61L)
  expectVectorLane("w1", 12, 8, 62L)
  expectVectorLane("w1", 13, 8, 63L)
  expectVectorLane("w1", 14, 8, 25L)
  expectVectorLane("w1", 15, 8, 26L)
  expectVectorLane("w4", 0, 8, 0L)
  expectVectorLane("w4", 1, 8, 25L)
  expectVectorLane("w4", 2, 8, 32L)
  expectVectorLane("w4", 3, 8, 57L)
  expectVectorLane("w4", 4, 8, 58L)
  expectVectorLane("w4", 5, 8, 67L)
  expectVectorLane("w4", 6, 8, 68L)
  expectVectorLane("w4", 7, 8, 69L)
  expectVectorLane("w4", 8, 8, 0L)
  expectVectorLane("w4", 9, 8, 57L)
  expectVectorLane("w4", 10, 8, 58L)
  expectVectorLane("w4", 11, 8, 67L)
  expectVectorLane("w4", 12, 8, 68L)
  expectVectorLane("w4", 13, 8, 69L)
  expectVectorLane("w4", 14, 8, 25L)
  expectVectorLane("w4", 15, 8, 32L)
  expectCondition(
      readBytes(top - 16 + 0, 16).contentEquals(octets("0196b3d3dfbf033d3dfbf65a00000000")),
      "Private phase bytes at offset0",
  )
  runUntil("encode12_msa_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 4)
  expectRegister("v1", 0)
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", top)
  expectOutput("")
  trace.close()
  for (x in trace.snapshot()) if (
      objects.any { (_, b, size) -> x.address < b + size && b < x.address + x.length }
  )
      expectCondition(
          (if (x.write) writes else reads + writes).any { (b, size) ->
            fits(x.address, x.length, b, size)
          },
          "Selected caller access interval",
      )
  for ((i, obj) in objects.withIndex()) {
    val (label, base, size) = obj
    val after = readBytes(label, size)
    for (j in after.indices) if (writes.none { (b, size) -> fits(base + j, 1, b, size) })
        expectCondition(after[j] == before[i][j], "Unowned caller byte unchanged")
  }
  runToExit()
  expectWord("status", 4)
  expectCondition(
      readBytes("written", 8).contentEquals(octets("0000000000000000")),
      "Caller records returned count or classifier value",
  )
}
