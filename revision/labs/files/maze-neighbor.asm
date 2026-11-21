# Return one orthogonal neighbor ID, or -1 at the edge.
# Inputs: a0 = cell ID 0..63; a1 = direction 0=N, 1=E, 2=S, 3=W.
.data
.align 2
answer: .word -2
dx: .byte 0, 1, 0, -1
dy: .byte -1, 0, 1, 0
.text
.globl main
main:
    li $a0, 7
    li $a1, 1
    balc neighbor_id
neighbor_done:
    sw $v0, answer
neighbor_exit:
    li $v0, 10
    syscall

neighbor_id:
    andi $t0, $a0, 7
    srl $t1, $a0, 3
    la $t2, dx
    daddu $t2, $t2, $a1
    lb $t3, 0($t2)
    addu $t0, $t0, $t3
    bltzc $t0, neighbor_absent
    nop
    li $t4, 8
    bgec $t0, $t4, neighbor_absent
    nop
    la $t2, dy
    daddu $t2, $t2, $a1
    lb $t3, 0($t2)
    addu $t1, $t1, $t3
    bltzc $t1, neighbor_absent
    nop
    bgec $t1, $t4, neighbor_absent
    nop
    sll $v0, $t1, 3
    addu $v0, $v0, $t0
    jrc $ra
neighbor_absent:
    li $v0, -1
    jrc $ra
