# The full native width is not its low 32-bit word.
.data
.align 3
settings: .dword 0x100000001,3,7
.text
.globl main
main:
    dla $t0,settings
    ld $s0,0($t0)
    lwu $s1,0($t0)
    sltiu $s2,$s0,513
    ld $s3,8($t0)
    ld $s4,16($t0)
full_width:
    dli $t1,0x8000000000000000
    li $t2,7
    sltu $t3,$t1,$t2
    slt $t4,$t1,$t2
unsigned_capacity:
    li $v0,10
    syscall
