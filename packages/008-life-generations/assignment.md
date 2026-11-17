# Evolve Life with a stable source generation

Implement Conway's Game of Life on a finite 8 by 8 board. Use the Default memory
layout and little-endian byte order. Each seed byte is zero
(dead) or one (alive), in row-major order. The word generations requests zero through
four evolution steps. A dead cell is born with exactly three live neighbors. A live
cell survives with two or three; otherwise the next state is dead. Exclude the center
from its eight-neighbor count. Hold all positions outside the board dead forever:
this assignment has no wraparound and is not an infinite-board simulation.

The program uses the supplied `board_a` and `board_b` allocations, each 100 bytes with
row stride ten. Interior logical coordinate (x,y) occupies offset (y+1)×10+(x+1).
All 36 halo bytes around each interior start zero and must receive no CPU stores.
Interior destination bytes may initially contain arbitrary values; compute all 64
next states independently of those old contents. Store only binary cell results.

Initialize the interior of `board_a` from the immutable 64-byte seed. Generation zero
is that initialized board, not the first evolved board. At a completed generation g,
`board_a` is current when g is even and `board_b` when g is odd. For g>0 the other board
must retain generation g−1. The current board receives no execution stores from the
ready checkpoint until the next checkpoint, or until normal exit after the last one.
Use the other board for the next generation, then exchange their working roles.

## Public generation checkpoint

Define `generation_ready` exactly once in text. Reach it after rendering each complete
generation from zero through the requested count, in order. At entry, generation must
contain g, the current board must contain that generation, and all 64 frame words must
be 0x00ffffff for live cells or zero for dead cells. The frame is fixed at 0x10010000
and occupies 256 bytes; `frame_guard` follows at 0x10010100. It must be fully overwritten
each time, including cells that died. After the final ready checkpoint, exit normally.
A zero-step request still initializes and renders generation zero.

This public label is an observation interface for grading and the future guided lesson.
It does not prescribe registers, other labels, cell traversal order, neighbor-count
implementation, or rule-expression instructions. A solution can compute through another
temporary representation if it publishes the required boards, protects the active source,
and honors the same checkpoint contract. The source and destination roles are externally
observable; an in-place sweep is not equivalent to the synchronous update required here.

Do not execute stores to seed, generations, any supplied guard, or either board's halo,
even if restored later. Seed, generations, and the guards must retain their supplied
values. Produce no console output and exit within 100,000 completed instructions.
No wall-clock delay or guarantee of one UI repaint per generation is required.

## Submit and explain

Submit main.asm and a trace of the default horizontal three-cell oscillator through
its first two steps. Explain why a destination write cannot change another cell's
neighbor count, which pointer is current after an odd or even number of swaps, and why
a partially computed board is not a publishable generation. Predict a stable block,
an edge case, and zero steps before running the public checks.
