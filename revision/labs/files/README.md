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
- `independent.asm`: a skeleton for writing the related program yourself.

The plugin creates only missing files and preserves edits on restart. When working
from the book without the guided course, copy `wrong-output.asm` to a separate
working file before repairing it. Keep the counterexample so you can compare the
first incorrect state with your repair. A correct copy can use an equivalent
instruction; explain the value passed to the service rather than copying spelling.

The native repair checkpoint observes one normal Run with the expected transcript
for input 20. It is a formative observation, not a proof for every input or private
instructor grading. The independent-writing task checks a related calculation
with multiple cases. The directly edited LaTeX chapter and lab supplement remain
the publication sources; these files do not generate their prose.


The independent program computes 3x+1 for inputs from -1000 through 1000. Its
skeleton supplies the prompt, answer prefix and newline data; you write the input,
calculation and output instructions. Labels and scratch registers are your choice.
Use one source file. The public check assembles the current editor text, then runs
separate machines for these cases:

| Input | Number after the supplied answer prefix |
| --- | ---: |
| 20 | 61 |
| 0 | 1 |
| -3 | -8 |
| -1000 | -2999 |
| 1000 | 3001 |

Each case requires the exact supplied prompt/prefix, the result and newline, and
normal exit within 2000 instructions. The check uses the existing MARS grader and
the IDE's assembly cache. It accepts equivalent programs and reports failures by
case. A source edit requires checking again. Five passing cases support your
reasoning; explain why the algorithm works throughout the stated range. Private
instructor checks remain separate. The skeleton intentionally does not pass.
