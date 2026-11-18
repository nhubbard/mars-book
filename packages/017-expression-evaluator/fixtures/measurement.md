# Complete parser-call work

| Scene | Loop instructions | Threshold instructions | Loop input reads | Threshold input reads |
|---|---:|---:|---:|---:|
| Single literal | 174 | 158 | 4 | 4 |
| Default expression | 1062 | 1050 | 49 | 45 |
| 31 binary plus signs | 6281 | 6520 | 283 | 281 |
| 63 unary minus signs | 3450 | 3434 | 130 | 130 |
| 31 nested groups | 6194 | 5248 | 315 | 253 |
| `2147483647+1?` | 537 | 514 | 25 | 23 |
| `1/(2-2` | 616 | 603 | 24 | 22 |

Counts cover the complete function, excluding setup, assembly, session creation and exit. Input reads exclude private stack accesses. They are simulator work, not cycles or elapsed-time claims. The addition chain has thirty-two ones; the unary scene has sixty-three minus signs and one; the group scene encloses one in thirty-one pairs. Equivalent correct implementations are not graded by these counts.
