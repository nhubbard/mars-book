# Choose full or greedy delta, committing only a complete record and image.
video_encode_record:
    li $v0, 1
    move $v1, $zero
# BEGIN dr_admit
    b dr_missing_early
    nop
# END dr_admit
dr_admitted:
    nop
    daddiu $sp, $sp, -160
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    sd $s7, 64($sp)
    move $s0, $a0
    move $s1, $a2
    move $s2, $a3
# BEGIN dr_context
    b dr_missing
    nop
# END dr_context
dr_context_ready:
    nop
# BEGIN dr_schedule
    b dr_missing
    nop
# END dr_schedule
dr_schedule_ready:
    nop
# BEGIN dr_validate
    b dr_missing
    nop
# END dr_validate
dr_pixels_valid:
    nop
    ld $t0, 80($sp)
    beqz $t0, dr_full
    nop
# BEGIN dr_xor
    b dr_missing
    nop
# END dr_xor
dr_xor_ready:
    nop
    move $t0, $zero
    move $s7, $zero
dr_measure:
# BEGIN dr_measure_skip
    b dr_missing
    nop
# END dr_measure_skip
dr_skip_measured:
    nop
# BEGIN dr_measure_change
    b dr_missing
    nop
# END dr_measure_change
dr_change_measured:
    nop
    li $t1, 4096
    bne $t0, $t1, dr_measure
    nop
# BEGIN dr_choose
    b dr_missing
    nop
# END dr_choose
dr_delta_admitted:
    nop
# BEGIN dr_header
    b dr_missing
    nop
# END dr_header
dr_header_ready:
    nop
    move $t0, $zero
    daddiu $t1, $s1, 16
dr_emit:
# BEGIN dr_emit_skip
    b dr_missing
    nop
# END dr_emit_skip
dr_skip_written:
    nop
# BEGIN dr_emit_change
    b dr_missing
    nop
# END dr_emit_change
dr_change_written:
    nop
    li $t2, 4096
    bne $t0, $t2, dr_emit
    nop
# BEGIN dr_crc
    b dr_missing
    nop
# END dr_crc
dr_crc_ready:
    nop
# BEGIN dr_previous
    b dr_missing
    nop
# END dr_previous
dr_previous_ready:
    nop
# BEGIN dr_publish
    b dr_missing
    nop
# END dr_publish
dr_published:
    nop
    b dr_restore
    nop
dr_full:
# BEGIN dr_full_call
    b dr_missing
    nop
# END dr_full_call
dr_full_return:
    nop
    b dr_restore
    nop
dr_failed:
    li $v0, 1
    move $v1, $zero
    b dr_restore
    nop
dr_missing:
    li $v0, 4
    move $v1, $zero
dr_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    daddiu $sp, $sp, 160
dr_return:
    jr $ra
    nop
dr_missing_early:
    li $v0, 4
    jr $ra
    nop
