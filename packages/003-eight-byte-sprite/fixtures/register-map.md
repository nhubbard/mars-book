# Register roles in the illustrated program

| Register | Role |
| --- | --- |
| `$t0`, `$t1` | Current source row and exclusive source end |
| `$s0`, `$s1` | Frame row base and reversed-byte output pointer |
| `$t3` | Original unsigned row mask |
| `$t9`, `$t8` | Consumed source copy and reversed accumulator |
| `$t4` | Reversal count, then column bit mask |
| `$t5`, `$t6` | Extracted/masked bit and selected RGB color |
| `$t2`, `$t7` | Next original and mirrored destination words |

Reusing `$t4` is safe because the reversal count is no longer needed when the
column mask is initialized. These roles describe the illustration, not a graded
register allocation.
