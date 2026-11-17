# RGB fields in one word

| Bit positions | Hexadecimal field | Meaning in this example |
| --- | --- | --- |
| 31 through 24 | `00` | Unused by Bitmap Display |
| 23 through 16 | `33` | Red = 51 |
| 15 through 8 | `66` | Green = 102 |
| 7 through 0 | `99` | Blue = 153 |

Each hexadecimal digit represents four bits. A pair represents one byte.
