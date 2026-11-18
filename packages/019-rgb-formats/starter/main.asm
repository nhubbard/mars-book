# mars:tool bitmap width=512 height=64 unitWidth=16 unitHeight=32 base=static_data
.data
preview: .space 256
preview_end: .word 0x13579bdf
rgb_a_guard: .word 0x31415926
rgb_a: .byte 7,3,1,15,11,9,23,19,17,31,27,25,39,35,33,47,43,41,55,51,49,63,59,57,71,67,65,79,75,73,87,83,81,95,91,89,103,99,97,111,107,105,119,115,113,127,123,121,127,63,-9,-128,64,-10,-127,65,-11,-126,66,-12,-125,67,-13,-124,68,-14,-123,69,-15,-122,70,-16,-121,71,-17,-120,72,-18,-119,73,-19,-118,74,-20,-117,75,-21,-116,76,-22,-115,77,-23,-114,78,-24
    .space 32
rgb_a_end: .word 0x27182818
rgb_b_guard: .word 0x11223344
rgb_b: .space 128
rgb_b_end: .word 0x22334455
packed_a_guard: .word 0x33445566
packed_a: .space 96
packed_a_end: .word 0x44556677
packed_b_guard: .word 0x55667788
packed_b: .space 96
packed_b_end: .word 0x66778899
count: .word 32
packed_count: .word 99
expanded_count: .word 99
caller_end: .word 0x77889900
.align 4
gather_red: .byte 0,64,3,64,6,64,9,64,12,64,15,64,26,64,29,64
gather_green: .byte 1,64,4,64,7,64,10,64,13,64,24,64,27,64,30,64
gather_blue: .byte 2,64,5,64,8,64,11,64,14,64,25,64,28,64,31,64
scatter_first: .byte 0,1,16,2,3,18,4,5,20,6,7,22,8,9,24,10
scatter_last: .byte 20,6,7,22,8,9,24,10,11,26,12,13,28,14,15,30
.text
.globl main
main:
    dla $a0, rgb_a
    dla $a1, packed_a
    dla $t0, count
    lw $a2, 0($t0)
pack_call:
    jal pack_rgb565
    nop
pack_ready:
    nop
pack_continue:
    dla $t0, packed_count
    sw $v0, 0($t0)
    dla $a0, packed_a
    dla $a1, rgb_b
    dla $t0, count
    lw $a2, 0($t0)
unpack_call:
    jal unpack_rgb565
    nop
unpack_ready:
    nop
unpack_continue:
    dla $t0, expanded_count
    sw $v0, 0($t0)
    dla $t0, rgb_a
    dla $t1, rgb_b
    dla $t2, preview
    dla $t3, preview
    daddiu $t3, $t3, 128
    dla $t4, count
    lw $t4, 0($t4)
    beq $t4, $zero, preview_ready
    nop
preview_loop:
    lbu $t5, 0($t0)
    lbu $t6, 1($t0)
    lbu $t7, 2($t0)
    sll $t5, $t5, 16
    sll $t6, $t6, 8
    or $t5, $t5, $t6
    or $t5, $t5, $t7
    sw $t5, 0($t2)
    lbu $t5, 0($t1)
    lbu $t6, 1($t1)
    lbu $t7, 2($t1)
    sll $t5, $t5, 16
    sll $t6, $t6, 8
    or $t5, $t5, $t6
    or $t5, $t5, $t7
    sw $t5, 0($t3)
    daddiu $t0, $t0, 3
    daddiu $t1, $t1, 3
    daddiu $t2, $t2, 4
    daddiu $t3, $t3, 4
    addiu $t4, $t4, -1
    bne $t4, $zero, preview_loop
    nop
preview_ready:
    nop
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

pack_rgb565:
    move $v0, $a2
# BEGIN vector_pack_gate
    beq $zero, $zero, pack_tail
    nop
# END vector_pack_gate
# BEGIN vector_pack_controls
    beq $zero, $zero, pack_return
    nop
# END vector_pack_controls
pack_block:
# BEGIN vector_pack_load
    beq $zero, $zero, pack_return
    nop
# END vector_pack_load
# BEGIN vector_pack_copy
    beq $zero, $zero, pack_return
    nop
# END vector_pack_copy
# BEGIN vector_pack_shuffle
    beq $zero, $zero, pack_return
    nop
# END vector_pack_shuffle
# BEGIN vector_pack_red
    beq $zero, $zero, pack_return
    nop
# END vector_pack_red
# BEGIN vector_pack_green
    beq $zero, $zero, pack_return
    nop
# END vector_pack_green
# BEGIN vector_pack_blue
    beq $zero, $zero, pack_return
    nop
# END vector_pack_blue
# BEGIN vector_pack_join
    beq $zero, $zero, pack_return
    nop
# END vector_pack_join
# BEGIN vector_pack_store
    beq $zero, $zero, pack_return
    nop
# END vector_pack_store
# BEGIN vector_pack_next
    beq $zero, $zero, pack_return
    nop
# END vector_pack_next
pack_tail:
# BEGIN pack_bound
    beq $zero, $zero, pack_return
    nop
# END pack_bound
pack_pixel:
# BEGIN pack_load
    beq $zero, $zero, pack_return
    nop
# END pack_load
# BEGIN pack_red
    beq $zero, $zero, pack_return
    nop
# END pack_red
# BEGIN pack_green
    beq $zero, $zero, pack_return
    nop
# END pack_green
# BEGIN pack_blue
    beq $zero, $zero, pack_return
    nop
# END pack_blue
# BEGIN pack_join
    beq $zero, $zero, pack_return
    nop
# END pack_join
# BEGIN pack_store
    beq $zero, $zero, pack_return
    nop
# END pack_store
# BEGIN pack_next
    beq $zero, $zero, pack_return
    nop
# END pack_next
pack_return:
    jr $ra
    nop

unpack_rgb565:
    move $v0, $a2
# BEGIN vector_unpack_gate
    beq $zero, $zero, unpack_tail
    nop
# END vector_unpack_gate
# BEGIN vector_unpack_controls
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_controls
unpack_block:
# BEGIN vector_unpack_load
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_load
# BEGIN vector_unpack_fields
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_fields
# BEGIN vector_unpack_red
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_red
# BEGIN vector_unpack_green
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_green
# BEGIN vector_unpack_blue
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_blue
# BEGIN vector_unpack_compact
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_compact
# BEGIN vector_unpack_interleave
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_interleave
# BEGIN vector_unpack_copy
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_copy
# BEGIN vector_unpack_shuffle
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_shuffle
# BEGIN vector_unpack_store
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_store
# BEGIN vector_unpack_next
    beq $zero, $zero, unpack_return
    nop
# END vector_unpack_next
unpack_tail:
# BEGIN unpack_bound
    beq $zero, $zero, unpack_return
    nop
# END unpack_bound
unpack_pixel:
# BEGIN unpack_load
    beq $zero, $zero, unpack_return
    nop
# END unpack_load
# BEGIN unpack_red
    beq $zero, $zero, unpack_return
    nop
# END unpack_red
# BEGIN unpack_green
    beq $zero, $zero, unpack_return
    nop
# END unpack_green
# BEGIN unpack_blue
    beq $zero, $zero, unpack_return
    nop
# END unpack_blue
# BEGIN unpack_store
    beq $zero, $zero, unpack_return
    nop
# END unpack_store
# BEGIN unpack_next
    beq $zero, $zero, unpack_return
    nop
# END unpack_next
unpack_return:
    jr $ra
    nop
