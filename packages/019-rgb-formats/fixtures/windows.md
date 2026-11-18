# Bounded windows and selector coordinates

Eight RGB888 pixels occupy bytes [0,24). Pack loads [0,16) and [8,24), then writes [0,16) packed output. With first load as right source A and second as left B, vshf selectors 0..15 name A and 16..31 name B. Selector 64 supplies zero. Red selectors are 0,3,6,9,12,15,26,29; green 1,4,7,10,13,24,27,30; blue 2,5,8,11,14,25,28,31. Put selector 64 after each to make zero-extended halfwords. Input offset eighteen is B lane ten, selector twenty-six.

Expansion reads [0,16), then stores output [0,16) and [8,24). Shared output bytes must agree. A destination selector vector is overwritten by shuffle results, so copy preserved controls every iteration. Mapped padding never authorizes an overread or overstore.
