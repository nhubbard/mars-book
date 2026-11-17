# Word-format service contracts

| Selector in `$v0` | Argument | Representation |
| --- | --- | --- |
| 1 | Low word of `$a0` | Signed decimal |
| 36 | Low word of `$a0` | Unsigned decimal |
| 34 | Low word of `$a0` | 0x plus eight lowercase hexadecimal digits |
| 35 | Low word of `$a0` | Thirty-two binary digits, no prefix |

These requests produce no automatic line ending. Service 4 prints the supplied
prefix and newline strings. The register may hold 64 bits, but these four
formatting contracts describe a word.
