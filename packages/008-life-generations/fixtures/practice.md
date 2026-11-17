# Practice, invariants, and extensions

1. List the live cells after one and two steps of the default oscillator. State
   which physical buffer is current at each checkpoint.
2. Count the neighbors of a corner cell. Explain which of the eight relative loads
   use halo bytes and why no opposite-edge cell participates.
3. Show the in-place error at (2,3) after an early birth at (3,2). Which generation
   should each input byte have belonged to?
4. Explain why a zero-step request must render the seed, and why the unused destination
   interior has no required binary contents yet.
5. Begin with arbitrary nonzero destination bytes. Explain why skipping stores of dead
   results is incorrect even if the first run from cleared memory looked right.
6. State a per-cell sweep invariant and a generation-loop invariant. Use them to justify
   a different traversal order while retaining the same completed results.
7. Specify a wraparound or larger-board variant in a separate project. List the changed
   halo, stride, boundary, input, and grading requirements before editing instructions.
8. Compare instruction counts on a stable block and a dense pattern. Record the initial
   board and requested count; do not infer host-runtime speedup from counts alone.

A packed-bit or vector implementation is a further project. It still needs a complete
source snapshot and defined boundary behavior; a different representation does not
remove the synchronous-update requirement.
