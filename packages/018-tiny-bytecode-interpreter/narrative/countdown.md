# Trace the countdown program
The default program maintains a sum below a counter. It begins by pushing zero and
three. At the loop head, duplicate the counter and let the conditional branch pop
that duplicate. A zero counter selects cleanup; a nonzero counter continues to the
body while leaving the original counter available.

The body swaps the counter below the sum, copies that counter with `OVER`,
and adds the copy to the sum. Another swap restores sum below counter. Pushing one
and subtracting decrements the counter, then an unconditional branch selects the
loop head. At cleanup, drop the zero counter and halt with the sum live.


See assignment.md and the companion fixtures for complete numerical tables.


The last two rows halt with status one. Earlier rows return status zero because the
budget was exhausted first. The image has eighteen bytes and thirteen encoded
instructions, but the default run retires thirty-three instructions because of its
loop. Native MIPS instruction count is a fourth, separate quantity that includes the
interpreter's validation and dispatch work.
