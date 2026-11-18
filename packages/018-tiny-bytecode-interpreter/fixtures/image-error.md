# Provided image error

```asm
truncated_image:
    li $v0, -2
image_error:
    move $a2, $s6
    move $s6, $zero
    beq $zero, $zero, vm_commit
    nop
```
