# Independent decimation by two

Implement decimate_q15 in fir-decimate.asm, preserving its caller and public data
labels. Arguments a0,a1,a2 are full-width input, coefficient and output pointers;
a3 is an output count from zero through 32. Every sample and coefficient is a
signed little-endian halfword. The input, coefficient and output slices are
disjoint and may start at arbitrary byte alignment.

For retained output i, multiply input samples 2*i through 2*i+7 by the eight
oldest-first coefficients and add all products exactly. Compute the rounded
integer as floor((numerator+16384)/32768), then clamp to −32768…32767. This is
one nearest conversion with half ties toward positive infinity. Count a clip
only when that rounded integer is outside the range. Per-product rounding or
clipping the unrounded numerator changes the specified operation.

Store one halfword per retained output. Return the original count in v0 and
strict clipped-output count in v1, initializing both for every call. For positive
n, input has 2*n+6 samples, coefficients have eight and output has n. The last
window spans input indices 2*n−2 through 2*n+5. With n=0, seven declared history
samples remain unread: read neither input, coefficients nor output and write
nothing. Backing arena capacity is not permission to access outside a selected
slice. Odd pointers are supported on the Release 6 target; do not round them.

Within the assembled caller region, read only the selected input, coefficients
or output and write only selected output. Preserve all other bytes, including
guards, unnamed padding, metadata, input/coefficient contents and inactive output.
Preserve full s0 through s7, fp, incoming aligned sp and caller stack bytes.
Private constants and a restored private frame below incoming sp are allowed.
Use the supplied continuation and produce no console I/O.

A split after m retained outputs starts the next input at original sample 2*m
(byte offset 4*m) and its output at byte offset 2*m. Include the overlapping
history needed by its first window. This retains full-filter output indices
0,2,4,…; reading every other sample inside a window computes something else.
The default eight-output caller produces four 2000s and four zeros, with no clips.
At decimate_ready the live return values are 8/0 while published words are 99;
at results_ready they are 8/0. Normal exit follows.

The unchanged standalone fir-decimate.kts retains 142 public cases. Its 141
function trials cover every count under four input families, six half-tie
neighbors and three rounding/clipping distinctions; the remaining case checks
the complete default caller. Native profiles preserve every original input
construction, including Java Random values, role-swapped arenas and byte offsets.
Four additional cases compare a whole call with split calls at cuts zero, five
and 32 using asymmetric impulses, plus a clipped split that verifies independent
return counts. They use both continuations and preserve the already written
whole result while constructing the split result in a separate output slice.

The native groups have 43,36,34,33 cases: 146 total with 155 calls, because the
caller has separate return/publication observations and each split has three
calls. Original cases retain their 100000-instruction budget; split cases have
a cumulative 300000. Each split invocation has its own read/write windows,
active only during that invocation, including its overlapping input history.
A later call cannot read before its own input base or write an earlier
output slice, even if it stores the same value. Empty
invocations allow no caller-data access. The whole-call result remains
protected while the split result is constructed. The caller separately checks
filter execution and the later publication writes. Case-wide restrictions
also remain active; a call cannot expand them.
A fifth assessment checks complete silent exit. All five assessments must pass
for the current source, including unsaved changes.

Explain a scalar baseline, exact accumulation, one conversion, selected access
widths and split phase. Instructors may separately review vector strategy; no
scratch-register layout, private label or exact instruction count is required.
The finite cases do not exhaust every input/coefficient window or independently
chosen pointer-offset combination. The guided lesson requires the updated MARS
build that accepts underscored MSA anchors such as dotp_s.d and copy_s.d
and function-profile version 9 with invocation-scoped access policies.
