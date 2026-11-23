# RGB8 forward-filter contract

Implement `png_filter_scanline(a0 current RGB, a1 prior RGB or zero, a2 output,
a3 settings)`. Settings are three aligned native unsigned 64-bit fields: width,
selector and output capacity, at offsets zero, eight and sixteen. Accept widths
one through 512 and selectors zero through four. Required output is `1+3*width`
bytes. Capacity is unsigned; a high-bit-set value is large, not a shortage.
Read all 64 bits and validate settings before RGB/output access.

Success returns v0=0 and v1=`1+3*width`; emit the selector followed by exactly
three residual bytes per pixel. Failure returns v0=1 and v1=0, accesses no RGB
or output bytes and changes no caller object. The first 24 settings bytes are
readable; the final eight sentinel bytes in the supplied settings object are not.

For each original sample x, use a=original current-row sample three bytes earlier,
b=prior-row sample at this position, c=prior-row sample three bytes earlier.
Missing left neighbors are zero. A null prior pointer means b=c=0. None and Sub
never dereference the prior pointer, even if it is nonnull. Predictors are None
0, Sub a, Up b, Average floor((a+b)/2), and Paeth's selected neighbor. Paeth forms
p=a+b-c, compares absolute distances to a,b,c, and resolves ties in that order.
Emit `(x-predictor) modulo 256`. Keep unsigned samples and wide intermediate
arithmetic; only the residual wraps to a byte.

RGB slices can be unaligned and may exactly coincide with each other because
both are read-only. Output is disjoint from RGB and settings; settings are
disjoint from RGB. Among caller objects, read only settings and the selected
original inputs; output is writable but not readable. Write only selected output.
Preserve all other caller bytes. Your private constants, workspace and gaps remain
available. Forward and backward traversals are accepted; in-place output is not
part of this interface. The checks do not require private labels, scratch registers
or an exact instruction sequence.

Preserve full s0-s7, fp, sp and the 64-bit return continuation. Incoming sp is
eight-byte aligned. Perform no I/O. Repeated calls share one simulator, including
private state, with one cumulative instruction allowance. The JSON profiles
retain immutable public case data from the imported archive; changing extracted
copies cannot replace expectations. The final assessment covers all 266 cases.
Instructor-only solutions and expanded checks are not in this distribution.
