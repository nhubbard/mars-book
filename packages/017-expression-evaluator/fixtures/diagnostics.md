# Diagnostic positions

| Input | Status | Offset | Reason |
|---|---:|---:|---|
| `1 2` | -1 | 2 | Unexpected second literal |
| `(1` | -1 | 2 | Missing closing parenthesis |
| `1+` | -1 | 2 | Missing operand |
| `2147483648` | -2 | 9 | First excessive literal digit |
| `2147483647+1` | -2 | 10 | Addition overflow |
| `1/(2-2)` | -3 | 1 | Zero denominator |
