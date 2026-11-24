// Public animation protocol: observe every complete initial/updated state, then normal exit.
data class Seed(val name: String, val x: Int, val y: Int, val dx: Int, val dy: Int)

fun reflected(p: Int, v: Int): Pair<Int, Int> {
  val trial = p + v
  return when {
    trial <= 0 -> -trial to -v
    trial >= 1792 -> 3584 - trial to -v
    else -> trial to v
  }
}

fun cell(raw: Int, mode: Int): Int = raw / 256 + if (mode == 1 && raw % 256 >= 128) 1 else 0

val examples =
    listOf(
        Seed("default", 320, 448, 192, 320),
        Seed("lower contact", 128, 896, -128, 0),
        Seed("upper contact", 1664, 896, 128, 0),
        Seed("outward walls", 0, 1792, -1024, 1024),
        Seed("half thresholds", 127, 128, 1, -1),
        Seed("corner contact", 1664, 128, 128, -128),
    )

for (seed in examples) for (request in listOf(0, 1, 8, 32)) for (mode in 0..1) case(
    "${seed.name} request $request mode $mode",
    instructionLimit = 250000,
) {
  expectDataAddress("frame", 0x10010000L)
  expectDataAddress("frame_guard", 0x10010100L)
  for ((name, value) in
      listOf(
          "seed_x" to seed.x,
          "seed_y" to seed.y,
          "seed_vx" to seed.dx,
          "seed_vy" to seed.dy,
          "steps" to request,
          "round_mode" to mode,
      )) setWord(name, value)
  for (name in listOf("x_raw", "y_raw", "vx_raw", "vy_raw", "tick", "pixel_id")) setWord(
      name,
      0x12345678,
  )
  for (i in 0 until 64) {
    setWord("frame", 0x34567890, offset = i * 4)
    setWord("spare_frame", 0x76543210, offset = i * 4)
  }
  val protected =
      mutableListOf(
          traceMemoryStores("seed_x", 16),
          traceMemoryStores("steps", 8),
          traceMemoryStores("spare_frame", 256),
      )
  for (name in
      listOf(
          "frame_guard",
          "spare_guard",
          "seed_guard",
          "options_guard",
          "state_guard",
      )) protected += traceMemoryStores(name, 4)
  val markers = traceExecution("frame_ready", "frame_continue")
  var current = seed
  fun checkFrame(t: Int) {
    expectWord("x_raw", current.x)
    expectWord("y_raw", current.y)
    expectWord("vx_raw", current.dx)
    expectWord("vy_raw", current.dy)
    expectWord("tick", t)
    val id = 8 * cell(current.y, mode) + cell(current.x, mode)
    expectWord("pixel_id", id)
    for (i in 0 until 64) {
      expectWord("frame", if (i == id) 0xffffff else 0, offset = i * 4)
      expectWord("spare_frame", 0x76543210, offset = i * 4)
    }
    protected.forEach { it.expectNone() }
  }
  for (t in 0..request) {
    runUntil("frame_ready")
    checkFrame(t)
    expectCondition(
        markers.snapshot().instructions == t.toLong(),
        "Exactly one supplied marker belongs to each earlier tick",
    )
    if (t < request) {
      step()
      val horizontal = reflected(current.x, current.dx)
      val vertical = reflected(current.y, current.dy)
      current =
          Seed(current.name, horizontal.first, vertical.first, horizontal.second, vertical.second)
    }
  }
  runToExit()
  checkFrame(request)
  expectOutput("")
  expectCondition(
      markers.snapshot().instructions == request + 1L,
      "Initial state plus every requested step must be published once",
  )
}
