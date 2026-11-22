# mars:tool bitmap width=128 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame:       .space 512
frame_guard: .word 0x13579bdf
sprite:      .byte 0x16, 0x3c, 0x7e, 0xdb, 0xff, 0x24, 0x42, 0x81
rows_guard:  .word 0x2468ace0

.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $a0, sprite
    la $a1, frame
    balc draw_vertical
after_vertical:
    la $a0, sprite
    la $a1, frame
    balc draw_vertical
after_repeat:
    li $v0, 10
    syscall

# Draw the source rows in reverse vertical order into columns 8..15.
# Preserve columns 0..7, the source, guards and the chapter's saved registers.
draw_vertical:
    break

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
