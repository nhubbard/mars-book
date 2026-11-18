# Read the frame without relying on color names

| Classification   | RGB word   | Meaning and priority                      |
|:-----------------|:-----------|:------------------------------------------|
| Wall, dark       | `0x202020` | Wall byte is 1; overrides all work flags. |
| Route, blue      | `0x0072b2` | Open cell with path byte 1.               |
| Unseen, white    | `0xffffff` | Open cell with path 0 and state 0.        |
| Frontier, orange | `0xe69f00` | Open cell with path 0 and state 1.        |
| Processed, green | `0x009e73` | Open cell with path 0 and state 2.        |

The frame is a rendering of the arrays. The arrays and this legend supply the text representation when colors are hard to distinguish. In the final small-room frame, IDs 0, 1, and 9 are route cells, ID 8 is processed, and all remaining IDs are walls.
