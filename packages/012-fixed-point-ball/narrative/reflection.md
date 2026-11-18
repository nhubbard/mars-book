# Reflect the part of a step beyond a wall

Advance one axis independently. Let $`p`$ be its current raw position, $`v`$ its raw velocity, and $`L=1792`$ its upper bound. The tentative position is $`z=p+v`$. The published velocity bound is $`-1024\leq v\leq1024`$, or at most four pixels per step. The reflection rule is
``` math
(p',v')=
 \begin{cases}
 (-z,-v),&z\leq0,\\
 (z,v),&0<z<L,\\
 (2L-z,-v),&z\geq L.
 \end{cases}
```
At an exact wall contact the new position is on that wall and the velocity points inward. A zero velocity stays zero, including at a wall. Initial states may put a nonzero velocity outward at a wall; the same rule reflects that motion back into the interval.

<div class="MARSWorkedExample">

Preserve the overshoot The vertical position is $`p=1728`$, or 6.75 pixels, and the velocity is $`v=320`$, or 1.25 pixels per step. The tentative result is raw 2048, or eight pixels. It lies one pixel beyond the wall at seven. Reflect that overshoot:
``` math
p'=2\cdot1792-2048=1536=6\cdot256,\qquad v'=-320.
```
The step finishes at six pixels, traveling upward. It travels one quarter of a pixel to the wall and one pixel back, totaling the specified 1.25 pixels. Clamping the position to seven would discard one pixel of this step’s travel. Merely reversing the velocity while keeping raw 2048 would leave an invalid position outside the drawing.

</div>

The native reflection figure shows the tentative point beyond the wall and its reflected position. The hollow point is not a published state.

# Make the bounds part of the argument

The coordinate and velocity bounds make the tentative raw position lie between $`-1024`$ and 2816. A lower-wall reflection therefore produces a position between zero and 1024. An upper-wall reflection produces a position between 768 and 1792. An interior result stays between zero and 1792. Each case finishes inside the permitted interval without a second reflection.

Negating the bounded velocity preserves its magnitude and is representable in a signed word. None of these ball updates can overflow a signed 32-bit sum. This is a proof from the input bounds, not a property of fixed-point arithmetic in general. Larger velocities would require another contract: a single reflection can then leave the result beyond the opposite wall. Repeated reflection or a periodic folding formula can handle that larger domain, but it is not silently included in this exercise.

Treat the two axes separately. At a corner, both components can reflect during one simulation step. A horizontal reflection changes only horizontal velocity; it does not reverse the vertical component. Screen coordinates make an upward vertical velocity negative. The sign follows the chosen axis direction, not the name of the wall.
