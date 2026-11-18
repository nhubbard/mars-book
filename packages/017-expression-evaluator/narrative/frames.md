# Retain one unfinished operation per call
Consider `18-3*(2+-4)` without whitespace. The outer expression retains its
left value, eighteen, while the right term evaluates the multiplication. The term
retains three while the primary evaluates its group. Within that group, the expression
retains two while unary minus evaluates four. A nested call cannot borrow the caller's
live temporary registers without preserving the values the caller still needs.

The guided design uses shared parser registers for the input base, cursor, length,
status and error offset. They describe one parse and deliberately change as helper
calls consume input. Stack slots hold values that belong to one invocation: its
return address, its accumulated left value, and a pending operator and operator
position. A nested expression has its own slots for the same roles. Restoring a
call frame must not roll back the shared cursor or discard the first error.

The public entry preserves the caller's saved registers before assigning those
registers parser roles. Each nonleaf helper preserves its own incoming return address
before another call overwrites `ra`. Store and reload all sixty-four bits.
A small input does not make a return address a small integer. The final restore must
recover the original stack pointer and caller registers on success and on every error
path, including errors several calls deep.


See fixtures/frames.md for the conceptual call-frame trace.
