case("all byte values against documented ranges", instructionLimit=200000) {
    val intervals=listOf(0 to 127,128 to 191,160 to 191,128 to 159,144 to 191,128 to 143,0 to 255)
    for ((low,high) in intervals) for (byte in 0..255) {
        setRegister("a0",byte);setRegister("a1",low);setRegister("a2",high)
        callFunction("within_range","done")
        expectRegister("v0",if(byte in low..high)1 else 0)
    }
    runToExit();expectOutput("")
}
case("complete range caller", instructionLimit=1000) {
    runToExit();expectWord("range_result",0);expectOutput("")
}
