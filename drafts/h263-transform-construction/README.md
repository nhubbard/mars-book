# Forward and inverse eight-by-eight transforms

Native steps 41–50 explain normalization, signed fixed-point products, transposed
unrounded intermediates, final descaling/clipping and independent inverse accuracy.
The ordinary caller has an unfinished `h263_transform8` returning development four.
Provided halfword/word tables use constants scaled by 65536. Accurate alternatives
may use other tables, precision or factorization; acceptance does not compare
private registers, labels, exact instruction counts or every rounded bit pattern.

Pass an eight-byte-aligned 48-byte request in a0 and sixteen-byte-aligned 128-byte
output in a1. Request fields are native-endian unsigned 64-bit quantities:

| Offset | Field | Contract |
| --- | --- | --- |
| 0 | Input pointer | Two-byte-aligned signed halfwords |
| 8 | Input byte stride | Even, 16..4096 |
| 16 | Direction | 0 forward, 1 inverse |
| 24 | Mode | 0 scalar, 1 MSA arithmetic |
| 32, 40 | Reserved | Both zero |

Supply eight mapped active halfwords in each of eight rows, a valid aligned stack,
and output disjoint from input/request. Minimal input extent is `7*stride+16`.
Validate metadata, alignment and non-wrapping extents before sample reads.
Metadata rejection returns v0=1/v1=0 with no input samples read or output changed.
Then validate signed input ranges: forward -256..255, inverse -2048..2047.
Out-of-domain samples return v0=2/v1=0, allowing validation reads but no output
writes. Preserve saved GPRs, SP, request and all input bytes.

Success returns v0=0/v1=64 and writes 64 tight native-endian signed halfwords.
Forward order is F[v,u], inverse spatial order f[y,x]. Forward output is clipped
-2048..2047 and inverse -256..255. Preserve negative inverse residuals for later
prediction addition and final pixel clipping. Require one-unit accuracy against
the independent floating reference in finite checks and exact zero outputs for
zero input. Inverse acceptance also requires the separate full Annex A gate;
finite cases alone do not certify it.

```sh
python3 drafts/h263-transform-construction/prepare.py --source my-transform.asm --mode 1 --direction 0 --pattern flat --output forward-flat.asm
```

Direction one selects inverse. Patterns are `flat` or `worked`. Flat forward is
64 samples of 128, producing DC 1024 and zero AC; flat inverse is DC 1024 alone,
producing 128 throughout. Worked forward installs actual raw-picture luma
residuals at current (16,32) versus reference (14,30); its first coefficient is
approximately -192. Worked inverse combines DC 1024 and a few AC terms and begins
near 137. The helper preserves all instruction bytes and provided/edited tables,
and refuses existing output paths. The ordinary entry aligns its stack before
calling. At `transform_caller_return`, output and return registers are complete;
named status/count fields have not yet received the caller's copies.

```sh
python3 drafts/h263-transform-construction/grade.py my-transform.asm --cli /path/to/mars-cli.jar --report transform-results.json
```

The 96 public finite cases use an independent floating matrix, both modes and byte
orders, signed extremes, impulses/mixed patterns, exact zeros, varied strides and
rejected metadata/domain values. They check each output, input accesses, guards
and saved state. The high-address harness provides 16 KiB of private stack.
Reports require a new path. Matching output values does not by itself prove use
of MSA; the instructor's assessment also considers the exercise's vector objective.

The full instructor accuracy check follows H.263 Annex A, independent of the
student forward entry. Both supplied precisions, both modes and both byte orders
passed 480000 target inverse blocks and 48 zero calls. A deliberately wrong
signed rounding bias failed all six full cohorts. The chapter records the
measured peak/MSE/bias statistics and limits. Another valid inverse must undergo
its own gate; passing these reference runs does not certify an untested solution.

Quantization, closed local reconstruction, baseline bitstream output, full-encoder
interoperability/quality grading and chapter distributions/captures remain unfinished.
The authoring plugin remains separate and depends on MARS.
