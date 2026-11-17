# Practice and extension questions

1. Trace (0,0) to (2,1), then reverse the supplied endpoints. Identify the exact tie
   and show why the canonical output remains unchanged.
2. Trace one update of (1,6) to (3,0). Explain why y can change without x.
3. Replace the two independent conditions with if/else in pseudocode. Which default
   update first loses a required diagonal step?
4. Explain why comparing only x for termination fails on a vertical line.
5. Derive the address of (7,7) and the location of the frame guard.
6. Change the framebuffer width to sixteen in a separate project. List the header,
   allocation, row-stride, input bounds, and expected-image checks that need revision.
7. Add a second color layer or clipping as a new assignment. State its tie, endpoint,
   preservation, and out-of-bounds contracts before changing code.
8. Compare simulated instruction counts for another line using both methods. Include
   the endpoints and explain why the result does not establish physical cycle cost.

For review, state the error relation at the loop boundary and name which registers
are invariant. Explain the difference between exhausted endpoint cases and sampled
colors/backgrounds.
