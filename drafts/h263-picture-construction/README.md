# Complete QCIF picture construction

Native `picture-loop.tex`, `picture-contract.tex`, `picture-construction.tex` and
`picture-interoperability.tex` are authoritative. Steps 101–110 join your tested
search, prediction, block reconstruction and macroblock syntax implementations.
The complete-picture entry uses an initial I-picture and subsequent INTER-only
P-pictures as an explicit teaching policy. Actual baseline H.263 permits additional
mode choices; they are outside this controller policy, not absent from the standard.

Complete `h263_encode_picture` in your working source. Its 96-byte aligned request
contains twelve native-endian doublewords: current picture, old reconstructed
reference (zero for I), new reconstruction, writer context, workspace, workspace
capacity, picture selector (0 I/1 P), temporal reference, quantizer, arithmetic mode,
GFID and reserved zero. Current, reference and new reconstruction use tight planar
QCIF Y/Cb/Cr, 38016 bytes each. The aligned writer context starts with used count
zero and capacity at most one MiB. A sixteen-byte-aligned workspace supplies at
least 169088 bytes: 131072 bytes of encoded staging followed by 38016 reconstructed
samples. Only that prefix is used. Supply aligned SP and 65536 private stack bytes.

All required extents are mapped and non-wrapping. Both output arenas, request,
context, workspace and private stack are disjoint from each other and the inputs.
Read-only current/reference may overlap. Validate full-width controls, record
alignment and extents before picture/workspace accesses. Preserve inputs, request,
context pointer/capacity, saved GPRs and incoming SP. Vector registers are caller-saved.

Use raster macroblock order, complete old reconstructed references, radius-two
full-pixel search and the existing six-block prediction/reconstruction contracts.
Emit nonempty, byte-aligned GOB headers for rows one through eight, with no group-zero
header. This makes each row's initial vector predictor zero and later predictors
the preceding actual vector. Use half-sample units in macroblock syntax, doubling
the selected full-pixel displacement. Keep one quantizer throughout the picture.
GFID is supplied by the clip loop: same across GOBs, unchanged across unchanged
PTYPE and changed across a PTYPE change. A first I followed by P pictures can use
zero then one for all later pictures. Do not alternate it on every picture.

Stage all output. The supported picture fits within 105070 bytes, so the 131072-byte
private bit arena is sufficient. Explicitly write every published bit, including
final zero alignment; clearing the entire arena first is unnecessary. On success,
copy exactly the staged stream bytes and all reconstructed samples, then commit
the caller used count. Return status zero/byte count. Invalid metadata returns
one/zero, insufficient chosen-picture capacity two/zero and child failure three/zero.
Every failure retains both caller outputs and context; workspace may change after
valid metadata. The supplied entry returns development status four.

```sh
python3 drafts/h263-picture-construction/compose.py \
  --source my-picture.asm --search my-search.asm \
  --prediction my-macroblock-prediction.asm --block my-block.asm \
  --syntax my-macroblock-syntax.asm --output joined-picture.asm
python3 drafts/h263-picture-construction/prepare.py \
  --source joined-picture.asm --picture 1 --tr 6 --gfid 1 \
  --output p-picture.asm
python3 drafts/h263-picture-construction/grade.py \
  p-picture.asm --java /path/to/java --cli /path/to/mars-cli.jar \
  --report picture-results.json
```

Composition retains your dependency instructions/data. Preparation changes only
constant sample arrays and controls. Both require a new output path. The 54 public
cases independently decode all headers and macroblocks, repeat bounded motion
search and check selected levels and accurate reconstruction against floating
models. They check byte orders, state, guards and rejection, accepting alternative
valid entropy codes, skip selection, private frames and accurate transform rounding.
The ordinary flat I-picture prints 696; a flat P-picture using skips prints 48.
At `picture_caller_return`, before its first store, outputs/context/returns are
complete and named status/byte fields still hold their previous values.

`model.py` inspects every macroblock in a complete explicit-row-GOB clip, validates
motion geometry and GFID history, and rejects truncated/trailing syntax.
`check_stream.py` additionally requires FFmpeg for strict independent decoding,
checks picture counts and temporal references, and reports source quality and
local-decoder drift. It requires a new report path. The current clip gate requires
at least 35 dB PSNR per non-identical plane/picture and at most sixteen sample values
of decoder/local difference. These tolerance limits supplement the standalone
transform and inverse accuracy gates; they do not prescribe private encoder bits.

Two actual eight-picture MIPS encoders produce 2212 and 5101 bytes and independently
decode, with maximum local difference two. The reference executes 93429640 guest
instructions; one fresh CLI observation takes about 6.7 seconds including startup,
assembly, guest work and file I/O. This supports retaining the proposed eight-picture
fixture; it is not isolated core throughput or an IntelliJ launch measurement.

The student clip-driver/file-failure construction, final chapter distributions and
actual IDE figures remain unfinished. Screenshot automation belongs to the separate
authoring plugin declaring a MARS dependency. Independent accessibility review and
later substantive editing remain separate.
