# Two prefix invariants

| Algorithm | Before its next pass | Progress |
| --- | --- | --- |
| Adjacent insertion | Items before outer index i are ordered | Inner index decreases on exchange; outer index increases after insertion |
| Selection | Items before i are the smallest completed set in final positions | Scan index increases; i increases after minimum placement |

Each complete exchange preserves the active multiset. Insertion removes exactly one
inversion per adjacent inverted exchange. Selection requires 28 value comparisons
for eight items and at most seven non-self exchanges in this implementation.
