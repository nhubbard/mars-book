# Restore triples from expanded lanes
Expansion loads eight packed halfwords from sixteen bytes. It extracts red, green
and blue fields and performs the same bit repetition in eight lanes. Each expanded
component then occupies a halfword whose high byte is zero.

The even-byte pack operation selects byte indices zero, two, four and so on. Applying
it with the same register as both inputs places the eight low component bytes in
the low half of the result and repeats them in the high half. Interleaving the
low halves of red and green creates the sixteen-byte sequence
$R_0,G_0,R_1,G_1,\ldots,R_7,G_7$. Blue remains in its original halfword arrangement.
Two final byte shuffles can select triples from these two sources.

Store output bytes zero through fifteen from the first shuffle, then bytes eight
through twenty-three from the second. The shared positions eight through fifteen
must have identical values in both results. Both stores stay within the 24-byte
output slice. Repeated writes inside that slice are permitted; writing extra bytes
outside it and restoring them later is forbidden.

This construction uses 32 byte stores as observed by the simulator for 24 logical
output bytes. A reduction in native instructions therefore need not reduce observed
byte accesses. Keep those measurements separate when comparing the implementations.
