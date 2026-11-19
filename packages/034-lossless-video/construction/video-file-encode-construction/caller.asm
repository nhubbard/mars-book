main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, file_encode_frames
    daddiu $a0, $a0, 4
    dla $t0, file_encode_source_count
    ld $a1, 0($t0)
    dla $a2, file_encode_output
    daddiu $a2, $a2, 3
    dla $a3, file_encode_context
    dla $t0, file_encode_previous
    daddiu $t0, $t0, 4
    sd $t0, 32($a3)
    dla $t0, file_encode_xor
    daddiu $t0, $t0, 4
    sd $t0, 48($a3)
file_encode_call:
    jal video_encode_file
    nop
file_encode_call_return:
    beqz $v0, file_encode_validate
    nop
    li $t0, 4
    beq $v0, $t0, file_encode_missing
    nop
    b file_encode_failed
    nop
file_encode_validate:
    dla $t0, file_encode_produced_count
    sd $v1, 0($t0)
    move $a1, $v1
    dla $a0, file_encode_output
    daddiu $a0, $a0, 3
    dla $a2, index_directory
    daddiu $a2, $a2, 8
    dla $a3, index_context
    dla $t0, index_scratch
    daddiu $t0, $t0, 4
    sd $t0, 8($a3)
file_encode_index_call:
    jal video_index
    nop
file_encode_index_return:
    bnez $v0, file_encode_failed
    nop
    dla $a3, seek_context
    dla $t0, index_context
    ld $t1, 48($t0)
    sd $t1, 0($a3)
    dla $t1, index_directory
    daddiu $t1, $t1, 8
    sd $t1, 8($a3)
    ld $t1, 24($t0)
    sd $t1, 16($a3)
    ld $t1, 32($t0)
    sd $t1, 24($a3)
    dla $t1, index_scratch
    daddiu $t1, $t1, 4
    sd $t1, 40($a3)
    dla $a0, file_encode_output
    daddiu $a0, $a0, 3
    dla $t0, file_encode_target
    ld $a1, 0($t0)
    dla $a2, seek_frame
    daddiu $a2, $a2, 4
file_encode_seek_call:
    jal video_seek
    nop
file_encode_seek_return:
    bnez $v0, file_encode_failed
    nop
    dla $a0, file_encode_path
    dla $a1, file_encode_output
    daddiu $a1, $a1, 3
    dla $t0, file_encode_produced_count
    ld $a2, 0($t0)
    dla $a3, file_encode_store_context
file_encode_store_call:
    jal video_store
    nop
file_encode_store_return:
    beqz $v0, file_encode_reload
    nop
    li $t0, 4
    beq $v0, $t0, file_encode_missing
    nop
    b file_encode_failed
    nop
file_encode_reload:
    dla $a0, file_encode_path
    dla $a1, index_file_data
    daddiu $a1, $a1, 3
    dla $t0, io_capacity
    ld $a2, 0($t0)
    dla $a3, io_load_context
file_encode_load_call:
    jal video_load
    nop
file_encode_load_return:
    bnez $v0, file_encode_failed
    nop
    dla $t0, file_encode_produced_count
    ld $t0, 0($t0)
    bne $v1, $t0, file_encode_failed
    nop
    dla $a0, file_encode_success_message
    b file_encode_message
    nop
file_encode_failed:
    dla $a0, file_encode_failure_message
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
file_encode_missing:
    dla $a0, file_encode_missing_message
file_encode_message:
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
file_encode_function_return:
    nop
file_encode_store_function_return:
    nop
