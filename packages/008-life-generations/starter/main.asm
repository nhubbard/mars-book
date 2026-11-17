# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame: .space 256
frame_guard: .word 0x13579bdf
seed: .byte 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
seed_guard: .word 0x2468ace0
board_a: .space 100
board_a_guard: .word 0x10203040
board_b: .space 100
board_b_guard: .word 0x50607080
generations: .word 2
generations_guard: .word 0x12345678
generation: .word -1
generation_guard: .word 0x31415926
.text
.globl main
main:
# BEGIN STUDENT PROGRAM
# END STUDENT PROGRAM

generation_ready:
    nop
done:
    li $v0, 10
    syscall
