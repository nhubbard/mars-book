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
    beq $t0, $t1, main_gcd64
    nop
    li $t1, 1
    beq $t0, $t1, main_pow32
    nop
    li $t1, 2
    beq $t0, $t1, main_mul128
    nop
    li $t1, 3
    beq $t0, $t1, main_addmod128
    nop
    li $t1, 4
    beq $t0, $t1, main_reduce128
    nop
    li $t1, 5
    beq $t0, $t1, main_mulmod128
    nop
    li $t1, 6
    beq $t0, $t1, main_pow128
    nop
    b main_done
    nop
main_gcd64:
    dla $t0, left
    ld $a0, 0($t0)
    dla $t0, right
    ld $a1, 0($t0)
gcd64_call:
    jal rsa_gcd64
    nop
gcd64_return:
    move $v0, $v1
    b main_done
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
    jal rsa_pow32
    nop
pow32_return:
    b main_done
    nop
main_mul128:
    dla $a0, left
    dla $a1, right
    dla $a2, output
mul128_call:
    jal rsa_mul128
    nop
mul128_return:
    b main_done
    nop
main_addmod128:
    dla $a0, left
    dla $a1, right
    dla $a2, modulus
    dla $a3, output
addmod128_call:
    jal rsa_addmod128
    nop
addmod128_return:
    b main_done
    nop
main_reduce128:
    dla $a0, left
    dla $a1, modulus
    dla $a2, output
reduce128_call:
    jal rsa_reduce128
    nop
reduce128_return:
    b main_done
    nop
main_mulmod128:
    dla $a0, left
    dla $a1, right
    dla $a2, modulus
    dla $a3, output
mulmod128_call:
    jal rsa_mulmod128
    nop
mulmod128_return:
    b main_done
    nop
main_pow128:
    dla $a0, left
    dla $a1, exponent
    dla $a2, modulus
    dla $a3, output
pow128_call:
    jal rsa_pow128
    nop
pow128_return:
    b main_done
    nop
main_done:
    dla $t0, status
    sw $v0, 0($t0)
    li $v0, 10
    syscall
function_return:
    nop
rsa_gcd64:
# BEGIN gcd_pair
    b development
    nop
# END gcd_pair
# BEGIN gcd_remainders
gcd_loop:
    b development
    nop
# END gcd_remainders
# BEGIN gcd_return
gcd_done:
    b development
    nop
# END gcd_return
rsa_pow32:
# BEGIN word_identity
    b development
    nop
# END word_identity
# BEGIN word_select
pow32_loop:
    b development
    nop
# END word_select
# BEGIN word_shift
pow32_shift:
    b development
    nop
# END word_shift
# BEGIN word_publish
pow32_done:
    b development
    nop
# END word_publish
rsa_mul128:
# BEGIN product_clear
mul_clear:
    b development
    nop
# END product_clear
after_product_clear:
# BEGIN product_row
mul_outer:
    b development
    nop
# END product_row
# BEGIN product_partial
mul_inner:
    b development
    nop
# END product_partial
after_product_partial:
# BEGIN product_columns
    b development
    nop
# END product_columns
after_product_columns:
# BEGIN product_rows
    b development
    nop
# END product_rows
after_product_rows:
# BEGIN product_publish
    b development
    nop
# END product_publish
rsa_addmod128:
# BEGIN add_guard
    b development
    nop
# END add_guard
# BEGIN add_limbs
addmod_sum:
    b development
    nop
# END add_limbs
after_add_limbs:
# BEGIN add_compare
addmod_compare:
    b development
    nop
# END add_compare
# BEGIN add_borrow
addmod_subtract:
addmod_borrow:
addmod_done:
    b development
    nop
# END add_borrow
rsa_reduce128:
# BEGIN reduce_guard
    b development
    nop
# END reduce_guard
# BEGIN reduce_compare
reduce_begin_compare:
reduce_compare:
    b development
    nop
# END reduce_compare
reduce_slow:
    daddiu $sp, $sp, -80
    sd $s0, 32($sp)
    sd $s1, 40($sp)
    sd $s2, 48($sp)
    sd $s3, 56($sp)
    sd $ra, 64($sp)
# BEGIN reduce_identity
    b reduce_development
    nop
# END reduce_identity
# BEGIN reduce_bit
reduce_bit:
    b reduce_development
    nop
# END reduce_bit
after_reduce_bit:
# BEGIN reduce_accumulate
reduce_next:
    b reduce_development
    nop
# END reduce_accumulate
after_reduce_accumulate:
# BEGIN reduce_publish
    b reduce_development
    nop
# END reduce_publish
after_reduce_publish:
    move $v0, $zero
    b reduce_restore
    nop
reduce_development:
    li $v0, 3
reduce_restore:
    ld $s0, 32($sp)
    ld $s1, 40($sp)
    ld $s2, 48($sp)
    ld $s3, 56($sp)
    ld $ra, 64($sp)
    daddiu $sp, $sp, 80
    jr $ra
    nop
reduce_copy_input:
    lwu $t0, 0($a0)
    sw $t0, 0($a2)
    lwu $t0, 4($a0)
    sw $t0, 4($a2)
    lwu $t0, 8($a0)
    sw $t0, 8($a2)
    lwu $t0, 12($a0)
    sw $t0, 12($a2)
    move $v0, $zero
    jr $ra
    nop
reduce_modulus_one:
    sw $zero, 0($a2)
    sw $zero, 4($a2)
    sw $zero, 8($a2)
    sw $zero, 12($a2)
    move $v0, $zero
    jr $ra
    nop
rsa_mulmod128:
# BEGIN modproduct_guard
    b development
    nop
# END modproduct_guard
mulmod_frame:
    daddiu $sp, $sp, -80
    sd $s0, 48($sp)
    sd $s1, 56($sp)
    sd $ra, 64($sp)
# BEGIN modproduct_copy
    b mulmod_development
    nop
# END modproduct_copy
# BEGIN modproduct_select
mulmod_loop:
    b mulmod_development
    nop
# END modproduct_select
# BEGIN modproduct_shift
mulmod_shift:
    b mulmod_development
    nop
# END modproduct_shift
after_modproduct_shift:
# BEGIN modproduct_double
    b mulmod_development
    nop
# END modproduct_double
# BEGIN modproduct_publish
mulmod_done:
    b mulmod_development
    nop
# END modproduct_publish
after_modproduct_publish:
    move $v0, $zero
    b mulmod_restore
    nop
mulmod_development:
    li $v0, 3
mulmod_restore:
    ld $s0, 48($sp)
    ld $s1, 56($sp)
    ld $ra, 64($sp)
    daddiu $sp, $sp, 80
    jr $ra
    nop
rsa_pow128:
# BEGIN power_guard
    b development
    nop
# END power_guard
pow128_frame:
    daddiu $sp, $sp, -96
    sd $s0, 64($sp)
    sd $s1, 72($sp)
    sd $ra, 80($sp)
# BEGIN power_identity
    b pow128_development
    nop
# END power_identity
# BEGIN power_select
pow128_loop:
    b pow128_development
    nop
# END power_select
# BEGIN power_shift
pow128_shift:
    b pow128_development
    nop
# END power_shift
after_power_shift:
# BEGIN power_square
    b pow128_development
    nop
# END power_square
# BEGIN power_publish
pow128_done:
    b pow128_development
    nop
# END power_publish
after_power_publish:
    move $v0, $zero
    b pow128_restore
    nop
pow128_development:
    li $v0, 3
pow128_restore:
    ld $s0, 64($sp)
    ld $s1, 72($sp)
    ld $ra, 80($sp)
    daddiu $sp, $sp, 96
    jr $ra
    nop
development:
    li $v0, 3
    li $v1, 3
    jr $ra
    nop
rsa_invalid:
    li $v0, 1
    jr $ra
    nop
