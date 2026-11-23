# Strided active rows, wide vector accumulation and exact input alias.
.data
lesson_current: .byte -91:3
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
    .byte -91:160
    .byte -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1
lesson_reference: .byte -91:3
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .byte -91:160
    .byte 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
.text
.globl main
main:
    dli $t0,-16
    and $sp,$sp,$t0
    dla $a0,lesson_current
    daddiu $a0,$a0,3
    dla $a1,lesson_reference
    daddiu $a1,$a1,3
    li $a2,176
    li $a3,176
    balc h263_sad16
    nop
    move $s0,$v1
    dla $a0,lesson_current
    daddiu $a0,$a0,3
    dla $a1,lesson_reference
    daddiu $a1,$a1,3
    li $a2,176
    li $a3,176
    balc h263_sad16_msa
    nop
    move $s1,$v1
lesson_sad_compared:
    nop
    dla $a0,lesson_current
    daddiu $a0,$a0,3
    move $a1,$a0
    li $a2,176
    li $a3,176
    balc h263_sad16_msa
    nop
lesson_sad_same_window:
    nop
    li $v0,10
    syscall

.text
h263_sad16:
    move $t9, $zero
    bc sad_entry
    nop
h263_sad16_msa:
    li $t9, 1
sad_entry:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a2, 16
    bnezc $t0, sad_return
    nop
    sltiu $t0, $a2, 4097
    beqzc $t0, sad_return
    nop
    sltiu $t0, $a3, 16
    bnezc $t0, sad_return
    nop
    sltiu $t0, $a3, 4097
    beqzc $t0, sad_return
    nop
    dsll $t0, $a2, 4
    dsubu $t0, $t0, $a2
    daddiu $t0, $t0, 16
    daddu $t1, $a0, $t0
    sltu $t1, $t1, $a0
    bnezc $t1, sad_return
    nop
    dsll $t0, $a3, 4
    dsubu $t0, $t0, $a3
    daddiu $t0, $t0, 16
    daddu $t1, $a1, $t0
    sltu $t1, $t1, $a1
    bnezc $t1, sad_return
    nop
sad_admitted:
    nop
    move $t0, $a0
    move $t1, $a1
    li $t2, 16
    move $t8, $zero
    bnezc $t9, sad_vector
    nop
sad_scalar_row:
    move $t4, $t0
    move $t5, $t1
    li $t3, 16
sad_scalar_byte:
    lbu $t6, 0($t4)
    lbu $t7, 0($t5)
    subu $t6, $t6, $t7
    abs $t6, $t6
    daddu $t8, $t8, $t6
    daddiu $t4, $t4, 1
    daddiu $t5, $t5, 1
    daddiu $t3, $t3, -1
    bnezc $t3, sad_scalar_byte
    nop
    daddu $t0, $t0, $a2
    daddu $t1, $t1, $a3
    daddiu $t2, $t2, -1
    bnezc $t2, sad_scalar_row
    nop
    move $v1, $t8
    bc sad_success
    nop
sad_vector:
    ldi.b $w4, 0
sad_vector_row:
    ld.b $w0, 0($t0)
    ld.b $w1, 0($t1)
    asub_u.b $w2, $w0, $w1
    hadd_u.h $w3, $w2, $w2
    addv.h $w4, $w4, $w3
    daddu $t0, $t0, $a2
    daddu $t1, $t1, $a3
    daddiu $t2, $t2, -1
    bnezc $t2, sad_vector_row
    nop
sad_vector_accumulated:
    nop
    hadd_u.w $w5, $w4, $w4
    hadd_u.d $w6, $w5, $w5
    copy_s.d $t6, $w6[0]
    copy_s.d $t7, $w6[1]
    daddu $v1, $t6, $t7
sad_success:
    move $v0, $zero
sad_result_ready:
    nop
sad_return:
    jrc $ra
    nop
