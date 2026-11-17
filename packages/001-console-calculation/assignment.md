# Calculate twice an integer plus one

Write a MARS assembly program that reads one signed decimal integer x, computes
2 × x + 1, and prints the result. Inputs are restricted to -1000 through 1000,
including both endpoints. Every intermediate value in the demonstrated calculation
fits in a signed 32-bit word. Input validation and out-of-range behavior are outside
this assignment's contract.

Use the educational MARS syscall interface. Before reading, print exactly the following quoted text, without the quotation marks:

```text
"Enter a small integer: "
```

Then print `Twice your number plus one is: ` followed by the result in signed decimal
and a newline. Both prompts end in one space. For input 20, the program's output
bytes, expressed as a string with an escaped newline, are:

```text
Enter a small integer: Twice your number plus one is: 41\n
```

Interactive terminals may also echo the typed input. That echo is not part of the
program output checked by the grader. Exit normally using the supplied exit service.
Do not add extra headings, blank lines, or diagnostic output to the submitted program.

## Guided exercise

Work through steps 01 through 22 in order. The guide uses $t0 as the arithmetic
result register so the book's observations refer to the same state. Predict each
state before running or stepping. The local checks grade the completed program;
step descriptions explain the intermediate checkpoints. The future plugin's
instruction-by-instruction guidance is described in docs/interactive-design.md
at the repository root and is not implemented by this package yet.

## Free-writing exercise

After completing the guide, start again from the starter. Use a different correct
arithmetic sequence or scratch register while preserving the input/output contract.
Source spelling, private labels, and scratch-register choices are not graded.
Compare outputs for -1000, -3, 0, 20, and 1000. Explain why changing the implementation
preserves the mathematical result within this input domain.

## Evidence to submit

Submit main.asm plus a brief explanation of the input register, the arithmetic
intermediate values for input -3, and the argument register used to print the result.
Run the public checks. Your instructor may use additional in-domain inputs.
