import java.math.BigInteger

class EvaluationFailure(val status: Int, val offset: Int) : RuntimeException()

class Model(private val bytes: ByteArray) {
  private var at = 0
  private val min = BigInteger.valueOf(Int.MIN_VALUE.toLong())
  private val max = BigInteger.valueOf(Int.MAX_VALUE.toLong())

  private fun byte() = bytes[at].toInt() and 255

  private fun space() {
    while (at < bytes.size && byte() in listOf(9, 10, 13, 32)) at++
  }

  private fun checked(value: BigInteger, where: Int): BigInteger {
    if (value < min || value > max) throw EvaluationFailure(-2, where)
    return value
  }

  private fun primary(): BigInteger {
    space()
    if (at == bytes.size) throw EvaluationFailure(-1, at)
    if (byte() == 40) {
      at++
      val value = expression()
      space()
      if (at == bytes.size || byte() != 41) throw EvaluationFailure(-1, at)
      at++
      return value
    }
    if (byte() !in 48..57) throw EvaluationFailure(-1, at)
    var value = BigInteger.ZERO
    while (at < bytes.size && byte() in 48..57) {
      value = checked(value * BigInteger.TEN + BigInteger.valueOf((byte() - 48).toLong()), at)
      at++
    }
    return value
  }

  private fun unary(): BigInteger {
    space()
    if (at < bytes.size && byte() in listOf(43, 45)) {
      val sign = byte()
      val position = at++
      val value = unary()
      return if (sign == 45) checked(-value, position) else value
    }
    return primary()
  }

  private fun term(): BigInteger {
    var value = unary()
    while (true) {
      space()
      if (at == bytes.size || byte() !in listOf(42, 47)) return value
      val op = byte()
      val position = at++
      val right = unary()
      if (op == 47 && right == BigInteger.ZERO) throw EvaluationFailure(-3, position)
      value = checked(if (op == 42) value * right else value / right, position)
    }
  }

  private fun expression(): BigInteger {
    var value = term()
    while (true) {
      space()
      if (at == bytes.size || byte() !in listOf(43, 45)) return value
      val op = byte()
      val position = at++
      val right = term()
      value = checked(if (op == 43) value + right else value - right, position)
    }
  }

  fun result(): Pair<Int, Int> =
      try {
        val value = expression()
        space()
        if (at != bytes.size) throw EvaluationFailure(-1, at)
        0 to value.toInt()
      } catch (e: EvaluationFailure) {
        e.status to e.offset
      }
}

val programs =
    listOf(
        "18 - 3 * (2 + -4)",
        "1+2*3",
        "1-2-3",
        "8/3/2",
        "-7/3",
        "--5",
        "",
        " ",
        "1+",
        "(1",
        "1 2",
        "1/(2-2)",
        "1/(2-2",
        "2147483647+1?",
        "2147483647+1*?",
        "2147483648",
        "-2147483648",
        "(-2147483647-1)",
        "-(-2147483647-1)",
        "(-2147483647-1)/-1",
    )

for ((index, text) in programs.withIndex()) case("caller $index", instructionLimit = 100000) {
  val bytes = text.encodeToByteArray()
  val padded = ByteArray(96) { 128.toByte() }
  bytes.copyInto(padded)
  for (i in 0 until 24) {
    var word = 0
    for (j in 0..3) word = word or ((padded[i * 4 + j].toInt() and 255) shl (j * 8))
    setWord("input_a", word, offset = i * 4)
  }
  setWord("length", bytes.size)
  val expected = Model(bytes).result()
  runUntil("expression_call")
  val length = (readDataAddress("state_end") + 4 - readDataAddress("input_a_guard")).toInt()
  val before = readBytes("input_a_guard", length)
  val history = traceMemoryAccesses("input_a_guard", length)
  runUntil("expression_ready")
  expectRegister("v0", expected.first)
  expectRegister("v1", expected.second)
  expectWord("status", 99)
  expectWord("result", 99)
  expectCondition(
      readBytes("input_a_guard", length).contentEquals(before),
      "Function leaves all caller data unchanged",
  )
  for (a in history.snapshot()) {
    expectCondition(!a.write, "No caller-data stores inside evaluator")
    expectCondition(
        a.address >= readDataAddress("input_a") &&
            a.address + a.length <= readDataAddress("input_a") + bytes.size,
        "Input reads fit the selected slice",
    )
  }
  history.close()
  runToExit()
  expectWord("status", expected.first)
  expectWord("result", expected.second)
  expectCondition(
      readBytes("input_a", 96).contentEquals(padded.map { it.toInt() and 255 }.toIntArray()),
      "Caller continuation preserves input",
  )
  expectOutput("")
}
