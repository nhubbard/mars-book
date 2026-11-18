# Keep cursor positions in bytes
The input pointer identifies the first available byte. The cursor is an offset
from that pointer, not an address. At entry the cursor is zero. A successful
character read uses the full-width address obtained by adding the cursor to the
base. Advancing the cursor does not alter the base, and a small input length does
not make a word-sized pointer sufficient.

Before any load, establish that the cursor is less than the supplied length.
Skipping whitespace must perform the same check on every iteration. A slice of
length zero has no byte available at its base, even when the surrounding allocation
is mapped. An all-whitespace slice reaches end of input and then reports a missing
operand at the length offset.

Only four whitespace byte values are accepted: decimal 9, 10, 13 and 32. Vertical
tab, decimal 11, is invalid. A space between `1` and `2` ends the
first literal; it does not turn `1 2` into twelve. The second digit is a
trailing token, diagnosed at byte two. A non-ASCII character may occupy several
bytes in the source encoding, but this grammar accepts none of those multibyte
sequences as a token. Diagnostics count bytes, including preceding whitespace.
