# Unfinished row reconstruction. The two entries share a per-call working frame.
png_unfilter:
    move $t9, $zero
    b unfilter_entry
    nop
png_unfilter_msa:
    li $t9, 1
unfilter_entry:
    li $v0, 1
    move $v1, $zero
# BEGIN row_shape
    b row_missing_early
    nop
# END row_shape
row_shape_ready:
    nop
# BEGIN row_selectors
    b row_missing_early
    nop
# END row_selectors
row_selectors_ready:
    nop
    daddiu $sp, $sp, -1552
    sd $a1, 1536($sp)
# BEGIN row_zero
    b row_missing_frame
    nop
# END row_zero
row_zero_ready:
    nop
filter_row:
# BEGIN row_cursor
    b row_missing_frame
    nop
# END row_cursor
row_cursor_ready:
    nop
    beqz $t0, filter_row_done
    nop
    beqz $v1, filter_byte
    nop
    li $t4, 2
    beq $t0, $t4, filter_up_vector
    nop
filter_byte:
# BEGIN row_neighbors
    b row_missing_frame
    nop
# END row_neighbors
row_neighbors_ready:
    nop
filter_predict:
    li $t7, 1
    beq $t0, $t7, filter_left
    nop
    li $t7, 2
    beq $t0, $t7, filter_store
    nop
    li $t7, 3
    beq $t0, $t7, filter_average
    nop
# BEGIN row_paeth
    b row_missing_frame
    nop
# END row_paeth
row_paeth_ready:
    nop
    b filter_store
    nop
filter_left:
# BEGIN row_left
    b row_missing_frame
    nop
# END row_left
row_left_ready:
    nop
    b filter_store
    nop
filter_average:
# BEGIN row_average
    b row_missing_frame
    nop
# END row_average
row_average_ready:
    nop
filter_store:
# BEGIN row_store
    b row_missing_frame
    nop
# END row_store
row_store_ready:
    nop
    bne $t1, $t3, filter_byte
    nop
    b filter_row_done
    nop
filter_up_vector:
# BEGIN row_vector_bound
    b row_missing_frame
    nop
# END row_vector_bound
row_vector_ready:
    nop
    bnez $t4, filter_up_tail
    nop
# BEGIN row_vector_body
    b row_missing_frame
    nop
# END row_vector_body
row_vector_written:
    nop
    b filter_up_vector
    nop
filter_up_tail:
# BEGIN row_up_tail
    b row_missing_frame
    nop
# END row_up_tail
row_up_tail_ready:
    nop
filter_row_done:
# BEGIN row_finish
    b row_missing_frame
    nop
# END row_finish
row_advance_ready:
    nop
    bnez $a1, filter_row
    nop
    ld $v1, 1536($sp)
    daddiu $sp, $sp, 1552
    move $v0, $zero
row_result_ready:
    nop
    jr $ra
    nop
row_missing_frame:
    daddiu $sp, $sp, 1552
row_missing_early:
    li $v0, 4
    move $v1, $zero
row_early:
    jr $ra
    nop
