# Records across a circular boundary

Read the ring-buffer companion lab after snapshots and reusable storage. You
will use byte addressing, preserved function state and completed-call observations.
The new work separates physical positions from logical ownership, accounts for a
record's prefix, and makes failed operations leave caller data unchanged. Use
MIPS64 Release 6, Default memory and little-endian byte order.

The questions appear inline in Learn. Predict before executing, and use feedback
to explain a wrong answer. Source pointers identify instructions for discussion;
a highlight does not establish that the debugger is paused there.

## Verify one cursor step

`worked/cursor.asm` is complete. Its caller passes index six and capacity seven
to `advance_cursor`. At the helper's `addiu`, the candidate has not been calculated.
At `bnec`, it is seven, so the branch must fall through to the zero assignment.
At `cursor_done`, v0 is zero and the caller's store is still pending.

Stop that Debug session before opening `worked/cursor-eight.asm`. It changes only
the capacity to eight. At the branch, the same candidate seven is now valid. The
branch bypasses the zero assignment and the helper returns seven. Explain why
capacity is an input, and why a bit mask cannot replace the comparison for every
permitted capacity.

Run `worked/cursor.asm` normally after the observations. It stores zero to
`cursor_result` and exits silently. The debugger establishes the returned value;
the empty console transcript establishes normal exit. `worked/cursor.kts` retains
the separate original checks: all 528 valid index/capacity pairs and the complete
caller. The written companion explains why a whole-record jump needs a different
bound from a one-byte increment.

## Implement the record functions

Open `independent/ring.asm`. The data and fourteen-command caller are complete;
replace the deliberate breaks in `push_record` and `pop_record`. Keep the public
allocations, caller, callable labels and marker. Choose your own private helpers,
registers, successful store order and copy direction. Read `independent/README.md`
for the descriptor layout and the complete interface.

A record owns one length byte and its L payload bytes. A zero-length record still
owns its prefix. The payload is binary, so zero is ordinary data and no terminator
is copied or appended. The descriptor's used count distinguishes an empty queue
from a full one when head equals tail. Old physical bytes do not become live
records merely because they still resemble a prefix and payload.

For push, reject a record whose own size cannot fit before testing current free
space. For pop, check emptiness before reading a prefix, then check destination
capacity before consuming or copying anything. A failed call permits no caller-data
store, even a store later restored to its original bytes. Registers can hold
candidate state while you decide whether to commit a successful operation.

Use the three **Check this program** activities. The first runs all 260 public
push trials; pop can still be unfinished. The second independently runs all 260
pop trials. Together they retain the original 520 kernel cases, checking both
result registers, exact bytes, full saved-register values, incoming continuations
and read/write footprints. The third follows the caller continuously, checking each
completed command across twelve prefixes, including zero commands and the full
fourteen-command sequence. A final matching string or descriptor is insufficient
when an earlier failed call wrote data or published an incorrect state.

These assessments use separate machines and leave your active Debug session
untouched. Treat a failed case as a concrete counterexample: find its first wrong
result, byte or access before changing the program. Hints are released in stages
according to instructor policy. Direct/free-writing entry uses the same source
and interface, rather than matching a reference instruction sequence.

## Inspect completed commands with a condition

After all three assessments pass, Debug your independent program. The public
`operation_ready` marker is reached after the caller publishes the function's
results and command index. Its s3 loop counter identifies that completed command.
Set a breakpoint on the marker's `nop`, right-click the breakpoint, enable
**Condition**, and enter `$s3 == 3` before starting.

Learn asks for four stops in the same session. Change only the breakpoint
condition between them, then Resume:

- Command three shows a wrapped record: head four, tail three, used seven.
- Command eight is full: head and tail four, used eight.
- Command ten rejects output capacity six for a seven-byte record. It returns
  (-2,7) without consuming the record or changing its output.
- Command eleven retries with enough capacity. It returns (7,8), leaves head and
  tail four, and makes used zero. The old physical bytes remain.

At each stop, compare the requested registers and inspect `descriptor_a`,
`ring_a` and `output` in Memory. The descriptor's first four words are head,
tail, used and capacity; its next eight bytes store the ring pointer. In the
Default layout the first descriptor begins at hexadecimal 10010000. The checker
observations occur at this completed-call boundary, not at an arbitrary instruction
inside a function.

Stop Debug and run the completed source normally. The supplied caller exits
without console output. Explain the queue using the descriptor and record bytes,
rather than treating silence as evidence of correct queue behavior. Edits retain
attempt history but require renewing execution observations for the changed file.
Keep the written lab's mask, missing-prefix and failed-pop counterexamples alongside
your implementation and its invariants.
