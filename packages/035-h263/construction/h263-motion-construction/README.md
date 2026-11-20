# Bounded full-pixel motion search

This stage builds on the completed scalar/MSA SAD entries. The native chapter in
`includes/chapters/035-h263` explains candidate geometry, ties, the calling
interface and steps 11–20. Copy your completed SAD bodies into your new working
source, retaining a single ordinary main entry. The supplied entries return
development status four; no completed search or SAD solution is included here.

`h263_search16` takes an eight-byte-aligned request pointer in a0 and an
eight-byte-aligned 24-byte output pointer in a1. The request has eight native-endian
unsigned 64-bit fields at successive eight-byte offsets:

| Offset | Meaning | Accepted values |
| --- | --- | --- |
| 0 | Current block pointer | Mapped active 16-by-16 samples |
| 8 | Reference plane pointer | Mapped 176-by-144 active samples |
| 16 | Current byte stride | 16..4096 |
| 24 | Reference byte stride | 176..4096 |
| 32 | Current picture x | 0..160 |
| 40 | Current picture y | 0..128 |
| 48 | Full-pixel radius | 0..2 |
| 56 | Comparison mode | 0 scalar, 1 MSA |

The current pointer already addresses the block. The reference pointer addresses
the whole plane. Candidate `(dx,dy)` addresses reference `(x+dx,y+dy)`. Intersect
`[-radius,radius]` independently with `[-x,160-x]` and `[-y,128-y]`. Score every
admitted position using exact 16-by-16 SAD. Among minimum scores prefer `(0,0)`,
then increasing dy and dx. An alternative traversal must preserve that result.

On success return v0=0/v1=the unique admitted count. Store signed 64-bit dx/dy at
output offsets 0/8 and unsigned SAD at 16. Invalid arguments return v0=1/v1=0,
leave output unchanged and read no pixels. Validate record alignment and
non-wrapping 64-/24-byte extents before request reads; validate all unsigned fields
and current/reference extents `15*currentStride+16`/`143*referenceStride+176`
before pixels. Both input byte pointers may be unaligned. The caller supplies
mapped records/images and an output area disjoint from request/images; input
images may overlap. Preserve request, images, saved GPRs and SP. Pixel reads must
fit current active rows or the union of admitted reference windows, never padding.

Prepare a new worked file **from your current source**, retaining all instructions:

```sh
python3 drafts/h263-motion-construction/prepare.py --source my-search.asm --mode 1 --output worked-search.asm
```

Mode zero selects scalar comparisons. Preparation installs the current raw block
from frame one at (16,32) and frame zero's reference luma plane, both stride176.
The ordinary caller initializes pointer fields at runtime. It prints `-2 -2 2976`
and exits zero when complete. Its return boundary is `motion_caller_return`:
result bytes are already published, v0=0/v1=25, and the caller has not yet copied
the returned registers to its named status/count fields. Unfinished bodies print
the development message and exit one. Existing prepared output files are refused.

```sh
python3 drafts/h263-motion-construction/grade.py my-search.asm --cli /path/to/mars-cli.jar --report search-results.json
```

The 56 public cases supply separate high-address arenas and 16 KiB of private
stack. They compare mathematical results, bounds, preservation and both byte
orders/modes. No private labels, frame layout or exact instruction count is
required. Feedback reports instruction work; it does not establish that a solution
uses MSA merely because its results agree. Reports require new paths.

This search policy selects full-pixel luma candidates for the restricted encoder.
Motion-vector units, chroma prediction, differential vector syntax and residual
coding remain later work. The first worked example deliberately uses raw windows;
the eventual encoder must search its locally reconstructed reference. No completed
encoder, chapter distribution or IDE screenshot is claimed. The separate authoring
plugin depends on MARS and handles future captures.
