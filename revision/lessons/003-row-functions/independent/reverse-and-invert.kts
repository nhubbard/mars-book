// Generated public function checks; regenerate with tools/function_lesson_cases.py.
// Implementation registers and frame offsets may differ. Each case owns a fresh simulator.

case("positive upper bits", instructionLimit = 100000) {
    setRegister("sp", 0x000000007fffeffcuL.toLong())
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    val saved = mapOf(
        "s0" to 0x1122334400000000uL.toLong(),
        "s1" to 0x1122334401010101uL.toLong(),
        "s2" to 0x1122334402020202uL.toLong(),
        "s3" to 0x1122334403030303uL.toLong(),
        "s4" to 0x1122334404040404uL.toLong(),
        "s5" to 0x1122334405050505uL.toLong(),
        "s6" to 0x1122334406060606uL.toLong(),
        "s7" to 0x1122334407070707uL.toLong(),
        "fp" to 0x1020304005060708uL.toLong(),
        "gp" to 0x0102030450607080uL.toLong(),
    )
    saved.forEach { (register, value) -> setRegister(register, value) }
    val callerBytes = readBytes(top, 32)
    val helper = traceExecution("reverse_byte", "reverse_byte_return")
    for (input in 0..255) {
        setRegister("a0", 0x1234567800000000uL.toLong() or input.toLong())
        val before = helper.snapshot().instructions
        callFunction("reverse_and_invert", returnTo = if (input % 2 == 0) "after_invert" else "after_repeat")
        expectCondition(helper.snapshot().instructions > before, "The provided helper was not executed")
        expectRegister("v0", ((Integer.reverse(input) ushr 24) xor 255).toLong())
        expectRegister("sp", top)
        saved.forEach { (register, value) -> expectRegister(register, value) }
        expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
        expectOutput("")
    }
}

case("sign bit set", instructionLimit = 100000) {
    setRegister("sp", 0x000000007fffe080uL.toLong())
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    val saved = mapOf(
        "s0" to 0xfedcba9800000000uL.toLong(),
        "s1" to 0xfedcba9801010101uL.toLong(),
        "s2" to 0xfedcba9802020202uL.toLong(),
        "s3" to 0xfedcba9803030303uL.toLong(),
        "s4" to 0xfedcba9804040404uL.toLong(),
        "s5" to 0xfedcba9805050505uL.toLong(),
        "s6" to 0xfedcba9806060606uL.toLong(),
        "s7" to 0xfedcba9807070707uL.toLong(),
        "fp" to 0xffdeb99c05060708uL.toLong(),
        "gp" to 0xeefc8ad850607080uL.toLong(),
    )
    saved.forEach { (register, value) -> setRegister(register, value) }
    val callerBytes = readBytes(top, 32)
    val helper = traceExecution("reverse_byte", "reverse_byte_return")
    for (input in 0..255) {
        setRegister("a0", 0x8000000000000000uL.toLong() or input.toLong())
        val before = helper.snapshot().instructions
        callFunction("reverse_and_invert", returnTo = if (input % 2 == 0) "after_invert" else "after_repeat")
        expectCondition(helper.snapshot().instructions > before, "The provided helper was not executed")
        expectRegister("v0", ((Integer.reverse(input) ushr 24) xor 255).toLong())
        expectRegister("sp", top)
        saved.forEach { (register, value) -> expectRegister(register, value) }
        expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
        expectOutput("")
    }
}

case("all upper bits set", instructionLimit = 100000) {
    setRegister("sp", 0x000000007fffd0f7uL.toLong())
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    val saved = mapOf(
        "s0" to 0xa5a55a5a00000000uL.toLong(),
        "s1" to 0xa5a55a5a01010101uL.toLong(),
        "s2" to 0xa5a55a5a02020202uL.toLong(),
        "s3" to 0xa5a55a5a03030303uL.toLong(),
        "s4" to 0xa5a55a5a04040404uL.toLong(),
        "s5" to 0xa5a55a5a05050505uL.toLong(),
        "s6" to 0xa5a55a5a06060606uL.toLong(),
        "s7" to 0xa5a55a5a07070707uL.toLong(),
        "fp" to 0xa4a7595e05060708uL.toLong(),
        "gp" to 0xb5856a1a50607080uL.toLong(),
    )
    saved.forEach { (register, value) -> setRegister(register, value) }
    val callerBytes = readBytes(top, 32)
    val helper = traceExecution("reverse_byte", "reverse_byte_return")
    for (input in 0..255) {
        setRegister("a0", 0xffffffffffffff00uL.toLong() or input.toLong())
        val before = helper.snapshot().instructions
        callFunction("reverse_and_invert", returnTo = if (input % 2 == 0) "after_invert" else "after_repeat")
        expectCondition(helper.snapshot().instructions > before, "The provided helper was not executed")
        expectRegister("v0", ((Integer.reverse(input) ushr 24) xor 255).toLong())
        expectRegister("sp", top)
        saved.forEach { (register, value) -> expectRegister(register, value) }
        expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
        expectOutput("")
    }
}
