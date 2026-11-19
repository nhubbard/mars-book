main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, io_path
    dla $a1, index_file_data
    daddiu $a1, $a1, 3
    dla $t0, io_capacity
    ld $a2, 0($t0)
    dla $a3, io_load_context
io_load_call:
    jal video_load
    nop
io_load_return:
    dla $t0, io_last_status
    sd $v0, 0($t0)
    sd $v1, 8($t0)
    beqz $v0, io_index_begin
    nop
    li $t0, 4
    beq $v0, $t0, io_main_missing
    nop
    dla $a0, io_load_failure_message
    b io_main_failed
    nop
io_index_begin:
    dla $t0, io_loaded_count
    sd $v1, 0($t0)
    move $a1, $v1
    dla $a0, index_file_data
    daddiu $a0, $a0, 3
    dla $a2, index_directory
    daddiu $a2, $a2, 8
    dla $a3, index_context
    dla $t0, index_scratch
    daddiu $t0, $t0, 4
    sd $t0, 8($a3)
io_index_call:
    jal video_index
    nop
io_index_return:
    beqz $v0, io_main_success
    nop
    li $t0, 4
    beq $v0, $t0, io_main_missing
    nop
    dla $a0, index_failure_message
io_main_failed:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
io_main_success:
    dla $a0, io_success_message
    b io_main_message
    nop
io_main_missing:
    dla $a0, io_missing_message
io_main_message:
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
io_function_return:
    nop
