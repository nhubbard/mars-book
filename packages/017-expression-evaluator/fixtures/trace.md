# Default byte offsets and reductions

The default slice `18 - 3 * (2 + -4)` has seventeen bytes. Unary minus at fourteen yields -4; plus at twelve yields -2; multiplication at seven yields -6; subtraction at three yields 24. At expression_ready the return registers are 0 and 24 while status/result memory still holds 99. Continue past expression_continue before expecting the stored 0 and 24.
