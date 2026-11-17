# mars:tool bitmap width=128 height=64 unitWidth=8 unitHeight=8 base=static_data
# Default memory layout. Keep the frame first and preserve all supplied objects.
.data
.align 2
frame:         .space 512
frame_guard:   .word 0x13579bdf
sprite:        .byte 22, 60, 126, -37, -1, 36, 66, -127
sprite_end:
mirrored_rows: .space 8
rows_guard:    .word 0x2468ace0

.text
.globl main
main:
# BEGIN STUDENT PROGRAM
# END STUDENT PROGRAM

done:
    li $v0, 10
    syscall
