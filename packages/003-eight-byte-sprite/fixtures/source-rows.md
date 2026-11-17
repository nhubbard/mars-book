# The starter row patterns

| Row | Declaration | Unsigned byte | Binary, leftmost bit first |
| --- | --- | --- | --- |
| 0 | `22` | `16` | `00010110` |
| 1 | `60` | `3c` | `00111100` |
| 2 | `126` | `7e` | `01111110` |
| 3 | `-37` | `db` | `11011011` |
| 4 | `-1` | `ff` | `11111111` |
| 5 | `36` | `24` | `00100100` |
| 6 | `66` | `42` | `01000010` |
| 7 | `-127` | `81` | `10000001` |

Unsigned-byte entries are hexadecimal. A one bit means white. The signed
initializer −37 and unsigned value 219 describe the same byte pattern db.
