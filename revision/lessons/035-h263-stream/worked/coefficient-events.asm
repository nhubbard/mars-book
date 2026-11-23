# Bounded worked experiment for Chapter 27. The independent reader remains unfinished.
.data
 .align 1
lesson_levels: .half 255,1,0,0,0,0,0,0,-1
    .half 0:55
lesson_compact: .byte -91:64
lesson_escape: .byte -91:64
.align 3
lesson_compact_context: .dword 0,32,3
lesson_escape_context: .dword 0,32,3
lesson_request: .dword 0,0,1,0
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

.data
.align 3
coeff_scan:
    .dword 0,1,8,16,9,2,3,10,17,24,32,25,18,11,4,5,12,19,26,33,40,48,41,34,27,20,13,6,7,14,21,28,35,42,49,56,57,50,43,36,29,22,15,23,30,37,44,51,58,59,52,45,38,31,39,46,53,60,61,54,47,55,62,63
.align 3
coeff_codes:
    .dword 0,0,1,4,3
    .dword 0,0,2,30,5
    .dword 0,0,3,42,7
    .dword 0,0,4,46,8
    .dword 0,0,5,62,9
    .dword 0,0,6,74,10
    .dword 0,0,7,72,10
    .dword 0,0,8,66,11
    .dword 0,0,9,64,11
    .dword 0,0,10,14,12
    .dword 0,0,11,12,12
    .dword 0,0,12,64,12
    .dword 0,1,1,12,4
    .dword 0,1,2,40,7
    .dword 0,1,3,60,9
    .dword 0,1,4,30,11
    .dword 0,1,5,66,12
    .dword 0,1,6,160,13
    .dword 0,2,1,28,5
    .dword 0,2,2,58,9
    .dword 0,2,3,28,11
    .dword 0,2,4,162,13
    .dword 0,3,1,26,6
    .dword 0,3,2,70,10
    .dword 0,3,3,26,11
    .dword 0,4,1,24,6
    .dword 0,4,2,68,10
    .dword 0,4,3,164,13
    .dword 0,5,1,22,6
    .dword 0,5,2,24,11
    .dword 0,5,3,166,13
    .dword 0,6,1,38,7
    .dword 0,6,2,22,11
    .dword 0,6,3,168,13
    .dword 0,7,1,36,7
    .dword 0,7,2,20,11
    .dword 0,8,1,34,7
    .dword 0,8,2,18,11
    .dword 0,9,1,32,7
    .dword 0,9,2,16,11
    .dword 0,10,1,44,8
    .dword 0,10,2,170,13
    .dword 0,11,1,42,8
    .dword 0,12,1,40,8
    .dword 0,13,1,56,9
    .dword 0,14,1,54,9
    .dword 0,15,1,66,10
    .dword 0,16,1,64,10
    .dword 0,17,1,62,10
    .dword 0,18,1,60,10
    .dword 0,19,1,58,10
    .dword 0,20,1,56,10
    .dword 0,21,1,54,10
    .dword 0,22,1,52,10
    .dword 0,23,1,68,12
    .dword 0,24,1,70,12
    .dword 0,25,1,172,13
    .dword 0,26,1,174,13
    .dword 1,0,1,14,5
    .dword 1,0,2,50,10
    .dword 1,0,3,10,12
    .dword 1,1,1,30,7
    .dword 1,1,2,8,12
    .dword 1,2,1,28,7
    .dword 1,3,1,26,7
    .dword 1,4,1,24,7
    .dword 1,5,1,38,8
    .dword 1,6,1,36,8
    .dword 1,7,1,34,8
    .dword 1,8,1,32,8
    .dword 1,9,1,52,9
    .dword 1,10,1,50,9
    .dword 1,11,1,48,9
    .dword 1,12,1,46,9
    .dword 1,13,1,44,9
    .dword 1,14,1,42,9
    .dword 1,15,1,40,9
    .dword 1,16,1,38,9
    .dword 1,17,1,48,10
    .dword 1,18,1,46,10
    .dword 1,19,1,44,10
    .dword 1,20,1,42,10
    .dword 1,21,1,40,10
    .dword 1,22,1,38,10
    .dword 1,23,1,36,10
    .dword 1,24,1,34,10
    .dword 1,25,1,14,11
    .dword 1,26,1,12,11
    .dword 1,27,1,10,11
    .dword 1,28,1,8,11
    .dword 1,29,1,72,12
    .dword 1,30,1,74,12
    .dword 1,31,1,76,12
    .dword 1,32,1,78,12
    .dword 1,33,1,176,13
    .dword 1,34,1,178,13
    .dword 1,35,1,180,13
    .dword 1,36,1,182,13
    .dword 1,37,1,184,13
    .dword 1,38,1,186,13
    .dword 1,39,1,188,13
    .dword 1,40,1,190,13


.text
.globl main
main:
    andi $t0,$sp,15
    dsubu $sp,$sp,$t0
    dla $a0,lesson_request
    dla $t0,lesson_levels
    sd $t0,0($a0)
    dla $t0,lesson_compact_context
    sd $t0,8($a0)
    dla $t1,lesson_compact
    daddiu $t1,$t1,5
    sd $t1,0($t0)
    balc h263_emit_coefficients
    nop
    move $s0,$v1
    dla $t0,lesson_compact_context
    ld $s1,16($t0)
lesson_coeff_compact:
    nop
    dla $t0,lesson_escape_context
    dla $t1,lesson_escape
    daddiu $t1,$t1,5
    sd $t1,0($t0)
    dla $a0,lesson_escape_context
    li $a1,255
    li $a2,8
    balc h263_write_bits
    nop
    dla $a0,lesson_escape_context
    li $a1,98305
    li $a2,22
    balc h263_write_bits
    nop
    dla $a0,lesson_escape_context
    li $a1,114943
    li $a2,22
    balc h263_write_bits
    nop
    dla $t0,lesson_escape_context
    ld $s1,16($t0)
    daddiu $s0,$s1,-3
lesson_coeff_escape:
    nop
    dla $t0,lesson_levels
    li $t1,128
    sh $t1,126($t0)
    dla $a0,lesson_request
    balc h263_emit_coefficients
    nop
    dla $t0,lesson_compact_context
    ld $s1,16($t0)
lesson_coeff_rejected:
    nop
    li $v0,10
    syscall

h263_emit_coefficients:
    li $v0, 1
    move $v1, $zero
    andi $t0, $a0, 7
    bnezc $t0, coeff_early_return
    nop
    daddiu $t0, $a0, 32
    sltu $t0, $t0, $a0
    bnezc $t0, coeff_early_return
    nop
    daddiu $sp, $sp, -1440
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
    ld $t0, 24($a0)
    bnezc $t0, coeff_restore
    nop
    sltiu $t0, $s2, 2
    beqzc $t0, coeff_restore
    nop
    andi $t0, $s0, 1
    bnezc $t0, coeff_restore
    nop
    daddiu $t0, $s0, 128
    sltu $t0, $t0, $s0
    bnezc $t0, coeff_restore
    nop
    andi $t0, $s1, 7
    bnezc $t0, coeff_restore
    nop
    daddiu $t0, $s1, 24
    sltu $t0, $t0, $s1
    bnezc $t0, coeff_restore
    nop
    ld $s4, 0($s1)
    ld $s5, 8($s1)
    ld $s3, 16($s1)
    dli $t0, 1048576
    sltu $t0, $t0, $s5
    bnezc $t0, coeff_restore
    nop
    daddu $t0, $s4, $s5
    sltu $t0, $t0, $s4
    bnezc $t0, coeff_restore
    nop
    dsll $t0, $s5, 3
    sltu $t0, $t0, $s3
    bnezc $t0, coeff_restore
    nop
    move $s6, $zero
coeff_validate_loop:
    dsll $t0, $s6, 1
    daddu $t0, $t0, $s0
    lh $t1, 0($t0)
    bnezc $s6, coeff_validate_ordinary
    nop
    beqzc $s2, coeff_validate_ordinary
    nop
    daddiu $t0, $t1, -1
    sltiu $t0, $t0, 255
    beqzc $t0, coeff_restore
    nop
    li $t0, 128
    beqc $t1, $t0, coeff_restore
    nop
    bc coeff_validate_next
    nop
coeff_validate_ordinary:
    daddiu $t0, $t1, 127
    sltiu $t0, $t0, 255
    beqzc $t0, coeff_restore
    nop
coeff_validate_next:
    daddiu $s6, $s6, 1
    sltiu $t0, $s6, 64
    bnezc $t0, coeff_validate_loop
    nop
    li $s7, 63
coeff_find_last:
    slt $t0, $s7, $s2
    bnezc $t0, coeff_last_ready
    nop
    dsll $t0, $s7, 3
    dla $t1, coeff_scan
    daddu $t0, $t0, $t1
    ld $t0, 0($t0)
    dsll $t0, $t0, 1
    daddu $t0, $t0, $s0
    lh $t0, 0($t0)
    bnezc $t0, coeff_last_ready
    nop
    daddiu $s7, $s7, -1
    bc coeff_find_last
    nop
coeff_last_ready:
    move $fp, $zero
    sd $zero, 1336($sp)
    sd $zero, 1352($sp)
    beqzc $s2, coeff_plan_start
    nop
    lhu $t0, 0($s0)
    sd $t0, 112($sp)
    li $t0, 8
    sd $t0, 120($sp)
    sd $t0, 1336($sp)
    li $fp, 1
coeff_plan_start:
    move $s6, $s2
coeff_plan_loop:
    slt $t0, $s7, $s6
    bnezc $t0, coeff_plan_ready
    nop
    dsll $t0, $s6, 3
    dla $t1, coeff_scan
    daddu $t0, $t0, $t1
    ld $t0, 0($t0)
    dsll $t0, $t0, 1
    daddu $t0, $t0, $s0
    lh $t2, 0($t0)
    bnezc $t2, coeff_nonzero
    nop
    ld $t0, 1352($sp)
    daddiu $t0, $t0, 1
    sd $t0, 1352($sp)
    bc coeff_plan_next
    nop
coeff_nonzero:
    move $t5, $t2
    bgezc $t5, coeff_magnitude_ready
    nop
    dsubu $t5, $zero, $t5
coeff_magnitude_ready:
    xor $t6, $s6, $s7
    sltiu $t6, $t6, 1
    ld $t7, 1352($sp)
    slt $t8, $t2, $zero
    dli $t0, 98304
    dsll $t1, $t6, 14
    or $t0, $t0, $t1
    dsll $t1, $t7, 8
    or $t0, $t0, $t1
    andi $t1, $t2, 255
    or $t0, $t0, $t1
    li $t1, 22
    dla $t3, coeff_codes
    li $t4, 102
coeff_lookup_loop:
    ld $t9, 0($t3)
    bnec $t9, $t6, coeff_lookup_next
    nop
    ld $t9, 8($t3)
    bnec $t9, $t7, coeff_lookup_next
    nop
    ld $t9, 16($t3)
    bnec $t9, $t5, coeff_lookup_next
    nop
    ld $t0, 24($t3)
    or $t0, $t0, $t8
    ld $t1, 32($t3)
    bc coeff_plan_store
    nop
coeff_lookup_next:
    daddiu $t3, $t3, 40
    daddiu $t4, $t4, -1
    bnezc $t4, coeff_lookup_loop
    nop
coeff_plan_store:
    dsll $t2, $fp, 4
    daddu $t2, $t2, $sp
    sd $t0, 112($t2)
    sd $t1, 120($t2)
    ld $t0, 1336($sp)
    daddu $t0, $t0, $t1
    sd $t0, 1336($sp)
    daddiu $fp, $fp, 1
    sd $zero, 1352($sp)
coeff_plan_next:
    daddiu $s6, $s6, 1
    bc coeff_plan_loop
    nop
coeff_plan_ready:
    sd $fp, 1328($sp)
    ld $t0, 1336($sp)
    dsll $t1, $s5, 3
    dsubu $t1, $t1, $s3
    sltu $t1, $t1, $t0
    bnezc $t1, coeff_no_space
    nop
    beqzc $t0, coeff_empty
    nop
    dsrl $t1, $s3, 3
    daddu $t1, $t1, $s4
    sd $t1, 1360($sp)
    andi $t1, $s3, 7
    daddu $t0, $t0, $t1
    daddiu $t0, $t0, 7
    dsrl $t0, $t0, 3
    sd $t0, 1344($sp)
    move $s6, $zero
coeff_stage_bytes:
    ld $t0, 1360($sp)
    daddu $t0, $t0, $s6
    lbu $t1, 0($t0)
    daddu $t0, $sp, $s6
    sb $t1, 1136($t0)
    daddiu $s6, $s6, 1
    ld $t0, 1344($sp)
    sltu $t0, $s6, $t0
    bnezc $t0, coeff_stage_bytes
    nop
    daddiu $t0, $sp, 1136
    sd $t0, 80($sp)
    li $t0, 192
    sd $t0, 88($sp)
    andi $t0, $s3, 7
    sd $t0, 96($sp)
    move $s6, $zero
coeff_emit_plan:
    daddiu $a0, $sp, 80
    dsll $t0, $s6, 4
    daddu $t0, $t0, $sp
    ld $a1, 112($t0)
    ld $a2, 120($t0)
    balc h263_write_bits
    nop
    bnezc $v0, coeff_child_failure
    nop
    daddiu $s6, $s6, 1
    ld $t0, 1328($sp)
    sltu $t0, $s6, $t0
    bnezc $t0, coeff_emit_plan
    nop
    move $s6, $zero
coeff_publish:
    daddu $t0, $sp, $s6
    lbu $t1, 1136($t0)
    ld $t0, 1360($sp)
    daddu $t0, $t0, $s6
    sb $t1, 0($t0)
    daddiu $s6, $s6, 1
    ld $t0, 1344($sp)
    sltu $t0, $s6, $t0
    bnezc $t0, coeff_publish
    nop
    ld $v1, 1336($sp)
    daddu $t0, $s3, $v1
    sd $t0, 16($s1)
    move $v0, $zero
    bc coeff_restore
    nop
coeff_empty:
    move $v0, $zero
    move $v1, $zero
    bc coeff_restore
    nop
coeff_no_space:
    li $v0, 2
    move $v1, $zero
    bc coeff_restore
    nop
coeff_child_failure:
    li $v0, 3
    move $v1, $zero
coeff_restore:
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
    daddiu $sp, $sp, 1440
coeff_early_return:
    jrc $ra
    nop

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
