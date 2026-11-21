.data
.align 4
rgb_guard: .word 0x13579bdf
rgb: .byte 123,45,67,0,0,0,255,255,255,128,64,247,7,3,7,8,4,8,248,252,248,31,127,223
    .space 104
rgb_end: .word 0x2468ace0
packed_guard: .word 0x31415926
packed: .space 96
packed_end: .word 0x27182818
count: .word 8
completed: .word -1
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, rgb
    dla $a1, packed
    lw $a2, count
    balc pack_rgb555
    nop
pack555_done:
    nop
    sw $v0, completed
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# Implement Chapter 14's independent packing contract.
pack_rgb555:
    break
