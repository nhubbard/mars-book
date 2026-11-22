# Change a component; retain a copy

Use MIPS64 Release 6, the Default memory layout, little-endian memory and
pseudoinstruction support. Keep these two programs in separate files. The worked
program changes blue; these exercises ask you to choose the location for red.

Both programs must print `Enter an RGB integer: `, read one decimal integer with
service 5, do their memory work, and exit normally with service 10. Print no other
text. The input precondition is 0 through 16777215 inclusive, the decimal range
for a 24-bit RGB word. You do not need to validate inputs outside that range.
The prompt is the only program output; the supplied input is not part of stdout.
For the worked color 0x00336699, enter its decimal value, 3368601.

## Change red in red-component.asm

Store the input color at address `0x10010000`. Change its red component to 0xAA
while retaining the green and blue components and the zero high byte. Preserve
the neighboring word at `0x10010004`, whose initial value is 0x13579BDF. Read the
modified red byte with `lbu` and inspect it in Debug. For the worked input, the
new complete word is 0x00AA6699. Determine the byte offset from the actual
little-endian layout before choosing your store.

## Retain the original in independent-copy.asm

Store the input color at `0x10010000`, retain its complete original word at
`0x10010008`, and change the first pixel's red component to 0xAA. The copied word
must retain the original RGB value. Preserve the guard words at `0x10010004`
(0x13579BDF) and `0x1001000C` (0x2468ACE0). Draw all four objects before writing
stores. Explain why copying a value into a register alone does not populate the
second memory object.

## Observe and check

Use ordinary Run and Debug on the current file. Compare memory words with their
bytes in increasing address order. You may rename labels, choose scratch
registers and use equivalent calculations; the absolute addresses above form the
shared interface, so retain that data layout. Instructors may require earlier
predictions or Debug observations through the native progression controls.

The public profiles test six input colors: 0, 1, 3368601, 8388735, 16711935 and
16777215. Read the adjacent `.cases.json` for every input, expected stdout and
expected final byte. Each case gets a fresh session and a 20000-instruction
termination limit. Final bytes and normal termination determine the check result;
the checker does not inspect your source pattern or prescribe an instruction count.
The `lbu` observation and your explanation remain part of the written exercise:
a final memory check cannot establish which load instruction you used.

A final unchanged guard establishes its exit value, not the absence of every
intermediate write. Use Debug to investigate an unexpected write even if you
later restore the guard. The six cases are examples and do not prove the whole
input domain. Explain how your operations preserve both retained components
for an arbitrary admitted RGB value.
