# Windows, wide sums and stream history

Read Chapter 16 and its written lab. Begin with the eight-product scalar
calculation, keeping coefficient scale beside the exact numerator. Follow the
scalar impulse and unchanged vector source to establish the same complete
output and counts before comparing their instruction schedules.

Use the asymmetric impulse to explain coefficient order. Then inspect widened
words, doubleword pair sums, combined lanes and the complete 64-bit numerator.
Each individual product can fit a word while its pair sum does not. Widening
after the arithmetic has overflowed cannot repair the lost result.

Follow negative and positive half ties, a just-outside rounded integer and the
round-once example. Separate a wrong numerator from a wrong conversion of a
correct numerator. Decide clipping from the rounded integer, not from a stored
endpoint or the unrounded fraction. Every small Debug example can resume to
normal exit; separate ordinary Run tasks are used for complete comparisons and
the split caller rather than repeated after every single-window observation.

Continue the stream explicitly. For the full-rate filter, the second input base
after five outputs advances ten bytes, retaining the predecessors needed by the
next window. The independent decimator retains windows at input indices 0,2,4,…
while preserving all eight consecutive samples within each window. Derive its
input and output advances before writing code.

Implement decimate_q15 under independent/README.md's contract. Run the four
function groups and separate normal-exit check on the same current source.
Keep window indices, coefficient order, arithmetic widths, conversion rule and
split-call phase beside your passing evidence. Equivalent solutions are accepted;
explain a chosen vector strategy separately from behavioral acceptance.
