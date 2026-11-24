val operations =
    listOf(
        Triple(false, 3, 0),
        Triple(false, 2, 8),
        Triple(true, 31, 0),
        Triple(false, 3, 16),
        Triple(true, 31, 8),
        Triple(true, 31, 16),
        Triple(false, 0, 24),
        Triple(true, 0, 24),
        Triple(false, 7, 32),
        Triple(false, 0, 24),
        Triple(true, 6, 32),
        Triple(true, 7, 32),
        Triple(true, 31, 48),
        Triple(false, 8, 48),
    )

for (count in listOf(0, 1, 2, 3, 4, 6, 8, 9, 10, 11, 12, 14)) case(
    "main first $count commands",
    instructionLimit = 20000,
) {
  setWord("command_count", count)
  val source = readBytes("source", 64).toList()
  val output = readBytes("output", 64).toMutableList()
  val ring = readBytes("ring_a", 32).toMutableList()
  val base = readDataAddress("ring_a")
  val descriptor = readDataAddress("descriptor_a")
  val whole =
      traceMemoryAccesses(
          "descriptor_a",
          (readDataAddress("state_end") + 4 - descriptor).toInt(),
          maxAccesses = 100_000,
      )
  var head = 0
  var tail = 0
  var used = 0
  val queue = mutableListOf<List<Int>>()
  var checkpoint = 0
  val writes = mutableSetOf<Long>()
  for (i in 16..23) writes += descriptor + i
  val markers = traceExecution("operation_ready", "operation_continue")
  fun checkWrites() {
    for (access in whole.snapshot().filter { it.write }) expectCondition(
        (0 until access.length).all { access.address + it in writes },
        "Every main store has a permitted destination",
    )
  }
  for (index in 0 until count) {
    val previousWrites = writes.toSet()
    writes.clear()
    if (index == 0) for (i in 16..23) writes += descriptor + i
    val (pop, amount, offset) = operations[index]
    val status: Int
    val aux: Int
    if (!pop) {
      if (amount >= 8) {
        status = -2
        aux = 0
      } else if (amount + 1 > 8 - used) {
        status = -1
        aux = 0
      } else {
        status = 0
        aux = amount + 1
        val body = source.drop(offset).take(amount)
        queue += body
        for (byte in listOf(amount) + body) {
          ring[tail] = byte
          writes += base + tail
          tail = (tail + 1) % 8
        }
        used += aux
      }
    } else {
      if (queue.isEmpty()) {
        status = -1
        aux = 0
      } else if (amount < queue.first().size) {
        status = -2
        aux = queue.first().size
      } else {
        val body = queue.removeAt(0)
        status = body.size
        aux = body.size + 1
        head = (head + aux) % 8
        used -= aux
        for ((i, byte) in body.withIndex()) {
          output[offset + i] = byte
          writes += readDataAddress("output") + offset + i
        }
      }
    }
    if (status >= 0) for (i in 0 until 12) writes += descriptor + i
    for (name in listOf("last_status", "last_aux", "operation_index")) for (i in 0..3) writes +=
        readDataAddress(name) + i
    runUntil("operation_ready")
    expectWord("operation_index", index)
    expectWord("last_status", status)
    expectWord("last_aux", aux)
    expectWord("descriptor_a", head)
    expectWord("descriptor_a", tail, offset = 4)
    expectWord("descriptor_a", used, offset = 8)
    expectWord("descriptor_a", 8, offset = 12)
    expectBytes("ring_a", *ring.toIntArray())
    expectBytes("output", *output.toIntArray())
    expectBytes("source", *source.toIntArray())
    expectCondition(
        markers.snapshot().instructions == index.toLong(),
        "Exactly the earlier ready markers executed",
    )
    for (access in whole.snapshot().drop(checkpoint).filter { it.write }) expectCondition(
        (0 until access.length).all { access.address + it in writes },
        "Current operation stores respect its own success or failure",
    )
    checkpoint = whole.snapshot().size
    writes += previousWrites
    checkWrites()
    if (index + 1 < count) step()
  }
  runToExit()
  checkWrites()
  if (count > 0)
      expectCondition(
          whole.snapshot().drop(checkpoint).none { it.write },
          "No caller-data writes after the final published operation",
      )
  expectWord("descriptor_a", base.toInt(), offset = 16)
  expectWord("descriptor_a", (base ushr 32).toInt(), offset = 20)
  expectCondition(
      markers.snapshot().instructions == count.toLong(),
      "One completed marker per command",
  )
  expectWord("descriptor_a", head)
  expectWord("descriptor_a", tail, offset = 4)
  expectWord("descriptor_a", used, offset = 8)
  expectBytes("ring_a", *ring.toIntArray())
  expectBytes("output", *output.toIntArray())
  expectOutput("")
}
