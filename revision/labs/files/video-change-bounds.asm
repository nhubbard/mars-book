# Independent changed-pixel rectangle exercise for Chapter 25.
.data
.align 4
bounds_previous: .word 1,2,3,4,5,6
    .byte -91:16392
bounds_current: .word 1,258,3,4,1029,6
    .byte -91:16392
.align 3
bounds_settings: .dword 2,3
    .byte -91:16
bounds_result: .byte -91:64
bounds_status: .word -1
.align 3
bounds_count: .dword 0
bounds_workspace: .space 8192
.text
.globl main
main:
    dla $a0, bounds_previous
    dla $a1, bounds_current
    dla $a2, bounds_result
    dla $a3, bounds_settings
    balc video_change_bounds
    nop
bounds_demo_return:
    dla $t0, bounds_status
    sw $v0, 0($t0)
    dla $t0, bounds_count
    sd $v1, 0($t0)
    li $v0, 10
    syscall
bounds_function_return:
    nop
# a0/a1: read-only, word-aligned native arrays, possibly equal.
# a2: separate eight-byte-aligned 32-byte result; a3: native u64 width,height.
# Width/height 1..64. Compare all 32 bits. Result: x0,y0,x1,y1, exclusive ends.
# No changes: four zero doublewords. Success v0=0, v1=changed-word count.
# Invalid dimensions/alignment: v0=1,v1=0, no image access or caller writes.
# Preserve saved registers, fp, sp and return continuation. No I/O.
video_change_bounds:
    break
