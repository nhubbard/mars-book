// Generated stable-partition checks; regenerate with tools/partition_lesson_cases.py.
// Word byte strings target the book little-endian environment.

case("stable partition 0 offset 0 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
      7,
      0,
      0,
      90,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 0, 0, 0, 106)
  setBytes("values_before", 223, 155, 87, 19)
  setBytes("values_guard", 222, 155, 87, 19)
  setBytes("count_guard", 221, 155, 87, 19)
  setBytes("result_guard", 220, 155, 87, 19)
  setRegister("s0", 0x1122334400000000uL.toLong())
  setRegister("s1", 0x1122334401010101uL.toLong())
  setRegister("s2", 0x1122334402020202uL.toLong())
  setRegister("s3", 0x1122334403030303uL.toLong())
  setRegister("s4", 0x1122334404040404uL.toLong())
  setRegister("s5", 0x1122334405050505uL.toLong())
  setRegister("s6", 0x1122334406060606uL.toLong())
  setRegister("s7", 0x1122334407070707uL.toLong())
  setRegister("fp", 0x1020304005060708uL.toLong())
  setRegister("gp", 0x0102030450607080uL.toLong())
  setRegister("sp", 0x000000007fffeffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
      7,
      0,
      0,
      90,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 0, 0, 0, 106)
  expectBytes("values_before", 223, 155, 87, 19)
  expectBytes("values_guard", 222, 155, 87, 19)
  expectBytes("count_guard", 221, 155, 87, 19)
  expectBytes("result_guard", 220, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
      7,
      0,
      0,
      90,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 0, 0, 0, 106)
  expectBytes("values_before", 223, 155, 87, 19)
  expectBytes("values_guard", 222, 155, 87, 19)
  expectBytes("count_guard", 221, 155, 87, 19)
  expectBytes("result_guard", 220, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 1 offset 0 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 1, 0, 0, 106)
  setBytes("values_before", 192, 155, 87, 19)
  setBytes("values_guard", 255, 155, 87, 19)
  setBytes("count_guard", 254, 155, 87, 19)
  setBytes("result_guard", 253, 155, 87, 19)
  setRegister("s0", 0x1122334401010101uL.toLong())
  setRegister("s1", 0x1122334402020202uL.toLong())
  setRegister("s2", 0x1122334403030303uL.toLong())
  setRegister("s3", 0x1122334404040404uL.toLong())
  setRegister("s4", 0x1122334405050505uL.toLong())
  setRegister("s5", 0x1122334406060606uL.toLong())
  setRegister("s6", 0x1122334407070707uL.toLong())
  setRegister("s7", 0x1122334408080808uL.toLong())
  setRegister("fp", 0x1020304004070609uL.toLong())
  setRegister("gp", 0x0102030451617181uL.toLong())
  setRegister("sp", 0x000000007fffdffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 1, 0, 0, 106)
  expectBytes("values_before", 192, 155, 87, 19)
  expectBytes("values_guard", 255, 155, 87, 19)
  expectBytes("count_guard", 254, 155, 87, 19)
  expectBytes("result_guard", 253, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 1, 0, 0, 106)
  expectBytes("values_before", 192, 155, 87, 19)
  expectBytes("values_guard", 255, 155, 87, 19)
  expectBytes("count_guard", 254, 155, 87, 19)
  expectBytes("result_guard", 253, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 2 offset 0 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 2, 0, 0, 106)
  setBytes("values_before", 225, 155, 87, 19)
  setBytes("values_guard", 224, 155, 87, 19)
  setBytes("count_guard", 159, 155, 87, 19)
  setBytes("result_guard", 158, 155, 87, 19)
  setRegister("s0", 0x1122334402020202uL.toLong())
  setRegister("s1", 0x1122334403030303uL.toLong())
  setRegister("s2", 0x1122334404040404uL.toLong())
  setRegister("s3", 0x1122334405050505uL.toLong())
  setRegister("s4", 0x1122334406060606uL.toLong())
  setRegister("s5", 0x1122334407070707uL.toLong())
  setRegister("s6", 0x1122334408080808uL.toLong())
  setRegister("s7", 0x1122334409090909uL.toLong())
  setRegister("fp", 0x102030400704050auL.toLong())
  setRegister("gp", 0x0102030452627282uL.toLong())
  setRegister("sp", 0x000000007fffcffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 2, 0, 0, 106)
  expectBytes("values_before", 225, 155, 87, 19)
  expectBytes("values_guard", 224, 155, 87, 19)
  expectBytes("count_guard", 159, 155, 87, 19)
  expectBytes("result_guard", 158, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
      5,
      0,
      0,
      90,
      6,
      0,
      0,
      90,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 2, 0, 0, 106)
  expectBytes("values_before", 225, 155, 87, 19)
  expectBytes("values_guard", 224, 155, 87, 19)
  expectBytes("count_guard", 159, 155, 87, 19)
  expectBytes("result_guard", 158, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 3 offset 0 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 3, 0, 0, 106)
  setBytes("values_before", 130, 155, 87, 19)
  setBytes("values_guard", 129, 155, 87, 19)
  setBytes("count_guard", 128, 155, 87, 19)
  setBytes("result_guard", 191, 155, 87, 19)
  setRegister("s0", 0x1122334403030303uL.toLong())
  setRegister("s1", 0x1122334404040404uL.toLong())
  setRegister("s2", 0x1122334405050505uL.toLong())
  setRegister("s3", 0x1122334406060606uL.toLong())
  setRegister("s4", 0x1122334407070707uL.toLong())
  setRegister("s5", 0x1122334408080808uL.toLong())
  setRegister("s6", 0x1122334409090909uL.toLong())
  setRegister("s7", 0x112233440a0a0a0auL.toLong())
  setRegister("fp", 0x102030400605040buL.toLong())
  setRegister("gp", 0x0102030453637383uL.toLong())
  setRegister("sp", 0x000000007fffbffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 3, 0, 0, 106)
  expectBytes("values_before", 130, 155, 87, 19)
  expectBytes("values_guard", 129, 155, 87, 19)
  expectBytes("count_guard", 128, 155, 87, 19)
  expectBytes("result_guard", 191, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 3, 0, 0, 106)
  expectBytes("values_before", 130, 155, 87, 19)
  expectBytes("values_guard", 129, 155, 87, 19)
  expectBytes("count_guard", 128, 155, 87, 19)
  expectBytes("result_guard", 191, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 4 offset 0 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      255,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 4, 0, 0, 106)
  setBytes("values_before", 163, 155, 87, 19)
  setBytes("values_guard", 162, 155, 87, 19)
  setBytes("count_guard", 161, 155, 87, 19)
  setBytes("result_guard", 160, 155, 87, 19)
  setRegister("s0", 0x1122334404040404uL.toLong())
  setRegister("s1", 0x1122334405050505uL.toLong())
  setRegister("s2", 0x1122334406060606uL.toLong())
  setRegister("s3", 0x1122334407070707uL.toLong())
  setRegister("s4", 0x1122334408080808uL.toLong())
  setRegister("s5", 0x1122334409090909uL.toLong())
  setRegister("s6", 0x112233440a0a0a0auL.toLong())
  setRegister("s7", 0x112233440b0b0b0buL.toLong())
  setRegister("fp", 0x102030400102030cuL.toLong())
  setRegister("gp", 0x0102030454647484uL.toLong())
  setRegister("sp", 0x000000007fffaffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      255,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 4, 0, 0, 106)
  expectBytes("values_before", 163, 155, 87, 19)
  expectBytes("values_guard", 162, 155, 87, 19)
  expectBytes("count_guard", 161, 155, 87, 19)
  expectBytes("result_guard", 160, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      255,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
      4,
      0,
      0,
      90,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 4, 0, 0, 106)
  expectBytes("values_before", 163, 155, 87, 19)
  expectBytes("values_guard", 162, 155, 87, 19)
  expectBytes("count_guard", 161, 155, 87, 19)
  expectBytes("result_guard", 160, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 5 offset 0 length 8", instructionLimit = 10000) {
  setBytes(
      "values",
      2,
      0,
      0,
      0,
      254,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
  )
  setBytes("count", 0, 0, 0, 0)
  setBytes("negative_count", 5, 0, 0, 106)
  setBytes("values_before", 68, 155, 87, 19)
  setBytes("values_guard", 67, 155, 87, 19)
  setBytes("count_guard", 66, 155, 87, 19)
  setBytes("result_guard", 65, 155, 87, 19)
  setRegister("s0", 0x1122334405050505uL.toLong())
  setRegister("s1", 0x1122334406060606uL.toLong())
  setRegister("s2", 0x1122334407070707uL.toLong())
  setRegister("s3", 0x1122334408080808uL.toLong())
  setRegister("s4", 0x1122334409090909uL.toLong())
  setRegister("s5", 0x112233440a0a0a0auL.toLong())
  setRegister("s6", 0x112233440b0b0b0buL.toLong())
  setRegister("s7", 0x112233440c0c0c0cuL.toLong())
  setRegister("fp", 0x102030400003020duL.toLong())
  setRegister("gp", 0x0102030455657585uL.toLong())
  setRegister("sp", 0x000000007fff9ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 32
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      254,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 5, 0, 0, 106)
  expectBytes("values_before", 68, 155, 87, 19)
  expectBytes("values_guard", 67, 155, 87, 19)
  expectBytes("count_guard", 66, 155, 87, 19)
  expectBytes("result_guard", 65, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      254,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 5, 0, 0, 106)
  expectBytes("values_before", 68, 155, 87, 19)
  expectBytes("values_guard", 67, 155, 87, 19)
  expectBytes("count_guard", 66, 155, 87, 19)
  expectBytes("result_guard", 65, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 6 offset 0 length 8", instructionLimit = 10000) {
  setBytes(
      "values",
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
  )
  setBytes("count", 0, 0, 0, 0)
  setBytes("negative_count", 6, 0, 0, 106)
  setBytes("values_before", 101, 155, 87, 19)
  setBytes("values_guard", 100, 155, 87, 19)
  setBytes("count_guard", 99, 155, 87, 19)
  setBytes("result_guard", 98, 155, 87, 19)
  setRegister("s0", 0x1122334406060606uL.toLong())
  setRegister("s1", 0x1122334407070707uL.toLong())
  setRegister("s2", 0x1122334408080808uL.toLong())
  setRegister("s3", 0x1122334409090909uL.toLong())
  setRegister("s4", 0x112233440a0a0a0auL.toLong())
  setRegister("s5", 0x112233440b0b0b0buL.toLong())
  setRegister("s6", 0x112233440c0c0c0cuL.toLong())
  setRegister("s7", 0x112233440d0d0d0duL.toLong())
  setRegister("fp", 0x102030400300010euL.toLong())
  setRegister("gp", 0x0102030456667686uL.toLong())
  setRegister("sp", 0x000000007fff8ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 32
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 8)
  expectBytes(
      "values",
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 6, 0, 0, 106)
  expectBytes("values_before", 101, 155, 87, 19)
  expectBytes("values_guard", 100, 155, 87, 19)
  expectBytes("count_guard", 99, 155, 87, 19)
  expectBytes("result_guard", 98, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 8)
  expectBytes(
      "values",
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 6, 0, 0, 106)
  expectBytes("values_before", 101, 155, 87, 19)
  expectBytes("values_guard", 100, 155, 87, 19)
  expectBytes("count_guard", 99, 155, 87, 19)
  expectBytes("result_guard", 98, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 7 offset 0 length 8", instructionLimit = 10000) {
  setBytes(
      "values",
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
  )
  setBytes("count", 0, 0, 0, 0)
  setBytes("negative_count", 7, 0, 0, 106)
  setBytes("values_before", 6, 155, 87, 19)
  setBytes("values_guard", 5, 155, 87, 19)
  setBytes("count_guard", 4, 155, 87, 19)
  setBytes("result_guard", 3, 155, 87, 19)
  setRegister("s0", 0x1122334407070707uL.toLong())
  setRegister("s1", 0x1122334408080808uL.toLong())
  setRegister("s2", 0x1122334409090909uL.toLong())
  setRegister("s3", 0x112233440a0a0a0auL.toLong())
  setRegister("s4", 0x112233440b0b0b0buL.toLong())
  setRegister("s5", 0x112233440c0c0c0cuL.toLong())
  setRegister("s6", 0x112233440d0d0d0duL.toLong())
  setRegister("s7", 0x112233440e0e0e0euL.toLong())
  setRegister("fp", 0x102030400201000fuL.toLong())
  setRegister("gp", 0x0102030457677787uL.toLong())
  setRegister("sp", 0x000000007fff7ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 32
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 7, 0, 0, 106)
  expectBytes("values_before", 6, 155, 87, 19)
  expectBytes("values_guard", 5, 155, 87, 19)
  expectBytes("count_guard", 4, 155, 87, 19)
  expectBytes("result_guard", 3, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 7, 0, 0, 106)
  expectBytes("values_before", 6, 155, 87, 19)
  expectBytes("values_guard", 5, 155, 87, 19)
  expectBytes("count_guard", 4, 155, 87, 19)
  expectBytes("result_guard", 3, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 8 offset 0 length 4", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
  )
  setBytes("count", 5, 0, 0, 0)
  setBytes("negative_count", 8, 0, 0, 106)
  setBytes("values_before", 39, 155, 87, 19)
  setBytes("values_guard", 38, 155, 87, 19)
  setBytes("count_guard", 37, 155, 87, 19)
  setBytes("result_guard", 36, 155, 87, 19)
  setRegister("s0", 0x1122334408080808uL.toLong())
  setRegister("s1", 0x1122334409090909uL.toLong())
  setRegister("s2", 0x112233440a0a0a0auL.toLong())
  setRegister("s3", 0x112233440b0b0b0buL.toLong())
  setRegister("s4", 0x112233440c0c0c0cuL.toLong())
  setRegister("s5", 0x112233440d0d0d0duL.toLong())
  setRegister("s6", 0x112233440e0e0e0euL.toLong())
  setRegister("s7", 0x112233440f0f0f0fuL.toLong())
  setRegister("fp", 0x102030400d0e0f00uL.toLong())
  setRegister("gp", 0x0102030458687888uL.toLong())
  setRegister("sp", 0x000000007fff6ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 16
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 8, 0, 0, 106)
  expectBytes("values_before", 39, 155, 87, 19)
  expectBytes("values_guard", 38, 155, 87, 19)
  expectBytes("count_guard", 37, 155, 87, 19)
  expectBytes("result_guard", 36, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      2,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 8, 0, 0, 106)
  expectBytes("values_before", 39, 155, 87, 19)
  expectBytes("values_guard", 38, 155, 87, 19)
  expectBytes("count_guard", 37, 155, 87, 19)
  expectBytes("result_guard", 36, 155, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 9 offset 0 length 4", instructionLimit = 10000) {
  setBytes(
      "values",
      255,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
  )
  setBytes("count", 5, 0, 0, 0)
  setBytes("negative_count", 9, 0, 0, 106)
  setBytes("values_before", 200, 154, 87, 19)
  setBytes("values_guard", 199, 154, 87, 19)
  setBytes("count_guard", 198, 154, 87, 19)
  setBytes("result_guard", 197, 154, 87, 19)
  setRegister("s0", 0x1122334409090909uL.toLong())
  setRegister("s1", 0x112233440a0a0a0auL.toLong())
  setRegister("s2", 0x112233440b0b0b0buL.toLong())
  setRegister("s3", 0x112233440c0c0c0cuL.toLong())
  setRegister("s4", 0x112233440d0d0d0duL.toLong())
  setRegister("s5", 0x112233440e0e0e0euL.toLong())
  setRegister("s6", 0x112233440f0f0f0fuL.toLong())
  setRegister("s7", 0x1122334410101010uL.toLong())
  setRegister("fp", 0x102030400c0f0e01uL.toLong())
  setRegister("gp", 0x0102030459697989uL.toLong())
  setRegister("sp", 0x000000007fff5ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 16
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      255,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 9, 0, 0, 106)
  expectBytes("values_before", 200, 154, 87, 19)
  expectBytes("values_guard", 199, 154, 87, 19)
  expectBytes("count_guard", 198, 154, 87, 19)
  expectBytes("result_guard", 197, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      255,
      255,
      255,
      255,
      252,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      254,
      255,
      255,
      255,
      0,
      0,
      0,
      90,
      1,
      0,
      0,
      90,
      2,
      0,
      0,
      90,
      3,
      0,
      0,
      90,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 9, 0, 0, 106)
  expectBytes("values_before", 200, 154, 87, 19)
  expectBytes("values_guard", 199, 154, 87, 19)
  expectBytes("count_guard", 198, 154, 87, 19)
  expectBytes("result_guard", 197, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 10 offset 0 length 8", instructionLimit = 10000) {
  setBytes(
      "values",
      255,
      255,
      255,
      127,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      254,
      255,
      255,
      255,
  )
  setBytes("count", 0, 0, 0, 0)
  setBytes("negative_count", 10, 0, 0, 106)
  setBytes("values_before", 233, 154, 87, 19)
  setBytes("values_guard", 232, 154, 87, 19)
  setBytes("count_guard", 231, 154, 87, 19)
  setBytes("result_guard", 230, 154, 87, 19)
  setRegister("s0", 0x112233440a0a0a0auL.toLong())
  setRegister("s1", 0x112233440b0b0b0buL.toLong())
  setRegister("s2", 0x112233440c0c0c0cuL.toLong())
  setRegister("s3", 0x112233440d0d0d0duL.toLong())
  setRegister("s4", 0x112233440e0e0e0euL.toLong())
  setRegister("s5", 0x112233440f0f0f0fuL.toLong())
  setRegister("s6", 0x1122334410101010uL.toLong())
  setRegister("s7", 0x1122334411111111uL.toLong())
  setRegister("fp", 0x102030400f0c0d02uL.toLong())
  setRegister("gp", 0x010203045a6a7a8auL.toLong())
  setRegister("sp", 0x000000007fff4ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 32
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      254,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 10, 0, 0, 106)
  expectBytes("values_before", 233, 154, 87, 19)
  expectBytes("values_guard", 232, 154, 87, 19)
  expectBytes("count_guard", 231, 154, 87, 19)
  expectBytes("result_guard", 230, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      254,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 10, 0, 0, 106)
  expectBytes("values_before", 233, 154, 87, 19)
  expectBytes("values_guard", 232, 154, 87, 19)
  expectBytes("count_guard", 231, 154, 87, 19)
  expectBytes("result_guard", 230, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 11 offset 0 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 11, 0, 0, 106)
  setBytes("values_before", 138, 154, 87, 19)
  setBytes("values_guard", 137, 154, 87, 19)
  setBytes("count_guard", 136, 154, 87, 19)
  setBytes("result_guard", 135, 154, 87, 19)
  setRegister("s0", 0x112233440b0b0b0buL.toLong())
  setRegister("s1", 0x112233440c0c0c0cuL.toLong())
  setRegister("s2", 0x112233440d0d0d0duL.toLong())
  setRegister("s3", 0x112233440e0e0e0euL.toLong())
  setRegister("s4", 0x112233440f0f0f0fuL.toLong())
  setRegister("s5", 0x1122334410101010uL.toLong())
  setRegister("s6", 0x1122334411111111uL.toLong())
  setRegister("s7", 0x1122334412121212uL.toLong())
  setRegister("fp", 0x102030400e0d0c03uL.toLong())
  setRegister("gp", 0x010203045b6b7b8buL.toLong())
  setRegister("sp", 0x000000007fff3ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 11, 0, 0, 106)
  expectBytes("values_before", 138, 154, 87, 19)
  expectBytes("values_guard", 137, 154, 87, 19)
  expectBytes("count_guard", 136, 154, 87, 19)
  expectBytes("result_guard", 135, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 11, 0, 0, 106)
  expectBytes("values_before", 138, 154, 87, 19)
  expectBytes("values_guard", 137, 154, 87, 19)
  expectBytes("count_guard", 136, 154, 87, 19)
  expectBytes("result_guard", 135, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 12 offset 0 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 12, 0, 0, 106)
  setBytes("values_before", 171, 154, 87, 19)
  setBytes("values_guard", 170, 154, 87, 19)
  setBytes("count_guard", 169, 154, 87, 19)
  setBytes("result_guard", 168, 154, 87, 19)
  setRegister("s0", 0x112233440c0c0c0cuL.toLong())
  setRegister("s1", 0x112233440d0d0d0duL.toLong())
  setRegister("s2", 0x112233440e0e0e0euL.toLong())
  setRegister("s3", 0x112233440f0f0f0fuL.toLong())
  setRegister("s4", 0x1122334410101010uL.toLong())
  setRegister("s5", 0x1122334411111111uL.toLong())
  setRegister("s6", 0x1122334412121212uL.toLong())
  setRegister("s7", 0x1122334413131313uL.toLong())
  setRegister("fp", 0x10203040090a0b04uL.toLong())
  setRegister("gp", 0x010203045c6c7c8cuL.toLong())
  setRegister("sp", 0x000000007fff2ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 12, 0, 0, 106)
  expectBytes("values_before", 171, 154, 87, 19)
  expectBytes("values_guard", 170, 154, 87, 19)
  expectBytes("count_guard", 169, 154, 87, 19)
  expectBytes("result_guard", 168, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 12, 0, 0, 106)
  expectBytes("values_before", 171, 154, 87, 19)
  expectBytes("values_guard", 170, 154, 87, 19)
  expectBytes("count_guard", 169, 154, 87, 19)
  expectBytes("result_guard", 168, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 13 offset 0 length 2", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 3, 0, 0, 0)
  setBytes("negative_count", 13, 0, 0, 106)
  setBytes("values_before", 76, 154, 87, 19)
  setBytes("values_guard", 75, 154, 87, 19)
  setBytes("count_guard", 74, 154, 87, 19)
  setBytes("result_guard", 73, 154, 87, 19)
  setRegister("s0", 0x112233440d0d0d0duL.toLong())
  setRegister("s1", 0x112233440e0e0e0euL.toLong())
  setRegister("s2", 0x112233440f0f0f0fuL.toLong())
  setRegister("s3", 0x1122334410101010uL.toLong())
  setRegister("s4", 0x1122334411111111uL.toLong())
  setRegister("s5", 0x1122334412121212uL.toLong())
  setRegister("s6", 0x1122334413131313uL.toLong())
  setRegister("s7", 0x1122334414141414uL.toLong())
  setRegister("fp", 0x10203040080b0a05uL.toLong())
  setRegister("gp", 0x010203045d6d7d8duL.toLong())
  setRegister("sp", 0x000000007fff1ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 8
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 13, 0, 0, 106)
  expectBytes("values_before", 76, 154, 87, 19)
  expectBytes("values_guard", 75, 154, 87, 19)
  expectBytes("count_guard", 74, 154, 87, 19)
  expectBytes("result_guard", 73, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 13, 0, 0, 106)
  expectBytes("values_before", 76, 154, 87, 19)
  expectBytes("values_guard", 75, 154, 87, 19)
  expectBytes("count_guard", 74, 154, 87, 19)
  expectBytes("result_guard", 73, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 14 offset 0 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 14, 0, 0, 106)
  setBytes("values_before", 109, 154, 87, 19)
  setBytes("values_guard", 108, 154, 87, 19)
  setBytes("count_guard", 107, 154, 87, 19)
  setBytes("result_guard", 106, 154, 87, 19)
  setRegister("s0", 0x112233440e0e0e0euL.toLong())
  setRegister("s1", 0x112233440f0f0f0fuL.toLong())
  setRegister("s2", 0x1122334410101010uL.toLong())
  setRegister("s3", 0x1122334411111111uL.toLong())
  setRegister("s4", 0x1122334412121212uL.toLong())
  setRegister("s5", 0x1122334413131313uL.toLong())
  setRegister("s6", 0x1122334414141414uL.toLong())
  setRegister("s7", 0x1122334415151515uL.toLong())
  setRegister("fp", 0x102030400b080906uL.toLong())
  setRegister("gp", 0x010203045e6e7e8euL.toLong())
  setRegister("sp", 0x000000007fff0ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 14, 0, 0, 106)
  expectBytes("values_before", 109, 154, 87, 19)
  expectBytes("values_guard", 108, 154, 87, 19)
  expectBytes("count_guard", 107, 154, 87, 19)
  expectBytes("result_guard", 106, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 14, 0, 0, 106)
  expectBytes("values_before", 109, 154, 87, 19)
  expectBytes("values_guard", 108, 154, 87, 19)
  expectBytes("count_guard", 107, 154, 87, 19)
  expectBytes("result_guard", 106, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 15 offset 0 length 4", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 5, 0, 0, 0)
  setBytes("negative_count", 15, 0, 0, 106)
  setBytes("values_before", 14, 154, 87, 19)
  setBytes("values_guard", 13, 154, 87, 19)
  setBytes("count_guard", 12, 154, 87, 19)
  setBytes("result_guard", 11, 154, 87, 19)
  setRegister("s0", 0x112233440f0f0f0fuL.toLong())
  setRegister("s1", 0x1122334410101010uL.toLong())
  setRegister("s2", 0x1122334411111111uL.toLong())
  setRegister("s3", 0x1122334412121212uL.toLong())
  setRegister("s4", 0x1122334413131313uL.toLong())
  setRegister("s5", 0x1122334414141414uL.toLong())
  setRegister("s6", 0x1122334415151515uL.toLong())
  setRegister("s7", 0x1122334416161616uL.toLong())
  setRegister("fp", 0x102030400a090807uL.toLong())
  setRegister("gp", 0x010203045f6f7f8fuL.toLong())
  setRegister("sp", 0x000000007ffefffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 16
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 15, 0, 0, 106)
  expectBytes("values_before", 14, 154, 87, 19)
  expectBytes("values_guard", 13, 154, 87, 19)
  expectBytes("count_guard", 12, 154, 87, 19)
  expectBytes("result_guard", 11, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 15, 0, 0, 106)
  expectBytes("values_before", 14, 154, 87, 19)
  expectBytes("values_guard", 13, 154, 87, 19)
  expectBytes("count_guard", 12, 154, 87, 19)
  expectBytes("result_guard", 11, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 16 offset 0 length 5", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 6, 0, 0, 0)
  setBytes("negative_count", 16, 0, 0, 106)
  setBytes("values_before", 47, 154, 87, 19)
  setBytes("values_guard", 46, 154, 87, 19)
  setBytes("count_guard", 45, 154, 87, 19)
  setBytes("result_guard", 44, 154, 87, 19)
  setRegister("s0", 0x1122334410101010uL.toLong())
  setRegister("s1", 0x1122334411111111uL.toLong())
  setRegister("s2", 0x1122334412121212uL.toLong())
  setRegister("s3", 0x1122334413131313uL.toLong())
  setRegister("s4", 0x1122334414141414uL.toLong())
  setRegister("s5", 0x1122334415151515uL.toLong())
  setRegister("s6", 0x1122334416161616uL.toLong())
  setRegister("s7", 0x1122334417171717uL.toLong())
  setRegister("fp", 0x1020304015161718uL.toLong())
  setRegister("gp", 0x0102030440706090uL.toLong())
  setRegister("sp", 0x000000007ffeeffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 20
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 16, 0, 0, 106)
  expectBytes("values_before", 47, 154, 87, 19)
  expectBytes("values_guard", 46, 154, 87, 19)
  expectBytes("count_guard", 45, 154, 87, 19)
  expectBytes("result_guard", 44, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 16, 0, 0, 106)
  expectBytes("values_before", 47, 154, 87, 19)
  expectBytes("values_guard", 46, 154, 87, 19)
  expectBytes("count_guard", 45, 154, 87, 19)
  expectBytes("result_guard", 44, 154, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 17 offset 0 length 6", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 7, 0, 0, 0)
  setBytes("negative_count", 17, 0, 0, 106)
  setBytes("values_before", 208, 153, 87, 19)
  setBytes("values_guard", 207, 153, 87, 19)
  setBytes("count_guard", 206, 153, 87, 19)
  setBytes("result_guard", 205, 153, 87, 19)
  setRegister("s0", 0x1122334411111111uL.toLong())
  setRegister("s1", 0x1122334412121212uL.toLong())
  setRegister("s2", 0x1122334413131313uL.toLong())
  setRegister("s3", 0x1122334414141414uL.toLong())
  setRegister("s4", 0x1122334415151515uL.toLong())
  setRegister("s5", 0x1122334416161616uL.toLong())
  setRegister("s6", 0x1122334417171717uL.toLong())
  setRegister("s7", 0x1122334418181818uL.toLong())
  setRegister("fp", 0x1020304014171619uL.toLong())
  setRegister("gp", 0x0102030441716191uL.toLong())
  setRegister("sp", 0x000000007ffedffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 24
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000006uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 7, 0, 0, 0)
  expectBytes("negative_count", 17, 0, 0, 106)
  expectBytes("values_before", 208, 153, 87, 19)
  expectBytes("values_guard", 207, 153, 87, 19)
  expectBytes("count_guard", 206, 153, 87, 19)
  expectBytes("result_guard", 205, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000006uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 7, 0, 0, 0)
  expectBytes("negative_count", 17, 0, 0, 106)
  expectBytes("values_before", 208, 153, 87, 19)
  expectBytes("values_guard", 207, 153, 87, 19)
  expectBytes("count_guard", 206, 153, 87, 19)
  expectBytes("result_guard", 205, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 18 offset 0 length 7", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 8, 0, 0, 0)
  setBytes("negative_count", 18, 0, 0, 106)
  setBytes("values_before", 241, 153, 87, 19)
  setBytes("values_guard", 240, 153, 87, 19)
  setBytes("count_guard", 239, 153, 87, 19)
  setBytes("result_guard", 238, 153, 87, 19)
  setRegister("s0", 0x1122334412121212uL.toLong())
  setRegister("s1", 0x1122334413131313uL.toLong())
  setRegister("s2", 0x1122334414141414uL.toLong())
  setRegister("s3", 0x1122334415151515uL.toLong())
  setRegister("s4", 0x1122334416161616uL.toLong())
  setRegister("s5", 0x1122334417171717uL.toLong())
  setRegister("s6", 0x1122334418181818uL.toLong())
  setRegister("s7", 0x1122334419191919uL.toLong())
  setRegister("fp", 0x102030401714151auL.toLong())
  setRegister("gp", 0x0102030442726292uL.toLong())
  setRegister("sp", 0x000000007ffecffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 28
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000007uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 8, 0, 0, 0)
  expectBytes("negative_count", 18, 0, 0, 106)
  expectBytes("values_before", 241, 153, 87, 19)
  expectBytes("values_guard", 240, 153, 87, 19)
  expectBytes("count_guard", 239, 153, 87, 19)
  expectBytes("result_guard", 238, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000007uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 8, 0, 0, 0)
  expectBytes("negative_count", 18, 0, 0, 106)
  expectBytes("values_before", 241, 153, 87, 19)
  expectBytes("values_guard", 240, 153, 87, 19)
  expectBytes("count_guard", 239, 153, 87, 19)
  expectBytes("result_guard", 238, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 19 offset 0 length 8", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 0, 0, 0, 0)
  setBytes("negative_count", 19, 0, 0, 106)
  setBytes("values_before", 146, 153, 87, 19)
  setBytes("values_guard", 145, 153, 87, 19)
  setBytes("count_guard", 144, 153, 87, 19)
  setBytes("result_guard", 143, 153, 87, 19)
  setRegister("s0", 0x1122334413131313uL.toLong())
  setRegister("s1", 0x1122334414141414uL.toLong())
  setRegister("s2", 0x1122334415151515uL.toLong())
  setRegister("s3", 0x1122334416161616uL.toLong())
  setRegister("s4", 0x1122334417171717uL.toLong())
  setRegister("s5", 0x1122334418181818uL.toLong())
  setRegister("s6", 0x1122334419191919uL.toLong())
  setRegister("s7", 0x112233441a1a1a1auL.toLong())
  setRegister("fp", 0x102030401615141buL.toLong())
  setRegister("gp", 0x0102030443736393uL.toLong())
  setRegister("sp", 0x000000007ffebffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 0L)
  val length = 32
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 19, 0, 0, 106)
  expectBytes("values_before", 146, 153, 87, 19)
  expectBytes("values_guard", 145, 153, 87, 19)
  expectBytes("count_guard", 144, 153, 87, 19)
  expectBytes("result_guard", 143, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 0L)
  setRegister("a1", 0x0000000000000008uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      3,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 0, 0, 0, 0)
  expectBytes("negative_count", 19, 0, 0, 106)
  expectBytes("values_before", 146, 153, 87, 19)
  expectBytes("values_guard", 145, 153, 87, 19)
  expectBytes("count_guard", 144, 153, 87, 19)
  expectBytes("result_guard", 143, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 20 offset 1 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 20, 0, 0, 106)
  setBytes("values_before", 179, 153, 87, 19)
  setBytes("values_guard", 178, 153, 87, 19)
  setBytes("count_guard", 177, 153, 87, 19)
  setBytes("result_guard", 176, 153, 87, 19)
  setRegister("s0", 0x1122334414141414uL.toLong())
  setRegister("s1", 0x1122334415151515uL.toLong())
  setRegister("s2", 0x1122334416161616uL.toLong())
  setRegister("s3", 0x1122334417171717uL.toLong())
  setRegister("s4", 0x1122334418181818uL.toLong())
  setRegister("s5", 0x1122334419191919uL.toLong())
  setRegister("s6", 0x112233441a1a1a1auL.toLong())
  setRegister("s7", 0x112233441b1b1b1buL.toLong())
  setRegister("fp", 0x102030401112131cuL.toLong())
  setRegister("gp", 0x0102030444746494uL.toLong())
  setRegister("sp", 0x000000007ffeaffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 20, 0, 0, 106)
  expectBytes("values_before", 179, 153, 87, 19)
  expectBytes("values_guard", 178, 153, 87, 19)
  expectBytes("count_guard", 177, 153, 87, 19)
  expectBytes("result_guard", 176, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 20, 0, 0, 106)
  expectBytes("values_before", 179, 153, 87, 19)
  expectBytes("values_guard", 178, 153, 87, 19)
  expectBytes("count_guard", 177, 153, 87, 19)
  expectBytes("result_guard", 176, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 21 offset 1 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 21, 0, 0, 106)
  setBytes("values_before", 84, 153, 87, 19)
  setBytes("values_guard", 83, 153, 87, 19)
  setBytes("count_guard", 82, 153, 87, 19)
  setBytes("result_guard", 81, 153, 87, 19)
  setRegister("s0", 0x1122334415151515uL.toLong())
  setRegister("s1", 0x1122334416161616uL.toLong())
  setRegister("s2", 0x1122334417171717uL.toLong())
  setRegister("s3", 0x1122334418181818uL.toLong())
  setRegister("s4", 0x1122334419191919uL.toLong())
  setRegister("s5", 0x112233441a1a1a1auL.toLong())
  setRegister("s6", 0x112233441b1b1b1buL.toLong())
  setRegister("s7", 0x112233441c1c1c1cuL.toLong())
  setRegister("fp", 0x102030401013121duL.toLong())
  setRegister("gp", 0x0102030445756595uL.toLong())
  setRegister("sp", 0x000000007ffe9ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 21, 0, 0, 106)
  expectBytes("values_before", 84, 153, 87, 19)
  expectBytes("values_guard", 83, 153, 87, 19)
  expectBytes("count_guard", 82, 153, 87, 19)
  expectBytes("result_guard", 81, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 21, 0, 0, 106)
  expectBytes("values_before", 84, 153, 87, 19)
  expectBytes("values_guard", 83, 153, 87, 19)
  expectBytes("count_guard", 82, 153, 87, 19)
  expectBytes("result_guard", 81, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 22 offset 1 length 2", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 3, 0, 0, 0)
  setBytes("negative_count", 22, 0, 0, 106)
  setBytes("values_before", 117, 153, 87, 19)
  setBytes("values_guard", 116, 153, 87, 19)
  setBytes("count_guard", 115, 153, 87, 19)
  setBytes("result_guard", 114, 153, 87, 19)
  setRegister("s0", 0x1122334416161616uL.toLong())
  setRegister("s1", 0x1122334417171717uL.toLong())
  setRegister("s2", 0x1122334418181818uL.toLong())
  setRegister("s3", 0x1122334419191919uL.toLong())
  setRegister("s4", 0x112233441a1a1a1auL.toLong())
  setRegister("s5", 0x112233441b1b1b1buL.toLong())
  setRegister("s6", 0x112233441c1c1c1cuL.toLong())
  setRegister("s7", 0x112233441d1d1d1duL.toLong())
  setRegister("fp", 0x102030401310111euL.toLong())
  setRegister("gp", 0x0102030446766696uL.toLong())
  setRegister("sp", 0x000000007ffe8ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 8
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 22, 0, 0, 106)
  expectBytes("values_before", 117, 153, 87, 19)
  expectBytes("values_guard", 116, 153, 87, 19)
  expectBytes("count_guard", 115, 153, 87, 19)
  expectBytes("result_guard", 114, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 22, 0, 0, 106)
  expectBytes("values_before", 117, 153, 87, 19)
  expectBytes("values_guard", 116, 153, 87, 19)
  expectBytes("count_guard", 115, 153, 87, 19)
  expectBytes("result_guard", 114, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 23 offset 1 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 23, 0, 0, 106)
  setBytes("values_before", 22, 153, 87, 19)
  setBytes("values_guard", 21, 153, 87, 19)
  setBytes("count_guard", 20, 153, 87, 19)
  setBytes("result_guard", 19, 153, 87, 19)
  setRegister("s0", 0x1122334417171717uL.toLong())
  setRegister("s1", 0x1122334418181818uL.toLong())
  setRegister("s2", 0x1122334419191919uL.toLong())
  setRegister("s3", 0x112233441a1a1a1auL.toLong())
  setRegister("s4", 0x112233441b1b1b1buL.toLong())
  setRegister("s5", 0x112233441c1c1c1cuL.toLong())
  setRegister("s6", 0x112233441d1d1d1duL.toLong())
  setRegister("s7", 0x112233441e1e1e1euL.toLong())
  setRegister("fp", 0x102030401211101fuL.toLong())
  setRegister("gp", 0x0102030447776797uL.toLong())
  setRegister("sp", 0x000000007ffe7ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 23, 0, 0, 106)
  expectBytes("values_before", 22, 153, 87, 19)
  expectBytes("values_guard", 21, 153, 87, 19)
  expectBytes("count_guard", 20, 153, 87, 19)
  expectBytes("result_guard", 19, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 23, 0, 0, 106)
  expectBytes("values_before", 22, 153, 87, 19)
  expectBytes("values_guard", 21, 153, 87, 19)
  expectBytes("count_guard", 20, 153, 87, 19)
  expectBytes("result_guard", 19, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 24 offset 1 length 4", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 5, 0, 0, 0)
  setBytes("negative_count", 24, 0, 0, 106)
  setBytes("values_before", 55, 153, 87, 19)
  setBytes("values_guard", 54, 153, 87, 19)
  setBytes("count_guard", 53, 153, 87, 19)
  setBytes("result_guard", 52, 153, 87, 19)
  setRegister("s0", 0x1122334418181818uL.toLong())
  setRegister("s1", 0x1122334419191919uL.toLong())
  setRegister("s2", 0x112233441a1a1a1auL.toLong())
  setRegister("s3", 0x112233441b1b1b1buL.toLong())
  setRegister("s4", 0x112233441c1c1c1cuL.toLong())
  setRegister("s5", 0x112233441d1d1d1duL.toLong())
  setRegister("s6", 0x112233441e1e1e1euL.toLong())
  setRegister("s7", 0x112233441f1f1f1fuL.toLong())
  setRegister("fp", 0x102030401d1e1f10uL.toLong())
  setRegister("gp", 0x0102030448786898uL.toLong())
  setRegister("sp", 0x000000007ffe6ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 16
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 24, 0, 0, 106)
  expectBytes("values_before", 55, 153, 87, 19)
  expectBytes("values_guard", 54, 153, 87, 19)
  expectBytes("count_guard", 53, 153, 87, 19)
  expectBytes("result_guard", 52, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 24, 0, 0, 106)
  expectBytes("values_before", 55, 153, 87, 19)
  expectBytes("values_guard", 54, 153, 87, 19)
  expectBytes("count_guard", 53, 153, 87, 19)
  expectBytes("result_guard", 52, 153, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 25 offset 1 length 5", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 6, 0, 0, 0)
  setBytes("negative_count", 25, 0, 0, 106)
  setBytes("values_before", 216, 152, 87, 19)
  setBytes("values_guard", 215, 152, 87, 19)
  setBytes("count_guard", 214, 152, 87, 19)
  setBytes("result_guard", 213, 152, 87, 19)
  setRegister("s0", 0x1122334419191919uL.toLong())
  setRegister("s1", 0x112233441a1a1a1auL.toLong())
  setRegister("s2", 0x112233441b1b1b1buL.toLong())
  setRegister("s3", 0x112233441c1c1c1cuL.toLong())
  setRegister("s4", 0x112233441d1d1d1duL.toLong())
  setRegister("s5", 0x112233441e1e1e1euL.toLong())
  setRegister("s6", 0x112233441f1f1f1fuL.toLong())
  setRegister("s7", 0x1122334420202020uL.toLong())
  setRegister("fp", 0x102030401c1f1e11uL.toLong())
  setRegister("gp", 0x0102030449796999uL.toLong())
  setRegister("sp", 0x000000007ffe5ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 20
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 25, 0, 0, 106)
  expectBytes("values_before", 216, 152, 87, 19)
  expectBytes("values_guard", 215, 152, 87, 19)
  expectBytes("count_guard", 214, 152, 87, 19)
  expectBytes("result_guard", 213, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 25, 0, 0, 106)
  expectBytes("values_before", 216, 152, 87, 19)
  expectBytes("values_guard", 215, 152, 87, 19)
  expectBytes("count_guard", 214, 152, 87, 19)
  expectBytes("result_guard", 213, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 26 offset 1 length 6", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 7, 0, 0, 0)
  setBytes("negative_count", 26, 0, 0, 106)
  setBytes("values_before", 249, 152, 87, 19)
  setBytes("values_guard", 248, 152, 87, 19)
  setBytes("count_guard", 247, 152, 87, 19)
  setBytes("result_guard", 246, 152, 87, 19)
  setRegister("s0", 0x112233441a1a1a1auL.toLong())
  setRegister("s1", 0x112233441b1b1b1buL.toLong())
  setRegister("s2", 0x112233441c1c1c1cuL.toLong())
  setRegister("s3", 0x112233441d1d1d1duL.toLong())
  setRegister("s4", 0x112233441e1e1e1euL.toLong())
  setRegister("s5", 0x112233441f1f1f1fuL.toLong())
  setRegister("s6", 0x1122334420202020uL.toLong())
  setRegister("s7", 0x1122334421212121uL.toLong())
  setRegister("fp", 0x102030401f1c1d12uL.toLong())
  setRegister("gp", 0x010203044a7a6a9auL.toLong())
  setRegister("sp", 0x000000007ffe4ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 24
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000006uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 7, 0, 0, 0)
  expectBytes("negative_count", 26, 0, 0, 106)
  expectBytes("values_before", 249, 152, 87, 19)
  expectBytes("values_guard", 248, 152, 87, 19)
  expectBytes("count_guard", 247, 152, 87, 19)
  expectBytes("result_guard", 246, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000006uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 7, 0, 0, 0)
  expectBytes("negative_count", 26, 0, 0, 106)
  expectBytes("values_before", 249, 152, 87, 19)
  expectBytes("values_guard", 248, 152, 87, 19)
  expectBytes("count_guard", 247, 152, 87, 19)
  expectBytes("result_guard", 246, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 27 offset 1 length 7", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 8, 0, 0, 0)
  setBytes("negative_count", 27, 0, 0, 106)
  setBytes("values_before", 154, 152, 87, 19)
  setBytes("values_guard", 153, 152, 87, 19)
  setBytes("count_guard", 152, 152, 87, 19)
  setBytes("result_guard", 151, 152, 87, 19)
  setRegister("s0", 0x112233441b1b1b1buL.toLong())
  setRegister("s1", 0x112233441c1c1c1cuL.toLong())
  setRegister("s2", 0x112233441d1d1d1duL.toLong())
  setRegister("s3", 0x112233441e1e1e1euL.toLong())
  setRegister("s4", 0x112233441f1f1f1fuL.toLong())
  setRegister("s5", 0x1122334420202020uL.toLong())
  setRegister("s6", 0x1122334421212121uL.toLong())
  setRegister("s7", 0x1122334422222222uL.toLong())
  setRegister("fp", 0x102030401e1d1c13uL.toLong())
  setRegister("gp", 0x010203044b7b6b9buL.toLong())
  setRegister("sp", 0x000000007ffe3ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 4L)
  val length = 28
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000007uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 8, 0, 0, 0)
  expectBytes("negative_count", 27, 0, 0, 106)
  expectBytes("values_before", 154, 152, 87, 19)
  expectBytes("values_guard", 153, 152, 87, 19)
  expectBytes("count_guard", 152, 152, 87, 19)
  expectBytes("result_guard", 151, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 4L)
  setRegister("a1", 0x0000000000000007uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 4)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 8, 0, 0, 0)
  expectBytes("negative_count", 27, 0, 0, 106)
  expectBytes("values_before", 154, 152, 87, 19)
  expectBytes("values_guard", 153, 152, 87, 19)
  expectBytes("count_guard", 152, 152, 87, 19)
  expectBytes("result_guard", 151, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 28 offset 2 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 28, 0, 0, 106)
  setBytes("values_before", 187, 152, 87, 19)
  setBytes("values_guard", 186, 152, 87, 19)
  setBytes("count_guard", 185, 152, 87, 19)
  setBytes("result_guard", 184, 152, 87, 19)
  setRegister("s0", 0x112233441c1c1c1cuL.toLong())
  setRegister("s1", 0x112233441d1d1d1duL.toLong())
  setRegister("s2", 0x112233441e1e1e1euL.toLong())
  setRegister("s3", 0x112233441f1f1f1fuL.toLong())
  setRegister("s4", 0x1122334420202020uL.toLong())
  setRegister("s5", 0x1122334421212121uL.toLong())
  setRegister("s6", 0x1122334422222222uL.toLong())
  setRegister("s7", 0x1122334423232323uL.toLong())
  setRegister("fp", 0x10203040191a1b14uL.toLong())
  setRegister("gp", 0x010203044c7c6c9cuL.toLong())
  setRegister("sp", 0x000000007ffe2ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 8L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 28, 0, 0, 106)
  expectBytes("values_before", 187, 152, 87, 19)
  expectBytes("values_guard", 186, 152, 87, 19)
  expectBytes("count_guard", 185, 152, 87, 19)
  expectBytes("result_guard", 184, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 28, 0, 0, 106)
  expectBytes("values_before", 187, 152, 87, 19)
  expectBytes("values_guard", 186, 152, 87, 19)
  expectBytes("count_guard", 185, 152, 87, 19)
  expectBytes("result_guard", 184, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 29 offset 2 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 29, 0, 0, 106)
  setBytes("values_before", 92, 152, 87, 19)
  setBytes("values_guard", 91, 152, 87, 19)
  setBytes("count_guard", 90, 152, 87, 19)
  setBytes("result_guard", 89, 152, 87, 19)
  setRegister("s0", 0x112233441d1d1d1duL.toLong())
  setRegister("s1", 0x112233441e1e1e1euL.toLong())
  setRegister("s2", 0x112233441f1f1f1fuL.toLong())
  setRegister("s3", 0x1122334420202020uL.toLong())
  setRegister("s4", 0x1122334421212121uL.toLong())
  setRegister("s5", 0x1122334422222222uL.toLong())
  setRegister("s6", 0x1122334423232323uL.toLong())
  setRegister("s7", 0x1122334424242424uL.toLong())
  setRegister("fp", 0x10203040181b1a15uL.toLong())
  setRegister("gp", 0x010203044d7d6d9duL.toLong())
  setRegister("sp", 0x000000007ffe1ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 8L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 29, 0, 0, 106)
  expectBytes("values_before", 92, 152, 87, 19)
  expectBytes("values_guard", 91, 152, 87, 19)
  expectBytes("count_guard", 90, 152, 87, 19)
  expectBytes("result_guard", 89, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 29, 0, 0, 106)
  expectBytes("values_before", 92, 152, 87, 19)
  expectBytes("values_guard", 91, 152, 87, 19)
  expectBytes("count_guard", 90, 152, 87, 19)
  expectBytes("result_guard", 89, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 30 offset 2 length 2", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 3, 0, 0, 0)
  setBytes("negative_count", 30, 0, 0, 106)
  setBytes("values_before", 125, 152, 87, 19)
  setBytes("values_guard", 124, 152, 87, 19)
  setBytes("count_guard", 123, 152, 87, 19)
  setBytes("result_guard", 122, 152, 87, 19)
  setRegister("s0", 0x112233441e1e1e1euL.toLong())
  setRegister("s1", 0x112233441f1f1f1fuL.toLong())
  setRegister("s2", 0x1122334420202020uL.toLong())
  setRegister("s3", 0x1122334421212121uL.toLong())
  setRegister("s4", 0x1122334422222222uL.toLong())
  setRegister("s5", 0x1122334423232323uL.toLong())
  setRegister("s6", 0x1122334424242424uL.toLong())
  setRegister("s7", 0x1122334425252525uL.toLong())
  setRegister("fp", 0x102030401b181916uL.toLong())
  setRegister("gp", 0x010203044e7e6e9euL.toLong())
  setRegister("sp", 0x000000007ffe0ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 8L)
  val length = 8
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 30, 0, 0, 106)
  expectBytes("values_before", 125, 152, 87, 19)
  expectBytes("values_guard", 124, 152, 87, 19)
  expectBytes("count_guard", 123, 152, 87, 19)
  expectBytes("result_guard", 122, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 30, 0, 0, 106)
  expectBytes("values_before", 125, 152, 87, 19)
  expectBytes("values_guard", 124, 152, 87, 19)
  expectBytes("count_guard", 123, 152, 87, 19)
  expectBytes("result_guard", 122, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 31 offset 2 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 31, 0, 0, 106)
  setBytes("values_before", 30, 152, 87, 19)
  setBytes("values_guard", 29, 152, 87, 19)
  setBytes("count_guard", 28, 152, 87, 19)
  setBytes("result_guard", 27, 152, 87, 19)
  setRegister("s0", 0x112233441f1f1f1fuL.toLong())
  setRegister("s1", 0x1122334420202020uL.toLong())
  setRegister("s2", 0x1122334421212121uL.toLong())
  setRegister("s3", 0x1122334422222222uL.toLong())
  setRegister("s4", 0x1122334423232323uL.toLong())
  setRegister("s5", 0x1122334424242424uL.toLong())
  setRegister("s6", 0x1122334425252525uL.toLong())
  setRegister("s7", 0x1122334426262626uL.toLong())
  setRegister("fp", 0x102030401a191817uL.toLong())
  setRegister("gp", 0x010203044f7f6f9fuL.toLong())
  setRegister("sp", 0x000000007ffdfffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 8L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 31, 0, 0, 106)
  expectBytes("values_before", 30, 152, 87, 19)
  expectBytes("values_guard", 29, 152, 87, 19)
  expectBytes("count_guard", 28, 152, 87, 19)
  expectBytes("result_guard", 27, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 31, 0, 0, 106)
  expectBytes("values_before", 30, 152, 87, 19)
  expectBytes("values_guard", 29, 152, 87, 19)
  expectBytes("count_guard", 28, 152, 87, 19)
  expectBytes("result_guard", 27, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 32 offset 2 length 4", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 5, 0, 0, 0)
  setBytes("negative_count", 32, 0, 0, 106)
  setBytes("values_before", 63, 152, 87, 19)
  setBytes("values_guard", 62, 152, 87, 19)
  setBytes("count_guard", 61, 152, 87, 19)
  setBytes("result_guard", 60, 152, 87, 19)
  setRegister("s0", 0x1122334420202020uL.toLong())
  setRegister("s1", 0x1122334421212121uL.toLong())
  setRegister("s2", 0x1122334422222222uL.toLong())
  setRegister("s3", 0x1122334423232323uL.toLong())
  setRegister("s4", 0x1122334424242424uL.toLong())
  setRegister("s5", 0x1122334425252525uL.toLong())
  setRegister("s6", 0x1122334426262626uL.toLong())
  setRegister("s7", 0x1122334427272727uL.toLong())
  setRegister("fp", 0x1020304025262728uL.toLong())
  setRegister("gp", 0x01020304704050a0uL.toLong())
  setRegister("sp", 0x000000007ffdeffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 8L)
  val length = 16
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 32, 0, 0, 106)
  expectBytes("values_before", 63, 152, 87, 19)
  expectBytes("values_guard", 62, 152, 87, 19)
  expectBytes("count_guard", 61, 152, 87, 19)
  expectBytes("result_guard", 60, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 32, 0, 0, 106)
  expectBytes("values_before", 63, 152, 87, 19)
  expectBytes("values_guard", 62, 152, 87, 19)
  expectBytes("count_guard", 61, 152, 87, 19)
  expectBytes("result_guard", 60, 152, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 33 offset 2 length 5", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 6, 0, 0, 0)
  setBytes("negative_count", 33, 0, 0, 106)
  setBytes("values_before", 32, 152, 87, 19)
  setBytes("values_guard", 223, 159, 87, 19)
  setBytes("count_guard", 222, 159, 87, 19)
  setBytes("result_guard", 221, 159, 87, 19)
  setRegister("s0", 0x1122334421212121uL.toLong())
  setRegister("s1", 0x1122334422222222uL.toLong())
  setRegister("s2", 0x1122334423232323uL.toLong())
  setRegister("s3", 0x1122334424242424uL.toLong())
  setRegister("s4", 0x1122334425252525uL.toLong())
  setRegister("s5", 0x1122334426262626uL.toLong())
  setRegister("s6", 0x1122334427272727uL.toLong())
  setRegister("s7", 0x1122334428282828uL.toLong())
  setRegister("fp", 0x1020304024272629uL.toLong())
  setRegister("gp", 0x01020304714151a1uL.toLong())
  setRegister("sp", 0x000000007ffddffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 8L)
  val length = 20
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 33, 0, 0, 106)
  expectBytes("values_before", 32, 152, 87, 19)
  expectBytes("values_guard", 223, 159, 87, 19)
  expectBytes("count_guard", 222, 159, 87, 19)
  expectBytes("result_guard", 221, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 33, 0, 0, 106)
  expectBytes("values_before", 32, 152, 87, 19)
  expectBytes("values_guard", 223, 159, 87, 19)
  expectBytes("count_guard", 222, 159, 87, 19)
  expectBytes("result_guard", 221, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 34 offset 2 length 6", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 7, 0, 0, 0)
  setBytes("negative_count", 34, 0, 0, 106)
  setBytes("values_before", 193, 159, 87, 19)
  setBytes("values_guard", 192, 159, 87, 19)
  setBytes("count_guard", 255, 159, 87, 19)
  setBytes("result_guard", 254, 159, 87, 19)
  setRegister("s0", 0x1122334422222222uL.toLong())
  setRegister("s1", 0x1122334423232323uL.toLong())
  setRegister("s2", 0x1122334424242424uL.toLong())
  setRegister("s3", 0x1122334425252525uL.toLong())
  setRegister("s4", 0x1122334426262626uL.toLong())
  setRegister("s5", 0x1122334427272727uL.toLong())
  setRegister("s6", 0x1122334428282828uL.toLong())
  setRegister("s7", 0x1122334429292929uL.toLong())
  setRegister("fp", 0x102030402724252auL.toLong())
  setRegister("gp", 0x01020304724252a2uL.toLong())
  setRegister("sp", 0x000000007ffdcffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 8L)
  val length = 24
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000006uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 7, 0, 0, 0)
  expectBytes("negative_count", 34, 0, 0, 106)
  expectBytes("values_before", 193, 159, 87, 19)
  expectBytes("values_guard", 192, 159, 87, 19)
  expectBytes("count_guard", 255, 159, 87, 19)
  expectBytes("result_guard", 254, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 8L)
  setRegister("a1", 0x0000000000000006uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 7, 0, 0, 0)
  expectBytes("negative_count", 34, 0, 0, 106)
  expectBytes("values_before", 193, 159, 87, 19)
  expectBytes("values_guard", 192, 159, 87, 19)
  expectBytes("count_guard", 255, 159, 87, 19)
  expectBytes("result_guard", 254, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 35 offset 3 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 35, 0, 0, 106)
  setBytes("values_before", 226, 159, 87, 19)
  setBytes("values_guard", 225, 159, 87, 19)
  setBytes("count_guard", 224, 159, 87, 19)
  setBytes("result_guard", 159, 159, 87, 19)
  setRegister("s0", 0x1122334423232323uL.toLong())
  setRegister("s1", 0x1122334424242424uL.toLong())
  setRegister("s2", 0x1122334425252525uL.toLong())
  setRegister("s3", 0x1122334426262626uL.toLong())
  setRegister("s4", 0x1122334427272727uL.toLong())
  setRegister("s5", 0x1122334428282828uL.toLong())
  setRegister("s6", 0x1122334429292929uL.toLong())
  setRegister("s7", 0x112233442a2a2a2auL.toLong())
  setRegister("fp", 0x102030402625242buL.toLong())
  setRegister("gp", 0x01020304734353a3uL.toLong())
  setRegister("sp", 0x000000007ffdbffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 12L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 35, 0, 0, 106)
  expectBytes("values_before", 226, 159, 87, 19)
  expectBytes("values_guard", 225, 159, 87, 19)
  expectBytes("count_guard", 224, 159, 87, 19)
  expectBytes("result_guard", 159, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 35, 0, 0, 106)
  expectBytes("values_before", 226, 159, 87, 19)
  expectBytes("values_guard", 225, 159, 87, 19)
  expectBytes("count_guard", 224, 159, 87, 19)
  expectBytes("result_guard", 159, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 36 offset 3 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 36, 0, 0, 106)
  setBytes("values_before", 131, 159, 87, 19)
  setBytes("values_guard", 130, 159, 87, 19)
  setBytes("count_guard", 129, 159, 87, 19)
  setBytes("result_guard", 128, 159, 87, 19)
  setRegister("s0", 0x1122334424242424uL.toLong())
  setRegister("s1", 0x1122334425252525uL.toLong())
  setRegister("s2", 0x1122334426262626uL.toLong())
  setRegister("s3", 0x1122334427272727uL.toLong())
  setRegister("s4", 0x1122334428282828uL.toLong())
  setRegister("s5", 0x1122334429292929uL.toLong())
  setRegister("s6", 0x112233442a2a2a2auL.toLong())
  setRegister("s7", 0x112233442b2b2b2buL.toLong())
  setRegister("fp", 0x102030402122232cuL.toLong())
  setRegister("gp", 0x01020304744454a4uL.toLong())
  setRegister("sp", 0x000000007ffdaffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 12L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 36, 0, 0, 106)
  expectBytes("values_before", 131, 159, 87, 19)
  expectBytes("values_guard", 130, 159, 87, 19)
  expectBytes("count_guard", 129, 159, 87, 19)
  expectBytes("result_guard", 128, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 36, 0, 0, 106)
  expectBytes("values_before", 131, 159, 87, 19)
  expectBytes("values_guard", 130, 159, 87, 19)
  expectBytes("count_guard", 129, 159, 87, 19)
  expectBytes("result_guard", 128, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 37 offset 3 length 2", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 3, 0, 0, 0)
  setBytes("negative_count", 37, 0, 0, 106)
  setBytes("values_before", 164, 159, 87, 19)
  setBytes("values_guard", 163, 159, 87, 19)
  setBytes("count_guard", 162, 159, 87, 19)
  setBytes("result_guard", 161, 159, 87, 19)
  setRegister("s0", 0x1122334425252525uL.toLong())
  setRegister("s1", 0x1122334426262626uL.toLong())
  setRegister("s2", 0x1122334427272727uL.toLong())
  setRegister("s3", 0x1122334428282828uL.toLong())
  setRegister("s4", 0x1122334429292929uL.toLong())
  setRegister("s5", 0x112233442a2a2a2auL.toLong())
  setRegister("s6", 0x112233442b2b2b2buL.toLong())
  setRegister("s7", 0x112233442c2c2c2cuL.toLong())
  setRegister("fp", 0x102030402023222duL.toLong())
  setRegister("gp", 0x01020304754555a5uL.toLong())
  setRegister("sp", 0x000000007ffd9ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 12L)
  val length = 8
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 37, 0, 0, 106)
  expectBytes("values_before", 164, 159, 87, 19)
  expectBytes("values_guard", 163, 159, 87, 19)
  expectBytes("count_guard", 162, 159, 87, 19)
  expectBytes("result_guard", 161, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 37, 0, 0, 106)
  expectBytes("values_before", 164, 159, 87, 19)
  expectBytes("values_guard", 163, 159, 87, 19)
  expectBytes("count_guard", 162, 159, 87, 19)
  expectBytes("result_guard", 161, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 38 offset 3 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 38, 0, 0, 106)
  setBytes("values_before", 69, 159, 87, 19)
  setBytes("values_guard", 68, 159, 87, 19)
  setBytes("count_guard", 67, 159, 87, 19)
  setBytes("result_guard", 66, 159, 87, 19)
  setRegister("s0", 0x1122334426262626uL.toLong())
  setRegister("s1", 0x1122334427272727uL.toLong())
  setRegister("s2", 0x1122334428282828uL.toLong())
  setRegister("s3", 0x1122334429292929uL.toLong())
  setRegister("s4", 0x112233442a2a2a2auL.toLong())
  setRegister("s5", 0x112233442b2b2b2buL.toLong())
  setRegister("s6", 0x112233442c2c2c2cuL.toLong())
  setRegister("s7", 0x112233442d2d2d2duL.toLong())
  setRegister("fp", 0x102030402320212euL.toLong())
  setRegister("gp", 0x01020304764656a6uL.toLong())
  setRegister("sp", 0x000000007ffd8ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 12L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 38, 0, 0, 106)
  expectBytes("values_before", 69, 159, 87, 19)
  expectBytes("values_guard", 68, 159, 87, 19)
  expectBytes("count_guard", 67, 159, 87, 19)
  expectBytes("result_guard", 66, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 38, 0, 0, 106)
  expectBytes("values_before", 69, 159, 87, 19)
  expectBytes("values_guard", 68, 159, 87, 19)
  expectBytes("count_guard", 67, 159, 87, 19)
  expectBytes("result_guard", 66, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 39 offset 3 length 4", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 5, 0, 0, 0)
  setBytes("negative_count", 39, 0, 0, 106)
  setBytes("values_before", 102, 159, 87, 19)
  setBytes("values_guard", 101, 159, 87, 19)
  setBytes("count_guard", 100, 159, 87, 19)
  setBytes("result_guard", 99, 159, 87, 19)
  setRegister("s0", 0x1122334427272727uL.toLong())
  setRegister("s1", 0x1122334428282828uL.toLong())
  setRegister("s2", 0x1122334429292929uL.toLong())
  setRegister("s3", 0x112233442a2a2a2auL.toLong())
  setRegister("s4", 0x112233442b2b2b2buL.toLong())
  setRegister("s5", 0x112233442c2c2c2cuL.toLong())
  setRegister("s6", 0x112233442d2d2d2duL.toLong())
  setRegister("s7", 0x112233442e2e2e2euL.toLong())
  setRegister("fp", 0x102030402221202fuL.toLong())
  setRegister("gp", 0x01020304774757a7uL.toLong())
  setRegister("sp", 0x000000007ffd7ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 12L)
  val length = 16
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 39, 0, 0, 106)
  expectBytes("values_before", 102, 159, 87, 19)
  expectBytes("values_guard", 101, 159, 87, 19)
  expectBytes("count_guard", 100, 159, 87, 19)
  expectBytes("result_guard", 99, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 39, 0, 0, 106)
  expectBytes("values_before", 102, 159, 87, 19)
  expectBytes("values_guard", 101, 159, 87, 19)
  expectBytes("count_guard", 100, 159, 87, 19)
  expectBytes("result_guard", 99, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 40 offset 3 length 5", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 6, 0, 0, 0)
  setBytes("negative_count", 40, 0, 0, 106)
  setBytes("values_before", 7, 159, 87, 19)
  setBytes("values_guard", 6, 159, 87, 19)
  setBytes("count_guard", 5, 159, 87, 19)
  setBytes("result_guard", 4, 159, 87, 19)
  setRegister("s0", 0x1122334428282828uL.toLong())
  setRegister("s1", 0x1122334429292929uL.toLong())
  setRegister("s2", 0x112233442a2a2a2auL.toLong())
  setRegister("s3", 0x112233442b2b2b2buL.toLong())
  setRegister("s4", 0x112233442c2c2c2cuL.toLong())
  setRegister("s5", 0x112233442d2d2d2duL.toLong())
  setRegister("s6", 0x112233442e2e2e2euL.toLong())
  setRegister("s7", 0x112233442f2f2f2fuL.toLong())
  setRegister("fp", 0x102030402d2e2f20uL.toLong())
  setRegister("gp", 0x01020304784858a8uL.toLong())
  setRegister("sp", 0x000000007ffd6ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 12L)
  val length = 20
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 40, 0, 0, 106)
  expectBytes("values_before", 7, 159, 87, 19)
  expectBytes("values_guard", 6, 159, 87, 19)
  expectBytes("count_guard", 5, 159, 87, 19)
  expectBytes("result_guard", 4, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 12L)
  setRegister("a1", 0x0000000000000005uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 3)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 6, 0, 0, 0)
  expectBytes("negative_count", 40, 0, 0, 106)
  expectBytes("values_before", 7, 159, 87, 19)
  expectBytes("values_guard", 6, 159, 87, 19)
  expectBytes("count_guard", 5, 159, 87, 19)
  expectBytes("result_guard", 4, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 41 offset 4 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 41, 0, 0, 106)
  setBytes("values_before", 40, 159, 87, 19)
  setBytes("values_guard", 39, 159, 87, 19)
  setBytes("count_guard", 38, 159, 87, 19)
  setBytes("result_guard", 37, 159, 87, 19)
  setRegister("s0", 0x1122334429292929uL.toLong())
  setRegister("s1", 0x112233442a2a2a2auL.toLong())
  setRegister("s2", 0x112233442b2b2b2buL.toLong())
  setRegister("s3", 0x112233442c2c2c2cuL.toLong())
  setRegister("s4", 0x112233442d2d2d2duL.toLong())
  setRegister("s5", 0x112233442e2e2e2euL.toLong())
  setRegister("s6", 0x112233442f2f2f2fuL.toLong())
  setRegister("s7", 0x1122334430303030uL.toLong())
  setRegister("fp", 0x102030402c2f2e21uL.toLong())
  setRegister("gp", 0x01020304794959a9uL.toLong())
  setRegister("sp", 0x000000007ffd5ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 16L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 41, 0, 0, 106)
  expectBytes("values_before", 40, 159, 87, 19)
  expectBytes("values_guard", 39, 159, 87, 19)
  expectBytes("count_guard", 38, 159, 87, 19)
  expectBytes("result_guard", 37, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 41, 0, 0, 106)
  expectBytes("values_before", 40, 159, 87, 19)
  expectBytes("values_guard", 39, 159, 87, 19)
  expectBytes("count_guard", 38, 159, 87, 19)
  expectBytes("result_guard", 37, 159, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 42 offset 4 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 42, 0, 0, 106)
  setBytes("values_before", 201, 158, 87, 19)
  setBytes("values_guard", 200, 158, 87, 19)
  setBytes("count_guard", 199, 158, 87, 19)
  setBytes("result_guard", 198, 158, 87, 19)
  setRegister("s0", 0x112233442a2a2a2auL.toLong())
  setRegister("s1", 0x112233442b2b2b2buL.toLong())
  setRegister("s2", 0x112233442c2c2c2cuL.toLong())
  setRegister("s3", 0x112233442d2d2d2duL.toLong())
  setRegister("s4", 0x112233442e2e2e2euL.toLong())
  setRegister("s5", 0x112233442f2f2f2fuL.toLong())
  setRegister("s6", 0x1122334430303030uL.toLong())
  setRegister("s7", 0x1122334431313131uL.toLong())
  setRegister("fp", 0x102030402f2c2d22uL.toLong())
  setRegister("gp", 0x010203047a4a5aaauL.toLong())
  setRegister("sp", 0x000000007ffd4ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 16L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 42, 0, 0, 106)
  expectBytes("values_before", 201, 158, 87, 19)
  expectBytes("values_guard", 200, 158, 87, 19)
  expectBytes("count_guard", 199, 158, 87, 19)
  expectBytes("result_guard", 198, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 42, 0, 0, 106)
  expectBytes("values_before", 201, 158, 87, 19)
  expectBytes("values_guard", 200, 158, 87, 19)
  expectBytes("count_guard", 199, 158, 87, 19)
  expectBytes("result_guard", 198, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 43 offset 4 length 2", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 3, 0, 0, 0)
  setBytes("negative_count", 43, 0, 0, 106)
  setBytes("values_before", 234, 158, 87, 19)
  setBytes("values_guard", 233, 158, 87, 19)
  setBytes("count_guard", 232, 158, 87, 19)
  setBytes("result_guard", 231, 158, 87, 19)
  setRegister("s0", 0x112233442b2b2b2buL.toLong())
  setRegister("s1", 0x112233442c2c2c2cuL.toLong())
  setRegister("s2", 0x112233442d2d2d2duL.toLong())
  setRegister("s3", 0x112233442e2e2e2euL.toLong())
  setRegister("s4", 0x112233442f2f2f2fuL.toLong())
  setRegister("s5", 0x1122334430303030uL.toLong())
  setRegister("s6", 0x1122334431313131uL.toLong())
  setRegister("s7", 0x1122334432323232uL.toLong())
  setRegister("fp", 0x102030402e2d2c23uL.toLong())
  setRegister("gp", 0x010203047b4b5babuL.toLong())
  setRegister("sp", 0x000000007ffd3ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 16L)
  val length = 8
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 43, 0, 0, 106)
  expectBytes("values_before", 234, 158, 87, 19)
  expectBytes("values_guard", 233, 158, 87, 19)
  expectBytes("count_guard", 232, 158, 87, 19)
  expectBytes("result_guard", 231, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 43, 0, 0, 106)
  expectBytes("values_before", 234, 158, 87, 19)
  expectBytes("values_guard", 233, 158, 87, 19)
  expectBytes("count_guard", 232, 158, 87, 19)
  expectBytes("result_guard", 231, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 44 offset 4 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 44, 0, 0, 106)
  setBytes("values_before", 139, 158, 87, 19)
  setBytes("values_guard", 138, 158, 87, 19)
  setBytes("count_guard", 137, 158, 87, 19)
  setBytes("result_guard", 136, 158, 87, 19)
  setRegister("s0", 0x112233442c2c2c2cuL.toLong())
  setRegister("s1", 0x112233442d2d2d2duL.toLong())
  setRegister("s2", 0x112233442e2e2e2euL.toLong())
  setRegister("s3", 0x112233442f2f2f2fuL.toLong())
  setRegister("s4", 0x1122334430303030uL.toLong())
  setRegister("s5", 0x1122334431313131uL.toLong())
  setRegister("s6", 0x1122334432323232uL.toLong())
  setRegister("s7", 0x1122334433333333uL.toLong())
  setRegister("fp", 0x10203040292a2b24uL.toLong())
  setRegister("gp", 0x010203047c4c5cacuL.toLong())
  setRegister("sp", 0x000000007ffd2ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 16L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 44, 0, 0, 106)
  expectBytes("values_before", 139, 158, 87, 19)
  expectBytes("values_guard", 138, 158, 87, 19)
  expectBytes("count_guard", 137, 158, 87, 19)
  expectBytes("result_guard", 136, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 44, 0, 0, 106)
  expectBytes("values_before", 139, 158, 87, 19)
  expectBytes("values_guard", 138, 158, 87, 19)
  expectBytes("count_guard", 137, 158, 87, 19)
  expectBytes("result_guard", 136, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 45 offset 4 length 4", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 5, 0, 0, 0)
  setBytes("negative_count", 45, 0, 0, 106)
  setBytes("values_before", 172, 158, 87, 19)
  setBytes("values_guard", 171, 158, 87, 19)
  setBytes("count_guard", 170, 158, 87, 19)
  setBytes("result_guard", 169, 158, 87, 19)
  setRegister("s0", 0x112233442d2d2d2duL.toLong())
  setRegister("s1", 0x112233442e2e2e2euL.toLong())
  setRegister("s2", 0x112233442f2f2f2fuL.toLong())
  setRegister("s3", 0x1122334430303030uL.toLong())
  setRegister("s4", 0x1122334431313131uL.toLong())
  setRegister("s5", 0x1122334432323232uL.toLong())
  setRegister("s6", 0x1122334433333333uL.toLong())
  setRegister("s7", 0x1122334434343434uL.toLong())
  setRegister("fp", 0x10203040282b2a25uL.toLong())
  setRegister("gp", 0x010203047d4d5daduL.toLong())
  setRegister("sp", 0x000000007ffd1ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 16L)
  val length = 16
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 45, 0, 0, 106)
  expectBytes("values_before", 172, 158, 87, 19)
  expectBytes("values_guard", 171, 158, 87, 19)
  expectBytes("count_guard", 170, 158, 87, 19)
  expectBytes("result_guard", 169, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 16L)
  setRegister("a1", 0x0000000000000004uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      255,
      255,
      255,
      127,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 5, 0, 0, 0)
  expectBytes("negative_count", 45, 0, 0, 106)
  expectBytes("values_before", 172, 158, 87, 19)
  expectBytes("values_guard", 171, 158, 87, 19)
  expectBytes("count_guard", 170, 158, 87, 19)
  expectBytes("result_guard", 169, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 46 offset 5 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 46, 0, 0, 106)
  setBytes("values_before", 77, 158, 87, 19)
  setBytes("values_guard", 76, 158, 87, 19)
  setBytes("count_guard", 75, 158, 87, 19)
  setBytes("result_guard", 74, 158, 87, 19)
  setRegister("s0", 0x112233442e2e2e2euL.toLong())
  setRegister("s1", 0x112233442f2f2f2fuL.toLong())
  setRegister("s2", 0x1122334430303030uL.toLong())
  setRegister("s3", 0x1122334431313131uL.toLong())
  setRegister("s4", 0x1122334432323232uL.toLong())
  setRegister("s5", 0x1122334433333333uL.toLong())
  setRegister("s6", 0x1122334434343434uL.toLong())
  setRegister("s7", 0x1122334435353535uL.toLong())
  setRegister("fp", 0x102030402b282926uL.toLong())
  setRegister("gp", 0x010203047e4e5eaeuL.toLong())
  setRegister("sp", 0x000000007ffd0ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 20L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 46, 0, 0, 106)
  expectBytes("values_before", 77, 158, 87, 19)
  expectBytes("values_guard", 76, 158, 87, 19)
  expectBytes("count_guard", 75, 158, 87, 19)
  expectBytes("result_guard", 74, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 46, 0, 0, 106)
  expectBytes("values_before", 77, 158, 87, 19)
  expectBytes("values_guard", 76, 158, 87, 19)
  expectBytes("count_guard", 75, 158, 87, 19)
  expectBytes("result_guard", 74, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 47 offset 5 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 47, 0, 0, 106)
  setBytes("values_before", 110, 158, 87, 19)
  setBytes("values_guard", 109, 158, 87, 19)
  setBytes("count_guard", 108, 158, 87, 19)
  setBytes("result_guard", 107, 158, 87, 19)
  setRegister("s0", 0x112233442f2f2f2fuL.toLong())
  setRegister("s1", 0x1122334430303030uL.toLong())
  setRegister("s2", 0x1122334431313131uL.toLong())
  setRegister("s3", 0x1122334432323232uL.toLong())
  setRegister("s4", 0x1122334433333333uL.toLong())
  setRegister("s5", 0x1122334434343434uL.toLong())
  setRegister("s6", 0x1122334435353535uL.toLong())
  setRegister("s7", 0x1122334436363636uL.toLong())
  setRegister("fp", 0x102030402a292827uL.toLong())
  setRegister("gp", 0x010203047f4f5fafuL.toLong())
  setRegister("sp", 0x000000007ffcfffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 20L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 47, 0, 0, 106)
  expectBytes("values_before", 110, 158, 87, 19)
  expectBytes("values_guard", 109, 158, 87, 19)
  expectBytes("count_guard", 108, 158, 87, 19)
  expectBytes("result_guard", 107, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 47, 0, 0, 106)
  expectBytes("values_before", 110, 158, 87, 19)
  expectBytes("values_guard", 109, 158, 87, 19)
  expectBytes("count_guard", 108, 158, 87, 19)
  expectBytes("result_guard", 107, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 48 offset 5 length 2", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 3, 0, 0, 0)
  setBytes("negative_count", 48, 0, 0, 106)
  setBytes("values_before", 15, 158, 87, 19)
  setBytes("values_guard", 14, 158, 87, 19)
  setBytes("count_guard", 13, 158, 87, 19)
  setBytes("result_guard", 12, 158, 87, 19)
  setRegister("s0", 0x1122334430303030uL.toLong())
  setRegister("s1", 0x1122334431313131uL.toLong())
  setRegister("s2", 0x1122334432323232uL.toLong())
  setRegister("s3", 0x1122334433333333uL.toLong())
  setRegister("s4", 0x1122334434343434uL.toLong())
  setRegister("s5", 0x1122334435353535uL.toLong())
  setRegister("s6", 0x1122334436363636uL.toLong())
  setRegister("s7", 0x1122334437373737uL.toLong())
  setRegister("fp", 0x1020304035363738uL.toLong())
  setRegister("gp", 0x01020304605040b0uL.toLong())
  setRegister("sp", 0x000000007ffceffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 20L)
  val length = 8
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 48, 0, 0, 106)
  expectBytes("values_before", 15, 158, 87, 19)
  expectBytes("values_guard", 14, 158, 87, 19)
  expectBytes("count_guard", 13, 158, 87, 19)
  expectBytes("result_guard", 12, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 48, 0, 0, 106)
  expectBytes("values_before", 15, 158, 87, 19)
  expectBytes("values_guard", 14, 158, 87, 19)
  expectBytes("count_guard", 13, 158, 87, 19)
  expectBytes("result_guard", 12, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 49 offset 5 length 3", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 4, 0, 0, 0)
  setBytes("negative_count", 49, 0, 0, 106)
  setBytes("values_before", 48, 158, 87, 19)
  setBytes("values_guard", 47, 158, 87, 19)
  setBytes("count_guard", 46, 158, 87, 19)
  setBytes("result_guard", 45, 158, 87, 19)
  setRegister("s0", 0x1122334431313131uL.toLong())
  setRegister("s1", 0x1122334432323232uL.toLong())
  setRegister("s2", 0x1122334433333333uL.toLong())
  setRegister("s3", 0x1122334434343434uL.toLong())
  setRegister("s4", 0x1122334435353535uL.toLong())
  setRegister("s5", 0x1122334436363636uL.toLong())
  setRegister("s6", 0x1122334437373737uL.toLong())
  setRegister("s7", 0x1122334438383838uL.toLong())
  setRegister("fp", 0x1020304034373639uL.toLong())
  setRegister("gp", 0x01020304615141b1uL.toLong())
  setRegister("sp", 0x000000007ffcdffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 20L)
  val length = 12
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 49, 0, 0, 106)
  expectBytes("values_before", 48, 158, 87, 19)
  expectBytes("values_guard", 47, 158, 87, 19)
  expectBytes("count_guard", 46, 158, 87, 19)
  expectBytes("result_guard", 45, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 20L)
  setRegister("a1", 0x0000000000000003uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 2)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 4, 0, 0, 0)
  expectBytes("negative_count", 49, 0, 0, 106)
  expectBytes("values_before", 48, 158, 87, 19)
  expectBytes("values_guard", 47, 158, 87, 19)
  expectBytes("count_guard", 46, 158, 87, 19)
  expectBytes("result_guard", 45, 158, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 50 offset 6 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 50, 0, 0, 106)
  setBytes("values_before", 209, 157, 87, 19)
  setBytes("values_guard", 208, 157, 87, 19)
  setBytes("count_guard", 207, 157, 87, 19)
  setBytes("result_guard", 206, 157, 87, 19)
  setRegister("s0", 0x1122334432323232uL.toLong())
  setRegister("s1", 0x1122334433333333uL.toLong())
  setRegister("s2", 0x1122334434343434uL.toLong())
  setRegister("s3", 0x1122334435353535uL.toLong())
  setRegister("s4", 0x1122334436363636uL.toLong())
  setRegister("s5", 0x1122334437373737uL.toLong())
  setRegister("s6", 0x1122334438383838uL.toLong())
  setRegister("s7", 0x1122334439393939uL.toLong())
  setRegister("fp", 0x102030403734353auL.toLong())
  setRegister("gp", 0x01020304625242b2uL.toLong())
  setRegister("sp", 0x000000007ffccffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 24L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 24L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 50, 0, 0, 106)
  expectBytes("values_before", 209, 157, 87, 19)
  expectBytes("values_guard", 208, 157, 87, 19)
  expectBytes("count_guard", 207, 157, 87, 19)
  expectBytes("result_guard", 206, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 24L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 50, 0, 0, 106)
  expectBytes("values_before", 209, 157, 87, 19)
  expectBytes("values_guard", 208, 157, 87, 19)
  expectBytes("count_guard", 207, 157, 87, 19)
  expectBytes("result_guard", 206, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 51 offset 6 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 51, 0, 0, 106)
  setBytes("values_before", 242, 157, 87, 19)
  setBytes("values_guard", 241, 157, 87, 19)
  setBytes("count_guard", 240, 157, 87, 19)
  setBytes("result_guard", 239, 157, 87, 19)
  setRegister("s0", 0x1122334433333333uL.toLong())
  setRegister("s1", 0x1122334434343434uL.toLong())
  setRegister("s2", 0x1122334435353535uL.toLong())
  setRegister("s3", 0x1122334436363636uL.toLong())
  setRegister("s4", 0x1122334437373737uL.toLong())
  setRegister("s5", 0x1122334438383838uL.toLong())
  setRegister("s6", 0x1122334439393939uL.toLong())
  setRegister("s7", 0x112233443a3a3a3auL.toLong())
  setRegister("fp", 0x102030403635343buL.toLong())
  setRegister("gp", 0x01020304635343b3uL.toLong())
  setRegister("sp", 0x000000007ffcbffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 24L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 24L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 51, 0, 0, 106)
  expectBytes("values_before", 242, 157, 87, 19)
  expectBytes("values_guard", 241, 157, 87, 19)
  expectBytes("count_guard", 240, 157, 87, 19)
  expectBytes("result_guard", 239, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 24L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 51, 0, 0, 106)
  expectBytes("values_before", 242, 157, 87, 19)
  expectBytes("values_guard", 241, 157, 87, 19)
  expectBytes("count_guard", 240, 157, 87, 19)
  expectBytes("result_guard", 239, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 52 offset 6 length 2", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 3, 0, 0, 0)
  setBytes("negative_count", 52, 0, 0, 106)
  setBytes("values_before", 147, 157, 87, 19)
  setBytes("values_guard", 146, 157, 87, 19)
  setBytes("count_guard", 145, 157, 87, 19)
  setBytes("result_guard", 144, 157, 87, 19)
  setRegister("s0", 0x1122334434343434uL.toLong())
  setRegister("s1", 0x1122334435353535uL.toLong())
  setRegister("s2", 0x1122334436363636uL.toLong())
  setRegister("s3", 0x1122334437373737uL.toLong())
  setRegister("s4", 0x1122334438383838uL.toLong())
  setRegister("s5", 0x1122334439393939uL.toLong())
  setRegister("s6", 0x112233443a3a3a3auL.toLong())
  setRegister("s7", 0x112233443b3b3b3buL.toLong())
  setRegister("fp", 0x102030403132333cuL.toLong())
  setRegister("gp", 0x01020304645444b4uL.toLong())
  setRegister("sp", 0x000000007ffcaffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 24L)
  val length = 8
  setRegisterToDataAddress("a0", "values", 24L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 52, 0, 0, 106)
  expectBytes("values_before", 147, 157, 87, 19)
  expectBytes("values_guard", 146, 157, 87, 19)
  expectBytes("count_guard", 145, 157, 87, 19)
  expectBytes("result_guard", 144, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 24L)
  setRegister("a1", 0x0000000000000002uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      253,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
  )
  expectBytes("count", 3, 0, 0, 0)
  expectBytes("negative_count", 52, 0, 0, 106)
  expectBytes("values_before", 147, 157, 87, 19)
  expectBytes("values_guard", 146, 157, 87, 19)
  expectBytes("count_guard", 145, 157, 87, 19)
  expectBytes("result_guard", 144, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 53 offset 7 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 53, 0, 0, 106)
  setBytes("values_before", 180, 157, 87, 19)
  setBytes("values_guard", 179, 157, 87, 19)
  setBytes("count_guard", 178, 157, 87, 19)
  setBytes("result_guard", 177, 157, 87, 19)
  setRegister("s0", 0x1122334435353535uL.toLong())
  setRegister("s1", 0x1122334436363636uL.toLong())
  setRegister("s2", 0x1122334437373737uL.toLong())
  setRegister("s3", 0x1122334438383838uL.toLong())
  setRegister("s4", 0x1122334439393939uL.toLong())
  setRegister("s5", 0x112233443a3a3a3auL.toLong())
  setRegister("s6", 0x112233443b3b3b3buL.toLong())
  setRegister("s7", 0x112233443c3c3c3cuL.toLong())
  setRegister("fp", 0x102030403033323duL.toLong())
  setRegister("gp", 0x01020304655545b5uL.toLong())
  setRegister("sp", 0x000000007ffc9ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 28L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 28L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 53, 0, 0, 106)
  expectBytes("values_before", 180, 157, 87, 19)
  expectBytes("values_guard", 179, 157, 87, 19)
  expectBytes("count_guard", 178, 157, 87, 19)
  expectBytes("result_guard", 177, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 28L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 53, 0, 0, 106)
  expectBytes("values_before", 180, 157, 87, 19)
  expectBytes("values_guard", 179, 157, 87, 19)
  expectBytes("count_guard", 178, 157, 87, 19)
  expectBytes("result_guard", 177, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 54 offset 7 length 1", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 2, 0, 0, 0)
  setBytes("negative_count", 54, 0, 0, 106)
  setBytes("values_before", 85, 157, 87, 19)
  setBytes("values_guard", 84, 157, 87, 19)
  setBytes("count_guard", 83, 157, 87, 19)
  setBytes("result_guard", 82, 157, 87, 19)
  setRegister("s0", 0x1122334436363636uL.toLong())
  setRegister("s1", 0x1122334437373737uL.toLong())
  setRegister("s2", 0x1122334438383838uL.toLong())
  setRegister("s3", 0x1122334439393939uL.toLong())
  setRegister("s4", 0x112233443a3a3a3auL.toLong())
  setRegister("s5", 0x112233443b3b3b3buL.toLong())
  setRegister("s6", 0x112233443c3c3c3cuL.toLong())
  setRegister("s7", 0x112233443d3d3d3duL.toLong())
  setRegister("fp", 0x102030403330313euL.toLong())
  setRegister("gp", 0x01020304665646b6uL.toLong())
  setRegister("sp", 0x000000007ffc8ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 28L)
  val length = 4
  setRegisterToDataAddress("a0", "values", 28L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 54, 0, 0, 106)
  expectBytes("values_before", 85, 157, 87, 19)
  expectBytes("values_guard", 84, 157, 87, 19)
  expectBytes("count_guard", 83, 157, 87, 19)
  expectBytes("result_guard", 82, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 28L)
  setRegister("a1", 0x0000000000000001uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 1)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 2, 0, 0, 0)
  expectBytes("negative_count", 54, 0, 0, 106)
  expectBytes("values_before", 85, 157, 87, 19)
  expectBytes("values_guard", 84, 157, 87, 19)
  expectBytes("count_guard", 83, 157, 87, 19)
  expectBytes("result_guard", 82, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}

case("stable partition 55 offset 8 length 0", instructionLimit = 10000) {
  setBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  setBytes("count", 1, 0, 0, 0)
  setBytes("negative_count", 55, 0, 0, 106)
  setBytes("values_before", 118, 157, 87, 19)
  setBytes("values_guard", 117, 157, 87, 19)
  setBytes("count_guard", 116, 157, 87, 19)
  setBytes("result_guard", 115, 157, 87, 19)
  setRegister("s0", 0x1122334437373737uL.toLong())
  setRegister("s1", 0x1122334438383838uL.toLong())
  setRegister("s2", 0x1122334439393939uL.toLong())
  setRegister("s3", 0x112233443a3a3a3auL.toLong())
  setRegister("s4", 0x112233443b3b3b3buL.toLong())
  setRegister("s5", 0x112233443c3c3c3cuL.toLong())
  setRegister("s6", 0x112233443d3d3d3duL.toLong())
  setRegister("s7", 0x112233443e3e3e3euL.toLong())
  setRegister("fp", 0x102030403231303fuL.toLong())
  setRegister("gp", 0x01020304675747b7uL.toLong())
  setRegister("sp", 0x000000007ffc7ffcuL.toLong())
  val top = readRegister("sp") and -16L
  setRegister("sp", top)
  val saved =
      listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp")
          .associateWith(::readRegister)
  val callerBytes = readBytes(top, 32)
  val callerStores = traceMemoryStores(top, 32)
  val protected =
      listOf(
          traceMemoryStores("count", 4),
          traceMemoryStores("negative_count", 4),
          traceMemoryStores("values_before", 4),
          traceMemoryStores("values_guard", 4),
          traceMemoryStores("count_guard", 4),
          traceMemoryStores("result_guard", 4),
      )
  val accesses = traceMemoryAccesses("values", 40, offset = -4)
  val first = readDataAddress("values", 32L)
  val length = 0
  setRegisterToDataAddress("a0", "values", 32L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_done")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 55, 0, 0, 106)
  expectBytes("values_before", 118, 157, 87, 19)
  expectBytes("values_guard", 117, 157, 87, 19)
  expectBytes("count_guard", 116, 157, 87, 19)
  expectBytes("result_guard", 115, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
  setRegisterToDataAddress("a0", "values", 32L)
  setRegister("a1", 0x0000000000000000uL.toLong())
  callFunction("partition_negative", returnTo = "partition_exit")
  expectRegister("v0", 0)
  expectBytes(
      "values",
      3,
      0,
      0,
      0,
      252,
      255,
      255,
      255,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      128,
      255,
      255,
      255,
      127,
      255,
      255,
      255,
      255,
      2,
      0,
      0,
      0,
      253,
      255,
      255,
      255,
  )
  expectBytes("count", 1, 0, 0, 0)
  expectBytes("negative_count", 55, 0, 0, 106)
  expectBytes("values_before", 118, 157, 87, 19)
  expectBytes("values_guard", 117, 157, 87, 19)
  expectBytes("count_guard", 116, 157, 87, 19)
  expectBytes("result_guard", 115, 157, 87, 19)
  for (access in accesses.snapshot()) expectCondition(
      access.address >= first &&
          access.length <= length &&
          access.address - first <= length.toLong() - access.length,
      "Access outside active span",
  )
  protected.forEach { it.expectNone() }
  callerStores.expectNone()
  expectRegister("sp", top)
  saved.forEach { (reg, value) -> expectRegister(reg, value) }
  expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")
  expectOutput("")
}
