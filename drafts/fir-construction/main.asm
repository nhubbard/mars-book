.data
source_guard: .word 0x13579bdf
source: .half 0,0,0,0,0,0,0,16000,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    .space 146
source_end: .word 0x2468ace0
coeff_guard: .word 0x31415926
coeff: .half 4096,4096,4096,4096,4096,4096,4096,4096
    .space 48
coeff_end: .word 0x27182818
output_guard: .word 0x11223344
output: .space 192
output_end: .word 0x22334455
count: .word 16
processed: .word 99
clipped: .word 99
caller_end: .word 0x55667788
.text
.globl main
main:
    dla $a0, source
    dla $a1, coeff
    dla $a2, output
    dla $t0, count
    lw $a3, 0($t0)
filter_call:
    jal fir_q15
    nop
filter_ready:
    nop
filter_continue:
    dla $t0, processed
    sw $v0, 0($t0)
    sw $v1, 4($t0)
results_ready:
    nop
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

fir_q15:
# BEGIN vector_entry
    beq $zero, $zero, fir_scalar
    nop
# END vector_entry
# BEGIN vector_coefficients
    beq $zero, $zero, fir_return
    nop
# END vector_coefficients
# BEGIN coefficient_mask
    beq $zero, $zero, fir_return
    nop
# END coefficient_mask
# BEGIN coefficient_low_words
    beq $zero, $zero, fir_return
    nop
# END coefficient_low_words
# BEGIN coefficient_high_words
    beq $zero, $zero, fir_return
    nop
# END coefficient_high_words
fir_window:
# BEGIN vector_input
    beq $zero, $zero, fir_return
    nop
# END vector_input
# BEGIN input_mask
    beq $zero, $zero, fir_return
    nop
# END input_mask
# BEGIN input_low_words
    beq $zero, $zero, fir_return
    nop
# END input_low_words
# BEGIN input_high_words
    beq $zero, $zero, fir_return
    nop
# END input_high_words
# BEGIN dot_low
    beq $zero, $zero, fir_return
    nop
# END dot_low
# BEGIN dot_high
    beq $zero, $zero, fir_return
    nop
# END dot_high
# BEGIN add_pair_vectors
    beq $zero, $zero, fir_return
    nop
# END add_pair_vectors
# BEGIN copy_low_sum
    beq $zero, $zero, fir_return
    nop
# END copy_low_sum
# BEGIN copy_high_sum
    beq $zero, $zero, fir_return
    nop
# END copy_high_sum
# BEGIN finish_sum
    beq $zero, $zero, fir_return
    nop
# END finish_sum
fir_round:
    daddiu $t0, $t0, 16384
    dsra $t0, $t0, 15
    li $t8, 32767
    slt $t9, $t8, $t0
    bne $t9, $zero, fir_high
    nop
    li $t8, -32768
    slt $t9, $t0, $t8
    bne $t9, $zero, fir_low
    nop
    beq $zero, $zero, fir_store
    nop
fir_high:
    li $t0, 32767
    beq $zero, $zero, fir_clipped
    nop
fir_low:
    li $t0, -32768
fir_clipped:
    addiu $v1, $v1, 1
fir_store:
    sb $t0, 0($a2)
    dsrl $t1, $t0, 8
    sb $t1, 1($a2)
    daddiu $a0, $a0, 2
    daddiu $a2, $a2, 2
    addiu $a3, $a3, -1
    bne $a3, $zero, fir_window
    nop
fir_return:
    jr $ra
    nop

fir_scalar:
    move $v0, $a3
    move $v1, $zero
# BEGIN scalar_bound
    beq $zero, $zero, scalar_return
    nop
# END scalar_bound
scalar_window:
# BEGIN scalar_window
    beq $zero, $zero, scalar_return
    nop
# END scalar_window
scalar_tap:
# BEGIN sample_load
    beq $zero, $zero, scalar_return
    nop
# END sample_load
# BEGIN sample_join
    beq $zero, $zero, scalar_return
    nop
# END sample_join
# BEGIN sample_sign
    beq $zero, $zero, scalar_return
    nop
# END sample_sign
# BEGIN coefficient_load
    beq $zero, $zero, scalar_return
    nop
# END coefficient_load
# BEGIN coefficient_join
    beq $zero, $zero, scalar_return
    nop
# END coefficient_join
# BEGIN coefficient_sign
    beq $zero, $zero, scalar_return
    nop
# END coefficient_sign
# BEGIN scalar_product
    beq $zero, $zero, scalar_return
    nop
# END scalar_product
# BEGIN scalar_accumulate
    beq $zero, $zero, scalar_return
    nop
# END scalar_accumulate
# BEGIN next_tap
    beq $zero, $zero, scalar_return
    nop
# END next_tap
scalar_round:
# BEGIN round_bias
    beq $zero, $zero, scalar_return
    nop
# END round_bias
# BEGIN round_scale
    beq $zero, $zero, scalar_return
    nop
# END round_scale
# BEGIN upper_bound
    beq $zero, $zero, scalar_return
    nop
# END upper_bound
# BEGIN lower_bound
    beq $zero, $zero, scalar_return
    nop
# END lower_bound
scalar_high:
# BEGIN upper_clamp
    beq $zero, $zero, scalar_return
    nop
# END upper_clamp
scalar_low:
# BEGIN lower_clamp
    beq $zero, $zero, scalar_return
    nop
# END lower_clamp
scalar_clipped:
# BEGIN count_clip
    beq $zero, $zero, scalar_return
    nop
# END count_clip
scalar_store:
# BEGIN store_low
    beq $zero, $zero, scalar_return
    nop
# END store_low
# BEGIN store_high
    beq $zero, $zero, scalar_return
    nop
# END store_high
# BEGIN next_window
    beq $zero, $zero, scalar_return
    nop
# END next_window
scalar_return:
    jr $ra
    nop
