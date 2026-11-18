# mars:tool cache blocks=16 wordsPerBlock=4 placement=full ways=16 replacement=lru logging=true
.data
.align 6
left: .word 1,2,3,4,5,6
.space 1064
left_end: .word 0x13579bdf
.align 6
right: .word 7,8,9,10,11,12
.space 1064
right_end: .word 0x2468ace0
.align 6
product: .word 0x5a5aa5a5:544
product_end: .word 0x31415926
shape: .word 2,3,2,2
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
    beq $t0, $zero, matmul_call
    nop
    dla $t9, matmul_tiled
matmul_call:
    jalr $t9
    nop
matmul_done:
    dla $t0, completed
    sw $v0, 0($t0)
results_ready:
    li $v0, 10
    syscall

# Direct grading can supply a distinct return continuation.
function_return:
    beq $zero, $zero, matmul_done
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
# BEGIN n_empty
    beq $zero, $zero, naive_return
    nop
# END n_empty
naive_row:
# BEGIN n_column
    beq $zero, $zero, naive_return
    nop
# END n_column
naive_column:
# BEGIN n_sum
    beq $zero, $zero, naive_return
    nop
# END n_sum
# BEGIN n_left
    beq $zero, $zero, naive_return
    nop
# END n_left
# BEGIN n_right
    beq $zero, $zero, naive_return
    nop
# END n_right
# BEGIN n_zero_k
    beq $zero, $zero, naive_return
    nop
# END n_zero_k
naive_dot:
# BEGIN n_load_left
    beq $zero, $zero, naive_return
    nop
# END n_load_left
# BEGIN n_load_right
    beq $zero, $zero, naive_return
    nop
# END n_load_right
# BEGIN n_product
    beq $zero, $zero, naive_return
    nop
# END n_product
# BEGIN n_accumulate
    beq $zero, $zero, naive_return
    nop
# END n_accumulate
# BEGIN n_reduce
    beq $zero, $zero, naive_return
    nop
# END n_reduce
naive_store:
# BEGIN n_output_address
    beq $zero, $zero, naive_return
    nop
# END n_output_address
# BEGIN n_store
    beq $zero, $zero, naive_return
    nop
# END n_store
# BEGIN n_next_column
    beq $zero, $zero, naive_return
    nop
# END n_next_column
# BEGIN n_next_row
    beq $zero, $zero, naive_return
    nop
# END n_next_row
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
    jr $ra
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
# BEGIN t_empty
    beq $zero, $zero, tiled_return
    nop
# END t_empty
# BEGIN t_zero_setup
    beq $zero, $zero, tiled_return
    nop
# END t_zero_setup
zero_product:
# BEGIN t_zero_store
    beq $zero, $zero, tiled_return
    nop
# END t_zero_store
# BEGIN t_zero_next
    beq $zero, $zero, tiled_return
    nop
# END t_zero_next
# BEGIN t_reduction_gate
    beq $zero, $zero, tiled_return
    nop
# END t_reduction_gate
block_row:
# BEGIN t_row_bound
    beq $zero, $zero, tiled_return
    nop
# END t_row_bound
row_bound:
# BEGIN t_column_start
    beq $zero, $zero, tiled_return
    nop
# END t_column_start
block_column:
# BEGIN t_column_bound
    beq $zero, $zero, tiled_return
    nop
# END t_column_bound
column_bound:
# BEGIN t_reduction_start
    beq $zero, $zero, tiled_return
    nop
# END t_reduction_start
block_reduction:
# BEGIN t_reduction_bound
    beq $zero, $zero, tiled_return
    nop
# END t_reduction_bound
reduction_bound:
# BEGIN t_row_start
    beq $zero, $zero, tiled_return
    nop
# END t_row_start
blocked_row:
# BEGIN t_inner_column
    beq $zero, $zero, tiled_return
    nop
# END t_inner_column
blocked_column:
# BEGIN t_output_address
    beq $zero, $zero, tiled_return
    nop
# END t_output_address
# BEGIN t_partial
    beq $zero, $zero, tiled_return
    nop
# END t_partial
# BEGIN t_left_address
    beq $zero, $zero, tiled_return
    nop
# END t_left_address
# BEGIN t_right_address
    beq $zero, $zero, tiled_return
    nop
# END t_right_address
# BEGIN t_dot_setup
    beq $zero, $zero, tiled_return
    nop
# END t_dot_setup
blocked_dot:
# BEGIN t_load_pair
    beq $zero, $zero, tiled_return
    nop
# END t_load_pair
# BEGIN t_product
    beq $zero, $zero, tiled_return
    nop
# END t_product
# BEGIN t_accumulate
    beq $zero, $zero, tiled_return
    nop
# END t_accumulate
# BEGIN t_reduce
    beq $zero, $zero, tiled_return
    nop
# END t_reduce
# BEGIN t_store
    beq $zero, $zero, tiled_return
    nop
# END t_store
# BEGIN t_next_column
    beq $zero, $zero, tiled_return
    nop
# END t_next_column
# BEGIN t_next_row
    beq $zero, $zero, tiled_return
    nop
# END t_next_row
# BEGIN t_next_reduction
    beq $zero, $zero, tiled_return
    nop
# END t_next_reduction
# BEGIN t_next_column_tile
    beq $zero, $zero, tiled_return
    nop
# END t_next_column_tile
# BEGIN t_next_row_tile
    beq $zero, $zero, tiled_return
    nop
# END t_next_row_tile
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
    jr $ra
    nop
