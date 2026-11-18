# Make final input an explicit event

With final flag zero, an unfinished but still possible prefix is retained and the call returns active status zero. An empty nonfinal chunk supplies no new bytes and must preserve that prefix. A caller can later finalize by passing an empty chunk with final flag one. If need is zero, finish successfully. Otherwise, record a truncation at the absolute end position. There is no offending byte at that end.

| Return | Stored status | Meaning |
|---:|:---|:---|
| 0 | 0 | Chunk accepted; more input may follow |
| 1 | 1 | Final input ended between complete scalar encodings |
| $`-1`$ | $`-1`$ | A byte cannot extend the accepted UTF-8 prefix |
| $`-2`$ | $`-2`$ | Final input ended inside an otherwise possible encoding |
| $`-3`$ | Unchanged | The state was already terminal on entry |

The first impossible byte is not accepted: neither total nor the call’s consumed count includes it. A final flag does not change that error into truncation. The scanner stops at its first error and supplies no replacement text. A later call returns minus three without reading input or changing the stored diagnostic, even if its new bytes happen to be valid.

A terminal status belongs to this stream object. The function still returns normally and the caller can continue running, select another initialized state, or later exit the program. Terminal stream status is not a simulator halt.

An active error call records the accepted prefix and terminal diagnostic. Its state is therefore intentionally updated on error. This differs from the prior queue’s failed-operation contract, which preserved its earlier state. Specify an interface’s error effects rather than assuming that every function shares the same policy. Only calls that enter with an already terminal state are free of state writes here.

During construction, the supplied return stub makes incomplete increments stop, but they do not yet implement the completed interface. Early observations describe live temporary values. The final publication increment writes all state fields, then replaces the temporary diagnostic in `v1` with the current call’s accepted-byte count. Run final behavioral grading after that increment is complete.
