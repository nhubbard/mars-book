# Stored-block construction; fixed and dynamic paths remain explicit development exits.
png_inflate:
# BEGIN inflate_bounds
    li $v0, 4
    move $v1, $zero
    b inflate_early
    nop
# END inflate_bounds
inflate_bounds_ready:
    nop
    daddiu $sp, $sp, -2048
    sd $ra, 64($sp)
    sd $s0, 72($sp)
    sd $s1, 80($sp)
    sd $s2, 88($sp)
    sd $s3, 96($sp)
    sd $s4, 104($sp)
    sd $s5, 112($sp)
    sd $s6, 120($sp)
    sd $s7, 128($sp)
    sd $fp, 136($sp)
    move $fp, $sp
# BEGIN inflate_header
    b inflate_missing
    nop
# END inflate_header
inflate_header_ready:
    nop
# BEGIN inflate_state
    b inflate_missing
    nop
# END inflate_state
inflate_state_ready:
    nop
deflate_block:
    li $a0, 3
    jal read_bits
    nop
# BEGIN inflate_dispatch
    b inflate_missing
    nop
# END inflate_dispatch
inflate_dispatch_ready:
    nop
stored_block:
# BEGIN inflate_align
    b inflate_missing
    nop
# END inflate_align
inflate_align_ready:
    nop
# BEGIN inflate_lengths
    b inflate_missing
    nop
# END inflate_lengths
inflate_lengths_ready:
    nop
# BEGIN inflate_stored_bounds
    b inflate_missing
    nop
# END inflate_stored_bounds
inflate_stored_bounds_ready:
    nop
stored_copy:
# BEGIN inflate_copy
    b inflate_missing
    nop
# END inflate_copy
inflate_copy_ready:
    nop
block_done:
# BEGIN inflate_block_done
    b inflate_missing
    nop
# END inflate_block_done
inflate_final_ready:
    nop
# BEGIN inflate_exact
    b inflate_missing
    nop
# END inflate_exact
inflate_exact_ready:
    nop
    move $a0, $s7
    move $a1, $s5
    jal adler32
    nop
inflate_adler_ready:
    nop
    sd $v0, 0($sp)
    move $a0, $s1
    jal inflate_be32
    nop
inflate_trailer_ready:
    nop
# BEGIN inflate_checksum
    b inflate_missing
    nop
# END inflate_checksum
inflate_checksum_ready:
    nop
# BEGIN inflate_publish
    b inflate_missing
    nop
# END inflate_publish
inflate_publish_ready:
    nop
    b inflate_restore
    nop
fixed_block:
dynamic_block:
inflate_missing:
    li $v0, 4
    move $v1, $zero
    b inflate_restore
    nop
inflate_bad:
    li $v0, 1
    move $v1, $zero
inflate_restore:
    ld $ra, 64($sp)
    ld $s0, 72($sp)
    ld $s1, 80($sp)
    ld $s2, 88($sp)
    ld $s3, 96($sp)
    ld $s4, 104($sp)
    ld $s5, 112($sp)
    ld $s6, 120($sp)
    ld $s7, 128($sp)
    ld $fp, 136($sp)
    daddiu $sp, $sp, 2048
inflate_early:
    jr $ra
    nop

# Until complete, internal helpers unwind the owning inflater frame.
read_bits:
# BEGIN inflate_refill
    b inflate_missing
    nop
# END inflate_refill
inflate_refill_unreached:
    nop
bits_ready:
# BEGIN inflate_extract
    b inflate_missing
    nop
# END inflate_extract
inflate_bits_ready:
    nop
    jr $ra
    nop
adler32:
# BEGIN inflate_adler
    b inflate_missing
    nop
# END inflate_adler
inflate_adler_return_ready:
    nop
    jr $ra
    nop
inflate_be32:
# BEGIN inflate_trailer
    b inflate_missing
    nop
# END inflate_trailer
inflate_be32_ready:
    nop
    jr $ra
    nop
