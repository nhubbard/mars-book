# H.263 chapter contract and implementation boundary

Chapters 33 and 34 have their initial text, student/instructor distributions and
all five PNG/six video IDE figure plans completed before this work begins. Chapter
35 remains an initial draft in progress. Human substantive editing is a separate
later goal.

The target is a restricted baseline QCIF H.263 **encoder**, with Python-generated
raw digital samples as input and an interoperable elementary stream as output.
The selected picture-level syntax uses standard QCIF, I and P pictures and the
ordinary baseline clock. Split-screen/camera/freeze indicators, unrestricted
motion vectors, arithmetic coding, advanced prediction, PB pictures, multipoint
and extra insertion information are disabled in the initial profile. Extended
PTYPE/custom formats and optional annex modes are outside it. Macroblock and
coefficient emission, local reconstruction accuracy, mode selection and final
quality acceptance still require implementation and independent checks; a
picture-header inspector is not the complete conformance gate.

The input contract is in `drafts/h263-fixtures/README.md`: 176-by-144 Y and 88-by-72
Cb/Cr planes, 8-bit planar Y/Cb/Cr order, tight rows, 38016 bytes/picture, nominal
limited sample ranges and 12:11 sample aspect ratio. Timing is exact integer
baseline clock steps. The candidate default is eight pictures at 5000/1001 Hz,
1.6016 s including the final picture hold. Input limits remain at most 30 pictures/s
and ten seconds. That default must be revisited after whole-encoder measurement;
current measurements cover SAD, search, prediction and transform kernels, not a
complete encoder.

The first callable contract is exact scalar/MSA 16-by-16 SAD with independent
16..4096-byte strides, unaligned byte pointers, valid read-only aliases, complete
64-bit address arithmetic and no reads outside active samples. Guided instructions
are native LaTeX. The preparation helper changes supplied data controls while
retaining instruction text. Free-writing acceptance is based on results, register
preservation and access bounds, not private labels or exact instruction counts.

The host preflight has encoded the generated input using FFmpeg 9.0.2's baseline
H.263 encoder, decoded it using a separate strict invocation, checked all eight
QCIF pictures and inspected temporal references 0,6,12,18,24,30,36,42. The stream
was 2194 bytes. Per-picture plane errors and exact tool provenance are retained
privately. These results prove the input/tool path, not a student MIPS encoder.

Two private SAD implementations pass 104 cases each, including maximum unsigned
sample differences, varied strides, both byte orders, aliases, wide addresses
and rejected argument windows. Four assembled wrong kernels are rejected, including a pointer-only alias shortcut
that overlooks different row strides.
Checked function-entry-to-return counts are 3245 scalar, 207 reference MSA and
239 alternative MSA instructions. These are observations, not grading thresholds,
CPU timings or whole-encoder runtime measurements.

A repeated raw picture is not automatically equal to the preceding reconstructed
reference. The first SAD exercise uses raw windows to isolate arithmetic; the
encoder must later supply its locally reconstructed reference to the same kernel.
Do not require a skipped macroblock merely because consecutive input hashes agree.
The host preflight retains an explicit observation of this distinction for the
repeated-picture case.

Public SAD feedback now supplies 32 completed-entry cases with separate high-address
arenas, a 16-KiB private stack and protected result reports. Bounded full-pixel motion search now has a callable request/result contract, ten
construction observations/quizzes and 56 public cases. Two traversals pass 160
private cases each; three implementations pass public feedback, and five wrong
search bodies plus the unfinished starter are rejected. The geometry clips full
16-by-16 windows to the picture, minimizes SAD and resolves ties by centre first,
then increasing dy/dx. The worked raw result is (-2,-2), SAD 2976, 25 candidates.
Pixel accesses, input/request/result guards, full-width admission and saved state
are checked in both byte orders. Private references measure 82312 scalar versus
6412 MSA instructions for the named interior shifted-pattern search; these are
search entry-to-return observations, not a completed encoder benchmark.

Baseline eight-by-eight prediction and signed residuals now cover all four
phases, exact default rounding and bounded halo reads. Full-pixel luma motion
may need half-sample chroma prediction; negative odd components require a floored
origin plus phase one. Public feedback has 96 cases. Two primary bodies pass 184
private cases each; an additional coalesced-read version passes all 184 and
three bodies pass public feedback. Thirty host-positioned real windows cover
six blocks and five vectors, with 120 cases per primary implementation. These
positioned-window checks cover the block kernel; the subsequent target-side
coordination checks below cover macroblock origins and publication. Local
reconstruction remains unfinished.
Five wrong kernels and the unfinished starter are rejected. Per-sample signed
residuals and the exact pre-quantization reconstruction identity are checked.

Target-side six-block prediction now derives all Y/Cb/Cr origins from complete
plane pointers and six independent strides. It stages six 192-byte records and
publishes 1152 bytes only after every child succeeds. All plane extents are
validated before pixels; failed child status two retains caller output. The
ordinary program entry aligns the stack before passing aligned private records.
Two bodies, including reverse block order, pass 212 private cases each; three
layouts pass 64 public cases each. Eight implementation-specific probes verify
later-child failure after private writes. Five wrong bodies and the unfinished
starter are rejected. Twenty ordinary completed callers exercise five signed
vectors in both modes. The default returns total 2976; odd (-1,-1) returns 5788
and zero returns 9072. These are prediction/residual results, not encoded output.

Forward/inverse transforms now use a signed halfword interface with bounded
coefficient/residual output, private staging and an independent floating reference.
Two accurate precisions are accepted: reference scale 65536 and alternative
32768. Each passes 156 private finite cases; three layouts pass 96 public cases.
The full target inverse accuracy gate passes all six Annex A cohorts for each
body/mode/byte-order combination: 480000 blocks and 48 exact-zero calls. All five
error limits pass; a wrong signed bias fails every full cohort. This proves the
tested inverses, not complete-stream conformance. Sixteen completed ordinary
callers cover both directions, modes and flat/worked inputs.

Quantization now has a verified callable exercise: a stated signed-magnitude
forward policy capped at 127, separate INTRA DC mapping and exact baseline
reconstruction/clipping. Both traversal orders pass 2044 private cases across all
31 parameters; three layouts pass 204 public cases. Five wrong bodies and the
unfinished starter are rejected. Eight completed ordinary callers and all 276
public tooling tests pass. The native chapter has 62 pages and sixty construction
observations/quizzes. Exact exercise levels do not constrain legal full-encoder
choices. A complete callable block path now joins those entries and publishes selected
levels, reconstructed coefficients, signed inverse output and clipped pixels.
Two accurate precisions/traversals pass 368 private cases; three layouts pass
144 public cases. Eight named repeated-call cases use actual prior output as
prediction, and 24 child-failure probes retain caller output. Eight ordinary
composed callers and all 286 public tooling tests pass. The chapter now has
71 pages and seventy construction observations/quizzes. Bounded bit output now
adds a verified context/append contract, exact MSB-first replacement, empty writes,
capacity preflight and explicit alignment, bringing the chapter to 79 pages and
eighty observations/quizzes. Both schedules pass 1632 private cases; three layouts
pass 160 public cases. Four repeated-chain cases and all 294 public tooling tests
pass. A named unaligned 32-bit append costs 656 per-bit versus 189 byte-fragment
instructions, including admission/commit; this is not whole-encoder timing.
Four target header-packing cases agree with the independent header inspector,
without claiming complete-stream conformance. Picture ownership, coefficient and
baseline picture/macroblock syntax remain next. Complete block coefficient emission
now includes all 102 compact codes, baseline escapes, natural-to-zigzag addressing,
INTRA DC and whole-block private publication. Two legal coding choices pass 916
private cases each; three layouts pass 100 public cases. Four retained-context
cases, twelve late child failures, sixteen ordinary callers and all 304 public
tooling tests pass. Grading independently recovers levels instead of demanding
private bytes. The chapter has 89 pages and ninety construction observations/quizzes.
Controlled instruction work separates coding from bit packing; smaller payloads
can cost extra sparse lookup work. Macroblock/motion/type syntax, picture ownership,
complete stream/output and their quality/performance checks remain unfinished. Keep the
closed reconstruction loop explicit: later prediction uses reconstructed samples.
Final grading needs an independent decoder, profile validation, meaningful quality
checks, failure/publication contracts and equivalent valid encoders. Do not accept
an all-intra shortcut as the completed inter-picture project, substitute the old
teaching format, or require the host encoder's exact bit choices.

Sources consulted:

- [ITU-T H.263 (02/1998)](https://www.itu.int/rec/dologin_pub.asp?id=T-REC-H.263-199802-S!!PDF-E&lang=e&type=items), clauses 4.1 and 5.1 for initial format/timing/header decisions; later codec stages must consult their own normative clauses.
- [FFmpeg rawvideo documentation](https://ffmpeg.org/ffmpeg-formats.html#rawvideo), for explicit host input parameters.
- [FFmpeg's H.263 encoder source](https://raw.githubusercontent.com/FFmpeg/FFmpeg/master/libavcodec/ituh263enc.c), consulted as an independent implementation reference; no codec implementation copied into the book.
- [MIPS64 SIMD Architecture Module, revision 1.12](https://s3-eu-west-1.amazonaws.com/downloads-mips/documents/MD00868-1D-MSA64-AFP-01.12.pdf), for unsigned vector differences, widening, transfers and byte loads; compare with the pinned MARS implementations and execution checks.

Native book includes remain authoritative. No build regenerates chapter prose
from metadata. Future screenshots must be real light-theme IDE captures by the
separate authoring plugin depending on MARS, with retained receipts and native
LaTeX annotations. None is claimed for chapter 35 yet.


## Complete restricted-baseline H.263 macroblocks

Steps 91–100 now compose macroblock type, presence flags, normalized half-sample
motion differences and all required block payloads. Native chapter 35 has 98 pages,
one hundred observations/quizzes, 31 tagged tables/116 header cells and four
described native figures. The new workflow carries the student's completed
coefficient emitter and writer forward without publishing target solutions.

Two instructor implementations pass 8638 private cases each, including all 4096
selected/predictor component pairs in both byte orders. Three layouts pass 116
public cases. Eight endpoint cases, four retained-context chains and twenty
forced child-failure cases pass. Five incorrect bodies and the unfinished entry
are rejected. Fourteen ordinary prepared/composed callers pass expected execution;
all 316 public tooling tests pass. Numeric facts for 92 MCBPC/CBPY/MVD entries
match an independent codec's tables. Semantic grading accepts legal differences
in compact/escape coding and skip selection.

Picture ownership/publication, file output, independent whole-stream decoding,
quality and performance, final chapter archives and actual IDE figures remain
unfinished. MARS and authoring-plugin source are unchanged in this increment.
The separate authoring plugin declares its MARS dependency; no GUI launch or
capture is claimed. Independent accessibility review and later substantive editing
remain separate.


## Complete H.263 picture controller and actual MIPS stream proof

Native steps 101–110 join the tested kernels into raster-ordered QCIF pictures,
with explicit row GOB headers, reconstructed references, half-sample motion syntax,
private complete-picture staging and final output publication. The chapter now has
107 pages and 110 observations/quizzes. A 54-case public checker independently
decodes every macroblock, repeats the search and checks selected levels and accurate
reconstruction, accepting equivalent entropy codes, skip choices and transforms.

Two actual MIPS encoders independently decode all eight Python-generated pictures.
The reference/alternative produce 2212/5101 bytes and execute 93429640/95888083 guest
instructions. Maximum local-versus-independent-decoder difference is two sample
values for each. One initial reference fresh CLI run takes about 6.7 seconds,
including JVM, assembly, simulation and real file I/O. The eight-picture default
is retained; these are whole-process observations, not isolated core throughput.
All 328 public tooling tests pass. Instructor implementations, late-child probes
and complete real-file driver evidence remain private.

Student clip-driver construction, file-failure grading, chapter archives and
actual IDE figures remain unfinished. No MARS or authoring-plugin source changes
or graphical IDE launches occur in this increment. The separate authoring plugin
continues to declare its MARS dependency. Independent accessibility review and
later substantive editing remain open.


## Complete H.263 clip driver and file-error grading

Native steps 111–120 complete the student-facing clip loop: bounded short reads,
guarded short writes, accepted-reference publication, exact EOF and owned-descriptor
cleanup. The new exclusive-directory workflow generates raw samples, runs the
actual student MIPS source and independently decodes its output. The chapter has
117 pages and 120 observations/quizzes. All 340 public tooling tests pass.

Both drivers pass 74 public cases; an independent layout and focused private probes
exercise real late child failures and incorrect I/O/publication behavior. Targeted
write failures in either output use a backward-compatible grader addition in MARS
commit 6c66f101, tested by all 122 CLI Jupiter tests. This is a grader capability;
no simulator arithmetic or instruction semantics changed. The commit is local and
is not pushed as part of the book publication.

Actual eight-picture encoders produce 2212/5101 bytes and execute
95583213/98072067 guest instructions, including guarded I/O and cleanup. Both
independently decode with maximum local difference two; whole fresh CLI observations
are about 7–8 seconds. A 44-picture 8.8088-second clip also independently decodes,
including temporal-reference wraparound. The eight-picture default is retained.
Final chapter distributions and actual IDE figures remain unfinished. Capture
implementation stays in the separate authoring plugin depending on MARS; no plugin
source or GUI launch changes occur here. Independent accessibility review and later
substantive editing remain separate.


## H.263 chapter distributions

Package 035-h263 is registered alongside the preceding 34 projects. It exports
all 120 native construction steps, twelve retained working stages, thirteen
callable entries, a free-writing shell and an actual generated-input run workflow.
The student archive contains 1196 public cases and no completed target assembly
or answer keys. The instructor distribution adds 31 public/private domains with
15636 cases per implementation, 120 explanations and 240 progressive hints.

Relocated archives verify 32468 positive cases across reference, alternative and
independent frames, reject both unfinished starters, and independently decode
both actual eight-picture runs. Thirty-two extracted helper entrypoints resolve
their local imports. The full inverse-accuracy runner and exact prior reference
body/constants/core provenance remain separate mandatory evidence; new submitted
transforms must run the full gate. All 355 public Python tests pass.

The course runner supports a matching harness per check, retaining the simulator's
existing assembly-work and wide-data limits. Native export preserves the directly
edited book source and cross-reference labels, refuses to replace edited exports,
and retains reviewed capture state. The chapter is 118 pages, its assignment two
pages, and the complete manuscript 1705 pages. The existing Minion Pro/Noto Sans
Math/IntelliJ-exported code setup is unchanged.

Six H.263 IDE figures remain explicitly planned; capture tooling stays in the
separate authoring plugin depending on MARS. No graphical IDE launch or main-plugin
source change occurred in this packaging step. Independent accessibility review
and later substantive editing remain open. The required grader revision 6c66f101
is still a local MARS commit and is not pushed by book publication.
