# Arithmetic model of the player's anchored deadlines; no host-clock measurement.
.text
.globl main
main:
    li $t0,1000
    li $t1,40
    daddu $s0,$t0,$t1
    li $t2,1010
    dsubu $s1,$s0,$t2
    li $t2,1035
    dsubu $s2,$s0,$t2
lesson_early_wake:
    nop
    li $t2,1047
    dsubu $s3,$t2,$s0
    daddu $s4,$s0,$t1
lesson_late_reading:
    nop
    li $t2,8
    dmul $s6,$t2,$t1
    daddu $s5,$t0,$s6
lesson_last_deadline:
    nop
    li $v0,10
    syscall
