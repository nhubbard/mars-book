# Trace the byte update

| Boundary in little-endian memory | Word at `pixel` | Bytes in increasing address order |
| --- | --- | --- |
| Before the first store | `0x00000000` | `00 00 00 00` |
| After `sw` | `0x00336699` | `99 66 33 00` |
| After `sb` | `0x003366ff` | `ff 66 33 00` |

At every boundary, the neighboring word must retain its initial value. The
final `lw` copies the third row into a register without changing these bytes.
