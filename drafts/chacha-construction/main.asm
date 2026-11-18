.data
.align 3
source: .byte -91:4160
source_end:
destination: .byte -91:4160
destination_end:
state: .word 0x61707865,0x3320646e,0x79622d32,0x6b206574
    .word 0x03020100,0x07060504,0x0b0a0908,0x0f0e0d0c
    .word 0x13121110,0x17161514,0x1b1a1918,0x1f1e1d1c
    .word 1,0x09000000,0x4a000000,0
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
mode: .word 0
in_place: .word 0
length: .dword 65
status: .word -1
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $t0, mode
    lw $t0, 0($t0)
    beqz $t0, main_quarter
    nop
    li $t1, 1
    beq $t0, $t1, main_scalar
    nop
    li $t1, 2
    beq $t0, $t1, main_msa
    nop
    b main_stream
    nop
main_quarter:
    dla $a0, quad
    daddiu $a1, $a0, 20
    daddiu $a2, $a0, 40
    daddiu $a3, $a0, 60
quarter_call:
    jal chacha_quarter
    nop
    b main_done
    nop
main_scalar:
    dla $a0, state
    dla $a1, destination
scalar_call:
    jal chacha_block_scalar
    nop
    b main_done
    nop
main_msa:
    dla $a0, state
    dla $a1, destination
msa_call:
    jal chacha_block_msa
    nop
    b main_done
    nop
main_stream:
    dla $a0, state
    dla $a1, source
    dla $a2, destination
    dla $t0, length
    ld $a3, 0($t0)
    dla $t0, in_place
    lw $t0, 0($t0)
    beqz $t0, stream_call
    nop
    move $a2, $a1
stream_call:
    jal chacha_xor
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
# BEGIN qr_load
    b quarter_development
    nop
# END qr_load
after_qr_load:
# BEGIN qr_group1
    b quarter_development
    nop
# END qr_group1
after_qr_group1:
# BEGIN qr_group2
    b quarter_development
    nop
# END qr_group2
after_qr_group2:
# BEGIN qr_group3
    b quarter_development
    nop
# END qr_group3
after_qr_group3:
# BEGIN qr_group4
    b quarter_development
    nop
# END qr_group4
after_qr_group4:
# BEGIN qr_store
    b quarter_development
    nop
# END qr_store
after_qr_store:
    move $v0, $zero
    jr $ra
    nop
quarter_development:
    li $v0, 3
    jr $ra
    nop
chacha_block_scalar:
    daddiu $sp, $sp, -48
    sd $ra, 40($sp)
    sd $s0, 32($sp)
    sd $s1, 24($sp)
    sd $s2, 16($sp)
# BEGIN scalar_setup
    b scalar_development
    nop
# END scalar_setup
after_scalar_setup:
scalar_copy:
# BEGIN scalar_copy
    b scalar_development
    nop
# END scalar_copy
after_scalar_copy:
# BEGIN scalar_count
    b scalar_development
    nop
# END scalar_count
after_scalar_count:
scalar_double_round:
# BEGIN scalar_call1
    b scalar_development
    nop
# END scalar_call1
after_scalar_call1:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_call2
    b scalar_development
    nop
# END scalar_call2
after_scalar_call2:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_call3
    b scalar_development
    nop
# END scalar_call3
after_scalar_call3:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_call4
    b scalar_development
    nop
# END scalar_call4
after_scalar_call4:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_call5
    b scalar_development
    nop
# END scalar_call5
after_scalar_call5:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_call6
    b scalar_development
    nop
# END scalar_call6
after_scalar_call6:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_call7
    b scalar_development
    nop
# END scalar_call7
after_scalar_call7:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_call8
    b scalar_development
    nop
# END scalar_call8
after_scalar_call8:
    bnez $v0, scalar_development
    nop
# BEGIN scalar_repeat
    b scalar_development
    nop
# END scalar_repeat
after_scalar_repeat:
scalar_feed:
# BEGIN scalar_feed
    b scalar_development
    nop
# END scalar_feed
after_scalar_feed:
    ld $s2, 16($sp)
    ld $s1, 24($sp)
    ld $s0, 32($sp)
    ld $ra, 40($sp)
    daddiu $sp, $sp, 48
    move $v0, $zero
    jr $ra
    nop
scalar_development:
    li $v0, 3
    ld $s2, 16($sp)
    ld $s1, 24($sp)
    ld $s0, 32($sp)
    ld $ra, 40($sp)
    daddiu $sp, $sp, 48
    jr $ra
    nop
chacha_block_msa:
# BEGIN msa_load
    b msa_development
    nop
# END msa_load
after_msa_load:
msa_double_round:
# BEGIN msa_column_a
    b msa_development
    nop
# END msa_column_a
after_msa_column_a:
# BEGIN msa_column_b
    b msa_development
    nop
# END msa_column_b
after_msa_column_b:
# BEGIN msa_select
    b msa_development
    nop
# END msa_select
after_msa_select:
# BEGIN msa_diagonal_a
    b msa_development
    nop
# END msa_diagonal_a
after_msa_diagonal_a:
# BEGIN msa_diagonal_b
    b msa_development
    nop
# END msa_diagonal_b
after_msa_diagonal_b:
# BEGIN msa_repeat
    b msa_development
    nop
# END msa_repeat
after_msa_repeat:
# BEGIN msa_feed
    b msa_development
    nop
# END msa_feed
after_msa_feed:
    move $v0, $zero
    jr $ra
    nop
msa_development:
    li $v0, 3
    jr $ra
    nop
chacha_xor:
# BEGIN stream_validate
    b stream_unframed_development
    nop
# END stream_validate
after_stream_validate:
# BEGIN stream_counter_check
    b stream_unframed_development
    nop
# END stream_counter_check
after_stream_counter_check:
    daddiu $sp, $sp, -192
    sd $ra, 184($sp)
    sd $s0, 176($sp)
    sd $s1, 168($sp)
    sd $s2, 160($sp)
    sd $s3, 152($sp)
    sd $s4, 144($sp)
# BEGIN stream_setup
    b stream_development
    nop
# END stream_setup
after_stream_setup:
stream_copy_state:
# BEGIN stream_copy
    b stream_development
    nop
# END stream_copy
after_stream_copy:
stream_next_block:
# BEGIN stream_block
    b stream_development
    nop
# END stream_block
after_stream_block:
    bnez $v0, stream_development
    nop
# BEGIN stream_count
    b stream_development
    nop
# END stream_count
after_stream_count:
stream_count_ready:
# BEGIN stream_bytes
    b stream_development
    nop
# END stream_bytes
after_stream_bytes:
# BEGIN stream_remaining
    b stream_development
    nop
# END stream_remaining
after_stream_remaining:
# BEGIN stream_advance
    b stream_development
    nop
# END stream_advance
after_stream_advance:
stream_done:
    move $v0, $zero
    b stream_restore
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
    jr $ra
    nop
stream_empty:
    move $v0, $zero
    jr $ra
    nop
stream_unframed_development:
    li $v0, 3
    jr $ra
    nop
stream_length_error:
    li $v0, 1
    jr $ra
    nop
stream_counter_error:
    li $v0, 2
    jr $ra
    nop
