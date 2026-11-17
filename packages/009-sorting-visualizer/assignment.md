# Visualize insertion and selection sort

Sort the active prefix of the eight-word values array into signed ascending order.
The input count is zero through eight, and every active value is an integer from −4
through 3. Inactive words may contain arbitrary 32-bit values and must receive no
execution stores, even temporarily. Preserve count, algorithm, and every supplied
guard throughout execution. Use the Default little-endian layout. The frame is 256
bytes at 0x10010000, followed by frame_guard at 0x10010100.

Algorithm zero selects adjacent-exchange insertion sort: extend a sorted prefix one
position at a time, exchange an item with its left neighbor while it is strictly
smaller, and stop moving it when equal or greater. Algorithm one selects selection
sort: scan each remaining suffix for its first minimum, exchange that minimum with
the suffix's first item if their indices differ, and advance the sorted prefix.
Use strict less-than when updating a minimum. Self-exchanges are not published.
These policies define reproducible visualization states, including duplicate keys.

## Public visualization interface

When main runs, reach `sort_ready` after rendering the initial array (phase=0, `swap_count`=0), after
every completed algorithm exchange (phase=1, incremented `swap_count`), and once at
completion (phase=2, unchanged final `swap_count`). At each entry the array must be a
permutation of its initial active values and the whole frame must match it. Both
word stores of an exchange precede publication. Do not publish a half-exchange.
After the final snapshot, exit normally through done. Empty, singleton, and already
sorted inputs still publish initial and final snapshots, with no extra exchange.

Column x represents active item x. Encode value v as bar height v+5, from one through
eight cells. A cell at screen row y is white 0x00ffffff exactly when x<count and
y≥3−values[x]; every other cell is black zero. This offset encoding makes negative
values visible while preserving signed order; it is not a signed-magnitude plot.
Write all 64 frame words on every render, including black and inactive cells.

Provide a callable render function. Its arguments are `a0`, a pointer to a word array,
and `a1`, its valid length zero through eight. It reads only that active prefix, writes
the fixed frame, reaches `sort_ready`, and returns through `ra`. It preserves `s0`..`s7` and
`sp` and changes no source words, guards, count/algorithm, phase, or `swap_count`. Temporary
and argument registers may be changed. The caller owns publication metadata; an
independent render call leaves it unchanged, regardless of its current values. A caller can pass another valid word-aligned
prefix address, so the function must not rely on main's saved-register setup or the
values symbol itself. done is the normal exit continuation used by independent tests.

The guide uses a leaf function: it calls no other function and therefore does not
need to save a nested return address. Equivalent implementations may use other internal
structures if they preserve the stated contracts. The public labels do not prescribe
private loop labels, scratch registers, or an exact instruction total. A shift-based
insertion implementation needs a different animation contract; this project explicitly
publishes complete adjacent exchanges.

Produce no console output and exit within 100,000 completed instructions. No host-time
animation delay is required. A fast Run can skip visible repaints; Debug at `sort_ready`
provides a stable complete snapshot. The checks observe real arrays and frames rather
than trusting a student-written log of intended swaps.

## Submit and explain

Submit main.asm, a trace of both algorithms on the default input, and a statement of
each sorted-prefix invariant. Explain why order alone does not prove value preservation,
how duplicates affect stability, and how the rendering call returns without destroying
algorithm state. Compare sorted, reversed, and shuffled eight-item fixtures using both
whole-run and sorting-range instruction/branch counts, with the measured scope stated.
