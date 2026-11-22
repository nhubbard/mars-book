# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame:             .space 256
frame_guard:       .word 0x13579bdf
seed:
    .byte 0,0,0,0,0,0,0,0
    .byte 0,0,0,0,0,0,0,0
    .byte 0,0,0,0,0,0,0,0
    .byte 0,0,1,1,1,0,0,0
    .byte 0,0,0,0,0,0,0,0
    .byte 0,0,0,0,0,0,0,0
    .byte 0,0,0,0,0,0,0,0
    .byte 0,0,0,0,0,0,0,0
seed_guard:        .word 0x2468ace0
board_a:           .space 100
board_a_guard:     .word 0x10203040
board_b:           .space 100
board_b_guard:     .word 0x31415926
generations:       .word 2
generations_guard: .word 0x55667788
generation:        .word -1
generation_guard:  .word 0x11223344
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $s0, board_a
    la $s1, board_b
    move $s2, $zero
    lw $s3, generations
    la $s4, seed
    daddiu $s5, $s0, 11
    li $s6, 8
seed_row:
    li $s7, 8
seed_cell:
    lbu $t0, 0($s4)
    sb $t0, 0($s5)
    daddiu $s4, $s4, 1
    daddiu $s5, $s5, 1
    addiu $s7, $s7, -1
    bnezc $s7, seed_cell
    nop
    daddiu $s5, $s5, 2
    addiu $s6, $s6, -1
    bnezc $s6, seed_row
    nop
render_generation:
    move $a0, $s0
    la $a1, frame
    balc render_board
    sw $s2, generation
generation_ready:
    beqc $s2, $s3, life_done
    nop
    move $a0, $s0
    move $a1, $s1
    balc life_step
generation_built:
    move $t0, $s0
    move $s0, $s1
    move $s1, $t0
    addiu $s2, $s2, 1
    bc render_generation
life_done:
    li $v0, 10
    syscall

life_step:
    daddiu $sp, $sp, -48
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    sd $s4, 40($sp)
    daddiu $s0, $a0, 11
    daddiu $s1, $a1, 11
    li $s2, 8
    move $s4, $zero
step_row:
    li $s3, 8
step_cell:
    move $a0, $s0
    balc count_neighbors
    li $t0, 3
    li $t1, 1
    beqc $v0, $t0, store_cell
    nop
    li $t0, 2
    move $t1, $zero
    bnec $v0, $t0, store_cell
    nop
    lbu $t1, 0($s0)
store_cell:
    sb $t1, 0($s1)
    addu $s4, $s4, $t1
    daddiu $s0, $s0, 1
    daddiu $s1, $s1, 1
    addiu $s3, $s3, -1
    bnezc $s3, step_cell
    nop
    daddiu $s0, $s0, 2
    daddiu $s1, $s1, 2
    addiu $s2, $s2, -1
    bnezc $s2, step_row
    nop
    move $v0, $s4
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    ld $s4, 40($sp)
    daddiu $sp, $sp, 48
    jrc $ra

count_neighbors:
    lbu $v0, -11($a0)
    lbu $t0, -10($a0)
    addu $v0, $v0, $t0
    lbu $t0, -9($a0)
    addu $v0, $v0, $t0
    lbu $t0, -1($a0)
    addu $v0, $v0, $t0
    lbu $t0, 1($a0)
    addu $v0, $v0, $t0
    lbu $t0, 9($a0)
    addu $v0, $v0, $t0
    lbu $t0, 10($a0)
    addu $v0, $v0, $t0
    lbu $t0, 11($a0)
    addu $v0, $v0, $t0
    jrc $ra

render_board:
    daddiu $t0, $a0, 11
    move $t1, $a1
    li $t2, 8
    li $t5, 0x00ffffff
render_row:
    li $t3, 8
render_cell:
    lbu $t4, 0($t0)
    subu $t4, $zero, $t4
    and $t4, $t4, $t5
    sw $t4, 0($t1)
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, 4
    addiu $t3, $t3, -1
    bnezc $t3, render_cell
    nop
    daddiu $t0, $t0, 2
    addiu $t2, $t2, -1
    bnezc $t2, render_row
    nop
    jrc $ra
