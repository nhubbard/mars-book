# Complete arithmetic example, not a complete expression parser.
# checked_binary: a0/a1 signed-word operands; a2 one of ASCII + - * /;
# a3 operator byte offset. v0 status, v1 signed value or error offset.
# Preserves saved registers and caller memory; performs no I/O.
.data
product: .word 99
status: .word 99
result: .word 99
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    li $a0, 3
    li $a1, -2
    li $a2, 42
    li $a3, 7
    balc checked_binary
    nop
product_ready:
    nop
    bnezc $v0, arithmetic_store
    nop
    dla $t0, product
    sw $v1, 0($t0)
    li $a0, 18
    move $a1, $v1
    li $a2, 45
    li $a3, 3
    balc checked_binary
    nop
arithmetic_ready:
    nop
arithmetic_store:
    dla $t0, status
    sw $v0, 0($t0)
    sw $v1, 4($t0)
arithmetic_stored:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

checked_binary:
    li $t0, 43
    beqc $a2, $t0, checked_add
    nop
    li $t0, 45
    beqc $a2, $t0, checked_subtract
    nop
    li $t0, 42
    beqc $a2, $t0, checked_multiply
    nop
    beqzc $a1, checked_zero
    nop
    ddiv $t0, $a0, $a1
    bc checked_range
    nop
checked_add:
    daddu $t0, $a0, $a1
    bc checked_range
    nop
checked_subtract:
    dsubu $t0, $a0, $a1
    bc checked_range
    nop
checked_multiply:
    dmul $t0, $a0, $a1
checked_range:
    sll $t1, $t0, 0
    beqc $t1, $t0, checked_value
    nop
    li $v0, -2
    move $v1, $a3
    jrc $ra
    nop
checked_zero:
    li $v0, -3
    move $v1, $a3
    jrc $ra
    nop
checked_value:
    move $v0, $zero
    move $v1, $t0
    jrc $ra
    nop
