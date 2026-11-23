# Arriving bits have a different interpretation as a numerical field and a code.
.text
.globl main
main:
    li $t0,1
    li $t1,0
    sll $t2,$t1,1
    or $t2,$t2,$t0
    sll $t3,$t0,1
    or $t3,$t3,$t1
same_arrivals:
    li $t4,0x23
    li $t5,0xdc
    li $t6,0xff
    sll $t6,$t6,8
    or $t5,$t5,$t6
    xor $t7,$t4,$t5
stored_complement:
    li $v0,10
    syscall
