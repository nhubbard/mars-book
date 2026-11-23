# Working with MSA lanes

Read Chapter 14 and its written lab. Derive the first RGB565 halfword and its
stored byte pair before running the worked source. In Learn, answer the inline
predictions beside the source, then follow the Debug observations. A breakpoint
stops before its instruction executes: at pack_fields the packed register exists,
but the selected output store has not happened.

Use byte and halfword views of the same vector to explain the gather. Account
for the overlapping loads at offsets zero and eight, then trace selectors from
the rightmost and second source. Follow the second group to explain why each
working destination receives fresh controls. Separate the returned count from
the caller's later publication and from the display adapter's pixel words.

Use the supplied complete count variants for zero, seven, eight, nine and
seventeen. Predict complete groups and scalar pixels before Debug. Relate pointer
movement to the selected intervals. An address can be mapped while lying outside
the requested slice. A normal Run and a plausible preview do not establish those
bounds; inspect the values and reason about complete load/store widths.

Write pack_rgb555 independently under independent/README.md's contract. Explain
its different green field, zero top bit, vector layout and tail. Use the checker
on the current source, inspect its first failure and retry. Keep your argument
for the allowed counts beside the finite test evidence. Both the function/caller
assessment and complete normal-exit assessment must pass for the current source.
