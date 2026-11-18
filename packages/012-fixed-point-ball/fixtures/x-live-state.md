# Track the live and published versions of a state

The guided main keeps its live values in saved registers. The axis updater and renderer borrow temporary registers, then return without disturbing those live values. Publication copies the live state to named output words for observation.

| Guided register | Live role               | Published observation            |
|:----------------|:------------------------|:---------------------------------|
| `$s0`           | Raw x position          | `x_raw`                          |
| `$s1`           | Raw y position          | `y_raw`                          |
| `$s2`           | Raw horizontal velocity | `vx_raw`                         |
| `$s3`           | Raw vertical velocity   | `vy_raw`                         |
| `$s4`           | Remaining updates       | Input request minus current tick |
| `$s5`           | Current tick            | `tick`                           |
| `$s6`           | Rounding mode           | Read-only `round_mode` input     |
| `$s7`           | Full main frame pointer | Configured Bitmap Display base   |

Between the two axis calls and the next publication, registers can already hold an updated state while the memory outputs still describe the last published tick. That is an intermediate implementation state. The ready boundary is placed after both axes, metadata, and rendering are coherent, so a reader does not mistake a half-updated snapshot for the next simulation state.
