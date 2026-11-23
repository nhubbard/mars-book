# Reflect pixels within each row

Implement qoi_decode_mirrored in qoi-mirrored.asm. Its supplied body deliberately
contains break. Keep the caller and public interfaces. The complete helpers may
be called or inspected. Choose an equivalent design and temporary registers;
checks judge behavior and memory permissions rather than a source pattern.

| Argument | Meaning |
| --- | --- |
| a0 | First selected encoded byte. The pointer may be unaligned. |
| a1 | Full-width unsigned selected byte count. |
| a2 | Word-aligned framebuffer base. |
| a3 | Full-width unsigned pixel capacity, measured in pixels. |

The local QOI subset has width and height 1–64, channels 3 or 4 and colorspace
0 or 1. It reconstructs every RGBA component. Header dimensions are unsigned
big-endian 32-bit fields. The selected file must contain exactly one valid image,
its eight-byte marker and no trailing bytes. Full-byte RGB/RGBA tags have priority
over the upper-bit RUN class. Reset predictor, all cache entries and prior-INDEX
history independently for every parse and repeated invocation. The main chapter
defines the remaining strict framing rules and transition formulas.

First reject selected counts above 20502 (unsigned) with status 2, and counts
below 23 with status 1, without using input or output pointers. Large and negative
64-bit bit patterns must not become small valid counts through truncation.
For an admitted selected count, prove the complete format before testing output
capacity. Malformed input therefore reports status 1 even when capacity is zero.

| Condition | v0 | v1 |
| --- | --- | --- |
| Complete valid image fits capacity | 0 | (height << 32) OR width |
| Invalid format or selected count below 23 | 1 | 0 |
| Selected count above 20502, or valid image exceeds capacity | 2 | 0 |

Pixel (x,y) goes to index y*width + (width-1-x). Multiply that index by four
when forming the destination byte address. Store the native word 0xAARRGGBB;
increasing little-endian addresses contain B, G, R, A. Reverse each row without
exchanging rows. Preserve all four components. Runs can cross rows, so a direct
emitter computes a reflected position for each produced pixel while its decoder
state still advances in source order. A decode-then-swap design is also valid.

Read only the selected source span. On success, read/write the actual output
pixel span as needed by your design; do not access source padding or output
padding. Keep all source bytes and other caller objects unchanged, even through
same-value writes. On format or capacity failure, do not publish any output
pixel or modify caller objects. Private owned stack space can hold saved state,
cache and scratch data. The enclosing access history has the original finite
200,000-notice allowance; it is not an extra permission to touch caller objects.

Preserve s0–s7, fp and incoming sp at their full 64-bit values. Keep stack
alignment at call boundaries and retain the return path if you call helpers.
No caller stack slots above incoming sp are granted. Do not print. Return to the
provided continuation. The ordinary caller then records status/dimensions and
exits; that continuation and termination are checked as well.

The groups contain 150 dimension/capacity cases, nine chunk/flag cases, 32 source
alignment cases, four malformed files, seven full-width rejected counts, one
ordinary caller and two state-initialization counterexamples. The original
instruction allowances and memory contracts are retained. They accept row swaps,
direct reflection and relocated data objects. These are finite domains, not every
possible stream or every combination of addresses. Start with the first failing
case, state the violated condition, then choose a Debug observation that can
separate an incorrect decoder transition from an incorrect destination address.
