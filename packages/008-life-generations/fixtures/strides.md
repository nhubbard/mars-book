# One logical board, three physical representations

| Allocation | Cell width | Physical row stride | First logical cell |
| --- | --- | --- | --- |
| seed | 1 byte | 8 bytes | seed+0 |
| `board_a` or `board_b` | 1 byte | 10 bytes | board base+11 |
| frame | 4 bytes | 32 bytes | frame+0 |

Interior logical offset is (y+1)×10+(x+1) in a board, y×8+x in seed, and
4×(y×8+x) in frame. Do not reuse one representation's stride for another.
