# Independent matrix-vector reduction

Replace the failing matvec body. Preserve the public caller and data labels.
Arguments a0/a1/a2/a3 are full pointers to the packed word matrix, word vector,
doubleword output and two-word descriptor M,K. Dimensions range from zero through
sixteen. Active signed inputs range from -32768 through 32767. For each selected
row i, return the exact sum of matrix[i*K+q] times vector[q] for q=0 through K-1.
Store one eight-byte signed result per row and return M in v0. There is no scaling,
saturation or intended wraparound.

Use passed pointers, which may select interior disjoint windows and may exceed
four gigabytes. The matrix has M*K active words, the vector K active words and
the output M active doublewords. With M=0, read neither input and write no output.
With K=0, store M zero doublewords without input reads. The default rows [1,2,3]
and [4,5,6] with vector [7,8,9] produce 50 and 122. Restart the vector for each row.

Preserve full s0 through s7, fp and incoming aligned sp, caller stack bytes,
inputs, descriptor, guards and inactive output. A restored private frame below
incoming sp is allowed. Within the assembled caller region, read only selected
inputs, the descriptor and initialized selected output; write only selected
output during the function. A read from unnamed alignment padding is forbidden.
Every selected output byte must be written during this call before any partial-
sum read. Setup and a previous call do not initialize this call’s output. Produce
no console I/O and return through the incoming continuation.

Learn’s native function profile contains eleven cases: the eight original input
families, a direct default-input function case, an explicit positive 2^31 case,
and the complete default caller. Each of the ten function cases calls twice
through different continuations, giving twenty direct function returns plus
one complete-caller check. Each case has a cumulative 100000-instruction budget. The
original standalone Kotlin script retains its nine checks and is supplied
unchanged. The adjacent JSON files are bounded grading data, not executable
scripts. Function-profile version 8 requires the updated MARS checker.

Both native assessments must pass for the current source: function/caller checks
and complete silent normal exit. Use either row-at-a-time, reverse reduction,
private stack or initialized-output accumulation when it satisfies the interface.
No private label, scratch-register choice, exact instruction count, traffic count
or cache miss count is required. Finite cases support your reasoning; they do
not replace the argument for every allowed shape and input value.
