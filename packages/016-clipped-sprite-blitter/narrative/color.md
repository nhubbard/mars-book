# Distinguish color from transparency

The Bitmap tool interprets the low twenty-four bits of a stored word as red, green, and blue. This function's transparency rule instead compares the whole word. The default key is hexadecimal 00ff00ff. The word 01ff00ff displays the same RGB color but is opaque because its high byte differs. Black, encoded as zero, is also opaque under this key.

Loading both color and key with `lw` sign extends each word in the same way. Equality of these canonical full-register values is therefore equality of their original 32-bit words, including keys with bit 31 set. Do not mask either operand to its displayed RGB value. Do not reserve zero as a second transparent value.

A transparent pixel leaves the destination untouched. Reading and writing back the old destination value would make the final image look correct, but would violate the no-store rule. That distinction matters for observable memory writes and for tools that react to stores. An opaque pixel whose value already matches the destination still contributes to `v0`; the return value counts selected opaque pixels, not the number of changed words or executed stores.
