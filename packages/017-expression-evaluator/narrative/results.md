# Separate success values from error positions
The meaning of `v1` depends on `v0`. On success it is a signed
integer result; on failure it is an unsigned, nonnegative byte offset no greater
than the supplied length. Inspect the status before interpreting the second result.
The value minus two in `v0`, for example, is an overflow status rather than
an expression result. A successful expression can itself evaluate to minus two,
which is then returned in `v1` with a zero status.

A syntax error points at the unexpected byte, or at end of input if a required
operand or closing parenthesis is absent. A decimal-literal overflow points at
the first digit whose inclusion makes that literal exceed its maximum. Overflow
from a unary operation points at its sign byte. Overflow or division by zero from
a binary operation points at that operator's byte. These positions describe the
operation that failed; they need not be the rightmost byte that was examined.


See the companion worked fixtures for the numerical tables.
