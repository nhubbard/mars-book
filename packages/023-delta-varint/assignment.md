# Encode changes with variable-length integers

Implement delta_encode and delta_decode. On entry a0 is a full source pointer, a1 is
input length, a2 is a full destination pointer, and a3 is output capacity. Encoding
consumes 0..32 signed word readings and produces up to 160 bytes; decoding consumes
0..160 bytes and produces up to 32 signed word readings. Word pointers are aligned
to four bytes; byte pointers may have any alignment. Incoming sp is eight-byte aligned.
Keep the supplied caller, labels and arena sizes: 40 words and 192 bytes. Functions
may receive independent offsets into these arenas, including pointers above 32 bits.
Slices are disjoint. Each call starts with predecessor zero, including repeated calls.

Subtract each reading from its predecessor in full width. Differences range from
-4294967295 through 4294967295. Map d to 2*d when nonnegative, otherwise -2*d-1.
Emit the shortest base-128 representation, low group first, with seven payload bits
per byte and bit seven indicating a further group. Zero is byte 00. Maximum width is
five bytes; a fifth byte must terminate with payload less than 32. A multibyte value
with terminal payload zero is malformed. Decode the unsigned value and exact signed
difference before adding the predecessor and checking the reconstructed signed-word
range. Byte spelling validity precedes reading-range validity.

Return committed input in v0, committed output in v1 and status in a3: zero complete,
one capacity, two malformed bytes, three reconstructed reading outside int32.
Encoder count units are readings then bytes; decoder units are bytes then readings.
Commit only complete values. A rejected candidate advances neither counter and writes
no destination bytes, including writes later restored. Only the final committed
output prefix may be written. Read only the logical input slice, not mapped padding.
Private stack use and submission-owned read-only constants are allowed. Preserve full
s0..s7, fp and sp and return through supplied ra. No console I/O is allowed.

Empty input succeeds before testing capacity. Nonempty input at zero capacity returns
status one without reading input or writing output. Decoder capacity precedes parsing
the next candidate. Preserve earlier completed output on a later error. Do not retain
hidden stream state between calls. For [1000,1002,1001,1001,20000], the hexadecimal
wire is d0 0f 04 01 00 ee a8 02. Byte capacity seven commits four readings and five
bytes, with status one. The remaining candidate needs three bytes, so two unused
positions inside capacity must remain unchanged.

## Guided and free-writing work

The native chapter is the composition authority. Replace its fifty marked regions
in the stated order; keep supplied frame, status and exit code. The initial width
gate deliberately bypasses checks until the known-valid byte loop is constructed.
Unfinished stages return provisionally and can have pending output; they are not
completed codec implementations. Guided checks inspect the illustrated temporaries.

Use `python3 prepare.py s28-1 --source /path/to/your.asm --output /tmp/codec-view.asm`
to prepare a separate observation file. It refuses an existing output or the source
path. Most recipes pause at codec_done before caller result stores. Recipe s57-1
pauses at the first dec_terminal visit inside the completed decoder, then continues
to return. Working saved registers are restored by the exit code.

Run `java -jar /path/to/cli.jar grade checks/construction/s28.kts /path/to/your.asm`
for that illustrated observation. For completed free-writing solutions, use
checks/kernel.kts, checks/program.kts and checks/reuse.kts. Final checks accept other
correct registers, labels and algorithms under the public arithmetic, memory and ABI
contract. They do not require reference instruction counts. The starter intentionally
fails final checks. Use JDK 25 and the MARS commit pinned in package.json.

The book compares fixed words, independent signed mapping, and mapped deltas; it
records full entry-to-return instruction work separately. No host-time speedup follows
from fewer bytes. Guided progression, hint policies and mode selection are described
for future consumers; remote delivery and policy enforcement remain pending. All four
light-theme IDE capture slots remain planned. Instructor keys, solutions and additional
cases are distributed separately.
