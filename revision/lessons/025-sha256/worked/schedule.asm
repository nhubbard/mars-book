# Worked experiment: decode one block and derive W16 through W19.
# This is not the six-function independent SHA-256 implementation.
.data
.align 2
block: .byte 97,98,99,128
       .byte 0:52
       .byte 0,0,0,0,0,0,0,24
schedule: .space 256
.text
.globl main
main:
    dla $s0, block
    dla $s1, schedule
    li $s2, 16
decode_word:
    lbu $t0, 0($s0)
    sll $t0, $t0, 24
    lbu $t1, 1($s0)
    sll $t1, $t1, 16
    or $t0, $t0, $t1
    lbu $t1, 2($s0)
    sll $t1, $t1, 8
    or $t0, $t0, $t1
    lbu $t1, 3($s0)
    or $t0, $t0, $t1
decoded_word:
    sw $t0, 0($s1)
    daddiu $s0, $s0, 4
    daddiu $s1, $s1, 4
    addiu $s2, $s2, -1
    bnezc $s2, decode_word
    nop
words_ready:
    li $s2, 4
expand_word:
    lw $t0, -60($s1)
    rotr $t1, $t0, 7
    rotr $t2, $t0, 18
    xor $t1, $t1, $t2
    srl $t2, $t0, 3
    xor $t1, $t1, $t2
    lw $t3, -8($s1)
    rotr $t4, $t3, 17
    rotr $t5, $t3, 19
    xor $t4, $t4, $t5
    srl $t5, $t3, 10
    xor $t4, $t4, $t5
    lw $t6, -64($s1)
    lw $t7, -28($s1)
    addu $t6, $t6, $t1
    addu $t6, $t6, $t7
    addu $t6, $t6, $t4
expanded_word:
    sw $t6, 0($s1)
    daddiu $s1, $s1, 4
    addiu $s2, $s2, -1
    bnezc $s2, expand_word
    nop
schedule_ready:
    li $v0, 10
    syscall
