# Project contract

Implement `transpose_naive` and `transpose_tiled` for separate row-major word matrices.
For source shape R rows by C columns, both produce shape C rows by R columns and copy
input[i*C+j] to output[j*R+i]. Each element is an arbitrary 32-bit pattern. Each
dimension is an integer from zero through sixteen, inclusive. An empty dimension
requires zero matrix reads and writes.

On entry, `a0` is the full source pointer, `a1` the full destination pointer, `a2` is R,
and `a3` is C. Pointers are word aligned, nonoverlapping, and name active windows of
R*C words wholly inside the named `input` and `output` allocations of 256 words each.
Do not assume the pointers equal the label addresses. Return R*C in `v0`, preserve
all 64 bits of `s0` through `s7`, and restore `sp`. Other argument/temporary registers
may change. Return through `ra` to the supplied continuation. Do not perform I/O.

The source allocation and all guard words are read-only. Do not write inactive output
prefixes or suffixes, even if restoring their values. Within the two matrix allocations,
perform exactly one aligned word read of each active source cell and one aligned word
write to its corresponding destination. No output reads or source writes are permitted.
Each cell must be read before its destination is written. The checks use the simulator's
native data-access notices and their widths; they do not prescribe an opcode spelling.

The ordinary function's reads follow source row-major order; its corresponding output
writes follow that same source-cell order. The tiled function completes all reads and
writes of one clipped 4-by-4 source tile before accessing another tile. Any outer tile
order and any internal schedule satisfying that rule are accepted. Auxiliary/stack
traffic outside the matrix buffers is allowed, subject to the calling and protection
contract. It contributes to whole-function cache measurements. Exact reference miss or
instruction counts are not correctness requirements.

Keep `transpose_done` as the stopped return boundary for checks, followed by the supplied
caller code that stores `v0` in `copied` and exits normally. The default main chooses
ordinary with METHOD=0 and tiled with METHOD=1. Function grading invokes both directly,
regardless of METHOD. Guided fragments are appended in the named starter regions;
free-writing mode may use different internal labels and registers.

The comparison uses eight cache lines of four words, LRU replacement, and a cold cache
at function entry. Stop before the caller's `copied` store. Compare full associativity,
direct mapping, and two-way placement at constant capacity. Describe cache misses,
simulated instructions, and host elapsed time separately. Actual IDE screenshots and
interactive progression are planned; CLI checks and author-verified cumulative programs
are executable now.
