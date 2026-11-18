# Mark instruction starts without marking operands
The default image has eighteen bytes but only thirteen encoded instructions.
Its immediate operands occupy positions one, three, six, twelve and fifteen. A target
inside that set is invalid even though the byte is mapped and belongs to the image.


See fixtures/boundaries.md for the instruction-boundary example.


At byte six, hexadecimal `10` is a branch target operand meaning decimal
sixteen. At byte nine, the same byte value is the `ADD` opcode. The decode
position determines its role. Scanning every byte as a potential opcode would reject
legal constants and invent instruction starts inside operands.

The reference records starts in one 64-bit bitmap. Bit position equals byte offset,
so offset 63 uses the high bit. Check a target against the program length before
forming its bit mask. Native variable shifts use only their supported shift-count
bits; shifting by 64 cannot serve as an out-of-range test. The equivalent alternative
uses one private byte per possible offset, initially zero, marking starts with one.
Both representations answer the same membership question after complete decoding.
