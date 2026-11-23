# Unsigned remainder experiments, not a complete gcd implementation.
.text
.globl main
main:
    li $t0, 13
    li $t1, 11
    dmodu $t2, $t0, $t1
first_remainder:
    move $t0, $t1
    move $t1, $t2
    dmodu $t2, $t0, $t1
second_remainder:
    li $t0, -1
    li $t1, 17
    dmodu $t2, $t0, $t1
    dmod $t3, $t0, $t1
high_remainder:
    li $v0, 10
    syscall
