# Ordering complete endpoint pairs

| Supplied endpoints | Canonical A | Canonical B | Reason |
| --- | --- | --- | --- |
| (6,4), (1,1) | (1,1) | (6,4) | Compare x |
| (3,7), (3,0) | (3,0) | (3,7) | Equal x, compare y |
| (1,6), (6,2) | (1,6) | (6,2) | Smaller x already first; negative y direction is retained |
| (4,4), (4,4) | (4,4) | (4,4) | Same point |

Pseudocode: swap the pairs when x1 < x0, or when x1 = x0 and y1 < y0. A pair swap
needs both x and y exchanges. Never store the reordered values back into the input.
