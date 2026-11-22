.text
.globl main
main:
    li $t0, 0x7fffffff
    addiu $t1, $t0, 1
    daddiu $t2, $t0, 1
inspect:
    li $v0, 10
    syscall
