.data
.align 6
words: .word 1000,1002,1001,1001,20000
       .word 0x6a6b6c6d:35
words_end: .word 0x13579bdf
.align 6
stream: .byte -91:192
stream_end: .word 0x2468ace0
word_count: .word 5
byte_capacity: .word 160
byte_count: .word 8
word_capacity: .word 32
method: .word 0
input_used: .word -1
output_used: .word -1
status: .word -1
control_end: .word 0x31415926
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $t0, method
    lw $t0, 0($t0)
    bne $t0, $zero, choose_decode
    nop
    dla $a0, words
    dla $t0, word_count
    lw $a1, 0($t0)
    dla $a2, stream
    dla $t0, byte_capacity
    lw $a3, 0($t0)
    dla $t9, delta_encode
    beq $zero, $zero, codec_call
    nop
choose_decode:
    dla $a0, stream
    dla $t0, byte_count
    lw $a1, 0($t0)
    dla $a2, words
    dla $t0, word_capacity
    lw $a3, 0($t0)
    dla $t9, delta_decode
codec_call:
    jalr $t9
    nop
codec_done:
    dla $t0, input_used
    sw $v0, 0($t0)
    dla $t0, output_used
    sw $v1, 0($t0)
    dla $t0, status
    sw $a3, 0($t0)
results_ready:
    li $v0, 10
    syscall
function_return:
    beq $zero, $zero, codec_done
    nop

delta_encode:
    daddiu $sp, $sp, -80
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    move $s4, $zero
    move $s5, $zero
    move $s6, $zero
    move $a3, $zero
enc_next:
# BEGIN e_end
    beq $zero, $zero, enc_return
    nop
# END e_end
# BEGIN e_capacity
    beq $zero, $zero, enc_return
    nop
# END e_capacity
# BEGIN e_load
    beq $zero, $zero, enc_return
    nop
# END e_load
# BEGIN e_difference
    beq $zero, $zero, enc_return
    nop
# END e_difference
enc_map:
# BEGIN e_double
    beq $zero, $zero, enc_return
    nop
# END e_double
# BEGIN e_sign
    beq $zero, $zero, enc_return
    nop
# END e_sign
# BEGIN e_map
    beq $zero, $zero, enc_return
    nop
# END e_map
# BEGIN e_length_setup
    beq $zero, $zero, enc_return
    nop
# END e_length_setup
enc_length:
# BEGIN e_length_shift
    beq $zero, $zero, enc_return
    nop
# END e_length_shift
# BEGIN e_length_end
    beq $zero, $zero, enc_return
    nop
# END e_length_end
# BEGIN e_length_next
    beq $zero, $zero, enc_return
    nop
# END e_length_next
enc_check_capacity:
# BEGIN e_remaining
    beq $zero, $zero, enc_return
    nop
# END e_remaining
# BEGIN e_fit
    beq $zero, $zero, enc_return
    nop
# END e_fit
# BEGIN e_output_address
    beq $zero, $zero, enc_return
    nop
# END e_output_address
enc_byte:
# BEGIN e_payload
    beq $zero, $zero, enc_return
    nop
# END e_payload
# BEGIN e_emission_shift
    beq $zero, $zero, enc_return
    nop
# END e_emission_shift
# BEGIN e_terminal
    beq $zero, $zero, enc_return
    nop
# END e_terminal
# BEGIN e_flag
    beq $zero, $zero, enc_return
    nop
# END e_flag
# BEGIN e_store_more
    beq $zero, $zero, enc_return
    nop
# END e_store_more
# BEGIN e_next_byte
    beq $zero, $zero, enc_return
    nop
# END e_next_byte
enc_last_byte:
# BEGIN e_store_last
    beq $zero, $zero, enc_return
    nop
# END e_store_last
# BEGIN e_commit_reading
    beq $zero, $zero, enc_return
    nop
# END e_commit_reading
# BEGIN e_next_value
    beq $zero, $zero, enc_return
    nop
# END e_next_value
enc_full:
    li $a3, 1
enc_return:
    move $v0, $s5
    move $v1, $s6
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 80
    jr $ra
    nop

delta_decode:
    daddiu $sp, $sp, -80
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    move $s4, $zero
    move $s5, $zero
    move $s6, $zero
    move $a3, $zero
dec_next:
# BEGIN d_end
    beq $zero, $zero, dec_return
    nop
# END d_end
# BEGIN d_capacity
    beq $zero, $zero, dec_return
    nop
# END d_capacity
# BEGIN d_scan
    beq $zero, $zero, dec_return
    nop
# END d_scan
# BEGIN d_unsigned
    beq $zero, $zero, dec_return
    nop
# END d_unsigned
# BEGIN d_indices
    beq $zero, $zero, dec_return
    nop
# END d_indices
dec_byte:
# BEGIN d_read_bound
    beq $zero, $zero, dec_return
    nop
# END d_read_bound
# BEGIN d_load
    beq $zero, $zero, dec_return
    nop
# END d_load
# BEGIN d_payload
    beq $zero, $zero, dec_return
    nop
# END d_payload
# BEGIN d_advance_scan
    beq $zero, $zero, dec_return
    nop
# END d_advance_scan
# BEGIN d_fifth_gate
    beq $zero, $zero, dec_accumulate
    nop
# END d_fifth_gate
# BEGIN d_fifth_continuation
    beq $zero, $zero, dec_return
    nop
# END d_fifth_continuation
# BEGIN d_fifth_payload
    beq $zero, $zero, dec_return
    nop
# END d_fifth_payload
dec_accumulate:
# BEGIN d_shift_payload
    beq $zero, $zero, dec_return
    nop
# END d_shift_payload
# BEGIN d_accumulate
    beq $zero, $zero, dec_return
    nop
# END d_accumulate
# BEGIN d_continuation
    beq $zero, $zero, dec_return
    nop
# END d_continuation
# BEGIN d_next_byte
    beq $zero, $zero, dec_return
    nop
# END d_next_byte
dec_terminal:
# BEGIN d_single
    beq $zero, $zero, dec_return
    nop
# END d_single
# BEGIN d_minimal
    beq $zero, $zero, dec_return
    nop
# END d_minimal
dec_unmap:
# BEGIN d_inverse_mask
    beq $zero, $zero, dec_return
    nop
# END d_inverse_mask
# BEGIN d_inverse
    beq $zero, $zero, dec_return
    nop
# END d_inverse
# BEGIN d_reconstruct
    beq $zero, $zero, dec_return
    nop
# END d_reconstruct
# BEGIN d_minimum
    beq $zero, $zero, dec_return
    nop
# END d_minimum
# BEGIN d_maximum
    beq $zero, $zero, dec_return
    nop
# END d_maximum
# BEGIN d_output_address
    beq $zero, $zero, dec_return
    nop
# END d_output_address
# BEGIN d_store
    beq $zero, $zero, dec_return
    nop
# END d_store
# BEGIN d_commit_input
    beq $zero, $zero, dec_return
    nop
# END d_commit_input
# BEGIN d_next_value
    beq $zero, $zero, dec_return
    nop
# END d_next_value
dec_full:
    li $a3, 1
    beq $zero, $zero, dec_return
    nop
dec_bad:
    li $a3, 2
    beq $zero, $zero, dec_return
    nop
dec_range:
    li $a3, 3
dec_return:
    move $v0, $s5
    move $v1, $s6
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 80
    jr $ra
    nop
