# Draw the rows in reverse vertical order

Replace draw_vertical's break body. The caller passes the address of eight source
bytes in a0 and the base of a sixteen-by-eight frame in a1. Destination row y
uses source byte 7-y. Draw into columns eight through fifteen; bit seven remains
the leftmost pixel of each eight-pixel row. Store zero for black and 0x00ffffff
for white. Each logical pixel occupies four bytes, and the row stride is 64 bytes.

Preserve the left eight columns, all source bytes, frame_guard and rows_guard.
Return through the incoming continuation, restore sp, and preserve s0 through
s7, fp and gp at full width. Keep the 32 caller-owned bytes at and above the
incoming aligned sp unchanged at return. Produce no console or host/device I/O
apart from the ordinary memory stores that draw the frame. You may call the
provided draw_row or write an equivalent renderer. There is no required private
label, scratch register, frame layout or exact instruction count.

The supplied main makes two ordinary calls, reloading the arguments before the
second. after_vertical and after_repeat are distinct continuations. The native
checker calls the function directly and pauses before each continuation rather
than executing those caller instructions. Both calls must satisfy the same
contract; do not depend on a particular incoming link or a new zero-filled frame.

Five public cases use an asymmetric sprite, a single-bit diagonal, mixed edge
bits, all black and all white. Before each case, the checker initializes every
frame word to a distinct nonzero value, replaces the source and guards, chooses
full-width saved-register values and aligns a different caller stack location.
It passes actual assembled data-label addresses. The frame and source may move
in memory without changing the interface. Preserve the public fixture labels so
these objects can be located; implementation labels remain your choice.

Each case makes two calls in one machine. After each return, checks compare all
128 frame words as bytes, the source, both guards, the saved registers, sp and
caller bytes, and require silent execution. These are after-return comparisons,
not a complete write history. Memory and scratch state persist between the two
calls. Each case owns a fresh machine and a cumulative budget of 20000 executed
instructions. Five passing cases represent ten returned calls, not exhaustive
coverage of all possible eight-byte sources.

Read the adjacent version-2 function profile to inspect the exact public values.
Its byte strings use the book's little-endian word layout. The adjacent Kotlin
script expresses the same checks through the CLI grading DSL. With JDK 25 and a
MARS CLI that supports label-relative byte setup, run from this directory:

java -jar /path/to/cli.jar grade vertical-sprite.kts vertical-sprite.asm

Use failure details to locate the first violated obligation. Keep your own
address calculation and a trace of the first and final rows; a passing report
cannot supply that explanation. Instructor policies can control progression,
attempts and hint release. Private instructor materials remain separate.
