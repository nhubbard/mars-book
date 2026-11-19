video_now:
# BEGIN vn_read
    b vn_missing
    nop
# END vn_read
vn_read_ready:
    nop
# BEGIN vn_join
    b vn_missing
    nop
# END vn_join
vn_join_ready:
    nop
    jr $ra
    nop
vn_missing:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop

video_play:
    li $v0, 1
    move $v1, $zero
# BEGIN vp_admit
    b vp_missing_early
    nop
# END vp_admit
vp_admitted:
    nop
    daddiu $sp, $sp, -352
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    sd $s7, 64($sp)
    sd $zero, 72($sp)
    sd $zero, 80($sp)
    sd $zero, 88($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
# BEGIN vp_context
    b vp_missing
    nop
# END vp_context
vp_context_ready:
    nop
# BEGIN vp_index_context
    b vp_missing
    nop
# END vp_index_context
vp_index_context_ready:
    nop
# BEGIN vp_index
    b vp_missing
    nop
# END vp_index
vp_indexed:
    nop
# BEGIN vp_range
    b vp_missing
    nop
# END vp_range
vp_range_ready:
    nop
# BEGIN vp_seek_context
    b vp_missing
    nop
# END vp_seek_context
vp_seek_context_ready:
    nop
# BEGIN vp_record_context
    b vp_missing
    nop
# END vp_record_context
vp_record_context_ready:
    nop
# BEGIN vp_anchor
    b vp_missing
    nop
# END vp_anchor
vp_anchored:
    nop
vp_loop:
# BEGIN vp_deadline
    b vp_missing
    nop
# END vp_deadline
vp_deadline_ready:
    nop
    ld $t0, 72($sp)
    bnez $t0, vp_next_record
    nop
# BEGIN vp_first
    b vp_missing
    nop
# END vp_first
vp_first_ready:
    nop
    b vp_frame_ready
    nop
vp_next_record:
# BEGIN vp_window
    b vp_missing
    nop
# END vp_window
vp_window_ready:
    nop
# BEGIN vp_record
    b vp_missing
    nop
# END vp_record
vp_record_ready:
    nop
vp_frame_ready:
# BEGIN vp_presented
    b vp_missing
    nop
# END vp_presented
vp_frame_presented:
    nop
vp_wait:
# BEGIN vp_sample
    b vp_missing
    nop
# END vp_sample
vp_sampled:
    nop
# BEGIN vp_compare
    b vp_missing
    nop
# END vp_compare
vp_clock_compared:
    nop
    bnez $t2, vp_sleep_begin
    nop
    b vp_late_begin
    nop
vp_sleep_begin:
# BEGIN vp_sleep
    b vp_missing
    nop
# END vp_sleep
vp_sleep_requested:
    nop
    b vp_wait
    nop
vp_late_begin:
# BEGIN vp_late
    b vp_missing
    nop
# END vp_late
vp_interval_complete:
    nop
# BEGIN vp_advance
    b vp_missing
    nop
# END vp_advance
vp_advanced:
    nop
    bne $s4, $s5, vp_loop
    nop
# BEGIN vp_publish
    b vp_missing
    nop
# END vp_publish
vp_published:
    nop
    b vp_restore
    nop
vp_nested_failed:
    li $t0, 4
    beq $v0, $t0, vp_missing
    nop
vp_failed:
    li $v0, 1
    ld $v1, 72($sp)
    b vp_restore
    nop
vp_missing:
    li $v0, 4
    ld $v1, 72($sp)
vp_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    daddiu $sp, $sp, 352
vp_return:
    jr $ra
    nop
vp_missing_early:
    li $v0, 4
    jr $ra
    nop
