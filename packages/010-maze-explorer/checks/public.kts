data class Maze(val name: String, val walls: List<Int>, val start: Int, val goal: Int)

fun adjacent(cell: Int): List<Int> {
  val row = cell / 8
  val col = cell % 8
  return listOf(row - 1 to col, row to col + 1, row + 1 to col, row to col - 1)
      .filter { (r, c) -> r in 0..7 && c in 0..7 }
      .map { (r, c) -> r * 8 + c }
}

fun shortest(maze: Maze): IntArray {
  val distances = IntArray(64) { 255 }
  val work = ArrayDeque<Int>()
  distances[maze.start] = 0
  work.addLast(maze.start)
  while (work.isNotEmpty()) {
    val cell = work.removeFirst()
    for (neighbor in adjacent(cell)) if (maze.walls[neighbor] == 0 && distances[neighbor] == 255) {
      distances[neighbor] = distances[cell] + 1
      work.addLast(neighbor)
    }
  }
  return distances
}

fun color(wall: Int, state: Int, path: Int): Int =
    when {
      wall != 0 -> 0x202020
      path != 0 -> 0x0072b2
      state == 0 -> 0xffffff
      state == 1 -> 0xe69f00
      else -> 0x009e73
    }

val fixtures =
    listOf(
        Maze(
            "two equal shortest routes",
            List(64) { if (it / 8 < 2 && it % 8 < 2) 0 else 1 },
            0,
            9,
        ),
        Maze("start equals goal", List(64) { 0 }, 63, 63),
        Maze("full queue capacity", List(64) { 0 }, 0, 63),
        Maze("opposite corner", List(64) { 0 }, 63, 0),
        Maze("no horizontal row wrap", List(64) { if (it == 7 || it == 8) 0 else 1 }, 7, 8),
        Maze("diagonal is not adjacent", List(64) { if (it == 0 || it == 9) 0 else 1 }, 0, 9),
        Maze(
            "long corridor",
            List(64) {
              val y = it / 8
              val x = it % 8
              if (y % 2 == 0 || (y % 4 == 1 && x == 7) || (y % 4 == 3 && x == 0)) 0 else 1
            },
            0,
            56,
        ),
        Maze("isolated start", List(64) { if (it == 27 || it == 63) 0 else 1 }, 27, 63),
    )

for ((fixtureIndex, maze) in fixtures.withIndex()) case(maze.name, instructionLimit = 250000) {
  expectDataAddress("frame", 0x10010000L)
  expectDataAddress("frame_guard", 0x10010100L)
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
  val protected =
      mutableListOf(
          traceMemoryStores("walls", 64),
          traceMemoryStores("start", 4),
          traceMemoryStores("goal", 4),
      )
  for ((index, label) in guards.withIndex()) {
    setWord(label, 0x13579bdf xor (fixtureIndex * 131 + index))
    protected += traceMemoryStores(label, 4)
  }
  // Fixed-size ABI byte arrays are four-byte aligned. These words install little-endian input
  // bytes.
  for (word in 0 until 16) {
    val bytes = maze.walls.subList(word * 4, word * 4 + 4)
    setWord(
        "walls",
        bytes[0] or (bytes[1] shl 8) or (bytes[2] shl 16) or (bytes[3] shl 24),
        offset = word * 4,
    )
  }
  setWord("start", maze.start)
  setWord("goal", maze.goal)
  for (cell in 0 until 64) setWord("frame", 0x12345678, offset = cell * 4)
  for (label in listOf("states", "parents", "distance", "queue", "path")) for (word in
      0 until 16) setWord(label, 0x5a5a5a5a, offset = word * 4)
  for (label in listOf("head", "tail", "current", "phase", "result", "length")) setWord(
      label,
      123456,
  )
  val oracle = shortest(maze)
  val states = IntArray(64)
  val parents = IntArray(64) { 255 }
  val distances = IntArray(64) { 255 }
  val queue = mutableListOf(maze.start)
  val path = IntArray(64)
  states[maze.start] = 1
  distances[maze.start] = 0
  var head = 0
  var current = -1
  fun snapshot(phase: Int, result: Int = -1, length: Int = -1) {
    expectWord("phase", phase)
    expectWord("head", head)
    expectWord("tail", queue.size)
    expectWord("current", current)
    expectWord("result", result)
    expectWord("length", length)
    expectCondition(
        readBytes("states", 64).contentEquals(states),
        "states must match the published BFS transitions",
    )
    expectCondition(
        readBytes("parents", 64).contentEquals(parents),
        "parents must record each discovery predecessor",
    )
    expectCondition(
        readBytes("distance", 64).contentEquals(distances),
        "distances must be shortest edge counts",
    )
    expectCondition(
        readBytes("path", 64).contentEquals(path),
        "path mask must match the parent chain",
    )
    expectCondition(
        readBytes("queue", 64).toList() == queue + List(64 - queue.size) { 0 },
        "queue must retain its FIFO discovery history and a zero unused suffix",
    )
    for (cell in 0 until 64) expectWord(
        "frame",
        color(maze.walls[cell], states[cell], path[cell]),
        offset = cell * 4,
    )
    expectCondition(
        readBytes("walls", 64).toList() == maze.walls,
        "maze input must remain unchanged",
    )
    expectWord("start", maze.start)
    expectWord("goal", maze.goal)
    for ((index, label) in guards.withIndex()) expectWord(
        label,
        0x13579bdf xor (fixtureIndex * 131 + index),
    )
    protected.forEach { it.expectNone() }
  }
  runUntil("maze_ready")
  snapshot(0)
  step()
  var found = false
  while (head < queue.size && !found) {
    current = queue[head]
    head++
    states[current] = 2
    val newlyDiscovered =
        if (current == maze.goal) emptySet()
        else adjacent(current).filter { maze.walls[it] == 0 && states[it] == 0 }.toSet()
    runUntil("maze_ready")
    expectWord("phase", 1)
    expectWord("head", head)
    expectWord("current", current)
    val tail = readWord("tail")
    expectCondition(
        tail == queue.size + newlyDiscovered.size && tail <= 64,
        "enqueue every unseen open neighbor exactly once",
    )
    val actual = readBytes("queue", tail).toList()
    expectCondition(actual.take(queue.size) == queue, "dequeue must not overwrite FIFO history")
    val suffix = actual.drop(queue.size)
    expectCondition(
        suffix.size == suffix.toSet().size && suffix.toSet() == newlyDiscovered,
        "new queue entries must be exactly the unseen orthogonal neighbors",
    )
    for (cell in suffix) {
      states[cell] = 1
      parents[cell] = current
      distances[cell] = distances[current] + 1
      expectCondition(
          distances[cell] == oracle[cell],
          "FIFO discovery must assign a shortest distance",
      )
      queue += cell
    }
    snapshot(1)
    found = current == maze.goal
    step()
  }
  if (found) {
    var cell = maze.goal
    var remaining = oracle[cell]
    while (true) {
      path[cell] = 1
      if (cell == maze.start) break
      val parent = parents[cell]
      expectCondition(
          parent in 0..63 && parent in adjacent(cell),
          "path predecessor must be an orthogonal cell",
      )
      expectCondition(
          remaining > 0 && distances[parent] == remaining - 1,
          "parent chain must strictly decrease distance",
      )
      remaining--
      cell = parent
    }
    expectCondition(
        remaining == 0 && path.sum() == oracle[maze.goal] + 1,
        "path must contain one more cell than edge",
    )
  }
  val result = if (found) 1 else 0
  val length = if (found) oracle[maze.goal] else -1
  expectCondition(found == (oracle[maze.goal] != 255), "search result must agree with reachability")
  runUntil("maze_ready")
  snapshot(2, result, length)
  runToExit()
  snapshot(2, result, length)
  expectOutput("")
}
