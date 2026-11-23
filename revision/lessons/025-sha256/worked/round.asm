# Worked experiment: one compression round for the padded abc block.
# The working state below is not yet a completed hash or feed-forward result.
.data
.align 2
state: .word 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
round_state: .space 32
.text
.globl main
main:
    dla $t0, state
    lw $s0, 0($t0)
    lw $s1, 4($t0)
    lw $s2, 8($t0)
    lw $s3, 12($t0)
    lw $s4, 16($t0)
    lw $s5, 20($t0)
    lw $s6, 24($t0)
    lw $s7, 28($t0)
round_inputs:
    rotr $t0, $s4, 6
    rotr $t1, $s4, 11
    xor $t0, $t0, $t1
    rotr $t1, $s4, 25
    xor $t0, $t0, $t1
    and $t1, $s4, $s5
    nor $t2, $s4, $zero
    and $t2, $t2, $s6
    xor $t1, $t1, $t2
    addu $t3, $s7, $t0
    addu $t3, $t3, $t1
    li $t2, 0x428a2f98
    addu $t3, $t3, $t2
    li $t2, 0x61626380
    addu $t3, $t3, $t2
first_sum:
    rotr $t0, $s0, 2
    rotr $t1, $s0, 13
    xor $t0, $t0, $t1
    rotr $t1, $s0, 22
    xor $t0, $t0, $t1
    and $t1, $s0, $s1
    and $t2, $s0, $s2
    xor $t1, $t1, $t2
    and $t2, $s1, $s2
    xor $t1, $t1, $t2
    addu $t4, $t0, $t1
round_sums:
    addu $t5, $t3, $t4
    addu $t6, $s3, $t3
new_words:
    move $s7, $s6
    move $s6, $s5
    move $s5, $s4
    move $s4, $t6
    move $s3, $s2
    move $s2, $s1
    move $s1, $s0
    move $s0, $t5
    dla $t0, round_state
    sw $s0, 0($t0)
    sw $s1, 4($t0)
    sw $s2, 8($t0)
    sw $s3, 12($t0)
    sw $s4, 16($t0)
    sw $s5, 20($t0)
    sw $s6, 24($t0)
    sw $s7, 28($t0)
round_ready:
    li $v0, 10
    syscall
