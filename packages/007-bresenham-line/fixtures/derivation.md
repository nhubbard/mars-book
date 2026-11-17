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

## Why the major axis advances

The nearest-cell rule also gives a bound that explains progress. On an x-major line,
write δ = j − D×i/dx. Nearest rounding gives |δ| ≤ 1/2. Substituting into the error
relation gives err = dx − D + dx×δ, so err ≥ dx/2 − D ≥ −D/2 because dx ≥ D.
The x-step condition therefore always succeeds on a nonfinal x-major sample.

On a y-major line, use δ = i − dx×j/D. The same rounding bound gives
err = dx − D − D×δ ≤ dx − D/2 ≤ dx/2 because D ≥ dx. The y-step condition always
succeeds there. The other condition chooses whether the minor coordinate steps too.

The ordered starting point satisfies the nearest-cell relation with i = j = 0.
The threshold calculation above gives the next rounded minor coordinate, and the
major-axis argument guarantees one unit of major progress. Repeating that reasoning
preserves the relation until the endpoint. There are max(dx,D) updates and
max(dx,D)+1 painted samples, at most eight on this canvas. The endpoint test prevents
an additional update. The point case needs no update or division argument.
