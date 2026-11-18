# FIR construction draft

This companion supports the directly edited native chapter at drafts/fixed-point-fir.tex.
The full student package and interactive runner remain in development. It is outside
the main book's twenty complete text packages. No IDE screenshots have been captured.

Begin with main.asm and retain labels and region markers. The first twenty-one edits
complete fir_scalar, reached through the initial public tail transfer. The later entry
edit switches fir_q15 to the developing vector path. The supplied vector quantizer
uses the same rounding, clamp, count and store rule. The completed scalar routine stays
available for comparison; final free-writing submissions need only fir_q15 and the
supplied harness, with equivalent behavior.

steps.json is a companion transcript with quizzes and two hints per step. It does not
generate or constrain native prose. Correct-choice keys remain private. Enforced hints,
progression and mode switching are future runner capabilities.

## Repeat an observation

Use your cumulative program after the named step's edit. From the public repository:

```sh
python3 drafts/fir-construction/prepare.py s06-1 \
  --source /absolute/path/to/your-cumulative-main.asm \
  --output /tmp/fir-s06-1.asm
```

The output must not exist and cannot be your source. Open it in MARS, begin a fresh
Debug execution and pause before filter_ready. Inputs include every one of the seven
history samples, followed by the new samples. Count is input length minus seven, not
the full array length. Taps contains eight signed codes in oldest-first order. The
helper writes these arrays, pads source to 192 bytes and coeff to 64 bytes, fills the
entire 192-byte output with a5, and leaves guards/metadata unchanged. It emits signed
byte initializer spellings above raw 127. Read result bytes unsigned.

For manual preparation, replace the contents between source and source_end with the
complete `.half` input array and enough trailing space for 192 total bytes. Replace
coeff contents with its eight `.half` codes and 48 trailing bytes. Fill output with
192 `.byte -91` values. Set count to input length minus seven. Keep all other lines.
Even zero-output recipes list seven history values, which the kernel must not read.

Each recipe describes expected state at filter_ready. outputPrefix is followed by a5
bytes wherever the partial function has not stored output. registers are signed scalar
expectations. vectors are raw unsigned lane bit patterns at the given width. Unlisted
registers are unconstrained. advance applies to outer a0 and a2; walkAdvance applies
to scalar t1 and t2 relative to the input and coefficient starts. Private checks repeat
these observations in isolated calls at source/coefficient/output offsets three/five/
seven as well as the ordinary caller's aligned pointers.

Recipes s42-1 and s42-2 apply to the completed program. They distinguish raw -32768.5,
which rounds to the negative endpoint without clipping, from rounded -32769, which
clips once. Their identical output bytes cannot establish the clipping history alone.
Restore the original inputs for the default sixteen-output impulse experiment.
