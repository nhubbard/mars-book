main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, io_path
    dla $a1, index_file_data
    daddiu $a1, $a1, 3
    dla $t0, io_capacity
    ld $a2, 0($t0)
    dla $a3, io_load_context
seek_load_call:
    jal video_load
    nop
seek_load_return:
    bnez $v0, seek_main_failure
    nop
    move $a1, $v1
    dla $a0, index_file_data
    daddiu $a0, $a0, 3
    dla $a2, index_directory
    daddiu $a2, $a2, 8
    dla $a3, index_context
    dla $t0, index_scratch
    daddiu $t0, $t0, 4
    sd $t0, 8($a3)
seek_index_call:
    jal video_index
    nop
seek_index_return:
    bnez $v0, seek_main_failure
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
    dla $a0, index_file_data
    daddiu $a0, $a0, 3
    dla $t0, seek_target
    ld $a1, 0($t0)
    dla $a2, seek_frame
    daddiu $a2, $a2, 4
seek_call:
    jal video_seek
    nop
seek_call_return:
    dla $t0, seek_last_status
    sd $v0, 0($t0)
    sd $v1, 8($t0)
    beqz $v0, seek_main_success
    nop
    li $t0, 4
    beq $v0, $t0, seek_main_missing
    nop
seek_main_failure:
    dla $a0, seek_failure_message
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
seek_main_success:
    dla $a0, seek_success_message
    b seek_main_message
    nop
seek_main_missing:
    dla $a0, seek_missing_message
seek_main_message:
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
seek_function_return:
    nop
