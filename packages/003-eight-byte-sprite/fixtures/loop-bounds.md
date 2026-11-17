# Count the work at each loop boundary

| Boundary | Completed work | Next position |
| --- | --- | --- |
| End of reversal | 8 source bits | Reversed byte ready to store |
| End of column loop | 8 pairs, or 16 pixel words | Current frame row complete |
| After row increment | 1 source byte and 1 reversed byte | Next row, 64 frame bytes later |
| End of outer loop | 8 source rows, 8 output bytes, 128 pixel words | Exclusive source end and frame boundary |

The memory footprint is 512 frame bytes plus 8 reversed-row bytes. Source data
and canary objects are additional storage and must retain their values.
