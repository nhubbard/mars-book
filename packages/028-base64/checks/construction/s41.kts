fun octets(s: String) = IntArray(s.length / 2) { s.substring(2 * it, 2 * it + 2).toInt(16) }

fun arena(s: String, offset: Int = 0) =
    IntArray(8192) { 165 }.also { octets(s).copyInto(it, offset) }

case("s41-1 offset function", instructionLimit = 2000000) {
  val initialSource =
      arena("67636f54584b58754e34444a456c756b37545a2f794246616f2b77316673635157513d3d", 3)
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
  setWord("mode", 7.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 36.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  setRegisterToDataAddress("a0", "source", 3L)
  setRegister("a1", 36L)
  setRegisterToDataAddress("a2", "destination", 5L)
  setRegister("a3", 8192L)
  setRegisterToTextAddress("ra", "function_return")
  startAt("b64_decode_msa")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 3L) to 36)
  val writes: List<Pair<Long, Int>> = listOf(readDataAddress("destination", 5L) to 25)
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  octets("81ca135ca5ee3780c9125ba4ed367fc8115aa3ec357ec71059").copyInto(expectedDestination, 5)
  runUntil("decode_done")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 80)
  runUntil("function_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 0)
  expectRegister("v1", 25)
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

case("s41-1 caller", instructionLimit = 2000000) {
  val initialSource =
      arena("67636f54584b58754e34444a456c756b37545a2f794246616f2b77316673635157513d3d", 0)
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
  setWord("mode", 7.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 36.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  startAt("main")
  runUntil("decode_msa_call")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 0L) to 36)
  val writes: List<Pair<Long, Int>> = listOf(readDataAddress("destination", 0L) to 25)
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  octets("81ca135ca5ee3780c9125ba4ed367fc8115aa3ec357ec71059").copyInto(expectedDestination, 0)
  runUntil("decode_done")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 80)
  runUntil("decode_msa_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 0)
  expectRegister("v1", 25)
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
  expectWord("status", 0)
  expectCondition(
      readBytes("written", 8).contentEquals(octets("1900000000000000")),
      "Caller records returned count or classifier value",
  )
}

case("s41-inplace offset function", instructionLimit = 2000000) {
  val initialSource =
      arena("67636f54584b58754e34444a456c756b37545a2f794246616f2b77316673635157513d3d", 3)
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
  setWord("mode", 7.toInt())
  setWord("in_place", 1.toInt())
  setWord("count", 36.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  setRegisterToDataAddress("a0", "source", 3L)
  setRegister("a1", 36L)
  setRegisterToDataAddress("a2", "source", 3L)
  setRegister("a3", 8192L)
  setRegisterToTextAddress("ra", "function_return")
  startAt("b64_decode_msa")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 3L) to 36)
  val writes: List<Pair<Long, Int>> = listOf(readDataAddress("source", 3L) to 25)
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  octets("81ca135ca5ee3780c9125ba4ed367fc8115aa3ec357ec71059").copyInto(expectedSource, 3)
  runUntil("decode_done")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 80)
  runUntil("function_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 0)
  expectRegister("v1", 25)
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

case("s41-inplace caller", instructionLimit = 2000000) {
  val initialSource =
      arena("67636f54584b58754e34444a456c756b37545a2f794246616f2b77316673635157513d3d", 0)
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
  setWord("mode", 7.toInt())
  setWord("in_place", 1.toInt())
  setWord("count", 36.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  startAt("main")
  runUntil("decode_msa_call")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 0L) to 36)
  val writes: List<Pair<Long, Int>> = listOf(readDataAddress("source", 0L) to 25)
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  octets("81ca135ca5ee3780c9125ba4ed367fc8115aa3ec357ec71059").copyInto(expectedSource, 0)
  runUntil("decode_done")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 80)
  runUntil("decode_msa_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 0)
  expectRegister("v1", 25)
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
  expectWord("status", 0)
  expectCondition(
      readBytes("written", 8).contentEquals(octets("1900000000000000")),
      "Caller records returned count or classifier value",
  )
}
