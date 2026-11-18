# Exact format fixture

RGB888 byte i*3 is red, i*3+1 green, i*3+2 blue. RGB565 has fields R at 15..11, G at 10..5 and B at 4..0, stored low byte first. For (123,45,67), retained (15,11,8) yields decimal 31080, hex 7968, bytes 68 79, and expansion (123,44,66). Red 128 expands to 132 under the exact rule. Five-bit error is -7..7, green error -3..3. These are code-value bounds, not perceptual distances.
