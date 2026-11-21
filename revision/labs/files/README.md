# First-unit programs

`console.asm` is the complete worked program. `wrong-output.asm` is a public
counterexample with the same input, arithmetic and messages. Its integer-output
argument comes from the current service selector instead of the retained answer.
It supports the written lab's investigation of a correct calculation followed by
incorrect output. Predict the console result before running it.

The native MARS course creates three files under `mars-labs/console/`:

- `first-program.asm`: the worked program, corresponding to `console.asm` here.
- `wrong-output.asm`: the counterexample, kept available for comparison.
- `repair-output.asm`: a working copy of the counterexample for your repair.

The plugin creates only missing files and preserves edits on restart. When working
from the book without the guided course, copy `wrong-output.asm` to a separate
working file before repairing it. Keep the counterexample so you can compare the
first incorrect state with your repair. A correct copy can use an equivalent
instruction; explain the value passed to the service rather than copying spelling.

The native repair checkpoint observes one normal Run with the expected transcript
for input 20. It is a formative observation, not a proof for every input or private
instructor grading. The independent-writing task will require a related calculation
and multiple cases. The directly edited LaTeX chapter and lab supplement remain
the publication sources; these files do not generate their prose.
