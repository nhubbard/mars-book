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

This package's prediction definition is a draft runtime asset. The current
plugin validates its format during import; native lesson presentation is being
integrated. The assembly programs can already be used with ordinary Run and Debug.
