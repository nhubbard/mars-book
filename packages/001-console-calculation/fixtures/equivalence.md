# Compare two correct arithmetic sequences

For an allowed input `x`, compare the intermediate ranges:

| Sequence | First intermediate | Final result |
| --- | --- | --- |
| `x + x`, then add 1 | −2000 through 2000 | −1999 through 2001 |
| `x + 1`, then add `x` | −999 through 1001 | −1999 through 2001 |

Both remain within the signed 32-bit range. Explain why their final values are
equal, then run the public checks on your alternative. Keep the same prompts,
line ending, and normal termination.
