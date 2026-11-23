# Validate only the four-limb modulus. No arithmetic result is calculated here.
.data
.align 2
modulus: .word 0, 0, 0, 0
output: .word 0x5a5a5a5a
.text
.globl main
main:
    li $a0, 1
    li $a1, 1
    dla $a2, modulus
    li $a3, 1
    balc validate_modulus
    nop
validation_return:
    dla $t0, output
    lwu $t1, 0($t0)
observed_output:
    move $s0, $v0
    li $v0, 10
    syscall
validate_modulus:
    lwu $t2, 0($a2)
    lwu $t3, 4($a2)
    or $t5, $t2, $t3
    lwu $t2, 8($a2)
    or $t5, $t5, $t2
    lwu $t2, 12($a2)
    or $t5, $t5, $t2
    beqzc $t5, invalid_modulus
    nop
    move $v0, $zero
    jrc $ra
    nop
invalid_modulus:
    li $v0, 1
    jrc $ra
    nop
