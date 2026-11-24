fun exchanges(input: List<Int>, algorithm: Int): List<List<Int>> {
  val current = input.toMutableList()
  val states = mutableListOf(input.toList())
  if (algorithm == 0) {
    for (end in 1 until current.size) {
      var index = end
      while (index > 0 && current[index] < current[index - 1]) {
        val left = current[index - 1]
        current[index - 1] = current[index]
        current[index] = left
        states += current.toList()
        index--
      }
    }
  } else {
    for (left in 0 until (current.size - 1).coerceAtLeast(0)) {
      val minimum = current.subList(left, current.size).minOrNull()!!
      val index = (left until current.size).first { current[it] == minimum }
      if (index != left) {
        val value = current[left]
        current[left] = current[index]
        current[index] = value
        states += current.toList()
      }
    }
  }
  check(current == input.sorted())
  check(states.all { it.sorted() == input.sorted() })
  return states
}

fun pixel(state: List<Int>, x: Int, y: Int): Int =
    if (x < state.size && y >= 3 - state[x]) 0xffffff else 0

val fixtures =
    listOf(
        listOf(2, -2, 3, -4, 0, 1, -3, -1),
        (-4..3).toList(),
        (3 downTo -4).toList(),
        listOf(0, -1, 0, -1),
        emptyList(),
        listOf(-4),
    )

for ((fixtureIndex, input) in fixtures.withIndex()) for (method in 0..1) {
  case("fixture $fixtureIndex method $method", instructionLimit = 100000) {
    expectDataAddress("frame", 0x10010000L)
    expectDataAddress("frame_guard", 0x10010100L)
    val protected = mutableListOf(traceMemoryStores("count", 4), traceMemoryStores("algorithm", 4))
    val guards =
        listOf(
            "frame_guard",
            "values_guard",
            "count_guard",
            "algorithm_guard",
            "phase_guard",
            "swaps_guard",
        )
    for ((index, guard) in guards.withIndex()) {
      protected += traceMemoryStores(guard, 4)
      setWord(guard, 0x13579bdf xor (fixtureIndex * 131 + index))
    }
    if (input.size < 8)
        protected += traceMemoryStores("values", (8 - input.size) * 4, offset = input.size * 4)
    val tail = List(8 - input.size) { 0x5a000000 xor (fixtureIndex * 73 + it) }
    for ((index, value) in (input + tail).withIndex()) setWord("values", value, offset = index * 4)
    setWord("count", input.size)
    setWord("algorithm", method)
    setWord("phase", -1)
    setWord("swap_count", -1)
    for (cell in 0 until 64) setWord("frame", 0x12345678, offset = cell * 4)
    val states = exchanges(input, method)
    val published = states + listOf(states.last())
    for ((index, state) in published.withIndex()) {
      runUntil("sort_ready")
      expectWord("phase", if (index == 0) 0 else if (index == published.lastIndex) 2 else 1)
      expectWord("swap_count", index.coerceAtMost(states.lastIndex))
      for ((position, value) in (state + tail).withIndex()) expectWord(
          "values",
          value,
          offset = position * 4,
      )
      for (y in 0 until 8) for (x in 0 until 8) expectWord(
          "frame",
          pixel(state, x, y),
          offset = (y * 8 + x) * 4,
      )
      if (index < published.lastIndex) step()
    }
    runToExit()
    expectWord("phase", 2)
    expectWord("swap_count", states.lastIndex)
    for ((position, value) in (input.sorted() + tail).withIndex()) expectWord(
        "values",
        value,
        offset = position * 4,
    )
    for (y in 0 until 8) for (x in 0 until 8) expectWord(
        "frame",
        pixel(input.sorted(), x, y),
        offset = (y * 8 + x) * 4,
    )
    expectWord("count", input.size)
    expectWord("algorithm", method)
    for ((index, guard) in guards.withIndex()) expectWord(
        guard,
        0x13579bdf xor (fixtureIndex * 131 + index),
    )
    protected.forEach { it.expectNone() }
    expectOutput("")
  }
}
