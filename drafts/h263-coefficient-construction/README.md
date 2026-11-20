# Complete baseline block coefficient payloads

The authoritative native composition is in `coefficient-events.tex`,
`coefficient-contract.tex` and `coefficient-construction.tex` (steps 81–90).
This stage emits actual baseline block syntax, not a complete H.263 stream.

Complete `h263_emit_coefficients` in a copy of `main.asm`, then replace the writer
stub with your completed `h263_write_bits` and following helpers. The provided
scan and 102-entry compact-code table are normative numeric data; `codes.json`
retains official-source provenance. `tables.asm` is also included as supplied data
in the ordinary caller. It has no emitter solution. Each row stores LAST, RUN,
magnitude, full code with zero sign bit, and declared width including sign.

The 32-byte request is eight-byte aligned and contains four native-endian
doublewords: level pointer, writer-context pointer, INTRA selector 0/1 and reserved
zero. Levels are 64 tight natural-order signed halfwords, aligned two. Ordinary
levels are [-127,127]; INTRA index zero instead holds code 1..255 excluding 128.
Request/context/levels and output are disjoint, mapped and non-wrapping. Context
is the earlier aligned 24-byte structure with capacity at most one MiB and used
count within its bits. Preserve saved GPRs/SP, input/request and context pointer/
capacity. Supply aligned SP and private space; vector registers are caller-saved.

Validate metadata before level reads, then validate the complete domain before
output access. Invalid request/context/level returns status one/count zero, with
level-validation reads allowed after valid metadata. A nonfitting chosen plan
returns two/zero before caller-buffer reads. Child failure returns three/zero;
private staging may have read caller bytes, but no caller buffer or count writes
are allowed. An unfinished entry reports development status four.

Successful output appends one complete legal block at the retained cursor,
returns zero/appended-bit-count and updates the used count. Prefix/suffix bits
outside that payload remain unchanged. Empty INTER emits nothing and performs
no buffer access/context write; INTRA always emits its eight-bit DC code. The
entry emits no macroblock presence flags, padding or file writes.

Compact codes and baseline escapes can encode the same event. Capacity admission
uses the complete chosen plan. With at least 22*N plus any eight INTRA DC bits
available, every legal choice fits and must succeed. Less space may fit a compact
plan but reject an escape-only plan; no private exact-byte requirement is imposed.
The decoder compares the recovered levels instead. Width, LAST, run bounds,
forbidden levels and complete consumption are checked independently.

```sh
python3 drafts/h263-coefficient-construction/prepare.py \
  --source my-coefficients.asm --intra 1 --pattern worked \
  --capacity 192 --used 3 --output worked.asm
python3 drafts/h263-coefficient-construction/grade.py \
  worked.asm --java /path/to/java --cli /path/to/mars-cli.jar \
  --report coefficient-results.json
```

Preparation preserves all instructions and code tables, changes data/controls
only, and refuses existing output. Reports also need a new path. At
`coeff_caller_return`, before its first instruction, output/context/return
registers are complete and named status/bit fields have not yet received copies.
The compact worked INTRA block prints 16; the valid escape-only version prints 52.
Both exit zero and decode to DC 255, level +1 at natural index one and -1 at eight.

The 100 public cases decode every emitted level and check committed length,
input/context/buffer/stack guards, read/write bounds, high addresses, both native
byte orders, maximum capacity and rejection. Private cases add every compact table
entry with both signs and block types. Equivalent code choices and private frames
remain valid. `model.py` is a host bit-string encoder/decoder for independent
experiments; it does not generate a target solution.

The controlled work fixture in the book uses the same byte-fragment child for
both code choices. Compact versus escape-only complete guest instruction counts
are 3206/2952 for the worked block and 12861/18550 for a dense alternating-sign
AC block, while emitted sizes are 16/52 and 199/1394 bits respectively. Fewer bits
do not guarantee less work in a sparse linear lookup. These are finite instruction
observations, not whole-encoder timing. The exact dense fixture is in public
`check.kts`; the preparation helper's dense pattern is all-one ordinary levels.

Numeric code/scan facts follow ITU-T H.263 (02/1998), clauses 5.4.1/5.4.2,
Tables 15–17 and Figure 14; the chapter links the official recommendation.
Macroblock flags, motion/type syntax, picture ownership, full-stream quality/
interoperability/performance, output failures, distributions and actual IDE figures
remain subsequent work. Capture automation stays in the separate authoring plugin
depending on MARS.
