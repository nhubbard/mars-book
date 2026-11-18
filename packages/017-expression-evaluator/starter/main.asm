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
    dla $a0, input_a
    dla $t0, length
    lw $a1, 0($t0)
expression_call:
    jal eval_expr
    nop
expression_ready:
    nop
expression_continue:
    dla $t0, status
    sw $v0, 0($t0)
    sw $v1, 4($t0)
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

eval_expr:
    daddiu $sp, $sp, -80
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $ra, 64($sp)
    move $s0, $a0
    move $s1, $zero
    move $s2, $a1
    move $s3, $zero
    move $s4, $zero
    jal parse_expr
    nop
    bne $s3, $zero, eval_error
    nop
    move $s5, $v0
    jal skip_space
    nop
    beq $s1, $s2, eval_success
    nop
    li $s3, -1
    move $s4, $s1
eval_error:
    move $v0, $s3
    move $v1, $s4
    beq $zero, $zero, eval_restore
    nop
eval_success:
    move $v0, $zero
    move $v1, $s5
eval_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $ra, 64($sp)
    daddiu $sp, $sp, 80
    jr $ra
    nop

skip_space:
# BEGIN space_read
    beq $zero, $zero, space_return
    nop
# END space_read
# BEGIN space_classify
    beq $zero, $zero, space_return
    nop
# END space_classify
space_advance:
# BEGIN space_advance
    beq $zero, $zero, space_return
    nop
# END space_advance
space_return:
    jr $ra
    nop

parse_primary:
    daddiu $sp, $sp, -32
    sd $ra, 24($sp)
# BEGIN primary_entry
    move $v0, $zero
    beq $zero, $zero, primary_return
    nop
# END primary_entry
primary_digit:
# BEGIN primary_digit
    beq $zero, $zero, primary_return
    nop
# END primary_digit
# BEGIN primary_next
    beq $zero, $zero, primary_return
    nop
# END primary_next
primary_number:
# BEGIN primary_number
    beq $zero, $zero, primary_return
    nop
# END primary_number
primary_group:
# BEGIN primary_group_call
    beq $zero, $zero, primary_return
    nop
# END primary_group_call
# BEGIN primary_close
    beq $zero, $zero, primary_return
    nop
# END primary_close
primary_overflow:
# BEGIN primary_overflow
    beq $zero, $zero, primary_return
    nop
# END primary_overflow
primary_syntax:
# BEGIN primary_syntax
    beq $zero, $zero, primary_return
    nop
# END primary_syntax
primary_return:
    ld $ra, 24($sp)
    daddiu $sp, $sp, 32
    jr $ra
    nop

parse_unary:
    daddiu $sp, $sp, -32
    sd $ra, 24($sp)
# BEGIN unary_entry
    move $v0, $zero
    beq $zero, $zero, unary_return
    nop
# END unary_entry
unary_primary:
# BEGIN unary_primary
    beq $zero, $zero, unary_return
    nop
# END unary_primary
unary_sign:
# BEGIN unary_sign
    beq $zero, $zero, unary_return
    nop
# END unary_sign
# BEGIN unary_negate
    beq $zero, $zero, unary_return
    nop
# END unary_negate
unary_value:
    move $v0, $t0
unary_return:
    ld $ra, 24($sp)
    daddiu $sp, $sp, 32
    jr $ra
    nop

parse_term:
    daddiu $sp, $sp, -48
    sd $ra, 40($sp)
# BEGIN term_first
    move $v0, $zero
    beq $zero, $zero, term_return
    nop
# END term_first
term_loop:
# BEGIN term_probe
    beq $zero, $zero, term_return
    nop
# END term_probe
term_operator:
# BEGIN term_operator
    beq $zero, $zero, term_return
    nop
# END term_operator
# BEGIN term_apply
    beq $zero, $zero, term_return
    nop
# END term_apply
# BEGIN term_repeat
    beq $zero, $zero, term_return
    nop
# END term_repeat
term_value:
# BEGIN term_value
    beq $zero, $zero, term_return
    nop
# END term_value
term_return:
    ld $ra, 40($sp)
    daddiu $sp, $sp, 48
    jr $ra
    nop

parse_expr:
    daddiu $sp, $sp, -48
    sd $ra, 40($sp)
# BEGIN expr_first
    move $v0, $zero
    beq $zero, $zero, expr_return
    nop
# END expr_first
expr_loop:
# BEGIN expr_probe
    beq $zero, $zero, expr_return
    nop
# END expr_probe
expr_operator:
# BEGIN expr_operator
    beq $zero, $zero, expr_return
    nop
# END expr_operator
# BEGIN expr_apply
    beq $zero, $zero, expr_return
    nop
# END expr_apply
# BEGIN expr_repeat
    beq $zero, $zero, expr_return
    nop
# END expr_repeat
expr_value:
# BEGIN expr_value
    beq $zero, $zero, expr_return
    nop
# END expr_value
expr_return:
    ld $ra, 40($sp)
    daddiu $sp, $sp, 48
    jr $ra
    nop

apply_binary:
# BEGIN apply_dispatch
    beq $zero, $zero, apply_error
    nop
# END apply_dispatch
# BEGIN apply_divide
    beq $zero, $zero, apply_error
    nop
# END apply_divide
apply_add:
# BEGIN apply_add
    beq $zero, $zero, apply_error
    nop
# END apply_add
apply_subtract:
# BEGIN apply_subtract
    beq $zero, $zero, apply_error
    nop
# END apply_subtract
apply_multiply:
# BEGIN apply_multiply
    beq $zero, $zero, apply_error
    nop
# END apply_multiply
apply_check:
# BEGIN apply_check
    beq $zero, $zero, apply_error
    nop
# END apply_check
apply_zero:
# BEGIN apply_zero
    beq $zero, $zero, apply_error
    nop
# END apply_zero
apply_error:
    move $s4, $a3
    move $v0, $zero
    jr $ra
    nop
apply_value:
    move $v0, $t0
    jr $ra
    nop
