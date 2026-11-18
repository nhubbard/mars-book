# Private register protocol and intermediate stops

s0=full code base, s1=byte length, s2=full descriptor, s3=full stack pointer, s4=capacity, s5=64-bit boundary bitmap, s6=phase cursor then guest PC, s7=depth, v0=status, v1=retired, a2=fault, a3=remaining budget. Setup copies the input state pointer before reassigning a2. targets_begin and execute_begin reset the cursor. Validation never increments v1.

Stop at vm_commit before its stores and restores for the stated intermediate observations. At vm_ready saved registers have returned to their caller values and the descriptor holds the public outputs. Replace temporary branches instead of appending after them. Preserve listed nop delay slots; the guest bytecode has no delay slots.
