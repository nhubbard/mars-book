# Reference register meanings after setup

| Register | Meaning | Default |
| --- | --- | --- |
| `$s0`, `$s1` | Current x, y | 1, 1 |
| `$s2`, `$s3` | Ordered final x, y | 6, 4 |
| `$s4` | dx, nonnegative horizontal difference | 5 |
| `$s5` | dy = negative vertical magnitude | −3 |
| `$s6` | sy, signed y step | +1 |
| `$s7` | Running err | 2 |
| `$t2` | Saved e2 during an update | 4 on first update |
| `$t8` | Fixed frame base | 0x10010000 |
| `$t9` | Full color word | 0x00ffffff |

Earlier setup boundaries give `$s5` a signed difference and then a nonnegative magnitude.
Only after error initialization does it have the negative convention in this table.
The scratch address and comparison registers have shorter lifetimes.
