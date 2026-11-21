.data
.align 2
values: .word 2, -2, 3, -4, 0, 1, -3, -1
values_guard: .word 0x13579bdf
count: .word 8
count_guard: .word 0x2468ace0
negative_count: .word -1
result_guard: .word 0x10203040
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $a0, values
    lw $a1, count
    balc partition_negative
partition_done:
    sw $v0, negative_count
partition_exit:
    li $v0, 10
    syscall

# a0: word-aligned array address; a1: active length 0..8; words are signed 32-bit.
# Group negatives before nonnegatives, preserving relative order within each group.
# Return v0 = number of negatives; preserve inactive words, guards and saved state.
# Replace the failing body. Equivalent algorithms and private frame layouts are allowed.
partition_negative:
    break
