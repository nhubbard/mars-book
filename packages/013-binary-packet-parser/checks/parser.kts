import edu.missouristate.mars.cli.grading.AssemblyCase

data class Input(
    val name: String,
    val bytes: List<Int>,
    val n: Int,
    val offset: Int = 0,
    val target: String = "record_a",
)

fun packet(length: Int, seq: Int = 0x1234, flags: Int = 1): List<Int> {
  val raw =
      listOf(0x4d, 0x50, 1, flags, length shr 8, length and 255, seq shr 8, seq and 255) +
          (0 until length).map { (it * 129 + 127) and 255 }
  return raw + ((-raw.sum()) and 255)
}

fun expected(b: List<Int>, n: Int): Int {
  if (n < 8) return -1
  if (b[0] != 0x4d || b[1] != 0x50) return -2
  if (b[2] != 1) return -3
  if (b[3] and 254 != 0) return -4
  val size = b[4] * 256 + b[5]
  if (size > 64) return -5
  if (n < size + 9) return -1
  if (b.take(size + 9).sum() and 255 != 0) return -6
  return size + 9
}

fun AssemblyCase.loadBytes(bytes: List<Int>) {
  val padded = bytes + List(256 - bytes.size) { (it * 37 + 19) and 255 }
  for (i in 0 until 64) setWord(
      "packet",
      (0..3).fold(0) { w, k -> w or (padded[i * 4 + k] shl (k * 8)) },
      offset = i * 4,
  )
}

val inputs = mutableListOf<Input>()

for (length in listOf(0, 1, 2, 3, 7, 8, 31, 63, 64)) {
  val b = packet(length, if (length % 2 == 0) 0x80ff else 0xff80, length % 2)
  for (n in 0..b.size) inputs +=
      Input("prefix $length/$n", b, n, n % 8, if (n % 2 == 0) "record_a" else "record_b")
  inputs += Input("suffix $length", b + packet(1) + listOf(128, 255), b.size + 12, 7, "record_b")
  inputs += Input("bad checksum $length", b.dropLast(1) + ((b.last() + 1) and 255), b.size, 1)
}

for (flags in 0..255) {
  val b = packet(0, flags = flags)
  inputs += Input("flags $flags", b, 9, flags % 8)
}

for (length in listOf(65, 127, 128, 255, 256, 257, 32767, 32768, 65535)) {
  val b = packet(0).toMutableList()
  b[4] = length shr 8
  b[5] = length and 255
  for (n in listOf(7, 8, 9, 128)) inputs += Input("length $length n$n", b, n)
}

for (field in 0..3) for (n in 0..9) {
  val b = packet(0).toMutableList()
  b[field] = 255
  inputs += Input("bad field $field n$n", b, n, n % 8)
}

for ((index, input) in inputs.withIndex()) case(input.name, instructionLimit = 5000) {
  val bytes = List(input.offset) { 0xa5 } + input.bytes
  loadBytes(bytes)
  val sourceBefore = readBytes("packet_guard", 264)
  val dataTrace =
      traceMemoryAccesses(
          "packet_guard",
          (readDataAddress("state_end") + 4 - readDataAddress("packet_guard")).toInt(),
          maxAccesses = 100_000,
      )
  val other = if (input.target == "record_a") "record_b" else "record_a"
  val recordBefore = readBytes(input.target, 16)
  val protected =
      mutableListOf(traceMemoryStores("packet_guard", 264), traceMemoryStores(other, 16))
  for (label in
      listOf(
          "record_a_guard",
          "record_b_guard",
          "available",
          "status",
          "payload_address",
          "state_end",
      )) protected += traceMemoryStores(label, if (label == "payload_address") 8 else 4)
  val out = traceMemoryStores(input.target, 16)
  val reads = traceMemoryAccesses("packet_guard", 264, maxAccesses = 100_000)
  val continuation = if (index % 2 == 0) "function_return" else "done"
  val avoided = traceExecution("function_return", "done")
  for (i in 0..7) setRegister("s$i", 0x2345678900000000L + i * 1013L)
  setRegister("sp", 0x7fff0000L)
  setRegisterToTextAddress("ra", continuation)
  setRegisterToDataAddress("a0", "packet", input.offset.toLong())
  setRegister("a1", input.n)
  setRegisterToDataAddress("a2", input.target)
  startAt("parse_packet")
  runUntil(continuation)
  val result = expected(sourceBefore.drop(4 + input.offset), input.n)
  expectRegister("v0", result)
  expectRegister("v1", if (result < 0) 0L else readDataAddress("packet") + input.offset + 8)
  if (result < 0) {
    out.expectNone()
    expectBytes(input.target, *recordBefore)
  } else {
    val b = input.bytes
    val l = b[4] * 256 + b[5]
    val fields = listOf(b[3], b[6] * 256 + b[7], l, b.drop(8).take(l).sum() and 255)
    for ((i, v) in fields.withIndex()) expectWord(input.target, v, offset = i * 4)
  }
  reads.expectReadsWithin(readDataAddress("packet") + input.offset, input.n)
  expectCondition(avoided.snapshot().instructions == 0L, "Return to supplied continuation")
  for (i in 0..7) expectRegister("s$i", 0x2345678900000000L + i * 1013L)
  expectRegister("sp", 0x7fff0000L)
  protected.forEach { it.expectNone() }
  expectBytes("packet_guard", *sourceBefore)
  fun checkWrites() {
    val base = readDataAddress(input.target)
    for (access in dataTrace.snapshot().filter { it.write }) expectCondition(
        result >= 0 &&
            access.address >= base &&
            access.length <= 16 &&
            access.address - base <= 16 - access.length,
        "Write wholly within selected output record, and only on success",
    )
  }
  checkWrites()
  reads.close()
  out.close()
  runToExit()
  checkWrites()
  protected.forEach { it.expectNone() }
  expectOutput("")
}
