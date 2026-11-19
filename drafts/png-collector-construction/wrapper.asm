# Temporary decoder: validate the container, then stop before inflation.
png_decode:
    li $v0, 1
    move $v1, $zero
    dli $t0, 1048577
    sltu $t0, $a1, $t0
    beqz $t0, collector_partial_early
    nop
    sltiu $t0, $a1, 8
    bnez $t0, collector_partial_early
    nop
    daddiu $sp, $sp, -64
    sd $ra, 0($sp)
    ld $a2, 16($a3)
    ld $t1, 24($a3)
    dla $t0, collector_capacity
    ld $t0, 0($t0)
    sltu $t1, $t1, $t0
    bnez $t1, collector_partial_exit
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
    bnez $v0, collector_partial_exit
    nop
    li $v0, 4
collector_partial_exit:
    move $v1, $zero
    ld $ra, 0($sp)
    daddiu $sp, $sp, 64
collector_partial_early:
    jr $ra
    nop
png_decode_msa:
    b png_decode
    nop
