// Observe complete native function work; guest budget and native instructions have different units.
val image = byteArrayOf(1,0,1,3,32,49,16,34,35,16,34,1,1,17,48,4,33,0)
for (budget in listOf(0, 64)) case("countdown native work at guest budget $budget", instructionLimit=100000) {
    val padded = ByteArray(96); image.copyInto(padded)
    for (i in 0 until 24) {
        var word = 0
        for (j in 0..3) word = word or ((padded[4*i+j].toInt() and 255) shl (8*j))
        setWord("code_a", word, 4*i)
    }
    val stack = readDataAddress("stack_a")
    setWord("state_a", 8, 16)
    setWord("state_a", stack.toInt(), 24)
    setWord("state_a", (stack ushr 32).toInt(), 28)
    setRegisterToDataAddress("a0", "code_a"); setRegister("a1", image.size)
    setRegisterToDataAddress("a2", "state_a"); setRegister("a3", budget)
    setRegister("sp", 0x7fff0000L)
    val work = traceExecution()
    try { callFunction("run_vm", "function_return") } finally { work.close() }
    val retired = if (budget == 0) 0 else 33
    expectRegister("v0", if (budget == 0) 0 else 1); expectRegister("v1", retired)
    expectWord("state_a", if (budget == 0) 0 else 18)
    expectWord("state_a", if (budget == 0) 0 else 1, 4)
    expectWord("state_a", retired, 8); expectWord("state_a", -1, 20)
    if (budget != 0) expectWord("stack_a", 6)
    expectOutput("")
    println("WORK,$budget,$retired,${work.snapshot().instructions}")
}
