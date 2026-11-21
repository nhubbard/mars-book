.data
pcm_a_guard: .word 0x13579bdf
pcm_a: .half -32768,32767,30000,-30000,0,32767,-32768,1234,30000
    .space 174
pcm_a_end: .word 0x2468ace0
pcm_b_guard: .word 0x31415926
pcm_b: .half 0,0,-10000,10000,-1,-1,1,5678,-10000
    .space 174
pcm_b_end: .word 0x27182818
pcm_c_guard: .word 0x11223344
pcm_c: .space 192
pcm_c_end: .word 0x22334455
pcm_d_guard: .word 0x33445566
pcm_d: .space 192
pcm_d_end: .word 0x44556677
count: .word 9
processed: .word 99
clipped: .word 99
caller_end: .word 0x55667788
.text
.globl main
main:
    dla $a0, pcm_a
    dla $a1, pcm_b
    dla $a2, pcm_c
    lw $a3, count
    balc subtract_pcm16
    nop
subtract_ready:
    nop
    sw $v0, processed
    sw $v1, clipped
results_ready:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# Implement Chapter 15's independent signed saturated difference.
subtract_pcm16:
    break
