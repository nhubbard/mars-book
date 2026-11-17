# The input register before and after a request

| Boundary for input 20 | `$v0` | Meaning |
| --- | --- | --- |
| Before the input syscall | `5` | Read-integer service selector |
| After the input syscall returns | `20` | Returned integer |
| Before the next string syscall | `4` | Print-string service selector |

The transition explains why a later service setup can overwrite an input or
result that remains only in `$v0`.
