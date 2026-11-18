# Vector round

```asm
fir_round:
    daddiu $t0, $t0, 16384
    dsra $t0, $t0, 15
    li $t8, 32767
    slt $t9, $t8, $t0
    bne $t9, $zero, fir_high
    nop
    li $t8, -32768
    slt $t9, $t0, $t8
    bne $t9, $zero, fir_low
    nop
    beq $zero, $zero, fir_store
    nop
```
