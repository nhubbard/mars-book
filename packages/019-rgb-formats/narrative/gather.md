# Build the shuffle controls from byte positions
For the packing shuffle, call the first load A and the load at offset eight B.
Use A as the right source operand and B as the left source operand of
`vshf.b`. Selectors zero through fifteen choose A's bytes. Selectors sixteen
through thirty-one choose B's bytes. Selector 64 supplies zero. These are the only
selector values needed by this design.


See the companion fixtures for numerical tables.


Pixel six's red byte is at input offset eighteen. A does not contain it. B begins
at eight, so this byte is B lane ten, selected by sixteen plus ten, or 26. Selecting
eighteen would instead choose B lane two, input offset ten. The overlapping loads
make these two coordinate systems easy to confuse; derive selectors from the actual
register contents rather than copying input offsets directly.

The destination register initially holds the control bytes and is overwritten by
the shuffle result. Keep unchanged controls in separate registers and copy them
into the working destinations for every block. Reusing the previous result as the
next block's controls can make the first group pass and later groups fail.
