# One four-neighbor rounding differs from two rounded pair averages.
.data
lesson_current: .byte -128:64
lesson_reference: .byte -128,-128,-128,-128,-128,-128,-128,-128,-128,-128,96,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128,-128
lesson_zero_current: .byte 0:64
lesson_rounding_reference: .byte 0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
.align 4
lesson_output: .byte -91:192
.align 3
lesson_request: .dword 0,0,8,9,1,1,1,0
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
    dla $a1,lesson_output
    balc h263_predict8
    nop
    dla $t0,lesson_output
    lbu $s0,0($t0)
    lh $s1,64($t0)
    lbu $s2,18($t0)
lesson_prediction_values:
    nop
    dla $a0,lesson_request
    dla $t0,lesson_zero_current
    sd $t0,0($a0)
    dla $t0,lesson_rounding_reference
    sd $t0,8($a0)
    dla $a1,lesson_output
    balc h263_predict8
    nop
    dla $t0,lesson_output
    lbu $s0,0($t0)
    lh $s1,64($t0)
lesson_single_rounding:
    nop
    dla $a0,lesson_request
    li $t0,2
    sd $t0,32($a0)
    dla $a1,lesson_output
    balc h263_predict8
    nop
lesson_prediction_rejected:
    nop
    li $v0,10
    syscall

.text
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
