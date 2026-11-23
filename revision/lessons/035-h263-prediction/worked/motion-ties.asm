# Flat data makes tie policy and candidate geometry directly observable.
.data
lesson_current: .byte 0:256
lesson_reference: .byte 0:25344
.align 3
lesson_request: .dword 0,0,16,176,16,32,2,1
lesson_result: .dword -1,-1,-1
.text
.globl main
main:
    dli $t0,-16
    and $sp,$sp,$t0
    dla $a0,lesson_request
    dla $t0,lesson_current
    sd $t0,0($a0)
    dla $t0,lesson_reference
    sd $t0,8($a0)
    dla $a1,lesson_result
    balc h263_search16
    nop
    dla $t0,lesson_result
    ld $s0,0($t0)
    ld $s1,8($t0)
    ld $s2,16($t0)
lesson_motion_interior:
    nop
    dla $a0,lesson_request
    sd $zero,32($a0)
    sd $zero,40($a0)
    dla $a1,lesson_result
    balc h263_search16
    nop
lesson_motion_corner:
    nop
    dla $a0,lesson_request
    li $t0,32
    sd $t0,40($a0)
    dla $a1,lesson_result
    balc h263_search16
    nop
lesson_motion_edge:
    nop
    li $v0,10
    syscall

.text
h263_search16:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, motion_early_return
    nop
    andi $t0, $a1, 7
    bnezc $t0, motion_early_return
    nop
    daddiu $t0, $a0, 64
    sltu $t0, $t0, $a0
    bnezc $t0, motion_early_return
    nop
    daddiu $t0, $a1, 24
    sltu $t0, $t0, $a1
    bnezc $t0, motion_early_return
    nop
    daddiu $sp, $sp, -192
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    sd $s7, 64($sp)
    sd $fp, 72($sp)
    move $fp, $a1
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $s5, 40($a0)
    ld $s6, 48($a0)
    ld $s7, 56($a0)
    sltiu $t0, $s2, 16
    bnezc $t0, motion_return
    nop
    sltiu $t0, $s2, 4097
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s3, 176
    bnezc $t0, motion_return
    nop
    sltiu $t0, $s3, 4097
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s4, 161
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s5, 129
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s6, 3
    beqzc $t0, motion_return
    nop
    sltiu $t0, $s7, 2
    beqzc $t0, motion_return
    nop
    dsll $t0, $s2, 4
    dsubu $t0, $t0, $s2
    daddiu $t0, $t0, 16
    daddu $t0, $t0, $s0
    sltu $t0, $t0, $s0
    bnezc $t0, motion_return
    nop
    li $t0, 143
    dmul $t0, $t0, $s3
    daddiu $t0, $t0, 176
    daddu $t0, $t0, $s1
    sltu $t0, $t0, $s1
    bnezc $t0, motion_return
    nop
    dsubu $t0, $zero, $s6
    sltu $t1, $s4, $s6
    beqzc $t1, motion_lo_x_ready
    nop
    dsubu $t0, $zero, $s4
motion_lo_x_ready:
    sd $t0, 128($sp)
    move $t0, $s6
    li $t1, 160
    dsubu $t1, $t1, $s4
    sltu $t2, $t1, $s6
    beqzc $t2, motion_hi_x_ready
    nop
    move $t0, $t1
motion_hi_x_ready:
    sd $t0, 136($sp)
    dsubu $t0, $zero, $s6
    sltu $t1, $s5, $s6
    beqzc $t1, motion_lo_y_ready
    nop
    dsubu $t0, $zero, $s5
motion_lo_y_ready:
    sd $t0, 144($sp)
    move $t0, $s6
    li $t1, 128
    dsubu $t1, $t1, $s5
    sltu $t2, $t1, $s6
    beqzc $t2, motion_hi_y_ready
    nop
    move $t0, $t1
motion_hi_y_ready:
    sd $t0, 152($sp)
    sd $zero, 80($sp)
    sd $zero, 88($sp)
    sd $zero, 96($sp)
    sd $zero, 104($sp)
    li $t0, 65536
    sd $t0, 112($sp)
    sd $zero, 120($sp)
    sd $zero, 160($sp)
motion_candidate:
    move $a0, $s0
    ld $t0, 80($sp)
    daddu $t0, $t0, $s4
    ld $t1, 88($sp)
    daddu $t1, $t1, $s5
    dmul $t1, $t1, $s3
    daddu $a1, $s1, $t1
    daddu $a1, $a1, $t0
    move $a2, $s2
    move $a3, $s3
    beqzc $s7, motion_scalar
    nop
    balc h263_sad16_msa
    nop
    bc motion_scored
    nop
motion_scalar:
    balc h263_sad16
    nop
motion_scored:
    bnezc $v0, motion_failed
    nop
    ld $t0, 120($sp)
    daddiu $t0, $t0, 1
    sd $t0, 120($sp)
    ld $t0, 112($sp)
    sltu $t1, $v1, $t0
    beqzc $t1, motion_keep
    nop
motion_update:
    sd $v1, 112($sp)
    ld $t0, 80($sp)
    sd $t0, 96($sp)
    ld $t0, 88($sp)
    sd $t0, 104($sp)
motion_keep:
    ld $t0, 160($sp)
    bnezc $t0, motion_advance
    nop
    li $t0, 1
    sd $t0, 160($sp)
    ld $t0, 128($sp)
    sd $t0, 80($sp)
    ld $t0, 144($sp)
    sd $t0, 88($sp)
    bc motion_rect_candidate
    nop
motion_advance:
    ld $t0, 80($sp)
    ld $t1, 136($sp)
    beqc $t0, $t1, motion_next_row
    nop
    daddiu $t0, $t0, 1
    sd $t0, 80($sp)
    bc motion_rect_candidate
    nop
motion_next_row:
    ld $t0, 88($sp)
    ld $t1, 152($sp)
    beqc $t0, $t1, motion_complete
    nop
    daddiu $t0, $t0, 1
    sd $t0, 88($sp)
    ld $t0, 128($sp)
    sd $t0, 80($sp)
motion_rect_candidate:
    ld $t0, 80($sp)
    ld $t1, 88($sp)
    or $t0, $t0, $t1
    beqzc $t0, motion_advance
    nop
    bc motion_candidate
    nop
motion_complete:
    ld $t0, 96($sp)
    sd $t0, 0($fp)
    ld $t0, 104($sp)
    sd $t0, 8($fp)
    ld $t0, 112($sp)
    sd $t0, 16($fp)
    move $v0, $zero
    ld $v1, 120($sp)
    bc motion_return
    nop
motion_failed:
    li $v0, 1
    move $v1, $zero
motion_return:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    ld $fp, 72($sp)
    daddiu $sp, $sp, 192
motion_early_return:
    jrc $ra
    nop

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
