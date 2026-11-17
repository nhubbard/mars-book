# Rasterize a line with an integer decision state

Paint the line between two supplied integer endpoints on an 8 by 8 word framebuffer.
Each coordinate in the four-word `endpoints` block is between zero and seven, inclusive;
the order is x0, y0, x1, y1. Paint selected cells with the separate `color` input word,
which is between 0 and 0x00ffffff. At normal exit, preserve every other framebuffer word exactly,
including its high byte. Do not clear the canvas as part of this operation.

The frame occupies 256 bytes at 0x10010000 in the Default layout. Its guard is at
0x10010100. Address a cell as frame + 4 × (8 × y + x). The display uses screen
coordinates: x increases rightward and y downward. Include both endpoints, and paint
a single cell when the endpoints coincide. End normally within 10,000 completed
instructions and produce no console output.

## Pixel selection and exact ties

First order the endpoints by x, breaking an equal-x tie by y. Work from the earlier
ordered point A to B regardless of the caller's original order. Let dx = Bx − Ax,
D = |By − Ay|, and sy = +1 when By ≥ Ay, otherwise −1. This gives dx ≥ 0.

If dx ≥ D and dx is nonzero, select one cell for each k from 0 through dx:
x = Ax + k, y = Ay + sy × floor((2 × k × D + dx) / (2 × dx)).
If D > dx, select one for each k from 0 through D:
x = Ax + floor((2 × k × dx + D) / (2 × D)), y = Ay + sy × k.
If both differences are zero, select A alone. Here floor means round down; all
fractions shown have nonnegative numerators and positive denominators.

These formulas define nearest minor-axis coordinates, taking an exact halfway choice
forward along the canonical direction. Thus (0, 0) to (2, 1) selects (1, 1), not (1, 0).
Reversing the supplied endpoints must give the same image. Other tie conventions can
be useful rasterizers, but this assignment uses the convention above so its results
are reproducible. The formulas specify the result; division is not required in a solution.

## Inputs, evidence, and equivalent solutions

Do not execute stores to the endpoint block, the color word, or any supplied guard,
even if a later store restores its value. The grader varies coordinates, colors, and
existing framebuffer words. It checks all 64 final cells and protected-data history.
It does not require a pixel-store order, exact instruction count, private labels, or
specific scratch registers. Rewriting a selected cell redundantly is permitted by
this final-image contract, although unnecessary work should be explained.

The guide implements an incremental Bresenham decision state with addition, comparisons,
and branches. A closed-form rational implementation is an accepted alternative when it
produces the same cells and preserves the same inputs. The default drawing is (1, 1)
to (6, 4), with six white cells. A fast Run may show only the completed drawing;
Debug checkpoints let you inspect the error updates between stores.

## Submit and explain

Submit main.asm and a trace of the default line. Explain the role of the saved doubled
error, why two independent coordinate decisions are needed, where endpoint equality
is tested, and why endpoint normalization makes the image reversal invariant. Predict
one steep line, one negative-slope line, an exact tie, and a coincident-endpoint case
before running the public checks.
