# Complete the transform entry; preparation changes data and controls only.
.data
.align 4
transform_input: .half 128:64
transform_input_end:
.align 3
transform_request: .dword 0, 16, 0, 1, 0, 0
.align 4
transform_result: .byte -91:128
transform_last_status: .dword -1
transform_last_count: .dword -1
transform_newline: .asciiz "\n"
transform_unfinished: .asciiz "Transform is still under construction.\n"
transform_invalid: .asciiz "Invalid transform request or input domain.\n"
.data
.align 4
transform_B_h:
    .half 23170,23170,23170,23170,23170,23170,23170,23170
    .half 32138,27246,18205,6393,-6393,-18205,-27246,-32138
    .half 30274,12540,-12540,-30274,-30274,-12540,12540,30274
    .half 27246,-6393,-32138,-18205,18205,32138,6393,-27246
    .half 23170,-23170,-23170,23170,23170,-23170,-23170,23170
    .half 18205,-32138,6393,27246,-27246,-6393,32138,-18205
    .half 12540,-30274,30274,-12540,-12540,30274,-30274,12540
    .half 6393,-18205,27246,-32138,32138,-27246,18205,-6393
.align 4
transform_B_w:
    .word 23170,23170,23170,23170,23170,23170,23170,23170
    .word 32138,27246,18205,6393,-6393,-18205,-27246,-32138
    .word 30274,12540,-12540,-30274,-30274,-12540,12540,30274
    .word 27246,-6393,-32138,-18205,18205,32138,6393,-27246
    .word 23170,-23170,-23170,23170,23170,-23170,-23170,23170
    .word 18205,-32138,6393,27246,-27246,-6393,32138,-18205
    .word 12540,-30274,30274,-12540,-12540,30274,-30274,12540
    .word 6393,-18205,27246,-32138,32138,-27246,18205,-6393
.align 4
transform_BT_h:
    .half 23170,32138,30274,27246,23170,18205,12540,6393
    .half 23170,27246,12540,-6393,-23170,-32138,-30274,-18205
    .half 23170,18205,-12540,-32138,-23170,6393,30274,27246
    .half 23170,6393,-30274,-18205,23170,27246,-12540,-32138
    .half 23170,-6393,-30274,18205,23170,-27246,-12540,32138
    .half 23170,-18205,-12540,32138,-23170,-6393,30274,-27246
    .half 23170,-27246,12540,6393,-23170,32138,-30274,18205
    .half 23170,-32138,30274,-27246,23170,-18205,12540,-6393
.align 4
transform_BT_w:
    .word 23170,32138,30274,27246,23170,18205,12540,6393
    .word 23170,27246,12540,-6393,-23170,-32138,-30274,-18205
    .word 23170,18205,-12540,-32138,-23170,6393,30274,27246
    .word 23170,6393,-30274,-18205,23170,27246,-12540,-32138
    .word 23170,-6393,-30274,18205,23170,-27246,-12540,32138
    .word 23170,-18205,-12540,32138,-23170,-6393,30274,-27246
    .word 23170,-27246,12540,6393,-23170,32138,-30274,18205
    .word 23170,-32138,30274,-27246,23170,-18205,12540,-6393
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, transform_request
    dla $t0, transform_input
    sd $t0, 0($a0)
    dla $a1, transform_result
    jal h263_transform8
    nop
transform_caller_return:
    dla $t0, transform_last_status
    sd $v0, 0($t0)
    dla $t0, transform_last_count
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, transform_caller_unfinished
    nop
    bnez $v0, transform_caller_invalid
    nop
    dla $t0, transform_result
    lh $a0, 0($t0)
    li $v0, 1
    syscall
    dla $a0, transform_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
transform_caller_unfinished:
    dla $a0, transform_unfinished
    b transform_caller_fail
    nop
transform_caller_invalid:
    dla $a0, transform_invalid
transform_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_transform8:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
