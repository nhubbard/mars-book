# Complete h263_quantize8. Preparation edits data controls only.
.data
.align 4
quant_input: .half 1024,15,16,-16,31,32,-32,2047,-2048
    .half 0:55
quant_input_end:
.align 3
quant_request: .dword 0, 8, 1, 1
.align 4
quant_result: .byte -91:256
quant_last_status: .dword -1
quant_last_count: .dword -1
quant_newline: .asciiz "\n"
quant_unfinished: .asciiz "Quantizer is still under construction.\n"
quant_invalid: .asciiz "Invalid quantizer request or coefficient domain.\n"
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, quant_request
    dla $t0, quant_input
    sd $t0, 0($a0)
    dla $a1, quant_result
    jal h263_quantize8
    nop
quant_caller_return:
    dla $t0, quant_last_status
    sd $v0, 0($t0)
    dla $t0, quant_last_count
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, quant_caller_unfinished
    nop
    bnez $v0, quant_caller_invalid
    nop
    dla $t0, quant_result
    lh $a0, 0($t0)
    li $v0, 1
    syscall
    dla $a0, quant_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
quant_caller_unfinished:
    dla $a0, quant_unfinished
    b quant_caller_fail
    nop
quant_caller_invalid:
    dla $a0, quant_invalid
quant_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_quantize8:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
