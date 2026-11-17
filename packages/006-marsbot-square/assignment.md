# Draw a square with coordinate feedback

Draw a clockwise, axis-aligned square from the robot's initial position (0, 0).
The input word `side` is an integer from 1 through 63. Visit the corners in this
order: (side, 0), (side, side), (0, side), (0, 0). The complete pen trail must consist
of those four straight sides, with no missing or extra strokes. End stopped at the
origin, facing north (heading zero), with the pen up. Produce no console output.

Use the Default memory layout. Its MarsBot register block begins at 0xffff8000:
heading is at +0x10, pen at +0x20, x feedback at +0x30, y feedback at +0x40, and
movement control at +0x50. Heading is measured clockwise from north. Screen x increases
to the right and y downward. One changes a control register with a store, then reads
coordinate feedback to decide when to stop. A store to a coordinate is not a teleport.

Do not execute stores to `side`, `side_guard`, or either coordinate register, even
if the original value is restored later. Use the input; a hard-coded sixteen-pixel
path is insufficient. The published contract concerns the actual pen trail and final
device state. It does not require particular scratch registers, polling labels, a
specific command count, or the reference source.

The IDE header selects 64 completed instructions per movement tick. Grading may choose
another integer rate from 8 through 128. The supplied polling loop is short enough to
stop before the next movement tick throughout that range. Do not place output services
or long calculations between its coordinate read and stop. Exit within 100,000 completed
instructions. This ceiling guards against nontermination; instruction count is not a score.

## Guided and free-writing versions

The guided version writes four explicit legs. Each leg sets its heading while stopped,
starts movement, polls the relevant coordinate, and stops before turning. A free-writing
solution may drive a loop from a command table or use another equivalent structure.
The grader attaches the same robot model as the IDE and checks its real trail. A RAM
list of intended corners does not establish that the robot traveled with its pen down.

## Submit and explain

Submit `main.asm`, a trace at each corner, and the invariant for one polling loop.
Explain why a fixed busy-wait count depends on the device rate, why north cannot be the
first outward leg from the top-left boundary, and why reaching the origin alone does
not prove that a square was drawn. Run the public checks for the default and a short side.
Try a changed rate in a separate copy and predict the effect before executing.
