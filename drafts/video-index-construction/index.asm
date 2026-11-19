# Unfinished two-pass complete-file validation and native offset publication.
video_index:
    li $v0, 1
    move $v1, $zero
# BEGIN index_admit
    b index_missing_early
    nop
# END index_admit
index_admitted:
    nop
    daddiu $sp, $sp, -256
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
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
# BEGIN index_context
    b index_missing
    nop
# END index_context
index_context_ready:
    nop
# BEGIN index_geometry
    b index_missing
    nop
# END index_geometry
index_geometry_ready:
    nop
# BEGIN index_schedule
    b index_missing
    nop
# END index_schedule
index_schedule_ready:
    nop
# BEGIN index_timing
    b index_missing
    nop
# END index_timing
index_timing_ready:
    nop
# BEGIN index_capacity
    b index_missing
    nop
# END index_capacity
index_capacity_ready:
    nop
    li $s6, 24
    move $s7, $zero
index_validate_loop:
# BEGIN index_window
    b index_missing
    nop
# END index_window
index_window_ready:
    nop
# BEGIN index_validate
    b index_missing
    nop
# END index_validate
index_record_valid:
    nop
# BEGIN index_advance
    b index_missing
    nop
# END index_advance
index_record_advanced:
    nop
    bne $s7, $s4, index_validate_loop
    nop
# BEGIN index_exact
    b index_missing
    nop
# END index_exact
index_file_valid:
    nop
    li $s6, 24
    move $s7, $zero
index_publish_loop:
# BEGIN index_entry
    b index_missing
    nop
# END index_entry
index_entry_stored:
    nop
# BEGIN index_next
    b index_missing
    nop
# END index_next
index_entry_advanced:
    nop
    bne $s7, $s4, index_publish_loop
    nop
# BEGIN index_publish
    b index_missing
    nop
# END index_publish
index_published:
    nop
    b index_restore
    nop
index_fail:
    li $v0, 1
    move $v1, $zero
    b index_restore
    nop
index_missing:
    li $v0, 4
    move $v1, $zero
index_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    daddiu $sp, $sp, 256
index_return:
    jr $ra
    nop
index_missing_early:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
