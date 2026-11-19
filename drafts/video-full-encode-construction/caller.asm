main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, encode_source
    daddiu $a0, $a0, 4
    dla $t0, encode_source_count
    ld $a1, 0($t0)
    dla $a2, encode_output
    daddiu $a2, $a2, 3
    dla $a3, encode_context
    dla $t0, encode_previous
    daddiu $t0, $t0, 4
    sd $t0, 24($a3)
    ld $t0, 8($a3)
    dla $t1, encode_original_sequence
    sd $t0, 0($t1)
encode_call:
    jal video_encode_full
    nop
encode_call_return:
    dla $t0, encode_last_status
    sd $v0, 0($t0)
    sd $v1, 8($t0)
    beqz $v0, encode_roundtrip
    nop
    li $t0, 4
    beq $v0, $t0, encode_main_missing
    nop
    b encode_main_failure
    nop
encode_roundtrip:
    move $a1, $v1
    dla $a0, encode_output
    daddiu $a0, $a0, 3
    dla $a2, encode_decoded
    daddiu $a2, $a2, 4
    dla $a3, encode_decode_context
    dla $t0, encode_original_sequence
    ld $t0, 0($t0)
    sd $t0, 8($a3)
    dla $t0, encode_context
    ld $t0, 16($t0)
    sd $t0, 16($a3)
encode_decode_call:
    jal video_apply_record
    nop
encode_decode_return:
    bnez $v0, encode_main_failure
    nop
    dla $a0, encode_success_message
    b encode_main_message
    nop
encode_main_failure:
    dla $a0, encode_failure_message
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
encode_main_missing:
    dla $a0, encode_missing_message
encode_main_message:
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
encode_function_return:
    nop
