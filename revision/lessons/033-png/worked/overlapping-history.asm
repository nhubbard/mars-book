# One existing A supplies a forward match of length six at distance one.
.data
history: .byte 65,-91,-91,-91,-91,-91,-91,-91
.text
.globl main
main:
    dla $t0,history
    daddiu $t1,$t0,1
    li $t2,6
    move $t4,$zero
match_byte:
    lbu $t3,0($t0)
    sb $t3,0($t1)
    daddiu $t0,$t0,1
    daddiu $t1,$t1,1
    addiu $t4,$t4,1
    addiu $t2,$t2,-1
    bnezc $t2,match_byte
    nop
    dla $t0,history
    lwu $t6,0($t0)
    lbu $t7,7($t0)
match_complete:
    li $v0,10
    syscall
