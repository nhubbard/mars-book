# Filter samples with Q15 coefficients

Implement fir_q15. Pass full input-window, coefficient and destination addresses in a0,
a1 and a2 and output count 0..64 in a3. Return the original count in v0 and the number
of rounded results requiring saturation in v1. Samples and coefficients are signed
sixteen-bit little-endian codes at arbitrary byte alignment.

## Exact arithmetic and history

Input has seven chronological history samples followed by count new samples. a0 points
to the oldest history value. Eight coefficient codes are stored oldest-first, aligned
with each window. Code c represents c/32768. For each output i, compute the exact sum
S = sum(input[i+j]*coefficient[j]) for j=0..7. Compute R = floor((S+16384)/32768) once,
then store R clamped to -32768..32767. Count a clip only when R lies strictly outside
that interval. This is nearest rounding with ties toward positive infinity, not signed
truncation toward zero or per-product rounding/saturation. Raw -32768.5 rounds to the
negative endpoint without clipping. All signed coefficient codes are allowed.

One minimum/minimum product is 1073741824, two sum to 2147483648 and eight to
8589934592. Preserve exact wide partial sums before quantization. A word dot-product
result can overflow before it is copied to a wider register.

Each call uses only supplied state. For a split after m outputs, the second input
begins 2*m bytes after the original base, with the required seven-sample history.
Concatenated outputs must equal a whole call. No hidden history is retained.

## Slices and callable state

Input, coefficient and output slices are pairwise disjoint, of lengths 2*(count+7), 16
and 2*count bytes. Only logical output may change, including temporary writes. Reads
fit selected input, coefficients or output. Mapped padding does not enlarge a slice.
Zero output count accesses no caller data, including no history or coefficient read.
Private native stack storage and submission-owned constants are allowed. The supplied
source/output arenas are 192 bytes and coefficients 64; capacities are not slice lengths.

Preserve all bits of s0..s7, fp and sp, return through supplied ra and produce no console
output. Arguments, temporaries, vector registers and overlapping floating-point storage
may change. Keep the supplied caller/data harness. The public interface and exact
behavior define correctness; scratch registers and instruction counts do not.

## Guided and free-writing modes

The starter intentionally fails complete filtering. Its public entry initially transfers
to fir_scalar. Complete that function first, retaining region markers and labels. The
later entry edit starts vector construction. The supplied vector quantizer repeats the
proven scalar rounding/clamp/store rule, and the scalar comparison remains available.

Free-writing solutions may choose another algorithm, helpers, registers or grouping.
fir_scalar is optional in a final equivalent submission. Behavioral checks accept scalar
and vector implementations. A separately assigned vectorization objective requires
strategy review as well. Construction checks name the illustrated intermediate state;
use them for the guided sequence, not a different algorithm's partial program.

Read fixtures/guided-inputs.md to prepare each observation in a separate source file.
Start a fresh Debug execution and pause before filter_ready, before caller result stores.
The transcript has quizzes and two available hints per step. Enforced hints, progression,
skipping and mode-switch policies are documented future interactive-runner capabilities.

## Run public checks

Use JDK 25 and the matching MARS CLI from the extracted package:

```sh
java -jar /absolute/path/to/cli.jar grade checks/kernel.kts /absolute/path/to/submission.asm
java -jar /absolute/path/to/cli.jar grade checks/program.kts /absolute/path/to/submission.asm
java -jar /absolute/path/to/cli.jar grade checks/splits.kts /absolute/path/to/submission.asm
```

The kernel checks every count, unaligned slices, exact arithmetic, preserved state and
all caller-data access bytes. The program check covers all 65 counts. The split check
compares whole and concatenated outputs for every cut. Default count sixteen uses an
impulse 16000 after seven zeros and eight coefficients 4096: eight outputs 2000 then
zeros. At filter_ready v0=16, v1=0 and processed/clipped remain 99. At results_ready
the words contain sixteen and zero. Source and coefficient bytes stay unchanged.

Five light-theme capture plans cover caller states, rounded clipping distinctions and
wide accumulation. Actual IDE screenshots and coordinate receipts remain pending in
the separate authoring plugin. Numerical capture models are not screenshots.

## Reproduce the listening files

Six mono WAVs contain two seconds at 16000 frames per second. The low component is a
period-sixty-four integer triangle; high repeats 0,12000,0,-12000. Seven preceding
periodic samples are supplied explicitly. Eight coefficients 4096 average complete
windows: filtered high-only PCM is zero, and filtered mixed equals filtered low.
Filtered low still changes shape and delay from its original input. No normalization
or live playback-device result is claimed. Numerical formulas and plots support use
without audio.

```sh
python3 render-listening.py --material . --cli /absolute/path/to/cli.jar \
  --java /absolute/path/to/jdk-25/bin/java --source /absolute/path/to/submission.asm \
  --output /tmp/fir-listening
```

This independently authored public utility runs checks/listening.kts, verifies each
returned sample against the published integer formula, and writes WAVs and a receipt.
It contains no instructor source or private test cases.

## Work counts and publication

Quiet sixty-four-output examples execute 10246 scalar or 1930 MSA instructions, not
host elapsed-time speedups. The scalar table measures the direct alternative entry;
the starter's temporary public transfer adds dispatch outside that boundary. Correct
solutions need not match exact counts or use the same load widths.

The editable book remains main.tex and includes in the public source repository.
native here is a versioned chapter snapshot with original root-relative includes, not
another composition engine. Markdown/reading.json are companion task transcripts.
Human review, actual captures and independent accessibility validation remain pending.
