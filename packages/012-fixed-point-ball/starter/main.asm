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
    li $t0, DEMO
    bne $t0, $zero, overflow_demo
    nop
# BEGIN initialize
# END initialize
publish:
# BEGIN publish
# END publish
frame_ready:
    nop
frame_continue:
# BEGIN advance
# END advance
    # Fallback exit keeps unfinished guided versions safe to execute.
    beq $zero, $zero, done
    nop

overflow_demo:
    li $a0, 0x7fffff00
    li $a1, 0x200
    jal add_fixed_probe
    nop
overflow_ready:
    nop
done:
    li $v0, 10
    syscall

advance_axis:
# BEGIN axis
# END axis
axis_return:
    jr $ra
    nop

render_ball:
# BEGIN render
# END render
render_return:
    jr $ra
    nop

add_fixed_probe:
# BEGIN probe
# END probe
probe_return:
    jr $ra
    nop
