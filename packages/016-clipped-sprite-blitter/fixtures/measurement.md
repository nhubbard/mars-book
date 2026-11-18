# Measured traversal work

Columns are placement x/y, complete-function native instructions for the preclipped
reference and per-cell alternative, source-read notices, and destination-store notices.

| x | y | Preclipped | Per-cell | Source reads | Stores |
|---:|---:|---:|---:|---:|---:|
| -2 | 1 | 227 | 446 | 12 | 8 |
| 0 | 0 | 308 | 604 | 20 | 13 |
| 6 | -2 | 126 | 332 | 4 | 2 |
| -1 | 4 | 170 | 434 | 8 | 6 |
| 8 | 0 | 44 | 306 | 0 | 0 |

The final two columns agree between these implementations. Counts include function
entry, restore and return, excluding caller setup, assembly, session creation, tool
rendering and exit. They are simulator work counts, not cycles or elapsed-time results.
Descriptor reads and private stack accesses are excluded from the source/store columns.
Equivalent correct programs may have different counts; grading does not require these
numbers. A store-eliding equivalent still counts every visible opaque source cell.
