// Generated public behavior checks; edit the adjacent .cases.json and regenerate.
// Each case runs in a fresh simulator; labels and scratch registers are not checked.

case("input 0", input = "0\n", instructionLimit = 20000) {
  runToExit()
  expectOutput(
      "Enter an integer from 0 to 255: Unsigned byte: 0\nSigned byte: 0\nWrapped byte plus one: 1\n",
  )
}

case("input 1", input = "1\n", instructionLimit = 20000) {
  runToExit()
  expectOutput(
      "Enter an integer from 0 to 255: Unsigned byte: 1\nSigned byte: 1\nWrapped byte plus one: 2\n",
  )
}

case("input 127", input = "127\n", instructionLimit = 20000) {
  runToExit()
  expectOutput(
      "Enter an integer from 0 to 255: Unsigned byte: 127\nSigned byte: 127\nWrapped byte plus one: 128\n",
  )
}

case("input 128", input = "128\n", instructionLimit = 20000) {
  runToExit()
  expectOutput(
      "Enter an integer from 0 to 255: Unsigned byte: 128\nSigned byte: -128\nWrapped byte plus one: 129\n",
  )
}

case("input 150", input = "150\n", instructionLimit = 20000) {
  runToExit()
  expectOutput(
      "Enter an integer from 0 to 255: Unsigned byte: 150\nSigned byte: -106\nWrapped byte plus one: 151\n",
  )
}

case("input 254", input = "254\n", instructionLimit = 20000) {
  runToExit()
  expectOutput(
      "Enter an integer from 0 to 255: Unsigned byte: 254\nSigned byte: -2\nWrapped byte plus one: 255\n",
  )
}

case("input 255", input = "255\n", instructionLimit = 20000) {
  runToExit()
  expectOutput(
      "Enter an integer from 0 to 255: Unsigned byte: 255\nSigned byte: -1\nWrapped byte plus one: 0\n",
  )
}
