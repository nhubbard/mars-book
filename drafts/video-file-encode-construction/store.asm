# Transfer admitted nonempty bytes; container validation belongs to the caller.
video_store:
    li $v0, 1
    move $v1, $zero
# BEGIN vs_admit
    b vs_missing_early
    nop
# END vs_admit
vs_admitted:
    nop
    daddiu $sp, $sp, -96
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    li $s3, -1
# BEGIN vs_state
    b vs_missing
    nop
# END vs_state
vs_state_ready:
    nop
# BEGIN vs_open
    b vs_missing
    nop
# END vs_open
vs_open_ready:
    nop
vs_loop:
    beq $s2, $s1, vs_success_begin
    nop
# BEGIN vs_request
    b vs_missing
    nop
# END vs_request
vs_requested:
    nop
# BEGIN vs_result
    b vs_missing
    nop
# END vs_result
vs_progress_ready:
    nop
    b vs_loop
    nop
vs_success_begin:
# BEGIN vs_success
    b vs_missing
    nop
# END vs_success
vs_success_ready:
    nop
vs_close_begin:
# BEGIN vs_close
    b vs_missing
    nop
# END vs_close
vs_closed:
    nop
# BEGIN vs_publish
    b vs_missing
    nop
# END vs_publish
vs_published:
    nop
    b vs_restore
    nop
vs_open_failed:
    li $v0, 1
    move $v1, $zero
    b vs_restore
    nop
vs_missing:
    bltz $s3, vs_missing_status
    nop
    move $a0, $s3
    li $v0, 16
    syscall
vs_missing_status:
    li $v0, 4
    move $v1, $zero
vs_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    daddiu $sp, $sp, 96
vs_return:
    jr $ra
    nop
vs_missing_early:
    li $v0, 4
    jr $ra
    nop
