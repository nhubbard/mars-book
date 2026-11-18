# Keep bounds and live depth distinct
Capacity is the number of operand words available to this call. Depth is the number
of live values currently stored there. Initially depth is zero even when the physical
array contains values left by an earlier run. The top live value, when one exists,
is at index depth minus one. The next free slot is at index depth.

`DUP`, `DROP` and `JZ` require one live value. `OVER`,
`SWAP` and binary arithmetic require two. Check that requirement before
checking whether an operation that copies a value also has room to push it.
An empty zero-capacity stack executing `DUP` therefore reports underflow;
a full nonempty stack executing `DUP` reports overflow. A push checks room
before writing. A successful arithmetic operation consumes two values and produces
one, reducing depth by one.

For binary arithmetic, the right value is initially on top and the left value is
just below it. On a successful subtraction, replace those two with left minus right.
For example, pushing seven and then twelve before `SUB` produces minus five.
Compute and check a candidate before publishing the guided implementation's result.
On an arithmetic fault, the live prefix remains the two original operands along
with any older live values.

Capacity can be smaller than the backing allocation. The sixteen-word arrays in the
starter make guards and subviews easy to inspect, but a selected capacity of three
permits accesses to only three words beginning at the descriptor's stack address.
Cells beyond that subview remain caller-owned. Inactive cells inside it are workspace;
the active prefix, descriptor results and ownership bounds define the graded state.
