# Compute a SHA-256 fingerprint

Implement sha256_schedule, sha256_compress, sha256_init, sha256_update, sha256_final
and sha256_file using the native chapter's MIPS64 Release 6 environment and little-endian
simulator memory. SHA-256 block interpretation and digest output are big-endian.
The native chapter is the publication authority; metadata does not regenerate its prose.

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

## Guided construction and free writing

Follow 47 steps and 38 marked edits in one cumulative working source. The six supplied
entry/exit pairs let incomplete routines return development status 3. This is not a
completed result. A partially filled update can temporarily store used 64 on development
return; do not continue it as a completed context. Partial finalization consumes its
prepared context. Use a fresh prepared image for every observation. The provisional
file exit closes an acquired descriptor even while later code is unfinished.

Use prepare.py with an exact recipe ID, --source /path/to/current.asm and a new --output
path. It refuses to overwrite the source or an existing output. Optional --fixture-output
writes exact bytes to a new host file and embeds its absolute ASCII path, at most 127
bytes plus terminator. That does not force short reads, failed opens or read errors:
guided CLI checks install those controls explicitly. The synthetic s34-2 context checks
high length bits without claiming an enormous file was actually read.

Use JDK 25 and the pinned MARS build, which supplies address-based byte snapshots
and the firstDescriptor file-fixture option.

Guided checks in checks/construction describe the illustrated intermediate allocation.
Run java -jar /path/to/cli.jar grade checks/construction/s34.kts /path/to/current.asm.
Each check installs the exact declared recipe inputs itself. Pauses inside the routine
are distinct from the normal caller checkpoint main_done before its status store and
results_ready after it. Completed free-writing submissions use checks/kernel.kts,
checks/file.kts, checks/program.kts and checks/reuse.kts. Those checks accept equivalent
working-register choices, buffer workspace contents and smaller positive read requests.
Repeated file calls also verify fresh state, non-default fixture descriptors and
preservation of an existing successful digest when a later call fails. Use the pinned
MARS revision with the firstDescriptor fixture option. The unfinished starter
intentionally fails final correctness checks.

## Publication and scope

The table counts simulated instructions from function entry to supplied return, with
initialization, update and finalization reported separately. It establishes no host
elapsed-time or hardware-cycle ratios. The program is a teaching implementation, not
NIST algorithm certification, a keyed authentication scheme or a production library.

Student/instructor distributions are separate. Actual automated light-theme IDE captures,
remote delivery, enforced hint/progression controls, the deferred author rewrite and
independent accessibility validation remain pending. The four capture entries are plans;
no capture PNGs or coordinate/annotation receipts are claimed.
