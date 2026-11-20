# Complete h263_write_bits; the caller appends two fields to one context.
.data
writer_buffer: .byte -91:8
.align 3
writer_context: .dword 0, 8, 0
writer_first: .dword 5, 3
writer_second: .dword 19, 5
writer_last_status: .dword -1
writer_last_count: .dword -1
writer_newline: .asciiz "\n"
writer_unfinished: .asciiz "Bit writer is still under construction.\n"
writer_invalid: .asciiz "Invalid bit writer context or field.\n"
writer_full: .asciiz "Bit writer has insufficient capacity.\n"
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, writer_context
    dla $t0, writer_buffer
    sd $t0, 0($a0)
    dla $t0, writer_first
    ld $a1, 0($t0)
    ld $a2, 8($t0)
    jal h263_write_bits
    nop
writer_first_return:
    bnez $v0, writer_caller_status
    nop
    dla $a0, writer_context
    dla $t0, writer_second
    ld $a1, 0($t0)
    ld $a2, 8($t0)
    jal h263_write_bits
    nop
writer_caller_return:
writer_caller_status:
    dla $t0, writer_last_status
    sd $v0, 0($t0)
    dla $t0, writer_last_count
    sd $v1, 0($t0)
    li $t0, 4
    beq $v0, $t0, writer_caller_unfinished
    nop
    li $t0, 2
    beq $v0, $t0, writer_caller_full
    nop
    bnez $v0, writer_caller_invalid
    nop
    dla $t0, writer_buffer
    lbu $a0, 0($t0)
    li $v0, 1
    syscall
    dla $a0, writer_newline
    li $v0, 4
    syscall
    move $a0, $zero
    li $v0, 17
    syscall
writer_caller_unfinished:
    dla $a0, writer_unfinished
    b writer_caller_fail
    nop
writer_caller_full:
    dla $a0, writer_full
    b writer_caller_fail
    nop
writer_caller_invalid:
    dla $a0, writer_invalid
writer_caller_fail:
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 17
    syscall
h263_write_bits:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
