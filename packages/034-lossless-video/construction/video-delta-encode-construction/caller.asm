main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $t0, delta_previous
    daddiu $t0, $t0, 4
    dla $t1, delta_decoded
    daddiu $t1, $t1, 4
    li $t2, 4096
delta_seed_decoder:
    lwu $t3, 0($t0)
    sw $t3, 0($t1)
    daddiu $t0, $t0, 4
    daddiu $t1, $t1, 4
    daddiu $t2, $t2, -1
    bnez $t2, delta_seed_decoder
    nop
    dla $a0, delta_source
    daddiu $a0, $a0, 4
    dla $t0, delta_source_count
    ld $a1, 0($t0)
    dla $a2, delta_output
    daddiu $a2, $a2, 3
    dla $a3, delta_context
    dla $t0, delta_previous
    daddiu $t0, $t0, 4
    sd $t0, 24($a3)
    dla $t0, delta_scratch
    daddiu $t0, $t0, 4
    sd $t0, 64($a3)
    ld $t0, 8($a3)
    dla $t1, delta_original_sequence
    sd $t0, 0($t1)
delta_call:
    jal video_encode_record
    nop
delta_call_return:
    beqz $v0, delta_roundtrip
    nop
    li $t0, 4
    beq $v0, $t0, delta_main_missing
    nop
    b delta_main_failure
    nop
delta_roundtrip:
    move $a1, $v1
    dla $a0, delta_output
    daddiu $a0, $a0, 3
    dla $a2, delta_decoded
    daddiu $a2, $a2, 4
    dla $a3, delta_decode_context
    dla $t0, delta_original_sequence
    ld $t0, 0($t0)
    sd $t0, 8($a3)
    dla $t0, delta_context
    ld $t0, 16($t0)
    sd $t0, 16($a3)
delta_decode_call:
    jal video_apply_record
    nop
delta_decode_return:
    bnez $v0, delta_main_failure
    nop
    dla $a0, delta_success_message
    b delta_main_message
    nop
delta_main_failure:
    dla $a0, delta_failure_message
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
delta_main_missing:
    dla $a0, delta_missing_message
delta_main_message:
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
delta_function_return:
    nop
