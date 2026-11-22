# Signed raw interpolation

Replace only the unfinished `interpolate_raw` body and add any private helpers
it needs. Keep the caller and its data labels so the public checks can call the
function and exercise the complete program. `a0` and `a1` contain sign-extended
signed 32-bit raw words. `a2` is an integer weight from zero through 256, inclusive.
The endpoint words may contain any signed 32-bit value. They share the same
scale; the function does not convert them to whole pixels.

Return in `v0` the sign-extended signed raw integer nearest to
`((256 - weight) * left + weight * right) / 256`. Exact half ties go toward
positive infinity. For example, `(-256, 512, 64)` returns `-64`; `(-1, 0, 128)`
returns zero; weight zero returns the left endpoint and weight 256 returns the
right endpoint. Choose arithmetic widths from the permitted input range, not
just these examples. The exact mean remains between the endpoints, but a
weighted product or endpoint difference can exceed a signed word.

Preserve `s0`–`s7`, `fp`, `gp`, and the incoming aligned `sp`. Return through the
incoming `ra`; either caller continuation may be supplied. Scratch registers
and a private stack frame below the incoming stack pointer are available.
Do not write any of the six caller data words, even temporarily. Do not write
the caller’s 32 bytes at or above the incoming stack pointer. Produce no output.
The caller, rather than the function, stores the returned word in `answer`.

The first Learn check has eight cases. Seven fresh-machine groups contain all
3240 combinations of 18 boundary endpoints and ten weights, plus the original
128 seeded samples: 3368 calls in total. Each group has at most 512 calls, an
instruction budget of 100000, and alternating caller continuations. Passed
arguments differ from the global fixture values. Saved registers contain full
64-bit values. Input and caller-stack store traces reject a write even if its
original bytes are restored. A final case executes `main` until after its
result store and checks `answer` and the surrounding input and guard words.
The second Learn check executes the same source to silent normal exit with a
1000-instruction budget. Both checks must pass for the current source revision.

A failure names the case and, for function calls, the one-based call number,
entry, continuation and supplied scalar arguments. Copy those three arguments
into a small test before changing the algorithm. Passing this finite corpus
is useful evidence, not a proof for every endpoint and weight. Justify the
width and rounding rule separately. Neither check requires a particular
instruction sequence or private frame layout.

For command-line feedback, use the accompanying public `fixed-interpolate.kts`
with `mars grade`, as described in the lab. It contains the same complete
scalar corpus and a whole-caller execution check. The JSON profiles are data
for Learn and are not executable grading scripts.
