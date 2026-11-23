# An expression owns its evaluation order

Read the expression companion after the nested-data chapter. This unit assumes bounded byte access, signed-word representation and full-width function frames. The task is to implement a specified language, including its first diagnostics.

Start with worked/arithmetic.asm. It receives operands as numbers, computes 3*(-2), then 18-(-6), and stores the results. Predict each returned pair and inspect the caller. In worked/overflow.asm the first operands become 46341 and 46341; compare the wide candidate with its sign-extended low word before interpreting the returned error offset. Both examples are complete; neither parses text. worked/arithmetic.kts checks 256 operand/operator combinations plus the first complete caller.

Implement eval_expr in independent/expression.asm. Replace its one break body while keeping the caller, arenas and public observation labels. Choose your own private helpers, parser strategy and frame layout. a0 is a full input pointer and a1 is a length from zero through 64. Read only the selected slice, keep all caller data read-only even temporarily, preserve full s0-s7/fp/sp and return through the incoming ra. Private tables and a correctly restored stack are permitted. Produce no console output.

The companion defines precedence, left association, recursive unary signs and parenthesized groups. Literals stop at 2147483647; every intermediate value must fit signed 32 bits. Only the specified ASCII digits, punctuation and four whitespace bytes are admitted. The input is not a zero-terminated string. Return status 0/value on success, or syntax -1, overflow -2 or zero-division -3 with a zero-based byte offset. An offset may equal length when something is missing at the end.

Check successful evaluation first (251 original cases), then exact diagnostics (289 original cases). Their union retains all 540 original direct-call trials. Six additional public recovery cases make three calls each in one machine and alternate arenas. Twenty caller variants distinguish setup, actual return, publication and exit. All four assessments use the same continuing source; hints and progression follow instructor policy.

Debug the default caller at expression_call, expression_ready and expression_stored in one session. It passes seventeen bytes and returns (0,24), then publishes the pair to memory. A correct stored pair does not excuse an incorrect return or a forbidden evaluator store. Finish with ordinary Run, whose expected transcript is empty.

Retain and explain a left-associated chain, unary signs, separated digit sequences, both question-mark examples and both denominator groups from the companion. Call again after an error with a different slice. Explain the first failed requirement, accurate byte offset, slice preservation and balanced caller state for each example.
