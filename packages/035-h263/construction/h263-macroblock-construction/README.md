# Target-side six-block prediction

Steps 31–40 in the native chapter derive all Y/Cb/Cr origins in assembly, call
the completed eight-by-eight predictor and stage a whole macroblock before
publication. Carry your completed `h263_predict8` into the new working source and
retain one ordinary main. Both supplied entries initially return development four.

`h263_predict_mb` takes an eight-byte-aligned 144-byte request in a0 and a
sixteen-byte-aligned 1152-byte result pointer in a1. Use eighteen native-endian
64-bit fields; dx/dy are signed, all other fields unsigned:

| Offsets | Fields in order |
| --- | --- |
| 0, 8, 16 | Current Y, Cb, Cr plane pointers |
| 24, 32, 40 | Reference Y, Cb, Cr plane pointers |
| 48, 56, 64 | Current Y, Cb, Cr strides |
| 72, 80, 88 | Reference Y, Cb, Cr strides |
| 96, 104 | Current luma macroblock x/y |
| 112, 120 | Signed full-luma dx/dy |
| 128 | Mode: zero scalar, one MSA block arithmetic |
| 136 | Reserved zero |

Y planes are 176x144 and strides 176..4096. Cb/Cr are 88x72 and strides 88..4096.
All six strides are independent. The caller supplies mapped active full planes,
records and disjoint output; input byte pointers may be unaligned and read-only
images may overlap. Supply a sixteen-byte-aligned stack with valid private space.
The ordinary program entry aligns its legacy initial stack down before calling;
the callable routine preserves its already aligned incoming SP and saved GPRs.

Current x/y must be multiples of sixteen in 0..160/0..128. Components dx/dy are
-2..2, with the entire selected reference luma block inside the picture. Reject
out-of-picture selections instead of clamping or repeating search. Validate
record alignment/extents before request reads; validate every field and all six
non-wrapping plane extents `(height-1)*stride+width` before any pixel read.
Actual reads must fit current macroblock samples or the selected reference halo,
not padding or unrelated active image samples.

Derive Y local offsets (0,0),(8,0),(0,8),(8,8), with zero phases. For Cb and Cr,
current origin is (x/2,y/2); reference adds floor(dx/2),floor(dy/2), with nonnegative
phases dx-2*floor(dx/2),dy-2*floor(dy/2). Negative odd components require phase
one from the preceding integer origin. Use each plane's actual pointer/stride.

On success v0=0/v1=sum of all six block SAD values (0..97920). Store the previous
192-byte prediction/residual layout at offsets 0,192,384,576,768,960, in Y top-left,
Y top-right, Y bottom-left, Y bottom-right, Cb, Cr order. On argument rejection,
return v0=1/v1=0 without pixels or output changes. A nonzero child predictor result
returns v0=2/v1=0 without publishing any caller bytes; earlier child reads may
already have occurred. Stage child results privately and publish after all succeed.
The return-boundary guarantee does not make a multi-store copy hardware-atomic.
Preserve request and all input bytes. Equivalent traversal/frame layouts are valid.

```sh
python3 drafts/h263-macroblock-construction/prepare.py --source my-macroblock.asm --mode 1 --dx -2 --dy -2 --output worked-macroblock.asm
```

Preparation installs full raw pictures one and zero, preserving every instruction.
The ordinary caller supplies six plane pointers and current origin (16,32); it
does not compute child origins/phases. Full target integration must do that.
Existing preparation outputs are refused. The finished default prints 2976 and
exits zero. Vector (-1,-1) prints 5788 (Y5208/Cb232/Cr348); zero prints 9072
(Y7872/Cb480/Cr720). Both modes agree. At `macro_caller_return`, output and returned
status/total are complete; the caller has not copied those registers to named fields.
An unfinished outer routine prints the development message and exits one. A
finished outer routine using the unfinished child reports block prediction failure.

```sh
python3 drafts/h263-macroblock-construction/grade.py my-macroblock.asm --cli /path/to/mars-cli.jar --report macroblock-results.json
```

The 64 public cases compare every predicted byte, signed residual, record offset,
combined score, bounded source access and saved state, with both modes and byte
orders. A separate high-address harness supplies 16 KiB of private stack. It uses
doubleword guard fills to retain full-sized arenas within the assembly-work budget.
Reports need new paths. No exact instruction count or private label is required.

This remains prediction and residual preparation. Transform/quantization, local
reconstruction, bitstream output, full-encoder grading/distributions and chapter
IDE figures remain unfinished. The final encoder must use reconstructed reference
planes. The separate authoring plugin depends on MARS and handles future captures.
