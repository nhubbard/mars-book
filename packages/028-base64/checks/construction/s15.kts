fun octets(s: String) = IntArray(s.length / 2) { s.substring(2 * it, 2 * it + 2).toInt(16) }

fun arena(s: String, offset: Int = 0) =
    IntArray(8192) { 165 }.also { octets(s).copyInto(it, offset) }

case("s15-1 offset function", instructionLimit = 2000000) {
  val initialSource = arena("5a6d383d", 3)
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
  setWord("mode", 1.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 4.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  setRegisterToDataAddress("a0", "source", 3L)
  setRegister("a1", 4L)
  setRegisterToTextAddress("ra", "function_return")
  startAt("b64_validate")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 3L) to 4)
  val writes: List<Pair<Long, Int>> = emptyList()
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  runUntil("validate_good")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 48)
  expectRegister("s1", 1L)
  expectRegister("s2", 2L)
  expectRegister("s3", 60L)
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
  expectRegister("v1", 2)
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

case("s15-1 caller", instructionLimit = 2000000) {
  val initialSource = arena("5a6d383d", 0)
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
  setWord("mode", 1.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 4.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  startAt("main")
  runUntil("validate_call")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 0L) to 4)
  val writes: List<Pair<Long, Int>> = emptyList()
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  runUntil("validate_good")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 48)
  expectRegister("s1", 1L)
  expectRegister("s2", 2L)
  expectRegister("s3", 60L)
  runUntil("validate_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 0)
  expectRegister("v1", 2)
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
      readBytes("written", 8).contentEquals(octets("0200000000000000")),
      "Caller records returned count or classifier value",
  )
}

case("s15-badbits offset function", instructionLimit = 2000000) {
  val initialSource = arena("5a6d393d", 3)
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
  setWord("mode", 1.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 4.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  setRegisterToDataAddress("a0", "source", 3L)
  setRegister("a1", 4L)
  setRegisterToTextAddress("ra", "function_return")
  startAt("b64_validate")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 3L) to 4)
  val writes: List<Pair<Long, Int>> = emptyList()
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  runUntil("validate_bad")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 48)
  expectRegister("s1", 1L)
  expectRegister("s2", 3L)
  expectRegister("s3", 61L)
  runUntil("function_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 2)
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

case("s15-badbits caller", instructionLimit = 2000000) {
  val initialSource = arena("5a6d393d", 0)
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
  setWord("mode", 1.toInt())
  setWord("in_place", 0.toInt())
  setWord("count", 4.toInt())
  setWord("count", 0.toInt(), 4)
  setWord("capacity", 8192.toInt())
  setWord("capacity", 0.toInt(), 4)
  setWord("status", 4294967295.toInt())
  setWord("written", 4294967295.toInt())
  setWord("written", 4294967295.toInt(), 4)
  startAt("main")
  runUntil("validate_call")
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
  val reads: List<Pair<Long, Int>> = listOf(readDataAddress("source", 0L) to 4)
  val writes: List<Pair<Long, Int>> = emptyList()
  fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n
  val expectedSource = initialSource.copyOf()
  val expectedDestination = initialDestination.copyOf()
  runUntil("validate_bad")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("sp", top - 48)
  expectRegister("s1", 1L)
  expectRegister("s2", 3L)
  expectRegister("s3", 61L)
  runUntil("validate_return")
  expectCondition(
      readBytes("source", 8192).contentEquals(expectedSource),
      "Source matches independent phase",
  )
  expectCondition(
      readBytes("destination", 8192).contentEquals(expectedDestination),
      "Destination matches independent phase",
  )
  expectRegister("v0", 2)
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
  expectWord("status", 2)
  expectCondition(
      readBytes("written", 8).contentEquals(octets("0000000000000000")),
      "Caller records returned count or classifier value",
  )
}
