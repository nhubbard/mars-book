# Account for the visible and numerical loss
A five-bit field has 32 possible values and a six-bit field has 64. Green therefore
has twice as many retained code levels as either other component. The name RGB565
describes that allocation; it does not prescribe the conversion rule or a perceptual
error model. Here all channels use the explicit truncation and expansion formulas.


See the companion fixtures for numerical tables.


Packing discards low bits, but the expanded value need not be smaller than the
original. Red 128 becomes 132 under bit repetition. Writing a five-bit input as
$8q+u$, where $0\leq u\leq7$, gives expansion error
$\lfloor q/4\rfloor-u$. It lies between minus seven and seven. For green, the
corresponding error lies between minus three and three. These bounds concern each
component's integer code value, not a distance in a perceptual color space.

The supplied preview places the original 32-pixel row above the expanded row.
Small numerical changes may be difficult to distinguish at a particular zoom or
on a particular display. Inspect bytes and computed errors as well as the image.
A matching screenshot cannot establish that every possible component value is correct.
