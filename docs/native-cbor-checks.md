# Native CBOR sequence checks

The revised Chapter 21 independent task implements `cbor_sequence_count` in
`revision/labs/files/cbor-sequence.asm`. Its original Kotlin checker remains in
`revision/labs/checks/cbor-sequence.kts`. The new native data author preserves
all 203 original cases and adds two separately named cases for complete caller
execution and repeated calls within one session. This is the grading foundation;
the connected native guide and its desktop walkthrough are still being composed.

`tools/cbor_sequence_samples.json` retains every original input, selected count,
inherited depth, offset, case name and four-million-instruction allowance. Its
source digest binds that finite grid to the original checker. Decimal strings
retain unsigned 64-bit counts and depths without a host JSON number conversion.
`tools/cbor_sequence_lesson_cases.py` authors native function format 14 using a
small independent host model of the existing local supported-item profile. It
counts generic supported values; it does not apply the telemetry record schema.
It neither runs assembly nor recognizes a solution's source. Publication prose
remains directly edited LaTeX.

The local profile admits unsigned and negative integer heads, byte strings through
32 bytes, arrays through 64 elements, and maps through eight pairs with unsigned
keys zero through seven. Key identity is decoded numeric identity, including
nonpreferred head spellings, and each map has its own duplicate mask. A container
can open only while inherited depth is below eight, including an empty container.
Scalar values and byte strings do not open a container. These are the teaching
profile's rules, not a claim to accept all CBOR types.

The full-width unsigned selected count may be zero through 1024. A larger count
has status 2 before any pointer use, even when inherited depth is also invalid.
Depth above eight has status 1. An admitted empty selection succeeds with zero
items and does not use the source pointer. A complete nonempty sequence returns
status zero and its item count; any malformed suffix fails with status one and
zero items rather than publishing the count of its valid prefix.

Every original case seeds the complete source, record, destination and workspace
arenas with the original bytes, preserves s0–s7/fp/sp at full width, and retains
the original assembled workspace-end stack pointer. Three per-object policies
permit only selected source reads and prohibit all writes to caller objects.
The original post-call complete source/record/destination snapshots are also
compared exactly. Workspace remains private scratch storage.

A separate 500,000-notice access history covers the enclosing span from source
to the end of workspace. It includes padding, controls and stack traffic without
granting or prohibiting access there. Original exports verify that this exactly
matches the original watch span and caller-object windows in the retained
remaining-count, end-address and wide-address implementations. Each invocation
has a new history, so one call cannot hide a later truncated trace. Attempts at
file I/O fail: these are memory entries, and the original checker also rejects
any recorded file operation.

The supplemental ordinary caller starts with its actual assembly image, reaches
`sequence_return` with status zero/count one, and must then terminate and store
those results correctly. It does not seed the image with the function-test poison
setup. The retry case keeps one session and performs malformed, successful,
empty-at-depth-eight, and inherited-depth-failure calls, expecting status/count
pairs (1,0), (0,1), (0,0), (1,0). It retains the same caller snapshots and gives
each invocation its own access history. Its four calls share their case budget.

The resulting 205 cases / 208 calls occupy 104 profiles under the existing
ten-million-instruction and one-MiB JSON limits per profile. No original budget
is reduced. Generate them with `python3 tools/cbor_sequence_lesson_cases.py DIR`,
or add `--check` to compare a generated directory. Native lesson formats 10/11
can reference these bounded batches in a coherent assessment.

Private qualification runs accept the three retained correct designs, reject
six existing faults, the unfinished starter and a new unowned-close fault. The
original 203 checks accept the deliberate stale-empty and wrong-caller-store
counterexamples; the native corpus rejects each through its corresponding new
case while the other 204 cases pass. All 203 original inputs, budgets, registers,
full seeded arrays, exact trace spans and permissions were compared with exports
from the actual original grader in all three address/layout variants. The actual
profile archive is also loaded through the production native reader in a Jupiter
test. This is finite behavioral evidence; no learner walkthrough or new publication
capture is claimed by this grading-only stage.
