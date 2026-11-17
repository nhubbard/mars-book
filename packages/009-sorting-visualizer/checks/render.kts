fun exchanges(input: List<Int>, algorithm: Int): List<List<Int>> {
    val current = input.toMutableList()
    val states = mutableListOf(input.toList())
    if (algorithm == 0) {
        for (end in 1 until current.size) {
            var index = end
            while (index > 0 && current[index] < current[index - 1]) {
                val left = current[index - 1]
                current[index - 1] = current[index]
                current[index] = left
                states += current.toList()
                index--
            }
        }
    } else {
        for (left in 0 until (current.size - 1).coerceAtLeast(0)) {
            val minimum = current.subList(left, current.size).minOrNull()!!
            val index = (left until current.size).first { current[it] == minimum }
            if (index != left) {
                val value = current[left]
                current[left] = current[index]
                current[index] = value
                states += current.toList()
            }
        }
    }
    check(current == input.sorted())
    check(states.all { it.sorted() == input.sorted() })
    return states
}
fun pixel(state: List<Int>, x: Int, y: Int): Int =
    if (x < state.size && y >= 3 - state[x]) 0xffffff else 0

for (offset in 0..1) for (length in 0..(8-offset)) {
    case("renderer offset $offset count $length", instructionLimit = 10000) {
        val values = listOf(2,-2,3,-4,0,1,-3,-1)
        val input = values.drop(offset).take(length)
        val protected = listOf("values" to 32, "count" to 4, "algorithm" to 4, "phase" to 4, "swap_count" to 4,
            "frame_guard" to 4, "values_guard" to 4, "count_guard" to 4, "algorithm_guard" to 4, "phase_guard" to 4, "swaps_guard" to 4)
            .map { (name,size) -> traceMemoryStores(name,size) }
        for ((index,value) in values.withIndex()) setWord("values",value,offset=index*4)
        setWord("phase",99); setWord("swap_count",77)
        for (cell in 0 until 64) setWord("frame",0x12345678,offset=cell*4)
        for (index in 0..7) setRegister("s$index", 0x1122334455667700L+index)
        setRegister("sp",0x7fffeff0L)
        setRegisterToDataAddress("a0","values",offset=offset*4L)
        setRegister("a1",length)
        setRegisterToTextAddress("ra","done")
        startAt("render")
        runUntil("sort_ready")
        for (y in 0 until 8) for (x in 0 until 8) expectWord("frame",pixel(input,x,y),offset=(y*8+x)*4)
        runToExit()
        for (index in 0..7) expectRegister("s$index",0x1122334455667700L+index)
        expectRegister("sp",0x7fffeff0L)
        expectWord("phase",99); expectWord("swap_count",77)
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}
