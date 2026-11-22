# Clockwise rotation

Implement `rotate_clockwise` in `matrix-rotate.asm`, replacing its `break` body.
The caller passes a source pointer in `a0`, destination pointer in `a1`, row count
in `a2` and column count in `a3`. Both dimensions are integers zero through
sixteen. The word-aligned pointers name nonoverlapping active windows inside
the two 256-word allocations and may differ from the allocation labels. Use the
passed pointers. The source words contain arbitrary 32-bit patterns.

Map source coordinate `(i,j)` to destination `(j, rows-1-i)`. The destination
has `columns` rows and `rows` columns. Its linear index is therefore
`j*rows+(rows-1-i)`. For source rows `[0,1,2]` and `[3,4,5]`, the required output
rows are `[3,0]`, `[4,1]`, `[5,2]`. Return `rows*columns` in `v0`. Empty shapes
return zero without any matrix access. Do not infer the row width from a square
example or truncate a full pointer when adding a bounded byte displacement.

Preserve source words, inactive destination words, guards and caller metadata.
Preserve `s0`–`s7`, `fp`, `gp`, the incoming aligned `sp` and the caller's 32 stack
bytes. Private frames below the incoming stack pointer are permitted. Return
through the supplied `ra`; produce no output. The caller stores the returned
count in `copied`, then exits.

Each active source word must receive exactly one aligned four-byte read notice.
Each active destination word must receive exactly one aligned four-byte write
notice, after the corresponding source read. Do not read the destination or
write the source, including temporary writes that are later restored. Any
traversal order meeting these rules is accepted. You do not have to use tiles
or achieve a particular miss or instruction count. These are memory-notice
requirements, not a fixed instruction sequence.

Learn's first check has 22 cases. Twenty-one shapes include empty matrices,
single rows/columns, rectangles, tile-edge dimensions and the full allocation.
Each shape is called twice through different continuations, with signed word
patterns, full-width saved state and interior pointer windows where space permits.
The per-call transfer observation resets between calls. Bounds checks also
observe inactive allocation regions, so an empty matrix cannot read a first word
before noticing its empty dimension. Final memory checks retain all 256 source
and output words; store observations protect input and caller-owned bytes even
when their final contents are restored. The last case runs `main` through its
result store and checks the complete default output and count fifteen.

Learn's second check runs the same source to silent normal exit. Both checks
must pass for the current revision. Each function case has a cumulative budget
of 100000 instructions; the normal-exit case allows 10000. These budgets prevent
an unbounded run; they do not prescribe an exact implementation cost.

For command-line checking, use the accompanying `matrix-rotate.kts` with
`mars grade`, as described in the lab. It preserves the original ten public
shape checks, including per-cell trace order and complete program behavior.
The JSON profiles are Learn's data, not executable grading scripts. A passing
sample corpus does not prove the mapping for every shape: justify the formula,
empty path and address bounds separately. On failure, retain the shape, window
offsets and the named source/destination element before changing a loop.
