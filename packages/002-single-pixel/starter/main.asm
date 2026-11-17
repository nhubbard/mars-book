# mars:tool bitmap width=64 height=64 unitWidth=32 unitHeight=32 base=static_data
# Keep pixel as the first static data word in the default memory layout.
.data
.align 2
pixel:    .word 0
neighbor: .word 0x13579bdf

.text
.globl main
main:
# BEGIN STUDENT PROGRAM
# END STUDENT PROGRAM

done:
    li $v0, 10
    syscall
