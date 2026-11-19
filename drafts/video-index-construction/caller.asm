main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, index_file_data
    daddiu $a0, $a0, 3
    dla $t0, index_file_count
    ld $a1, 0($t0)
    dla $a2, index_directory
    daddiu $a2, $a2, 8
    dla $a3, index_context
    dla $t0, index_scratch
    daddiu $t0, $t0, 4
    sd $t0, 8($a3)
index_call:
    jal video_index
    nop
index_call_return:
    dla $t0, index_last_status
    sd $v0, 0($t0)
    sd $v1, 8($t0)
    beqz $v0, index_main_success
    nop
    li $t0, 4
    beq $v0, $t0, index_main_missing
    nop
    dla $a0, index_failure_message
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
index_main_success:
    dla $a0, index_success_message
    b index_main_message
    nop
index_main_missing:
    dla $a0, index_missing_message
index_main_message:
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
index_function_return:
    nop
