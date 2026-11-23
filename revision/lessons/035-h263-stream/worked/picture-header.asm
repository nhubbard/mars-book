# Bounded worked experiment for Chapter 27. The independent reader remains unfinished.
.data
lesson_header: .byte -91:16
.align 3
lesson_context: .dword 0,7,0

.text
.globl main
main:
    andi $t0,$sp,15
    dsubu $sp,$sp,$t0
    dla $t0,lesson_context
    dla $t1,lesson_header
    sd $t1,0($t0)
    dla $a0,lesson_context
    li $a1,32
    li $a2,22
    balc h263_write_bits
    nop
    dla $t0,lesson_context
    ld $s0,16($t0)
lesson_header_prefix:
    nop
    dla $a0,lesson_context
    li $a1,0
    li $a2,8
    balc h263_write_bits
    nop
    dla $a0,lesson_context
    li $a1,4160
    li $a2,13
    balc h263_write_bits
    nop
    dla $a0,lesson_context
    li $a1,8
    li $a2,5
    balc h263_write_bits
    nop
    dla $a0,lesson_context
    li $a1,0
    li $a2,1
    balc h263_write_bits
    nop
    dla $a0,lesson_context
    li $a1,0
    li $a2,1
    balc h263_write_bits
    nop
    dla $t0,lesson_header
    li $t1,7
    move $s4,$zero
lesson_header_bytes:
    lbu $t2,0($t0)
    dsll $s4,$s4,8
    or $s4,$s4,$t2
    daddiu $t0,$t0,1
    daddiu $t1,$t1,-1
    bnezc $t1,lesson_header_bytes
    dsrl $s0,$s4,34
    dsrl $s1,$s4,26
    andi $s1,$s1,255
    dsrl $s2,$s4,13
    andi $s2,$s2,8191
    dsrl $s3,$s4,8
    andi $s3,$s3,31
lesson_header_fields:
    nop
    li $s0,56
    li $s1,50
    li $s2,50
    dsubu $s3,$s1,$s2
    andi $s4,$s4,63
lesson_valid_bits:
    nop
    li $v0,10
    syscall

h263_write_bits:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, writer_return
    nop
    daddiu $t0, $a0, 24
    sltu $t0, $t0, $a0
    bnezc $t0, writer_return
    nop
    ld $t0, 0($a0)
    ld $t1, 8($a0)
    ld $t2, 16($a0)
    dli $t3, 1048576
    sltu $t3, $t3, $t1
    bnezc $t3, writer_return
    nop
    daddu $t3, $t0, $t1
    sltu $t3, $t3, $t0
    bnezc $t3, writer_return
    nop
    dsll $t1, $t1, 3
    sltu $t3, $t1, $t2
    bnezc $t3, writer_return
    nop
    sltiu $t3, $a2, 33
    beqzc $t3, writer_return
    nop
    dsrlv $t3, $a1, $a2
    bnezc $t3, writer_return
    nop
    dsubu $t1, $t1, $t2
    sltu $t1, $t1, $a2
    bnezc $t1, writer_no_space
    nop
    move $v0, $zero
    beqzc $a2, writer_return
    nop
    move $t3, $a2
writer_chunk_loop:
    dsrl $t1, $t2, 3
    daddu $t1, $t1, $t0
    andi $t5, $t2, 7
    li $t4, 8
    dsubu $t4, $t4, $t5
    sltu $t9, $t3, $t4
    beqzc $t9, writer_take_ready
    nop
    move $t4, $t3
writer_take_ready:
    dsubu $t7, $t3, $t4
    dsrlv $t8, $a1, $t7
    li $t6, 1
    dsllv $t6, $t6, $t4
    daddiu $t6, $t6, -1
    and $t8, $t8, $t6
    li $t7, 8
    dsubu $t7, $t7, $t5
    dsubu $t7, $t7, $t4
    dsllv $t8, $t8, $t7
    dsllv $t6, $t6, $t7
    nor $t6, $t6, $zero
    lbu $t9, 0($t1)
    and $t9, $t9, $t6
    or $t9, $t9, $t8
    sb $t9, 0($t1)
    daddu $t2, $t2, $t4
    dsubu $t3, $t3, $t4
    bnezc $t3, writer_chunk_loop
    nop
    sd $t2, 16($a0)
    move $v1, $a2
    jrc $ra
    nop
writer_no_space:
    li $v0, 2
writer_return:
    jrc $ra
    nop
