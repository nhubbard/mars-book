# The same word under two byte orders

For the numerical word `0x00336699` at address `A`:

| Byte order | At `A` | At `A + 1` | At `A + 2` | At `A + 3` |
| --- | --- | --- | --- | --- |
| Little-endian | `99` | `66` | `33` | `00` |
| Big-endian | `00` | `33` | `66` | `99` |

All entries are hexadecimal bytes. The numerical word and its RGB fields are
the same in both rows.
