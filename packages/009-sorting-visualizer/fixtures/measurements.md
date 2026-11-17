# Reference measurements with explicit scope

| Input | Algorithm | Whole instructions | Range instructions | Range branches | Taken | Not taken | Swaps |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Sorted | Insertion | 2,853 | 118 | 30 | 16 | 14 | 0 |
| Sorted | Selection | 3,262 | 527 | 115 | 79 | 36 | 0 |
| Reversed | Insertion | 41,276 | 1,077 | 107 | 44 | 63 | 28 |
| Reversed | Selection | 8,750 | 663 | 115 | 59 | 56 | 4 |
| Shuffled | Insertion | 26,197 | 716 | 79 | 33 | 46 | 17 |
| Shuffled | Selection | 10,111 | 686 | 115 | 65 | 50 | 5 |

Sorted is [−4,−3,−2,−1,0,1,2,3], reversed its reverse, and shuffled is the default
[2,−2,3,−4,0,1,−3,−1]. Range means `sort_begin` inclusive through `sort_end` exclusive,
with callee bodies outside it excluded and in-range call/metadata work retained.
Runs include no backstep. Counts are forward native events, not cycles or host time.
Exact totals describe the pinned reference, not a free-writing grading target.
