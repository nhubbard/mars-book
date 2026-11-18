# Discard low bits, then position each field
Let $R$, $G$ and $B$ be the unsigned component values. The retained fields are
$$
 r=\left\lfloor R/8\right\rfloor,\qquad
 g=\left\lfloor G/4\right\rfloor,\qquad
 b=\left\lfloor B/8\right\rfloor.
$$
They occupy nonoverlapping ranges in
$$
 P=2048r+32g+b.
$$
Unsigned right shifts implement the three divisions. Left shifts by eleven and
five position the red and green fields. Bitwise OR combines them with blue because
the positioned fields do not overlap. An addition also gives the same value for
these established ranges; that equivalence depends on first removing the discarded
bits rather than allowing carries between fields.

For $R=123$, $G=45$, $B=67$, the fields are 15, 11 and 8. Their packed value is
31,080, hexadecimal `7968`. Store bytes `68 79`. The blue field has
five bits and the green field crosses the byte boundary; treating each stored byte
as a complete color component would misinterpret both.

The scalar loop advances its input pointer by three and output pointer by two.
It decrements a remaining pixel count by one. Keep the original count separately
for the return value. Bytes processed, pixels processed and bytes remaining are
different quantities; using the same decrement for the two pointers would lose
alignment with the next pixel.
