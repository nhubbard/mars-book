# A color load placed in the wrong slot

This sequence is incorrect for conditional color selection:

```asm
beq $t5, $zero, write_pixel
ori $t6, $zero, 0xffff
write_pixel:
sw $t6, 0($t2)
```

The single-instruction color load is in the delayed branch's slot and executes
for both outcomes, setting the color to 0x0000ffff.
The illustrated program instead places nop in the slot and the conditional
white load after it. Keep multi-instruction pseudos out of delay slots.
