# One base-2^32 addition column, with a supplied carry from the previous column.
.text
.globl main
main:
    li $t0, -1
    dsrl $t0, $t0, 32
    move $t1, $t0
    li $t2, 1
column_inputs:
    daddu $t3, $t0, $t1
    daddu $t3, $t3, $t2
column_sum:
    dsll $t4, $t3, 32
    dsrl $t4, $t4, 32
    dsrl $t5, $t3, 32
column_parts:
    li $v0, 10
    syscall
