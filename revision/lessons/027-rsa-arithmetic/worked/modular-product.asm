# Two explicit multiplier-bit contributions: 250 times binary 11 modulo 251.
# A general multiword implementation cannot use one scalar multiply for the whole input.
.text
.globl main
main:
    li $t0, 250
    li $t1, 251
    move $t3, $zero
    daddu $t3, $t3, $t0
    dmodu $t3, $t3, $t1
first_contribution:
    daddu $t0, $t0, $t0
    dmodu $t0, $t0, $t1
doubled_residue:
    daddu $t3, $t3, $t0
    dmodu $t3, $t3, $t1
second_contribution:
    li $v0, 10
    syscall
