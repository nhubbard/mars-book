# Coordinate feedback and device state

Read the decisions-and-repetition chapter and its MarsBot companion lab. This
exercise assumes the counter's branches, loops and memory-mapped stores. It does
not require functions, arrays or a command table.

Use MIPS64 Release 6, Default memory and little-endian byte order. The source
comment connects MarsBot for ordinary Run and Debug. Its device block begins at
0x00000000ffff8000: heading is at offset 0x10, pen at 0x20, x feedback at 0x30,
y feedback at 0x40 and movement control at 0x50. The program retains the complete
positive device address in s0. A command is a store; progress is an observation
obtained by loading a coordinate. Do not write the coordinate words.

The native questions and controls appear in Learn. Read each explanation, choose
an answer there, and use its feedback to reconsider a wrong prediction. Source
pointers identify instructions for discussion. They do not execute those
instructions or establish a debugger pause.

## Observe three different conditions

`worked/east.asm` is the complete public one-leg experiment. It requests a
sixteen-pixel line and 64 completed simulator instructions per movement tick.
Open MarsBot while debugging so you can inspect position, movement and pen.

Start a fresh Debug session for this source. Set the named breakpoints requested
by Learn. At `east_stop`, the movement-stop store is next: x has reached side,
but movement is still enabled. Step over that store to `east_stopped`. Position
is retained, movement is disabled and the pen is still down. Step over the pen
store and pause at `done` before `li`. The final state has the pen up.

The three register comparisons in Learn establish source boundaries and the
retained endpoint values. Inspect the actual device flags as well; unchanged
register copies do not establish unchanged device state. The checker later
verifies final device state and the full trail in separate machines.

Stop that session before debugging `worked/east-slow.asm`. This version requests
side two at 128 completed instructions per tick. Repeat the same three boundaries.
The loop polls fresh coordinates, so it need not guess how many iterations
correspond to a particular distance. The written lab explains the response bound
for this short loop over side 1–63 and integer rate 8–128. Do not extend that
argument to arbitrary headings, longer loop bodies or unrelated rates.

Stop Debug, then run `worked/east.asm` normally. Its expected console transcript
is empty. That Run observation establishes normal exit and no console output;
it does not itself prove that any line was drawn. Use the one-leg assessment to
check the actual final trail and state over sixteen public length/rate pairs.
The original written-lab checker also retains an earlier stopped/pen-down check.

## Write the square independently

Open `independent/square.asm` and replace its deliberate failing `break` before
the supplied exit. Retain the data declaration and its layout. Read `side` rather
than hard-coding sixteen. The complete contract and checking scope are in
`independent/README.md`. You choose the register allocation, loop organization
and any helpers; the checks do not match a reference instruction sequence.

Use **Check this program** for feedback on your current source. A failed case
is a counterexample: distinguish a wrong route from missing ink, an incorrect
final flag, a forbidden write or nontermination before changing the program.
The calibrated-delay investigation in the written lab shows why passing the
default drawing alone is insufficient.

**Choose Downloaded MARS Lab Activity...** can open this independent task directly
when instructor policy permits it. Direct entry retains the same source, checks
and attempt history. Hints are available in stages when released by the course.
Edits invalidate earlier execution evidence for the edited file; renew those
observations after a change. Independent assessment uses separate machines and
does not reset your active Run/Debug device.

This is a development edition. Its native import, source-pointer, first-use and
resume verification status is recorded separately. No representative learner
review is claimed.
