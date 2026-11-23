.data
.align 6
words: .word 1000,1002,1001,1001,20000
       .word 0x6a6b6c6d:35
words_end: .word 0x13579bdf
.align 6
stream: .byte 2,-128,0
    .byte -91:189
stream_end: .word 0x2468ace0
word_count: .word 5
byte_capacity: .word 160
byte_count: .word 3
word_capacity: .word 1
method: .word 1
input_used: .word -1
output_used: .word -1
status: .word -1
control_end: .word 0x31415926
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $t0, method
    lw $t0, 0($t0)
    bnezc $t0, choose_decode
    nop
    dla $a0, words
    dla $t0, word_count
    lw $a1, 0($t0)
    dla $a2, stream
    dla $t0, byte_capacity
    lw $a3, 0($t0)
    dla $t9, delta_encode
    bc codec_call
    nop
choose_decode:
    dla $a0, stream
    dla $t0, byte_count
    lw $a1, 0($t0)
    dla $a2, words
    dla $t0, word_capacity
    lw $a3, 0($t0)
    dla $t9, delta_decode
codec_call:
    jalrc $t9
    nop
codec_done:
    dla $t0, input_used
    sw $v0, 0($t0)
    dla $t0, output_used
    sw $v1, 0($t0)
    dla $t0, status
    sw $a3, 0($t0)
results_ready:
    li $v0, 10
    syscall
function_return:
    bc codec_done
    nop

delta_encode:
    daddiu $sp, $sp, -80
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    move $s4, $zero
    move $s5, $zero
    move $s6, $zero
    move $a3, $zero
enc_next:
    beqc $s5, $s1, enc_return
    nop
    beqc $s6, $s3, enc_full
    nop
    dsll $t0, $s5, 2
    daddu $t0, $s0, $t0
    lw $t0, 0($t0)
    dsubu $t1, $t0, $s4
enc_map:
    dsll $t2, $t1, 1
    dsra $t3, $t1, 63
    xor $t2, $t2, $t3
    move $t3, $t2
    li $t4, 1
enc_length:
    dsrl $t3, $t3, 7
    beqzc $t3, enc_check_capacity
    nop
    addiu $t4, $t4, 1
    bc enc_length
    nop
enc_check_capacity:
    dsubu $t5, $s3, $s6
    sltu $t5, $t5, $t4
    bnezc $t5, enc_full
    nop
    daddu $t5, $s2, $s6
enc_byte:
    andi $t6, $t2, 127
    dsrl $t2, $t2, 7
    beqzc $t2, enc_last_byte
    nop
    ori $t6, $t6, 128
    sb $t6, 0($t5)
    daddiu $t5, $t5, 1
    bc enc_byte
    nop
enc_last_byte:
    sb $t6, 0($t5)
    move $s4, $t0
    addiu $s5, $s5, 1
    addu $s6, $s6, $t4
    bc enc_next
    nop
enc_full:
    li $a3, 1
enc_return:
    move $v0, $s5
    move $v1, $s6
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 80
    jrc $ra
    nop

delta_decode:
    daddiu $sp, $sp, -80
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    sd $fp, 64($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    move $s4, $zero
    move $s5, $zero
    move $s6, $zero
    move $a3, $zero
dec_next:
    beqc $s5, $s1, dec_return
    nop
    beqc $s6, $s3, dec_full
    nop
    move $t0, $s5
    move $t1, $zero
    move $t2, $zero
    move $t3, $zero
dec_byte:
    beqc $t0, $s1, dec_bad
    nop
    daddu $t4, $s0, $t0
    lbu $t4, 0($t4)
    andi $t5, $t4, 127
    addiu $t0, $t0, 1
    addiu $t3, $t3, 1
    li $t6, 5
    bnec $t3, $t6, dec_accumulate
    nop
    andi $t6, $t4, 128
    bnezc $t6, dec_bad
    nop
    sltiu $t6, $t5, 32
    beqzc $t6, dec_bad
    nop
dec_accumulate:
    dsllv $t6, $t5, $t2
    or $t1, $t1, $t6
    andi $t6, $t4, 128
    beqzc $t6, dec_terminal
    nop
    addiu $t2, $t2, 7
    bc dec_byte
    nop
dec_terminal:
    li $t6, 1
    beqc $t3, $t6, dec_unmap
    nop
    beqzc $t5, dec_bad
    nop
dec_unmap:
    andi $t5, $t1, 1
    dsubu $t5, $zero, $t5
    dsrl $t6, $t1, 1
    xor $t6, $t6, $t5
    daddu $t6, $s4, $t6
    dli $t7, -2147483648
    slt $t9, $t6, $t7
    bnezc $t9, dec_range
    nop
    li $t8, 2147483647
    slt $t9, $t8, $t6
    bnezc $t9, dec_range
    nop
    dsll $t7, $s6, 2
    daddu $t7, $s2, $t7
    sw $t6, 0($t7)
    move $s4, $t6
    move $s5, $t0
    addiu $s6, $s6, 1
    bc dec_next
    nop
dec_full:
    li $a3, 1
    bc dec_return
    nop
dec_bad:
    li $a3, 2
    bc dec_return
    nop
dec_range:
    li $a3, 3
dec_return:
    move $v0, $s5
    move $v1, $s6
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    ld $fp, 64($sp)
    daddiu $sp, $sp, 80
    jrc $ra
    nop
