# Equal output from unequal registers

Suppose register A contains `ffffffffffffffff` and register B contains
`00000000ffffffff`. These are unequal 64-bit values, but service 34 prints
`0xffffffff` for either argument. Service 1 prints `-1` for either argument,
and service 36 prints `4294967295` for either argument.

A console-only test cannot distinguish these registers through those services.
The eight-byte result fields can: their high four bytes differ. Check the
precision required by the contract, not just the precision visible in one report.
