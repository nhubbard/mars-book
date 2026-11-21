# Filtering a sequence of samples

Chapter 16 develops original project `021-fixed-point-fir` after PCM mixing.
The main proof has 104 pages, with this chapter on 99–104; the lab proof has
95 pages, with this chapter on 91–95. The advanced PCM/H.263 proof is unchanged.
It connects chronological windows, signed Q15 coefficient codes, exact accumulation,
single nearest rounding and final saturation. The main book explains the scalar
calculation before widening and reducing the equivalent vector calculation. The
written lab supplies numerical predictions, actual debugger observations, boundary
diagnosis, split-call reasoning and independent decimation by two.

The complete public `labs/files/fir-filter.asm` has compact branches, aligned
caller stack setup, a vector entry and a retained scalar function. It uses seven
explicit history samples and no hidden state. One full eight-tap window produces
one output; the input pointer advances two bytes. Zero outputs read no caller data.
The three printed fragments match the executed instruction sequences with
observation labels omitted and indentation normalized.

Outcomes are to derive a window's indices and coefficient order; keep units and
intermediate widths explicit; explain why two individually valid word products
can overflow their word pair sum; distinguish sign extension before arithmetic
from a later wide copy; distinguish rounding from clipping; and preserve stream
history when splitting calls. The independent task computes only even-indexed
windows without discarding samples within a window.

Vector, scalar and wide-address FIR variants each pass 679 public kernel, 65
caller, 5480 private, 16 repeated-call sequences and 65 split positions. The
private set includes every signed sample under four fixed coefficient/position
domains, 1024 random trials, all 256 endpoint-sign masks and 104 selected tap
cases. This is not all possible input/coefficient windows. A signed-halfword
scalar alternative additionally passes 679 public and 5480 private cases,
including odd byte addresses supported by the Release 6 target.

`labs/files/fir-decimate.asm` is an intentionally failing independent starter.
The public `labs/checks/fir-decimate.kts` accepts equivalent solutions. Its 142
cases cover all counts zero through 32 under four trial families, six half-tie
neighbors, three rounding/clipping distinctions and the default caller. Scalar
and vector solutions each pass those and 1601 expanded cases; a wide-address
vector also passes the expanded set. The expanded domain has all 33 counts
under 32 correlated offset seeds, 512 deterministic random trials and all 33
split positions. It does not cover all independently selected pointer offsets
or all sample/coefficient combinations. The starter fails all 142 cases.

Seventeen distinct semantic faults are rejected, twelve in filtering and five
in decimation. Thirteen positive written probes check the exact signed words,
pair and combined sums, full numerator, rounding/clipping boundaries, asymmetric
impulse order, one-final-rounding example, window counts and ordinary stack setup.
Schedule-specific observations do not constrain independent temporary registers.
No checker capability was missing and no MARS implementation change was required.

Three actual light-theme native Debug captures show widened words, pair sums
and combined sums for minimum inputs and coefficients. Native LaTeX supplies
all labels, crops, boxes and semantic descriptions. Source/image hashes,
component rectangles, batch receipts and eleven installed archive/JAR bindings
remain private. The separate authoring plugin ran the actual
`clean cleanSandbox runIde` tasks. The completed dedicated instance was terminated
with SIGTERM; no native close/restart or native Run verification is claimed.

Three fresh actual IntelliJ highlighting exports add three records, preserving
all previous 809 for 812 total. The authoring suite passes 104 Jupiter tests;
public tooling passes 391 tests. The existing compatible package validates and
builds, but the revised independent exercise is not yet a native lesson package.
Final proofs receive page/grayscale review and automated PDF/UA-2 validation.
Human author, learner and assistive-technology reviews remain open.

The original manuscript and unrelated MARS edits remain preserved. The next
main chapter develops compact integer and byte encodings; the separate Base64
companion follows the agreed prerequisite sequence. Later compositions, native
lessons beyond the first unit and production instructional-system work remain
unfinished. The proposed MARS-versus-hardware opening remains deferred.
