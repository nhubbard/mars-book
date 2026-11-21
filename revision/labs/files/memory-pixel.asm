# mars:tool bitmap width=64 height=64 unitWidth=32 unitHeight=32 base=static_data
.data
.align 2
pixel:    .word 0
neighbor: .word 0x13579bdf
          .space 8

.text
.globl main
main:
    la $t0, pixel
    li $t1, 0x00336699
store_color:
    sw $t1, 0($t0)
inspect_initial:
    lw $t2, 0($t0)
    lbu $t3, 0($t0)
    lb $t4, 0($t0)
    li $t5, 0xff
change_blue:
    sb $t5, 0($t0)
inspect_final:
    lw $t6, 0($t0)
    lw $t7, 4($t0)
    li $v0, 10
    syscall
