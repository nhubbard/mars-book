# Complete h263_emit_macroblock and carry your coefficient emitter and writer.
.data
.align 3
syntax_mcbpc_codes:
    .dword 1,1
    .dword 1,3
    .dword 2,3
    .dword 3,3
    .dword 1,1
    .dword 3,4
    .dword 2,4
    .dword 5,6
    .dword 3,5
    .dword 4,8
    .dword 3,8
    .dword 3,7
syntax_cbpy_codes:
    .dword 3,4
    .dword 5,5
    .dword 4,5
    .dword 9,4
    .dword 3,5
    .dword 7,4
    .dword 2,6
    .dword 11,4
    .dword 2,5
    .dword 3,6
    .dword 5,4
    .dword 10,4
    .dword 4,4
    .dword 8,4
    .dword 6,4
    .dword 3,2
syntax_mvd_codes:
    .dword 5,13
    .dword 7,13
    .dword 5,12
    .dword 7,12
    .dword 9,12
    .dword 11,12
    .dword 13,12
    .dword 15,12
    .dword 9,11
    .dword 11,11
    .dword 13,11
    .dword 15,11
    .dword 17,11
    .dword 19,11
    .dword 21,11
    .dword 23,11
    .dword 25,11
    .dword 27,11
    .dword 29,11
    .dword 31,11
    .dword 33,11
    .dword 35,11
    .dword 19,10
    .dword 21,10
    .dword 23,10
    .dword 7,8
    .dword 9,8
    .dword 11,8
    .dword 7,7
    .dword 3,5
    .dword 3,4
    .dword 3,3
    .dword 1,1
    .dword 2,3
    .dword 2,4
    .dword 2,5
    .dword 6,7
    .dword 10,8
    .dword 8,8
    .dword 6,8
    .dword 22,10
    .dword 20,10
    .dword 18,10
    .dword 34,11
    .dword 32,11
    .dword 30,11
    .dword 28,11
    .dword 26,11
    .dword 24,11
    .dword 22,11
    .dword 20,11
    .dword 18,11
    .dword 16,11
    .dword 14,11
    .dword 12,11
    .dword 10,11
    .dword 8,11
    .dword 14,12
    .dword 12,12
    .dword 10,12
    .dword 8,12
    .dword 6,12
    .dword 4,12
    .dword 6,13

.align 1
syntax_levels:
    .half 255
    .half 0:63
    .half 255
    .half 0:63
    .half 255
    .half 0:63
    .half 255
    .half 0:63
    .half 255
    .half 0:63
    .half 255
    .half 0:63
syntax_levels_end:
syntax_output: .byte -91:1152
.align 3
syntax_context: .dword 0, 1152, 3
syntax_request: .dword 0, 0, 0, 1, 0, 0, 0, 0, 1, 0
syntax_last_status: .dword -1
syntax_last_bits: .dword -1
syntax_newline: .asciiz "\n"
syntax_unfinished: .asciiz "Macroblock syntax is still under construction.\n"
syntax_invalid: .asciiz "Invalid macroblock syntax request, context or level.\n"
syntax_full: .asciiz "Macroblock syntax output has insufficient capacity.\n"
syntax_dependency: .asciiz "Macroblock syntax dependency failed.\n"
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $t0, syntax_context
    dla $t1, syntax_output
    sd $t1, 0($t0)
    dla $a0, syntax_request
    sd $t0, 8($a0)
    dla $t0, syntax_levels
    sd $t0, 0($a0)
    jal h263_emit_macroblock
    nop
syntax_caller_return:
    dla $t0, syntax_last_status
    sd $v0, 0($t0)
    dla $t0, syntax_last_bits
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, syntax_caller_unfinished
    nop
    li $t0, 3
    beq $v0, $t0, syntax_caller_dependency
    nop
    li $t0, 2
    beq $v0, $t0, syntax_caller_full
    nop
    bnez $v0, syntax_caller_invalid
    nop
    move $a0, $v1
    li $v0, 1
    syscall
    dla $a0, syntax_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
syntax_caller_unfinished:
    dla $a0, syntax_unfinished
    b syntax_caller_fail
    nop
syntax_caller_dependency:
    dla $a0, syntax_dependency
    b syntax_caller_fail
    nop
syntax_caller_full:
    dla $a0, syntax_full
    b syntax_caller_fail
    nop
syntax_caller_invalid:
    dla $a0, syntax_invalid
syntax_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_emit_macroblock:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
# Replace both dependency stubs and carry their required data/helpers.
h263_emit_coefficients:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
h263_write_bits:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
