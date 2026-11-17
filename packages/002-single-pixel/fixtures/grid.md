# Map four words to four cells

Let `A` be the configured base address. The two-column grid uses row-major order:

| Cell | Address | Drawing operation in this project |
| --- | --- | --- |
| Column 0, row 0 | `A` | Word store, then byte update |
| Column 1, row 0 | `A + 4` | No runtime write |
| Column 0, row 1 | `A + 8` | No runtime write |
| Column 1, row 1 | `A + 12` | No runtime write |

The reserved neighbor is at `A + 4`. Its initial bytes exist in memory, but this
program performs no store to it. The display's write-driven view should not be
used as a substitute for inspecting the neighbor in memory.
