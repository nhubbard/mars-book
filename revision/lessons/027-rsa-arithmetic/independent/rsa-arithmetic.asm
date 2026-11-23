# Independent unsigned RSA-arithmetic exercise: implement the seven entries.
# Preserve the supplied caller, object sizes and public contracts.
.data
.align 3
left: .word 3120,0,0,0
    .byte -91:112
left_end:
right: .word 17,0,0,0
    .byte -91:112
right_end:
modulus: .word 3233,0,0,0
    .byte -91:112
modulus_end:
exponent: .word 17,0,0,0
    .byte -91:112
exponent_end:
output: .byte -91:256
output_end:
status: .word -1
mode: .word 0
.align 3
workspace: .byte -91:1024
.text
.globl main
main:
    dla $sp, workspace
    daddiu $sp, $sp, 1024
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 0
    beqc $t0, $t1, main_gcd64
    nop
    li $t1, 1
    beqc $t0, $t1, main_pow32
    nop
    li $t1, 2
    beqc $t0, $t1, main_mul128
    nop
    li $t1, 3
    beqc $t0, $t1, main_addmod128
    nop
    li $t1, 4
    beqc $t0, $t1, main_reduce128
    nop
    li $t1, 5
    beqc $t0, $t1, main_mulmod128
    nop
    li $t1, 6
    beqc $t0, $t1, main_pow128
    nop
    bc main_done
    nop
main_gcd64:
    dla $t0, left
    ld $a0, 0($t0)
    dla $t0, right
    ld $a1, 0($t0)
gcd64_call:
    balc rsa_gcd64
    nop
gcd64_return:
    move $v0, $v1
    bc main_done
    nop
main_pow32:
    dla $t0, left
    lwu $a0, 0($t0)
    dla $t0, right
    lwu $a1, 0($t0)
    dla $t0, modulus
    lwu $a2, 0($t0)
    dla $a3, output
pow32_call:
    balc rsa_pow32
    nop
pow32_return:
    bc main_done
    nop
main_mul128:
    dla $a0, left
    dla $a1, right
    dla $a2, output
mul128_call:
    balc rsa_mul128
    nop
mul128_return:
    bc main_done
    nop
main_addmod128:
    dla $a0, left
    dla $a1, right
    dla $a2, modulus
    dla $a3, output
addmod128_call:
    balc rsa_addmod128
    nop
addmod128_return:
    bc main_done
    nop
main_reduce128:
    dla $a0, left
    dla $a1, modulus
    dla $a2, output
reduce128_call:
    balc rsa_reduce128
    nop
reduce128_return:
    bc main_done
    nop
main_mulmod128:
    dla $a0, left
    dla $a1, right
    dla $a2, modulus
    dla $a3, output
mulmod128_call:
    balc rsa_mulmod128
    nop
mulmod128_return:
    bc main_done
    nop
main_pow128:
    dla $a0, left
    dla $a1, exponent
    dla $a2, modulus
    dla $a3, output
pow128_call:
    balc rsa_pow128
    nop
pow128_return:
    bc main_done
    nop
main_done:
    dla $t0, status
    sw $v0, 0($t0)
    li $v0, 10
    syscall
function_return:
    nop
rsa_gcd64:
    break

rsa_pow32:
    break

rsa_mul128:
    break

rsa_addmod128:
    break

rsa_reduce128:
    break

rsa_mulmod128:
    break

rsa_pow128:
    break
