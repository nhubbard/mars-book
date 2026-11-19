# Independent public grading storage; no video implementation or keys.
.data 0x100000000
.align 4
videoCheck_a: .space 262208
.align 4
videoCheck_b: .space 262208
.align 4
videoCheck_out: .space 262208
.align 4
videoCheck_previous: .space 16448
.align 4
videoCheck_scratch: .space 16448
.align 4
videoCheck_directory: .space 2176
.align 4
videoCheck_context: .space 160
.align 4
videoCheck_stack: .space 32768
videoCheck_path: .asciiz "animation.mlv"
    .space 114
.text
videoCheck_return:
    nop
