# Reproduce a word overflow outside the motion bounds

The bounded ball update does not overflow. To study overflow itself, use a separate raw-sum probe with two arbitrary signed 32-bit inputs. Compute the wrapped word result and the exact sum in a wider register. Both results still use the same scale of 256; widening the arithmetic does not move the binary point.

Choose hexadecimal `7fffff00` and `00000200`. They represent 8,388,607 pixels and two pixels. Their mathematical raw sum is
``` math
2{,}147{,}483{,}392+512=2{,}147{,}483{,}904.
```
That sum is hexadecimal `80000100`. It exceeds the greatest positive signed 32-bit raw integer. Interpreting only those low 32 bits as a signed word gives $`-2{,}147{,}483{,}392`$, or $`-8{,}388{,}607`$ pixels, instead of the correct positive 8,388,609 pixels.

| Operation    | Raw result in hexadecimal | Signed raw interpretation |
|:-------------|:--------------------------|:--------------------------|
| Wrapped word | `80000100`                | $`-2{,}147{,}483{,}392`$  |
| Wider sum    | `0000000080000100`        | $`2{,}147{,}483{,}904`$   |

The word operation `addu` keeps the low 32-bit result without an overflow exception. In the 64-bit register view, that word result is sign-extended to `ffffffff80000100`. The upper ones do not recover the lost positive carry. The doubleword operation `daddu`, applied to the correctly sign-extended input values, retains the exact sum for this probe’s domain. The sum of two signed 32-bit integers fits in a signed 64-bit integer, even when it does not fit in a word.

The letter u in `addu` does not turn the result into an arbitrary precision number. It selects addition without the signed-overflow exception. The trapping word instruction `add` is a separate experiment: with these same operands it reports an arithmetic overflow rather than returning the wrapped word result. Keep the expected fault out of the ordinary animation run.

The probe must receive signed inputs as full register values. A negative word loaded with sign extension is suitable for the wider sum. Zero-extending the same negative word would turn it into a large positive input before the addition. Likewise, computing a wrapped word sum first and widening that result afterward cannot recover the exact sum. Widen the operands before the operation that needs the wider range.

# Choose a numeric policy before optimizing it

For the ball, the domain proof justifies the small word operations. For a larger world, longer update interval, or larger velocity, decide whether to reject an out-of-range input, widen the representation, saturate, or deliberately wrap. Those policies produce different programs. A wider accumulator also needs enough room for the largest intermediate operation, not merely the final displayed cell.

Changing the scale changes another tradeoff. More fractional bits represent smaller increments, but leave less signed range in a fixed-width word. Fewer fractional bits provide more range while discarding smaller motion. A renderer cannot reconstruct fractions that the state update has already thrown away.

<div class="MARSExercise">

Separate the three effects Describe one change that alters only the displayed cell, one that changes the raw trajectory, and one that causes a word overflow outside the ball’s input bounds. For each, name the register or memory observation that would distinguish it from the unchanged baseline. Keep host execution time separate from simulated step count.

</div>
