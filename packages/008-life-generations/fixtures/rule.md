# The complete neighbor-count rule

| Neighbor count | Next state if dead | Next state if alive |
| --- | --- | --- |
| 0 | 0 | 0 |
| 1 | 0 | 0 |
| 2 | 0 | 1 |
| 3 | 1 | 1 |
| 4 | 0 | 0 |
| 5 | 0 | 0 |
| 6 | 0 | 0 |
| 7 | 0 | 0 |
| 8 | 0 | 0 |

The center is not included in the count. The expression is next = (n=3) OR
(current_alive AND n=2). Current state and every neighbor belong to the same old generation.
