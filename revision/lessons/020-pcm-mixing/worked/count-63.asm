.data
pcm_a_guard: .word 0x13579bdf
pcm_a: .half 0,1000,-1000,30000,-30000,32767,-32768,20000,-20000,32767,-32768,12345,-12345,1,-1,0
    .space 160
pcm_a_end: .word 0x2468ace0
pcm_b_guard: .word 0x31415926
pcm_b: .half 0,2000,-2000,10000,-10000,0,0,20000,-20000,1,-1,-12345,12345,-1,1,32767
    .space 160
pcm_b_end: .word 0x27182818
pcm_c_guard: .word 0x11223344
pcm_c: .space 192
pcm_c_end: .word 0x22334455
pcm_d_guard: .word 0x33445566
pcm_d: .space 192
pcm_d_end: .word 0x44556677
count: .word 63
processed: .word 99
clipped: .word 99
wrapped_count: .word 99
caller_end: .word 0x55667788
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, pcm_a
    dla $a1, pcm_b
    dla $a2, pcm_c
    dla $t0, count
    lw $a3, 0($t0)
mix_call:
    balc mix_pcm16
    nop
mix_ready:
    nop
mix_continue:
    dla $t0, processed
    sw $v0, 0($t0)
    sw $v1, 4($t0)
    dla $a0, pcm_a
    dla $a1, pcm_b
    dla $a2, pcm_d
    dla $t0, count
    lw $a3, 0($t0)
wrap_call:
    balc mix_wrap16
    nop
wrap_ready:
    nop
wrap_continue:
    dla $t0, wrapped_count
    sw $v0, 0($t0)
comparison_ready:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

mix_pcm16:
    move $v0, $a3
    move $v1, $zero
    sltiu $t0, $a3, 8
    bnezc $t0, mix_tail
    nop
mix_block:
    ld.b $w0, 0($a0)
    ld.b $w1, 0($a1)
    addv.h $w2, $w0, $w1
    adds_s.h $w3, $w0, $w1
mix_saturated:
    ceq.h $w4, $w2, $w3
    nor.v $w4, $w4, $w4
mix_clipping_mask:
    pcnt.d $w5, $w4
mix_population:
    copy_s.d $t0, $w5[0]
    copy_s.d $t1, $w5[1]
    daddu $t0, $t0, $t1
    dsrl $t0, $t0, 4
    daddu $v1, $v1, $t0
mix_store_block:
    st.b $w3, 0($a2)
mix_after_block:
    daddiu $a0, $a0, 16
    daddiu $a1, $a1, 16
    daddiu $a2, $a2, 16
    addiu $a3, $a3, -8
    sltiu $t0, $a3, 8
    beqzc $t0, mix_block
    nop
mix_tail:
    beqzc $a3, mix_return
    nop
mix_sample:
    lbu $t0, 0($a0)
    lbu $t1, 1($a0)
    sll $t1, $t1, 8
    or $t0, $t0, $t1
    seh $t0, $t0
    lbu $t1, 0($a1)
    lbu $t2, 1($a1)
    sll $t2, $t2, 8
    or $t1, $t1, $t2
    seh $t1, $t1
    addu $t0, $t0, $t1
    li $t1, 32767
    slt $t2, $t1, $t0
    bnezc $t2, mix_high
    nop
    li $t1, -32768
    slt $t2, $t0, $t1
    bnezc $t2, mix_low
    nop
    bc mix_store
    nop
mix_high:
    li $t0, 32767
    bc mix_clipped
    nop
mix_low:
    li $t0, -32768
mix_clipped:
    addiu $v1, $v1, 1
mix_store:
    sb $t0, 0($a2)
    srl $t1, $t0, 8
    sb $t1, 1($a2)
    daddiu $a0, $a0, 2
    daddiu $a1, $a1, 2
    daddiu $a2, $a2, 2
    addiu $a3, $a3, -1
    bnezc $a3, mix_sample
    nop
mix_return:
    jrc $ra
    nop

mix_wrap16:
    move $v0, $a3
    move $v1, $zero
    beqzc $a3, wrap_return
    nop
wrap_sample:
    lbu $t0, 0($a0)
    lbu $t1, 1($a0)
    sll $t1, $t1, 8
    or $t0, $t0, $t1
    seh $t0, $t0
    lbu $t1, 0($a1)
    lbu $t2, 1($a1)
    sll $t2, $t2, 8
    or $t1, $t1, $t2
    seh $t1, $t1
    addu $t0, $t0, $t1
    sb $t0, 0($a2)
    srl $t1, $t0, 8
    sb $t1, 1($a2)
    daddiu $a0, $a0, 2
    daddiu $a1, $a1, 2
    daddiu $a2, $a2, 2
    addiu $a3, $a3, -1
    bnezc $a3, wrap_sample
    nop
wrap_return:
    jrc $ra
    nop
