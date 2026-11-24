for (n in (0..16).toList() + listOf(73, 256)) for (corrupt in listOf(false, true)) case(
    "main n$n corrupt$corrupt",
    instructionLimit = 5000,
) {
  setWord("available", n)
  if (corrupt) setWord("packet", 0x1bff807f, offset = 8)
  val before = readBytes("packet_guard", 264)
  val readonly =
      mutableListOf(
          traceMemoryStores("packet_guard", 264),
          traceMemoryStores("record_b", 16),
          traceMemoryStores("available", 4),
      )
  for (label in listOf("record_a_guard", "record_b_guard", "state_end")) readonly +=
      traceMemoryStores(label, 4)
  val recordWrites = traceMemoryStores("record_a", 16)
  val reads = traceMemoryAccesses("packet_guard", 264, maxAccesses = 100_000)
  val data =
      traceMemoryAccesses(
          "packet_guard",
          (readDataAddress("state_end") + 4 - readDataAddress("packet_guard")).toInt(),
          maxAccesses = 100_000,
      )
  val expected = if (n < 12) -1 else if (corrupt) -6 else 12
  fun checkResult() {
    if (expected < 0) {
      recordWrites.expectNone()
      for (i in 0..3) expectWord("record_a", -1, offset = i * 4)
    } else
        for ((i, value) in listOf(1, 4660, 3, 254).withIndex()) expectWord(
            "record_a",
            value,
            offset = i * 4,
        )
    readonly.forEach { it.expectNone() }
    expectBytes("packet_guard", *before)
    reads.expectReadsWithin(readDataAddress("packet"), n)
  }
  runUntil("parse_ready")
  expectRegister("v0", expected)
  val pointer = if (expected < 0) 0L else readDataAddress("packet") + 8
  expectRegister("v1", pointer)
  checkResult()
  val marker = traceExecution("parse_ready", "function_return")
  runToExit()
  checkResult()
  expectWord("status", expected)
  val pointerBytes = readBytes("payload_address", 8)
  val actual = pointerBytes.withIndex().fold(0L) { acc, (i, b) -> acc or (b.toLong() shl (8 * i)) }
  expectCondition(actual == pointer, "Main preserves the full returned payload pointer")
  expectCondition(marker.snapshot().instructions > 0, "Main resumes beyond the stopped marker")
  for (access in data.snapshot().filter { it.write }) {
    val permitted =
        listOf("record_a" to 16, "status" to 4, "payload_address" to 8).any { (name, size) ->
          val base = readDataAddress(name)
          access.address >= base &&
              access.length <= size &&
              access.address - base <= size - access.length
        }
    expectCondition(permitted, "Main writes only the selected record and result metadata")
  }
  expectOutput("")
}
