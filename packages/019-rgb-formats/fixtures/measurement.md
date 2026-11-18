# Complete-call native work

| Pixels | Scalar pack | MSA pack | Scalar expand | MSA expand |
|---:|---:|---:|---:|---:|
| 0 | 5 | 8 | 5 | 8 |
| 1 | 23 | 26 | 30 | 33 |
| 7 | 131 | 134 | 180 | 183 |
| 8 | 149 | 54 | 205 | 55 |
| 9 | 167 | 72 | 230 | 80 |
| 16 | 293 | 76 | 405 | 86 |
| 31 | 563 | 224 | 780 | 292 |
| 32 | 581 | 120 | 805 | 148 |

Counts include complete function entry, table setup, groups, tails and return; exclude caller preparation, assembly, session creation, preview and exit. They are not elapsed-time claims or grading requirements. At eight pixels, MSA pack reads 32 observed source bytes instead of 24 due to overlap; MSA expansion writes 32 observed bytes instead of 24. Each vector transfer publishes per-byte notices in the pinned core, not host bus transactions. Thirty-one pixels can need more work than thirty-two because of the seven-pixel scalar tail.
