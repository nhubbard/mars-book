# Define the encoded instructions
Opcode bytes and encoded byte sequences in this chapter are hexadecimal. Byte offsets,
lengths, depths and instruction counts are decimal unless a prefix says otherwise.
There are thirteen legal opcodes. Each instruction consists of one opcode followed
by its fixed number of operand bytes.


See assignment.md and the companion fixtures for complete numerical tables.


A branch operand is an absolute offset from the program base. The byte after
`JMP` is neither a signed displacement nor an address. `JZ` consumes
its test value on both outcomes: zero selects the encoded target, while a nonzero
value selects the byte after the two-byte branch instruction.

`PUSH8` sign extends its operand: `80` pushes minus 128 and
`ff` pushes minus one. `PUSH32` reads four bytes in least-significant
first order. Bytes `00 00 00 80` encode the minimum signed word directly.
All 32-bit patterns are valid constants. This format has no decimal-literal parser
or separate unary-sign token. The image's starting address need not be word aligned,
so the guided interpreter assembles that word from individual bytes.
