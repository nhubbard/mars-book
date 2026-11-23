# Independent RGB555 packing

Implement pack_rgb555 in rgb555.asm. Preserve the supplied caller and public data
labels. Arguments a0 and a1 are full-width input and output pointers; a2 is a
count from zero through 32. They select disjoint byte slices inside the supplied
buffers. Treat each input triple as unsigned red, green and blue bytes. Produce
one little-endian halfword per pixel:

    ((red >> 3) << 10) | ((green >> 3) << 5) | (blue >> 3)

All shifts in this expression are unsigned. The top bit of each result is zero.
Return the original count in v0 through the incoming continuation. For count
zero, access neither selected buffer. For other counts, read exactly within the
selected 3*count input bytes and write within the selected 2*count output bytes.
The output pointer can be odd: do not round it to an alignment boundary.
Ordinary Release 6 halfword loads/stores support unaligned addresses on this
target. Wider accesses must still fit the selected slice in full.

Preserve input bytes, guards, metadata and inactive output bytes. Within the
assembled caller region, execution reads may touch only selected input or
selected output, and stores may touch only selected output. Unnamed padding is
also outside those permissions. Reads of the selected output are allowed; the
checker does not impose a particular partial-result strategy. Preserve full s0
through s7, fp, incoming aligned sp and caller stack bytes. A restored private
frame below incoming sp is allowed. Produce no console I/O.

The default eight pixels include (123,45,67), which produces 0x3ca8, stored as
bytes a8 3c. White produces 0x7fff. The caller receives eight before it publishes
completed=8 and exits silently. The independent checker covers counts zero
through 32, interior offsets including odd destinations, untouched bytes, saved
state and return behavior. Native function-profile version 8 contains those 33
trials and the complete default caller. Each direct trial returns through two
different continuations, giving 67 calls across 34 cases. Its cumulative budget
is 200000 instructions per two-call trial and 100000 for the caller. A separate
console profile checks complete normal exit.

The unchanged rgb555.kts script is also supplied for command-line checking. Its
34 cases remain a separate route, with one direct call per count and 100000
instructions per case. Adjacent JSON profiles are bounded grading data, not
executable scripts. Native lesson format 7 requires MARS commit 923e114f or later.

Begin with a scalar derivation, choose a vector layout and prove its group gate
and tail. Equivalent scalar, vector and halfword implementations are accepted;
no scratch-register assignment, private label or exact instruction count is
required. MSA use and strategy are subjects for explanation and instructor
review. The finite cases do not prove correctness for every RGB888 input or all
independently chosen source/destination offsets.
