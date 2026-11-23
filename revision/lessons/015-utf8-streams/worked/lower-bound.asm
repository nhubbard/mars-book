# Complete experiment: one byte against an inclusive unsigned interval.
.data
sample: .byte 0xa0
.align 2
range_result: .word -1
.text
.globl main
main:
    dla $t0, sample
    lbu $a0, 0($t0)
    li $a1, 160
    li $a2, 191
    balc within_range
    nop
    dla $t0, range_result
range_done:
    sw $v0, 0($t0)
done:
    li $v0, 10
    syscall

# a0: unsigned byte; a1/a2: inclusive bounds; v0: one if inside, else zero.
within_range:
    sltu $t0, $a0, $a1
    bnezc $t0, outside_range
    nop
    sltu $t0, $a2, $a0
    xori $v0, $t0, 1
    jrc $ra
outside_range:
    move $v0, $zero
    jrc $ra
