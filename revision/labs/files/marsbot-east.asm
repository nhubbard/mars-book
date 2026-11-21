# mars:tool marsBot instructionsPerTick=64
.eqv BOT_BASE 0x00000000ffff8000
.data
.align 2
side: .word 16
side_guard: .word 0x13579bdf
.text
.globl main
main:
    dli $s0, BOT_BASE
    lw $s1, side
    li $t0, 1
    sw $t0, 0x20($s0)
    li $t1, 90
    sw $t1, 0x10($s0)
    sw $t0, 0x50($s0)
east_wait:
    lw $t2, 0x30($s0)
    bnec $t2, $s1, east_wait
    nop
east_stop:
    sw $zero, 0x50($s0)
east_stopped:
    sw $zero, 0x20($s0)
leg_finished:
done:
    li $v0, 10
    syscall
