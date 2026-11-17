# A load that misses the tick still leads to a timely stop

Let T be the completion callback that reaches the target after returning an older
coordinate from the current load. For the reference polling loop:

| Completion offset from T | Operation | Result |
| --- | --- | --- |
| 0 | Load completes; device reaches target | Loaded register is stale |
| 1 | `bne` | Branches using the stale register |
| 2 | `nop` | Taken-branch delay slot |
| 3 | `lw` | Reloads the target coordinate |
| 4 | `bne` | Falls through |
| 5 | `nop` | Final branch delay slot |
| 6 | `sw` zero to move | Disables motion before its completion callback |

The next movement tick is at T + rate. The permitted rate is at least eight, so no
extra pixel occurs in this worst phase. This argument depends on the shown short
path; adding instructions requires revisiting the bound.
