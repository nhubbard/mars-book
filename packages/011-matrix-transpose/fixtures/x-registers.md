# Keep coordinates and addresses in different roles

The guided ordinary function uses a pair of row starts plus a pair of advancing inner pointers. The tiled function recomputes the inner pointers at each tile row. These are explanatory conventions, not required register choices.

| State | Ordinary walkthrough | Tiled walkthrough |
|:---|:---|:---|
| Current cell | `$t0` row, `$t1` column | `$t4` row, `$t5` column |
| Tile bounds | No tile state | `$t0`, `$t1` origins; `$t2`, `$t3` exclusive ends |
| Source pointer | Row start `$t2`; inner pointer `$t3` | Inner pointer `$t6` |
| Destination | Row’s first destination `$t4`; inner pointer `$t5` | Inner pointer `$t7` |
| Destination stride | `$t6`=4R | `$v1`=4R |
| Source row stride | `$t7`=4C | Recomputed row start from $`iC+j_0`$ |
| Copied word | `$t8` | `$t8` |
| Return count | `$v0`=RC | `$v0`=RC |

A register has no permanent source-language type. Its role follows from the code that defines and consumes it. Mark each quantity as a coordinate, an exclusive bound, a byte stride, a full address, or a data word when tracing. Adding a column index directly to a pointer omits the four-byte scale. Adding a byte displacement twice scales the address twice. Neither error is corrected by the cache.
