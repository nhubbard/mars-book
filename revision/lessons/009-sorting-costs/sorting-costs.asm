# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame: .space 256
frame_guard: .word 0x13579bdf
values: .word 2, -2, 3, -4, 0, 1, -3, -1
values_guard: .word 0x2468ace0
count: .word 8
count_guard: .word 0x10203040
algorithm: .word 0
algorithm_guard: .word 0x50607080
phase: .word -1
phase_guard: .word 0x12345678
swap_count: .word -1
swaps_guard: .word 0x31415926
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $s0, values
    lw $s1, count
    sw $zero, phase
    sw $zero, swap_count
    move $a0, $s0
    move $a1, $s1
    balc render
sort_begin:
    lw $t0, algorithm
    beqzc $t0, choose_insertion
    nop
    move $a0, $s0
    move $a1, $s1
    balc selection_sort
    bc sort_end
choose_insertion:
    move $a0, $s0
    move $a1, $s1
    balc insertion_sort
sort_end:
    li $t0, 2
    sw $t0, phase
    move $a0, $s0
    move $a1, $s1
    balc render
done:
    li $v0, 10
    syscall

insertion_sort:
    daddiu $sp, $sp, -48
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    sd $s3, 32($sp)
    move $s0, $a0
    move $s1, $a1
    li $s2, 1
insertion_outer:
    bgec $s2, $s1, insertion_return
    nop
    move $s3, $s2
insertion_inner:
    beqzc $s3, insertion_next
    nop
    dsll $t0, $s3, 2
    daddu $t0, $s0, $t0
    lw $t1, 0($t0)
    lw $t2, -4($t0)
insertion_compare:
    bgec $t1, $t2, insertion_next
insertion_compare_end:
    nop
    sw $t1, -4($t0)
    sw $t2, 0($t0)
insertion_exchanged:
    move $a0, $s0
    move $a1, $s1
    balc publish_exchange
    addiu $s3, $s3, -1
    bc insertion_inner
insertion_next:
    addiu $s2, $s2, 1
    bc insertion_outer
insertion_return:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    ld $s3, 32($sp)
    daddiu $sp, $sp, 48
    jrc $ra
insertion_sort_end:

selection_sort:
    daddiu $sp, $sp, -32
    sd $ra, 0($sp)
    sd $s0, 8($sp)
    sd $s1, 16($sp)
    sd $s2, 24($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $zero
selection_outer:
    addiu $t1, $s2, 1
    bgec $t1, $s1, selection_return
    nop
    move $t0, $s2
    dsll $t3, $s2, 2
    daddu $t3, $s0, $t3
    lw $t2, 0($t3)
selection_scan:
    bgec $t1, $s1, selection_exchange
    nop
    dsll $t3, $t1, 2
    daddu $t3, $s0, $t3
    lw $t4, 0($t3)
selection_compare:
    bgec $t4, $t2, selection_scan_next
selection_compare_end:
    nop
    move $t2, $t4
    move $t0, $t1
selection_scan_next:
    addiu $t1, $t1, 1
    bc selection_scan
selection_exchange:
    beqc $t0, $s2, selection_next
    nop
    dsll $t3, $s2, 2
    daddu $t3, $s0, $t3
    lw $t4, 0($t3)
    sw $t2, 0($t3)
    dsll $t3, $t0, 2
    daddu $t3, $s0, $t3
    sw $t4, 0($t3)
selection_exchanged:
    move $a0, $s0
    move $a1, $s1
    balc publish_exchange
selection_next:
    addiu $s2, $s2, 1
    bc selection_outer
selection_return:
    ld $ra, 0($sp)
    ld $s0, 8($sp)
    ld $s1, 16($sp)
    ld $s2, 24($sp)
    daddiu $sp, $sp, 32
    jrc $ra
selection_sort_end:

publish_exchange:
    daddiu $sp, $sp, -16
    sd $ra, 0($sp)
    li $t0, 1
    sw $t0, phase
    lw $t0, swap_count
    addiu $t0, $t0, 1
    sw $t0, swap_count
    balc render
    ld $ra, 0($sp)
    daddiu $sp, $sp, 16
    jrc $ra

render:
    la $t2, frame
    move $t0, $zero
    li $t9, 8
render_row:
    move $t1, $zero
render_column:
    move $t4, $zero
    bgec $t1, $a1, render_store
    nop
    dsll $t5, $t1, 2
    daddu $t5, $a0, $t5
    lw $t6, 0($t5)
    li $t7, 3
    subu $t7, $t7, $t6
    bltc $t0, $t7, render_store
    nop
    li $t4, 0x00ffffff
render_store:
    sw $t4, 0($t2)
    daddiu $t2, $t2, 4
    addiu $t1, $t1, 1
    bltc $t1, $t9, render_column
    nop
    addiu $t0, $t0, 1
    bltc $t0, $t9, render_row
    nop
sort_ready:
    jrc $ra
