# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame:           .space 256
frame_guard:     .word 0x13579bdf
endpoints:       .word 1, 1, 6, 4
endpoints_guard: .word 0x2468ace0
color:           .word 0x00ffffff
color_guard:     .word 0x10203040
line_count:      .word 0
count_guard:     .word 0x31415926
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $a0, endpoints
    la $a1, frame
    lw $a2, color
    balc draw_line
line_complete:
    sw $v0, line_count
    li $v0, 10
    syscall

draw_line:
    lw $t0, 0($a0)
    lw $t1, 4($a0)
    lw $t2, 8($a0)
    lw $t3, 12($a0)
    slt $t9, $t2, $t0
    bnezc $t9, swap_endpoints
    nop
    bnec $t0, $t2, ordered
    nop
    slt $t9, $t3, $t1
    beqzc $t9, ordered
    nop
swap_endpoints:
    move $v1, $t0
    move $t0, $t2
    move $t2, $v1
    move $v1, $t1
    move $t1, $t3
    move $t3, $v1
ordered:
    subu $t4, $t2, $t0
    li $t6, 1
    subu $t5, $t3, $t1
    bgezc $t5, positive_delta
    nop
    li $t6, -1
    bc differences_ready
positive_delta:
    subu $t5, $zero, $t5
differences_ready:
    addu $t7, $t4, $t5
    move $v0, $zero

line_pixel:
    sll $v1, $t1, 3
    addu $v1, $v1, $t0
    dsll $v1, $v1, 2
    daddu $v1, $a1, $v1
    sw $a2, 0($v1)
    addiu $v0, $v0, 1
    bnec $t0, $t2, line_decide
    nop
    beqc $t1, $t3, line_return
    nop
line_decide:
    sll $t8, $t7, 1
    bltc $t8, $t5, skip_x
    nop
    addu $t7, $t7, $t5
    addiu $t0, $t0, 1
skip_x:
    bltc $t4, $t8, line_next
    nop
    addu $t7, $t7, $t4
    addu $t1, $t1, $t6
line_next:
    bc line_pixel
line_return:
    jrc $ra
