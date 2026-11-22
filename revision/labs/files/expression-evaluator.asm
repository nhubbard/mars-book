# Independent expression evaluator. Implement eval_expr; keep the caller/data harness.
.data
input_a_guard: .word 0x13579bdf
input_a: .byte 49,56,32,45,32,51,32,42,32,40,50,32,43,32,45,52,41
    .space 79
input_a_end: .word 0x2468ace0
input_b_guard: .word 0x31415926
input_b: .space 96
input_b_end: .word 0x27182818
length: .word 17
status: .word 99
result: .word 99
state_end: .word 0x12345678
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, input_a
    dla $t0, length
    lw $a1, 0($t0)
expression_call:
    balc eval_expr
    nop
expression_ready:
    nop
expression_continue:
    dla $t0, status
    sw $v0, 0($t0)
    sw $v1, 4($t0)
expression_stored:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

eval_expr:
    break
