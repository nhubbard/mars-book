import kotlin.random.Random

val edges =
    listOf(
        Int.MIN_VALUE,
        Int.MIN_VALUE + 1,
        -1025,
        -257,
        -256,
        -129,
        -128,
        -1,
        0,
        1,
        127,
        128,
        255,
        256,
        511,
        1024,
        Int.MAX_VALUE - 1,
        Int.MAX_VALUE,
    )
val weights = listOf(0, 1, 63, 64, 127, 128, 129, 192, 255, 256)
val examples = edges.flatMap { a -> edges.flatMap { b -> weights.map { w -> Triple(a, b, w) } } }
val random = Random(0x46495845)
val trials =
    examples + (0 until 128).map { Triple(random.nextInt(), random.nextInt(), random.nextInt(257)) }

case(
    "signed fixed interpolation endpoints, ties, ranges and caller state",
    instructionLimit = 500000,
) {
  val saved =
      (0..7).associate { "s$it" to (0x1122334400000000L + it) } +
          mapOf("fp" to 0x123456789abcdef0L, "gp" to 0x23456789abcdef01L)
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  saved.forEach { (name, value) -> setRegister(name, value) }
  val caller = readBytes(top, 32)
  val writes =
      listOf(
          traceMemoryStores("left_raw", 16),
          traceMemoryStores("answer", 8),
          traceMemoryStores(top, 32),
      )
  for ((index, trial) in trials.withIndex()) {
    val (a, b, w) = trial
    val numerator = (256L - w) * a.toLong() + w.toLong() * b.toLong()
    val expected = Math.floorDiv(numerator + 128, 256)
    expectCondition(
        expected in minOf(a, b).toLong()..maxOf(a, b).toLong(),
        "Rounded interpolation remains between endpoints",
    )
    setRegister("a0", a)
    setRegister("a1", b)
    setRegister("a2", w)
    callFunction(
        "interpolate_raw",
        returnTo = if (index % 2 == 0) "interpolate_done" else "interpolate_exit",
    )
    expectRegister("v0", expected)
    expectRegister("sp", top)
    saved.forEach { (name, value) -> expectRegister(name, value) }
    expectCondition(readBytes(top, 32).contentEquals(caller), "Caller stack remains intact")
  }
  writes.forEach { it.expectNone() }
  expectOutput("")
}

case("complete interpolation caller", instructionLimit = 1000) {
  runToExit()
  expectWord("answer", -64)
  expectOutput("")
}
