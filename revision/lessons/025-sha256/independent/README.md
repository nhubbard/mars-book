# Complete one streaming hash implementation

Work in `sha256.asm` throughout this lesson. Its six unfinished entries stop with
`break`; a reached break is evidence of unfinished code. Complete schedule,
compression, initialization, update, finalization and file ownership in that order.
Keep the supplied caller objects, controls and public labels. Choose your own
working registers, private helpers and stack frames under these contracts.

## Interfaces


sha256_schedule(a0 block,a1 schedule) reads exactly 64 bytes from an arbitrarily aligned
block and writes 64 numeric words to a word-aligned 256-byte array. All 64 words must be
the standard expanded schedule, independent of its prior contents. It returns v0=0.
sha256_compress(a0 state,a1 block,a2 workspace) reads one block, replaces eight aligned
state words by the compressed state and may use 256 aligned workspace bytes. Workspace
contents after compression are unspecified. Preserve the source; return v0=0.

An eight-byte-aligned context is 368 bytes: eight numeric state words at offset 0,
a uint64 original-byte total at 32, a uint32 used count at 40, reserved bytes 44..47,
a 64-byte buffer at 48 and 256 schedule-workspace bytes at 112. sha256_init(a0 context)
sets initial state, total and used independently of old contents; unused buffer and
workspace bytes are unspecified. sha256_update(a0 context,a1 source,a2 count 0..4096)
consumes the exact interval, changes only context and reads no source for an empty
update. Total original message bytes remain below 2^61. On return, state describes all
complete input blocks, used equals total modulo 64, and its buffer prefix holds the
remaining bytes. Return v0=0. Two contexts must stay independent under interleaving.

sha256_final(a0 context,a1 output32) adds standard padding and the full 64-bit big-endian
bit length, consumes context and writes exactly 32 big-endian digest bytes. Return v0=0.
After finalization, context contents are unspecified; initialize again before reuse.
Repeated finalization is not specified. Context and output are the only writable caller
intervals. Context workspace, register allocation, helpers and algorithmic equivalents
are not fixed by the reference implementation.

sha256_file(a0 terminated path,a1 scratch,a2 capacity 1..256,a3 output32) opens the path
read-only, starts one fresh context, consumes successful read prefixes, finalizes only
at EOF and closes the acquired descriptor. Return v0 status 0 success, 1 open failure,
2 read error. Preserve all 32 output bytes on failure. A positive short read continues;
a negative read fails; zero is EOF. Any positive request up to capacity is valid.
The wrapper owns only the selected scratch interval and successful output interval;
its context is private stack storage. Close on success and read failure; failed open
acquires no descriptor. No console output is permitted by any entry.

All caller objects are disjoint. Byte source/output/scratch/path slices may be unaligned;
state/schedule words are aligned to four and context to eight. Preserve every bit of
s0..s7, fp, sp and return through supplied ra. Incoming sp is eight-byte aligned. Use full
addresses, including above 4 GiB. Private read-only constants and stack storage are allowed.
Retain the supplied arenas and caller labels: 4160 data bytes, 96 state bytes, 320 schedule
bytes, 832 context bytes, 96 digest bytes, 128 path bytes and 320 scratch bytes. Tests choose
independent offsets into those backing objects. Unused arena bytes remain caller-owned.


## Feedback while constructing the implementation

The schedule, compression and initialization groups can run before later entries
work. The update group adds seventeen prefix cases that inspect the required
state, total, used count and active tail before any finalization. These are
additional construction checks. The completed-stream, file, caller and reuse
groups retain all 128 original correctness cases; the four original Kotlin
scripts are also supplied here for CLI use.

A source change makes earlier results stale. Run the relevant checks again after
editing shared helpers. Use the first failing case name to choose an input and
an earliest boundary in your own implementation. The checker accepts equivalent
registers, workspace values, frames and smaller positive file-read requests.
It does not accept writes into caller gaps, even if a store writes the same value.

## Observe your caller without creating another implementation

After the functions work, edit the existing `mode` word in this same file.
Mode zero initializes, updates and finalizes the three input bytes `abc`.
Mode one calls your file wrapper with the supplied path and scratch capacity.
The initial mode two selects schedule work; it is not the memory-stream path.
Finish any prior Debug process, save, and start a fresh launch after changing it.

`main_done` is before the caller stores status. `results_ready` is after that
store and before exit. These labels remain available even if every private
function label and register allocation differs from the worked experiments.
The lesson supplies deterministic file contents for its file Debug activities;
those fixtures are read-only and do not require creating a host file.
