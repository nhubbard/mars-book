# Provided target error

```asm
bad_target:
    li $v0, -3
    beq $zero, $zero, image_error
    nop
```
