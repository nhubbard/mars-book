# Check the representation before writing handlers
## Exercise: A mapped byte is not necessarily an instruction
Why is target six invalid in the default program?

- **A** The byte lies outside the eighteen-byte image.
- **B** The byte is an operand rather than an instruction beginning.
- **C** Every branch must select a larger offset.

Explain why a range check alone cannot establish target validity.

## Exercise: Budget and failure timing
Use capacity two. Compare budgets two and three for the default image. State status,
PC, depth, retired count and fault offset for each call. Which instruction is first
attempted by the larger-budget call that the smaller-budget call never attempts?

## Exercise: Two stacks
Identify the memory changed by `DUP` and the register addressing the interpreter
frame. Explain why a guest target is a byte offset, then give the full-width address
calculation for its opcode.
