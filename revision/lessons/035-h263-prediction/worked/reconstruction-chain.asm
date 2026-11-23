# Distinguish selected levels, reconstructed coefficients, inverse values and pixels.
.data
 .align 4
lesson_residual: .half 2:64
lesson_forward: .byte -91:128
.align 3
lesson_transform_request: .dword 0,16,0,1,0,0
lesson_current: .byte -126:64
lesson_prediction: .byte -128:64
lesson_next: .byte -124:64
.align 4
lesson_first: .byte -91:448
lesson_second: .byte -91:448
.align 3
lesson_request: .dword 0,8,0,8,8,0,1,0
.text
.globl main
main:
    dli $t0,-16
    and $sp,$sp,$t0
    dla $a0,lesson_transform_request
    dla $t0,lesson_residual
    sd $t0,0($a0)
    dla $a1,lesson_forward
    balc h263_transform8
    nop
    dla $t0,lesson_residual
    lh $s0,0($t0)
    dla $t0,lesson_forward
    lh $s1,0($t0)
lesson_forward_dc:
    nop
    dla $a0,lesson_request
    dla $t0,lesson_current
    sd $t0,0($a0)
    dla $t0,lesson_prediction
    sd $t0,16($a0)
    dla $a1,lesson_first
    balc h263_encode_block8
    nop
    dla $t0,lesson_first
    lh $s0,0($t0)
    lh $s1,128($t0)
    lh $s2,256($t0)
    lbu $s3,384($t0)
lesson_first_reconstruction:
    nop
    dla $a0,lesson_request
    dla $t0,lesson_next
    sd $t0,0($a0)
    dla $t0,lesson_first
    daddiu $t0,$t0,384
    sd $t0,16($a0)
    dla $a1,lesson_second
    balc h263_encode_block8
    nop
    dla $t0,lesson_second
    lh $s0,0($t0)
    lh $s1,128($t0)
    lh $s2,256($t0)
    lbu $s3,384($t0)
lesson_second_reconstruction:
    nop
    li $v0,10
    syscall

.data
.align 4
transform_B_h:
    .half 23170,23170,23170,23170,23170,23170,23170,23170
    .half 32138,27246,18205,6393,-6393,-18205,-27246,-32138
    .half 30274,12540,-12540,-30274,-30274,-12540,12540,30274
    .half 27246,-6393,-32138,-18205,18205,32138,6393,-27246
    .half 23170,-23170,-23170,23170,23170,-23170,-23170,23170
    .half 18205,-32138,6393,27246,-27246,-6393,32138,-18205
    .half 12540,-30274,30274,-12540,-12540,30274,-30274,12540
    .half 6393,-18205,27246,-32138,32138,-27246,18205,-6393
.align 4
transform_B_w:
    .word 23170,23170,23170,23170,23170,23170,23170,23170
    .word 32138,27246,18205,6393,-6393,-18205,-27246,-32138
    .word 30274,12540,-12540,-30274,-30274,-12540,12540,30274
    .word 27246,-6393,-32138,-18205,18205,32138,6393,-27246
    .word 23170,-23170,-23170,23170,23170,-23170,-23170,23170
    .word 18205,-32138,6393,27246,-27246,-6393,32138,-18205
    .word 12540,-30274,30274,-12540,-12540,30274,-30274,12540
    .word 6393,-18205,27246,-32138,32138,-27246,18205,-6393
.align 4
transform_BT_h:
    .half 23170,32138,30274,27246,23170,18205,12540,6393
    .half 23170,27246,12540,-6393,-23170,-32138,-30274,-18205
    .half 23170,18205,-12540,-32138,-23170,6393,30274,27246
    .half 23170,6393,-30274,-18205,23170,27246,-12540,-32138
    .half 23170,-6393,-30274,18205,23170,-27246,-12540,32138
    .half 23170,-18205,-12540,32138,-23170,-6393,30274,-27246
    .half 23170,-27246,12540,6393,-23170,32138,-30274,18205
    .half 23170,-32138,30274,-27246,23170,-18205,12540,-6393
.align 4
transform_BT_w:
    .word 23170,32138,30274,27246,23170,18205,12540,6393
    .word 23170,27246,12540,-6393,-23170,-32138,-30274,-18205
    .word 23170,18205,-12540,-32138,-23170,6393,30274,27246
    .word 23170,6393,-30274,-18205,23170,27246,-12540,-32138
    .word 23170,-6393,-30274,18205,23170,-27246,-12540,32138
    .word 23170,-18205,-12540,32138,-23170,-6393,30274,-27246
    .word 23170,-27246,12540,6393,-23170,32138,-30274,18205
    .word 23170,-32138,30274,-27246,23170,-18205,12540,-6393

.text
h263_encode_block8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, block_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, block_early_return
    nop
    daddiu $t0, $a0, 64
    sltu $t0, $t0, $a0
    bnezc $t0, block_early_return
    nop
    daddiu $t0, $a1, 448
    sltu $t0, $t0, $a1
    bnezc $t0, block_early_return
    nop
    daddiu $sp, $sp, -992
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $ra, 72($sp)
    ld $s0, 0($a0)
    ld $s1, 8($a0)
    ld $s2, 16($a0)
    ld $s3, 24($a0)
    ld $s4, 32($a0)
    ld $s5, 40($a0)
    ld $s6, 48($a0)
    ld $t0, 56($a0)
    bnezc $t0, block_restore
    nop
    move $s7, $a1
    sltiu $t0, $s5, 2
    beqzc $t0, block_restore
    nop
    sltiu $t0, $s6, 2
    beqzc $t0, block_restore
    nop
    daddiu $t0, $s4, -1
    sltiu $t0, $t0, 31
    beqzc $t0, block_restore
    nop
    daddiu $t0, $s1, -8
    sltiu $t0, $t0, 4089
    beqzc $t0, block_restore
    nop
    dsll $t0, $s1, 3
    dsubu $t0, $t0, $s1
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s0
    sltu $t0, $t0, $s0
    bnezc $t0, block_restore
    nop
    beqzc $s5, block_check_prediction
    nop
    or $t0, $s2, $s3
    bnezc $t0, block_restore
    nop
    bc block_gather_start
    nop
block_check_prediction:
    daddiu $t0, $s3, -8
    sltiu $t0, $t0, 4089
    beqzc $t0, block_restore
    nop
    dsll $t0, $s3, 3
    dsubu $t0, $t0, $s3
    daddiu $t0, $t0, 8
    daddu $t0, $t0, $s2
    sltu $t0, $t0, $s2
    bnezc $t0, block_restore
    nop
block_gather_start:
    move $fp, $zero
block_gather_loop:
    dsrl $t0, $fp, 3
    andi $t1, $fp, 7
    dmul $t2, $t0, $s1
    daddu $t2, $t2, $s0
    daddu $t2, $t2, $t1
    lbu $t2, 0($t2)
    move $t3, $zero
    bnezc $s5, block_prediction_ready
    nop
    dmul $t3, $t0, $s3
    daddu $t3, $t3, $s2
    daddu $t3, $t3, $t1
    lbu $t3, 0($t3)
block_prediction_ready:
    dsll $t0, $fp, 1
    daddu $t0, $t0, $sp
    sh $t3, 272($t0)
    dsubu $t2, $t2, $t3
    sh $t2, 144($t0)
    daddiu $fp, $fp, 1
    sltiu $t0, $fp, 64
    bnezc $t0, block_gather_loop
    nop
    daddiu $a0, $sp, 96
    daddiu $t0, $sp, 144
    sd $t0, 0($a0)
    li $t0, 16
    sd $t0, 8($a0)
    sd $zero, 16($a0)
    sd $s6, 24($a0)
    sd $zero, 32($a0)
    sd $zero, 40($a0)
    daddiu $a1, $sp, 400
    balc h263_transform8
    nop
    bnezc $v0, block_child_failure
    nop
    daddiu $a0, $sp, 96
    daddiu $t0, $sp, 400
    sd $t0, 0($a0)
    sd $s4, 8($a0)
    sd $s5, 16($a0)
    sd $s6, 24($a0)
    daddiu $a1, $sp, 528
    balc h263_quantize8
    nop
    bnezc $v0, block_child_failure
    nop
    sd $v1, 80($sp)
    daddiu $a0, $sp, 96
    daddiu $t0, $sp, 656
    sd $t0, 0($a0)
    li $t0, 16
    sd $t0, 8($a0)
    li $t0, 1
    sd $t0, 16($a0)
    sd $s6, 24($a0)
    sd $zero, 32($a0)
    sd $zero, 40($a0)
    daddiu $a1, $sp, 784
    balc h263_transform8
    nop
    bnezc $v0, block_child_failure
    nop
    move $fp, $zero
    bnezc $s6, block_vector_reconstruct
    nop
block_scalar_reconstruct:
    dsll $t0, $fp, 1
    daddu $t0, $t0, $sp
    lh $t1, 784($t0)
    lh $t2, 272($t0)
    daddu $t1, $t1, $t2
    bgezc $t1, block_pixel_min_ready
    nop
    move $t1, $zero
block_pixel_min_ready:
    slti $t2, $t1, 256
    bnezc $t2, block_pixel_ready
    nop
    li $t1, 255
block_pixel_ready:
    daddu $t0, $sp, $fp
    sb $t1, 912($t0)
    daddiu $fp, $fp, 1
    sltiu $t0, $fp, 64
    bnezc $t0, block_scalar_reconstruct
    nop
    bc block_publish_start
    nop
block_vector_reconstruct:
    ldi.h $w10, 0
    li $t0, 255
    fill.h $w11, $t0
block_vector_loop:
    dsll $t0, $fp, 1
    daddu $t0, $t0, $sp
    ld.h $w0, 784($t0)
    ld.h $w1, 272($t0)
    addv.h $w2, $w0, $w1
    max_s.h $w2, $w2, $w10
    min_s.h $w2, $w2, $w11
    st.h $w2, 144($sp)
    move $t1, $zero
block_store_vector_pixels:
    dsll $t0, $t1, 1
    daddu $t0, $t0, $sp
    lh $t2, 144($t0)
    daddu $t0, $fp, $t1
    daddu $t0, $t0, $sp
    sb $t2, 912($t0)
    daddiu $t1, $t1, 1
    sltiu $t0, $t1, 8
    bnezc $t0, block_store_vector_pixels
    nop
    daddiu $fp, $fp, 8
    sltiu $t0, $fp, 64
    bnezc $t0, block_vector_loop
    nop
block_publish_start:
    move $fp, $zero
block_publish_loop:
    daddu $t0, $sp, $fp
    ld $t1, 528($t0)
    daddu $t0, $s7, $fp
    sd $t1, 0($t0)
    daddiu $fp, $fp, 8
    sltiu $t0, $fp, 448
    bnezc $t0, block_publish_loop
    nop
    move $v0, $zero
    ld $v1, 80($sp)
    bc block_restore
    nop
block_child_failure:
    li $v0, 2
    move $v1, $zero
block_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    ld $ra, 72($sp)
    daddiu $sp, $sp, 992
block_early_return:
    jrc $ra
    nop

h263_transform8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, transform_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, transform_early_return
    nop
    daddiu $t0, $a0, 48
    sltu $t0, $t0, $a0
    bnezc $t0, transform_early_return
    nop
    daddiu $t0, $a1, 128
    sltu $t0, $t0, $a1
    bnezc $t0, transform_early_return
    nop
    daddiu $sp, $sp, -608
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    sd $a1, 72($sp)
    ld $s7, 0($a0)
    ld $fp, 8($a0)
    ld $s3, 16($a0)
    ld $s2, 24($a0)
    ld $t0, 32($a0)
    ld $t1, 40($a0)
    or $t0, $t0, $t1
    bnezc $t0, transform_restore
    nop
    andi $t0, $s7, 1
    bnezc $t0, transform_restore
    nop
    andi $t0, $fp, 1
    bnezc $t0, transform_restore
    nop
    sltiu $t0, $fp, 16
    bnezc $t0, transform_restore
    nop
    sltiu $t0, $fp, 4097
    beqzc $t0, transform_restore
    nop
    sltiu $t0, $s3, 2
    beqzc $t0, transform_restore
    nop
    sltiu $t0, $s2, 2
    beqzc $t0, transform_restore
    nop
    dsll $t0, $fp, 3
    dsubu $t0, $t0, $fp
    daddiu $t0, $t0, 16
    daddu $t0, $t0, $s7
    sltu $t0, $t0, $s7
    bnezc $t0, transform_restore
    nop
    li $t4, 256
    li $t5, 512
    beqzc $s3, transform_domain_ready
    nop
    li $t4, 2048
    li $t5, 4096
transform_domain_ready:
    daddiu $a3, $sp, 80
    li $t6, 8
transform_admit_row:
    move $t0, $s7
    li $t7, 8
transform_admit_sample:
    lh $t1, 0($t0)
    daddu $t2, $t1, $t4
    sltu $t2, $t2, $t5
    beqzc $t2, transform_bad_sample
    nop
    sh $t1, 0($a3)
    daddiu $t0, $t0, 2
    daddiu $a3, $a3, 2
    daddiu $t7, $t7, -1
    bnezc $t7, transform_admit_sample
    nop
    daddu $s7, $s7, $fp
    daddiu $t6, $t6, -1
    bnezc $t6, transform_admit_row
    nop
    dla $s0, transform_B_h
    dla $s1, transform_B_w
    beqzc $s3, transform_basis_ready
    nop
    dla $s0, transform_BT_h
    dla $s1, transform_BT_w
transform_basis_ready:
    daddiu $s6, $sp, 208
    daddiu $s7, $sp, 80
    move $s4, $zero
transform_first_row:
    dsll $t0, $s4, 4
    daddu $fp, $s7, $t0
    move $s5, $zero
transform_first_column:
    dsll $t0, $s5, 4
    daddu $t0, $s0, $t0
    bnezc $s2, transform_first_vector
    nop
    move $t1, $fp
    move $t2, $zero
    li $t3, 8
transform_first_scalar:
    lh $t4, 0($t0)
    lh $t5, 0($t1)
    dmul $t4, $t4, $t5
    daddu $t2, $t2, $t4
    daddiu $t0, $t0, 2
    daddiu $t1, $t1, 2
    daddiu $t3, $t3, -1
    bnezc $t3, transform_first_scalar
    nop
    bc transform_first_store
    nop
transform_first_vector:
    ld.h $w0, 0($fp)
    ld.h $w1, 0($t0)
    dotp_s.w $w2, $w0, $w1
    hadd_s.d $w3, $w2, $w2
    copy_s.d $t4, $w3[0]
    copy_s.d $t5, $w3[1]
    daddu $t2, $t4, $t5
transform_first_store:
    dsll $t0, $s5, 3
    daddu $t0, $t0, $s4
    dsll $t0, $t0, 2
    daddu $t0, $t0, $s6
    sw $t2, 0($t0)
    daddiu $s5, $s5, 1
    sltiu $t0, $s5, 8
    bnezc $t0, transform_first_column
    nop
    daddiu $s4, $s4, 1
    sltiu $t0, $s4, 8
    bnezc $t0, transform_first_row
    nop
    daddiu $s7, $sp, 464
    move $s4, $zero
transform_second_row:
    dsll $t0, $s4, 5
    daddu $fp, $s1, $t0
    move $s5, $zero
transform_second_column:
    dsll $t0, $s5, 5
    daddu $t0, $s6, $t0
    bnezc $s2, transform_second_vector
    nop
    move $t1, $fp
    move $t2, $zero
    li $t3, 8
transform_second_scalar:
    lw $t4, 0($t0)
    lw $t5, 0($t1)
    dmul $t4, $t4, $t5
    daddu $t2, $t2, $t4
    daddiu $t0, $t0, 4
    daddiu $t1, $t1, 4
    daddiu $t3, $t3, -1
    bnezc $t3, transform_second_scalar
    nop
    bc transform_round
    nop
transform_second_vector:
    ld.w $w0, 0($t0)
    ld.w $w1, 0($fp)
    dotp_s.d $w2, $w0, $w1
    ld.w $w3, 16($t0)
    ld.w $w4, 16($fp)
    dotp_s.d $w5, $w3, $w4
    addv.d $w6, $w2, $w5
    copy_s.d $t4, $w6[0]
    copy_s.d $t5, $w6[1]
    daddu $t2, $t4, $t5
transform_round:
    dli $t0, 0x80000000
    daddu $t2, $t2, $t0
    dsra32 $t2, $t2, 0
    li $t3, -2048
    li $t4, 2047
    beqzc $s3, transform_clip
    nop
    li $t3, -256
    li $t4, 255
transform_clip:
    slt $t5, $t2, $t3
    beqzc $t5, transform_clip_high
    nop
    move $t2, $t3
transform_clip_high:
    slt $t5, $t4, $t2
    beqzc $t5, transform_output_sample
    nop
    move $t2, $t4
transform_output_sample:
    dsll $t0, $s4, 3
    daddu $t0, $t0, $s5
    dsll $t0, $t0, 1
    daddu $t0, $s7, $t0
    sh $t2, 0($t0)
    daddiu $s5, $s5, 1
    sltiu $t0, $s5, 8
    bnezc $t0, transform_second_column
    nop
    daddiu $s4, $s4, 1
    sltiu $t0, $s4, 8
    bnezc $t0, transform_second_row
    nop
    move $t0, $s7
    ld $t1, 72($sp)
    li $t2, 16
transform_publish:
    ld $t3, 0($t0)
    sd $t3, 0($t1)
    daddiu $t0, $t0, 8
    daddiu $t1, $t1, 8
    daddiu $t2, $t2, -1
    bnezc $t2, transform_publish
    nop
    move $v0, $zero
    li $v1, 64
    bc transform_restore
    nop
transform_bad_sample:
    li $v0, 2
    move $v1, $zero
transform_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 608
transform_early_return:
    jrc $ra
    nop

h263_quantize8:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, quant_early_return
    nop
    andi $t0, $a1, 15
    bnezc $t0, quant_early_return
    nop
    daddiu $t0, $a0, 32
    sltu $t0, $t0, $a0
    bnezc $t0, quant_early_return
    nop
    daddiu $t0, $a1, 256
    sltu $t0, $t0, $a1
    bnezc $t0, quant_early_return
    nop
    ld $t0, 0($a0)
    andi $t1, $t0, 1
    bnezc $t1, quant_early_return
    nop
    daddiu $t1, $t0, 128
    sltu $t1, $t1, $t0
    bnezc $t1, quant_early_return
    nop
    ld $t1, 8($a0)
    daddiu $t2, $t1, -1
    sltiu $t2, $t2, 31
    beqzc $t2, quant_early_return
    nop
    ld $t2, 16($a0)
    sltiu $t3, $t2, 2
    beqzc $t3, quant_early_return
    nop
    ld $t3, 24($a0)
    sltiu $t4, $t3, 2
    beqzc $t4, quant_early_return
    nop
    daddiu $sp, $sp, -352
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    move $s0, $t0
    move $s1, $t1
    move $s2, $t2
    move $s3, $t3
    move $s4, $a1
    move $s5, $zero
    move $s6, $zero
    dsll $s7, $s1, 1
quant_level_loop:
    dsll $t0, $s5, 1
    daddu $t1, $s0, $t0
    lh $t2, 0($t1)
    daddiu $t3, $t2, 2048
    sltiu $t3, $t3, 4096
    beqzc $t3, quant_bad_domain
    nop
    bnezc $s5, quant_ordinary_level
    nop
    bnezc $s2, quant_dc_level
    nop
quant_ordinary_level:
    move $t3, $t2
    bgezc $t3, quant_abs_ready
    nop
    dsubu $t3, $zero, $t3
quant_abs_ready:
    ddivu $t3, $t3, $s7
    sltiu $t4, $t3, 128
    bnezc $t4, quant_magnitude_ready
    nop
    li $t3, 127
quant_magnitude_ready:
    bgezc $t2, quant_sign_ready
    nop
    dsubu $t3, $zero, $t3
quant_sign_ready:
    beqzc $t3, quant_store_level
    nop
    daddiu $s6, $s6, 1
    bc quant_store_level
    nop
quant_dc_level:
    daddiu $t3, $t2, 4
    dsra $t3, $t3, 3
    bgtzc $t3, quant_dc_min_ready
    nop
    li $t3, 1
quant_dc_min_ready:
    slti $t4, $t3, 255
    bnezc $t4, quant_dc_max_ready
    nop
    li $t3, 254
quant_dc_max_ready:
    li $t4, 128
    bnec $t3, $t4, quant_store_level
    nop
    li $t3, 255
quant_store_level:
    daddiu $t1, $sp, 80
    daddu $t1, $t1, $t0
    sh $t3, 0($t1)
    daddiu $s5, $s5, 1
    sltiu $t0, $s5, 64
    bnezc $t0, quant_level_loop
    nop
    beqzc $s3, quant_scalar_start
    nop
    fill.h $w10, $s1
    andi $t0, $s1, 1
    xori $t0, $t0, 1
    fill.h $w11, $t0
    li $t0, -2048
    fill.h $w12, $t0
    ldi.h $w13, 0
    li $t0, 2047
    fill.h $w14, $t0
    move $s5, $zero
quant_vector_loop:
    daddu $t0, $sp, $s5
    ld.h $w0, 80($t0)
    srai.h $w1, $w0, 15
    xor.v $w2, $w0, $w1
    subv.h $w2, $w2, $w1
    slli.h $w2, $w2, 1
    addvi.h $w2, $w2, 1
    mulv.h $w2, $w2, $w10
    subv.h $w2, $w2, $w11
    xor.v $w2, $w2, $w1
    subv.h $w2, $w2, $w1
    ceq.h $w3, $w0, $w13
    nor.v $w3, $w3, $w3
    and.v $w2, $w2, $w3
    max_s.h $w2, $w2, $w12
    min_s.h $w2, $w2, $w14
    st.h $w2, 208($t0)
    daddiu $s5, $s5, 16
    sltiu $t0, $s5, 128
    bnezc $t0, quant_vector_loop
    nop
    bc quant_dc_reconstruct
    nop
quant_scalar_start:
    move $s5, $zero
quant_scalar_loop:
    daddu $t0, $sp, $s5
    lh $t1, 80($t0)
    move $t2, $zero
    beqzc $t1, quant_store_rec
    nop
    move $t2, $t1
    bgezc $t2, quant_rec_abs_ready
    nop
    dsubu $t2, $zero, $t2
quant_rec_abs_ready:
    dsll $t2, $t2, 1
    daddiu $t2, $t2, 1
    dmul $t2, $t2, $s1
    andi $t3, $s1, 1
    xori $t3, $t3, 1
    dsubu $t2, $t2, $t3
    bgezc $t1, quant_rec_sign_ready
    nop
    dsubu $t2, $zero, $t2
quant_rec_sign_ready:
    li $t3, -2048
    slt $t4, $t2, $t3
    beqzc $t4, quant_rec_min_ready
    nop
    move $t2, $t3
quant_rec_min_ready:
    li $t3, 2047
    slt $t4, $t3, $t2
    beqzc $t4, quant_store_rec
    nop
    move $t2, $t3
quant_store_rec:
    sh $t2, 208($t0)
    daddiu $s5, $s5, 2
    sltiu $t0, $s5, 128
    bnezc $t0, quant_scalar_loop
    nop
quant_dc_reconstruct:
    beqzc $s2, quant_publish
    nop
    lh $t0, 80($sp)
    li $t1, 255
    bnec $t0, $t1, quant_dc_decode_ready
    nop
    li $t0, 128
quant_dc_decode_ready:
    dsll $t0, $t0, 3
    sh $t0, 208($sp)
quant_publish:
    move $s5, $zero
quant_publish_loop:
    daddu $t0, $sp, $s5
    ld $t1, 80($t0)
    daddu $t0, $s4, $s5
    sd $t1, 0($t0)
    daddiu $s5, $s5, 8
    sltiu $t0, $s5, 256
    bnezc $t0, quant_publish_loop
    nop
    move $v0, $zero
    move $v1, $s6
    bc quant_restore
    nop
quant_bad_domain:
    li $v0, 2
    move $v1, $zero
quant_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    daddiu $sp, $sp, 352
quant_early_return:
    jrc $ra
    nop
