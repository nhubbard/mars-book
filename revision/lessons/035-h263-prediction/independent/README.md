# Eight-by-eight signed reconstruction

Replace the deliberately unfinished body at `h263_reconstruct8`. The separate
`recon_function_return` trap is a grading return boundary, not another unfinished
function. Keep public labels available. Your own private labels, frame layout
and instruction spelling need not match a reference implementation.

Arguments are prediction byte pointer a0, signed inverse-halfword pointer a1,
output byte pointer a2 and settings pointer a3. Settings are three native unsigned
64-bit byte strides: prediction 8..4096, inverse even 16..4096, output 8..4096.
Settings require eight-byte alignment and inverse requires two-byte alignment.
Prediction/output byte pointers require no extra alignment. Every selected extent,
including the 24-byte settings record, must be non-wrapping in the 64-bit address
space. A containing image extent is seven row strides plus its active row width;
that does not admit reads of the gaps between rows.

There are eight rows, each containing eight samples. Prediction values are
unsigned bytes. Inverse samples are signed native halfwords and must all lie in
[-256,255]. Add in a sufficiently wide signed value, then publish each sum clipped
to [0,255]. Return status zero in v0 and the number of sums strictly below zero
or above 255 in v1. Sums exactly zero or 255 are already representable and do not
contribute to that count.

Output is disjoint from inverse and settings. It may exactly alias prediction
with identical prediction/output strides; otherwise it is disjoint from prediction.
Inputs/settings are disjoint except for that admitted output alias. Read only the
24 settings bytes, eight prediction bytes per row and sixteen inverse bytes per
row. Write only the successful eight output bytes per row. No row padding is
readable scratch, and the output is not a separate readable workspace. Preserve
all caller bytes outside selected successful output, including original inputs
except the stated exact alias. Private storage outside those objects is available.

Invalid metadata returns v0 one/v1 zero before sample access or output writes.
Test settings alignment and its non-wrapping extent before loading it; validate
full-width strides and every selected window before sample reads. Narrowing a
stride such as 0000000100000008 hexadecimal to eight would admit an invalid value.
Any inverse sample outside [-256,255] returns v0 two/v1 zero before output
publication. Validate every sample first, including the last correction under
an exact prediction alias. Writing earlier valid pixels before discovering the
last invalid sample violates the interface even when some byte values happen to
remain the same.

Preserve s0-s7, fp, gp, sp and the full return continuation. Incoming sp is
sixteen-byte aligned with 8192 private stack bytes supplied. Do no console or
file I/O. Repeated calls share one machine and one cumulative 100000-instruction
case allowance. Validate/replay and private staging are both admitted; an exact
reference instruction count or private register choice is not required.

The small supplied main uses prediction 128 and varied signed corrections.
Correct output rows are 0,0,1,128,254,255,255,255 with 24 clipped sums. Pause at
`recon_demo_return` before main records v0/v1 to inspect the result. A quiet Run
alone does not establish correct samples or publication behavior.

Five immutable profiles retain all 138 original public cases in both byte orders,
including 146 calls. Complete the staged checks, then recheck every case on the
same final source. Edits can stale earlier execution evidence; renew it when
prompted. Changing extracted profile copies cannot replace the imported archive's
expectations. Instructor solutions and expanded checks are separate.
