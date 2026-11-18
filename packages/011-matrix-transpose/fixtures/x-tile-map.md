# See the clipped partition

Tile A covers source columns 0..3 across all three rows. Tile B covers column 4 only.

| Row | Tile A indices | Tile B index |
| --- | --- | --- |
| 0 | 0, 1, 2, 3 | 4 |
| 1 | 5, 6, 7, 8 | 9 |
| 2 | 10, 11, 12, 13 | 14 |

A visits source indices 0,1,2,3,5,6,7,8,10,11,12,13 and output indices 0,3,6,9,1,4,7,10,2,5,8,11. B fills output indices 12,13,14. No padding cell is part of the matrix.
