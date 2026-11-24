package edu.missouristate.mars.book

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BookInstructionPointerTest {
  @Test
  fun `source pointer accepts zero-operand instructions and normal operand spacing`() {
    for ((instruction, mnemonic) in
        listOf(
            "syscall" to "syscall",
            "nop" to "nop",
            "SYSCALL # request the current service" to "syscall",
            "    nop" to "nop",
            "andi \$t2, \$v0, 1" to "andi",
            "andi\t\$t2,\$v0,1" to "andi",
        )) {
      assertTrue(bookInstructionMnemonicMatches(instruction, mnemonic), instruction)
    }
  }

  @Test
  fun `source pointer rejects prefix collisions labels and unrelated displayed lines`() {
    for (instruction in listOf("syscalls", "syscall:", "# syscall", "", "   ", "li \$v0, 14")) {
      assertFalse(bookInstructionMnemonicMatches(instruction, "syscall"), instruction)
    }
  }
}
