# Bounded MSB-first bit output

The native includes `bit-output.tex` and `bit-writer-construction.tex` contain the
authoritative exposition and steps 71–80. Complete `h263_write_bits` in a copy of
`main.asm`. This is a bit-packing kernel, not a complete H.263 stream encoder.

Pass an eight-byte-aligned 24-byte context in a0, an unsigned right-aligned value
in a1 and width 0..32 in a2. Context doublewords are buffer pointer, byte capacity
0..1048576 and committed bit count 0..8*capacity, in native byte order. Extents
must not wrap. Positive-capacity storage is mapped, byte-addressable and disjoint
from context. Zero-capacity storage is never accessed. Preserve saved GPRs/SP,
buffer pointer/capacity and every output bit outside the appended window.

Require value < 2^width, so zero-width requires value zero. Validate all full-width
metadata before output accesses. Invalid metadata returns status one/count zero;
insufficient remaining capacity returns two/zero. Both retain context and buffer
and perform no buffer accesses. A valid empty append returns zero/zero and makes
no context writes. Successful nonempty appends replace exactly their field bits,
MSB first, then commit used+width and return zero/width. Access only touched bytes;
wider reads/stores into neighbouring storage are not allowed by this exercise.

The first transmitted bit occupies bit seven of its byte. Native byte order of
the context does not change stream byte order. Unused low bits of a partial byte
are retained and are not stream data until explicitly appended. Syntax callers
must request required padding, not insert padding after every ordinary field.

```sh
python3 drafts/h263-bit-writer-construction/prepare.py \
  --source my-writer.asm --first 5 --first-width 3 \
  --second 419 --second-width 9 --output crossing.asm
python3 drafts/h263-bit-writer-construction/grade.py \
  crossing.asm --java /path/to/java --cli /path/to/mars-cli.jar \
  --report writer-results.json
```

Preparation changes field controls and the declared capacity within the provided
eight-byte arena; it retains instructions and refuses existing files. Feedback
reports also need a new path. The default caller appends fields 101 and 10011,
commits eight bits, prints first byte 179 (B3) and exits zero. At
`writer_caller_return`, before the first instruction, v0 is zero, v1 is five and
used count is eight; named status/count fields have not yet received their copies.
The crossing variant commits twelve bits and leaves BA 35. An explicit four-zero
append yields BA 30 and sixteen committed bits. Only committed bytes/bits belong
to the stream; this function does not write files.

The 160 public cases check representative widths, every starting bit offset, both
native byte orders, zero/all-one fields, exact fits, insufficient capacity, empty
writes, high addresses, the one-MiB endpoint and wide invalid controls. Independent
bit-list grading accepts both per-bit and byte-fragment schedules and different
private frames. Private cases cover every width and zero/one/mixed value patterns.

For a named all-one 32-bit append at bit three, the tested byte-fragment function
executes 189 instructions versus 656 for per-bit work, including validation and
commit. This is finite function work, not wall time or whole-encoder throughput.
No MSA use is required for this ordered scalar operation.

Field-level preflight does not make several calls atomic as a group. A coefficient
or macroblock writer needs its own total-space validation or staging. Coefficient
syntax, picture ownership, complete stream/output grading and actual chapter figures
remain subsequent work. Screenshot automation remains in the separate authoring
plugin depending on MARS.
