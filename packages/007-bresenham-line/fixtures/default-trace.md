# The six-sample default trace

| Painted point | err before update | e2 | x step? | y step? | Next point |
| --- | --- | --- | --- | --- | --- |
| (1,1) | 2 | 4 | Yes | Yes | (2,2) |
| (2,2) | 4 | 8 | Yes | No | (3,2) |
| (3,2) | 1 | 2 | Yes | Yes | (4,3) |
| (4,3) | 3 | 6 | Yes | No | (5,3) |
| (5,3) | 0 | 0 | Yes | Yes | (6,4) |
| (6,4) | 2 | Not evaluated | No update | No update | Exit |

Use dy = −3 and dx = 5 for both tests. The last row exits after painting, so its
possible doubled error is irrelevant to control flow.
