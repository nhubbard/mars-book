# Stable sign partition of a supplied span

Replace partition_negative's break body. a0 points to a word-aligned active span
within the eight-word values allocation; a1 gives its length from zero through
eight, with the whole span inside that allocation. Words are signed 32-bit
integers. Group negative words before nonnegative words and retain their
original relative order within each group. Zero is nonnegative. Return the
number of negatives in v0. An empty span returns zero and permits no array loads
or stores, even when its pointer is a valid address.

For [2,-2,3,-4,0,1,-3,-1], the result is [-2,-4,-3,-1,2,3,0,1] and the count is
four. Ascending sort would change each group's order and does not meet this
contract. Comparing directly with zero handles -2147483648 without negating it.
State your invariant before choosing instructions. In-place rotations or a
temporary-buffer method are both accepted. Private labels, scratch registers
and stack layouts are your choice. A private buffer can live below incoming sp;
the caller's frame is not temporary storage.

Preserve inactive words, values_before, values_guard, count, negative_count and
their guards. The direct checker passes a length different from global count;
use a1 rather than reloading a fixture variable. The ordinary supplied main
still calls with count eight and stores the returned value after the call.
Direct checks stop at the supplied continuation before executing that store or
the exit selector. Preserve s0–s7, fp, gp and sp at full width and the 32 caller
bytes above the aligned incoming pointer. Those caller bytes must receive no
execution stores, including same-value stores or writes later restored.

The public version-4 profile has 56 cases: eleven example inputs, plus all 45
valid offset/length pairs in an eight-word fixture. Examples include signed
extremes, duplicate values, all-negative, all-nonnegative and empty inputs.
An explicit guard precedes values. Access observers watch that four-byte guard,
the 32-byte array and its four-byte following guard. Every overlapping execution
read or store must fit wholly inside the active span. A wider access beginning
outside the watched region still counts if it overlaps. This is a bounded
observation, not a policy for all memory addresses. Separate observers prohibit
stores to supplied metadata and guards. Initial setup and host inspection occur
outside execution history. Access-history overflow fails explicitly.

Every case uses a fresh machine, different full-width saved values and stack
location, and a cumulative 10000-instruction budget. It calls your function twice
on the same span through partition_done and partition_exit. A correctly
partitioned span remains unchanged by another partition, and its negative count
is the same. The second call tests that property and the incoming continuation;
it does not create a new input. No exact store sequence, frame size or instruction
count is required within the execution budget.

Use Check this program in Learn for feedback on the current editor text. The
unfinished starter should fail. For the same cases outside the IDE, use JDK 25
and a MARS CLI supporting label-relative setup and execution access traces:

java -jar /path/to/cli.jar grade partition-negative.kts partition-negative.asm

The byte strings encode the book's little-endian words. Fifty-six passing cases
establish 112 returned calls for those inputs, not all possible signed arrays.
Keep a default trace, an empty-span argument and a signed-extreme example beside
the report. Instructor policies can control progression, attempts and hints;
private checks and instructor implementations remain in a separate distribution.
