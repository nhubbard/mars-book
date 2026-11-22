# mars:tool cache blocks=16 wordsPerBlock=4 placement=full ways=16 replacement=lru logging=true
.data
.align 6
left: .word -5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2,3,4,5,-5,-4,-3,-2,-1,0,1,2
left_end: .word 0x13579bdf
.align 6
right: .word -3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2,3,-3,-2,-1,0,1,2
right_end: .word 0x2468ace0
.align 6
product: .word 0x5a5aa5a5:544
product_end: .word 0x31415926
shape: .word 16,16,16,4
method: .word 0
completed: .word -1
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $a0, left
    dla $a1, right
    dla $a2, product
    dla $a3, shape
    dla $t0, method
    lw $t0, 0($t0)
    dla $t9, matmul_naive
    beqzc $t0, matmul_call
    nop
    dla $t9, matmul_tiled
matmul_call:
    jalrc $t9
    nop
matmul_done:
    dla $t0, completed
    sw $v0, 0($t0)
results_ready:
    li $v0, 10
    syscall

# Direct grading can supply a distinct return continuation.
function_return:
    bc matmul_done
    nop

matmul_naive:
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
    move $s4, $a0
    move $s5, $a1
    move $s6, $a2
    lw $s0, 0($a3)
    lw $s1, 4($a3)
    lw $s2, 8($a3)
    lw $s3, 12($a3)
    beqzc $s0, naive_return
    nop
    beqzc $s2, naive_return
    nop
    move $t0, $zero
naive_row:
    move $t1, $zero
naive_column:
    move $t2, $zero
    move $t3, $zero
    mul $t4, $t0, $s1
    dsll $t4, $t4, 2
    daddu $t4, $s4, $t4
    dsll $t5, $t1, 2
    daddu $t5, $s5, $t5
    dsll $t6, $s2, 2
    beqzc $s1, naive_store
    nop
naive_dot:
    lw $t7, 0($t4)
    lw $t8, 0($t5)
    dmul $t7, $t7, $t8
    daddu $t3, $t3, $t7
    daddiu $t4, $t4, 4
    daddu $t5, $t5, $t6
    addiu $t2, $t2, 1
    bnec $t2, $s1, naive_dot
    nop
naive_store:
    mul $t4, $t0, $s2
    addu $t4, $t4, $t1
    dsll $t4, $t4, 3
    daddu $t4, $s6, $t4
    sd $t3, 0($t4)
    addiu $t1, $t1, 1
    bnec $t1, $s2, naive_column
    nop
    addiu $t0, $t0, 1
    bnec $t0, $s0, naive_row
    nop
naive_return:
    mul $v0, $s0, $s2
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

matmul_tiled:
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
    move $s4, $a0
    move $s5, $a1
    move $s6, $a2
    lw $s0, 0($a3)
    lw $s1, 4($a3)
    lw $s2, 8($a3)
    lw $s3, 12($a3)
    beqzc $s0, tiled_return
    nop
    beqzc $s2, tiled_return
    nop
    mul $t0, $s0, $s2
    move $t1, $s6
zero_product:
    sd $zero, 0($t1)
    daddiu $t1, $t1, 8
    addiu $t0, $t0, -1
    bnezc $t0, zero_product
    nop
    beqzc $s1, tiled_return
    nop
    move $s7, $zero
block_row:
    addu $a0, $s7, $s3
    sltu $t9, $s0, $a0
    beqzc $t9, row_bound
    nop
    move $a0, $s0
row_bound:
    move $fp, $zero
block_column:
    addu $a1, $fp, $s3
    sltu $t9, $s2, $a1
    beqzc $t9, column_bound
    nop
    move $a1, $s2
column_bound:
    move $t0, $zero
block_reduction:
    addu $a2, $t0, $s3
    sltu $t9, $s1, $a2
    beqzc $t9, reduction_bound
    nop
    move $a2, $s1
reduction_bound:
    move $t1, $s7
blocked_row:
    move $t2, $fp
blocked_column:
    mul $t7, $t1, $s2
    addu $t7, $t7, $t2
    dsll $t7, $t7, 3
    daddu $t7, $s6, $t7
    ld $t4, 0($t7)
    mul $t5, $t1, $s1
    addu $t5, $t5, $t0
    dsll $t5, $t5, 2
    daddu $t5, $s4, $t5
    mul $t6, $t0, $s2
    addu $t6, $t6, $t2
    dsll $t6, $t6, 2
    daddu $t6, $s5, $t6
    dsll $a3, $s2, 2
    move $t3, $t0
blocked_dot:
    lw $t8, 0($t5)
    lw $t9, 0($t6)
    dmul $t8, $t8, $t9
    daddu $t4, $t4, $t8
    daddiu $t5, $t5, 4
    daddu $t6, $t6, $a3
    addiu $t3, $t3, 1
    bnec $t3, $a2, blocked_dot
    nop
    sd $t4, 0($t7)
    addiu $t2, $t2, 1
    bnec $t2, $a1, blocked_column
    nop
    addiu $t1, $t1, 1
    bnec $t1, $a0, blocked_row
    nop
    addu $t0, $t0, $s3
    sltu $t9, $t0, $s1
    bnezc $t9, block_reduction
    nop
    addu $fp, $fp, $s3
    sltu $t9, $fp, $s2
    bnezc $t9, block_column
    nop
    addu $s7, $s7, $s3
    sltu $t9, $s7, $s0
    bnezc $t9, block_row
    nop
tiled_return:
    mul $v0, $s0, $s2
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
