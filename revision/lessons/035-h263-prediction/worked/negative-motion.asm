# Negative odd motion uses floor and a half phase for chroma.
.data
lesson_current: .byte -128:38016
lesson_reference: .byte -128:38016
.align 4
lesson_output: .byte -91:1152
.align 3
lesson_request: .dword 0,0,0,0,0,0,176,88,88,176,88,88,16,32,-1,-1,1,0
.text
.globl main
main:
    dli $t0,-16
    and $sp,$sp,$t0
    dli $s0,-1
    dsra $s1,$s0,1
    li $t0,2
    ddiv $s2,$s0,$t0
    andi $s3,$s0,1
    daddiu $s4,$s1,8
    daddiu $s5,$s1,16
lesson_negative_origin:
    nop
    dla $a0,lesson_request
    dla $t0,lesson_current
    sd $t0,0($a0)
    daddiu $t1,$t0,25344
    sd $t1,8($a0)
    daddiu $t1,$t0,31680
    sd $t1,16($a0)
    dla $t0,lesson_reference
    sd $t0,24($a0)
    daddiu $t1,$t0,25344
    sd $t1,32($a0)
    daddiu $t1,$t0,31680
    sd $t1,40($a0)
    dla $a1,lesson_output
    balc h263_predict_mb
    nop
    dla $t0,lesson_output
    lbu $s0,0($t0)
    lh $s1,64($t0)
    lbu $s2,960($t0)
    lh $s3,1024($t0)
lesson_six_blocks:
    nop
    dla $a0,lesson_request
    li $t0,87
    sd $t0,88($a0)
    dla $a1,lesson_output
    balc h263_predict_mb
    nop
    dla $t0,lesson_output
    lbu $s0,0($t0)
    lbu $s1,960($t0)
lesson_coordinator_rejected:
    nop
    li $v0,10
    syscall

.text
h263_predict_mb:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, mb_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, mb_early_return
    nop
    daddiu $t0, $a0, 144
    sltu $t0, $t0, $a0
    bnezc $t0, mb_early_return
    nop
    daddiu $t0, $a1, 1152
    sltu $t0, $t0, $a1
    bnezc $t0, mb_early_return
    nop
    daddiu $sp, $sp, -1440
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
    move $s1, $a1
    daddiu $s0, $sp, 80
    move $t0, $a0
    move $t1, $s0
    li $t2, 18
mb_copy_request:
    ld $t3, 0($t0)
    sd $t3, 0($t1)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, 8
    daddiu $t2, $t2, -1
    bnezc $t2, mb_copy_request
    nop
    ld $s4, 96($s0)
    ld $s5, 104($s0)
    ld $s6, 112($s0)
    ld $s7, 120($s0)
    ld $fp, 128($s0)
    ld $t0, 136($s0)
    bnezc $t0, mb_restore
    nop
    sltiu $t0, $s4, 161
    beqzc $t0, mb_restore
    nop
    sltiu $t0, $s5, 129
    beqzc $t0, mb_restore
    nop
    or $t0, $s4, $s5
    andi $t0, $t0, 15
    bnezc $t0, mb_restore
    nop
    daddiu $t0, $s6, 2
    sltiu $t0, $t0, 5
    beqzc $t0, mb_restore
    nop
    daddiu $t0, $s7, 2
    sltiu $t0, $t0, 5
    beqzc $t0, mb_restore
    nop
    daddu $t0, $s4, $s6
    sltiu $t0, $t0, 161
    beqzc $t0, mb_restore
    nop
    daddu $t0, $s5, $s7
    sltiu $t0, $t0, 129
    beqzc $t0, mb_restore
    nop
    sltiu $t0, $fp, 2
    beqzc $t0, mb_restore
    nop
    move $t0, $zero
mb_validate_plane:
    dsll $t1, $t0, 3
    daddu $t2, $s0, $t1
    ld $t3, 0($t2)
    ld $t4, 48($t2)
    li $t5, 88
    li $t6, 71
    beqzc $t0, mb_luma_plane
    nop
    li $t7, 3
    bnec $t0, $t7, mb_plane_size_ready
    nop
mb_luma_plane:
    li $t5, 176
    li $t6, 143
mb_plane_size_ready:
    sltu $t7, $t4, $t5
    bnezc $t7, mb_restore
    nop
    sltiu $t7, $t4, 4097
    beqzc $t7, mb_restore
    nop
    dmul $t6, $t6, $t4
    daddu $t6, $t6, $t5
    daddu $t6, $t6, $t3
    sltu $t7, $t6, $t3
    bnezc $t7, mb_restore
    nop
    daddiu $t0, $t0, 1
    sltiu $t1, $t0, 6
    bnezc $t1, mb_validate_plane
    nop
    move $s2, $zero
    move $s3, $zero
mb_block:
    sltiu $t0, $s2, 4
    beqzc $t0, mb_chroma_block
    nop
    move $t0, $zero
    andi $t6, $s2, 1
    dsll $t6, $t6, 3
    daddu $t6, $t6, $s4
    dsrl $t7, $s2, 1
    dsll $t7, $t7, 3
    daddu $t7, $t7, $s5
    daddu $t8, $t6, $s6
    daddu $t9, $t7, $s7
    sd $zero, 256($sp)
    sd $zero, 264($sp)
    bc mb_geometry_ready
    nop
mb_chroma_block:
    daddiu $t0, $s2, -3
    dsrl $t6, $s4, 1
    dsrl $t7, $s5, 1
    dsra $t2, $s6, 1
    dsra $t3, $s7, 1
    daddu $t8, $t6, $t2
    daddu $t9, $t7, $t3
    andi $t2, $s6, 1
    andi $t3, $s7, 1
    sd $t2, 256($sp)
    sd $t3, 264($sp)
mb_geometry_ready:
    dsll $t0, $t0, 3
    daddu $t1, $s0, $t0
    ld $t2, 0($t1)
    ld $t3, 24($t1)
    ld $t4, 48($t1)
    ld $t5, 72($t1)
    dmul $t7, $t7, $t4
    daddu $t2, $t2, $t7
    daddu $t2, $t2, $t6
    dmul $t9, $t9, $t5
    daddu $t3, $t3, $t9
    daddu $t3, $t3, $t8
    sd $t2, 224($sp)
    sd $t3, 232($sp)
    sd $t4, 240($sp)
    sd $t5, 248($sp)
    sd $fp, 272($sp)
    sd $zero, 280($sp)
    daddiu $a0, $sp, 224
    li $t0, 192
    dmul $t0, $t0, $s2
    daddiu $a1, $sp, 288
    daddu $a1, $a1, $t0
    balc h263_predict8
    nop
    bnezc $v0, mb_dependency_failure
    nop
    daddu $s3, $s3, $v1
    daddiu $s2, $s2, 1
    sltiu $t0, $s2, 6
    bnezc $t0, mb_block
    nop
mb_publish:
    daddiu $t0, $sp, 288
    move $t1, $s1
    li $t2, 144
mb_publish_word:
    ld $t3, 0($t0)
    sd $t3, 0($t1)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, 8
    daddiu $t2, $t2, -1
    bnezc $t2, mb_publish_word
    nop
    move $v0, $zero
    move $v1, $s3
    bc mb_restore
    nop
mb_dependency_failure:
    li $v0, 2
    move $v1, $zero
mb_restore:
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
    daddiu $sp, $sp, 1440
mb_early_return:
    jrc $ra
    nop

h263_predict8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, pred_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, pred_early_return
    nop
    daddiu $t0, $a0, 64
    sltu $t0, $t0, $a0
    bnezc $t0, pred_early_return
    nop
    daddiu $t0, $a1, 192
    sltu $t0, $t0, $a1
    bnezc $t0, pred_early_return
    nop
    daddiu $sp, $sp, -96
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $s5, 40($a0)
    ld $s6, 48($a0)
    ld $t1, 56($a0)
    move $s7, $a1
    bnezc $t1, pred_return
    nop
    sltiu $t0, $s2, 8
    bnezc $t0, pred_return
    nop
    sltiu $t0, $s2, 4097
    beqzc $t0, pred_return
    nop
    sltiu $t0, $s4, 2
    beqzc $t0, pred_return
    nop
    sltiu $t0, $s5, 2
    beqzc $t0, pred_return
    nop
    sltiu $t0, $s6, 2
    beqzc $t0, pred_return
    nop
    daddiu $t1, $s4, 8
    sltu $t0, $s3, $t1
    bnezc $t0, pred_return
    nop
    sltiu $t0, $s3, 4097
    beqzc $t0, pred_return
    nop
    dsll $t0, $s2, 3
    dsubu $t0, $t0, $s2
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s0
    sltu $t0, $t0, $s0
    bnezc $t0, pred_return
    nop
    daddiu $t0, $s5, 7
    dmul $t0, $t0, $s3
    daddu $t0, $t0, $s4
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s1
    sltu $t0, $t0, $s1
    bnezc $t0, pred_return
    nop
    daddiu $fp, $s7, 64
    li $t8, 8
    bnezc $s6, pred_vector
    nop
pred_scalar_row:
    move $t0, $s0
    move $t1, $s1
    li $t7, 8
pred_scalar_pixel:
    lbu $t2, 0($t1)
    beqzc $s4, pred_scalar_horizontal_ready
    nop
    lbu $t3, 1($t1)
    addu $t2, $t2, $t3
pred_scalar_horizontal_ready:
    beqzc $s5, pred_scalar_vertical_ready
    nop
    daddu $t3, $t1, $s3
    lbu $t4, 0($t3)
    addu $t2, $t2, $t4
    beqzc $s4, pred_scalar_vertical_ready
    nop
    lbu $t4, 1($t3)
    addu $t2, $t2, $t4
pred_scalar_vertical_ready:
    addu $t3, $s4, $s5
    beqzc $t3, pred_scalar_rounded
    nop
    li $t4, 1
    daddiu $t5, $t3, -1
    sllv $t4, $t4, $t5
    addu $t2, $t2, $t4
    srlv $t2, $t2, $t3
pred_scalar_rounded:
    sb $t2, 0($s7)
    lbu $t4, 0($t0)
    subu $t4, $t4, $t2
    sh $t4, 0($fp)
    abs $t5, $t4
    daddu $v1, $v1, $t5
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, 1
    daddiu $s7, $s7, 1
    daddiu $fp, $fp, 2
    daddiu $t7, $t7, -1
    bnezc $t7, pred_scalar_pixel
    nop
    daddu $s0, $s0, $s2
    daddu $s1, $s1, $s3
    daddiu $t8, $t8, -1
    bnezc $t8, pred_scalar_row
    nop
    bc pred_success
    nop
pred_vector:
    ldi.h $w8, 0
    ldi.h $w9, 0
pred_vector_row:
    lbu $t2, 0($s0)
    insert.h $w0[0], $t2
    lbu $t2, 1($s0)
    insert.h $w0[1], $t2
    lbu $t2, 2($s0)
    insert.h $w0[2], $t2
    lbu $t2, 3($s0)
    insert.h $w0[3], $t2
    lbu $t2, 4($s0)
    insert.h $w0[4], $t2
    lbu $t2, 5($s0)
    insert.h $w0[5], $t2
    lbu $t2, 6($s0)
    insert.h $w0[6], $t2
    lbu $t2, 7($s0)
    insert.h $w0[7], $t2
    lbu $t2, 0($s1)
    insert.h $w1[0], $t2
    lbu $t2, 1($s1)
    insert.h $w1[1], $t2
    lbu $t2, 2($s1)
    insert.h $w1[2], $t2
    lbu $t2, 3($s1)
    insert.h $w1[3], $t2
    lbu $t2, 4($s1)
    insert.h $w1[4], $t2
    lbu $t2, 5($s1)
    insert.h $w1[5], $t2
    lbu $t2, 6($s1)
    insert.h $w1[6], $t2
    lbu $t2, 7($s1)
    insert.h $w1[7], $t2
    beqzc $s4, pred_vector_horizontal_ready
    nop
    lbu $t2, 1($s1)
    insert.h $w2[0], $t2
    lbu $t2, 2($s1)
    insert.h $w2[1], $t2
    lbu $t2, 3($s1)
    insert.h $w2[2], $t2
    lbu $t2, 4($s1)
    insert.h $w2[3], $t2
    lbu $t2, 5($s1)
    insert.h $w2[4], $t2
    lbu $t2, 6($s1)
    insert.h $w2[5], $t2
    lbu $t2, 7($s1)
    insert.h $w2[6], $t2
    lbu $t2, 8($s1)
    insert.h $w2[7], $t2
pred_vector_horizontal_ready:
    beqzc $s5, pred_vector_vertical_ready
    nop
    daddu $t0, $s1, $s3
    lbu $t2, 0($t0)
    insert.h $w3[0], $t2
    lbu $t2, 1($t0)
    insert.h $w3[1], $t2
    lbu $t2, 2($t0)
    insert.h $w3[2], $t2
    lbu $t2, 3($t0)
    insert.h $w3[3], $t2
    lbu $t2, 4($t0)
    insert.h $w3[4], $t2
    lbu $t2, 5($t0)
    insert.h $w3[5], $t2
    lbu $t2, 6($t0)
    insert.h $w3[6], $t2
    lbu $t2, 7($t0)
    insert.h $w3[7], $t2
    beqzc $s4, pred_vector_vertical_ready
    nop
    lbu $t2, 1($t0)
    insert.h $w4[0], $t2
    lbu $t2, 2($t0)
    insert.h $w4[1], $t2
    lbu $t2, 3($t0)
    insert.h $w4[2], $t2
    lbu $t2, 4($t0)
    insert.h $w4[3], $t2
    lbu $t2, 5($t0)
    insert.h $w4[4], $t2
    lbu $t2, 6($t0)
    insert.h $w4[5], $t2
    lbu $t2, 7($t0)
    insert.h $w4[6], $t2
    lbu $t2, 8($t0)
    insert.h $w4[7], $t2
pred_vector_vertical_ready:
    beqzc $s4, pred_vector_no_horizontal
    nop
    beqzc $s5, pred_vector_horizontal
    nop
    addv.h $w5, $w1, $w2
    addv.h $w6, $w3, $w4
    addv.h $w5, $w5, $w6
    srlri.h $w5, $w5, 2
    bc pred_vector_predicted
    nop
pred_vector_horizontal:
    aver_u.h $w5, $w1, $w2
    bc pred_vector_predicted
    nop
pred_vector_no_horizontal:
    beqzc $s5, pred_vector_integer
    nop
    aver_u.h $w5, $w1, $w3
    bc pred_vector_predicted
    nop
pred_vector_integer:
    move.v $w5, $w1
pred_vector_predicted:
    subv.h $w6, $w0, $w5
    st.h $w6, 0($fp)
    asub_s.h $w7, $w6, $w9
    addv.h $w8, $w8, $w7
    copy_u.h $t2, $w5[0]
    sb $t2, 0($s7)
    copy_u.h $t2, $w5[1]
    sb $t2, 1($s7)
    copy_u.h $t2, $w5[2]
    sb $t2, 2($s7)
    copy_u.h $t2, $w5[3]
    sb $t2, 3($s7)
    copy_u.h $t2, $w5[4]
    sb $t2, 4($s7)
    copy_u.h $t2, $w5[5]
    sb $t2, 5($s7)
    copy_u.h $t2, $w5[6]
    sb $t2, 6($s7)
    copy_u.h $t2, $w5[7]
    sb $t2, 7($s7)
    daddiu $s7, $s7, 8
    daddiu $fp, $fp, 16
    daddu $s0, $s0, $s2
    daddu $s1, $s1, $s3
    daddiu $t8, $t8, -1
    bnezc $t8, pred_vector_row
    nop
    hadd_u.w $w10, $w8, $w8
    hadd_u.d $w11, $w10, $w10
    copy_s.d $t0, $w11[0]
    copy_s.d $t1, $w11[1]
    daddu $v1, $t0, $t1
pred_success:
    move $v0, $zero
pred_return:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 96
pred_early_return:
    jrc $ra
    nop
