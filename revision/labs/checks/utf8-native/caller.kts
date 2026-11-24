// Generated public behavior checks; edit the adjacent .cases.json and regenerate.
// Each case runs continuously between its documented public markers in a fresh simulator.

import edu.missouristate.mars.cli.grading.ConsolePublicationCheck
import edu.missouristate.mars.cli.grading.FunctionAccessPolicyCheck
import edu.missouristate.mars.cli.grading.FunctionDataAddress
import edu.missouristate.mars.cli.grading.FunctionDataWindow
import edu.missouristate.mars.cli.grading.FunctionMemoryCheck

case("main count0 mode0", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(FunctionMemoryCheck("chunk_count", "00000000", 0, 1)),
          listOf(),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "00000000000000000000000080000000bf000000ffffffff00000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count0 mode1", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "00000000", 0, 1),
              FunctionMemoryCheck("input", "f08f9982", 4, 1),
          ),
          listOf(),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "00000000000000000000000080000000bf000000ffffffff00000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count0 mode2", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "00000000", 0, 1),
              FunctionMemoryCheck("chunks", "00000000", 40, 1),
          ),
          listOf(),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "00000000000000000000000080000000bf000000ffffffff00000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count1 mode0", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(FunctionMemoryCheck("chunk_count", "01000000", 0, 1)),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count1 mode1", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "01000000", 0, 1),
              FunctionMemoryCheck("input", "f08f9982", 4, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count1 mode2", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "01000000", 0, 1),
              FunctionMemoryCheck("chunks", "00000000", 40, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count2 mode0", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(FunctionMemoryCheck("chunk_count", "02000000", 0, 1)),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count2 mode1", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "02000000", 0, 1),
              FunctionMemoryCheck("input", "f08f9982", 4, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count2 mode2", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "02000000", 0, 1),
              FunctionMemoryCheck("chunks", "00000000", 40, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count3 mode0", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(FunctionMemoryCheck("chunk_count", "03000000", 0, 1)),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "07000000020000000100000080000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "02000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 5), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "07000000020000000100000080000000bf0000000400000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count3 mode1", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "03000000", 0, 1),
              FunctionMemoryCheck("input", "f08f9982", 4, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf00000004000000ffffffff05000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "02000000", 0, 1),
                          FunctionMemoryCheck("last_status", "ffffffff", 0, 1),
                          FunctionMemoryCheck("last_used", "00000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 5), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "05000000020000000300000090000000bf00000004000000ffffffff05000000",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count3 mode2", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "03000000", 0, 1),
              FunctionMemoryCheck("chunks", "00000000", 40, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "07000000020000000100000080000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "02000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 5), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "07000000020000000100000080000000bf0000000400000000000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count4 mode0", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(FunctionMemoryCheck("chunk_count", "04000000", 0, 1)),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "07000000020000000100000080000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "02000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 5), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "0b000000050000000000000080000000bf000000ffffffff01000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "03000000", 0, 1),
                          FunctionMemoryCheck("last_status", "01000000", 0, 1),
                          FunctionMemoryCheck("last_used", "04000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 7), 4)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "0b000000050000000000000080000000bf000000ffffffff01000000ffffffff",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count4 mode1", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "04000000", 0, 1),
              FunctionMemoryCheck("input", "f08f9982", 4, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf00000004000000ffffffff05000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "02000000", 0, 1),
                          FunctionMemoryCheck("last_status", "ffffffff", 0, 1),
                          FunctionMemoryCheck("last_used", "00000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 5), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf00000004000000ffffffff05000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "03000000", 0, 1),
                          FunctionMemoryCheck("last_status", "fdffffff", 0, 1),
                          FunctionMemoryCheck("last_used", "00000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable = listOf(),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "05000000020000000300000090000000bf00000004000000ffffffff05000000",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf08f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}

case("main count4 mode2", input = "", instructionLimit = 50000) {
  ConsolePublicationCheck(
          "chunk_ready",
          listOf(
              FunctionMemoryCheck("chunk_count", "04000000", 0, 1),
              FunctionMemoryCheck("chunks", "00000000", 40, 1),
          ),
          listOf(
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "02000000010000000200000080000000bf0000000100000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "00000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 0), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "05000000020000000300000090000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "01000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "03000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 2), 3)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "07000000020000000100000080000000bf0000000400000000000000ffffffff",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "02000000", 0, 1),
                          FunctionMemoryCheck("last_status", "00000000", 0, 1),
                          FunctionMemoryCheck("last_used", "02000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 5), 2)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
              ConsolePublicationCheck.State(
                  memory =
                      listOf(
                          FunctionMemoryCheck(
                              "state_a",
                              "07000000020000000100000080000000bf00000004000000feffffff07000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck(
                              "input",
                              "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                              0,
                              1,
                          ),
                          FunctionMemoryCheck("chunk_index", "03000000", 0, 1),
                          FunctionMemoryCheck("last_status", "feffffff", 0, 1),
                          FunctionMemoryCheck("last_used", "00000000", 0, 1),
                      ),
                  addressMemory = listOf(),
                  accessPolicies =
                      listOf(
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("state_a", 0),
                              FunctionDataAddress("state_end", 4),
                              readable = listOf(),
                              writable =
                                  listOf(
                                      FunctionDataWindow(FunctionDataAddress("state_a", 0), 32),
                                      FunctionDataWindow(FunctionDataAddress("chunk_index", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_status", 0), 4),
                                      FunctionDataWindow(FunctionDataAddress("last_used", 0), 4),
                                  ),
                              maxAccesses = 100000,
                              checkReads = false,
                          ),
                          FunctionAccessPolicyCheck(
                              FunctionDataAddress("input_guard", 0),
                              FunctionDataAddress("input_end", 4),
                              readable =
                                  listOf(FunctionDataWindow(FunctionDataAddress("input", 7), 0)),
                              writable = listOf(),
                              maxAccesses = 100000,
                              checkReads = true,
                          ),
                      ),
              ),
          ),
          ConsolePublicationCheck.State(
              memory =
                  listOf(
                      FunctionMemoryCheck(
                          "state_a",
                          "07000000020000000100000080000000bf00000004000000feffffff07000000",
                          0,
                          1,
                      ),
                      FunctionMemoryCheck(
                          "input",
                          "41e282acf09f998200c3a900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                          0,
                          1,
                      ),
                  ),
              addressMemory = listOf(),
              accessPolicies =
                  listOf(
                      FunctionAccessPolicyCheck(
                          FunctionDataAddress("state_a", 0),
                          FunctionDataAddress("state_end", 4),
                          readable = listOf(),
                          writable = listOf(),
                          maxAccesses = 100000,
                          checkReads = false,
                      ),
                  ),
          ),
      )
      .verify(this)
  expectOutput("")
}
