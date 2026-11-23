# mars:tool bitmap width=512 height=64 unitWidth=16 unitHeight=32 base=static_data
.data
preview: .space 256
preview_end: .word 0x13579bdf
rgb_a_guard: .word 0x31415926
rgb_a: .byte 123,45,67,0,0,0,255,255,255,128,64,247,7,3,7,8,4,8,248,252,248,31,127,223
    .byte 71,67,65,79,75,73,87,83,81,95,91,89,103,99,97,111,107,105,119,115,113,127,123,121,127,63,-9,-128,64,-10,-127,65,-11,-126,66,-12,-125,67,-13,-124,68,-14,-123,69,-15,-122,70,-16,-121,71,-17,-120,72,-18,-119,73,-19,-118,74,-20,-117,75,-21,-116,76,-22,-115,77,-23,-114,78,-24
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
count: .word 7
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
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, rgb_a
    dla $a1, packed_a
    dla $t0, count
    lw $a2, 0($t0)
pack_call:
    balc pack_rgb565
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
    balc unpack_rgb565
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
    beqzc $t4, preview_ready
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
    bnezc $t4, preview_loop
    nop
preview_ready:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

pack_rgb565:
    move $v0, $a2
    sltiu $t0, $a2, 8
    bnezc $t0, pack_tail
    nop
    dla $t0, gather_red
    ld.b $w16, 0($t0)
    dla $t0, gather_green
    ld.b $w17, 0($t0)
    dla $t0, gather_blue
    ld.b $w18, 0($t0)
pack_block:
    ld.b $w0, 0($a0)
    ld.b $w1, 8($a0)
pack_loaded:
    move.v $w4, $w16
    move.v $w5, $w17
    move.v $w6, $w18
    vshf.b $w4, $w1, $w0
    vshf.b $w5, $w1, $w0
    vshf.b $w6, $w1, $w0
pack_gathered:
    srli.h $w4, $w4, 3
    slli.h $w4, $w4, 11
    srli.h $w5, $w5, 2
    slli.h $w5, $w5, 5
    srli.h $w6, $w6, 3
    or.v $w4, $w4, $w5
    or.v $w4, $w4, $w6
pack_fields:
    st.b $w4, 0($a1)
    daddiu $a0, $a0, 24
    daddiu $a1, $a1, 16
    addiu $a2, $a2, -8
    sltiu $t0, $a2, 8
    beqzc $t0, pack_block
    nop
pack_tail:
    beqzc $a2, pack_return
    nop
pack_pixel:
    lbu $t0, 0($a0)
    lbu $t1, 1($a0)
    lbu $t2, 2($a0)
    srl $t0, $t0, 3
    sll $t0, $t0, 11
    srl $t1, $t1, 2
    sll $t1, $t1, 5
    srl $t2, $t2, 3
    or $t0, $t0, $t1
    or $t0, $t0, $t2
    sb $t0, 0($a1)
    srl $t0, $t0, 8
    sb $t0, 1($a1)
    daddiu $a0, $a0, 3
    daddiu $a1, $a1, 2
    addiu $a2, $a2, -1
    bnezc $a2, pack_pixel
    nop
pack_return:
    jrc $ra
    nop

unpack_rgb565:
    move $v0, $a2
    sltiu $t0, $a2, 8
    bnezc $t0, unpack_tail
    nop
    dla $t0, scatter_first
    ld.b $w16, 0($t0)
    dla $t0, scatter_last
    ld.b $w17, 0($t0)
unpack_block:
    ld.b $w0, 0($a0)
    srli.h $w1, $w0, 11
    srli.h $w2, $w0, 5
    ldi.h $w7, 63
    and.v $w2, $w2, $w7
    ldi.h $w7, 31
    and.v $w3, $w0, $w7
    srli.h $w4, $w1, 2
    slli.h $w1, $w1, 3
    or.v $w1, $w1, $w4
    srli.h $w4, $w2, 4
    slli.h $w2, $w2, 2
    or.v $w2, $w2, $w4
    srli.h $w4, $w3, 2
    slli.h $w3, $w3, 3
    or.v $w3, $w3, $w4
    pckev.b $w1, $w1, $w1
    pckev.b $w2, $w2, $w2
    ilvr.b $w4, $w2, $w1
    move.v $w5, $w16
    move.v $w6, $w17
    vshf.b $w5, $w3, $w4
    vshf.b $w6, $w3, $w4
    st.b $w5, 0($a1)
    st.b $w6, 8($a1)
    daddiu $a0, $a0, 16
    daddiu $a1, $a1, 24
    addiu $a2, $a2, -8
    sltiu $t0, $a2, 8
    beqzc $t0, unpack_block
    nop
unpack_tail:
    beqzc $a2, unpack_return
    nop
unpack_pixel:
    lbu $t0, 0($a0)
    lbu $t1, 1($a0)
    sll $t1, $t1, 8
    or $t0, $t0, $t1
    srl $t1, $t0, 11
    sll $t2, $t1, 3
    srl $t1, $t1, 2
    or $t1, $t1, $t2
    srl $t2, $t0, 5
    andi $t2, $t2, 63
    sll $t3, $t2, 2
    srl $t2, $t2, 4
    or $t2, $t2, $t3
    andi $t3, $t0, 31
    sll $t4, $t3, 3
    srl $t3, $t3, 2
    or $t3, $t3, $t4
    sb $t1, 0($a1)
    sb $t2, 1($a1)
    sb $t3, 2($a1)
    daddiu $a0, $a0, 2
    daddiu $a1, $a1, 3
    addiu $a2, $a2, -1
    bnezc $a2, unpack_pixel
    nop
unpack_return:
    jrc $ra
    nop
