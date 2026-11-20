# Complete the block transform and local reconstruction path

The native chapter includes `block-reconstruction.tex` and `block-construction.tex`
are authoritative. This is not a bitstream encoder or completed chapter distribution.
Implement `h263_encode_block8` in a copy of `main.asm`. Replace its dependency stubs
with your own tested transform tables, transform entry/helpers and quantizer entry.
The helper can carry in those completed prior working formats without changing
the block entry you have written:

```sh
python3 drafts/h263-reconstruction-construction/compose.py \
  --source my-block.asm --transform my-transform.asm \
  --quantizer my-quantizer.asm --output joined-block.asm
python3 drafts/h263-reconstruction-construction/prepare.py \
  --source joined-block.asm --intra 0 --mode 1 --current 130 \
  --prediction 128 --quant 8 --output block-observation.asm
python3 drafts/h263-reconstruction-construction/grade.py \
  block-observation.asm --java /path/to/java --cli /path/to/mars-cli.jar \
  --report block-results.json
```

Composition accepts one text section per provided working source and keeps each
dependency's data plus its entry and following helpers. Put required helpers after
the entry, as in the prior working format. Custom layouts can be composed manually;
final grading does not require this helper or private labels. The helper does not
supply solutions. It refuses existing output and accepts the original unjoined
block working format, retaining the block caller/entry instructions before its
explicit dependency marker. Preparation then changes data controls only and also
requires a new path. Keep prior entry/interface and inverse accuracy checks.

The 64-byte request holds eight native-endian doublewords: current pointer/stride,
prediction pointer/stride, Q, INTRA, mode and reserved zero. Active strides are
8..4096, independently. Input pointers can be unaligned; read only eight active
bytes per row. INTRA requires zero prediction fields and performs no prediction
reads. Mapped/non-wrapping extents are caller obligations. Read-only inputs may
alias. Request is aligned eight; output is aligned sixteen and disjoint from
inputs/request. Supply aligned SP and private space; preserve saved GPRs/SP and
input/request bytes. Vector registers are caller-saved.

Output is 448 bytes: levels at 0 (128 bytes), reconstructed coefficients at 128
(128), signed inverse values at 256 (128), pixels at 384 (64). Halfwords remain
in natural order and native byte order. Successful status is zero; count is the
ordinary nonzero level count, excluding INTRA DC. Invalid metadata returns one/
zero before input reads. Child failure returns two/zero after permitted input/
private work, with no caller-result writes. Stage every output before publication.

The supplied bytes use signed decimal spellings (-126 for bit pattern 130,
-128 for 128) to avoid assembler narrowing warnings; unsigned loads recover the
intended sample values. Preparation preserves these byte bits.

The default completed caller returns count one, first level one, reconstructed
DC 23, inverse residual three and first pixel 131. INTRA current 128 instead gives
code 255, coefficient 1024, pixels 128 and ordinary count zero. At
`block_caller_return` before its first instruction, output/return registers are
complete, and the named status/count fields still hold their previous values.
The default prints 131 and exits zero. Unfinished parent status four and failed
child status two have distinct messages and exit one.

The 144 public cases use independent floating transforms and permit prior one-unit
accuracy. They check that levels could come from an accurate forward transform,
that reconstructed coefficients exactly follow the actual levels, that inverse
values are accurate for those coefficients, and that pixels exactly equal clipped
sums of those inverse values and predictions. Saved state, all guards and full-width
admission are checked. This supplements standalone contracts and full inverse
accuracy, and does not prove complete-stream quality or MSA use solely from outputs.
Private fault injection checks child failure after private writes separately.

`reference.py` gives the independent floating worked trace. At Q=8, current 130
predicted from 128 reconstructs 131; a second current 132 predicted from that
reconstruction retains 131. Whole-picture ownership, six-block coding, independent
stream decoding, output failures, quality/performance checks and actual figures
remain subsequent work. All screenshot automation remains in the separate authoring
plugin depending on MARS.
