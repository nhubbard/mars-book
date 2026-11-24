case("pixel interface and final value", instructionLimit = 100) {
  expectDataAddress("pixel", 0x10010000L)
  expectDataAddress("neighbor", 0x10010004L)
  runToExit()
  expectWord("pixel", 0x003366ff)
  expectBytes("pixel", 0xff, 0x66, 0x33, 0x00)
  expectWord("neighbor", 0x13579bdf)
  expectOutput("")
}

case("preserve a caller supplied neighbor", instructionLimit = 100) {
  setWord("neighbor", 42)
  runToExit()
  expectWord("pixel", 0x003366ff)
  expectWord("neighbor", 42)
  expectOutput("")
}
