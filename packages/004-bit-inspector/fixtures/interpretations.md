# Interpret selected word patterns

| Word pattern | Signed decimal | Unsigned decimal |
| --- | --- | --- |
| `00000000` | 0 | 0 |
| `00000001` | 1 | 1 |
| `7fffffff` | 2147483647 | 2147483647 |
| `80000000` | -2147483648 | 2147483648 |
| `ffffffff` | -1 | 4294967295 |

Word patterns are hexadecimal. For a high-bit-set word with unsigned value U,
the signed value is U − 2^32. The bytes do not change when choosing a column.
