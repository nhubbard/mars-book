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
    balc draw_dashed
line_complete:
    sw $v0, line_count
    li $v0, 10
    syscall

# Draw alternating cells of the chapter's canonical raster, starting with cell zero.
# a0: address of x0,y0,x1,y1 words, each in 0..7
# a1: base of 64 framebuffer words; a2: RGB color in 0..0x00ffffff
# v0: number of painted cells. Preserve unselected cells, inputs and saved state.
# Replace the failing body. An equivalent algorithm is allowed.
draw_dashed:
    break
