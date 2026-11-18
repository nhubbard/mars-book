# Separate the guest machine from MIPS
The MIPS program implements another machine. Its guest instructions are small byte
records in ordinary data memory. MIPS load, compare and branch instructions decode
and carry out each record. The architectural MIPS program counter follows the
interpreter's code; the guest PC is an integer byte offset into its input image.
A guest branch changes that offset and returns to the interpreter loop.

The guest opcode byte `01` selects a signed-byte push. The two bytes
`01 03` therefore push the integer three. They are binary data, not a text
spelling of an assembly instruction. Byte `31` selects the conditional guest
branch; its coincidence with the ASCII encoding of the digit one gives it no textual
meaning here. Decode a byte according to its position in an instruction.

The operand stack belongs to the guest machine. The MIPS register-save frame belongs
to the interpreter function. A guest `DROP` reduces the logical operand depth;
it does not alter the MIPS stack pointer. A guest `JMP` does not call a native
MIPS function or overwrite its return address. Keeping these roles separate prevents
a byte offset, operand value and host address from being used interchangeably.
