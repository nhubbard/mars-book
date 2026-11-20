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
current measurements cover only the first SAD kernel.

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
arenas, a 16-KiB private stack and protected result reports. Cumulative observations
and bounded motion search are the next construction work, followed by prediction, transform/quantization, local
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
