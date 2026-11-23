# Complete worked record classifier. This is not a whole-image validator or VM.
.data
record: .byte 2,0,0,0,128
record_end: .byte 0xa5
length: .word 5
status: .word 99
width: .word 99
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, record
    dla $t0, length
    lw $a1, 0($t0)
    li $a2, 0
width_call:
    balc record_width
    nop
width_ready:
    nop
    dla $t0, status
    sw $v0, 0($t0)
    sw $v1, 4($t0)
width_stored:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

# a0: full code pointer; a1: selected length; a2: candidate start offset.
# v0: 0 valid, -1 unknown opcode, -2 incomplete record, -4 no byte to fetch.
# v1: encoded width on success, zero on failure. Caller-saved temporaries only.
record_width:
    li $v0, -4
    move $v1, $zero
    bgeu $a2, $a1, record_return
    nop
    daddu $t0, $a0, $a2
record_classify:
    lbu $t1, 0($t0)
    li $t2, 1
    beqzc $t1, record_check
    nop
    li $t4, 1
    beqc $t1, $t4, record_two
    nop
    li $t4, 2
    beqc $t1, $t4, record_five
    nop
    daddiu $t4, $t1, -16
    sltiu $t4, $t4, 4
    bnezc $t4, record_check
    nop
    daddiu $t4, $t1, -32
    sltiu $t4, $t4, 4
    bnezc $t4, record_check
    nop
    daddiu $t4, $t1, -48
    sltiu $t4, $t4, 2
    bnezc $t4, record_two
    nop
    li $v0, -1
    bc record_return
    nop
record_two:
    li $t2, 2
    bc record_check
    nop
record_five:
    li $t2, 5
record_check:
    dsubu $t3, $a1, $a2
    li $v0, -2
record_fits:
    bltu $t3, $t2, record_return
    nop
    move $v0, $zero
    move $v1, $t2
record_return:
    jr $ra
    nop
