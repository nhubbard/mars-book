# Reverse 0x16 through eight iterations

| Completed iterations | Consumed-source copy | Reversed accumulator | Remaining count |
| --- | --- | --- | --- |
| 0 | `16` | `00` | 8 |
| 1 | `0b` | `00` | 7 |
| 2 | `05` | `01` | 6 |
| 3 | `02` | `03` | 5 |
| 4 | `01` | `06` | 4 |
| 5 | `00` | `0d` | 3 |
| 6 | `00` | `1a` | 2 |
| 7 | `00` | `34` | 1 |
| 8 | `00` | `68` | 0 |

Source and accumulator values are hexadecimal. The three final shifts remain
necessary after the source copy becomes zero.
