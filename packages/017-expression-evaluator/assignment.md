# Evaluate integer expressions

Implement `eval_expr` for a byte slice addressed by full `a0` with length `a1`, zero through 64. The slice lies inside a supplied 96-byte arena and may be byte-unaligned. Its length is authoritative; no terminating zero is required. Every input load must fit wholly inside that slice. Reading ahead within the slice is allowed when it preserves the specified result and diagnosis. Input, guards and other caller data are read-only, including temporary writes. The selected slice is the only caller-data input: do not read the other arena, metadata or guards. Submission-owned constant tables may be read. Private stack storage is permitted. Preserve all bits of `s0` through `s7`, `fp` and `sp`, return through the supplied `ra`, and produce no console output.

Return status in `v0`: zero for success, minus one for syntax error, minus two for overflow, minus three for division by zero. On success, `v1` is the signed-word value. On failure, `v1` is a zero-based byte offset. An offset equal to length identifies a missing item at the input end.

The grammar is:

```text
expression := term { ('+' | '-') term }
term       := unary { ('*' | '/') unary }
unary      := ('+' | '-') unary | primary
primary    := digits | '(' expression ')'
digits     := one or more ASCII decimal digits
```

Binary operators at the same precedence associate left to right. Unary signs recurse. Space, tab, carriage return and line feed (decimal 32, 9, 13, 10) may occur between tokens. They do not join separated digit sequences. No other whitespace, comments, identifiers, implicit multiplication, hexadecimal, floating-point or multibyte tokens are accepted. An embedded zero byte is invalid input, not a terminator. Leading decimal zeros are allowed.

A decimal literal must be zero through 2147483647. Every intermediate arithmetic result must be -2147483648 through 2147483647. Division truncates toward zero. Check a literal after including each digit, a unary operation after its operand succeeds, and a binary operation after its required right operand succeeds. Reduce same-precedence operations left to right. A group checks its closing parenthesis after its inner expression succeeds. Check complete consumption after the outer expression and final whitespace skip. Stop at the first failure in that procedure; do not replace it with a later error or prevalidate the entire slice to give later syntax errors priority.

A syntax error points at the unexpected byte or the input end if something is missing. Literal overflow points at the first digit whose inclusion exceeds the literal bound. Unary overflow points at that sign. Binary overflow and zero division point at that operator. `2147483647+1?` therefore reports overflow at ten, while `2147483647+1*?` reports syntax at thirteen. `1/(2-2)` reports zero division at one; `1/(2-2` reports syntax at six because the denominator group cannot complete.

`-2147483648` reports literal overflow at ten. `(-2147483647-1)` succeeds with the minimum signed value. Negating that value or dividing it by -1 reports overflow. `(2147483647+1)*0` still fails at the addition.

Keep the supplied caller/data harness for grading. Free-writing implementations may replace the guided helper routines, private labels, stack layouts and register assignments. Equivalent parsing strategies are accepted when they preserve this complete behavior. Grading does not impose the reference instruction count, input traversal order or individual load width.

In guided mode, replace each named region's temporary return with its listed code. Some intermediate routines return provisional values; inspect only the observation for that stage. The unfinished starter exits but intentionally fails the final contract. See the native chapter for the complete explanation and the companion reading files for the exercise transcript. Actual IDE capture and interactive lesson recognition remain pending.

## Run the command-line checks

Use the JDK 25 `java` command and the packaged MARS CLI. From this package folder:

```sh
java -jar /absolute/path/to/mars-cli.jar --warnings-as-errors --assemble-only starter/main.asm
java -jar /absolute/path/to/mars-cli.jar grade checks/kernel.kts starter/main.asm
java -jar /absolute/path/to/mars-cli.jar grade checks/program.kts starter/main.asm
```

The original starter assembles and exits but is expected to fail the final checks.
Complete the guided regions or write an equivalent function, then run both scripts.
The kernel script tests the callable interface; the program script also checks the
supplied caller's checkpoint and stored results. Assembly success alone does not
establish evaluator correctness.
