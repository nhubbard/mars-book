# Carry in your completed transform tables/functions and quantizer function.
.data
block_current: .byte -126:64
block_prediction: .byte -128:64
.align 3
block_request: .dword 0, 8, 0, 8, 8, 0, 1, 0
.align 4
block_result: .byte -91:448
block_last_status: .dword -1
block_last_count: .dword -1
block_newline: .asciiz "\n"
block_unfinished: .asciiz "Block reconstruction is still under construction.\n"
block_invalid: .asciiz "Invalid block reconstruction request.\n"
block_dependency: .asciiz "Transform or quantizer dependency failed.\n"
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, block_request
    dla $t0, block_current
    sd $t0, 0($a0)
    ld $t0, 40($a0)
    bnez $t0, block_caller_intra
    nop
    dla $t0, block_prediction
    sd $t0, 16($a0)
    b block_caller_ready
    nop
block_caller_intra:
    sd $zero, 16($a0)
    sd $zero, 24($a0)
block_caller_ready:
    dla $a1, block_result
    jal h263_encode_block8
    nop
block_caller_return:
    dla $t0, block_last_status
    sd $v0, 0($t0)
    dla $t0, block_last_count
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, block_caller_unfinished
    nop
    li $t0, 2
    beq $v0, $t0, block_caller_dependency
    nop
    bnez $v0, block_caller_invalid
    nop
    dla $t0, block_result
    lbu $a0, 384($t0)
    li $v0, 1
    syscall
    dla $a0, block_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
block_caller_unfinished:
    dla $a0, block_unfinished
    b block_caller_fail
    nop
block_caller_dependency:
    dla $a0, block_dependency
    b block_caller_fail
    nop
block_caller_invalid:
    dla $a0, block_invalid
block_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_encode_block8:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
# Replace these stubs with your own completed prior entries and required data.
h263_transform8:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
h263_quantize8:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
