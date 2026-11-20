.text
main:
    li $v0, 10
    syscall
h263_sad16:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_sad16_msa:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_search16:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_predict8:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_predict_mb:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_transform8:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_quantize8:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_encode_block8:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_write_bits:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_emit_coefficients:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_emit_macroblock:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_encode_picture:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
h263_encode_clip:
    li $v0, 7
    move $v1, $zero
    jr $ra
    nop
