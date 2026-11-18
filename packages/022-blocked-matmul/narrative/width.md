# Keep partial sums exact

A minimum-times-minimum product is 1073741824. Two such products sum to 2147483648 and sixteen sum to 17179869184. Each individual product fits a signed word, but the complete sum needs 36 signed bits. Sign-extend inputs and retain full accumulator/output width.

The full directly edited composition is in `native/width.tex`.
