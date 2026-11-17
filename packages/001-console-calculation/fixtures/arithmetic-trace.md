# Trace the two arithmetic instructions

| Boundary | Input `20` | Input `-3` |
| --- | --- | --- |
| After the read | `$v0 = 20` | `$v0 = -3` |
| After `addu` | `$t0 = 40` | `$t0 = -6` |
| After `addiu` | `$t0 = 41` | `$t0 = -5` |

A breakpoint on `addu` stops before the second row has been produced. The next
two source steps reveal the second and third rows.
