# Separate component order from byte order
For pixel index $i$, packed RGB888 places red at byte $3i$, green at $3i+1$ and blue
at $3i+2$. Each byte is interpreted unsigned, so a load must produce zero through
255. A signed byte load would turn values above 127 into negative integers unless
the code explicitly removed that sign extension.

The supplied byte declarations use signed initializer notation where the assembler
requires it. For example, `-9` stores the eight-bit pattern `f7`.
An unsigned byte load reads that pattern as 247. The initializer spelling does not
change the channel's unsigned interpretation.

RGB565 first defines a numerical halfword. Its fields are independent of how the
halfword is stored. This exercise then chooses little-endian storage: the low eight
bits appear at byte $2i$ and the high eight bits at $2i+1$. Unaligned slices make
individual byte loads and stores a suitable scalar baseline. A halfword load or
store has additional native alignment requirements; mapped memory alone does not
establish that its address is aligned.


See the native chapter for the described conceptual diagram.


The diagram places the most significant field on the left, as is usual when writing
a binary number. Memory still stores the low byte first in this exercise. Do not
read the drawing from left to right as an address map.
