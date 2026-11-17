# mars:tool bitmap width=64 height=64 unitWidth=8 unitHeight=8 base=static_data
.data
.align 2
frame: .space 256
frame_guard: .word 0x13579bdf
endpoints: .word 1, 1, 6, 4
endpoints_guard: .word 0x2468ace0
color: .word 0x00ffffff
color_guard: .word 0x10203040
.text
.globl main
main:
# BEGIN STUDENT PROGRAM
# END STUDENT PROGRAM

done:
    li $v0, 10
    syscall
