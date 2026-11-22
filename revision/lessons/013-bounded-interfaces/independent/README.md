# Bounded binary delimiter parsing

Replace the unfinished `read_line` body in `bounded-line.asm`. Keep the public
caller and data labels. Arguments are input pointer `a0`, nonnegative available
count `a1` and a selected nonoverlapping, word-aligned eight-byte record in `a2`.
The input window lies inside the 256-byte arena at any permitted byte offset.
Use the supplied pointers and count, not the global default fields.

The record ends at the first LF byte, hexadecimal 0A. At most 32 preceding bytes
are payload. Every other byte is ordinary binary payload, including zero, CR
(0D) and high bytes. There is no text encoding conversion or escape syntax.
Prove the next byte available before loading it. If LF occurs among the first
33 available bytes, accept its zero through 32 preceding payload bytes. If 33
bytes are available without LF, return too-long. With fewer than 33 and no LF,
return need-more. At index 32, test the available byte for LF before rejecting
it as a 33rd payload byte. Do not read beyond that LF or the first 33 bytes.
Revisiting bytes inside the permitted prefix is allowed.

On success, return consumed count `length+1` in `v0` and the original full input
pointer in `v1`. Write two native words to the selected record: payload length
and payload sum modulo 256, excluding LF. On failure, return `-1` for need-more
or `-2` for too-long, with `v1=0`, and perform no output stores. Writing and then
restoring the record is forbidden. Preserve the input and its guards, other
record, metadata, saved registers `s0`–`s7`, `fp`, `gp`, aligned incoming `sp`
and the caller's 32 stack bytes. Private frames below incoming sp are available.
Produce no output and return through the supplied continuation.

The default `41 00 80 0A 42` consumes four bytes, publishes length three and sum
193, and returns the input pointer. The final 42 belongs to the next record.
Empty input needs more without a read. Immediate LF accepts an empty payload.
Thirty-two payload bytes without a next byte still need more; a following LF
accepts 33 consumed bytes, while a following non-LF proves too-long.

Learn separates the complete original corpus into two bounded profiles. The
first has 139 prefix, boundary and default-caller cases. The second covers all
256 possible first-byte values. The profiles retain all 393 original function
inputs, add a direct default-input function case and retain the complete default
caller. Every function case calls twice through different continuations, giving
789 checked returns across the profiles. Each case has a cumulative 5000-
instruction budget. Alternative one-pass, delimiter-first/two-pass, stack-using
and signed-payload summation methods are accepted when their results and accesses
meet this contract. No private labels, scratch registers or exact count is required.

Input access histories enforce the supplied prefix and the first-LF/33-byte
limit. Store histories protect inputs, records and caller data, including unnamed
padding between public objects. A forbidden continuation interval distinguishes
returning through the supplied pointer from jumping elsewhere and falling through
to the requested stop. Returned pointers and the caller's stored eight-byte
pointer are resolved from actual data labels, preserving full width after
relocation. Native word byte order and the binary grammar are separate concerns.

The third Learn check requires the complete program to exit silently. All three
checks must pass for the current source revision. The accompanying public
`bounded-line.kts` preserves all 394 original standalone checks, including the
complete caller. Use `mars grade` as described in the lab. The JSON profiles
are bounded data for Learn, not executable scripts. A finite corpus is evidence;
justify your bounds, error order and delayed stores for the full stated domain.
