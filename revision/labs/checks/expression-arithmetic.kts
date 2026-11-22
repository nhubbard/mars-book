import java.math.BigInteger

// The small worked helper assumes signed-word operands and one of these four operators.
val values = listOf(Int.MIN_VALUE, -65536, -1, 0, 1, 46340, 46341, Int.MAX_VALUE)
for (left in values) for (right in values) for (op in "+-*/") {
    case("checked $left $op $right", instructionLimit = 1000) {
        val position = 19
        val a = BigInteger.valueOf(left.toLong()); val b = BigInteger.valueOf(right.toLong())
        val candidate = if (op == '/' && right == 0) null else when (op) {
            '+' -> a + b; '-' -> a - b; '*' -> a * b; else -> a / b
        }
        val status = if (candidate == null) -3 else if (candidate < BigInteger.valueOf(Int.MIN_VALUE.toLong()) || candidate > BigInteger.valueOf(Int.MAX_VALUE.toLong())) -2 else 0
        for (index in 0..7) setRegister("s$index", 0x3456789a00000000L + index * 71L)
        setRegister("fp", 0x456789ab00000000L); setRegister("sp", 0x7fff0000L)
        setRegister("a0", left); setRegister("a1", right); setRegister("a2", op.code); setRegister("a3", position)
        val history = traceMemoryAccesses("product", 12)
        try { callFunction("checked_binary", "function_return") } finally { history.close() }
        expectRegister("v0", status); expectRegister("v1", if (status == 0) candidate!!.toLong() else position.toLong())
        for (index in 0..7) expectRegister("s$index", 0x3456789a00000000L + index * 71L)
        expectRegister("fp", 0x456789ab00000000L); expectRegister("sp", 0x7fff0000L)
        expectCondition(history.snapshot().isEmpty(), "Arithmetic helper does not access caller storage")
        expectOutput("")
    }
}
case("worked multiplication then subtraction", instructionLimit = 1000) {
    startAt("main"); runUntil("arithmetic_stored")
    expectRegister("v0", 0); expectRegister("v1", 24)
    expectWord("product", -6); expectWord("status", 0); expectWord("result", 24)
    runToExit(); expectOutput("")
}
