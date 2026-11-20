# Keep your completed SAD entries when combining this new stage.
.data
.align 4
motion_current: .byte -91:2688
motion_current_end:
motion_reference: .byte -91:25376
motion_reference_end:
.align 3
motion_request:
    .dword 0, 0, 176, 176
    .dword 16, 32, 2, 1
motion_result: .dword -99, -99, -99
motion_last_status: .dword -1
motion_last_count: .dword -1
motion_space: .asciiz " "
motion_newline: .asciiz "\n"
motion_unfinished: .asciiz "Motion search is still under construction.\n"
motion_error: .asciiz "Invalid motion search request.\n"
.text
.globl main
main:
    dla $a0, motion_request
    dla $t0, motion_current
    daddiu $t0, $t0, 16
    sd $t0, 0($a0)
    dla $t0, motion_reference
    daddiu $t0, $t0, 16
    sd $t0, 8($a0)
    dla $a1, motion_result
    jal h263_search16
    nop
motion_caller_return:
    dla $t0, motion_last_status
    sd $v0, 0($t0)
    dla $t0, motion_last_count
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, motion_caller_unfinished
    nop
    bnez $v0, motion_caller_error
    nop
    dla $t0, motion_result
    ld $a0, 0($t0)
    li $v0, 1
    syscall
    dla $a0, motion_space
    li $v0, 4
    syscall
    dla $t0, motion_result
    ld $a0, 8($t0)
    li $v0, 1
    syscall
    dla $a0, motion_space
    li $v0, 4
    syscall
    dla $t0, motion_result
    ld $a0, 16($t0)
    li $v0, 1
    syscall
    dla $a0, motion_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
motion_caller_unfinished:
    dla $a0, motion_unfinished
    b motion_caller_fail
    nop
motion_caller_error:
    dla $a0, motion_error
motion_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_search16:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
# Replace these development entries with your completed earlier SAD functions.
h263_sad16:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
h263_sad16_msa:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
