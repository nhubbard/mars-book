# Check the representations before writing the loops
## Exercise: A stored halfword
Pack the RGB888 pixel $(123,45,67)$. Write the numerical RGB565 value in hexadecimal,
then its two bytes in increasing address order. Expand those bytes and state which
original components changed. Explain why reversing the stored bytes would be a
representation error rather than a different rounding rule.

## Exercise: A vector load near the end
Seven RGB888 pixels remain. There are many mapped bytes after the logical slice.
May the next eight-pixel block execute its two vector loads? State the complete
read ranges for a legal block and explain the scalar tail's role.

## Exercise: An upward expansion error
Red code 128 loses its low bits when packed, yet expands to 132 under this contract.
Derive both values. Is an assertion that every expanded channel is at most its
original value valid? Give the error bounds for five-bit and six-bit channels.

## Exercise: The next block's controls
Why must the destination controls of `vshf.b` be reloaded or copied before
the next use? Describe a test length that can distinguish one correct block from
a loop that accidentally reuses its first result as controls.
