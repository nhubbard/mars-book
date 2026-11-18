.data
.align 3
source: .byte 77,97,110
    .byte -91:8189
source_end:
destination: .byte -91:8192
destination_end:
mode: .word 2
in_place: .word 0
count: .dword 3
capacity: .dword 4
status: .word -1
.align 3
written: .dword -1
.align 3
workspace: .byte -91:4096
alphabet: .ascii "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
.align 4
enc_left: .byte 0,0,1,2,3,3,4,5,6,6,7,8,9,9,10,11
.align 4
enc_left_shift: .byte 0,4,2,0,0,4,2,0,0,4,2,0,0,4,2,0
.align 4
enc_left_right: .byte 2,0,0,0,2,0,0,0,2,0,0,0,2,0,0,0
.align 4
enc_right: .byte 0,1,2,0,3,4,5,0,6,7,8,0,9,10,11,0
.align 4
enc_right_shift: .byte 0,4,6,0,0,4,6,0,0,4,6,0,0,4,6,0
.align 4
enc_right_mask: .byte 0,15,3,0,0,15,3,0,0,15,3,0,0,15,3,0
.align 4
dec_left: .byte 0,1,2,0,4,5,6,0,8,9,10,0,12,13,14,0
.align 4
dec_left_shift: .byte 2,4,6,0,2,4,6,0,2,4,6,0,2,4,6,0
.align 4
dec_left_mask: .byte -1,-1,-1,0,-1,-1,-1,0,-1,-1,-1,0,-1,-1,-1,0
.align 4
dec_right: .byte 1,2,3,0,5,6,7,0,9,10,11,0,13,14,15,0
.align 4
dec_right_shift: .byte 4,2,0,0,4,2,0,0,4,2,0,0,4,2,0,0
.align 4
dec_right_mask: .byte 15,63,63,0,15,63,63,0,15,63,63,0,15,63,63,0
.align 4
dec_compact: .byte 0,1,2,4,5,6,8,9,10,12,13,14,15,15,15,15
.text
.globl main
main:
    dla $sp, workspace
    daddiu $sp, $sp, 4096
    dla $t0, mode
    lw $t0, 0($t0)
    li $t1, 0
    beq $t0, $t1, main_digit
    nop
    li $t1, 1
    beq $t0, $t1, main_validate
    nop
    li $t1, 2
    beq $t0, $t1, main_encode_scalar
    nop
    li $t1, 3
    beq $t0, $t1, main_decode_scalar
    nop
    li $t1, 4
    beq $t0, $t1, main_encode12_msa
    nop
    li $t1, 5
    beq $t0, $t1, main_decode16_msa
    nop
    li $t1, 6
    beq $t0, $t1, main_encode_msa
    nop
    li $t1, 7
    beq $t0, $t1, main_decode_msa
    nop
    b unframed_development
    nop
main_digit:
    dla $a0, source
    lbu $a0, 0($a0)
digit_call:
    jal b64_digit
    nop
digit_return:
    move $v1, $v0
    li $t0, -2
    beq $v0, $t0, caller_digit_development
    nop
    bltz $v0, caller_digit_invalid
    nop
    move $v0, $zero
    b main_done
    nop
caller_digit_development:
    li $v0, 4
    b main_done
    nop
caller_digit_invalid:
    li $v0, 2
    b main_done
    nop
main_validate:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
validate_call:
    jal b64_validate
    nop
validate_return:
    b main_done
    nop
main_encode_scalar:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, capacity
    ld $a3, 0($t0)
encode_scalar_call:
    jal b64_encode_scalar
    nop
encode_scalar_return:
    b main_done
    nop
main_decode_scalar:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, in_place
    lw $t0, 0($t0)
    beqz $t0, caller_decode_scalar_capacity
    nop
    move $a2, $a0
caller_decode_scalar_capacity:
    dla $t0, capacity
    ld $a3, 0($t0)
decode_scalar_call:
    jal b64_decode_scalar
    nop
decode_scalar_return:
    b main_done
    nop
main_encode12_msa:
    dla $a0, source
    dla $a1, destination
encode12_msa_call:
    jal b64_encode12_msa
    nop
encode12_msa_return:
    b main_done
    nop
main_decode16_msa:
    dla $a0, source
    dla $a1, destination
    dla $t0, in_place
    lw $t0, 0($t0)
    beqz $t0, decode16_msa_call
    nop
    move $a1, $a0
decode16_msa_call:
    jal b64_decode16_msa
    nop
decode16_msa_return:
    b main_done
    nop
main_encode_msa:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, capacity
    ld $a3, 0($t0)
encode_msa_call:
    jal b64_encode_msa
    nop
encode_msa_return:
    b main_done
    nop
main_decode_msa:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, in_place
    lw $t0, 0($t0)
    beqz $t0, caller_decode_msa_capacity
    nop
    move $a2, $a0
caller_decode_msa_capacity:
    dla $t0, capacity
    ld $a3, 0($t0)
decode_msa_call:
    jal b64_decode_msa
    nop
decode_msa_return:
    b main_done
    nop
main_done:
    dla $t0, status
    sw $v0, 0($t0)
    dla $t0, written
    sd $v1, 0($t0)
    li $v0, 10
    syscall
function_return:
    nop
b64_digit:
# BEGIN digit_upper
    b digit_development
    nop
# END digit_upper
# BEGIN digit_ranges
    b digit_development
    nop
# END digit_ranges
# BEGIN digit_symbols
    b digit_development
    nop
# END digit_symbols
digit_direct:
    move $v0, $t0
    jr $ra
    nop
digit_lower:
    daddiu $v0, $t0, 26
    jr $ra
    nop
digit_number:
    daddiu $v0, $t0, 52
    jr $ra
    nop
digit_plus:
    li $v0, 62
    jr $ra
    nop
digit_slash:
    li $v0, 63
    jr $ra
    nop
b64_validate:
    daddiu $sp, $sp, -48
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $ra, 32($sp)
# BEGIN val_state
    b validate_development
    nop
# END val_state
# BEGIN val_scan
validate_loop:
    b validate_development
    nop
# END val_scan
# BEGIN val_one
validate_padding:
    b validate_development
    nop
# END val_one
# BEGIN val_two
validate_two:
    b validate_development
    nop
# END val_two
validate_good:
    move $v0, $zero
    move $v1, $s2
    b validate_restore
    nop
validate_bad:
    li $v0, 2
    move $v1, $zero
    b validate_restore
    nop
validate_development:
    li $v0, 4
    move $v1, $zero
validate_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $ra, 32($sp)
    daddiu $sp, $sp, 48
    jr $ra
    nop
b64_encode_scalar:
    move $t9, $zero
    b b64_encode
    nop
b64_encode_msa:
    li $t9, 1
b64_encode:
# BEGIN enc_guard
    b unframed_development
    nop
# END enc_guard
encode_frame:
    daddiu $sp, $sp, -64
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $ra, 48($sp)
# BEGIN enc_state
    b encode_development
    nop
# END enc_state
encode_loop:
    beqz $s1, encode_done
    nop
    beqz $s5, encode_scalar_group
    nop
# BEGIN enc_vector
    b encode_development
    nop
# END enc_vector
# BEGIN enc_load
encode_scalar_group:
    b encode_development
    nop
# END enc_load
# BEGIN enc_pair
encode_pack:
    b encode_development
    nop
# END enc_pair
after_enc_pair:
# BEGIN enc_third
    b encode_development
    nop
# END enc_third
after_enc_third:
# BEGIN enc_advance
    b encode_development
    nop
# END enc_advance
# BEGIN enc_padding
encode_pad_two:
encode_pad_one:
    b encode_development
    nop
# END enc_padding
encode_done:
    move $v0, $zero
    move $v1, $s3
    b encode_restore
    nop
encode_failed:
    move $v1, $zero
    b encode_restore
    nop
encode_development:
    li $v0, 4
    move $v1, $zero
encode_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $ra, 48($sp)
    daddiu $sp, $sp, 64
    jr $ra
    nop
b64_decode_scalar:
    move $t9, $zero
    b b64_decode
    nop
b64_decode_msa:
    li $t9, 1
b64_decode:
# BEGIN dec_guard
    b unframed_development
    nop
# END dec_guard
decode_frame:
    daddiu $sp, $sp, -80
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $ra, 56($sp)
# BEGIN dec_setup
    b decode_development
    nop
# END dec_setup
after_dec_setup:
# BEGIN dec_body
    b decode_development
    nop
# END dec_body
decode_vector_loop:
    beqz $s5, decode_scalar_loop
    nop
# BEGIN dec_vector
    b decode_development
    nop
# END dec_vector
decode_scalar_loop:
    beqz $s1, decode_done
    nop
    move $s4, $zero
# BEGIN dec_quartet
decode_quartet:
decode_pad_zero:
decode_digit_store:
    b decode_development
    nop
# END dec_quartet
after_dec_quartet:
# BEGIN dec_first
    b decode_development
    nop
# END dec_first
after_dec_first:
# BEGIN dec_second
    b decode_development
    nop
# END dec_second
after_dec_second:
# BEGIN dec_third
    b decode_development
    nop
# END dec_third
decode_done:
    move $v0, $zero
    move $v1, $s3
    b decode_restore
    nop
decode_capacity:
    li $v0, 3
decode_failed:
    move $v1, $zero
    b decode_restore
    nop
decode_development:
    li $v0, 4
    move $v1, $zero
decode_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $ra, 56($sp)
    daddiu $sp, $sp, 80
    jr $ra
    nop
b64_limit:
    li $v0, 1
    move $v1, $zero
    jr $ra
    nop
b64_text:
    li $v0, 2
    move $v1, $zero
    jr $ra
    nop
b64_capacity:
    li $v0, 3
    move $v1, $zero
    jr $ra
    nop
b64_empty:
    move $v0, $zero
    move $v1, $zero
    jr $ra
    nop
b64_encode12_msa:
    daddiu $sp, $sp, -16
# BEGIN enc12_copy
    b encode12_development
    nop
# END enc12_copy
after_enc12_copy:
# BEGIN enc12_left
    b encode12_development
    nop
# END enc12_left
after_enc12_left:
# BEGIN enc12_right
    b encode12_development
    nop
# END enc12_right
after_enc12_right:
# BEGIN enc12_upper
    b encode12_development
    nop
# END enc12_upper
after_enc12_upper:
# BEGIN enc12_digit
    b encode12_development
    nop
# END enc12_digit
after_enc12_digit:
# BEGIN enc12_symbols
    b encode12_development
    nop
# END enc12_symbols
after_enc12_symbols:
# BEGIN enc12_publish
    b encode12_development
    nop
# END enc12_publish
after_enc12_publish:
encode12_done:
    move $v0, $zero
    li $v1, 16
    b encode12_restore
    nop
encode12_development:
    li $v0, 4
    move $v1, $zero
encode12_restore:
    daddiu $sp, $sp, 16
    jr $ra
    nop
b64_decode16_msa:
    daddiu $sp, $sp, -16
# BEGIN dec16_upper
    b decode16_development
    nop
# END dec16_upper
after_dec16_upper:
# BEGIN dec16_lower
    b decode16_development
    nop
# END dec16_lower
after_dec16_lower:
# BEGIN dec16_digit
    b decode16_development
    nop
# END dec16_digit
after_dec16_digit:
# BEGIN dec16_symbols
    b decode16_development
    nop
# END dec16_symbols
after_dec16_symbols:
# BEGIN dec16_left
    b decode16_development
    nop
# END dec16_left
after_dec16_left:
# BEGIN dec16_compact
    b decode16_development
    nop
# END dec16_compact
after_dec16_compact:
# BEGIN dec16_publish
    b decode16_development
    nop
# END dec16_publish
after_dec16_publish:
decode16_done:
    move $v0, $zero
    li $v1, 12
    b decode16_restore
    nop
decode16_development:
    li $v0, 4
    move $v1, $zero
decode16_restore:
    daddiu $sp, $sp, 16
    jr $ra
    nop
unframed_development:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
digit_development:
    li $v0, -2
    jr $ra
    nop
