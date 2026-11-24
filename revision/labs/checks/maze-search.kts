// Generated public behavior checks; edit the adjacent .cases.json and regenerate.
// Each case runs in a fresh simulator. Maze checks use the documented public ABI.

import edu.missouristate.mars.cli.grading.ConsoleMazeCheck

case("two equal shortest routes", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "00000101010101010000010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101",
          0,
          9,
          0,
      )
      .verify(this)
  expectOutput("")
}

case("start equals goal", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
          63,
          63,
          1,
      )
      .verify(this)
  expectOutput("")
}

case("full queue capacity", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
          0,
          63,
          2,
      )
      .verify(this)
  expectOutput("")
}

case("opposite corner", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
          63,
          0,
          3,
      )
      .verify(this)
  expectOutput("")
}

case("no horizontal row wrap", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "01010101010101000001010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101",
          7,
          8,
          4,
      )
      .verify(this)
  expectOutput("")
}

case("diagonal is not adjacent", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "00010101010101010100010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101",
          0,
          9,
          5,
      )
      .verify(this)
  expectOutput("")
}

case("long corridor", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "00000000000000000101010101010100000000000000000000010101010101010000000000000000010101010101010000000000000000000001010101010101",
          0,
          56,
          6,
      )
      .verify(this)
  expectOutput("")
}

case("isolated start", input = "", instructionLimit = 250000) {
  ConsoleMazeCheck.Search(
          "01010101010101010101010101010101010101010101010101010100010101010101010101010101010101010101010101010101010101010101010101010100",
          27,
          63,
          7,
      )
      .verify(this)
  expectOutput("")
}
