# Multiply matrices in clipped tiles

Implement `matmul_naive` and `matmul_tiled`. On entry a0/a1/a2 are full pointers to
A/B/C, and a3 points to four signed words M,K,N,T. Compute C[i,j] = sum over k of
A[i,k]*B[k,j], for an M-by-K left input and K-by-N right input. All arrays are packed
row-major. Dimensions are 0..16 and tile edge T is 1..8. Inputs are signed values
-32768..32767 stored in four-byte words; outputs are exact signed eight-byte integers.
There is no fixed-point scale, saturation, rounding or intended wraparound.

Keep the supplied caller, arena labels and allocation sizes. Function grading passes
independent offsets within the 272-word left/right and 272-doubleword product arenas;
the pointers need not equal the labels and may exceed 32 bits. A/B and descriptor are
word aligned; C and incoming sp are doubleword aligned. Active slices are pairwise
disjoint, with lengths 4*M*K, 4*K*N and 8*M*N. The descriptor has sixteen bytes.
Read only the selected caller slices and descriptor. Submission-owned read-only
constants and private stack storage are allowed. A read that crosses a selected
boundary is invalid even if it stays mapped. Only selected C caller bytes may change;
writing and restoring an input is still forbidden. Initialize every output byte during
this call before reading it as a partial sum. Initial output contents are arbitrary.

Return M*N in v0, preserve every bit of s0..s7, fp and sp, and return through the
supplied ra continuation. Both matmul_done and function_return are possible supplied
continuations; do not hard-code a return address. Produce no console I/O. If M=0 or
N=0, access no matrix bytes. If K=0, write M*N zero doublewords without input reads.
Descriptor/private-stack accesses remain permitted. No hidden state from a prior call
may affect a later result.

The guided sequence develops scalar i-j-k, then clipped I-J-L-i-j-k blocking. An
instructor can assess that strategy separately. Final free-writing checks accept other
correct schedules, helper labels and temporary registers; they do not require exact
reference instruction or cache counts. Both public entries must satisfy the interface.
The method word selects scalar zero or tiled one for the caller, while direct checks
invoke each independently.

## Construction and final checks

Use the book's directly edited native chapter and the fifty companion step texts.
Replace a named region's temporary branch and nop with its increment. Prepare a
separate input file with `python3 prepare.py s15-2 --source /path/to/your.asm
--output /tmp/matrix-observation.asm`. The source and output must be distinct and the
output must not already exist. Most observations pause at matmul_done before the
caller stores v0 into completed; s48-1 pauses at the second block_reduction visit.
A provisional returned count does not establish that the product is complete.

Run `java -jar /path/to/cli.jar grade checks/construction/s15.kts /path/to/your.asm`
for that illustrated stage. The intermediate checks name its temporary registers.
Use checks/kernel.kts, checks/program.kts and checks/reuse.kts for completed solutions.
The first checks both entries through offset direct calls, the second runs the actual
caller, and the third checks successive calls with alternating return continuations.
Use JDK 25 and the pinned MARS revision from package.json. The unfinished starter is
expected to fail final-program checks.

## Measurement and delivery boundary

The native chapter compares a cold sixteen-line cache with sixteen bytes per line,
LRU replacement, and full/direct/two-way placement at constant capacity. It measures
function entry through return before caller storage and includes frame/descriptor data.
The stack is aligned at 0x7fff0000. Aligned ld/sd instructions emit two word notices
in this core; data notices and retired instructions are separate units. This cache
model has no latency or cycle estimate. Miss reductions establish no host-time speedup.

Native TeX files are the publication authority. The structured exercise files support
future guided/free-writing consumers and instructor hint/progression policies. The
remote runner and policy enforcement are not implemented yet. All four light-theme
IDE figure plans remain planned; no screenshot pixels or coordinate receipts are
included. Instructor keys and additional checks are distributed privately.
