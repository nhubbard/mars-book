# Temporary decoder: collect, inflate and reconstruct; stop before pixel packing.
png_decode:
    move $t9, $zero
    b row_decode_entry
    nop
png_decode_msa:
    li $t9, 1
row_decode_entry:
    li $v0, 1
    move $v1, $zero
    sltiu $t0, $a1, 8
    bnez $t0, inflater_partial_early
    nop
    dli $t0, 1048577
    sltu $t0, $a1, $t0
    beqz $t0, inflater_partial_early
    nop
    daddiu $sp, $sp, -80
    sd $ra, 0($sp)
    sd $a3, 48($sp)
    sd $t9, 56($sp)
    ld $a2, 16($a3)
    ld $t1, 24($a3)
    dla $t0, collector_capacity
    ld $t0, 0($t0)
    sltu $t1, $t1, $t0
    bnez $t1, inflater_partial_exit
    nop
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
    bnez $v0, inflater_partial_exit
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
    b inflater_partial_exit
    nop
inflate_call:
    jal png_inflate
    nop
inflate_return:
    bnez $v0, inflater_partial_exit
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
    b row_stage_return
    nop
row_scalar_call:
    jal png_unfilter
    nop
row_stage_return:
    bnez $v0, inflater_partial_exit
    nop
    li $v0, 4
inflater_partial_exit:
    move $v1, $zero
    ld $ra, 0($sp)
    daddiu $sp, $sp, 80
inflater_partial_early:
    jr $ra
    nop
