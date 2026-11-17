# Why the two error tests use these thresholds

Let A be the ordered start, dx ≥ 0, D ≥ 0, i = x−Ax, and j = sy×(y−Ay).
The invariant is err = dx−D−D×i+dx×j. An x step increases i by one and subtracts D;
a y step increases j by one and adds dx.

On an x-major line, the next ideal minor coordinate is D×(i+1)/dx. It rounds one
step forward when D×(i+1)/dx−j ≥ 1/2. Multiplying by the positive denominator and
substituting the invariant gives 2×err ≤ dx, the y-step test.

On a y-major line, the next ideal minor coordinate is dx×(j+1)/D. Its forward-step
condition dx×(j+1)/D−i ≥ 1/2 rearranges to 2×err ≥ −D, the x-step test.

The non-major axis uses its rounding test; the major axis advances on every update.
The point case exits before a zero-denominator argument is needed. Both tests use
the same pre-update error. Equality chooses the canonical forward minor step.
