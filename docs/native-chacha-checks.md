# Native ChaCha20 checks

The revised Chapter 22 unit retains original project `026-chacha20`. Its four
completed interfaces are the quarter round, raw scalar block, raw MSA block and
counted XOR wrapper. `tools/chacha_lesson_cases.py` authors the existing 73 public
cases as native function format 15. `tools/chacha_original_checks.json` binds all six original scripts by SHA-256
and case count; the tooling tests reject a stale source grid. The author does
not generate book composition or
recognize implementation source. Connected native lesson composition and a new
first/resume desktop walkthrough remain unfinished.

The original domains remain: 12 quarter-round cases over four word patterns and
three nonadjacent/permuted layouts; four arbitrary raw states for each block
entry; 37 XOR cases spanning empty input, exact tails, in-place operation,
full-width count rejection and final-counter admission; 11 supplied-caller cases;
and five same-session reuse cases. Original 100,000- and 1,000,000-instruction
allowances are unchanged. Nine profiles satisfy the existing ten-million total
instruction and one-MiB serialized-data limits.

The original case grid has 84 observed function invocations. The native corpus
has 95 calls because each supplied-caller case first executes the real `main`
dispatch to its original call boundary. The next native call runs that boundary
to `main_done`, retaining the original callee observation window. Caller setup
reads are therefore not folded into wider callee permissions. All eleven caller
cases additionally require actual exit and the stored status after `main_done`.

Each invocation retains complete before/after snapshots of source (4160 bytes),
destination (4160), state (128), quad (96) and four controls (20 total). Eight
per-object policies retain the original selected read/write windows. The separate
250,000-notice history retains the exact enclosing source-through-status span,
including candidate-owned gaps without assigning those gaps to the caller.
Per-object histories also retain the original explicit 250,000 bound. Saved
s0–s7/fp values, initial temporary values, the eight-byte aligned stack and full
unsigned arguments retain their original values. Byte arrays use increasing
address order and raw blocks add their original sixteen words modulo 2^32.

Format 15's `setupMemory` installs bounded label-relative bytes before a call's
arguments and observations. This is essential for changing the data at an address
between invocations in the same simulator. It preserves private state, other
memory and the cumulative instruction budget. Host setup is excluded from
execution-only access/store histories, and caller-stack snapshots stay active.
Earlier formats reject the new field. The public byte editor now also accepts a
live pause, consistently with word editing, while rejecting terminal execution.

The host arithmetic author uses four dependent add/XOR/rotate groups and ten
column/diagonal double-rounds. Its output matches the retained printed quarter
and block examples. Actual exports from the original JDK-backed checker match
all 73 cases and 84 invocations in the worked, alternative and wide layouts:
full setup, arguments, expected snapshots, budgets and access windows agree.
Three correct designs pass the translated corpus; all eight retained semantic
faults and the original unfinished starter fail. Native caller completion and
silence are additional requirements beyond the original observation window.
These are finite correctness checks, not cryptographic certification or learner
validation. Private qualification records remain in the instructor repository.
