# Reduce only four input bits, 1101, modulo five.
# This bounded experiment does not implement the 128-bit interface.
.text
.globl main
main:
    li $t0, 13
    li $t1, 5
    move $t2, $zero
    li $t3, 3
next_bit:
    dsrlv $t4, $t0, $t3
    andi $t4, $t4, 1
    dsll $t6, $t2, 1
    or $t6, $t6, $t4
    sltu $t7, $t6, $t1
    bnezc $t7, reduced_prefix
    nop
    dsubu $t6, $t6, $t1
reduced_prefix:
    move $t2, $t6
    daddiu $t3, $t3, -1
    bgezc $t3, next_bit
    nop
prefixes_done:
    li $v0, 10
    syscall
