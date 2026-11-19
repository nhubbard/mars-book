# Final caller wiring. Result publication remains a student-authored region.
png_decode:
    move $t9, $zero
    b publication_entry
    nop
png_decode_msa:
    li $t9, 1
publication_entry:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a1, 8
    bnez $t0, publication_early
    nop
    dli $t0, 1048577
    sltu $t0, $a1, $t0
    beqz $t0, publication_early
    nop
    daddiu $sp, $sp, -112
    sd $ra, 0($sp)
    sd $a3, 48($sp)
    sd $t9, 56($sp)
    sd $a2, 64($sp)
    ld $a2, 16($a3)
    ld $t0, 24($a3)
    sd $t0, 8($sp)
    dli $t0, 0xa5a5a5a5a5a5a5a5
    sd $t0, 16($sp)
    sd $t0, 24($sp)
    sd $t0, 32($sp)
    sd $t0, 40($sp)
    daddiu $a3, $sp, 8
collector_call:
    jal png_collect
    nop
collector_return:
    bnez $v0, publication_fail
    nop
    ld $t0, 48($sp)
    ld $a0, 16($t0)
    ld $a1, 32($sp)
    ld $a2, 32($t0)
    ld $a3, 40($sp)
    ld $t1, 40($t0)
    sltu $t1, $t1, $a3
    beqz $t1, inflate_call
    nop
    li $v0, 1
    b publication_fail
    nop
inflate_call:
    jal png_inflate
    nop
inflate_return:
    bnez $v0, publication_fail
    nop
    ld $t0, 48($sp)
    ld $a0, 32($t0)
    ld $a1, 40($sp)
    ld $a2, 16($sp)
    ld $a3, 24($sp)
    ld $t0, 56($sp)
    beqz $t0, row_scalar_call
    nop
row_vector_call:
    jal png_unfilter_msa
    nop
    b publication_rows_return
    nop
row_scalar_call:
    jal png_unfilter
    nop
publication_rows_return:
    bnez $v0, publication_fail
    nop
    ld $t0, 16($sp)
    sd $t0, 80($sp)
    ld $t0, 24($sp)
    sd $t0, 88($sp)
    ld $t1, 48($sp)
    ld $t0, 0($t1)
    sd $t0, 96($sp)
    ld $t0, 8($t1)
    sd $t0, 104($sp)
    ld $a0, 32($t1)
    ld $a1, 40($sp)
    ld $a2, 64($sp)
    daddiu $a3, $sp, 80
    ld $t0, 56($sp)
    beqz $t0, packing_scalar_call
    nop
packing_vector_call:
    jal png_blit_msa
    nop
    b publication_blit_return
    nop
packing_scalar_call:
    jal png_blit
    nop
publication_blit_return:
    bnez $v0, publication_fail
    nop
# BEGIN publish_results
    li $v0, 4
    move $v1, $zero
    b publication_return
    nop
# END publish_results
publication_results_ready:
    nop
    b publication_return
    nop
publication_fail:
    move $v1, $zero
publication_return:
    ld $ra, 0($sp)
    daddiu $sp, $sp, 112
publication_early:
    jr $ra
    nop
