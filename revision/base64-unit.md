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
