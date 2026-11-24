import edu.missouristate.mars.cli.grading.AssemblyCase

fun AssemblyCase.prepareCaller(returnLabel: String) {
  for (i in 0..7) setRegister("s$i", 0x2345678900000000L + i * 1013L)
  setRegister("sp", 0x7fff0000L)
  setRegisterToTextAddress("ra", returnLabel)
}

fun AssemblyCase.checkCaller() {
  for (i in 0..7) expectRegister("s$i", 0x2345678900000000L + i * 1013L)
  expectRegister("sp", 0x7fff0000L)
}

val axes =
    listOf(
        0 to 0,
        1792 to 0,
        896 to 0,
        128 to -128,
        1664 to 128,
        64 to -128,
        1728 to 128,
        0 to -1024,
        1792 to 1024,
        0 to 1024,
        1792 to -1024,
        896 to 1024,
        896 to -1024,
        1 to -1,
        1791 to 1,
        127 to 1,
        128 to -1,
        448 to 320,
        1728 to 320,
        1664 to 192,
    )

for ((index, pair) in axes.withIndex()) case(
    "axis ${pair.first} ${pair.second}",
    instructionLimit = 3000,
) {
  val p = pair.first
  val v = pair.second
  val trial = p + v
  val expected =
      when {
        trial <= 0 -> -trial to -v
        trial >= 1792 -> 3584 - trial to -v
        else -> trial to v
      }
  val readonly =
      traceMemoryStores(
          "frame",
          (readDataAddress("state_guard") + 4 - readDataAddress("frame")).toInt(),
      )
  val continuation = if (index % 2 == 0) "overflow_ready" else "done"
  val avoidedContinuation = traceExecution("overflow_ready", "done")
  prepareCaller(continuation)
  setRegister("a0", p)
  setRegister("a1", v)
  startAt("advance_axis")
  runUntil(continuation)
  expectCondition(
      avoidedContinuation.snapshot().instructions == 0L,
      "Return to the supplied continuation without executing the other marker",
  )
  expectRegister("v0", expected.first)
  expectRegister("v1", expected.second)
  checkCaller()
  readonly.expectNone()
  runToExit()
  readonly.expectNone()
  expectOutput("")
}

val coordinates = listOf(0, 127, 128, 129, 255, 256, 320, 448, 1663, 1664, 1791, 1792)

for (x in coordinates) for (y in coordinates) for (mode in 0..1) case(
    "render $x $y mode $mode",
    instructionLimit = 3000,
) {
  val target = if ((x + y + mode) % 2 == 0) "frame" else "spare_frame"
  val other = if (target == "frame") "spare_frame" else "frame"
  for (i in 0 until 64) {
    setWord(target, 0x34567890, offset = i * 4)
    setWord(other, 0x76543210, offset = i * 4)
  }
  val protected =
      mutableListOf(
          traceMemoryStores(other, 256),
          traceMemoryStores("seed_x", 16),
          traceMemoryStores("steps", 8),
          traceMemoryStores("x_raw", 24),
      )
  for (name in
      listOf(
          "frame_guard",
          "spare_guard",
          "seed_guard",
          "options_guard",
          "state_guard",
      )) protected += traceMemoryStores(name, 4)
  val continuation = if ((x + y) % 2 == 0) "overflow_ready" else "done"
  val avoidedContinuation = traceExecution("overflow_ready", "done")
  prepareCaller(continuation)
  setRegisterToDataAddress("a0", target)
  setRegister("a1", x)
  setRegister("a2", y)
  setRegister("a3", mode)
  startAt("render_ball")
  val column = x / 256 + if (mode == 1 && x % 256 >= 128) 1 else 0
  val row = y / 256 + if (mode == 1 && y % 256 >= 128) 1 else 0
  val id = row * 8 + column
  fun inspect() {
    for (i in 0 until 64) {
      expectWord(target, if (i == id) 0xffffff else 0, offset = i * 4)
      expectWord(other, 0x76543210, offset = i * 4)
    }
    protected.forEach { it.expectNone() }
  }
  runUntil(continuation)
  expectCondition(
      avoidedContinuation.snapshot().instructions == 0L,
      "Return to the supplied continuation without executing the other marker",
  )
  expectRegister("v0", id)
  checkCaller()
  inspect()
  runToExit()
  inspect()
  expectOutput("")
}

val signedWords = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -256, -1, 0, 1, 256, Int.MAX_VALUE)

for (a in signedWords) for (b in signedWords) case("sum $a $b", instructionLimit = 3000) {
  val exact = a.toLong() + b.toLong()
  val readonly =
      traceMemoryStores(
          "frame",
          (readDataAddress("state_guard") + 4 - readDataAddress("frame")).toInt(),
      )
  val continuation = if (((a xor b) and 1) == 0) "overflow_ready" else "done"
  val avoidedContinuation = traceExecution("overflow_ready", "done")
  prepareCaller(continuation)
  setRegister("a0", a)
  setRegister("a1", b)
  startAt("add_fixed_probe")
  runUntil(continuation)
  expectCondition(
      avoidedContinuation.snapshot().instructions == 0L,
      "Return to the supplied continuation without executing the other marker",
  )
  expectRegister("v0", exact.toInt())
  expectRegister("v1", exact)
  checkCaller()
  readonly.expectNone()
  runToExit()
  readonly.expectNone()
  expectOutput("")
}
