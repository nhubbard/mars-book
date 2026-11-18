# Trace the same motion through two renderers

Start at raw coordinates $`(320,448)`$, with raw velocities $`(192,320)`$. The following trace keeps the positions and velocities as exact integers. Only the final two columns apply the display rounding rules.

| Tick | Raw $`x`$ | Raw $`y`$ | Raw $`v_x`$ | Raw $`v_y`$ | Floor ID | Nearest ID |
|-----:|----------:|----------:|------------:|------------:|---------:|-----------:|
|    0 |       320 |       448 |         192 |         320 |        9 |         17 |
|    1 |       512 |       768 |         192 |         320 |       26 |         26 |
|    2 |       704 |      1088 |         192 |         320 |       34 |         35 |
|    3 |       896 |      1408 |         192 |         320 |       43 |         52 |
|    4 |      1088 |      1728 |         192 |         320 |       52 |         60 |
|    5 |      1280 |      1536 |         192 |    $`-320`$ |       53 |         53 |
|    6 |      1472 |      1216 |         192 |    $`-320`$ |       37 |         46 |
|    7 |      1664 |       896 |         192 |    $`-320`$ |       30 |         39 |
|    8 |      1728 |       576 |    $`-192`$ |    $`-320`$ |       22 |         23 |

The first vertical reflection occurs at tick five. Its raw position becomes 1536, preserving the overshoot described earlier, and its vertical velocity becomes $`-320`$. The first horizontal reflection occurs at tick eight. Before that reflection, the horizontal trial is $`1664+192=1856`$, leaving an overshoot of 64 raw units. The new position is $`1792-64=1728`$, and the horizontal velocity becomes $`-192`$.

At tick seven, nearest rounding already chooses column seven. At tick eight it still chooses column seven, although the horizontal velocity has reversed. The raw coordinates and pixel ID together explain a change that the image alone can hide. A useful debugger figure should show both, with the rounding mode and the stopped source boundary visible.

<div class="MARSExercise">

Predict an exact contact Start an axis at raw 1664 with velocity 128. Calculate the next raw position and velocity, and then advance it once more. Explain why the first frame can remain on the wall while its velocity is already inward. Repeat for raw 128 and velocity $`-128`$ at the lower wall.

</div>
