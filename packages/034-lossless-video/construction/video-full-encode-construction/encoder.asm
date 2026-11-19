# Encode one full MLV1 frame from 4096 native 0x00RRGGBB words.
video_encode_full:
    li $v0, 1
    move $v1, $zero
# BEGIN ef_admit
    b ef_missing_early
    nop
# END ef_admit
ef_admitted:
    nop
    daddiu $sp, $sp, -96
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    move $s0, $a0
    move $s1, $a2
    move $s2, $a3
# BEGIN ef_context
    b ef_missing
    nop
# END ef_context
ef_context_ready:
    nop
# BEGIN ef_state
    b ef_missing
    nop
# END ef_state
ef_state_ready:
    nop
# BEGIN ef_validate
    b ef_missing
    nop
# END ef_validate
ef_pixels_valid:
    nop
# BEGIN ef_header
    b ef_missing
    nop
# END ef_header
ef_header_ready:
    nop
# BEGIN ef_length
    b ef_missing
    nop
# END ef_length
ef_length_ready:
    nop
# BEGIN ef_payload
    b ef_missing
    nop
# END ef_payload
ef_payload_ready:
    nop
# BEGIN ef_crc_state
    b ef_missing
    nop
# END ef_crc_state
ef_crc_state_ready:
    nop
ef_crc_next:
    bne $t0, $t1, ef_crc_read
    nop
    bnez $t7, ef_crc_finish
    nop
    daddiu $t0, $s1, 16
    daddiu $t1, $s1, 12304
    li $t7, 1
    b ef_crc_next
    nop
ef_crc_read:
    lbu $t5, 0($t0)
    xor $t2, $t2, $t5
    li $t4, 8
ef_crc_bits:
# BEGIN ef_crc_bit
    b ef_missing
    nop
# END ef_crc_bit
ef_crc_bit_ready:
    nop
    daddiu $t4, $t4, -1
    bnez $t4, ef_crc_bits
    nop
    daddiu $t0, $t0, 1
    b ef_crc_next
    nop
ef_crc_finish:
# BEGIN ef_crc_store
    b ef_missing
    nop
# END ef_crc_store
ef_crc_ready:
    nop
# BEGIN ef_previous
    b ef_missing
    nop
# END ef_previous
ef_previous_ready:
    nop
# BEGIN ef_publish
    b ef_missing
    nop
# END ef_publish
ef_published:
    nop
    b ef_restore
    nop
ef_failed:
    li $v0, 1
    move $v1, $zero
    b ef_restore
    nop
ef_missing:
    li $v0, 4
    move $v1, $zero
ef_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    daddiu $sp, $sp, 96
ef_return:
    jr $ra
    nop
ef_missing_early:
    li $v0, 4
    jr $ra
    nop
