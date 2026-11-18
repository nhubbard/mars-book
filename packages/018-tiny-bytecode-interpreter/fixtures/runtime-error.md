# Provided runtime error

```asm
end_fault:
    li $v0, -4
    beq $zero, $zero, runtime_error
    nop
underflow_stack:
    li $v0, -5
    beq $zero, $zero, runtime_error
    nop
overflow_stack:
    li $v0, -6
    beq $zero, $zero, runtime_error
    nop
arithmetic_overflow:
    li $v0, -7
    beq $zero, $zero, runtime_error
    nop
zero_divisor:
    li $v0, -8
runtime_error:
    move $a2, $s6
```
