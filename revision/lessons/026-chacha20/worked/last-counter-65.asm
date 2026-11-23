# Complete worked ChaCha20 example for Chapter 22.
# Retain the supplied caller objects when running package 026 checks.
.data
.align 3
source: .byte 129,202,19,92,165,238,55,128,201,18,91,164,237,54,127,200,17,90,163,236,53,126,199,16,89,162,235,52,125,198,15,88,161,234,51,124,197,14,87,160,233,50,123,196,13,86,159,232,49,122,195,12,85,158,231,48,121,194,11,84,157,230,47,120,193
    .byte -91:4095
source_end:
destination: .byte -91:4160
destination_end:
state: .word 0x61707865,0x3320646e,0x79622d32,0x6b206574
    .word 0x03020100,0x07060504,0x0b0a0908,0x0f0e0d0c
    .word 0x13121110,0x17161514,0x1b1a1918,0x1f1e1d1c
    .word 0xffffffff,0x09000000,0x4a000000,0
    .byte -91:64
state_end:
quad: .word 0x11111111
    .byte -91:16
    .word 0x01020304
    .byte -91:16
    .word 0x9b8d6f43
    .byte -91:16
    .word 0x01234567
    .byte -91:32
quad_end:
mode: .word 3
in_place: .word 0
length: .dword 65
status: .word -1
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $t0, mode
    lw $t0, 0($t0)
    beqzc $t0, main_quarter
    nop
    li $t1, 1
    beqc $t0, $t1, main_scalar
    nop
    li $t1, 2
    beqc $t0, $t1, main_msa
    nop
    bc main_stream
    nop
main_quarter:
    dla $a0, quad
    daddiu $a1, $a0, 20
    daddiu $a2, $a0, 40
    daddiu $a3, $a0, 60
quarter_call:
    balc chacha_quarter
    nop
    bc main_done
    nop
main_scalar:
    dla $a0, state
    dla $a1, destination
scalar_call:
    balc chacha_block_scalar
    nop
    bc main_done
    nop
main_msa:
    dla $a0, state
    dla $a1, destination
msa_call:
    balc chacha_block_msa
    nop
    bc main_done
    nop
main_stream:
    dla $a0, state
    dla $a1, source
    dla $a2, destination
    dla $t0, length
    ld $a3, 0($t0)
    dla $t0, in_place
    lw $t0, 0($t0)
    beqzc $t0, stream_call
    nop
    move $a2, $a1
stream_call:
    balc chacha_xor
    nop
main_done:
    dla $t0, status
    sw $v0, 0($t0)
    li $v0, 10
    syscall
function_return:
    li $v0, 10
    syscall
chacha_quarter:
    lw $t0, 0($a0)
    lw $t1, 0($a1)
    lw $t2, 0($a2)
    lw $t3, 0($a3)
after_qr_load:
    addu $t0, $t0, $t1
    xor $t3, $t3, $t0
    rotr $t3, $t3, 16
after_qr_group1:
    addu $t2, $t2, $t3
    xor $t1, $t1, $t2
    rotr $t1, $t1, 20
after_qr_group2:
    addu $t0, $t0, $t1
    xor $t3, $t3, $t0
    rotr $t3, $t3, 24
after_qr_group3:
    addu $t2, $t2, $t3
    xor $t1, $t1, $t2
    rotr $t1, $t1, 25
after_qr_group4:
    sw $t0, 0($a0)
    sw $t1, 0($a1)
    sw $t2, 0($a2)
    sw $t3, 0($a3)
after_qr_store:
    move $v0, $zero
    jrc $ra
    nop
quarter_development:
    li $v0, 3
    jrc $ra
    nop
chacha_block_scalar:
    daddiu $sp, $sp, -48
    sd $ra, 40($sp)
    sd $s0, 32($sp)
    sd $s1, 24($sp)
    sd $s2, 16($sp)
    move $s0, $a0
    move $s1, $a1
    move $t0, $zero
after_scalar_setup:
scalar_copy:
    daddu $t1, $s0, $t0
    lw $t2, 0($t1)
    daddu $t1, $s1, $t0
    sw $t2, 0($t1)
    daddiu $t0, $t0, 4
    sltiu $t1, $t0, 64
    bnezc $t1, scalar_copy
    nop
after_scalar_copy:
    li $s2, 10
after_scalar_count:
scalar_double_round:
    daddiu $a0, $s1, 0
    daddiu $a1, $s1, 16
    daddiu $a2, $s1, 32
    daddiu $a3, $s1, 48
    balc chacha_quarter
    nop
after_scalar_call1:
    bnezc $v0, scalar_development
    nop
    daddiu $a0, $s1, 4
    daddiu $a1, $s1, 20
    daddiu $a2, $s1, 36
    daddiu $a3, $s1, 52
    balc chacha_quarter
    nop
after_scalar_call2:
    bnezc $v0, scalar_development
    nop
    daddiu $a0, $s1, 8
    daddiu $a1, $s1, 24
    daddiu $a2, $s1, 40
    daddiu $a3, $s1, 56
    balc chacha_quarter
    nop
after_scalar_call3:
    bnezc $v0, scalar_development
    nop
    daddiu $a0, $s1, 12
    daddiu $a1, $s1, 28
    daddiu $a2, $s1, 44
    daddiu $a3, $s1, 60
    balc chacha_quarter
    nop
after_scalar_call4:
    bnezc $v0, scalar_development
    nop
    daddiu $a0, $s1, 0
    daddiu $a1, $s1, 20
    daddiu $a2, $s1, 40
    daddiu $a3, $s1, 60
    balc chacha_quarter
    nop
after_scalar_call5:
    bnezc $v0, scalar_development
    nop
    daddiu $a0, $s1, 4
    daddiu $a1, $s1, 24
    daddiu $a2, $s1, 44
    daddiu $a3, $s1, 48
    balc chacha_quarter
    nop
after_scalar_call6:
    bnezc $v0, scalar_development
    nop
    daddiu $a0, $s1, 8
    daddiu $a1, $s1, 28
    daddiu $a2, $s1, 32
    daddiu $a3, $s1, 52
    balc chacha_quarter
    nop
after_scalar_call7:
    bnezc $v0, scalar_development
    nop
    daddiu $a0, $s1, 12
    daddiu $a1, $s1, 16
    daddiu $a2, $s1, 36
    daddiu $a3, $s1, 56
    balc chacha_quarter
    nop
after_scalar_call8:
    bnezc $v0, scalar_development
    nop
    addiu $s2, $s2, -1
    bgtzc $s2, scalar_double_round
    nop
    move $t0, $zero
after_scalar_repeat:
scalar_feed:
    daddu $t1, $s0, $t0
    lw $t2, 0($t1)
    daddu $t1, $s1, $t0
    lw $t3, 0($t1)
    addu $t2, $t2, $t3
    sw $t2, 0($t1)
    daddiu $t0, $t0, 4
    sltiu $t1, $t0, 64
    bnezc $t1, scalar_feed
    nop
after_scalar_feed:
    ld $s2, 16($sp)
    ld $s1, 24($sp)
    ld $s0, 32($sp)
    ld $ra, 40($sp)
    daddiu $sp, $sp, 48
    move $v0, $zero
    jrc $ra
    nop
scalar_development:
    li $v0, 3
    ld $s2, 16($sp)
    ld $s1, 24($sp)
    ld $s0, 32($sp)
    ld $ra, 40($sp)
    daddiu $sp, $sp, 48
    jrc $ra
    nop
chacha_block_msa:
    ld.w $w0, 0($a0)
    move.v $w4, $w0
    ld.w $w1, 16($a0)
    move.v $w5, $w1
    ld.w $w2, 32($a0)
    move.v $w6, $w2
    ld.w $w3, 48($a0)
    move.v $w7, $w3
    li $t0, 10
after_msa_load:
msa_double_round:
    addv.w $w0, $w0, $w1
    xor.v $w3, $w3, $w0
    slli.w $w8, $w3, 16
    srli.w $w9, $w3, 16
    or.v $w3, $w8, $w9
    addv.w $w2, $w2, $w3
    xor.v $w1, $w1, $w2
    slli.w $w8, $w1, 12
    srli.w $w9, $w1, 20
    or.v $w1, $w8, $w9
after_msa_column_a:
    addv.w $w0, $w0, $w1
    xor.v $w3, $w3, $w0
    slli.w $w8, $w3, 8
    srli.w $w9, $w3, 24
    or.v $w3, $w8, $w9
    addv.w $w2, $w2, $w3
    xor.v $w1, $w1, $w2
    slli.w $w8, $w1, 7
    srli.w $w9, $w1, 25
    or.v $w1, $w8, $w9
after_msa_column_b:
    shf.w $w1, $w1, 57
    shf.w $w2, $w2, 78
    shf.w $w3, $w3, 147
after_msa_select:
    addv.w $w0, $w0, $w1
    xor.v $w3, $w3, $w0
    slli.w $w8, $w3, 16
    srli.w $w9, $w3, 16
    or.v $w3, $w8, $w9
    addv.w $w2, $w2, $w3
    xor.v $w1, $w1, $w2
    slli.w $w8, $w1, 12
    srli.w $w9, $w1, 20
    or.v $w1, $w8, $w9
after_msa_diagonal_a:
    addv.w $w0, $w0, $w1
    xor.v $w3, $w3, $w0
    slli.w $w8, $w3, 8
    srli.w $w9, $w3, 24
    or.v $w3, $w8, $w9
    addv.w $w2, $w2, $w3
    xor.v $w1, $w1, $w2
    slli.w $w8, $w1, 7
    srli.w $w9, $w1, 25
    or.v $w1, $w8, $w9
after_msa_diagonal_b:
    shf.w $w1, $w1, 147
    shf.w $w2, $w2, 78
    shf.w $w3, $w3, 57
    addiu $t0, $t0, -1
    bgtzc $t0, msa_double_round
    nop
after_msa_repeat:
    addv.w $w0, $w0, $w4
    st.w $w0, 0($a1)
    addv.w $w1, $w1, $w5
    st.w $w1, 16($a1)
    addv.w $w2, $w2, $w6
    st.w $w2, 32($a1)
    addv.w $w3, $w3, $w7
    st.w $w3, 48($a1)
after_msa_feed:
    move $v0, $zero
    jrc $ra
    nop
msa_development:
    li $v0, 3
    jrc $ra
    nop
chacha_xor:
    sltiu $t0, $a3, 4097
    beqzc $t0, stream_length_error
    nop
    beqzc $a3, stream_empty
    nop
after_stream_validate:
    daddiu $t0, $a3, 63
    dsrl $t0, $t0, 6
    lwu $t1, 48($a0)
    daddu $t0, $t0, $t1
    daddiu $t0, $t0, -1
    dsrl $t0, $t0, 32
    bnezc $t0, stream_counter_error
    nop
after_stream_counter_check:
    daddiu $sp, $sp, -192
    sd $ra, 184($sp)
    sd $s0, 176($sp)
    sd $s1, 168($sp)
    sd $s2, 160($sp)
    sd $s3, 152($sp)
    sd $s4, 144($sp)
    move $s0, $a1
    move $s1, $a2
    move $s2, $a3
    move $t0, $zero
after_stream_setup:
stream_copy_state:
    daddu $t1, $a0, $t0
    lw $t2, 0($t1)
    daddu $t1, $sp, $t0
    sw $t2, 0($t1)
    daddiu $t0, $t0, 4
    sltiu $t1, $t0, 64
    bnezc $t1, stream_copy_state
    nop
after_stream_copy:
stream_next_block:
    move $a0, $sp
    daddiu $a1, $sp, 64
    balc chacha_block_scalar
    nop
after_stream_block:
    bnezc $v0, stream_development
    nop
    li $s3, 64
    sltiu $t0, $s2, 64
    beqzc $t0, stream_count_ready
    nop
    move $s3, $s2
after_stream_count:
stream_count_ready:
    move $s4, $zero
stream_byte:
    daddu $t0, $sp, $s4
    lbu $t1, 64($t0)
    lbu $t2, 0($s0)
    xor $t1, $t1, $t2
    sb $t1, 0($s1)
    daddiu $s0, $s0, 1
    daddiu $s1, $s1, 1
    daddiu $s4, $s4, 1
    bnec $s4, $s3, stream_byte
    nop
after_stream_bytes:
    dsubu $s2, $s2, $s3
    beqzc $s2, stream_done
    nop
after_stream_remaining:
    lw $t0, 48($sp)
    addiu $t0, $t0, 1
    sw $t0, 48($sp)
    bc stream_next_block
    nop
after_stream_advance:
stream_done:
    move $v0, $zero
    bc stream_restore
    nop
stream_development:
    li $v0, 3
stream_restore:
    ld $s4, 144($sp)
    ld $s3, 152($sp)
    ld $s2, 160($sp)
    ld $s1, 168($sp)
    ld $s0, 176($sp)
    ld $ra, 184($sp)
    daddiu $sp, $sp, 192
    jrc $ra
    nop
stream_empty:
    move $v0, $zero
    jrc $ra
    nop
stream_unframed_development:
    li $v0, 3
    jrc $ra
    nop
stream_length_error:
    li $v0, 1
    jrc $ra
    nop
stream_counter_error:
    li $v0, 2
    jrc $ra
    nop
