# Vector clamps

```asm
fir_high:
    li $t0, 32767
    beq $zero, $zero, fir_clipped
    nop
fir_low:
    li $t0, -32768
fir_clipped:
    addiu $v1, $v1, 1
```
