# Convert RGB888 and RGB565 pixels

Implement pack_rgb565 and unpack_rgb565 in the pinned MIPS64 Release 6 environment. Each receives the full source address in a0, full destination address in a1 and pixel count 0..32 in a2. Return the original count in v0. Source and destination allocations are disjoint, and either slice may start at any byte alignment. Preserve every bit of s0..s7, fp and sp, return through the supplied ra and produce no console output. Arguments, temporary registers, vector registers and their overlapping floating-point storage may change.

## Exact conversion

Packed RGB888 has consecutive unsigned R,G,B bytes, three per pixel, with no alpha or padding. RGB565 is a numerical halfword with red at bits 15..11, green at 10..5 and blue at 4..0. Store its low byte first, then its high byte, independently for each pixel.

For input component bytes R,G,B in 0..255, packing computes:

```text
r = floor(R/8)
g = floor(G/4)
b = floor(B/8)
P = r*2048 + g*32 + b
output bytes = P mod 256, floor(P/256)
```

Expansion reconstructs P from low+256*high and computes:

```text
r = floor(P/2048)
g = floor(P/32) mod 64
b = P mod 32
R' = r*8 + floor(r/4)
G' = g*4 + floor(g/16)
B' = b*8 + floor(b/4)
```

This is truncation on packing and bit repetition on expansion. An alternative rounding or interpolation rule does not satisfy this contract, even if the picture looks similar. There is no color-space conversion or gamma correction. RGB (123,45,67) packs to 0x7968, stored bytes 0x68,0x79, and expands to (123,44,66). Maximum fields expand to 255. Packing an expanded RGB565 value returns the same packed value, but the reverse round trip generally changes RGB888. An expanded component may exceed its original; red 128 becomes 132. Code-value error bounds are -7..7 for red/blue and -3..3 for green.

## Slices and ownership

Packing has a source slice of 3*count bytes and a destination of 2*count; expansion exchanges those sizes. All reads from caller data must fit inside the selected source or logical destination. Only logical destination bytes may change. Input, guards, other buffers, preview and metadata are immutable, including temporary stores later restored. No access may cross a logical boundary just because backing allocation bytes are mapped. Repeated reads and writes inside allowed slices are permitted. Zero count performs no caller-data access. Submission-owned constants and private native stack storage are allowed. The supplied RGB arenas have 128 bytes and packed arenas 96, but their padding is not part of the passed slice.

Keep the supplied caller/data harness. Free-writing implementations may replace private labels, register roles, dispatch, vector grouping and algorithms. Behavioral grading accepts equivalent scalar and vector strategies without an exact instruction count, access count or load width. A separately assigned vectorization objective may also need instructor review; output behavior alone does not prove an algorithm's strategy.

## Guided construction

Replace each region's temporary branch and nop with its listed instructions, retaining boundary markers and labels. The two initial vector gate placeholders transfer to the scalar tails. Complete both scalar loops before adding vector blocks. Other unfinished regions return through the appropriate supplied leaf-function exit. Partial observations are construction fixtures, not complete public-contract results. The untouched starter assembles and exits but intentionally fails final grading.

Eight RGB888 pixels occupy 24 bytes. The guided pack loads 16 bytes at offsets zero and eight; both fit that group. It gathers zero-extended channel halfwords, positions their fields and stores 16 packed bytes. The expansion path loads 16 bytes, expands eight halfword lanes, then produces 16-byte output windows at offsets zero and eight. Shared output positions must agree; both windows stay inside 24 output bytes. Counts below eight use scalar byte loads and stores. Each vector gate tests full-group eligibility before accessing the group.

The provided immutable tables initialize shuffle selectors. A vshf destination contains controls before execution and results afterward. Preserve controls separately and copy them each iteration. The native chapter derives selectors from actual loaded windows. Initializer -9 stores raw byte 0xf7; lbu reads it as 247. Signed initializer spelling does not imply a signed color interpretation.

## Caller checkpoints and display

The default count is 32. At pack_ready, v0 is 32, packed_a holds 64 output bytes and packed_count/expanded_count still contain 99. At unpack_ready, v0 is 32, rgb_b holds 96 expanded bytes, packed_count is 32 and expanded_count remains 99. At preview_ready, both count words contain 32 and preview has two rows of 32 display words. The top is original and bottom expanded. The header uses supported 512x64 display dimensions, 16x32 units and static_data base. The preview is the first data object. It is a separate 00RRGGBB word adapter; neither packed input format is directly that word layout. Short counts leave unrendered cells zero in the fresh caller run.

Capture recipes require IntelliJ Light and exact numerical checkpoint assertions. Actual IDE captures and coordinate receipts are still pending, as are the separate interactive lesson runner and policy controls. The native component chart is a conceptual diagram.

## Run the public checks

From the book repository run `python3 tools/course.py check --package 019-rgb-formats --cli /path/to/cli.jar --source /path/to/main.asm --java /path/to/java`. From an extracted package run `java -jar /path/to/cli.jar grade checks/kernel.kts starter/main.asm` and likewise checks/program.kts.
