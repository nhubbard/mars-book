import edu.missouristate.mars.cli.grading.AssemblyCase
import java.lang.Math.floorDiv
import java.util.Random

data class FirTrial(val input: IntArray, val taps: IntArray) {
  init {
    require(input.size in 7..71)
    require(taps.size == 8)
    require((input.asSequence() + taps.asSequence()).all { it in -32768..32767 })
  }

  val count: Int
    get() = input.size - 7
}

fun exact(t: FirTrial) =
    LongArray(t.count) { i -> (0..7).sumOf { j -> t.input[i + j].toLong() * t.taps[j] } }

fun quantized(t: FirTrial) = exact(t).map { floorDiv(it + 16384, 32768) }.toLongArray()

fun expected(t: FirTrial) = quantized(t).map { it.coerceIn(-32768, 32767).toInt() }.toIntArray()

fun clips(t: FirTrial) = quantized(t).count { it !in -32768L..32767L }

fun bytes(samples: IntArray) =
    samples.flatMap { listOf(it and 255, (it ushr 8) and 255) }.toIntArray()

fun signed16(a: IntArray) =
    IntArray(a.size / 2) { (a[it * 2] + a[it * 2 + 1] * 256).toShort().toInt() }

fun randomTrial(n: Int, r: Random) =
    FirTrial(IntArray(n + 7) { r.nextInt(65536) - 32768 }, IntArray(8) { r.nextInt(65536) - 32768 })

fun single(input: IntArray, taps: IntArray) = FirTrial(input, taps)

check(
    expected(single(IntArray(8) { -32768 }, IntArray(8) { -32768 }))
        .contentEquals(intArrayOf(32767)),
)

check(exact(single(IntArray(8) { -32768 }, IntArray(8) { -32768 }))[0] == 8589934592L)

check(expected(single(IntArray(8) { 1 }, IntArray(8) { 16384 })).contentEquals(intArrayOf(4)))

check(floorDiv(-16384L + 16384, 32768) == 0L && floorDiv(16384L + 16384, 32768) == 1L)

fun AssemblyCase.checkFir(t: FirTrial, index: Int): IntArray {
  val src = if (index % 2 == 0) "source" else "output"
  val dest = if (index % 2 == 0) "output" else "source"
  val so = index % 32
  val co = (index * 7 + 1) % 32
  val oo = (index * 13 + 3) % 32
  val expected = expected(t)
  val encoded = bytes(expected)
  val n = t.count
  fun prepare(label: String, size: Int, offset: Int, values: IntArray, poison: Boolean = false) {
    val data = IntArray(size) { (it * 31 + index * 17 + 193) and 255 }
    for (i in values.indices) data[offset + i] = if (poison) values[i] xor 255 else values[i]
    for (i in 0 until size / 4) {
      var w = 0
      for (j in 0..3) w = w or (data[i * 4 + j] shl (8 * j))
      setWord(label, w, offset = i * 4)
    }
  }
  prepare(src, 192, so, bytes(t.input))
  prepare("coeff", 64, co, bytes(t.taps))
  prepare(dest, 192, oo, encoded, true)
  val root = readDataAddress("source_guard")
  val size = (readDataAddress("caller_end") + 4 - root).toInt()
  val before = readBytes("source_guard", size)
  val input = readDataAddress(src) + so
  val coefficients = readDataAddress("coeff") + co
  val out = readDataAddress(dest) + oo
  val history = traceMemoryAccesses("source_guard", size, maxAccesses = 20000)
  for (i in 0..7) setRegister("s$i", 0x3456789000000000L + i * 1009)
  setRegister("sp", if (index % 2 == 0) 0x7fff0000L else 0x7ffe8000L)
  val sp = if (index % 2 == 0) 0x7fff0000L else 0x7ffe8000L
  setRegister("fp", 0x2345678900000033L)
  setRegister("v0", 0x12345678)
  setRegister("v1", 0x23456789)
  setRegisterToDataAddress("a0", src, so.toLong())
  setRegisterToDataAddress("a1", "coeff", co.toLong())
  setRegisterToDataAddress("a2", dest, oo.toLong())
  setRegister("a3", n)
  val avoided = traceExecution("function_return", "done")
  callFunction("fir_q15", if (index % 2 == 0) "function_return" else "done")
  expectRegister("v0", n)
  expectRegister("v1", clips(t))
  expectRegister("sp", sp)
  expectRegister("fp", 0x2345678900000033L)
  for (i in 0..7) expectRegister("s$i", 0x3456789000000000L + i * 1009)
  expectCondition(avoided.snapshot().instructions == 0L, "Return uses supplied continuation")
  val result = readBytes(dest, n * 2, offset = oo)
  expectCondition(
      result.contentEquals(encoded),
      "Every sample equals exact accumulation, one rounding and saturation",
  )
  val after = readBytes("source_guard", size)
  for (i in before.indices) if (root + i !in out until out + n * 2)
      expectCondition(before[i] == after[i], "Only logical output changes")
  for (a in history.snapshot()) expectCondition(
      n > 0 &&
          (0 until a.length).all {
            val p = a.address + it
            if (a.write) p in out until out + n * 2
            else
                p in input until input + 2 * (n + 7) ||
                    p in coefficients until coefficients + 16 ||
                    p in out until out + n * 2
          },
      "Every caller-data access fits the selected slices; no access for zero outputs",
  )
  history.close()
  avoided.close()
  return signed16(result)
}

val trials = buildList {
  val r = Random(21012026L)
  for (n in listOf(1, 64)) for (value in listOf(-32768, 32767)) add(
      FirTrial(IntArray(n + 7) { value }, IntArray(8) { 4096 }),
  )
  for (n in 0..64) add(randomTrial(n, r))
  for (n in listOf(1, 2, 7, 8, 9, 16, 63, 64)) for (alignment in 0..31) add(randomTrial(n, r))
  for (position in 0..7) for (sample in listOf(-32768, -1, 0, 1, 32767)) for (coefficient in
      listOf(-32768, -16384, -1, 0, 1, 16384, 32767)) {
    val input = IntArray(8)
    val taps = IntArray(8)
    input[position] = sample
    taps[position] = coefficient
    add(FirTrial(input, taps))
  }
  for (n in listOf(1, 2, 8, 64)) for (x in listOf(-32768, -32767, 1, 32767)) for (c in
      listOf(-32768, -16384, 16384, 32767)) add(FirTrial(IntArray(n + 7) { x }, IntArray(8) { c }))
  // Asymmetric impulse response and two samples distinguish coefficient order and window movement.
  add(
      FirTrial(
          IntArray(23) { if (it == 7) 16384 else 0 },
          intArrayOf(1024, 2048, 3072, 4096, 5120, 6144, 7168, 8192),
      ),
  )
  add(
      FirTrial(
          IntArray(9) { if (it == 7) 16000 else if (it == 8) -8000 else 0 },
          IntArray(8) { 4096 },
      ),
  )
  add(FirTrial(intArrayOf(32767, 1, 1, 0, 0, 0, 0, 0), intArrayOf(32767, 32767, 1, 0, 0, 0, 0, 0)))
  add(
      FirTrial(
          intArrayOf(-32768, 3, 0, 0, 0, 0, 0, 0),
          intArrayOf(32767, -16384, 0, 0, 0, 0, 0, 0),
      ),
  )
  for (c in listOf(16383, 16384, 16385, -16383, -16384, -16385)) add(
      FirTrial(IntArray(8) { if (it == 0) 1 else 0 }, IntArray(8) { if (it == 0) c else 0 }),
  )
}

for ((index, t) in trials.withIndex()) case("fir $index n=${t.count}", instructionLimit = 100000) {
  checkFir(t, index)
  runToExit()
  expectOutput("")
}
