import edu.missouristate.mars.cli.grading.AssemblyCase

data class Trial(val m: Int, val k: Int, val seed: Int)

fun AssemblyCase.verify(trial: Trial) {
  val (m, k, seed) = trial
  val mo = minOf(7, 256 - m * k).let { seed % (it + 1) }
  val vo = minOf(7, 16 - k).let { seed % (it + 1) }
  val oo = minOf(7, 16 - m).let { seed % (it + 1) }
  val palette = intArrayOf(-32768, 32767, 0, 1, -1, 16384, -16384)
  val matrix = IntArray(256) { 0x12345678 }
  val vector = IntArray(16) { 0x23456789 }
  for (index in 0 until m * k) matrix[mo + index] = palette[(index * 3 + seed) % palette.size]
  for (index in 0 until k) vector[vo + index] = palette[(index * 5 + seed + 1) % palette.size]
  for (index in matrix.indices) setWord("matrix", matrix[index], offset = index * 4)
  for (index in vector.indices) setWord("vector", vector[index], offset = index * 4)
  for (index in 0 until 32) setWord("output", 0x5a5aa5a5, offset = index * 4)
  setWord("shape", m)
  setWord("shape", k, offset = 4)
  val first = readDataAddress("matrix", mo * 4L)
  val second = readDataAddress("vector", vo * 4L)
  val output = readDataAddress("output", oo * 8L)
  val shape = readDataAddress("shape")
  val root = readDataAddress("matrix_guard")
  val size = (readDataAddress("completed") + 4 - root).toInt()
  val before = readBytes("matrix_guard", size)
  val history = traceMemoryAccesses(root, size, maxAccesses = 100000)
  setRegisterToDataAddress("a0", "matrix", mo * 4L)
  setRegisterToDataAddress("a1", "vector", vo * 4L)
  setRegisterToDataAddress("a2", "output", oo * 8L)
  setRegisterToDataAddress("a3", "shape")
  for (index in 0..7) setRegister("s$index", 0x1234567800000000L + index * 31)
  setRegister("fp", 0x2345678900000011L)
  setRegister("sp", 0x7fff0000L)
  callFunction("matvec", if (seed % 2 == 0) "function_return" else "matvec_done")
  history.close()
  expectRegister("v0", m)
  expectRegister("sp", 0x7fff0000L)
  expectRegister("fp", 0x2345678900000011L)
  for (index in 0..7) expectRegister("s$index", 0x1234567800000000L + index * 31)
  fun fits(address: Long, length: Int, base: Long, bytes: Int) =
      address >= base && length <= bytes && address - base <= bytes - length
  val initialized = BooleanArray(m * 8)
  for (access in history.snapshot()) {
    val destination = fits(access.address, access.length, output, m * 8)
    val inputs =
        m > 0 &&
            (fits(access.address, access.length, first, m * k * 4) ||
                fits(access.address, access.length, second, k * 4))
    expectCondition(
        if (access.write) destination
        else destination || inputs || fits(access.address, access.length, shape, 8),
        "Only selected matrix/vector, descriptor and initialized output caller bytes are accessible",
    )
    if (destination)
        for (index in
            (access.address - output).toInt() until
                (access.address - output).toInt() + access.length) {
          if (access.write) initialized[index] = true
          else expectCondition(initialized[index], "Initialize output before a partial-sum read")
        }
  }
  expectCondition(initialized.all { it }, "Initialize every selected output byte")
  for (row in 0 until m) {
    val expected =
        (0 until k).sumOf { col -> matrix[mo + row * k + col].toLong() * vector[vo + col] }
    expectWord("output", expected.toInt(), offset = (oo + row) * 8)
    expectWord("output", (expected ushr 32).toInt(), offset = (oo + row) * 8 + 4)
  }
  val after = readBytes("matrix_guard", size)
  for (index in before.indices) if (root + index !in output until output + m * 8)
      expectCondition(before[index] == after[index], "Preserve other caller bytes")
  expectOutput("")
}

for ((index, shape) in
    listOf(0 to 16, 16 to 0, 1 to 1, 1 to 2, 2 to 3, 3 to 5, 15 to 13, 16 to 16).withIndex()) case(
    "matrix-vector ${shape.first} by ${shape.second}",
    instructionLimit = 50000,
) {
  verify(Trial(shape.first, shape.second, index))
}

case("default caller publishes two complete sums", instructionLimit = 50000) {
  runUntil("matvec_done")
  expectRegister("v0", 2)
  expectWord("output", 50)
  expectWord("output", 0, offset = 4)
  expectWord("output", 122, offset = 8)
  expectWord("output", 0, offset = 12)
  runToExit()
  expectWord("completed", 2)
  expectOutput("")
}
