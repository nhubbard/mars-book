# Find the first broken numeric relationship

Record the input raw position, velocity, trial, reflected result, and selected cell before changing the instruction that appears wrong. A pixel alone omits fractional position and direction, so several distinct errors can look alike.

| Symptom | Relationship to inspect | Useful first boundary |
|:---|:---|:---|
| Wall looks correct, next step wrong | Exact-contact velocity direction | Axis function return |
| Ball sticks to or pauses at a wall | Overshoot preserved instead of clamped | Trial and reflected raw position |
| Slow motion disappears | Fractional state retained between ticks | Live raw register after update |
| Modes follow different raw paths | Quantization confined to rendering | Raw words at the same tick |
| Old white points remain | Complete frame describes current state | All 64 words at ready |
| Default frame works, spare fails | Destination argument used as full pointer | Independent renderer call |
| Final image right, sequence wrong | Initial and updated publications counted | Every ready marker through exit |
| Overflow lab has unexpected sign | Operation width and signed input extension | Probe return before exit |
| Restored input write passes visual inspection | Actions obey read-only contract | Execution-store history |

A lower instruction count cannot repair one of these failures. First restore the required state and memory effects, then repeat the measurement with the same input and stopped boundaries.
