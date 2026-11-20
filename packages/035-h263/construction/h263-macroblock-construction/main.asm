# Carry your completed h263_predict8 into this new working source.
.data
.align 4
macro_current: .byte -91:38048
macro_current_end:
macro_reference: .byte -91:38048
macro_reference_end:
.align 3
macro_request:
    .dword 0, 0, 0, 0, 0, 0
    .dword 176, 88, 88, 176, 88, 88
    .dword 16, 32, -2, -2, 1, 0
.align 4
macro_result: .byte -91:1152
macro_last_status: .dword -1
macro_last_sad: .dword -1
macro_newline: .asciiz "\n"
macro_unfinished: .asciiz "Macroblock prediction is still under construction.\n"
macro_invalid: .asciiz "Invalid macroblock request.\n"
macro_dependency: .asciiz "Block prediction failed or is unfinished.\n"
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, macro_request
    dla $t0, macro_current
    daddiu $t0, $t0, 16
    sd $t0, 0($a0)
    daddiu $t1, $t0, 25344
    sd $t1, 8($a0)
    daddiu $t1, $t1, 6336
    sd $t1, 16($a0)
    dla $t0, macro_reference
    daddiu $t0, $t0, 16
    sd $t0, 24($a0)
    daddiu $t1, $t0, 25344
    sd $t1, 32($a0)
    daddiu $t1, $t1, 6336
    sd $t1, 40($a0)
    dla $a1, macro_result
    jal h263_predict_mb
    nop
macro_caller_return:
    dla $t0, macro_last_status
    sd $v0, 0($t0)
    dla $t0, macro_last_sad
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, macro_caller_unfinished
    nop
    li $t0, 2
    beq $v0, $t0, macro_caller_dependency
    nop
    bnez $v0, macro_caller_invalid
    nop
    move $a0, $v1
    li $v0, 1
    syscall
    dla $a0, macro_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
macro_caller_unfinished:
    dla $a0, macro_unfinished
    b macro_caller_fail
    nop
macro_caller_dependency:
    dla $a0, macro_dependency
    b macro_caller_fail
    nop
macro_caller_invalid:
    dla $a0, macro_invalid
macro_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_predict_mb:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
h263_predict8:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
