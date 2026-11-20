# Complete h263_predict8; data preparation preserves the whole instruction section.
.data
.align 4
prediction_current: .byte -91:656
prediction_current_end:
prediction_reference: .byte -91:745
prediction_reference_end:
.align 3
prediction_request:
    .dword 0, 0, 88, 88
    .dword 1, 1, 1, 0
.align 4
prediction_result: .byte -91:192
prediction_last_status: .dword -1
prediction_last_sad: .dword -1
prediction_space: .asciiz " "
prediction_newline: .asciiz "\n"
prediction_unfinished: .asciiz "Prediction is still under construction.\n"
prediction_error: .asciiz "Invalid prediction request.\n"
.text
.globl main
main:
    dla $a0, prediction_request
    dla $t0, prediction_current
    daddiu $t0, $t0, 16
    sd $t0, 0($a0)
    dla $t0, prediction_reference
    daddiu $t0, $t0, 16
    sd $t0, 8($a0)
    dla $a1, prediction_result
    jal h263_predict8
    nop
prediction_caller_return:
    dla $t0, prediction_last_status
    sd $v0, 0($t0)
    dla $t0, prediction_last_sad
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, prediction_caller_unfinished
    nop
    bnez $v0, prediction_caller_error
    nop
    dla $t0, prediction_result
    lbu $a0, 0($t0)
    li $v0, 1
    syscall
    dla $a0, prediction_space
    li $v0, 4
    syscall
    dla $t0, prediction_result
    lh $a0, 64($t0)
    li $v0, 1
    syscall
    dla $a0, prediction_space
    li $v0, 4
    syscall
    dla $t0, prediction_last_sad
    ld $a0, 0($t0)
    li $v0, 1
    syscall
    dla $a0, prediction_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
prediction_caller_unfinished:
    dla $a0, prediction_unfinished
    b prediction_caller_fail
    nop
prediction_caller_error:
    dla $a0, prediction_error
prediction_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_predict8:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
