# What the private cases cover

| Family | Arrays | Algorithms per array |
| --- | --- | --- |
| Lengths 0..5 over {−1,0,1} | 364 | 2 |
| All two-item pairs over −4..3 | 64 | 2 |
| Twenty-four deterministic fixtures at each length 0..8 | 216 | 2 |
| Named sorted, reversed, shuffled, equal and duplicate cases | 9 | 2 |
| Total | 653 | 1,306 cases per implementation |

Some families overlap; the table counts fixtures, not unique global arrays. The
ternary-prefix and pair domains are exhaustive. All length-eight arrays over the
full eight-value domain are not exhausted. Independent renderer calls add 17 cases
with varying pointer offsets and lengths and preserved 64-bit caller canaries.
