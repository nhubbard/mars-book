.data
.align 3
source: .byte 84,87,70,117,84,82,61,61
    .byte -91:8184
source_end:
destination: .byte -91:8192
destination_end:
mode: .word 3
in_place: .word 0
count: .dword 8
capacity: .dword 0
status: .word -1
.align 3
written: .dword -1
.align 4
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
    beqc $t0, $t1, main_digit
    nop
    li $t1, 1
    beqc $t0, $t1, main_validate
    nop
    li $t1, 2
    beqc $t0, $t1, main_encode_scalar
    nop
    li $t1, 3
    beqc $t0, $t1, main_decode_scalar
    nop
    li $t1, 4
    beqc $t0, $t1, main_encode12_msa
    nop
    li $t1, 5
    beqc $t0, $t1, main_decode16_msa
    nop
    li $t1, 6
    beqc $t0, $t1, main_encode_msa
    nop
    li $t1, 7
    beqc $t0, $t1, main_decode_msa
    nop
    bc unframed_development
    nop
main_digit:
    dla $a0, source
    lbu $a0, 0($a0)
digit_call:
    balc b64_digit
    nop
digit_return:
    move $v1, $v0
    li $t0, -2
    beqc $v0, $t0, caller_digit_development
    nop
    bltzc $v0, caller_digit_invalid
    nop
    move $v0, $zero
    bc main_done
    nop
caller_digit_development:
    li $v0, 4
    bc main_done
    nop
caller_digit_invalid:
    li $v0, 2
    bc main_done
    nop
main_validate:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
validate_call:
    balc b64_validate
    nop
validate_return:
    bc main_done
    nop
main_encode_scalar:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, capacity
    ld $a3, 0($t0)
encode_scalar_call:
    balc b64_encode_scalar
    nop
encode_scalar_return:
    bc main_done
    nop
main_decode_scalar:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, in_place
    lw $t0, 0($t0)
    beqzc $t0, caller_decode_scalar_capacity
    nop
    move $a2, $a0
caller_decode_scalar_capacity:
    dla $t0, capacity
    ld $a3, 0($t0)
decode_scalar_call:
    balc b64_decode_scalar
    nop
decode_scalar_return:
    bc main_done
    nop
main_encode12_msa:
    dla $a0, source
    dla $a1, destination
encode12_msa_call:
    balc b64_encode12_msa
    nop
encode12_msa_return:
    bc main_done
    nop
main_decode16_msa:
    dla $a0, source
    dla $a1, destination
    dla $t0, in_place
    lw $t0, 0($t0)
    beqzc $t0, decode16_msa_call
    nop
    move $a1, $a0
decode16_msa_call:
    balc b64_decode16_msa
    nop
decode16_msa_return:
    bc main_done
    nop
main_encode_msa:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, capacity
    ld $a3, 0($t0)
encode_msa_call:
    balc b64_encode_msa
    nop
encode_msa_return:
    bc main_done
    nop
main_decode_msa:
    dla $a0, source
    dla $t0, count
    ld $a1, 0($t0)
    dla $a2, destination
    dla $t0, in_place
    lw $t0, 0($t0)
    beqzc $t0, caller_decode_msa_capacity
    nop
    move $a2, $a0
caller_decode_msa_capacity:
    dla $t0, capacity
    ld $a3, 0($t0)
decode_msa_call:
    balc b64_decode_msa
    nop
decode_msa_return:
    bc main_done
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
    daddiu $t0, $a0, -65
    sltiu $t1, $t0, 26
    bnezc $t1, digit_direct
    nop
    daddiu $t0, $a0, -97
    sltiu $t1, $t0, 26
    bnezc $t1, digit_lower
    nop
    daddiu $t0, $a0, -48
    sltiu $t1, $t0, 10
    bnezc $t1, digit_number
    nop
    li $t0, 43
    beqc $a0, $t0, digit_plus
    nop
    li $t0, 47
    beqc $a0, $t0, digit_slash
    nop
    li $v0, -1
    jrc $ra
    nop
digit_direct:
    move $v0, $t0
    jrc $ra
    nop
digit_lower:
    daddiu $v0, $t0, 26
    jrc $ra
    nop
digit_number:
    daddiu $v0, $t0, 52
    jrc $ra
    nop
digit_plus:
    li $v0, 62
    jrc $ra
    nop
digit_slash:
    li $v0, 63
    jrc $ra
    nop
b64_validate:
    daddiu $sp, $sp, -48
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $ra, 32($sp)
    move $s0, $a0
    move $s1, $a1
    dsrl $s2, $a1, 2
    li $t0, 3
    dmulu $s2, $s2, $t0
    move $s3, $zero
validate_loop:
    beqzc $s1, validate_good
    nop
    lbu $a0, 0($s0)
    li $t0, 61
    beqc $a0, $t0, validate_padding
    nop
    balc b64_digit
    nop
    li $t0, -2
    beqc $v0, $t0, validate_development
    nop
    bltzc $v0, validate_bad
    nop
    move $s3, $v0
    daddiu $s0, $s0, 1
    daddiu $s1, $s1, -1
    bc validate_loop
    nop
validate_padding:
    li $t0, 2
    beqc $s1, $t0, validate_two
    nop
    li $t0, 1
    bnec $s1, $t0, validate_bad
    nop
    andi $t0, $s3, 3
    bnezc $t0, validate_bad
    nop
    daddiu $s2, $s2, -1
    bc validate_good
    nop
validate_two:
    lbu $t0, 1($s0)
    li $t1, 61
    bnec $t0, $t1, validate_bad
    nop
    andi $t0, $s3, 15
    bnezc $t0, validate_bad
    nop
    daddiu $s2, $s2, -2
validate_good:
    move $v0, $zero
    move $v1, $s2
    bc validate_restore
    nop
validate_bad:
    li $v0, 2
    move $v1, $zero
    bc validate_restore
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
    jrc $ra
    nop
b64_encode_scalar:
    move $t9, $zero
    bc b64_encode
    nop
b64_encode_msa:
    li $t9, 1
b64_encode:
    li $t0, 4096
    sltu $t0, $t0, $a1
    bnezc $t0, b64_limit
    nop
    beqzc $a1, b64_empty
    nop
    daddiu $t0, $a1, 2
    li $t1, 3
    ddivu $t0, $t0, $t1
    dsll $t0, $t0, 2
    sltu $t1, $a3, $t0
    bnezc $t1, b64_capacity
    nop
encode_frame:
    daddiu $sp, $sp, -64
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $ra, 48($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $t0
    dla $s4, alphabet
    move $s5, $t9
encode_loop:
    beqzc $s1, encode_done
    nop
    beqzc $s5, encode_scalar_group
    nop
    sltiu $t0, $s1, 12
    bnezc $t0, encode_scalar_group
    nop
    move $a0, $s0
    move $a1, $s2
    balc b64_encode12_msa
    nop
    bnezc $v0, encode_failed
    nop
    daddiu $s0, $s0, 12
    daddiu $s1, $s1, -12
    daddiu $s2, $s2, 16
    bc encode_loop
    nop
encode_scalar_group:
    lbu $t0, 0($s0)
    move $t1, $zero
    move $t2, $zero
    sltiu $t3, $s1, 2
    bnezc $t3, encode_pack
    nop
    lbu $t1, 1($s0)
    sltiu $t3, $s1, 3
    bnezc $t3, encode_pack
    nop
    lbu $t2, 2($s0)
encode_pack:
    srl $t3, $t0, 2
    daddu $t4, $s4, $t3
    lbu $t4, 0($t4)
    sb $t4, 0($s2)
    andi $t3, $t0, 3
    sll $t3, $t3, 4
    srl $t4, $t1, 4
    or $t3, $t3, $t4
    daddu $t4, $s4, $t3
    lbu $t4, 0($t4)
    sb $t4, 1($s2)
after_enc_pair:
    sltiu $t3, $s1, 2
    bnezc $t3, encode_pad_two
    nop
    andi $t3, $t1, 15
    sll $t3, $t3, 2
    srl $t4, $t2, 6
    or $t3, $t3, $t4
    daddu $t4, $s4, $t3
    lbu $t4, 0($t4)
    sb $t4, 2($s2)
after_enc_third:
    sltiu $t3, $s1, 3
    bnezc $t3, encode_pad_one
    nop
    andi $t3, $t2, 63
    daddu $t4, $s4, $t3
    lbu $t4, 0($t4)
    sb $t4, 3($s2)
    daddiu $s0, $s0, 3
    daddiu $s1, $s1, -3
    daddiu $s2, $s2, 4
    bc encode_loop
    nop
encode_pad_two:
    li $t4, 61
    sb $t4, 2($s2)
encode_pad_one:
    li $t4, 61
    sb $t4, 3($s2)
encode_done:
    move $v0, $zero
    move $v1, $s3
    bc encode_restore
    nop
encode_failed:
    move $v1, $zero
    bc encode_restore
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
    jrc $ra
    nop
b64_decode_scalar:
    move $t9, $zero
    bc b64_decode
    nop
b64_decode_msa:
    li $t9, 1
b64_decode:
    li $t0, 5464
    sltu $t0, $t0, $a1
    bnezc $t0, b64_limit
    nop
    beqzc $a1, b64_empty
    nop
    andi $t0, $a1, 3
    bnezc $t0, b64_text
    nop
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
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    move $s5, $t9
    balc b64_validate
    nop
    bnezc $v0, decode_failed
    nop
    sltu $t0, $s3, $v1
    bnezc $t0, decode_capacity
    nop
    move $s3, $v1
    move $s6, $v1
after_dec_setup:
    move $s4, $s1
    daddu $t0, $s0, $s1
    lbu $t0, -1($t0)
    li $t1, 61
    bnec $t0, $t1, decode_vector_loop
    nop
    daddiu $s4, $s4, -4
decode_vector_loop:
    beqzc $s5, decode_scalar_loop
    nop
    sltiu $t0, $s4, 16
    bnezc $t0, decode_scalar_loop
    nop
    move $a0, $s0
    move $a1, $s2
    balc b64_decode16_msa
    nop
    bnezc $v0, decode_failed
    nop
    daddiu $s0, $s0, 16
    daddiu $s1, $s1, -16
    daddiu $s2, $s2, 12
    daddiu $s4, $s4, -16
    daddiu $s6, $s6, -12
    bc decode_vector_loop
    nop
decode_scalar_loop:
    beqzc $s1, decode_done
    nop
    move $s4, $zero
decode_quartet:
    daddu $t0, $s0, $s4
    lbu $a0, 0($t0)
    li $t0, 61
    beqc $a0, $t0, decode_pad_zero
    nop
    balc b64_digit
    nop
    bltzc $v0, decode_development
    nop
    bc decode_digit_store
    nop
decode_pad_zero:
    move $v0, $zero
decode_digit_store:
    dsll $t0, $s4, 2
    daddu $t0, $sp, $t0
    sw $v0, 64($t0)
    daddiu $s4, $s4, 1
    sltiu $t0, $s4, 4
    bnezc $t0, decode_quartet
    nop
after_dec_quartet:
    lwu $t0, 64($sp)
    lwu $t1, 68($sp)
    lwu $t2, 72($sp)
    lwu $t3, 76($sp)
    sll $t4, $t0, 2
    srl $t5, $t1, 4
    or $t4, $t4, $t5
    sb $t4, 0($s2)
    daddiu $s6, $s6, -1
    beqzc $s6, decode_done
    nop
after_dec_first:
    sll $t4, $t1, 4
    srl $t5, $t2, 2
    or $t4, $t4, $t5
    sb $t4, 1($s2)
    daddiu $s6, $s6, -1
    beqzc $s6, decode_done
    nop
after_dec_second:
    sll $t4, $t2, 6
    or $t4, $t4, $t3
    sb $t4, 2($s2)
    daddiu $s6, $s6, -1
    daddiu $s0, $s0, 4
    daddiu $s1, $s1, -4
    daddiu $s2, $s2, 3
    bc decode_scalar_loop
    nop
decode_done:
    move $v0, $zero
    move $v1, $s3
    bc decode_restore
    nop
decode_capacity:
    li $v0, 3
decode_failed:
    move $v1, $zero
    bc decode_restore
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
    jrc $ra
    nop
b64_limit:
    li $v0, 1
    move $v1, $zero
    jrc $ra
    nop
b64_text:
    li $v0, 2
    move $v1, $zero
    jrc $ra
    nop
b64_capacity:
    li $v0, 3
    move $v1, $zero
    jrc $ra
    nop
b64_empty:
    move $v0, $zero
    move $v1, $zero
    jrc $ra
    nop
b64_encode12_msa:
    daddiu $sp, $sp, -16
    sd $zero, 0($sp)
    sd $zero, 8($sp)
    lbu $t0, 0($a0)
    sb $t0, 0($sp)
    lbu $t0, 1($a0)
    sb $t0, 1($sp)
    lbu $t0, 2($a0)
    sb $t0, 2($sp)
    lbu $t0, 3($a0)
    sb $t0, 3($sp)
    lbu $t0, 4($a0)
    sb $t0, 4($sp)
    lbu $t0, 5($a0)
    sb $t0, 5($sp)
    lbu $t0, 6($a0)
    sb $t0, 6($sp)
    lbu $t0, 7($a0)
    sb $t0, 7($sp)
    lbu $t0, 8($a0)
    sb $t0, 8($sp)
    lbu $t0, 9($a0)
    sb $t0, 9($sp)
    lbu $t0, 10($a0)
    sb $t0, 10($sp)
    lbu $t0, 11($a0)
    sb $t0, 11($sp)
after_enc12_copy:
    ld.b $w0, 0($sp)
    dla $t1, enc_left
    ld.b $w1, 0($t1)
    vshf.b $w1, $w0, $w0
    dla $t1, enc_left_shift
    ld.b $w2, 0($t1)
    sll.b $w1, $w1, $w2
    dla $t1, enc_left_right
    ld.b $w2, 0($t1)
    srl.b $w1, $w1, $w2
after_enc12_left:
    dla $t1, enc_right
    ld.b $w3, 0($t1)
    vshf.b $w3, $w0, $w0
    dla $t1, enc_right_shift
    ld.b $w2, 0($t1)
    srl.b $w3, $w3, $w2
    dla $t1, enc_right_mask
    ld.b $w2, 0($t1)
    and.v $w3, $w3, $w2
    or.v $w1, $w1, $w3
    andi.b $w1, $w1, 63
after_enc12_right:
    ldi.b $w2, 25
    clt_u.b $w3, $w2, $w1
    andi.b $w3, $w3, 6
    addv.b $w4, $w1, $w3
after_enc12_upper:
    ldi.b $w2, 51
    clt_u.b $w3, $w2, $w1
    andi.b $w3, $w3, 75
    subv.b $w4, $w4, $w3
after_enc12_digit:
    ldi.b $w2, 61
    clt_u.b $w3, $w2, $w1
    andi.b $w3, $w3, 15
    subv.b $w4, $w4, $w3
    ldi.b $w2, 63
    ceq.b $w3, $w1, $w2
    andi.b $w3, $w3, 3
    addv.b $w4, $w4, $w3
after_enc12_symbols:
    ldi.b $w2, 65
    addv.b $w4, $w4, $w2
    st.b $w4, 0($a1)
after_enc12_publish:
encode12_done:
    move $v0, $zero
    li $v1, 16
    bc encode12_restore
    nop
encode12_development:
    li $v0, 4
    move $v1, $zero
encode12_restore:
    daddiu $sp, $sp, 16
    jrc $ra
    nop
b64_decode16_msa:
    daddiu $sp, $sp, -16
    ld.b $w0, 0($a0)
    xor.v $w5, $w5, $w5
    ldi.b $w1, 64
    clt_u.b $w2, $w1, $w0
    ldi.b $w1, 91
    clt_u.b $w3, $w0, $w1
    and.v $w2, $w2, $w3
    ldi.b $w1, 65
    subv.b $w4, $w0, $w1
    and.v $w4, $w4, $w2
    or.v $w5, $w5, $w4
after_dec16_upper:
    ldi.b $w1, 96
    clt_u.b $w2, $w1, $w0
    ldi.b $w1, 123
    clt_u.b $w3, $w0, $w1
    and.v $w2, $w2, $w3
    ldi.b $w1, 71
    subv.b $w4, $w0, $w1
    and.v $w4, $w4, $w2
    or.v $w5, $w5, $w4
after_dec16_lower:
    ldi.b $w1, 47
    clt_u.b $w2, $w1, $w0
    ldi.b $w1, 58
    clt_u.b $w3, $w0, $w1
    and.v $w2, $w2, $w3
    ldi.b $w1, 4
    addv.b $w4, $w0, $w1
    and.v $w4, $w4, $w2
    or.v $w5, $w5, $w4
after_dec16_digit:
    ldi.b $w1, 43
    ceq.b $w2, $w0, $w1
    andi.b $w2, $w2, 62
    or.v $w5, $w5, $w2
    ldi.b $w1, 47
    ceq.b $w2, $w0, $w1
    andi.b $w2, $w2, 63
    or.v $w5, $w5, $w2
after_dec16_symbols:
    dla $t1, dec_left
    ld.b $w6, 0($t1)
    vshf.b $w6, $w5, $w5
    dla $t1, dec_left_shift
    ld.b $w1, 0($t1)
    sll.b $w6, $w6, $w1
    dla $t1, dec_left_mask
    ld.b $w1, 0($t1)
    and.v $w6, $w6, $w1
after_dec16_left:
    dla $t1, dec_right
    ld.b $w7, 0($t1)
    vshf.b $w7, $w5, $w5
    dla $t1, dec_right_shift
    ld.b $w1, 0($t1)
    srl.b $w7, $w7, $w1
    dla $t1, dec_right_mask
    ld.b $w1, 0($t1)
    and.v $w7, $w7, $w1
    or.v $w6, $w6, $w7
    dla $t1, dec_compact
    ld.b $w7, 0($t1)
    vshf.b $w7, $w6, $w6
after_dec16_compact:
    st.b $w7, 0($sp)
    lbu $t0, 0($sp)
    sb $t0, 0($a1)
    lbu $t0, 1($sp)
    sb $t0, 1($a1)
    lbu $t0, 2($sp)
    sb $t0, 2($a1)
    lbu $t0, 3($sp)
    sb $t0, 3($a1)
    lbu $t0, 4($sp)
    sb $t0, 4($a1)
    lbu $t0, 5($sp)
    sb $t0, 5($a1)
    lbu $t0, 6($sp)
    sb $t0, 6($a1)
    lbu $t0, 7($sp)
    sb $t0, 7($a1)
    lbu $t0, 8($sp)
    sb $t0, 8($a1)
    lbu $t0, 9($sp)
    sb $t0, 9($a1)
    lbu $t0, 10($sp)
    sb $t0, 10($a1)
    lbu $t0, 11($sp)
    sb $t0, 11($a1)
after_dec16_publish:
decode16_done:
    move $v0, $zero
    li $v1, 12
    bc decode16_restore
    nop
decode16_development:
    li $v0, 4
    move $v1, $zero
decode16_restore:
    daddiu $sp, $sp, 16
    jrc $ra
    nop
unframed_development:
    li $v0, 4
    move $v1, $zero
    jrc $ra
    nop
digit_development:
    li $v0, -2
    jrc $ra
    nop
