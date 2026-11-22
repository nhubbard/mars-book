# Motion, rounding and raw integers

Read the fixed-point chapter and its lab before starting the guided trace. Use
an unchanged `fixed-point-ball.asm`: `DEMO` zero, `round_mode` zero, eight steps.
Convert raw 320 and raw 192 into pixels and pixels per tick. Predict the first
new position before running it. During Debug, distinguish the arguments to one
axis update, the partly updated pair in registers and the last complete
publication in memory. Record the overshoot and reflected position at each wall.

Use separate copies for nearest-cell rendering and the overflow probe. Explain
why a different rendered cell does not imply a different trajectory. Bound the
trial positions before arguing that word addition is sufficient for this motion
fixture; then explain why the independent interpolation needs wider intermediates.

Implement `interpolate_raw` in the independent source. Write its input and output
contract before coding. Work a negative half tie and an extreme endpoint pair
by hand. Use failing call inputs to make a small reproducible example. Complete
both checks: the function and stored caller result, then silent normal program
exit. The written lab asks for additional deliberate faults and an explanation
of their causes.
