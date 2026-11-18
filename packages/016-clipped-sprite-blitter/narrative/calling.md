# Borrow saved registers without losing caller state

The provided function scaffold reserves 64 bytes below the incoming stack pointer. Eight `sd` instructions preserve `s0` through `s7`. The corresponding `ld` instructions restore all sixty-four bits before adding the same 64 bytes back to `sp`. A word save would lose the caller's high register bits, even if every color and dimension used by this exercise fits in a word.

The function is a leaf: it calls no other function, so it does not overwrite its incoming return address. The supplied `jr` returns through that address. The scaffold does not use `fp`, which therefore remains unchanged. Keep the entry and restore sections while completing the guided regions. Every early exit branches to the same restore label; returning directly from the middle would leak the stack frame and borrowed register values.

Before assigning a saved register a second role, identify its last use in the first role. The guided loop reuses `s5` and `s6`: initially they hold the clipped left and top edges; after pointer construction they hold the copy width and height. This reuse is safe only after both source and destination origins have been computed. The debugger can display an edge or a loop extent in the same register at different breakpoints.
