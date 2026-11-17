# Measured instruction count for one fixture

| Implementation | Default endpoints | Completed simulated instructions |
| --- | --- | --- |
| Incremental error reference | (1,1) to (6,4) | 145 |
| Rounded rational alternative | (1,1) to (6,4) | 138 |

Counts include setup, pseudo-instruction expansion, loop work, and normal exit on the
pinned MARS CLI. Both programs produce the same image. No cycle latency or host runtime
is inferred from the seven-instruction difference. These totals are reference-fixture
evidence, not a grading limit or a promise for other endpoint pairs.
