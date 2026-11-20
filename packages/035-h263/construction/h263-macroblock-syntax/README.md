# Complete restricted-baseline macroblock syntax

The authoritative native composition is in `macroblock-syntax.tex`,
`macroblock-syntax-contract.tex` and `macroblock-syntax-construction.tex`, steps
91–100. This entry emits one actual baseline macroblock, not a complete stream.
The numeric MCBPC, CBPY and MVD tables retain official-source provenance in
`codes.json`. They contain data, not a target emitter implementation.

Complete `h263_emit_macroblock` in a copy of `main.asm`. Carry in your completed
coefficient emitter, its data and bit writer. The optional helper accepts the
previous working format; a different source organization may be composed manually:

```sh
python3 drafts/h263-macroblock-syntax/compose.py \
  --source my-macroblock.asm --coefficients my-coefficients.asm \
  --output joined-macroblock.asm
python3 drafts/h263-macroblock-syntax/prepare.py \
  --source joined-macroblock.asm --picture 1 --intra 0 --mask 33 \
  --mx -4 --my 6 --px 3 --py -2 --output moving-macroblock.asm
python3 drafts/h263-macroblock-syntax/grade.py \
  moving-macroblock.asm --java /path/to/java --cli /path/to/mars-cli.jar \
  --report macroblock-results.json
```

Both composition and preparation require a new output path. Preparation changes
supplied controls and levels while preserving instructions and numeric tables.
Its ordinary caller owns a 1152-byte output buffer; the entry contract admits
capacity through one MiB. Reports also require a new path.

The eight-byte-aligned request has ten native-endian doublewords: six-block
level pointer, writer-context pointer, picture selector (0 I/1 P), INTRA selector
(0 INTER/1 INTRA), selected horizontal/vertical motion, predictor horizontal/
vertical motion, allow-skip selector and reserved zero. The six tight 128-byte
natural-order blocks are Y top-left, top-right, bottom-left, bottom-right, Cb, Cr.
Each contains 64 signed halfwords. Ordinary levels are [-127,127]; INTRA index
zero is instead DC code 1..255 excluding 128. Motion fields use signed half-sample
units [-32,31], so full-pixel search results must be multiplied by two. INTRA
requires zero motion and predictor fields. INTER is invalid in an I-picture.

The context is the earlier aligned 24-byte pointer/capacity/used-count record.
All extents must be mapped, disjoint and non-wrapping. Preserve request, levels,
context pointer/capacity, saved GPRs and incoming SP; supply aligned SP and enough
private stack. Vector registers are caller-saved. Validate metadata before level
reads and all 384 level domains before invoking syntax children. Build the whole
payload privately, then admit its chosen length before any caller-buffer access.
Only successful output may change touched bytes and the used count. Retain all
prefix/suffix bits outside the appended payload.

Return status zero/appended bit count on success, one/zero for invalid metadata
or levels, two/zero for insufficient chosen-plan capacity, three/zero on child
failure. The starter returns development status four. Every failure leaves the
caller bytes and context intact and performs no caller-buffer access. Level
validation reads are permitted after valid metadata. A child may modify private
storage before failing. An INTER macroblock has at most 8487 payload bits here;
a 1152-byte private arena accommodates any initial bit offset.

INTRA presence flags describe AC only, but all six DC bytes remain mandatory.
INTER CBPY uses the complement of the direct four-bit Y pattern. Encode normalized
horizontal then vertical motion differences. A permitted skip means actual zero
motion and no residual, even with a nonzero predictor. Coded zero-motion output
is also accepted. The controller supplies a valid neighbour predictor; this entry
owns no reference picture or motion-history arrays.

At `syntax_caller_return`, before its first instruction, output/context/return
registers are complete and named status/bit fields still have their old values.
The default I-INTRA DC-only case appends 53 bits: five header bits and six DC
bytes. At the initial cursor three, it finishes on a byte boundary and prints 53.
A permitted P skip can use one bit; equivalent coded output may be longer.

The 116 public cases independently decode type, flags, recovered motion and all
six selected level arrays, then check exact consumption, saved state, complete
admission, guards and access bounds in both native byte orders. They accept valid
compact/escape choices, permitted skip/coded alternatives and private layouts.
Capacity depends on the complete chosen representation; enough worst-case space
must succeed, while a smaller buffer may admit only some legal representations.
`model.py` provides a host bit-string encoder/decoder, including prefix parsing
for repeated macroblocks; it does not generate a target solution.

Picture-level reconstruction ownership, GOB boundaries, file output, independent
whole-stream quality/interoperability/performance, final chapter distributions
and actual IDE figures remain subsequent work. Screenshot automation belongs in
the separate authoring plugin declaring a dependency on MARS.
