# mars:tool marsBot instructionsPerTick=64
.eqv BOT_BASE 0x00000000ffff8000
.data
.align 2
side: .word 16
side_guard: .word 0x13579bdf
.text
.globl main
main:
# BEGIN STUDENT PROGRAM
# END STUDENT PROGRAM

done:
    li $v0, 10
    syscall
