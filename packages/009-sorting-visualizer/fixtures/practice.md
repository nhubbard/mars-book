# Practice and extensions

1. Trace the default first exchange in each algorithm and compare the resulting arrays.
2. Count the inversions in [1,0,−1,0]. Predict adjacent-exchange insertion's swap count.
3. Explain why loading one value, overwriting the other slot, and then loading that
   overwritten slot loses data. Repair the exchange without changing multiplicities.
4. Derive the first white row for value −2 and the address of frame cell (7,7).
5. Call the renderer with a pointer to the second word and length three. Identify
   which source values are active and which saved registers must remain unchanged.
6. Trace [2a,2b,1] as records. State the identity assertion needed to test stability.
7. Compare the whole and range metrics for reversed input. Explain why render cost
   grows with the number of published exchanges.
8. Design a shift-based insertion or unrestricted signed-word extension separately.
   Specify its intermediate-state policy, arithmetic comparison, display encoding,
   record width, and grading changes before implementing it.

A nested helper call inside render also changes its return-address obligations.
State how the incoming continuation would be preserved before adding another `jal`.
