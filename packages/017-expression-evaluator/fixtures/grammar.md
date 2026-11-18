# Grammar and operator ownership

Expression contains terms separated by + or -. Term contains unary operands separated by * or /. Unary is a sign followed by unary, or primary. Primary is decimal digits or a parenthesized expression. Binary repetitions fold left to right; prefix signs recurse. A term leaves a plus byte unconsumed; a primary group consumes its own closing parenthesis after its inner expression succeeds.
