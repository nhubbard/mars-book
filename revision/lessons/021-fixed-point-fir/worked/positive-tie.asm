.data
source_guard: .word 0x13579bdf
source: .half 32767,1,1,0,0,0,0,0
    .space 176
source_end: .word 0x2468ace0
coeff_guard: .word 0x31415926
coeff: .half 32767,32767,16384,0,0,0,0,0
    .space 48
coeff_end: .word 0x27182818
output_guard: .word 0x11223344
output: .space 192
output_end: .word 0x22334455
count: .word 1
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
    balc fir_q15
    nop
filter_ready:
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

fir_q15:
    move $v0, $a3
    move $v1, $zero
    beqzc $a3, fir_return
    nop
    ld.b $w1, 0($a1)
    clti_s.h $w2, $w1, 0
    ilvr.h $w3, $w2, $w1
    ilvl.h $w4, $w2, $w1
fir_window:
    ld.b $w0, 0($a0)
    clti_s.h $w2, $w0, 0
    ilvr.h $w5, $w2, $w0
    ilvl.h $w6, $w2, $w0
fir_widened:
    dotp_s.d $w7, $w5, $w3
    dotp_s.d $w8, $w6, $w4
fir_pairs:
    addv.d $w7, $w7, $w8
fir_combined:
    copy_s.d $t0, $w7[0]
    copy_s.d $t1, $w7[1]
    daddu $t0, $t0, $t1
fir_round:
    daddiu $t0, $t0, 16384
fir_biased:
    dsra $t0, $t0, 15
fir_quantized:
    li $t8, 32767
    slt $t9, $t8, $t0
    bnezc $t9, fir_high
    nop
    li $t8, -32768
    slt $t9, $t0, $t8
    bnezc $t9, fir_low
    nop
    bc fir_store
    nop
fir_high:
    li $t0, 32767
    bc fir_clipped
    nop
fir_low:
    li $t0, -32768
fir_clipped:
    addiu $v1, $v1, 1
fir_store:
    sb $t0, 0($a2)
    dsrl $t1, $t0, 8
    sb $t1, 1($a2)
fir_next:
    daddiu $a0, $a0, 2
    daddiu $a2, $a2, 2
    addiu $a3, $a3, -1
    bnezc $a3, fir_window
    nop
fir_return:
    jrc $ra
    nop

fir_scalar:
    move $v0, $a3
    move $v1, $zero
    beqzc $a3, scalar_return
    nop
scalar_window:
    move $t0, $zero
    move $t1, $a0
    move $t2, $a1
    li $t3, 8
scalar_tap:
    lbu $t4, 0($t1)
    lbu $t5, 1($t1)
    sll $t5, $t5, 8
    or $t4, $t4, $t5
    seh $t4, $t4
    lbu $t5, 0($t2)
    lbu $t6, 1($t2)
    sll $t6, $t6, 8
    or $t5, $t5, $t6
    seh $t5, $t5
    dmul $t7, $t4, $t5
    daddu $t0, $t0, $t7
    daddiu $t1, $t1, 2
    daddiu $t2, $t2, 2
    addiu $t3, $t3, -1
    bnezc $t3, scalar_tap
    nop
scalar_round:
    daddiu $t0, $t0, 16384
    dsra $t0, $t0, 15
    li $t8, 32767
    slt $t9, $t8, $t0
    bnezc $t9, scalar_high
    nop
    li $t8, -32768
    slt $t9, $t0, $t8
    bnezc $t9, scalar_low
    nop
    bc scalar_store
    nop
scalar_high:
    li $t0, 32767
    bc scalar_clipped
    nop
scalar_low:
    li $t0, -32768
scalar_clipped:
    addiu $v1, $v1, 1
scalar_store:
    sb $t0, 0($a2)
    dsrl $t1, $t0, 8
    sb $t1, 1($a2)
    daddiu $a0, $a0, 2
    daddiu $a2, $a2, 2
    addiu $a3, $a3, -1
    bnezc $a3, scalar_window
    nop
scalar_return:
    jrc $ra
    nop
