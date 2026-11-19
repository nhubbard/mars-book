# Encode a packed sequence of native 64x64 images into one staged MLV1 file.
video_encode_file:
    li $v0, 1
    move $v1, $zero
# BEGIN fe_admit
    b fe_missing_early
    nop
# END fe_admit
fe_admitted:
    nop
    daddiu $sp, $sp, -224
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    sd $s7, 64($sp)
    sd $a1, 72($sp)
    sd $a0, 80($sp)
    move $s0, $a0
    move $s1, $a2
    move $s2, $a3
# BEGIN fe_context
    b fe_missing
    nop
# END fe_context
fe_context_ready:
    nop
# BEGIN fe_state
    b fe_missing
    nop
# END fe_state
fe_state_ready:
    nop
# BEGIN fe_record_context
    b fe_missing
    nop
# END fe_record_context
fe_record_context_ready:
    nop
# BEGIN fe_header
    b fe_missing
    nop
# END fe_header
fe_header_ready:
    nop
fe_loop:
# BEGIN fe_window
    b fe_missing
    nop
# END fe_window
fe_window_ready:
    nop
# BEGIN fe_record
    b fe_missing
    nop
# END fe_record
fe_record_ready:
    nop
# BEGIN fe_advance
    b fe_missing
    nop
# END fe_advance
fe_record_advanced:
    nop
    bne $s4, $s3, fe_loop
    nop
# BEGIN fe_exact
    b fe_missing
    nop
# END fe_exact
fe_complete:
    nop
# BEGIN fe_publish
    b fe_missing
    nop
# END fe_publish
fe_published:
    nop
    b fe_restore
    nop
fe_failed:
    li $v0, 1
    move $v1, $zero
    b fe_restore
    nop
fe_missing:
    li $v0, 4
    move $v1, $zero
fe_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    daddiu $sp, $sp, 224
fe_return:
    jr $ra
    nop
fe_missing_early:
    li $v0, 4
    jr $ra
    nop
