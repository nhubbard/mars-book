# A word result in a 64-bit register

For the in-range result −5, the low word is `0xfffffffb`. Sign extension
copies bit 31 into the upper 32 bits, yielding `0xfffffffffffffffb` in the
64-bit register. For positive 41, the full register is `0x0000000000000029`.

The `addu` and `addiu` operations in this example produce word results. Their
`u` suffix selects non-trapping signed-overflow behavior; it does not select
zero-extension. The input domain keeps this program away from overflow.
