# Chunk lengths are unsigned; calculate padded extents in 64 bits.
.text
.globl main
main:
    li $t0, 12
    li $t1, 3
    daddiu $t2, $t1, 1
    dli $t3, -2
    and $t2, $t2, $t3
    daddiu $t4, $t0, 8
    daddu $t4, $t4, $t2
first_extent:
    dli $t1, 0xffffffff
    daddiu $t2, $t1, 1
    and $t2, $t2, $t3
wide_extent:
    li $v0, 10
    syscall
