# Inspect one word at two widths

Read the 32-bit word at sample. Print its signed decimal, unsigned decimal,
hexadecimal, and binary representations using the exact four-line format below.
Also make these four 64-bit results available in the supplied data fields:

| Field | Required value |
| --- | --- |
| signed_value | The input word sign-extended to 64 bits |
| unsigned_value | The input word zero-extended to 64 bits |
| word_plus_one | Add one at 32-bit word width without overflow trapping, then sign-extend |
| wide_plus_one | Add one to the sign-extended input at 64-bit width |

The reference stores the results before printing. Equivalent solutions may use a
different order, registers, and instruction sequence if all final values and output
match. Preserve sample and the initial values of sample_guard and results_guard.
Produce no extra console text and exit normally within 1,000 completed instructions.

The input domain is all 32-bit patterns. The default initializer is -1, and the
expected output is exactly:

```text
signed: -1
unsigned: 4294967295
hex: 0xffffffff
binary: 11111111111111111111111111111111
```

Every line ends with one newline, including the last. Hexadecimal uses 0x followed
by eight lowercase digits; binary uses exactly 32 digits with no prefix. Decimal
representations have no grouping separators. The output describes the original word,
not the plus-one results. No terminal input is required.

## Guided version

Build the signed and unsigned loads, compare word and doubleword arithmetic, store
all eight bytes of each result, and reuse the familiar console service setup to print
the four representations. Predict the full register values and the memory bytes.
After the default sample, use the maximum signed word to expose the arithmetic-width
boundary. The illustration's register choices and labels are not submission requirements.

## Free-writing version

Choose another correct implementation of the same contract. The optional wide-shift
example is one possibility. Word-format console output alone cannot verify the upper
32 result bits, so run the memory checks as well. The grader can replace the input,
poison every result byte, and vary both guards.

## Submission

Submit main.asm and a trace for input -1 and input 2147483647. Include the signed
and unsigned interpretations, both load results, and both plus-one results. Explain
why the four word-format services can print identical output for two different
64-bit register values that share the same low word.
