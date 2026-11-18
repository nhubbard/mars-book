# Recognize complete byte patterns

A continuation byte has high bits `10`, giving an unsigned range of hexadecimal `80` through `bf`. Leading-byte classes determine the required number of continuations. Four leading-byte values impose an additional restriction on the first continuation. The ranges below follow Unicode 17’s UTF-8 definition.[^1] All byte values in the table are hexadecimal. A dash means that no byte occupies that position.

| Lead     | Next byte | Third byte | Fourth byte |
|:---------|:----------|:-----------|:------------|
| `00--7f` | —         | —          | —           |
| `c2--df` | `80--bf`  | —          | —           |
| `e0`     | `a0--bf`  | `80--bf`   | —           |
| `e1--ec` | `80--bf`  | `80--bf`   | —           |
| `ed`     | `80--9f`  | `80--bf`   | —           |
| `ee--ef` | `80--bf`  | `80--bf`   | —           |
| `f0`     | `90--bf`  | `80--bf`   | `80--bf`    |
| `f1--f3` | `80--bf`  | `80--bf`   | `80--bf`    |
| `f4`     | `80--8f`  | `80--bf`   | `80--bf`    |

A leading-byte classifier must reject a standalone continuation, `c0` or `c1`, and `f5` through `ff`. It must then enforce both bounds on each continuation. Classifying only the number of high leading bits and accepting any `80--bf` tail would admit encodings outside this table.

<div class="MARSWorkedExample">

The value behind three bytes For `e2 82 ac`, remove the structural leading bits. The remaining values are hexadecimal 2, 2, and 2c, or decimal 2, 2, and 44. Their bit positions give
``` math
2\cdot2^{12}+2\cdot2^6+44
 =8192+128+44=8364=\texttt{20ac}_{16}.
```
The scanner can validate and count this encoding without constructing 8364 in a register. Retaining the expected continuation count and next-byte bounds is enough for its interface. A decoder returning scalar values would need additional output and accumulation behavior.

</div>

[^1]: [Unicode 17, Chapter 3, Section 3.9.3, Table 3-7](https://www.unicode.org/versions/Unicode17.0.0/core-spec/chapter-3/). See also [RFC 3629, Section 4](https://www.rfc-editor.org/rfc/rfc3629), for a byte grammar.
