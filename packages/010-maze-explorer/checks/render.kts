fun pixel(wall: Int, state: Int, path: Int): Int =
    when {
      wall != 0 -> 0x202020
      path != 0 -> 0x0072b2
      state == 0 -> 0xffffff
      state == 1 -> 0xe69f00
      else -> 0x009e73
    }

for (fixture in 0 until 5) case("independent renderer $fixture", instructionLimit = 10000) {
  val walls = List(64) { if (fixture == 1 || fixture >= 3 && it % 4 == 0) 1 else 0 }
  val states = List(64) { if (fixture < 2) 0 else (it + fixture) % 3 }
  val path = List(64) { if (fixture >= 3 && it % (fixture - 1) == 0) 1 else 0 }
  val inputs =
      mapOf(
          "walls" to walls,
          "states" to states,
          "path" to path,
          "parents" to List(64) { 73 },
          "distance" to List(64) { 147 },
          "queue" to List(64) { 219 },
      )
  val guards =
      listOf(
          "frame_guard",
          "walls_guard",
          "start_guard",
          "goal_guard",
          "states_guard",
          "parents_guard",
          "distance_guard",
          "queue_guard",
          "path_guard",
          "metadata_guard",
      )
  val metadata = listOf("start", "goal", "head", "tail", "current", "phase", "result", "length")
  val protected =
      inputs.keys.map { traceMemoryStores(it, 64) } +
          (guards + metadata).map { traceMemoryStores(it, 4) }
  for ((label, bytes) in inputs) for (word in 0 until 16) {
    val i = word * 4
    setWord(
        label,
        bytes[i] or (bytes[i + 1] shl 8) or (bytes[i + 2] shl 16) or (bytes[i + 3] shl 24),
        offset = i,
    )
  }
  for ((index, label) in (guards + metadata).withIndex()) setWord(
      label,
      0x13579bdf xor (fixture * 31 + index),
  )
  for (cell in 0 until 64) setWord("frame", 0x12345678, offset = cell * 4)
  for (index in 0..7) setRegister("s$index", 0x1122334455667700L + index)
  setRegister("sp", 0x7fffeff0L)
  setRegisterToTextAddress("ra", "done")
  startAt("render")
  runUntil("maze_ready")
  for (cell in 0 until 64) expectWord(
      "frame",
      pixel(walls[cell], states[cell], path[cell]),
      offset = cell * 4,
  )
  for ((label, bytes) in inputs) expectCondition(
      readBytes(label, 64).toList() == bytes,
      "renderer must preserve $label",
  )
  for ((index, label) in (guards + metadata).withIndex()) expectWord(
      label,
      0x13579bdf xor (fixture * 31 + index),
  )
  for (index in 0..7) expectRegister("s$index", 0x1122334455667700L + index)
  expectRegister("sp", 0x7fffeff0L)
  runUntil("done")
  for (index in 0..7) expectRegister("s$index", 0x1122334455667700L + index)
  expectRegister("sp", 0x7fffeff0L)
  runToExit()
  for (cell in 0 until 64) expectWord(
      "frame",
      pixel(walls[cell], states[cell], path[cell]),
      offset = cell * 4,
  )
  protected.forEach { it.expectNone() }
  expectOutput("")
}
