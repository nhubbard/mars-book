# Reading nested data

Chapter 21 revises original project `032-cbor-telemetry`, following bounded parsers,
file ownership, checksums and the QOI state machine. Its historical main treatment occupies pages 133–139 of the 139-page proof.
The current lab proof has 214 pages: the revised CBOR core occupies 150–156,
with the expression and bytecode companions continuing through 170. Native
LaTeX remains the publication authority. Older test and page counts below are
retained as historical qualification, not newly rerun claims.

## Prerequisites and learning outcomes

The reader needs unsigned bounds, byte order, bit fields, nested calls, full-width
saved state and validation before publication. The chapter develops five outcomes:

- Decode a head without confusing its extent with a payload or container extent.
  The width table and full-counter loop connect concrete bytes to argument bits.
- Retain a parent's cursor, budget, outstanding work and identity while visiting
  children. A continuation table follows the actual nested optional value.
- Apply inherited depth and duplicate-key identity separately at each container.
  Empty-container boundaries and differently spelled repeated keys supply diagnosis.
- Separate generic syntax, telemetry schema and deterministic application projection.
  Exact local fields, late truncation and the 52-to-37-byte example expose the boundaries.
- Recognize a complete sequence independently, preserving inherited depth across
  siblings and clearing the result after an invalid suffix.

## Executable material and bounded evidence

`labs/files/cbor-telemetry.asm` supplies the complete five-entry codec and caller.
Its compact Release 6 branches and calls retain explicit nops and useful observation
labels. Mode five joins the memory pipeline; these entries do not perform file I/O.
A fresh encode-only caller still needs valid record input. The source includes the
52-byte trace, 37-byte output capacity and sentinel-backed caller arenas.

Reference, alternative and above-32-bit-address implementations each pass 775 original
public cases and 4084 private cases: 14577 total successful executions. The alternative
uses an explicit container stack and checked slices rather than the worked recursive
walker and private candidate publication. Seventeen further written cases check the
specific head, nested-map, depth, record, late-error and exact-capacity observations.

`labs/files/cbor-sequence.asm` intentionally leaves only the independent sequence
entry unfinished. Its 203 public cases cover empty selection, inherited depth,
separate maps, malformed suffixes, truncated heads, exact selected extent and unsigned
early errors. A targeted sibling case rejects accidental reuse of the helper's a2
result as depth. Remaining-count and end-address implementations, plus a wide-address
variant, each pass the public checks and 8192 expanded cases. That expanded domain
is all 256 one-byte argument values across 32 source offsets in a fixed mixed-item
sequence; it is not all possible nested structures or pointer combinations. The
untouched starter fails all 203 cases. Independent solutions remain private.

Twenty-six assembled semantic faults are rejected with their matching harnesses:
eight head/walker faults, twelve telemetry faults and six sequence faults. The exact
unmodified prototype baselines pass 1648 and 2436 cases respectively. An earlier
prototype/check mismatch is retained as preparation history and excluded from this
claim. A Kotlin method-size preparation failure is also historical; registering the
expanded cases in loops preserves the complete domain without exceeding a JVM method.

Twelve actual MARS-produced encodings, covering trace, maximum and exact-budget input
under two strategies and two address layouts, are independently decoded with pinned
cbor2 6.3.0 and compared with the preferred required-field projection. Those are
serialized simulator outputs inspected by a host library, not a simulator file-I/O
pipeline or a claim that a generic library defines this local bounded profile.
The wire conventions are based on RFC 8949; the local limits, small-key policy,
telemetry schema and application projection are explicitly teaching choices.

## Figures, publication and remaining work

Four actual IntelliJ Light Debug captures show native counter/identifier storage,
signed readings, an unchanged rejected record prefix and the 37-byte projection.
The separate authoring plugin ran after the actual clean, cleanSandbox and runIde
tasks. Exact observed rows agree with CLI boundary probes. Screenshot pixels remain
unchanged; titles, crops, outlines and semantic descriptions are native LaTeX.
Full-record preservation and access guarantees are established separately from the
pictured prefix. The authoring IDE was closed after its completed four-figure batch.

Two actual IntelliJ highlighter exports extend the previous 822 unchanged records
to 824. The authoring plugin passes 104 Jupiter tests; public tooling passes 405 tests.
The original 56-step package validates and exports successfully. Final chapter pages
were inspected in color, with representative code, tables and all screenshot types
also inspected in grayscale. Both exact final PDFs pass automated PDF/UA-2 validation.
Author, learner and assistive-technology reviews remain pending.

The earlier unit used ordinary Run and Debug; its native guide is now complete
as recorded below. Original projects 017 and 018 retain their expression/bytecode
companion roles. Their written supplements remain included, and their native
guides remain separate unfinished work. The next main
chapter develops word operations through ChaCha20. The complete curriculum and
production instructional-system goal remains active. The deferred opening orientation
has not been added. No MARS source change was required in this unit.

## Native grading foundation, 23 November 2026

The original 203 sequence cases now have an authored native data equivalent,
retaining every four-million-instruction allowance, full seeded state, exact
caller snapshots, permission window and 500,000-notice access history. Actual
original-grader exports match all fields in the remaining-count, end-address
and wide-address variants. Two additional cases verify real caller termination
and repeated calls within one session; they reject deliberate defects that the
original fresh-call grid accepts. The corpus has 205 cases / 208 calls in 104
bounded profiles, without a production API change. See
[the native grading contract](../docs/native-cbor-checks.md).

## Native instructional unit, 23 November 2026

`revision/lessons/032-cbor-telemetry` now supplies a versioned student archive,
12 inline predictions, 11 Debug activities with 31 ordered boundaries, one
ordinary Run and seven independent assessment groups. Twelve worked variants
change only caller/data selections; executable codec instructions remain exact.
The guide follows head width, full-width accumulation, recursive parent state,
depth and map identity, complete record acceptance and exact output capacity.
The independent sequence body remains unfinished. All 104 grading profiles
appear exactly once; all 205 cases and 208 calls remain available.

Twelve real simulator probe processes verify all 31 boundaries, caller exit,
complete source/record/output snapshots, untouched tails and zero file I/O.
The 555 public tooling tests, three focused native import/profile/PSI Jupiter
tests and 135 authoring-plugin Jupiter tests pass. Production MARS code is
unchanged from the prior QOI run of 255 CLI and 1277 plugin tests; that full
regression was not rerun for this content-only unit. The paired packaged API
verification passes with no internal API dependency.

The separate authoring plugin exercised the actual native first-use flow and
a second fresh IDE process, each launched through `clean cleanSandbox runIde`.
They pass 51 and 39 observations respectively. Thirteen quiz attempts (including
one deliberate incorrect answer), 12 edited source digests and assessment
attempt counts survive reopening. Source edits invalidate all 19 execution
checks; actual execution renews them. All 11 installed plugin JARs match their
pinned archives in both launches. Both IDE logs are free of ERROR/SEVERE entries;
owned test IDEs were stopped after native project disposal. The private evidence
retains 56 first-use and 40 resume captures with verified IntelliJ Light metadata.
These automated walkthroughs do not substitute for learner review.

The directly edited native lab now explains the transitions between these
observations, including a parent/child continuation table and the distinction
between record publication and output publication. Its four existing genuine
light-theme figures retain native LaTeX annotation. The exact 214-page proof
was inspected across the revised core and companion transitions, with a table
and screenshot page also inspected in grayscale. Embedded Minion Pro, Noto Sans
Math and JetBrains Mono are verified. Automated PDF/UA-2 passes 1728 rules and
1,010,519 checks with no failures. Human and assistive-technology review remain
pending. A structural audit confirms 27 chapters, all 35 original projects and
all 10 companion includes; this establishes coverage, not teaching quality.

Private evidence is retained under `verification/restructuring/native-cbor-unit-01`
and `native-cbor-ide-01` in the instructor repository. The exact student archive
is also the byte-identical native reader fixture. Chapter 22 word transformations
and ChaCha20 is next; the full curriculum goal remains active.
