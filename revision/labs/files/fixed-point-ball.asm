# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.eqv DEMO 0
.data
.align 2
frame: .space 256
frame_guard: .word 0x13579bdf
spare_frame: .space 256
spare_guard: .word 0x2468ace0
seed_x: .word 320
seed_y: .word 448
seed_vx: .word 192
seed_vy: .word 320
seed_guard: .word 0x31415926
steps: .word 8
round_mode: .word 0
options_guard: .word 0x27182818
x_raw: .word -1
y_raw: .word -1
vx_raw: .word -1
vy_raw: .word -1
tick: .word -1
pixel_id: .word -1
state_guard: .word 0x76543210
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    li $t0, DEMO
    bnezc $t0, overflow_demo
    nop
    la $t0, seed_x
    lw $s0, 0($t0)
    lw $s1, 4($t0)
    lw $s2, 8($t0)
    lw $s3, 12($t0)
    lw $s4, steps
    lw $s6, round_mode
    move $s5, $zero
    la $s7, frame
publish:
    la $t0, x_raw
    sw $s0, 0($t0)
    sw $s1, 4($t0)
    sw $s2, 8($t0)
    sw $s3, 12($t0)
    sw $s5, 16($t0)
    move $a0, $s7
    move $a1, $s0
    move $a2, $s1
    move $a3, $s6
    balc render_ball
    sw $v0, pixel_id
frame_ready:
    nop
frame_continue:
    beqzc $s4, done
    nop
    move $a0, $s0
    move $a1, $s2
    balc advance_axis
    move $s0, $v0
    move $s2, $v1
    move $a0, $s1
    move $a1, $s3
    balc advance_axis
    move $s1, $v0
    move $s3, $v1
    addiu $s4, $s4, -1
    addiu $s5, $s5, 1
    bc publish

overflow_demo:
    li $a0, 0x7fffff00
    li $a1, 0x200
    balc add_fixed_probe
overflow_ready:
    nop
done:
    li $v0, 10
    syscall

# Inputs and outputs are raw signed words with eight fractional bits.
advance_axis:
    addu $v0, $a0, $a1
    move $v1, $a1
    blezc $v0, reflect_low
    nop
    li $t0, 1792
    bgec $v0, $t0, reflect_high
    nop
    jrc $ra
reflect_low:
    subu $v0, $zero, $v0
    subu $v1, $zero, $v1
    jrc $ra
reflect_high:
    li $t0, 3584
    subu $v0, $t0, $v0
    subu $v1, $zero, $v1
    jrc $ra

# a0 = complete destination pointer; a1/a2 = x/y raw; a3 = rounding mode.
render_ball:
    sll $t0, $a3, 7
    addu $t1, $a1, $t0
    addu $t2, $a2, $t0
    srl $t1, $t1, 8
    srl $t2, $t2, 8
    sll $t2, $t2, 3
    addu $v0, $t2, $t1
    move $t3, $a0
    move $t4, $zero
    li $t5, 0x00ffffff
    li $t7, 64
render_pixel:
    move $t6, $zero
    bnec $t4, $v0, store_pixel
    nop
    move $t6, $t5
store_pixel:
    sw $t6, 0($t3)
    daddiu $t3, $t3, 4
    addiu $t4, $t4, 1
    bltc $t4, $t7, render_pixel
    nop
    jrc $ra

# v0 = sign-extended wrapped word sum; v1 = exact doubleword sum.
add_fixed_probe:
    addu $v0, $a0, $a1
    daddu $v1, $a0, $a1
    jrc $ra
