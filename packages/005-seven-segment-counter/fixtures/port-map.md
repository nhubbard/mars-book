# Digital Lab byte ports in the Default layout

| Address | Role | This program |
| --- | --- | --- |
| `0xffff0010` | Right display mask | Writes selected glyph bytes |
| `0xffff0011` | Left display mask | No writes |
| `0xffff0012` | Keypad row and interrupt control | No writes |
| `0xffff0013` | Counter enable | No writes |
| `0xffff0014` | Keypad result | No writes |

These are MARS peripheral addresses. The source header connects the view; it does
not turn a word store into a byte transaction.
