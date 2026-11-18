# Check the language before writing the loops
## Exercise: Precedence and association
Evaluate `9-6/2-1`. State the successive reductions and the final value.
Which interpretation follows the grammar?

- **A** Reduce `9-6` before dividing.
- **B** Reduce `6/2`, then perform the two subtractions left to right.
- **C** Reduce the rightmost subtraction before the leftmost one.


## Exercise: Error position
For `1/(2-2`, choose the required status and offset.

- **A** Division by zero at byte one.
- **B** Syntax error at byte six.
- **C** Success with value zero.

Explain which parser call cannot yet return a successful value.

## Exercise: Intermediate range
Explain why `(2147483647+1)*0` fails even though simplifying the mathematical
expression gives zero. Identify the operator byte that must be reported. Then give
a valid expression for the minimum signed word and a unary expression that overflows
when applied to that value.
