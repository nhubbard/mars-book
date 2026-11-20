# Prepare worked inputs from the generated QCIF fixture before Run or Debug.
.data
.align 4
sad_left: .byte -91:2688
sad_left_end:
sad_right: .byte -91:2688
sad_right_end:
sad_stride: .dword 176
sad_mode: .word 0
sad_last_status: .word -1
.align 3
sad_last_sum: .dword -1
sad_newline: .asciiz "\n"
sad_unfinished: .asciiz "SAD kernel is still under construction.\n"
sad_invalid: .asciiz "Invalid SAD window.\n"
.text
.globl main
main:
    dla $a0, sad_left
    daddiu $a0, $a0, 16
    dla $a1, sad_right
    daddiu $a1, $a1, 16
    dla $t0, sad_stride
    ld $a2, 0($t0)
    move $a3, $a2
    dla $t0, sad_mode
    lwu $t0, 0($t0)
    beqz $t0, sad_caller_scalar
    nop
    jal h263_sad16_msa
    nop
    b sad_caller_return
    nop
sad_caller_scalar:
    jal h263_sad16
    nop
sad_caller_return:
    dla $t0, sad_last_status
    sw $v0, 0($t0)
    dla $t0, sad_last_sum
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, sad_caller_unfinished
    nop
    bnez $v0, sad_caller_invalid
    nop
    move $a0, $v1
    li $v0, 1
    syscall
    dla $a0, sad_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
sad_caller_unfinished:
    dla $a0, sad_unfinished
    b sad_caller_error
    nop
sad_caller_invalid:
    dla $a0, sad_invalid
sad_caller_error:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
# Complete these callable entries; the preparation helper preserves instruction text.
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
