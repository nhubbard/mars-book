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


## Native FIR integration, 22 November 2026

The sixteenth main unit now has nine inline predictions, 32 Debug boundaries
across ten complete sources, three ordinary Runs and five independent
assessments. Scalar impulse reasoning precedes asymmetric coefficients, signed
widening, exact doubleword sums, one final rounding, strict clipping, zero work
and a full-rate five/eleven split. The decimation exercise preserves the same
arithmetic while advancing retained windows by four bytes.

The native profiles preserve all 141 original function-trial constructions and
the original caller observations, adding four three-call split cases: 146 cases,
155 calls and separate silent exit. Original budgets are retained. An independent
Kotlin export matches all original initial bytes, Java Random data, offsets and
expected results. Twenty-six original/native qualification processes accept ten
correct variants/routes and reject sixteen faulty ones as expected. Equivalent
scalar, vector, relocated and wide-address solutions remain accepted.

Integration exposed a real checking gap: case-wide memory permissions let a
later invocation access bytes outside its own selected slices. MARS commit
1974c0ef adds optional call.accessPolicies in function-profile version 9. Call
traces exist only during the relevant invocation and case restrictions remain
active. Host setup and inspection are excluded. Tests cover preceding-slice reads,
same-value earlier-output stores, zero work, full access widths, deep immutable
capture, cancellation, invalid spans and combined limits. Earlier formats reject
the new field; omission preserves their behavior. A FIR counterexample passes
the original script and broad native profiles, then fails the scoped profile at
Call 3 for a one-byte read before its selected input, including a zero-work tail.
Correct overlapping history remains permitted.

The complete regression passes 226 CLI / 1229 plugin tests with no failures,
errors or skips and both required coverage gates. Final focused tests import the
exact version 9 archive, verify its split/caller windows and demonstrate that a
broad call policy cannot widen a narrow case policy. All 497 book-tool and 127
separate-authoring Jupiter tests pass. Exact packaged plugins pass paired public
API verification with 91 MARS / 20 authoring experimental notices.

Two actual light-theme IDE walkthroughs use the new MARS archive. They exercise
wrong-answer recovery, all nine quizzes, 32 Debug boundaries, three Runs, starter
0/43 and correct unsaved assessment counts 43/36/34/33/1. Editing eleven sources
clears eighteen execution passes. Reopening retains all ten quiz attempts and
eleven unchanged source hashes, then renews eighteen checks. Eleven installed
JARs match the pinned archives in each process. Logs contain no ERROR/SEVERE;
projects close before their owned IDE processes stop. Actual clean cleanSandbox
runIde tasks precede both launches. The earlier version 8 walkthrough remains
historical evidence; it does not establish the stronger invocation restriction.

The final distributed archive clarifies overlapping history in its independent
README after those IDE imports. That is the sole changed member; every execution
and grading byte matches. The final archive separately passes a focused Jupiter
production-reader import. No extra GUI run is claimed for this documentation-only
clarification. Screenshot automation remains in the separate dependent plugin.

The directly edited lab chapter now supplies the complete native route, concrete
source variations, independent group descriptions and first-mismatch guidance.
The 204-page proof places this unit on 109–114; all six pages and adjacent
boundaries were visually reviewed, including the three lane strips in grayscale.
The exact final PDF passes 1728 PDF/UA-2 rules / 966296 checks with zero failures.
Embedded Minion Pro, Noto Sans Math, JetBrains Mono and existing native annotations
remain. All 41 manual MARS files are unchanged. Human learner and assistive-tool
reviews remain pending. The curriculum goal remains active; the proposed opening
is deferred. The next main unit develops compact integer encodings, followed by
its Base64 companion.
