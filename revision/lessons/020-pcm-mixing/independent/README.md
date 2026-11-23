# Independent saturated subtraction

Implement subtract_pcm16 in pcm-subtract.asm, preserving the supplied caller and
public data labels. Arguments a0,a1,a2 are full-width A, B and output pointers;
a3 is a count from zero through 64. Each selected slice contains 2*count bytes
at arbitrary byte alignment. A and B are disjoint or exactly identical. Output
is disjoint from both inputs or exactly equal to either, including all-identical
pointers. Shifted partial overlaps are excluded.

Recover each signed little-endian halfword. Compute the exact difference A−B in
a sufficiently wide intermediate, then clamp strictly outside −32768…32767.
Store the saturated halfword and return the original count in v0 and number of
clipped differences in v1. An exact endpoint contributes no clipping event.
Initialize the count for every call, including an empty call. With count zero,
read neither selected input nor selected output and write no selected output.
For identical inputs, write zero samples and return zero clips.

Read only selected A, B or output bytes within the assembled caller region;
write only selected output bytes. Complete access widths must fit a permitted
slice. Unnamed padding, guards, metadata, inactive output and nonaliased input
must stay untouched. Reads of selected output are allowed. Load both original
operands before an aliased output store. Ordinary Release 6 signed halfword
loads/stores support odd addresses; do not round a passed pointer. Preserve
full s0 through s7, fp, incoming aligned sp and caller stack bytes. Private
constants and a restored private stack frame are allowed. Produce no console
I/O and return through the incoming continuation.

The default nine differences are −32768,32767,32767,−32768,1,32767,−32768,−4444,
32767, with five clips. At subtract_ready, live v0/v1 are 9/5 while processed
and clipped still contain 99. At results_ready, the caller has published 9/5.
It then exits silently. Choose a positive/negative pair that exposes reversing
A−B, and distinguish exact endpoints from just-outside values.

The unchanged standalone pcm-subtract.kts retains all 332 public cases: counts
zero through 64 under five alias arrangements, six boundaries and the complete
caller. Each original case has a 100000-instruction budget. Native checking
preserves every original function-trial construction and splits the corpus into
five groups, respecting the reader's data and execution limits. The disjoint
group adds a four-call check that alternates clipped vector work, an unclipped
scalar call, clipped work again and an empty call. It uses both continuations
and a cumulative 400000-instruction budget. This checks returned state across
calls; the ordinary count-zero cases independently check empty access windows.

The native groups contain 73,65,65,65,65 cases: 333 total, with 337 calls because
the caller has separate return/publication observations and the reused case has
four calls. All six native assessments must pass for the current source: five
function groups and silent normal exit. Adjacent JSON files contain bounded
grading data; the plugin does not execute the standalone Kotlin script.

Keep your signed-value derivation, clipping-mask units, group gate, scalar tail
and exact-alias argument beside the passing evidence. No private label, scratch
register or exact instruction count is required. Instructors may separately
review MSA use. The finite cases are not exhaustive over every pair of signed
samples or every independently chosen source/destination offset.
