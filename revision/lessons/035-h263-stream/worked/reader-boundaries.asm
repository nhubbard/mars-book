# Bounded worked experiment for Chapter 27. The independent reader remains unfinished.
.data
 .align 2
lesson_full_field: .word 0x80000001
.align 3
lesson_large_width: .dword 0x100000009

.text
.globl main
main:
    andi $t0,$sp,15
    dsubu $sp,$sp,$t0
    dla $t0,lesson_full_field
    lw $s0,0($t0)
    lwu $s1,0($t0)
lesson_unsigned_field:
    nop
    li $s0,16
    li $s1,12
    li $s2,12
    dsubu $s3,$s1,$s2
lesson_remaining_bits:
    nop
    dla $t0,lesson_large_width
    ld $s0,0($t0)
    andi $s1,$s0,65535
    sltiu $s2,$s0,33
lesson_full_width:
    nop
    li $v0,10
    syscall

