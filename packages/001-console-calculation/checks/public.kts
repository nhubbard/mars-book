// Grade behavior; no scratch-register, instruction-count, or private-label contract.
case("input 20", input = "20\n", instructionLimit = 1000) {
  runToExit()
  expectOutput("Enter a small integer: Twice your number plus one is: 41\n")
}

case("input 0", input = "0\n", instructionLimit = 1000) {
  runToExit()
  expectOutput("Enter a small integer: Twice your number plus one is: 1\n")
}

case("input -3", input = "-3\n", instructionLimit = 1000) {
  runToExit()
  expectOutput("Enter a small integer: Twice your number plus one is: -5\n")
}
