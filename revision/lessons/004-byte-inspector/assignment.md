# Interpret one byte at register width

Use MIPS64 Release 6 and the Default memory layout. The complete byte inspector
accepts an integer from 0 through 255 and prints its low byte as unsigned and
signed values. The mask computes low bits; the program does not validate that
input range. Keep an unchanged copy before introducing deliberate faults.

For input 150, predict both result registers before running. In Debug, break at
`interpret`, inspect `t0` after `andi`, then inspect `t1` after `seb`. Compare
all sixteen hexadecimal digits. Continue through reporting and identify the
register supplying each integer-output argument.

Repeat with 0, 127, 128 and 255. Explain which boundary each case distinguishes.
Use the separate `word-boundary.asm` experiment to compare instruction widths;
service 1 alone does not display every bit in a 64-bit register.

The main book's Chapter 2 and its written lab contain the full explanations,
fault investigations and independent output variations. The older
`004-bit-inspector` word-report assignment is a separate program with different
prerequisites and checks.

The imported lesson presents four predictions, five Run observations and an
ordered Debug activity with three stops. Answer
questions and request hints in Learn. When an activity asks for execution, use
ordinary Run on the imported `byte-inspector.asm` with the requested integer.
A normally completed run must match both the consumed input and console output.
Keep the default assembly settings, including pseudoinstruction support.

After the Run activities, set a breakpoint on `andi` under `interpret` and launch
Debug with 150. Observe the input before the mask, then use Step Over to inspect
the completed mask and sign extension. Each instruction highlighted by Debug is
next to execute. Compare all sixteen hexadecimal digits. A machine edit or
Backstep is useful for exploration, but this activity requires a fresh forward
session to record its ordered observations. An incomplete sequence must be
repeated in one session.

Editing the source invalidates its Run and Debug observations. Use **Tools → Continue
Downloaded MARS Lab** to repeat the activities that need checking; prediction
answers and revealed hints remain. After the checked Debug stops, resume
execution and trace the output arguments. The deliberate faults, word-width
experiment and independent variations in the written lab remain manual.
This development package remains subject to human learner review.
