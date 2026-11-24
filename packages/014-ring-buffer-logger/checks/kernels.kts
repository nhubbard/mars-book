import edu.missouristate.mars.cli.grading.AssemblyCase

data class Trial(
    val capacity: Int,
    val head: Int,
    val records: List<List<Int>>,
    val pop: Boolean,
    val amount: Int,
    val salt: Int,
)

fun payload(length: Int, salt: Int) = List(length) { (it * 129 + salt * 37 + 128) and 255 }

fun AssemblyCase.putBytes(label: String, bytes: List<Int>) {
  for (i in 0 until bytes.size / 4) setWord(
      label,
      (0..3).fold(0) { word, k -> word or (bytes[i * 4 + k] shl (k * 8)) },
      offset = i * 4,
  )
}

fun AssemblyCase.putPointer(label: String, pointer: Long) {
  setWord(label, pointer.toInt(), offset = 16)
  setWord(label, (pointer ushr 32).toInt(), offset = 20)
}

val trials = mutableListOf<Trial>()

for (c in listOf(1, 2, 3, 7, 8, 13, 31, 32)) for (h in listOf(0, c - 1).distinct()) {
  val queues =
      listOf(
          emptyList(),
          listOf(emptyList()),
          listOf(payload(c - 1, c)),
          List(c) { emptyList<Int>() },
      )
  for ((q, records) in queues.withIndex()) for (pop in listOf(false, true)) for (n in
      listOf(0, 1, c - 1, c, 31).distinct().filter { it <= 31 }) trials +=
      Trial(c, h, records, pop, n, c * 97 + h * 7 + q)
}

for ((index, t) in trials.withIndex()) case(
    "${if(t.pop) "pop" else "push"} C${t.capacity} h${t.head} q${t.records.map{it.size}} n${t.amount} $index",
    instructionLimit = 5000,
) {
  val c = t.capacity
  val used = t.records.sumOf { it.size + 1 }
  val tail = (t.head + used) % c
  val descriptor = if (index % 2 == 0) "descriptor_a" else "descriptor_b"
  val ring = if (index % 3 == 0) "ring_b" else "ring_a"
  val ringGuard = ring + "_guard"
  val srcOffset = index % 8
  val outOffset = (index / 3) % 8
  val source = payload(64, t.salt)
  val output = payload(64, t.salt + 1).toMutableList()
  val bytes = payload(32, t.salt + 2).toMutableList()
  var cursor = t.head
  for (record in t.records) for (byte in listOf(record.size) + record) {
    bytes[cursor] = byte
    cursor = (cursor + 1) % c
  }
  putBytes("source", source)
  putBytes("output", output)
  putBytes(ring, bytes)
  setWord(descriptor, t.head)
  setWord(descriptor, tail, offset = 4)
  setWord(descriptor, used, offset = 8)
  setWord(descriptor, c, offset = 12)
  val base = readDataAddress(ring)
  putPointer(descriptor, base)
  val expectedRing = bytes.toMutableList()
  var expectedHead = t.head
  var expectedTail = tail
  var expectedUsed = used
  val writable = mutableSetOf<Long>()
  val status: Int
  val aux: Int
  if (!t.pop) {
    if (t.amount >= c) {
      status = -2
      aux = 0
    } else if (t.amount + 1 > c - used) {
      status = -1
      aux = 0
    } else {
      status = 0
      aux = t.amount + 1
      for (byte in listOf(t.amount) + source.drop(srcOffset).take(t.amount)) {
        expectedRing[expectedTail] = byte
        writable += base + expectedTail
        expectedTail = (expectedTail + 1) % c
      }
      expectedUsed += aux
    }
  } else {
    if (t.records.isEmpty()) {
      status = -1
      aux = 0
    } else if (t.amount < t.records.first().size) {
      status = -2
      aux = t.records.first().size
    } else {
      val first = t.records.first()
      status = first.size
      aux = first.size + 1
      for ((i, byte) in first.withIndex()) {
        output[outOffset + i] = byte
        writable += readDataAddress("output") + outOffset + i
      }
      expectedHead = (expectedHead + aux) % c
      expectedUsed -= aux
    }
  }
  if (status >= 0) for (i in 0 until 12) writable += readDataAddress(descriptor) + i
  val data =
      traceMemoryAccesses(
          "descriptor_a",
          (readDataAddress("state_end") + 4 - readDataAddress("descriptor_a")).toInt(),
          maxAccesses = 100_000,
      )
  val ringReads = traceMemoryAccesses(ringGuard, 40, maxAccesses = 100_000)
  val sourceReads = traceMemoryAccesses("source_guard", 72, maxAccesses = 100_000)
  val continuation = if (index % 2 == 0) "function_return" else "done"
  val avoided = traceExecution("function_return", "done")
  for (i in 0..7) setRegister("s$i", 0x3456789000000000L + i * 1013)
  setRegister("sp", 0x7fff0000L)
  setRegisterToTextAddress("ra", continuation)
  setRegisterToDataAddress("a0", descriptor)
  setRegisterToDataAddress(
      "a1",
      if (t.pop) "output" else "source",
      if (t.pop) outOffset.toLong() else srcOffset.toLong(),
  )
  setRegister("a2", t.amount)
  startAt(if (t.pop) "pop_record" else "push_record")
  runUntil(continuation)
  expectRegister("v0", status)
  expectRegister("v1", aux)
  expectCondition(avoided.snapshot().instructions == 0L, "Return through the supplied continuation")
  fun inspect() {
    expectWord(descriptor, expectedHead)
    expectWord(descriptor, expectedTail, offset = 4)
    expectWord(descriptor, expectedUsed, offset = 8)
    expectWord(descriptor, c, offset = 12)
    expectWord(descriptor, base.toInt(), offset = 16)
    expectWord(descriptor, (base ushr 32).toInt(), offset = 20)
    expectBytes(ring, *expectedRing.toIntArray())
    expectBytes("source", *source.toIntArray())
    expectBytes("output", *output.toIntArray())
    for (access in data.snapshot().filter { it.write }) expectCondition(
        (0 until access.length).all { access.address + it in writable },
        "Every native store byte belongs to the successful operation's permitted state/output",
    )
    ringReads.expectReadsWithin(base, c)
    if (!t.pop) sourceReads.expectReadsWithin(readDataAddress("source") + srcOffset, t.amount)
  }
  inspect()
  for (i in 0..7) expectRegister("s$i", 0x3456789000000000L + i * 1013)
  expectRegister("sp", 0x7fff0000L)
  runToExit()
  inspect()
  expectOutput("")
}
