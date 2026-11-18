# Vector

Eight halfwords fit a 128-bit vector. addv.h wraps each lane while adds_s.h saturates each signed lane. Compare those two sums with ceq.h; equal lanes are ffff. Invert the comparison to obtain ffff only for clipped lanes. A signed overflow wraps to the opposite sign, so it cannot accidentally equal its saturated endpoint.
