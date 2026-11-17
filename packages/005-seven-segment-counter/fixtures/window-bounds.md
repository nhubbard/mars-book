# Derive valid lookup positions

For start f and count n, valid selections satisfy f ≥ 0, n ≥ 0, and f + n ≤ 16.

| Window | Selected indices | Cursor after completion |
| --- | --- | --- |
| f = 0, n = 16 | 0 through 15 | glyphs + 16 |
| f = 14, n = 2 | 14 and 15 | glyphs + 16 |
| f = 5, n = 1 | 5 | glyphs + 6 |
| f = 16, n = 0 | None | glyphs + 16 |

The last row forms an exclusive-end pointer but does not dereference it in the
reference program. An empty request must not become a do-while iteration.
