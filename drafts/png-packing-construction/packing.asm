# Unfinished RGB8 packing; descriptor fields and result contract are in the chapter.
png_blit:
    move $t9, $zero
    b blit_entry
    nop
png_blit_msa:
    li $t9, 1
blit_entry:
    li $v0, 1
    move $v1, $zero
# BEGIN pack_admit
    b pack_missing
    nop
# END pack_admit
pack_admitted:
    nop
# BEGIN pack_windows
    b pack_missing
    nop
# END pack_windows
pack_windows_ready:
    nop
# BEGIN pack_state
    b pack_missing
    nop
# END pack_state
pack_state_ready:
    nop
    beqz $v1, blit_row
    nop
pack_masks_ready:
    nop
# BEGIN pack_load_masks
    b pack_missing
    nop
# END pack_load_masks
pack_masks_loaded:
    nop
blit_row:
# BEGIN pack_cursor
    b pack_missing
    nop
# END pack_cursor
pack_cursor_ready:
    nop
    beqz $v1, blit_pixel
    nop
blit_vector:
# BEGIN pack_vector_bound
    b pack_missing
    nop
# END pack_vector_bound
pack_vector_ready:
    nop
    bnez $t2, blit_tail
    nop
# BEGIN pack_vector_load
    b pack_missing
    nop
# END pack_vector_load
pack_inputs_loaded:
    nop
# BEGIN pack_shuffle_first
    b pack_missing
    nop
# END pack_shuffle_first
pack_first_written:
    nop
# BEGIN pack_shuffle_middle
    b pack_missing
    nop
# END pack_shuffle_middle
pack_middle_written:
    nop
# BEGIN pack_shuffle_last
    b pack_missing
    nop
# END pack_shuffle_last
pack_last_written:
    nop
# BEGIN pack_vector_advance
    b pack_missing
    nop
# END pack_vector_advance
pack_vector_advanced:
    nop
    b blit_vector
    nop
blit_tail:
    beqz $t1, blit_row_done
    nop
blit_pixel:
# BEGIN pack_pixel
    b pack_missing
    nop
# END pack_pixel
pack_pixel_written:
    nop
    bnez $t1, blit_pixel
    nop
blit_row_done:
# BEGIN pack_row
    b pack_missing
    nop
# END pack_row
pack_row_advanced:
    nop
    bnez $a3, blit_row
    nop
    move $v1, $t8
    move $v0, $zero
pack_success:
    jr $ra
    nop
pack_missing:
    li $v0, 4
    move $v1, $zero
pack_early:
    jr $ra
    nop
