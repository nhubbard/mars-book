# Unpadded URL-safe encoder

Implement b64url_encode in base64url.asm, preserving its caller and public data
labels. Inputs are full source pointer a0, unsigned byte count a1, full
destination pointer a2 and unsigned byte capacity a3. Accept counts zero through
64. Return status in v0 and exact output length in v1; every failure returns
length zero. Source and destination are disjoint, may be byte-unaligned and do
not require terminators.

Use the same six-bit grouping as the standard encoder. Map indices 62 and 63
to - and _ instead of + and /. Omit all = padding. One final source byte emits
two characters and two final bytes emit three. The exact output length is
ceil(8*n/6), at most 86 for this domain. Input fb ff ff ff produces -____w,
six character bytes. Write neither padding nor a NUL byte after the result.

Apply preflight tests in order: reject an unsigned count above 64 with status
one; accept count zero with status zero; for nonempty valid count, reject
insufficient unsigned capacity with status three before any caller buffer
access. Otherwise encode successfully with status zero. A high-bit-set count
is above the limit. A high-bit-set capacity is a large unsigned capacity.
Empty or failed work permits no caller buffer reads or writes, even when the
pointers are invalid. Private constants and a private stack frame are separate
from the selected caller buffers.

On success, read only the selected source and exact output slices and write only
the exact output. Preserve input, guards, metadata, padding and inactive output.
This includes same-value stores and temporary writes later restored. Mapped
backing capacity does not authorize extra vector loads, stores or a terminator.
Preserve all bits of s0 through s7, fp and incoming aligned sp, and restore any
private frame below sp. Return through the supplied continuation and perform no
console I/O. The supplied caller and checks use the end of the assembled
256-byte workspace for the stack, so data relocation does not change the contract.

At url_ready, the default caller has returned status zero and length six;
destination begins -____w and the next byte remains a5. Published status and
written words are still -1. At results_ready the caller has published zero and
six. Normal exit subsequently uses v0 for its service selector.

The unchanged base64url.kts retains 594 public cases: 593 direct function trials
and the default caller. Native data independently preserve every original Java
Base64 result, deterministic arena byte, offset, invalid-pointer case, unsigned
argument bit pattern and one-million-instruction budget. Four function groups
check lengths 0–31 under five capacities (160 cases), lengths 32–64 under five
capacities (165), all 256 one-byte inputs, and fourteen edge/state cases. The
last group includes the original caller before/after publication and an added
five-call sequence: capacity failure, successful retry, empty work, over-limit
work and a new successful tail. Invocation-scoped permissions prevent an earlier
call's windows from authorizing later empty or failed work. There are 595 function
cases and 600 calls, plus a separate complete silent-exit assessment.

Sixty-one bounded function profiles are grouped behind four student assessment
actions, followed by exit. Existing per-profile limits and every original case
budget remain. A failed later profile prevents completion, and cancellation
consumes no assessment attempt. Native lesson format 8 supports these batches;
function-profile version 10 supplies label-relative initial sp rather than a
hard-coded absolute address. Work without the interactive lesson remains possible
with the written lab and standalone script.

Explain unsigned preflight, sextet versus character values, unpadded tail length
and complete access widths beside your implementation. The finite cases do not
exhaust every binary sequence or independent pair of pointer offsets. Instructors
may separately review vector strategy; no private label, scratch register layout
or exact instruction count is required by these behavioral checks.
