# Prove the whole transfer fits the slice
Eight RGB888 pixels occupy 24 bytes, while two vector loads would transfer 32 bytes
if placed back to back. Instead load sixteen bytes at offset zero and sixteen at
offset eight. Their union is precisely the 24-byte input block. The eight-byte
overlap is legal because both accesses stay inside the selected source slice.
It also means a byte appears in two source registers, so shuffle selectors must
identify the chosen copy consistently.


See the native chapter for the described conceptual diagram.


The block guard is therefore a logical requirement: at least eight pixels must
remain before either load. A mapped arena can hide an overread because no native
address exception occurs. The grader checks all bytes in each observed access
against the selected slice. Allocated padding does not become input merely because
a vector transfer could reach it.

Full pointer arithmetic still matters. Add byte displacements to the full input
and output addresses with doubleword address arithmetic. The small counts do not
justify narrowing the bases. The same code is tested with mapped data above four GiB.
