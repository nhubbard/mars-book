# Initial, exchange, and final observations

| Main's publication | phase | `swap_count` | Array/frame meaning |
| --- | --- | --- | --- |
| First | 0 | 0 | Original active prefix |
| After each actual exchange | 1 | Previous count + 1 | Complete next algorithm permutation |
| Last | 2 | Unchanged final count | Sorted active prefix |

The renderer does not update metadata. In independent function tests its caller can
leave unrelated metadata values in place; only the frame and declared scratch registers
may change. Each render reaches the same ready return boundary.
