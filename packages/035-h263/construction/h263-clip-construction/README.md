# Complete H.263 clip I/O

Native `clip-io.tex`, `clip-construction.tex` and `clip-workflow.tex` are authoritative.
Steps 111–120 develop the complete student-facing file driver around the completed
picture entry. The chapter now has 120 observations/quizzes. Complete
`h263_encode_clip` in your working source and carry your own complete picture group
forward with `compose.py`; custom organization may be composed manually.

The 104-byte aligned request contains thirteen native-endian doublewords: input
path, encoded-stream path, reconstruction-trace path, picture count, clock step,
quantizer, arithmetic mode, raw buffer, reconstruction A, reconstruction B,
picture workspace, encoded-picture buffer and reserved zero. The two reconstruction
buffers and raw input each have 38016 active bytes; workspace is sixteen-byte
aligned and supplies 169088 bytes; encoded storage supplies 131072 bytes. Supply
aligned SP and 65536 private stack bytes. All required extents are mapped,
non-wrapping and disjoint, including request/result/path windows and private stack.

Count is 1..299, clock step 1..255, quantizer 1..31 and mode 0/1. Require
count*step*1001 <= 300000 after admitting each bounded factor. Each nonempty path
has a NUL within its mapped 4096-byte window. Validate controls/extents, then scan
paths and reject equal names by contents before any open. Preserve request/path
bytes, saved GPRs and incoming SP. Working buffers may change after valid admission.
The helper uses printable ASCII paths for the current byte-oriented syscall ABI.

An aligned 24-byte result contains accepted-picture count, accepted encoded-byte
total and last accepted reconstruction pointer. Invalid metadata returns one/zero
with this record unchanged and no file/data-buffer operations. After valid
admission initialize it to zero. Each complete input picture invokes the tested
picture encoder. Write all encoded bytes and all reconstruction bytes using actual
positive short-write counts, then promote the new reference and publish progress.
A failed later operation must retain the previous accepted reference samples.
Require exact EOF after the requested picture count and close every owned descriptor
on every path. Descriptor numbers are returned values, not fixed constants.

Return zero/accepted count on success, two/count on open failure, three/count on
read error/incomplete picture/trailing data, four/count on picture-child failure
or invalid returned byte count, five/count on output failure. Development status
seven marks the unfinished entry. A failed run may leave file prefixes or even an
unaccepted complete encoded picture; its nonzero status means the clip is unfinished.
Progress is useful evidence, not a replacement for success status.

Use bounded nonempty read/write requests of at most 4096 bytes. The current
educational write syscall reads one extra guard byte. The worked helper stages a
chunk plus a guard in private scratch storage, without adding the guard to the
file or reading beyond caller picture storage. Equivalent exact-transfer helpers
and private layouts are accepted.

```sh
python3 drafts/h263-clip-construction/compose.py \
  --source my-clip.asm --picture my-picture.asm --output joined-clip.asm
python3 drafts/h263-clip-construction/grade.py \
  joined-clip.asm --java /path/to/java --cli /path/to/mars-cli.jar \
  --report clip-feedback.json
python3 drafts/h263-clip-construction/run.py \
  joined-clip.asm --java /path/to/java --cli /path/to/mars-cli.jar \
  --output-dir run-eight
```

The 74 public cases use real simulated file syscalls and isolated fixture files,
including short I/O, missing paths, descriptor exhaustion, early EOF, input errors,
trailing input and independently targeted output failures. They decode every
accepted macroblock, repeat motion search and verify reconstructed pixels using
the actual accepted reference. They also check progress, reference retention,
cleanup, guards and saved state in both byte orders. Alternative valid entropy
codes, accurate transforms, bounded I/O chunk sizes and private frames remain valid.
These checks require the grader API added in MARS commit `6c66f101`, including
`provideFiles(..., failWriteAfterBytesByPath = ...)`; older grader archives lack it.
Standalone kernel and inverse-accuracy gates remain required.

The run helper requires a new directory, creates deterministic raw input and
metadata with Python, prepares your actual instructions and launches MARS. It then
uses the complete syntax inspector and independent FFmpeg decoder, reporting exact
source/input/tool hashes, quality, drift and finite guest work. It never uses a
host encoder to supply the submitted output. Use `prepare.py` alone for an IDE run.
At `clip_caller_return`, before its first store, return registers/progress are
complete while named caller status/count fields have their old values. A successful
default caller prints eight. Re-running manually in the same IDE working source
writes its declared output files again; prepare a new directory to retain earlier
experiments.

Both completed eight-picture drivers independently decode. Reference/alternative
sizes are 2212/5101 bytes and guest counts 95583213/98072067, including guarded
writes and cleanup. Maximum local/decoder difference is two sample values each.
Fresh complete CLI observations are about 7–8 seconds with uncontrolled host load.
An actual 44-picture, 8.8088-second reference clip also independently decodes across
temporal-reference wraparound. These are whole-process observations, not isolated
core or IDE launch measurements. Final chapter archives and actual light-mode IDE
figures remain subsequent work. Screenshot automation stays in the separate
authoring plugin depending on MARS. Independent accessibility review and later
substantive editing remain separate.
