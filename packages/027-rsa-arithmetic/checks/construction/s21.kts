fun octets(s: String) = IntArray(s.length / 2) { s.substring(2 * it, 2 * it + 2).toInt(16) }

case("s21-1 offset function", instructionLimit = 20000000) {
  val saved = List(9) { 0x456789ab00000000L + it * 509 }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val top = readDataAddress("workspace", 1024)
  setRegister("sp", top)
  for (i in 0 until 1024 step 4) setWord("workspace", 0xa5a5a5a5.toInt(), i)
  val initial_left =
      octets(
          "a5a5a5a560ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_left.indices step 4) setWord(
      "left",
      (0..3).fold(0) { v, j -> v or (initial_left[i + j] shl (8 * j)) },
      i,
  )
  val initial_right =
      octets(
          "a5a5a5a5a5a5a5a5a5a5a5a560ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_right.indices step 4) setWord(
      "right",
      (0..3).fold(0) { v, j -> v or (initial_right[i + j] shl (8 * j)) },
      i,
  )
  val initial_modulus =
      octets(
          "a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a561ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_modulus.indices step 4) setWord(
      "modulus",
      (0..3).fold(0) { v, j -> v or (initial_modulus[i + j] shl (8 * j)) },
      i,
  )
  val initial_exponent =
      octets(
          "a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a511000000000000000000000000000000a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_exponent.indices step 4) setWord(
      "exponent",
      (0..3).fold(0) { v, j -> v or (initial_exponent[i + j] shl (8 * j)) },
      i,
  )
  val initial_output =
      octets(
          "a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_output.indices step 4) setWord(
      "output",
      (0..3).fold(0) { v, j -> v or (initial_output[i + j] shl (8 * j)) },
      i,
  )
  setWord("mode", 3)
  setWord("status", -1)
  setRegisterToDataAddress("a0", "left", 4L)
  setRegisterToDataAddress("a1", "right", 12L)
  setRegisterToDataAddress("a2", "modulus", 20L)
  setRegisterToDataAddress("a3", "output", 4L)
  setRegisterToTextAddress("ra", "function_return")
  startAt("rsa_addmod128")
  val base = readDataAddress("left")
  val size = (readDataAddress("status") + 4 - base).toInt()
  val before = readBytes("left", size)
  val trace = traceMemoryAccesses(base, size, maxAccesses = 2000000)
  val reads: List<Pair<Long, Int>> =
      listOf(
          readDataAddress("left", 4L) to 16,
          readDataAddress("modulus", 20L) to 16,
          readDataAddress("right", 12L) to 16,
          readDataAddress("output", 4L) to 16,
      )
  val writes: List<Pair<Long, Int>> = listOf(readDataAddress("output", 4L) to 16)
  fun fits(a: Long, n: Int, b: Long, s: Int) = a >= b && n <= s && a - b <= s.toLong() - n
  runUntil("function_return")
  expectCondition(
      readBytes("left", 128)
          .contentEquals(
              octets(
                  "a5a5a5a560ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "left agrees with independent phase model",
  )
  expectCondition(
      readBytes("right", 128)
          .contentEquals(
              octets(
                  "a5a5a5a5a5a5a5a5a5a5a5a560ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "right agrees with independent phase model",
  )
  expectCondition(
      readBytes("modulus", 128)
          .contentEquals(
              octets(
                  "a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a561ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "modulus agrees with independent phase model",
  )
  expectCondition(
      readBytes("exponent", 128)
          .contentEquals(
              octets(
                  "a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a511000000000000000000000000000000a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "exponent agrees with independent phase model",
  )
  expectCondition(
      readBytes("output", 256)
          .contentEquals(
              octets(
                  "a5a5a5a55fffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "output agrees with independent phase model",
  )
  expectRegister("v0", 0)
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", top)
  expectOutput("")
  trace.close()
  for (x in trace.snapshot()) expectCondition(
      (if (x.write) writes else reads).any { (b, n) -> fits(x.address, x.length, b, n) },
      "Caller access stays within selected objects",
  )
  val after = readBytes("left", size)
  for (i in before.indices) if (writes.none { (b, n) -> fits(base + i, 1, b, n) })
      expectCondition(before[i] == after[i], "Caller bytes outside the output are preserved")
}

case("s21-1 caller", instructionLimit = 20000000) {
  val saved = List(9) { 0x456789ab00000000L + it * 509 }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val top = readDataAddress("workspace", 1024)
  setRegister("sp", top)
  for (i in 0 until 1024 step 4) setWord("workspace", 0xa5a5a5a5.toInt(), i)
  val initial_left =
      octets(
          "60ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_left.indices step 4) setWord(
      "left",
      (0..3).fold(0) { v, j -> v or (initial_left[i + j] shl (8 * j)) },
      i,
  )
  val initial_right =
      octets(
          "60ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_right.indices step 4) setWord(
      "right",
      (0..3).fold(0) { v, j -> v or (initial_right[i + j] shl (8 * j)) },
      i,
  )
  val initial_modulus =
      octets(
          "61ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_modulus.indices step 4) setWord(
      "modulus",
      (0..3).fold(0) { v, j -> v or (initial_modulus[i + j] shl (8 * j)) },
      i,
  )
  val initial_exponent =
      octets(
          "11000000000000000000000000000000a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_exponent.indices step 4) setWord(
      "exponent",
      (0..3).fold(0) { v, j -> v or (initial_exponent[i + j] shl (8 * j)) },
      i,
  )
  val initial_output =
      octets(
          "a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
      )
  for (i in initial_output.indices step 4) setWord(
      "output",
      (0..3).fold(0) { v, j -> v or (initial_output[i + j] shl (8 * j)) },
      i,
  )
  setWord("mode", 3)
  setWord("status", -1)
  startAt("main")
  runUntil("addmod128_call")
  val base = readDataAddress("left")
  val size = (readDataAddress("status") + 4 - base).toInt()
  val before = readBytes("left", size)
  val trace = traceMemoryAccesses(base, size, maxAccesses = 2000000)
  val reads: List<Pair<Long, Int>> =
      listOf(
          readDataAddress("left", 0L) to 16,
          readDataAddress("modulus", 0L) to 16,
          readDataAddress("right", 0L) to 16,
          readDataAddress("output", 0L) to 16,
      )
  val writes: List<Pair<Long, Int>> = listOf(readDataAddress("output", 0L) to 16)
  fun fits(a: Long, n: Int, b: Long, s: Int) = a >= b && n <= s && a - b <= s.toLong() - n
  runUntil("addmod128_return")
  expectCondition(
      readBytes("left", 128)
          .contentEquals(
              octets(
                  "60ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "left agrees with independent phase model",
  )
  expectCondition(
      readBytes("right", 128)
          .contentEquals(
              octets(
                  "60ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "right agrees with independent phase model",
  )
  expectCondition(
      readBytes("modulus", 128)
          .contentEquals(
              octets(
                  "61ffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "modulus agrees with independent phase model",
  )
  expectCondition(
      readBytes("exponent", 128)
          .contentEquals(
              octets(
                  "11000000000000000000000000000000a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "exponent agrees with independent phase model",
  )
  expectCondition(
      readBytes("output", 256)
          .contentEquals(
              octets(
                  "5fffffffffffffffffffffffffffffffa5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5a5",
              ),
          ),
      "output agrees with independent phase model",
  )
  expectRegister("v0", 0)
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", top)
  expectOutput("")
  trace.close()
  for (x in trace.snapshot()) expectCondition(
      (if (x.write) writes else reads).any { (b, n) -> fits(x.address, x.length, b, n) },
      "Caller access stays within selected objects",
  )
  val after = readBytes("left", size)
  for (i in before.indices) if (writes.none { (b, n) -> fits(base + i, 1, b, n) })
      expectCondition(before[i] == after[i], "Caller bytes outside the output are preserved")
  runToExit()
  expectWord("status", 0)
}
