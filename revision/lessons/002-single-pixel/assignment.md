# Follow a store into memory

This development unit accompanies the revised main book's addresses and memory
chapter and its written lab. Use MIPS64 Release 6, Default memory and little-endian
byte order. The complete memory-pixel.asm is the same worked program as the book.
Keep an unchanged copy while investigating faulty stores.

Before execution, distinguish the address 0x10010000 from the color 0x00336699.
After its word store, inspect bytes 99 66 33 00 in increasing address order. Trace
which values are copied into registers before the later byte store. The program
prints nothing: an empty console does not establish that its memory is correct.

The native lesson provides predictions and four ordered Debug observations.
Place breakpoints at store_color and change_blue. At store_color, the word store
is next to execute. Resume to change_blue after inspecting the first store and
loads. Then step over the byte store and final loads as directed in Learn.
Debug highlights the next instruction, not the instruction whose effect you are
already seeing. Use the actual session's memory and Bitmap views as additional
observations; the ordered checkpoints compare the named registers at those
boundaries. Editing machine state or using Backstep requires a fresh forward
Debug session for these observations.

The written lab explains the memory bytes, display cells, wrong offsets and
store widths. Its independent variations have starters and six public final-memory
cases each in independent/. Read independent/README.md before writing them.
Checking runs the current editor text in separate machines and does not change
your active Run or Debug session. Source edits invalidate older execution credit.

This package is a draft. Automated CLI qualification and IDE walkthrough status
are recorded separately; it has not received a representative learner review.
