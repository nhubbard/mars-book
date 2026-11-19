# CBOR cumulative construction draft

The native reading is drafts/cbor-telemetry.tex, composed directly with includes under
includes/chapters/032-cbor-telemetry. This draft has 56 steps, 52 unfinished regions,
56 quizzes, two hints per step and 57 exact observation recipes. Native TeX is the
publication authority; the structured exercise metadata does not regenerate prose.
Complete implementations and answer keys remain in the private instructor repository.

Use an existing working copy with `python3 prepare.py s01-1 --source working.asm
--output observation.asm`. The helper replaces only selected data declarations and
caller controls, preserves the full instruction section and refuses an existing output.
Mode order is head, skip, decode, bound, encode, pipeline, size, write. All scalar controls
retain their full unsigned 64-bit patterns. Bound/encode recipes seed a valid local
record; decode/pipeline recipes start it as a sentinel. Destination storage always
starts as a sentinel and is never seeded as an expected answer.

Development status four with zero count identifies an unfinished region. This status
is outside completed public statuses zero through two. Prior complete regions may
already have changed private storage, written encoded bytes or published fields after
full validation. Keep this separate from the completed failure guarantees. Propagate
an unfinished child before using its result. Every after-label has a distinct nop;
stop before its instruction. Some intentional branches use a handler checkpoint instead.
The pipeline stops on any nonzero result before continuing to the next entry.

The internal size helper takes an unsigned argument in a0 and returns zero status and
complete preferred head width in v1. The internal writer takes supported major type,
unsigned argument and a sufficiently large destination in a0..a2, returning zero and
complete head width. These are construction contracts for guided observations; the
five completed public entries do not require these private labels or algorithms.
The writer's caller establishes type and extent. Both private helpers use development
status four with zero result until filled. The decoder's consume-head helper uses its
active 288-byte frame and retained cursor/extent; it is not a standalone public entry.

Step 55 has two recipes: short encode capacity and trailing-byte decode. Its per-recipe
mode and return checkpoint are authoritative for the observation, even though the step
introduces both in one passage. The second recipe does not reuse an already-decoded
record. Capture plans are explicitly uncaptured. Public feedback exports, separate chapter distributions and actual IDE captures remain
pending. Earlier prototype evidence proves those earlier sources, not these unfilled
or partly filled construction files. The substantial rewrite remains deferred until
all 35 chapter drafts exist.

All 57 recipes assemble at normal and above-4-GiB addresses, for 114 prepared assembly
checks with warnings treated as errors. The untouched source passes 32 direct-entry
and caller safety cases at those layouts, including the frame-dependent consume-head
helper. They preserve full saved state and input/output arenas and perform no I/O.
This is initial unfinished-source evidence only; the completed checkpoint evidence below is a separate audit. Four light-theme capture plans have no captured pixels or receipts.

Private cumulative execution now verifies all 57 recipes at both layouts: 218 direct
and caller checkpoint/continuation observations. The counter and identifier recipes
move their pairs first so they can run before later field bodies exist; their native
passages state the changed offsets. The inline-size observation uses its successful
exit rather than an after-label bypassed by its branch. Exact intermediate fields,
private candidate bytes, output prefixes, saved state and subsequent development or
success results are checked. Both construction-compatible complete variants also pass
16336 entry-contract cases. Twelve assembled regressions are rejected, 24 nested
development cases pass and twelve actual outputs decode independently. These checks
do not certify every possible input or complete the pending public feedback, chapter
distributions, actual captures or accessibility review. Earlier prototype measurements
retain their original sources and boundaries.
