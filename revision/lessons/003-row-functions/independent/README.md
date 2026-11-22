# Write reverse_and_invert

The supplied main aligns its stack, calls the routine for input 0x16, retains the
returned 0x97 in s0, then calls it for 0x81 and retains 0x7e in s1. The two labels
after_invert and after_repeat are different continuations. These caller writes to
saved registers occur after the callee’s preservation obligation for that call.
The break body marks the unfinished routine; replace it before expecting a
successful ordinary Run.

The routine receives an arbitrary 64-bit value in a0. Only its low byte matters.
Use the provided reverse_byte helper, invert the eight result bits, and return a
value from zero through 255 in v0. Preserve s0–s7, fp, gp and the incoming sp, and
return through the incoming continuation. The helper may change temporary,
argument and result registers. Keep the original outer continuation available if
you make another call. Equivalent frame layouts and correct tail transfers are
accepted; no exact instruction count or private scratch register is required.

The native checker calls this function directly. It loads the source into a fresh
machine for each public case, initializes full-width saved-register patterns,
aligns the supplied caller pointer to sixteen bytes, and captures 32 bytes at
and above that pointer. It sets a0 and ra, starts at reverse_and_invert, and pauses
before the requested continuation. The stores and exit service in main do not run
during these direct checks. After each return it compares v0, the restored pointer,
saved registers, caller bytes and silent output, and requires actual completed
execution in the helper’s public instruction interval. Byte comparisons are made
after returns, not represented as a complete write history.

Each of the three cases makes 256 calls in that case’s machine, covering every
low byte. They use positive, sign-bit-set and all-one upper argument patterns,
different saved values and different stack locations. Return sites alternate.
Memory and scratch state persist between calls within a case; cases use separate
machines. The cumulative budget is 100000 instructions per case. Three passing
public cases therefore represent 768 returned calls, not three sample inputs.

The adjacent data profile is the native content contract; the generated Kotlin
script expresses the same case grid through the existing CLI grading DSL:

java -jar /path/to/cli.jar grade reverse-and-invert.kts reverse-and-invert.asm

Instructor policies may control access, prerequisites, attempts and hint release.
These are public formative checks; private instructor variations remain separate.
