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
count: .word 16
processed: .word 99
clipped: .word 99
wrapped_count: .word 99
caller_end: .word 0x55667788
.text
.globl main
main:
    dla $a0, pcm_a
    dla $a1, pcm_b
    dla $a2, pcm_c
    dla $t0, count
    lw $a3, 0($t0)
mix_call:
    jal mix_pcm16
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
    jal mix_wrap16
    nop
wrap_ready:
    nop
wrap_continue:
    dla $t0, wrapped_count
    sw $v0, 0($t0)
comparison_ready:
    nop
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

mix_pcm16:
    move $v0, $a3
    move $v1, $zero
# BEGIN vector_gate
    beq $zero, $zero, mix_tail
    nop
# END vector_gate
mix_block:
# BEGIN vector_load
    beq $zero, $zero, mix_return
    nop
# END vector_load
# BEGIN vector_wrap
    beq $zero, $zero, mix_return
    nop
# END vector_wrap
# BEGIN vector_saturate
    beq $zero, $zero, mix_return
    nop
# END vector_saturate
# BEGIN vector_equal
    beq $zero, $zero, mix_return
    nop
# END vector_equal
# BEGIN vector_invert
    beq $zero, $zero, mix_return
    nop
# END vector_invert
# BEGIN vector_popcount
    beq $zero, $zero, mix_return
    nop
# END vector_popcount
# BEGIN vector_low_count
    beq $zero, $zero, mix_return
    nop
# END vector_low_count
# BEGIN vector_high_count
    beq $zero, $zero, mix_return
    nop
# END vector_high_count
# BEGIN vector_sum_count
    beq $zero, $zero, mix_return
    nop
# END vector_sum_count
# BEGIN vector_lane_count
    beq $zero, $zero, mix_return
    nop
# END vector_lane_count
# BEGIN vector_accumulate
    beq $zero, $zero, mix_return
    nop
# END vector_accumulate
# BEGIN vector_store
    beq $zero, $zero, mix_return
    nop
# END vector_store
# BEGIN vector_next
    beq $zero, $zero, mix_return
    nop
# END vector_next
mix_tail:
# BEGIN scalar_bound
    beq $zero, $zero, mix_return
    nop
# END scalar_bound
mix_sample:
# BEGIN load_a
    beq $zero, $zero, mix_return
    nop
# END load_a
# BEGIN join_a
    beq $zero, $zero, mix_return
    nop
# END join_a
# BEGIN sign_a
    beq $zero, $zero, mix_return
    nop
# END sign_a
# BEGIN load_b
    beq $zero, $zero, mix_return
    nop
# END load_b
# BEGIN join_b
    beq $zero, $zero, mix_return
    nop
# END join_b
# BEGIN sign_b
    beq $zero, $zero, mix_return
    nop
# END sign_b
# BEGIN exact_sum
    beq $zero, $zero, mix_return
    nop
# END exact_sum
# BEGIN upper_test
    beq $zero, $zero, mix_return
    nop
# END upper_test
# BEGIN lower_test
    beq $zero, $zero, mix_return
    nop
# END lower_test
mix_high:
# BEGIN upper_value
    beq $zero, $zero, mix_return
    nop
# END upper_value
mix_low:
# BEGIN lower_value
    beq $zero, $zero, mix_return
    nop
# END lower_value
mix_clipped:
# BEGIN count_clip
    beq $zero, $zero, mix_return
    nop
# END count_clip
mix_store:
# BEGIN store_low
    beq $zero, $zero, mix_return
    nop
# END store_low
# BEGIN store_high
    beq $zero, $zero, mix_return
    nop
# END store_high
# BEGIN next_sample
    beq $zero, $zero, mix_return
    nop
# END next_sample
mix_return:
    jr $ra
    nop

mix_wrap16:
    move $v0, $a3
    move $v1, $zero
    beq $a3, $zero, wrap_return
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
    bne $a3, $zero, wrap_sample
    nop
wrap_return:
    jr $ra
    nop
