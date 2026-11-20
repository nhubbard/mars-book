# Complete h263_emit_coefficients and carry in your completed h263_write_bits.
.data
.align 3
coeff_scan:
    .dword 0,1,8,16,9,2,3,10,17,24,32,25,18,11,4,5,12,19,26,33,40,48,41,34,27,20,13,6,7,14,21,28,35,42,49,56,57,50,43,36,29,22,15,23,30,37,44,51,58,59,52,45,38,31,39,46,53,60,61,54,47,55,62,63
.align 3
coeff_codes:
    .dword 0,0,1,4,3
    .dword 0,0,2,30,5
    .dword 0,0,3,42,7
    .dword 0,0,4,46,8
    .dword 0,0,5,62,9
    .dword 0,0,6,74,10
    .dword 0,0,7,72,10
    .dword 0,0,8,66,11
    .dword 0,0,9,64,11
    .dword 0,0,10,14,12
    .dword 0,0,11,12,12
    .dword 0,0,12,64,12
    .dword 0,1,1,12,4
    .dword 0,1,2,40,7
    .dword 0,1,3,60,9
    .dword 0,1,4,30,11
    .dword 0,1,5,66,12
    .dword 0,1,6,160,13
    .dword 0,2,1,28,5
    .dword 0,2,2,58,9
    .dword 0,2,3,28,11
    .dword 0,2,4,162,13
    .dword 0,3,1,26,6
    .dword 0,3,2,70,10
    .dword 0,3,3,26,11
    .dword 0,4,1,24,6
    .dword 0,4,2,68,10
    .dword 0,4,3,164,13
    .dword 0,5,1,22,6
    .dword 0,5,2,24,11
    .dword 0,5,3,166,13
    .dword 0,6,1,38,7
    .dword 0,6,2,22,11
    .dword 0,6,3,168,13
    .dword 0,7,1,36,7
    .dword 0,7,2,20,11
    .dword 0,8,1,34,7
    .dword 0,8,2,18,11
    .dword 0,9,1,32,7
    .dword 0,9,2,16,11
    .dword 0,10,1,44,8
    .dword 0,10,2,170,13
    .dword 0,11,1,42,8
    .dword 0,12,1,40,8
    .dword 0,13,1,56,9
    .dword 0,14,1,54,9
    .dword 0,15,1,66,10
    .dword 0,16,1,64,10
    .dword 0,17,1,62,10
    .dword 0,18,1,60,10
    .dword 0,19,1,58,10
    .dword 0,20,1,56,10
    .dword 0,21,1,54,10
    .dword 0,22,1,52,10
    .dword 0,23,1,68,12
    .dword 0,24,1,70,12
    .dword 0,25,1,172,13
    .dword 0,26,1,174,13
    .dword 1,0,1,14,5
    .dword 1,0,2,50,10
    .dword 1,0,3,10,12
    .dword 1,1,1,30,7
    .dword 1,1,2,8,12
    .dword 1,2,1,28,7
    .dword 1,3,1,26,7
    .dword 1,4,1,24,7
    .dword 1,5,1,38,8
    .dword 1,6,1,36,8
    .dword 1,7,1,34,8
    .dword 1,8,1,32,8
    .dword 1,9,1,52,9
    .dword 1,10,1,50,9
    .dword 1,11,1,48,9
    .dword 1,12,1,46,9
    .dword 1,13,1,44,9
    .dword 1,14,1,42,9
    .dword 1,15,1,40,9
    .dword 1,16,1,38,9
    .dword 1,17,1,48,10
    .dword 1,18,1,46,10
    .dword 1,19,1,44,10
    .dword 1,20,1,42,10
    .dword 1,21,1,40,10
    .dword 1,22,1,38,10
    .dword 1,23,1,36,10
    .dword 1,24,1,34,10
    .dword 1,25,1,14,11
    .dword 1,26,1,12,11
    .dword 1,27,1,10,11
    .dword 1,28,1,8,11
    .dword 1,29,1,72,12
    .dword 1,30,1,74,12
    .dword 1,31,1,76,12
    .dword 1,32,1,78,12
    .dword 1,33,1,176,13
    .dword 1,34,1,178,13
    .dword 1,35,1,180,13
    .dword 1,36,1,182,13
    .dword 1,37,1,184,13
    .dword 1,38,1,186,13
    .dword 1,39,1,188,13
    .dword 1,40,1,190,13

.align 1
coeff_input: .half 255,1,0,0,0,0,0,0,-1
    .half 0:55
coeff_input_end:
coeff_output: .byte -91:192
.align 3
coeff_context: .dword 0, 192, 3
coeff_request: .dword 0, 0, 1, 0
coeff_last_status: .dword -1
coeff_last_bits: .dword -1
coeff_newline: .asciiz "\n"
coeff_unfinished: .asciiz "Coefficient coding is still under construction.\n"
coeff_invalid: .asciiz "Invalid coefficient request, context or level.\n"
coeff_full: .asciiz "Coefficient output has insufficient capacity.\n"
coeff_dependency: .asciiz "Bit writer dependency failed.\n"
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $t0, coeff_context
    dla $t1, coeff_output
    sd $t1, 0($t0)
    dla $a0, coeff_request
    sd $t0, 8($a0)
    dla $t0, coeff_input
    sd $t0, 0($a0)
    jal h263_emit_coefficients
    nop
coeff_caller_return:
    dla $t0, coeff_last_status
    sd $v0, 0($t0)
    dla $t0, coeff_last_bits
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, coeff_caller_unfinished
    nop
    li $t0, 3
    beq $v0, $t0, coeff_caller_dependency
    nop
    li $t0, 2
    beq $v0, $t0, coeff_caller_full
    nop
    bnez $v0, coeff_caller_invalid
    nop
    move $a0, $v1
    li $v0, 1
    syscall
    dla $a0, coeff_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
coeff_caller_unfinished:
    dla $a0, coeff_unfinished
    b coeff_caller_fail
    nop
coeff_caller_dependency:
    dla $a0, coeff_dependency
    b coeff_caller_fail
    nop
coeff_caller_full:
    dla $a0, coeff_full
    b coeff_caller_fail
    nop
coeff_caller_invalid:
    dla $a0, coeff_invalid
coeff_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_emit_coefficients:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
# Replace the dependency stub with your own completed bit writer.
h263_write_bits:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
