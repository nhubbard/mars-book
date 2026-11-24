import edu.missouristate.mars.cli.grading.AssemblyCase
import java.util.Random

data class MixTrial(val left: IntArray, val right: IntArray, val mode: Int = 0) {
  init {
    require(left.size == right.size && left.size <= 64)
    require((left.asSequence() + right.asSequence()).all { it in -32768..32767 })
    require(mode in 0..4)
    require(mode < 3 || left.contentEquals(right))
  }
}

fun trial(a: IntArray, b: IntArray, mode: Int) = MixTrial(a, if (mode >= 3) a.copyOf() else b, mode)

fun randomTrial(n: Int, mode: Int, r: Random) =
    trial(IntArray(n) { r.nextInt(65536) - 32768 }, IntArray(n) { r.nextInt(65536) - 32768 }, mode)

fun sums(t: MixTrial) = IntArray(t.left.size) { t.left[it] + t.right[it] }

fun saturated(t: MixTrial) = sums(t).map { it.coerceIn(-32768, 32767) }.toIntArray()

fun wrapped(t: MixTrial) = sums(t).map { it.toShort().toInt() }.toIntArray()

fun clipped(t: MixTrial) = sums(t).count { it !in -32768..32767 }

fun bytes(samples: IntArray) =
    samples.flatMap { listOf(it and 255, (it ushr 8) and 255) }.toIntArray()

fun signed16(a: IntArray) =
    IntArray(a.size / 2) { (a[it * 2] + a[it * 2 + 1] * 256).toShort().toInt() }

check(
    saturated(
            MixTrial(
                intArrayOf(32767, -32768, 30000, -30000, 30000, -30000),
                intArrayOf(1, -1, 2767, -2768, 2768, -2769),
            ),
        )
        .contentEquals(intArrayOf(32767, -32768, 32767, -32768, 32767, -32768)),
)

check(
    clipped(
        MixTrial(
            intArrayOf(32767, -32768, 30000, -30000, 30000, -30000),
            intArrayOf(1, -1, 2767, -2768, 2768, -2769),
        ),
    ) == 4,
)

fun AssemblyCase.checkMix(t: MixTrial, index: Int, wrap: Boolean = false): IntArray {
  val names = listOf("pcm_a", "pcm_b", "pcm_c", "pcm_d")
  val left = names[index % 4]
  val right = if (t.mode >= 3) left else names[(index + 1) % 4]
  val leftOffset = index % 32
  val rightOffset = if (t.mode >= 3) leftOffset else (index * 7 + 3) % 32
  val dest =
      when (t.mode) {
        1,
        4 -> left
        2 -> right
        else -> names[(index + 2) % 4]
      }
  val destOffset =
      when (t.mode) {
        1,
        4 -> leftOffset
        2 -> rightOffset
        else -> (index * 13 + 5) % 32
      }
  val expected = if (wrap) wrapped(t) else saturated(t)
  val expectedBytes = bytes(expected)
  val n = t.left.size
  fun prepare(label: String, offset: Int, samples: IntArray) {
    val data = IntArray(192) { (it * 31 + index * 17 + 193) and 255 }
    val values = bytes(samples)
    for (i in values.indices) data[offset + i] = values[i]
    for (i in 0 until 48) {
      var w = 0
      for (j in 0..3) w = w or (data[i * 4 + j] shl (8 * j))
      setWord(label, w, offset = i * 4)
    }
  }
  prepare(left, leftOffset, t.left)
  if (right != left) prepare(right, rightOffset, t.right)
  if (t.mode == 0 || t.mode == 3) {
    val data = IntArray(192) { (it * 13 + index * 37 + 79) and 255 }
    for (i in expectedBytes.indices) data[destOffset + i] = expectedBytes[i] xor 255
    for (i in 0 until 48) {
      var w = 0
      for (j in 0..3) w = w or (data[i * 4 + j] shl (8 * j))
      setWord(dest, w, offset = i * 4)
    }
  }
  val root = readDataAddress("pcm_a_guard")
  val size = (readDataAddress("caller_end") + 4 - root).toInt()
  val before = readBytes("pcm_a_guard", size)
  val a = readDataAddress(left) + leftOffset
  val b = readDataAddress(right) + rightOffset
  val out = readDataAddress(dest) + destOffset
  val length = n * 2
  val history = traceMemoryAccesses("pcm_a_guard", size, maxAccesses = 20000)
  for (i in 0..7) setRegister("s$i", 0x3456789000000000L + i * 1009)
  val sp = if (index % 2 == 0) 0x7fff0000L else 0x7ffe8000L
  setRegister("sp", sp)
  setRegister("fp", 0x2345678900000033L)
  setRegister("v0", 0x12345678)
  setRegister("v1", 0x23456789)
  setRegisterToDataAddress("a0", left, leftOffset.toLong())
  setRegisterToDataAddress("a1", right, rightOffset.toLong())
  setRegisterToDataAddress("a2", dest, destOffset.toLong())
  setRegister("a3", n)
  val avoided = traceExecution("function_return", "done")
  callFunction(
      if (wrap) "mix_wrap16" else "mix_pcm16",
      if (index % 2 == 0) "function_return" else "done",
  )
  expectRegister("v0", n)
  expectRegister("v1", if (wrap) 0 else clipped(t))
  expectRegister("sp", sp)
  expectRegister("fp", 0x2345678900000033L)
  for (i in 0..7) expectRegister("s$i", 0x3456789000000000L + i * 1009)
  expectCondition(avoided.snapshot().instructions == 0L, "Return through the supplied continuation")
  val result = readBytes(dest, length, offset = destOffset)
  expectCondition(
      result.contentEquals(expectedBytes),
      "Every mixed output byte equals the exact PCM rule",
  )
  val after = readBytes("pcm_a_guard", size)
  for (i in before.indices) if (root + i !in out until out + length)
      expectCondition(before[i] == after[i], "Only the selected logical destination changes")
  for (access in history.snapshot()) expectCondition(
      (0 until access.length).all {
        val p = access.address + it
        if (access.write) p in out until out + length
        else p in a until a + length || p in b until b + length || p in out until out + length
      },
      "Every access byte fits the selected logical slices",
  )
  history.close()
  avoided.close()
  return signed16(result)
}

val defaultA =
    intArrayOf(
        0,
        1000,
        -1000,
        30000,
        -30000,
        32767,
        -32768,
        20000,
        -20000,
        32767,
        -32768,
        12345,
        -12345,
        1,
        -1,
        0,
    )
val defaultB =
    intArrayOf(
        0,
        2000,
        -2000,
        10000,
        -10000,
        0,
        0,
        20000,
        -20000,
        1,
        -1,
        -12345,
        12345,
        -1,
        1,
        32767,
    )

for (n in 0..64) case("caller count $n", instructionLimit = 100000) {
  setWord("count", n)
  val a = signed16(readBytes("pcm_a", n * 2))
  val b = signed16(readBytes("pcm_b", n * 2))
  val t = MixTrial(a, b)
  val beforeA = readBytes("pcm_a", 192)
  val beforeB = readBytes("pcm_b", 192)
  runUntil("mix_ready")
  expectRegister("v0", n)
  expectRegister("v1", clipped(t))
  expectWord("processed", 99)
  expectWord("clipped", 99)
  expectWord("wrapped_count", 99)
  expectCondition(
      readBytes("pcm_c", n * 2).contentEquals(bytes(saturated(t))),
      "Saturated result ready before caller store",
  )
  runUntil("wrap_ready")
  expectRegister("v0", n)
  expectRegister("v1", 0)
  expectWord("processed", n)
  expectWord("clipped", clipped(t))
  expectWord("wrapped_count", 99)
  expectCondition(
      readBytes("pcm_d", n * 2).contentEquals(bytes(wrapped(t))),
      "Wrapping comparison ready",
  )
  runUntil("comparison_ready")
  expectWord("wrapped_count", n)
  expectCondition(
      readBytes("pcm_a", 192).contentEquals(beforeA) &&
          readBytes("pcm_b", 192).contentEquals(beforeB),
      "Caller sources unchanged",
  )
  for (j in n * 2 until 192) {
    expectBytes("pcm_c", 0, offset = j)
    expectBytes("pcm_d", 0, offset = j)
  }
  runToExit()
  expectOutput("")
}
