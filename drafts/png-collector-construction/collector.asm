# Append in the text section through attach.py. Every unfinished region exits safely.
png_collect:
# BEGIN collect_bounds
    li $v0, 4
    move $v1, $zero
    b collect_early
    nop
# END collect_bounds
collect_bounds_ready:
    nop
    daddiu $sp, $sp, -128
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    sd $s5, 48($sp)
    sd $s6, 56($sp)
    sd $s7, 64($sp)
    sd $fp, 72($sp)
# BEGIN collect_state
    b collect_missing
    nop
# END collect_state
collect_state_ready:
    nop
# BEGIN collect_signature
    b collect_missing
    nop
# END collect_signature
collect_signature_ready:
    nop
collect_chunk:
# BEGIN collect_envelope
    b collect_missing
    nop
# END collect_envelope
collect_envelope_ready:
    nop
# BEGIN collect_type
    b collect_missing
    nop
# END collect_type
collect_type_ready:
    nop
# BEGIN collect_crc_check
    b collect_missing
    nop
# END collect_crc_check
collect_crc_ready:
    nop
# BEGIN collect_dispatch
    b collect_missing
    nop
# END collect_dispatch
collect_dispatch_ready:
    nop
collect_ancillary:
# BEGIN collect_ancillary
    b collect_missing
    nop
# END collect_ancillary
collect_ancillary_ready:
    nop
    b collect_next
    nop
collect_ihdr:
# BEGIN collect_ihdr
    b collect_missing
    nop
# END collect_ihdr
collect_ihdr_ready:
    nop
    b collect_next
    nop
collect_plte:
# BEGIN collect_plte
    b collect_missing
    nop
# END collect_plte
collect_plte_ready:
    nop
    b collect_next
    nop
collect_idat:
# BEGIN collect_idat
    b collect_missing
    nop
# END collect_idat
collect_idat_ready:
    nop
collect_copy:
# BEGIN collect_copy
    b collect_missing
    nop
# END collect_copy
collect_copy_ready:
    nop
collect_next:
# BEGIN collect_next
    b collect_missing
    nop
# END collect_next
collect_next_ready:
    nop
    b collect_chunk
    nop
collect_iend:
# BEGIN collect_iend
    b collect_missing
    nop
# END collect_iend
collect_iend_ready:
    nop
# BEGIN collect_publish
    b collect_missing
    nop
# END collect_publish
collect_publish_ready:
    nop
    b collect_restore
    nop
collect_missing:
    li $v0, 4
    move $v1, $zero
    b collect_restore
    nop
collect_bad:
    li $v0, 1
    move $v1, $zero
collect_restore:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    ld $s5, 48($sp)
    ld $s6, 56($sp)
    ld $s7, 64($sp)
    ld $fp, 72($sp)
    daddiu $sp, $sp, 128
collect_early:
    jr $ra
    nop

# Internal helpers: incomplete bodies leave through the collector-owned frame.
png_be32:
# BEGIN collect_be32
    b collect_missing
    nop
# END collect_be32
collect_be32_ready:
    nop
    jr $ra
    nop
png_crc32:
# BEGIN collect_crc
    b collect_missing
    nop
# END collect_crc
collect_crc32_ready:
    nop
    jr $ra
    nop
