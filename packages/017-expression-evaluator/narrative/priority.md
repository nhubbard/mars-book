# Define which failure wins
The evaluator stops at the first failure in the following procedure. A literal is
checked as each digit is included. A unary operation is checked after its operand
has succeeded. A binary operation is checked after its right operand at the required
precedence level has succeeded. Same-precedence binary operations are checked as the
left-to-right loop reduces them. A group requires its closing parenthesis after its
inner expression succeeds. The outermost call requires complete consumption after
a successful expression and final whitespace skip. Once a failure is established,
return it without replacing it with a later diagnosis.

For `2147483647+1?`, the right term supplies one and stops before the question
mark. The addition then overflows at byte ten. The complete-input check that would
reject the question mark has not been reached. For `2147483647+1*?`, however,
the right term cannot supply a value: multiplication still needs an operand at byte
thirteen. That syntax error occurs before the outer addition can be attempted.

For `1/(2-2)`, the grouped subtraction succeeds and its closing parenthesis
is present; division then detects zero at byte one. Removing the closing parenthesis
changes the input to `1/(2-2`. The group fails at end of input, byte six,
before it can return a denominator. The required result is therefore a syntax error,
not division by zero. These rules are an exercise-specific interface; evaluating a
prefix does not certify that the entire original byte slice was syntactically valid.

An alternative parser may use an operator stack or a precedence threshold instead
of separate expression and term loops. It still has to reproduce these outcomes.
A preliminary validation pass that always gives a later syntax error priority over
an earlier arithmetic failure would implement a different interface.
