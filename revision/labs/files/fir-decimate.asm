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
count: .word 8
processed: .word 99
clipped: .word 99
caller_end: .word 0x55667788
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $a0, source
    dla $a1, coeff
    dla $a2, output
    dla $t0, count
    lw $a3, 0($t0)
filter_call:
    balc decimate_q15
    nop
decimate_ready:
    nop
filter_continue:
    dla $t0, processed
    sw $v0, 0($t0)
    sw $v1, 4($t0)
results_ready:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# Implement eight-tap filtering at window starts 0,2,4,... .
# Arguments a0=input, a1=coefficients, a2=output, a3=output count 0..32.
# Return count in v0 and strict clips after rounding in v1.
decimate_q15:
    break
