# mars:tool bitmap width=128 height=64 unitWidth=8 unitHeight=8 base=static_data initial=true
.data
bitmap_origin: .word 0x00444444
frame_a: .space 512
frame_a_end: .word 0x00555555
frame_b_guard: .word 0x13579bdf
frame_b: .space 512
frame_b_end: .word 0x2468ace0
sprite_a_guard: .word 0x31415926
sprite_a:
    .word 0x00ff00ff,0x00ff0000,0x00ff00ff,0x0000ff00,0x00ff00ff,0x00111111,0x00111111
    .word 0x000000ff,0,0x00ffff00,0x01ff00ff,0x00ff0000,0x00111111,0x00111111
    .word 0x00ff00ff,0x00ffffff,0x0000ffff,0x00ff00ff,0x0000ff00,0x00111111,0x00111111
    .word 0x00ff0000,0x00ff00ff,0x000000ff,0x00ffff00,0x00ff00ff,0x00111111,0x00111111
    .space 400
sprite_a_end: .word 0x27182818
sprite_b_guard: .word 0x12345678
sprite_b: .space 512
sprite_b_end: .word 0x23456789
.align 3
frame_desc_a: .dword 0
    .word 8,6,16
frame_desc_a_guard: .word 0x34567890
frame_desc_b: .dword 0
    .word 7,5,13
frame_desc_b_guard: .word 0x45678901
sprite_desc_a: .dword 0
    .word 5,4,7,0x00ff00ff
sprite_desc_a_guard: .word 0x56789012
    .word 0
sprite_desc_b: .dword 0
    .word 0,0,0,0
sprite_desc_b_guard: .word 0x67890123
    .word 0
position: .word -2,1
copied: .word -1
visible: .word -1
state_end: .word 0x78901234
.text
.globl main
main:
    dla $s0, frame_a
    li $t0, 0x00444444
    li $t1, 128
fill_all:
    sw $t0, 0($s0)
    daddiu $s0, $s0, 4
    addiu $t1, $t1, -1
    bne $t1, $zero, fill_all
    nop
    dla $s0, frame_a
    li $t2, 6
    li $t4, 0x00102030
fill_row:
    move $t0, $s0
    li $t3, 8
fill_active:
    sw $t4, 0($t0)
    daddiu $t0, $t0, 4
    addiu $t3, $t3, -1
    bne $t3, $zero, fill_active
    nop
    daddiu $s0, $s0, 64
    addiu $t2, $t2, -1
    bne $t2, $zero, fill_row
    nop
    dla $a0, frame_desc_a
    dla $t0, frame_a
    sd $t0, 0($a0)
    dla $a1, sprite_desc_a
    dla $t0, sprite_a
    sd $t0, 0($a1)
    dla $t0, position
    lw $a2, 0($t0)
    lw $a3, 4($t0)
blit_call:
    jal blit_sprite
    nop
blit_ready:
    nop
blit_continue:
    dla $t0, copied
    sw $v0, 0($t0)
    sw $v1, 4($t0)
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

blit_sprite:
    daddiu $sp, $sp, -64
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    move $v0, $zero
    move $v1, $zero
# BEGIN dimensions
# END dimensions
# BEGIN left
# END left
clip_left_ready:
# BEGIN top
# END top
clip_top_ready:
# BEGIN right
# END right
clip_right_ready:
# BEGIN bottom
# END bottom
clip_bottom_ready:
# BEGIN extent
# END extent
# BEGIN source_origin
# END source_origin
# BEGIN bases
# END bases
# BEGIN strides
# END strides
# BEGIN destination_address
# END destination_address
# BEGIN source_address
# END source_address
# BEGIN key_extent
# END key_extent
blit_row:
# BEGIN row
# END row
blit_pixel:
# BEGIN pixel
# END pixel
blit_skip:
# BEGIN advance
# END advance
# BEGIN next_row
# END next_row
blit_restore:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    daddiu $sp, $sp, 64
    jr $ra
    nop
