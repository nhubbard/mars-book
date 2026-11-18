# Mix signed PCM samples

Implement mix_pcm16 in the pinned MIPS64 Release 6 environment. Pass full source-A,
source-B and destination addresses in a0, a1 and a2 and a sample count 0..64 in a3.
Return the original count in v0 and the number of clipped samples in v1. Each sample
is signed sixteen-bit, little endian, two bytes, at any byte alignment.

For each pair, compute the exact mathematical sum. Store -32768 if it is smaller than
-32768, 32767 if larger than 32767, and the sum otherwise. Count one clipping event
only for a sum strictly outside that interval. Equal endpoints do not clip. Start the
count afresh on every call. The sum range -65536..65534 fits signed word arithmetic.

The two sources are disjoint or exactly identical. Output is disjoint from both or
exactly equal to either source. All three may be identical. Exact aliases have the same
start and logical length; shifted partial overlaps are outside this contract. A slice
has exactly 2*count bytes. Only destination bytes may change, including temporary writes.
A source is writable only when it is also exactly the destination. All caller-data
reads fit the selected sources or output. Mapped arena padding is not part of a logical
slice. Zero count performs no caller-data access. Submission-owned constants and private
native stack storage are allowed.

Preserve all bits of s0..s7, fp and sp, return through the supplied ra and produce no
console output. Argument, temporary and vector registers and overlapping floating-point
storage may change. Keep the supplied caller/data harness and the wrapping comparison
mix_wrap16, which returns v0=count and v1=0. Its output uses only the low sixteen sum
bits. It deliberately follows another arithmetic rule and is not a correct saturation
alternative.

## Guided construction and free writing

The marked starter intentionally fails the complete mixer contract. Replace one region
at a time, retaining labels and markers. Complete the scalar loop before enabling vector
groups. The provided entry retains the count and initializes v1; provisional regions
return through the supplied exit. Their partial results are construction observations.
Each authoring step has a separately runnable check in checks/construction. Those checks
name the illustrated temporaries. They do not establish an equivalent solution's
correctness at an intermediate point in a different algorithm.

For free writing, retain the public entry and harness while choosing private labels,
register roles, group sizes and algorithm. Both scalar and vector implementations are
accepted by the final checks without an exact instruction count, load width or scratch
register requirement. A separate vectorization objective requires instructor review of
the strategy; output behavior alone cannot prove it. Instructors may choose required
quizzes/checks, hint access, skipping and mode switching when the interactive runner is
implemented. This package records those policies; it does not yet enforce them in an IDE.

## Repeat an observation

Read fixtures/guided-inputs.md. The helper prepares a separate file from your cumulative
program and the exact named arrays, leaving your source intact. Start a fresh Debug
execution with a breakpoint at mix_ready. Source offsets in the ordinary caller are
zero; construction checks also make isolated calls at unaligned offsets three, five
and seven. Input recipes describe the aligned caller, not a hidden register setup.

## Run final public checks

From an extracted package, use JDK 25 and the matching MARS CLI:

```sh
java -jar /absolute/path/to/cli.jar grade checks/kernel.kts /absolute/path/to/submission.asm
java -jar /absolute/path/to/cli.jar grade checks/program.kts /absolute/path/to/submission.asm
```

The kernel check covers counts, byte alignments, exact aliases, endpoint distinctions,
result initialization, preserved registers and every caller-data access. The program
check covers all 65 counts through the complete caller and wrapping comparison.

At default count sixteen, mix_ready has v0=16 and v1=6, with pcm_c written and the
three result words still 99. At wrap_ready, processed=16 and clipped=6, live v0=16,
live v1=0 and wrapped_count still 99. At comparison_ready, the three words are 16,6,16.
The original sources remain unchanged for this caller's disjoint outputs. Four planned
light-theme IDE captures assert these states and a mixed block/tail case. Actual captures
and coordinate receipts remain pending in the separate authoring plugin.

## Listen or inspect samples

media contains twelve verified mono WAV files. Each has 32000 signed sixteen-bit
samples at 16000 frames per second, two seconds. Inputs a and b mix into one output;
they do not make each file stereo. No normalization is applied. Quiet saturated/wrapped
outputs are identical. Both louder scenes have 11000 clips with different waveforms.
Use the native sample formula and plot as an alternative to audio playback.

To reproduce all twelve files from your own completed mixer:

```sh
python3 render-listening.py --material . --cli /absolute/path/to/cli.jar \
  --java /absolute/path/to/jdk-25/bin/java --source /absolute/path/to/submission.asm \
  --output /tmp/pcm-listening
```

The renderer checks simulator-produced samples and writes a receipt. It requires the
supplied comparison and harness. This is offline sample generation; no live audio-device
timing or playback verification is claimed.

## Publication authority

The book is edited directly in main.tex and includes in the public source repository.
The native directory here is a versioned chapter snapshot for the student distribution;
its original root-relative includes are retained. It is not a separate build composition.
reading.json and Markdown are companion transcripts for a future runner. Human review,
actual IDE figures and independent publication accessibility validation remain outstanding.
