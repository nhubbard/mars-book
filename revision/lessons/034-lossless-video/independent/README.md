# Minimal exclusive changed-pixel bounds

Replace the deliberate `break` at `video_change_bounds`. The supplied main is a
small demonstration; its existing arrays and workspace are also used by the
public feedback. Keep the public labels available. Your function may use private
labels, constants, registers and storage without matching a reference source.

Arguments are previous-image pointer a0, current-image pointer a1, result pointer
a2 and settings pointer a3. Settings contain two aligned native unsigned 64-bit
values, width and height. Each must be one through 64. This numerical primitive
accepts those smaller dimensions; the serialized MLV1 format itself remains
fixed at 64 by 64.

Each input contains exactly width times height native 32-bit words in row-major
order. Both pointers require four-byte alignment. They may exactly coincide
because both inputs are read-only. Compare all 32 bits, including a high-bit-only
difference; this function does not validate a serialized pixel's RGB high byte.
Do not impose an extra sixteen-byte vector-alignment requirement.

Settings and result require eight-byte alignment. Their selected extents are
sixteen and 32 bytes respectively. They are disjoint from one another and from
both image intervals. Among caller-owned objects, read only settings and the
selected image words. Write only the selected result; the result is not readable
scratch. Caller bytes outside that result must retain their values. Private
storage outside those objects remains available. Exact aliases of the read-only
images do not imply permission for output overlap or partial output aliases.

On success return zero in v0 and the number of unequal words in v1. Publish four
native 64-bit values `(x0,y0,x1,y1)` at a2. Each changed position must be inside
the rectangle. Its left/top coordinates are the minima of all changed columns
and rows; right/bottom are one beyond their maxima. Thus right/bottom are
exclusive ends. If no words differ, publish four zeros and return count zero.
A rectangle's area is not its changed-word count.

Reject invalid dimensions or any required alignment with v0 one and v1 zero.
Test settings alignment before loading settings. Reject other invalid inputs
before reading any image word or changing any caller byte. Use full unsigned
64-bit dimensions; a value such as 0000000100000001 hexadecimal is not width one.
There is no I/O in this entry.

Preserve every bit of s0-s7, fp, sp and the supplied 64-bit return continuation.
The incoming stack is sixteen-byte aligned. Repeated calls share one simulator
and one cumulative million-instruction case budget, including retained private
state. The feedback admits equivalent one- and two-pass scans and does not grade
an exact instruction sequence.

The demonstration is width two and height three, with changes at indices one
and four. Correct output is `(0,0,2,3)` and count two. Pause at
`bounds_demo_return` to inspect v0/v1 and the result before main stores its status
and count. A quiet Run alone does not verify the returned bytes or ownership.

The eight immutable public profile files retain all 182 original cases in both
byte orders, including 192 total calls. Four staged checks support implementation,
then the final assessment reruns every case on this same source. Editing extracted
profile copies does not replace the expectations retained from the imported
archive. Renew source-bound evidence after edits. Instructor-only solutions and
expanded checks are distributed separately.
