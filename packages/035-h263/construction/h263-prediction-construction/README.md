# Baseline prediction and signed residuals

The native chapter's steps 21–30 explain signed chroma origins, interpolation,
bounded gathers and residual storage. `main.asm` is an ordinary Run/Debug caller
with one unfinished `h263_predict8` entry returning development status four.

The function takes an eight-byte-aligned 64-byte request pointer in a0 and a
sixteen-byte-aligned 192-byte output pointer in a1. Request fields are unsigned,
64-bit and native endian:

| Offset | Field | Accepted values |
| --- | --- | --- |
| 0 | Current block pointer | Mapped active eight-by-eight samples |
| 8 | Reference integer-origin pointer | Mapped active halo |
| 16 | Current byte stride | 8..4096 |
| 24 | Reference byte stride | 8+phaseX..4096 |
| 32 | Horizontal half-sample phase | 0 or 1 |
| 40 | Vertical half-sample phase | 0 or 1 |
| 48 | Mode | 0 scalar, 1 MSA |
| 56 | Reserved | Zero |

For baseline rounding control zero, phases select A, `(A+B+1)/2`, `(A+C+1)/2`
or `(A+B+C+D+2)/4`, with nonnegative integer division. B is right of A, C below
A and D below-right. Round the four-neighbour sum only once. The active reference
footprint is (8+phaseX) columns by (8+phaseY) rows. Do not read padding. A coalesced
read is permitted if every accessed byte is in the active footprint; an arbitrary
sixteen-byte load from a padded eight-byte row is not.

On success return v0=0/v1=the sum of absolute residuals (0..16320). Store 64 tight
predicted bytes at output offsets 0..63 and 64 signed native-endian halfwords at
64..191. Residual equals current minus prediction and must retain its sign.
At each position, prediction plus unquantized residual must recover the current
sample exactly. This arithmetic identity does not make later quantization lossless.

Validate both record alignments and non-wrapping record extents before request
reads. Validate every full-width field and input extents `7*currentStride+8` and
`(7+phaseY)*referenceStride+8+phaseX` before pixel reads. Invalid requests return
v0=1/v1=0 without pixel reads or output writes. Caller supplies mapped records,
active input samples and an output disjoint from request/images. Read-only images
may overlap and use independent strides; input byte pointers may be unaligned.
Preserve saved GPRs, SP, request and all input bytes.

Prepare a new file from **your current source**, preserving all instructions:

```sh
python3 drafts/h263-prediction-construction/prepare.py --source my-prediction.asm --mode 1 --phase-x 1 --phase-y 1 --output worked-prediction.asm
```

The worked current Cb window is at (8,16) in raw picture one; reference is at
(7,15) in raw picture zero. Strides are 88. Phases (1,1) correspond to full-luma
motion (-1,-1), using chroma origin floor(-1/2)=-1 and phase one on each axis.
For (-2,-2), select phases (0,0) at the same reference integer origin. Signed
arithmetic shift supplies the origin; division truncating toward zero is wrong
for negative odd displacements. Final macroblock code must compute these origins
itself and use a locally reconstructed reference.

The completed default worked caller prints `120 8 232`: first prediction, first
signed residual, and residual SAD. Phase (0,0) prints `128 0 0`; (1,0)/(0,1) print
`128 0 112`. It exits zero. Unfinished bodies print a development message and exit
one. At `prediction_caller_return`, before its first instruction, the output record
and return registers are complete, but named status/SAD fields have not been copied.
Existing preparation outputs are refused.

```sh
python3 drafts/h263-prediction-construction/grade.py my-prediction.asm --cli /path/to/mars-cli.jar --report prediction-results.json
```

The 96 public cases check every sample, signed residual, reconstruction identity,
active access, request/input/result guards, saved state, modes and byte orders.
A separate high-address harness provides 16 KiB of private stack. Reports need a
new path. Equivalent layouts and safe coalescing are accepted; no exact instruction
count or private label is required. Results alone do not prove use of MSA.

The complete H.263 encoder, target macroblock orchestration, transform/quantization,
local reconstruction, bitstream output and chapter captures are still unfinished.
The separate authoring plugin depends on MARS and handles future IDE figures.
