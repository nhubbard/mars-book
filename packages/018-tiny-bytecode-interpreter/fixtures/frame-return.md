# Provided frame return

```asm
vm_commit:
    sw $s6, 0($s2)
    sw $s7, 4($s2)
    sw $v1, 8($s2)
    sw $v0, 12($s2)
    sw $a2, 20($s2)
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    daddiu $sp, $sp, 64
    jr $ra
    nop
```
