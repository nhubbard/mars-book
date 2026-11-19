# Bounded nonempty file loading; destination is staging storage.
video_load:
    li $v0, 1
    move $v1, $zero
# BEGIN io_admit
    b io_missing_early
    nop
# END io_admit
io_admitted:
    nop
    daddiu $sp, $sp, -96
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
# BEGIN io_state
    b io_missing
    nop
# END io_state
io_state_ready:
    nop
# BEGIN io_open
    b io_missing
    nop
# END io_open
io_open_ready:
    nop
io_read_loop:
# BEGIN io_request
    b io_missing_owned
    nop
# END io_request
io_request_ready:
    nop
# BEGIN io_result
    b io_missing_owned
    nop
# END io_result
io_progress_ready:
    nop
    b io_read_loop
    nop
io_probe_begin:
# BEGIN io_probe
    b io_missing_owned
    nop
# END io_probe
io_probe_ready:
    nop
    bnez $v0, io_close_begin
    nop
io_eof_begin:
# BEGIN io_eof
    b io_missing_owned
    nop
# END io_eof
io_eof_ready:
    nop
    b io_close_begin
    nop
io_close_begin:
# BEGIN io_close
    b io_missing_owned
    nop
# END io_close
io_close_ready:
    nop
# BEGIN io_publish
    b io_missing
    nop
# END io_publish
io_published:
    nop
    b io_restore
    nop
io_missing_owned:
    move $a0, $s3
    li $v0, 16
    syscall
io_missing:
    li $v0, 4
    move $v1, $zero
    b io_restore
    nop
io_open_failed:
    li $v0, 1
    move $v1, $zero
io_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    daddiu $sp, $sp, 96
io_return:
    jr $ra
    nop
io_missing_early:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
