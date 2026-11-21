.data
.align 2
cursor_result: .word -1
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    li $a0, 6
    li $a1, 7
    balc advance_cursor
cursor_done:
    sw $v0, cursor_result
    li $v0, 10
    syscall

advance_cursor:
    addiu $v0, $a0, 1
    bnec $v0, $a1, cursor_return
    nop
    move $v0, $zero
cursor_return:
    jrc $ra
