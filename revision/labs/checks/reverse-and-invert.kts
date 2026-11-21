// Public formative checks. Implementation registers and stack layout may differ.
case("all byte inputs and preservation across repeated calls", instructionLimit = 100000) {
    val top = readRegister("sp") and -16L
    setRegister("sp", top)
    val saved = (0..7).associate { "s$it" to (0x1122334400000000L + it * 0x01010101L) } +
        mapOf("fp" to 0x0123456789abcdefL, "gp" to 0x023456789abcdef0L)
    saved.forEach { (register, value) -> setRegister(register, value) }
    val callerBytes = readBytes(top, 32)
    val helper = traceExecution("reverse_byte", "reverse_byte_return")
    for (input in 0..255) {
        // Set high bits too: only the low byte is part of this function's input.
        setRegister("a0", 0x1234567800000000L or input.toLong())
        val before = helper.snapshot().instructions
        callFunction("reverse_and_invert", returnTo = "after_invert")
        expectCondition(helper.snapshot().instructions > before, "The provided helper was not executed")
        val reversed = Integer.reverse(input) ushr 24
        expectRegister("v0", (reversed xor 255).toLong())
        expectRegister("sp", top)
        saved.forEach { (register, value) -> expectRegister(register, value) }
        expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
        expectOutput("")
    }
}
