# Seek within an immutable, already indexed file; publish only a complete frame.
video_seek:
    li $v0, 1
    move $v1, $zero
# BEGIN seek_admit
    b seek_missing_early
    nop
# END seek_admit
seek_admitted:
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
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
# BEGIN seek_context
    b seek_missing
    nop
# END seek_context
seek_context_ready:
    nop
# BEGIN seek_state
    b seek_missing
    nop
# END seek_state
seek_state_ready:
    nop
seek_scan:
# BEGIN seek_offset
    b seek_missing
    nop
# END seek_offset
seek_offset_ready:
    nop
# BEGIN seek_full
    b seek_missing
    nop
# END seek_full
seek_full_ready:
    nop
# BEGIN seek_record_context
    b seek_missing
    nop
# END seek_record_context
seek_replay_ready:
    nop
seek_replay:
# BEGIN seek_window
    b seek_missing
    nop
# END seek_window
seek_window_ready:
    nop
# BEGIN seek_apply
    b seek_missing
    nop
# END seek_apply
seek_applied:
    nop
# BEGIN seek_advance
    b seek_missing
    nop
# END seek_advance
seek_reconstructed:
    nop
# BEGIN seek_copy
    b seek_missing
    nop
# END seek_copy
seek_frame_copied:
    nop
# BEGIN seek_publish
    b seek_missing
    nop
# END seek_publish
seek_published:
    nop
    b seek_restore
    nop
seek_failed:
    li $v0, 1
    move $v1, $zero
    b seek_restore
    nop
seek_missing:
    li $v0, 4
    move $v1, $zero
seek_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    daddiu $sp, $sp, 192
seek_return:
    jr $ra
    nop
seek_missing_early:
    li $v0, 4
    jr $ra
    nop
