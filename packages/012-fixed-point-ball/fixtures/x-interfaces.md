# Read the function interfaces independently

| Function | Arguments | Results and permitted effects |
|:---|:---|:---|
| `advance_axis` | `$a0` raw position; `$a1` raw velocity | `$v0` position; `$v1` velocity; provided static objects unchanged |
| `render_ball` | `$a0` destination; `$a1`, `$a2` raw x, y; `$a3` mode | `$v0` pixel ID; destination frame written; other provided objects unchanged |
| `add_fixed_probe` | `$a0`, `$a1` signed word inputs as full register values | `$v0` sign-extended wrapped word; `$v1` exact doubleword sum; provided objects unchanged |

All three preserve the full contents of `$s0` through `$s7` and restore `$sp`. The caller can supply different return continuations, so return through the link value rather than a global exit label. Temporary and argument registers may change. The probe’s operand domain is wider than the motion domain; keep those numeric promises separate.
