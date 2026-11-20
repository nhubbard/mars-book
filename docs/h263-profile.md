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

Quantization is next, followed by local
reconstruction, bit writing and baseline picture/macroblock syntax. Keep the
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
