# Write two byte-inspector variations

Use the reasoning from Chapter 2 to write the input, calculation and output
instructions yourself. Each starter supplies strings and an entry point named
`main`. Its current body exits immediately. Write your program before that final
exit request, or replace the body with your own complete program.

The input precondition for both programs is one decimal integer from 0 through
255. Read it once. Print the original unsigned and signed interpretations on the
first two result lines, then print a third result. You choose the intermediate
registers, instruction sequence and labels. The checks compare the console
interface; they do not look up scratch registers or private source labels.

## Keep the ordinary sum

Work in `plus-one.asm`. The third result is the unsigned byte value plus one.
Keep that sum as an ordinary register value. Input 255 therefore produces 256
on the third line. The third prefix is exactly `Unsigned byte plus one: `,
including the space after the colon. End each result line with a newline.

Before writing instructions, name the three values you must retain and the
moment each is needed for output. Preparing a string request changes the service
selector. Decide where your arithmetic results will survive those requests.
The supplied data declarations let you concentrate on that value flow.

## Narrow the sum to eight bits

Keep the first solution and work separately in `wrapped-byte.asm`. Its first two
results have the same contract. For the third result, add one to the unsigned
byte value, then keep only the low eight bits of the sum. Input 255 now produces
zero. Its third prefix is exactly `Wrapped byte plus one: `.

Identify which operation discards the extra bit. A signed interpretation and a
narrowing operation answer different questions: one assigns a meaning to bits;
the other selects which bits remain. Explain both operations in your program.

These are the public cases:

| Input | Unsigned | Signed | Ordinary sum | Narrowed sum |
| ---: | ---: | ---: | ---: | ---: |
| 0 | 0 | 0 | 1 | 1 |
| 1 | 1 | 1 | 2 | 2 |
| 127 | 127 | 127 | 128 | 128 |
| 128 | 128 | -128 | 129 | 129 |
| 150 | 150 | -106 | 151 | 151 |
| 254 | 254 | -2 | 255 | 255 |
| 255 | 255 | -1 | 256 | 0 |

The shared prompt is exactly `Enter an integer from 0 to 255: `. The first two
prefixes are `Unsigned byte: ` and `Signed byte: `. Preserve the strings supplied
in each starter, or produce the same output using your own data declarations.
The IDE console can display the input you type; that input echo is separate from
the program output checked below.

## Run and investigate

Predict the rows for 128 and 255 before executing. Run each program in MARS and
compare all three results. If a result is wrong, pause after its calculation and
again before the corresponding integer-output request. Decide whether the first
incorrect state is the result itself, its lifetime or the output argument.

A normally completed starter still fails these tasks because it prints none of
the required results. A program that prints the original two results can also
fail: those results do not establish that the third calculation is correct.
Input 255 distinguishes the two third-result contracts even when every smaller
public input agrees. Use that difference when explaining your tests.

## Run the public command-line checks

These optional checks use the MARS command-line JAR and JDK 25. Obtain the JAR
from the course distribution and replace the example path below with its actual
location. Save your assembly file before checking it: the command-line grader
reads the saved file. Run these commands from the folder containing the starters
and check files:

```sh
java -jar "/path/to/mars-cli.jar" grade plus-one.kts plus-one.asm
java -jar "/path/to/mars-cli.jar" grade wrapped-byte.kts wrapped-byte.asm
```

Each command reports `PASS` or `FAIL` for seven named inputs. A case runs in a
fresh simulator and must print the exact prompt, prefixes, numbers and newlines,
then terminate normally. Its 20,000-instruction limit stops a check that does not
finish. Read a failed case's expected and actual output, trace that input in MARS,
revise the program and save it before checking again.

The `.cases.json` files contain the public inputs and output contracts. The
adjacent `.kts` files express those same cases in the existing MARS grading DSL.
They assess the independent programs separately from the worked program's guided
Learn observations. Passing these public cases supports your explanation of the
algorithm for the full input range; it does not supply that explanation.
