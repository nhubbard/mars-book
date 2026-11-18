# Rounding

Use floor((S+16384)/32768) once, then clamp. Negative half becomes zero. Eight half-valued products round together to four, rather than eight individually rounded ones.
