# Provided library: CRC of twelve header bytes followed by payload, omitting 12..15.
# a0: admitted record storage; a1: payload bytes, 0..28672. Leaf, caller-saved only.
# Writes the four checksum bytes. Returns status zero and unsigned CRC in v1.
video_record_crc:
    move $t0, $a0
    daddiu $t1, $a0, 12
    dli $t2, 0xffffffff
    dli $t6, 0xedb88320
    move $t7, $zero
drCrc_next:
    bne $t0, $t1, drCrc_byte
    nop
    bnez $t7, drCrc_finish
    nop
    daddiu $t0, $a0, 16
    daddu $t1, $t0, $a1
    li $t7, 1
    b drCrc_next
    nop
drCrc_byte:
    lbu $t3, 0($t0)
    xor $t2, $t2, $t3
    li $t4, 8
drCrc_bit:
    andi $t5, $t2, 1
    dsrl $t2, $t2, 1
    beqz $t5, drCrc_shifted
    nop
    xor $t2, $t2, $t6
drCrc_shifted:
    daddiu $t4, $t4, -1
    bnez $t4, drCrc_bit
    nop
    daddiu $t0, $t0, 1
    b drCrc_next
    nop
drCrc_finish:
    dli $t0, 0xffffffff
    xor $v1, $t2, $t0
    dsrl $t0, $v1, 24
    sb $t0, 12($a0)
    dsrl $t0, $v1, 16
    sb $t0, 13($a0)
    dsrl $t0, $v1, 8
    sb $t0, 14($a0)
    sb $v1, 15($a0)
    move $v0, $zero
    jr $ra
    nop
