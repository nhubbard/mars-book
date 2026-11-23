# One logical shift of a supplied 128-bit exponent held in two doublewords.
# This experiment does not calculate modular exponentiation.
.text
.globl main
main:
    dli $s0, 0x8000000000000001
    li $s1, 1
exponent_inputs:
    andi $t0, $s1, 1
    dsll $t1, $s0, 63
    dsrl $t2, $s1, 1
    or $t2, $t2, $t1
    dsrl $t3, $s0, 1
shifted_exponent:
    li $t4, 1
    li $t5, 1
    dmodu $t4, $t4, $t5
unit_identity:
    li $v0, 10
    syscall
