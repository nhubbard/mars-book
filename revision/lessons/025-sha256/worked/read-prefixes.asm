# Worked experiment: count file bytes and publish the count ONLY at EOF.
# This is a byte-count interface, not a SHA function or partial-message digest.
.data
path: .asciiz "fixture.bin"
scratch: .byte 165:7
.align 2
output_count: .word 0xa5a5a5a5
status: .word -1
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $a0, path
    dla $a1, scratch
    li $a2, 7
    dla $a3, output_count
file_call:
    balc count_file
    nop
main_done:
    dla $t0, status
    sw $v0, 0($t0)
results_ready:
    li $v0, 10
    syscall

# a0=path, a1=scratch, a2=capacity, a3=output_count.
# Returns 0 at EOF, 1 on failed open, 2 on read error. Failure preserves output.
count_file:
    daddiu $sp, $sp, -64
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    move $s0, $a1
    move $s1, $a2
    move $s2, $a3
    move $s3, $zero
    move $a1, $zero
    move $a2, $zero
    li $v0, 13
    syscall
    bltzc $v0, open_error
    nop
    move $s4, $v0
read_next:
    move $a0, $s4
    move $a1, $s0
    move $a2, $s1
    li $v0, 14
    syscall
    bltzc $v0, read_error
    nop
    beqzc $v0, file_eof
    nop
    daddu $s3, $s3, $v0
    bc read_next
    nop
file_eof:
    sw $s3, 0($s2)
    move $s5, $zero
    bc close_file
    nop
read_error:
    li $s5, 2
close_file:
    move $a0, $s4
    li $v0, 16
    syscall
    move $v0, $s5
    bc return_count
    nop
open_error:
    li $v0, 1
return_count:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    daddiu $sp, $sp, 64
    jrc $ra
    nop
