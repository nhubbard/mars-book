# Separate availability from validity

The input length is part of the function’s authority to read. Suppose the caller passes an address $`p`$ and count $`n`$. A read of $`w`$ bytes beginning at $`q`$ is permitted when its entire interval fits:
``` math
q\geq p,\qquad w\leq n,\qquad q-p\leq n-w.
```
The subtraction form avoids constructing an overflowing end address in a general checker. This exercise bounds the count to 256 and places the complete interval inside the supplied arena. The assembly can compare small lengths before forming payload addresses. Preserve the full 64-bit pointer when advancing through bytes.

A read beginning inside the interval can still cross its end. With two bytes available, a four-byte load at the input pointer is too wide. Alignment is a separate issue: a byte load can read an unaligned pointer safely, while an ordinary word load also needs suitable alignment. Passing an address check does not establish that a particular load width or alignment is appropriate.

Mapped memory and caller-supplied input are different boundaries. An overread may successfully retrieve padding from the data segment. Its success at the hardware level does not make the read valid for this function. Final registers and memory can look correct after such an overread. The exercise checks native data-access addresses and widths as well as final results. Those checks observe the input arena and its guards; they do not forbid unrelated legitimate stack or constant reads outside that region.

The count describes bytes already available to this call. The parser does not wait for more data or perform input syscalls. A return of `-1` lets a caller retain the prefix and retry when more bytes arrive. The same prefix may later become a valid packet or a definite format error. Do not infer validity from incompleteness.
