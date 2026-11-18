# Reach the minimum value through an operation
The sign is an operator, separate from the digit token. Consequently
`-2147483648` contains a decimal literal that is already too large before
unary minus can run. The failure is literal overflow at byte ten, the final digit.
The expression `(-2147483647-1)` reaches the minimum value through valid
steps: read an in-range literal, negate it, then subtract one. That complete expression
succeeds with $-2^{31}$.

Applying another unary minus to that grouped result must overflow. Do not simplify
pairs of minus signs before evaluating their operands: an inner operation can fail
even if an algebraically simplified final value would fit. Similarly,
`(2147483647+1)*0` fails at the addition. Multiplication by zero does not
cancel the obligation to check the earlier intermediate result.

These choices distinguish this language from a general mathematical calculator and
from other programming languages' literal rules. The implementation and grader use
this explicit domain. Extending it to a special signed-minimum literal, hexadecimal
input or wider result type would require a revised contract and revised diagnostic
examples.
