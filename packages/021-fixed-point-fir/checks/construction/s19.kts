case("s19-1 unaligned", instructionLimit = 100000) {
  fun put(label: String, values: IntArray) {
    for (i in 0 until values.size / 4) {
      var w = 0
      for (j in 0..3) w = w or (values[i * 4 + j] shl (j * 8))
      setWord(label, w, offset = i * 4)
    }
  }
  val input = IntArray(192)
  val coefficients = IntArray(64)
  val x = listOf<Int>(0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128)
  val c = listOf<Int>(0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128)
  for (j in x.indices) input[j + 3] = x[j]
  for (j in c.indices) coefficients[j + 5] = c[j]
  put("source", input)
  put("coeff", coefficients)
  put("output", IntArray(192) { 165 })
  setWord("count", 1)
  val root = readDataAddress("source_guard")
  val size = (readDataAddress("caller_end") + 4 - root).toInt()
  val before = readBytes("source_guard", size)
  val first = readDataAddress("source") + 3
  val taps = readDataAddress("coeff") + 5
  val output = readDataAddress("output") + 7
  for (i in 0..7) setRegister("s$i", 0x3456789000000000L + i * 1009)
  setRegister("sp", 0x7fff0000L)
  setRegister("fp", 0x2345678900000033L)
  setRegister("v0", 123)
  setRegister("v1", 91)
  for (i in 0..9) setRegister("t$i", 0x456789ab00000000L + i * 17)
  setRegisterToDataAddress("a0", "source", 3)
  setRegisterToDataAddress("a1", "coeff", 5)
  setRegisterToDataAddress("a2", "output", 7)
  setRegister("a3", 1)
  val history = traceMemoryAccesses("source_guard", size, maxAccesses = 20000)
  callFunction("fir_q15", "function_return")
  expectRegister("v0", 1)
  expectRegister("sp", 0x7fff0000L)
  expectRegister("fp", 0x2345678900000033L)
  for (i in 0..7) expectRegister("s$i", 0x3456789000000000L + i * 1009)
  expectRegister("t0", 32767L)
  expectRegister("v1", 0L)
  val expected = listOf<Int>()
  for (j in 0 until 2) expectBytes(
      "output",
      if (j < expected.size) expected[j] else 165,
      offset = 7 + j,
  )
  val after = readBytes("source_guard", size)
  for (j in before.indices) if (root + j !in output until output + 2)
      expectCondition(before[j] == after[j], "Other caller bytes preserved")
  for (access in history.snapshot()) expectCondition(
      1 > 0 &&
          (0 until access.length).all {
            val b = access.address + it
            if (access.write) b in output until output + 2
            else
                b in first until first + 16 ||
                    b in taps until taps + 16 ||
                    b in output until output + 2
          },
      "Every access byte fits logical slices; empty output has no caller access",
  )
  history.close()
  runToExit()
  expectOutput("")
}

case("s19-1 caller", instructionLimit = 100000) {
  fun put(label: String, values: IntArray) {
    for (i in 0 until values.size / 4) {
      var w = 0
      for (j in 0..3) w = w or (values[i * 4 + j] shl (j * 8))
      setWord(label, w, offset = i * 4)
    }
  }
  val input = IntArray(192)
  val coefficients = IntArray(64)
  val x = listOf<Int>(0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128)
  val c = listOf<Int>(0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128, 0, 128)
  for (j in x.indices) input[j + 0] = x[j]
  for (j in c.indices) coefficients[j + 0] = c[j]
  put("source", input)
  put("coeff", coefficients)
  put("output", IntArray(192) { 165 })
  setWord("count", 1)
  val root = readDataAddress("source_guard")
  val size = (readDataAddress("caller_end") + 4 - root).toInt()
  val before = readBytes("source_guard", size)
  val first = readDataAddress("source") + 0
  val taps = readDataAddress("coeff") + 0
  val output = readDataAddress("output") + 0
  for (i in 0..7) setRegister("s$i", 0x3456789000000000L + i * 1009)
  setRegister("sp", 0x7fff0000L)
  setRegister("fp", 0x2345678900000033L)
  setRegister("v0", 123)
  setRegister("v1", 91)
  for (i in 0..9) setRegister("t$i", 0x456789ab00000000L + i * 17)
  runUntil("filter_call")
  val history = traceMemoryAccesses("source_guard", size, maxAccesses = 20000)
  runUntil("filter_ready")
  expectRegister("v0", 1)
  expectRegister("sp", 0x7fff0000L)
  expectRegister("fp", 0x2345678900000033L)
  for (i in 0..7) expectRegister("s$i", 0x3456789000000000L + i * 1009)
  expectRegister("t0", 32767L)
  expectRegister("v1", 0L)
  val expected = listOf<Int>()
  for (j in 0 until 2) expectBytes(
      "output",
      if (j < expected.size) expected[j] else 165,
      offset = 0 + j,
  )
  val after = readBytes("source_guard", size)
  for (j in before.indices) if (root + j !in output until output + 2)
      expectCondition(before[j] == after[j], "Other caller bytes preserved")
  for (access in history.snapshot()) expectCondition(
      1 > 0 &&
          (0 until access.length).all {
            val b = access.address + it
            if (access.write) b in output until output + 2
            else
                b in first until first + 16 ||
                    b in taps until taps + 16 ||
                    b in output until output + 2
          },
      "Every access byte fits logical slices; empty output has no caller access",
  )
  history.close()
  runToExit()
  expectOutput("")
}
