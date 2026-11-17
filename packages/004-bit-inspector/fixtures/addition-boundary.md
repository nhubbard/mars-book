# Cross the signed-word boundary

| Original signed value | Word-plus-one result | Doubleword-plus-one result |
| --- | --- | --- |
| -1 | 0 | 0 |
| 2147483646 | 2147483647 | 2147483647 |
| 2147483647 | -2147483648 | 2147483648 |

At the final row, the full word result is ffffffff80000000 and the full
doubleword result is 0000000080000000. Both have the same low word.

The doubleword column starts from the signed load. Starting from the unsigned
load would be a different calculation: 4294967295 plus one is 4294967296 for
the all-one word. Its low word is zero, so a low-word check would miss that error.
