# mars:tool digitalLab
.eqv RIGHT_DISPLAY 0x00000000ffff0010
.data
.align 2
glyphs:       .byte 0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07
              .byte 0x7f, 0x6f, 0x77, 0x7c, 0x39, 0x5e, 0x79, 0x71
glyph_guard:  .word 0x13579bdf
first_digit:  .word 0
digit_count:  .word 16
inputs_guard: .word 0x2468ace0

.text
.globl main
main:
    dli $t7, RIGHT_DISPLAY
    la $t0, glyphs
    la $t4, first_digit
    lw $t2, 0($t4)
    lw $t1, 4($t4)
    daddu $t0, $t0, $t2
loop:
    beqzc $t1, done
    nop
    lbu $t3, 0($t0)
port_write:
    sb $t3, 0($t7)
displayed:
    daddiu $t0, $t0, 1
    addiu $t2, $t2, 1
    addiu $t1, $t1, -1
iteration_done:
    bc loop
done:
    li $v0, 10
    syscall
