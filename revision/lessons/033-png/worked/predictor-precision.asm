# Predictor arithmetic uses wider signed integers before residuals wrap to bytes.
.text
.globl main
main:
    li $t0,255
    li $t1,145
    addu $t2,$t0,$t1
    srl $t3,$t2,1
    andi $t4,$t2,255
    srl $t4,$t4,1
    li $t5,201
    subu $t5,$t5,$t3
    andi $t5,$t5,255
average_precision:
    li $s0,255
    li $s1,0
    li $s2,85
    addu $s3,$s0,$s1
    subu $s3,$s3,$s2
    subu $s4,$s0,$s3
    subu $s5,$s3,$s1
    subu $s6,$s3,$s2
first_tie:
    li $s0,0
    li $s1,3
    li $s2,1
    addu $s3,$s0,$s1
    subu $s3,$s3,$s2
    subu $s4,$s3,$s0
    subu $s5,$s1,$s3
    subu $s6,$s3,$s2
second_tie:
    li $v0,10
    syscall
