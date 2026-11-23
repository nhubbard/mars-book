# Regrouping bytes as Base64 text

The Base64 companion follows Chapter 17 as section 17.7, revising original project
`028-base64`. The lab proof has 105 pages, with the companion on 100–105.
It is directly edited native LaTeX in `labs/companions/base64.tex`.
The main proof remains unchanged at 111 pages and the advanced PCM/H.263 excerpt
at twelve. The written lab develops sextet versus character values, boundary-
crossing bits, canonical padding, whole-buffer validation, exact in-place decode,
vector input/output footprints and independent unpadded URL-safe encoding.

`labs/files/base64-codec.asm` is a complete standard-alphabet scalar/MSA codec
with six completed entries and the original fixture controls. Compact branches
replace the older delayed spellings. The supplied private workspace is aligned
to sixteen bytes for the book's ordinary caller; constants and routine semantics
remain the existing complete contract. The single printed scalar fragment matches
executed operations and a fresh actual IntelliJ export.

The worked reference, a valid alternative and an above-32-bit-address variant
each pass the original encoding (70), decoding (74), canonical (578), bounds (46),
direct kernels (100), caller (6), reuse (10) and private (432) cases. The private
set has 96 seeded binary inputs across four whole entries and 24 late-pad-bit
variants across two decoders. It is not exhaustive over all binary sequences.
Canonical cases cover all one-byte classifier values and selected late errors;
direct kernels check exact 12/16 footprints and permitted in-place decoding.

`labs/files/base64url.asm` is an intentionally failing independent starter.
The public `labs/checks/base64url.kts` has 594 cases: all 65 lengths under five
capacity choices, all 256 one-byte inputs, seven familiar texts, three unsigned
count bounds, empty/no-pointer and capacity-before-source cases, and the caller.
Scalar and vector-based implementations each pass public and 4641 expanded cases;
a wide-address vector-based implementation passes the expanded set. The expanded
domain has all 65 lengths under 32 correlated offsets, every two-byte input in
2048 groups of 32 calls, 512 deterministic trials and a same-session retry case.
It is not every input sequence or every independent pair of offsets. The vector
alternative reuses MSA sextet generation with scalar alphabet lookup; equivalent
behavior alone does not prove a particular instruction strategy or speedup.
The starter fails all 594 public cases.

Seventeen distinct semantic faults are rejected: eleven standard codec and six
URL variants. They include signed byte interpretation, wrong cross-byte shifts,
nonzero unused pad bits, signed count/capacity checks, skipped validation, vector
overread/overstore, uninitialized shuffle controls, extra output stores, wrong
alphabet, padded lengths and padding writes. Ten positive written examples verify
Man's sextets and ASCII, all three tails, late canonical errors and precedence,
vector sextets/ASCII and a thirteen-byte whole MSA tail.

Three actual light-theme native Debug captures show malformed status/zero length,
the first unchanged destination row after a late error, and sixteen numerical
sextets before alphabet conversion. Native LaTeX supplies all labels, crops,
boxes and semantic descriptions. The first row illustrates the failure; separate
executable access checks prove no output stores across the caller buffer.
PNG/source hashes, geometry and eleven installed archive/JAR bindings remain
private. The separate authoring plugin ran the actual
`clean cleanSandbox runIde` tasks. Its completed dedicated instance was terminated
with SIGTERM; no native close/restart or native Run test is claimed here.

One fresh actual IntelliJ export adds one record while preserving all prior 815,
for 816 total. The authoring suite passes 104 Jupiter tests; public tooling passes
391 tests. Original compatible package 028 validates and builds. The revised
independent exercise is not yet a delivered native lesson package. Final lab
proofs receive visual/grayscale review and pinned automated PDF/UA-2 validation.
Human author, learner and assistive-technology reviews remain open.

The initial branch conversion accidentally matched vector .b suffixes and was
rejected by assembly. Its match is now restricted to instruction names at the
start of lines. The initial private URL harness also omitted its shuffle constants;
those were added before accepted qualification. Failed source and diagnostics
remain private, checks were not weakened, and neither failed source entered the
public implementation commits. Original manuscript and unrelated MARS edits are
preserved. No upstream implementation change was needed.

The next main chapter develops bounded file I/O and a complete binary PPM artifact.
Later compositions, native lessons beyond the first unit, production policy/content
delivery and human review remain incomplete. The proposed MARS-versus-hardware
opening remains deferred.


## Native Base64 integration, 22 November 2026

The Chapter 17 companion now has ten inline predictions, 25 Debug stops across
twelve complete sources, three ordinary Runs and five independent assessments.
Worked reasoning connects numerical sextets to character codes, canonical tail
bits, whole-buffer validation, exact twelve/sixteen byte vector footprints,
special-symbol conversion, exact in-place decoding and a thirteen-byte whole
MSA tail. Complete variants retain every executed codec function body. Stops at
the exit syscall distinguish the caller's service selector from published status.

All 593 original direct-function constructions match an independent export from
the unchanged Kotlin script and Java Base64 oracle, including complete arena
bytes, offsets, invalid pointers and unsigned argument bits. Native data retain
all 594 original cases and add one five-call capacity retry/reuse case: 595
function cases, 600 calls and separate silent exit. Four function groups contain
160/165/256/14 cases in 16/17/26/2 bounded files. Every original million-instruction
budget remains. Twenty-four original/native qualification processes accept ten
correct routes and reject fourteen faulty routes. Scalar, vector, relocated and
wide-address alternatives remain accepted; six semantic faults and the unfinished
starter remain rejected.

Retaining the original workspace-based stack required a real grader capability.
MARS 15074f62 adds FunctionCheckCase.initialRegisterAddresses and strict function-
profile version 10. Addresses resolve after initial memory and before stack
alignment and snapshots. Integer/address collisions and aliases are rejected;
caller-owned maps are captured before opening a session. Five simulator tests
cover relocated/full-width pointers, stack alignment/preservation, immutable
capture and failure cleanup. Three parser tests cover immutable data, versioning
and invalid input. No simulator instruction semantics changed.

The full regression passes 231 CLI / 1242 plugin tests with no failures, errors
or skips and required coverage/parser gates. A later focused exact-archive
Jupiter test verifies all vector bits, intra-block publication anchors, batches,
workspace stacks and invalid-pointer cases; no second full run is claimed for
this test-only fixture. All 513 book-tool and 129 authoring tests pass. Paired
verification accepts the exact MARS and authoring archives with 91/20 experimental
API notices. All twelve worked-source probes verify actual values and complete
source/destination access notices. Earlier value-only probes remain historical.

The initial real IDE diagnostic passed the first quiz and Debug stop but exposed
an authoring assumption: the debugger may retain the caret at the paused frame.
The corrected separate driver resolves its trusted fixture anchors from public
MARS syntax types and platform PSI, including the syscall later within main_done.
It uses supported read locking and breakpoint APIs; no production internal
resolver or custom assembly parser is imported. The stalled run is retained
separately and is not a completed walkthrough.

Two subsequent light-theme IDE processes exercise every question, observation,
Run and assessment. The first includes wrong-answer recovery, starter 0/160 and
a correct unsaved vector solution 160/165/256/14/1. Thirteen edits clear twenty
execution passes. Reopening retains eleven quiz attempts and thirteen unchanged
source hashes, then renews all twenty passes. Assessment counts advance from
2/1/1/1/1 to 3/2/2/2/2. Eleven installed JARs match pinned archives in each process;
accepted logs contain no ERROR/SEVERE. Native close/disposal precedes stopping
only the owned IDE process. Actual clean cleanSandbox runIde tasks precede both
launches; all authoring automation remains in the separate plugin.

The directly edited LaTeX companion supplies the native route, complete source
variations, independent groups and first-mismatch guidance. It occupies section
17.8, lab pages 120–127 in the 207-page proof. All eight pages and adjacent
boundaries were visually reviewed; the three existing capture views on pages
123/124 were also reviewed in grayscale. The exact final PDF passes 1728
PDF/UA-2 rules / 975917 checks with zero failures and embeds Minion Pro, Noto Sans
Math and JetBrains Mono. No new listing or replacement publication photograph
was needed. The main learning proof is unchanged. All 41 manual MARS files are
preserved. Human learner and assistive-technology reviews remain pending. The
curriculum goal remains active; the proposed opening stays deferred. The next
native main unit develops bounded file I/O and PPM images.
