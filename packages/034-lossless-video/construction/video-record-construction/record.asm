# Unfinished exact-record validation and scalar frame application.
video_apply_record:
    li $v0, 1
    move $v1, $zero
# BEGIN record_admit
    b record_missing_early
    nop
# END record_admit
record_admitted:
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
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
# BEGIN record_context
    b record_missing
    nop
# END record_context
record_context_ready:
    nop
# BEGIN record_kind
    b record_missing
    nop
# END record_kind
record_kind_ready:
    nop
# BEGIN record_sequence
    b record_missing
    nop
# END record_sequence
record_sequence_ready:
    nop
# BEGIN record_extent
    b record_missing
    nop
# END record_extent
record_extent_ready:
    nop
# BEGIN record_crc_state
    b record_missing
    nop
# END record_crc_state
record_crc_state_ready:
    nop
record_crc_next:
    bne $t0, $t1, record_crc_read
    nop
    bnez $t7, record_crc_finish
    nop
    move $t0, $s5
    move $t1, $s6
    li $t7, 1
    b record_crc_next
    nop
record_crc_read:
# BEGIN record_crc_byte
    b record_missing
    nop
# END record_crc_byte
record_crc_byte_ready:
    nop
    li $t4, 8
record_crc_bits:
# BEGIN record_crc_bit
    b record_missing
    nop
# END record_crc_bit
record_crc_bit_ready:
    nop
    bnez $t4, record_crc_bits
    nop
    b record_crc_next
    nop
record_crc_finish:
# BEGIN record_crc_check
    b record_missing
    nop
# END record_crc_check
record_crc_checked:
    nop
# BEGIN record_shape
    b record_missing
    nop
# END record_shape
record_shape_ready:
    nop
    beqz $s4, record_validation_ready
    nop
record_validate_pair:
# BEGIN record_pair
    b record_missing
    nop
# END record_pair
record_pair_ready:
    nop
# BEGIN record_pixel_bounds
    b record_missing
    nop
# END record_pixel_bounds
record_pixels_ready:
    nop
# BEGIN record_payload_bounds
    b record_missing
    nop
# END record_payload_bounds
record_payload_ready:
    nop
    sltiu $t4, $t1, 4096
    bnez $t4, record_validate_pair
    nop
# BEGIN record_exact
    b record_missing
    nop
# END record_exact
record_validation_ready:
    nop
    bnez $s4, record_apply_delta
    nop
# BEGIN record_full
    b record_missing
    nop
# END record_full
record_full_applied:
    nop
    b record_publish_begin
    nop
record_apply_delta:
# BEGIN record_delta
    b record_missing
    nop
# END record_delta
record_delta_applied:
    nop
record_publish_begin:
# BEGIN record_publish
    b record_missing
    nop
# END record_publish
record_published:
    nop
    b record_restore
    nop
record_fail:
    li $v0, 1
    move $v1, $zero
    b record_restore
    nop
record_missing:
    li $v0, 4
    move $v1, $zero
record_restore:
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
record_return:
    jr $ra
    nop
record_missing_early:
    li $v0, 4
    move $v1, $zero
    jr $ra
    nop
