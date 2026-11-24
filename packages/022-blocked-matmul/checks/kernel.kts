import edu.missouristate.mars.cli.grading.AssemblyCase

// Public arithmetic and ownership oracle, authored from this exercise's contract.
data class MatrixTrial(
    val m: Int,
    val k: Int,
    val n: Int,
    val tile: Int,
    val seed: Int = 0,
    val pattern: String = "mixed",
)

fun matrixInput(size: Int, seed: Int, pattern: String, left: Boolean): IntArray {
  val extremes = intArrayOf(-32768, 32767, 0, 1, -1, 16384, -16384, 123)
  return IntArray(size) { index ->
    when (pattern) {
      "minimum" -> -32768
      "alternating" -> if ((index + seed) % 2 == 0) -32768 else 32767
      "hand" -> if (left) index + 1 else index + 7
      "quiet" -> if (left) index % 11 - 5 else index % 7 - 3
      else -> extremes[Math.floorMod(index * (if (left) 3 else 5) + seed, extremes.size)]
    }
  }
}

fun MatrixTrial.product(a: IntArray, b: IntArray): LongArray =
    LongArray(m * n) { cell ->
      val row = cell / n
      val column = cell % n
      (0 until k).sumOf { q -> a[row * k + q].toLong() * b[q * n + column] }
    }

fun AssemblyCase.verifyProduct(
    trial: MatrixTrial,
    method: Int,
    caller: Boolean = false,
    finishCaller: Boolean = true,
    continuation: String = "function_return",
) {
  val (m, k, n, tile) = trial
  val ao = if (caller) 0 else Math.floorMod(m + k + trial.seed, 4)
  val bo = if (caller) 0 else Math.floorMod(k + n + trial.seed * 3, 6)
  val co = if (caller) 0 else Math.floorMod(m + n + trial.seed * 5, 8)
  val a = matrixInput(m * k, trial.seed, trial.pattern, true)
  val b =
      matrixInput(k * n, trial.seed + if (trial.pattern == "hand") 0 else 1, trial.pattern, false)
  val expected = trial.product(a, b)
  for (j in 0 until 272) {
    setWord("left", if (j in ao until ao + a.size) a[j - ao] else 0x12345678, offset = j * 4)
    setWord("right", if (j in bo until bo + b.size) b[j - bo] else 0x23456789, offset = j * 4)
  }
  for (j in 0 until 544) setWord("product", 0x5a5aa5a5, offset = j * 4)
  for ((j, value) in listOf(m, k, n, tile).withIndex()) setWord("shape", value, offset = j * 4)
  setWord("method", method)
  setWord("completed", -1)
  val saved = LongArray(9) { 0x3456789a00000000L + it * 1009 }
  for (j in 0..7) setRegister("s$j", saved[j])
  setRegister("fp", saved[8])
  setRegister("sp", 0x7fff0000L)
  if (caller) runUntil("matmul_call")
  else {
    setRegisterToDataAddress("a0", "left", ao * 4L)
    setRegisterToDataAddress("a1", "right", bo * 4L)
    setRegisterToDataAddress("a2", "product", co * 8L)
    setRegisterToDataAddress("a3", "shape")
  }
  val first = readDataAddress("left", ao * 4L)
  val second = readDataAddress("right", bo * 4L)
  val output = readDataAddress("product", co * 8L)
  val descriptor = readDataAddress("shape")
  val root = readDataAddress("left")
  val size = (readDataAddress("completed") + 4 - root).toInt()
  val before = readBytes("left", size)
  val owned =
      listOf(
              "left" to 1088,
              "right" to 1088,
              "product" to 2176,
              "shape" to 16,
              "method" to 4,
              "completed" to 4,
              "left_end" to 4,
              "right_end" to 4,
              "product_end" to 4,
          )
          .map { (name, bytes) -> readDataAddress(name) to bytes }
  val history = traceMemoryAccesses(root, size, maxAccesses = 100000)
  if (caller) runUntil("matmul_done")
  else callFunction(if (method == 0) "matmul_naive" else "matmul_tiled", continuation)
  history.close()
  expectRegister("v0", m * n)
  expectRegister("sp", 0x7fff0000L)
  expectRegister("fp", saved[8])
  for (j in 0..7) expectRegister("s$j", saved[j])
  expectWord("completed", -1)
  fun fits(address: Long, length: Int, base: Long, bytes: Int) =
      address >= base && length.toLong() <= bytes && address - base <= bytes.toLong() - length
  val initialized = BooleanArray(m * n * 8)
  for (access in history.snapshot()) {
    val product = fits(access.address, access.length, output, m * n * 8)
    val input =
        m * n > 0 &&
            (fits(access.address, access.length, first, m * k * 4) ||
                fits(access.address, access.length, second, k * n * 4))
    val shape = fits(access.address, access.length, descriptor, 16)
    val touchesCaller = owned.any { (base, bytes) ->
      access.address < base + bytes && access.address + access.length > base
    }
    expectCondition(
        if (access.write) product else product || input || shape || !touchesCaller,
        "Respect caller objects and active matrix intervals; submission-owned constants may be read",
    )
    if (product)
        for (j in
            (access.address - output).toInt() until
                (access.address - output).toInt() + access.length) {
          if (access.write) initialized[j] = true
          else
              expectCondition(
                  initialized[j],
                  "Initialize each output byte before reading a partial sum",
              )
        }
  }
  expectCondition(initialized.all { it }, "Write every selected output byte during this call")
  fun verifyMemory() {
    for (j in 0 until 272) {
      expectWord("left", if (j in ao until ao + a.size) a[j - ao] else 0x12345678, offset = j * 4)
      expectWord("right", if (j in bo until bo + b.size) b[j - bo] else 0x23456789, offset = j * 4)
      val d = j - co
      val value = if (d in expected.indices) expected[d] else 0x5a5aa5a55a5aa5a5L
      expectWord("product", value.toInt(), offset = j * 8)
      expectWord("product", (value ushr 32).toInt(), offset = j * 8 + 4)
    }
    val after = readBytes("left", size)
    for (j in before.indices) if (
        root + j !in output until output + m * n * 8 &&
            root + j !in readDataAddress("completed") until readDataAddress("completed") + 4
    )
        expectCondition(before[j] == after[j], "Preserve every other caller byte")
  }
  verifyMemory()
  if (finishCaller) {
    runUntil("results_ready")
    expectWord("completed", m * n)
    verifyMemory()
    runToExit()
    verifyMemory()
    expectOutput("")
  }
}

val publicShapes =
    listOf(
        Triple(0, 3, 2),
        Triple(2, 3, 0),
        Triple(2, 0, 2),
        Triple(1, 1, 1),
        Triple(2, 3, 2),
        Triple(3, 2, 5),
        Triple(5, 7, 3),
        Triple(1, 16, 3),
        Triple(16, 1, 15),
        Triple(15, 13, 11),
        Triple(16, 16, 16),
    )

for ((m, k, n) in publicShapes) for (tile in 1..8) for (method in 0..1) case(
    "public $m $k $n tile $tile method $method",
    instructionLimit = 1000000,
) {
  verifyProduct(
      MatrixTrial(m, k, n, tile, m + k + n, if (m == 2 && k == 3 && n == 2) "hand" else "mixed"),
      method,
  )
}

for (method in 0..1) case(
    "two-product signed-word boundary method $method",
    instructionLimit = 1000000,
) {
  verifyProduct(MatrixTrial(1, 2, 1, 2, pattern = "minimum"), method)
}
