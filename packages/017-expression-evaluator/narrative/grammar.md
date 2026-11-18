# Specify precedence in the grammar
The grammar uses four named rules. An expression contains terms separated by plus
or minus. A term contains unary operands separated by multiplication or division.
A unary operand is either a sign followed by another unary operand, or a primary.
A primary is a decimal literal or a parenthesized expression.


See the companion worked fixtures for the numerical tables.

The grammar gives multiplication and division higher precedence than binary plus
and minus: a term finishes its multiplicative work before returning a value to the
expression routine. A unary sign binds to its operand before that operand is used
by a binary operator. Parentheses let a primary contain a complete expression,
regardless of the precedence level outside the group.

The repetition in the expression and term rules is evaluated left to right.
For `1-2-3`, start with one, subtract two to obtain minus one, and then
subtract three to obtain minus four. For `8/3/2`, the first quotient is
two and the second is one. Right association would produce a different computation.
Unary signs recurse instead: in `--5`, the inner minus produces minus five
and the outer minus produces five. Repeated signs do not form a separate decrement
or increment token in this language; `1++2` means one plus unary-positive two.
