# Use counterexamples to diagnose a search

| Observed defect | Property to inspect |
|:---|:---|
| Cells 7 and 8 become connected | Coordinate bounds must precede flattened indexing. |
| The same cell appears twice in queue history | Mark on discovery, before a later dequeue. |
| Goal distance is correct but its parent is unrelated | Parent must be the cell that performed the first discovery. |
| Final route omits the start | Mark before the start test, then stop before reading its sentinel. |
| Old orange cells remain after processing | Redraw the entire frame with the current classification arrays. |
| Input is restored after an unauthorized store | A protected execution-store trace checks history as well as final bytes. |

Each defect has a concrete counterexample. Run it on a small fixture so the failure can be traced to an instruction and a state change rather than only to the final picture.
