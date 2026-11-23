# Complementary PPM serializer

Implement ppm_encode_negative in ppm-negative.asm. Preserve the supplied caller,
helpers and public data labels. Inputs are full framebuffer pointer a0, packed
height/width in a1, full destination pointer a2 and unsigned byte capacity a3.
Width occupies bits 0–31 and height bits 32–63; both must be in 1..64.

Each input pixel is the integer word 0xAARRGGBB. Ignore AA. Emit the canonical
ASCII header P6\nW H\n255\n, then consecutive bytes 255-R,255-G,255-B per pixel.
Width and height use ordinary decimal without leading zeroes. The smallest
header has eleven bytes; each two-digit dimension adds one. The required extent
is the header length plus 3*W*H bytes, at most 12,301. Append no NUL byte.
The source words are read-only. Objects are disjoint, source words are aligned,
and destination offsets may be arbitrary byte offsets.

Check dimensions first. An invalid width or height returns status one in v0 and
zero in v1 without any caller-buffer access. Then check the entire serialized
extent against unsigned capacity. Insufficient capacity returns status two and
zero length, also without any caller-buffer access. Both pointers can be invalid
on these failure cases. A high-bit-set capacity is a large unsigned capacity;
using a signed comparison changes the contract.

On success return status zero and the exact byte count. Read only the selected
pixel words and exact accepted output slice, and write only that output slice.
Neither rejected output nor source words may be written temporarily and restored.
This includes same-value writes outside the permitted output. Private constants
and private stack storage are separate from the watched caller arenas. Preserve
all bits of s0 through s7, fp and incoming aligned sp, restore your private frame
and return through the supplied continuation. Perform no console I/O. The direct
checks initialize sp at the assembled end of the 4,096-byte workspace, including
when the supplied solution uses wide data addresses.

The default caller passes red, green, blue and white. Predict cyan, magenta,
yellow and black. At negative_return, v0=0 and v1=23; destination contains the
eleven-byte two-by-two header and twelve complementary samples. Caller status
and count still contain −1. At function_return, those objects contain zero and
23. The subsequent exit service replaces v0 with ten.

The original ppm-negative.kts retains 167 public cases: 160 dimension/capacity
trials, six invalid dimensions and the complete default caller. Native function
data preserve all 166 direct constructions, including every original instruction
allowance, offset, pixel, alpha byte, unsigned argument and invalid pointer.
The caller is checked before and after publication in a separate two-call case;
a normal silent-exit assessment retains its three-million-instruction allowance.
Four function groups cover 80 one-digit-width trials, 80 two-digit-width trials,
six invalid dimensions and the caller. Their 57 bounded files are grouped behind
four assessment actions, followed by exit. Function-profile version 11 and
console-profile version 4 preserve the original larger allowances without changing
the ten-million limit per file. This native delivery does not execute the Kotlin
script as downloaded code.

Explain the channel order, unsigned preflight and exact permitted accesses.
The finite cases cover selected dimension, offset, capacity and pixel patterns;
they do not exhaust all possible images. After checking, use the written lab's
run_revision_ppm.py --negative command on your completed source to produce and
verify a real complementary image. Free writing with the supplied script remains
available without the interactive lesson. Instructor policy controls progression,
assessment attempts and graduated hints.
