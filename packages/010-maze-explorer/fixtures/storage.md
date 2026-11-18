# The public work arrays

All six byte arrays below have 64 entries and four-byte-aligned bases. Their elements are still bytes; alignment does not change the stride. The frame contains 64 words.

| Array | Per-cell meaning | Values and initialization |
|:---|:---|:---|
| `walls` | Input occupancy | 0 open, 1 wall; never written by execution. |
| `states` | Discovery status | 0 unseen, 1 frontier, 2 processed; initialize to 0. |
| `parents` | First predecessor | Cell ID 0 through 63, or 255; initialize to 255. |
| `distance` | Shortest edge count | Finite 0 through 63, or 255; initialize to 255. |
| `queue` | Discovery history | Cell IDs in the used prefix; initialize the full array to 0. |
| `path` | Final route membership | 0 or 1; initialize to 0. |

The six arrays include the input maze. Five are working arrays initialized by the program. Words `head`, `tail`, `current`, `phase`, `result`, and `length` describe progress. These words require word loads and stores.
