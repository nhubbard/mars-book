# Trace the default result
The supplied byte slice is `18 - 3 * (2 + -4)`, seventeen bytes long.
The outer subtraction is at offset three, multiplication at seven, addition at
twelve, and unary minus at fourteen. Byte offsets include the spaces. The following
reductions explain the successful result; the parser still has to consume each
required closing parenthesis and check that no trailing token remains.


See the companion worked fixtures for the numerical tables.

On return from `eval_expr`, `v0` is zero and `v1` is 24.
The caller pauses at `expression_ready` before storing those registers.
At that point the memory words `status` and `result` still hold their
initial value, 99. Continue through `expression_continue` before checking
the stored zero and 24. This separates a completed function result from the caller's
later record of it.
