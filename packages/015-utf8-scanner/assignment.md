# Validate UTF-8 across input chunks

Implement scan_utf8(a0=state pointer,a1=chunk pointer,a2=byte count,a3=final flag).
Use MIPS64 Release 6, default little-endian native state words, and the MARS ABI.
A chunk has 0..64 bytes in the supplied 96-byte input arena, and can be unaligned.
The selected 32-byte state is word aligned and does not overlap input. The caller
supplies a reachable active or terminal state. For an active call, total+n<=4096.
Preserve full s0..s7 and sp, return through supplied ra, and produce no output.

State word offsets: total accepted bytes 0, complete scalar count 4, continuations
needed 8, next-byte minimum 12, next-byte maximum 16, sequence start 20, status 24,
detecting offset 28. Empty state: [0,0,0,128,191,-1,0,-1] in decimal. Active status
is zero, successful final status one, malformed minus one, truncated minus two.
Idle active state has need=0, ordinary bounds 128..191, start=-1. A pending state
retains its lead start and next-byte bounds, resetting bounds after the first tail.

Return v0=0 for accepted nonfinal input, 1 for completed final input, -1 for the
first impossible byte, -2 for final input ending with a still-possible prefix.
v1 is accepted bytes from this call, excluding an offending byte. Total is absolute
stream progress. Count increments only on complete scalars. On malformed input,
start identifies its lead or invalid leading byte and error identifies the first
impossible byte. On truncation, start retains the lead and error is absolute end.
All accepted prefix progress is committed. A final flag never masks a byte error.

A terminal state rejects any later call with v0=-3,v1=0, no input reads, and no
caller-data writes. Its stored status and diagnostic remain unchanged. Active
calls may write only the selected state. Preserve input, guards, other state,
and caller metadata. Input reads fit wholly within the current chunk, even when
adjacent bytes are mapped. Read-ahead inside that chunk is allowed. Store order
while active, scratch registers, branch structure and exact counts are not grading
requirements. A legal restored private stack frame is allowed.

Use Unicode 17 Table 3-7 UTF-8 structure: shortest scalar encodings, no surrogates,
no values above U+10FFFF. Count valid unassigned/noncharacter scalars and U+0000;
do not strip U+FEFF. No normalization, grapheme count, replacement output or recovery
is implemented. references.json pins primary standards sources.

Keep the supplied data layout, main and marker. Guided mode inserts fragments in
named regions; free writing can replace the function internals and add immutable
lookup data after state_end. Instructor progression and hint controls await the
trainer. Four IDE figures are planned recipes, not completed screenshots.

```sh
java -jar /path/to/cli.jar grade checks/stream.kts starter/main.asm
java -jar /path/to/cli.jar grade checks/program.kts starter/main.asm
```

The finished source is required for these checks; intermediate return values can
be temporary. The course tool runs both scripts:
`python3 tools/course.py check --package 015-utf8-scanner --cli /path/to/cli.jar --source /path/to/main.asm`.
The CLI must include the pinned stateful callFunction API. The starter exits normally
but is intentionally incomplete and must fail final grading.
