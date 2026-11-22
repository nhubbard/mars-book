# mars:tool bitmap width=128 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame:         .space 512
frame_guard:   .word 0x13579bdf
sprite:        .byte 0x16, 0x3c, 0x7e, 0xdb, 0xff, 0x24, 0x42, 0x81
mirrored_rows: .space 8
rows_guard:    .word 0x2468ace0

.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $s0, sprite
    la $s1, mirrored_rows
    la $s2, frame
    li $s3, 8
row_loop:
    lbu $s4, 0($s0)
    move $a0, $s4
    balc reverse_byte
row_reversed:
    sb $v0, 0($s1)
    move $a0, $v0
    daddiu $a1, $s2, 32
    balc draw_row
right_row_drawn:
    move $a0, $s4
    move $a1, $s2
    balc draw_row
row_drawn:
    daddiu $s0, $s0, 1
    daddiu $s1, $s1, 1
    daddiu $s2, $s2, 64
    addiu $s3, $s3, -1
    bnezc $s3, row_loop
    nop
frame_complete:
    li $v0, 10
    syscall

draw_row:
    andi $t0, $a0, 0xff
    li $t1, 8
draw_pixel:
    andi $t2, $t0, 0x80
    move $t3, $zero
    beqzc $t2, store_pixel
    nop
    li $t3, 0x00ffffff
store_pixel:
    sw $t3, 0($a1)
    daddiu $a1, $a1, 4
    sll $t0, $t0, 1
    addiu $t1, $t1, -1
    bnezc $t1, draw_pixel
    nop
draw_row_return:
    jrc $ra

reverse_byte:
    andi $t0, $a0, 0xff
    move $v0, $zero
    li $t1, 8
reverse_bit:
    sll $v0, $v0, 1
    andi $t2, $t0, 1
    or $v0, $v0, $t2
    srl $t0, $t0, 1
    addiu $t1, $t1, -1
    bnezc $t1, reverse_bit
    nop
reverse_byte_return:
    jrc $ra
