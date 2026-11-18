# Interpret each public checkpoint

| Phase | Stable boundary | Result and route length |
|:---|:---|:---|
| 0 | Initialized start, before any dequeue | Both words are minus one. |
| 1 | One dequeue and all required discoveries completed | Both words remain minus one, including the goal-removal checkpoint. |
| 2 | Complete path or exhausted search, with final redraw | Success: result 1 and shortest edge count. Failure: result 0 and length minus one. |

A successful search publishes the goal-removal state before reconstructing the path. The final publication therefore has its own occurrence even when the queue is empty. The path mask stays zero until reconstruction. At phase two its only nonzero entries are the chosen route, or none on failure. Execution exits normally afterward.
