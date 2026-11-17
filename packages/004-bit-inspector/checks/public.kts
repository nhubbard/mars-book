fun fullBytes(value: Long): IntArray = IntArray(8) {
    ((value ushr (it * 8)) and 255L).toInt()
}
val inputs = listOf(-1, 0, Int.MAX_VALUE)
for ((index, input) in inputs.withIndex()) {
    case("word $index: $input", instructionLimit = 1000) {
        setWord("sample", input)
        val firstGuard = 0x13579bdf xor index
        val lastGuard = 0x2468ace0 xor index
        setWord("sample_guard", firstGuard)
        setWord("results_guard", lastGuard)
        for (field in listOf("signed_value", "unsigned_value", "word_plus_one", "wide_plus_one")) {
            setWord(field, 0x55555555)
            setWord(field, 0x55555555, offset = 4)
        }
        runToExit()
        expectBytes("signed_value", *fullBytes(input.toLong()))
        expectBytes("unsigned_value", *fullBytes(input.toUInt().toLong()))
        expectBytes("word_plus_one", *fullBytes((input.toLong() + 1).toInt().toLong()))
        expectBytes("wide_plus_one", *fullBytes(input.toLong() + 1))
        expectWord("sample", input)
        expectWord("sample_guard", firstGuard)
        expectWord("results_guard", lastGuard)
        val hex = input.toUInt().toString(16).padStart(8, '0')
        val binary = input.toUInt().toString(2).padStart(32, '0')
        expectOutput("signed: $input\nunsigned: ${input.toUInt()}\nhex: 0x$hex\nbinary: $binary\n")
    }
}
