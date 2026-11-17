# A one-byte offset changes another field

If the byte store used `1($t0)` in little-endian memory, it would replace the
green byte. The result would be `0x0033ff99`, with bytes `99 ff 33 00`.
A store at `4($t0)` would reach the neighboring word instead of the pixel.

Predict both cases before experimenting in a separate copy. Restore offset zero
before running the assignment checks. A color change alone does not establish
that the intended field was changed.
