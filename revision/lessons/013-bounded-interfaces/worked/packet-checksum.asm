.data
.align 2
packet_guard: .word 0x13579bdf
packet: .byte 0x4d,0x50,1,1,0,3,0x12,0x34,127,-128,-1,27
    .space 244
packet_end_guard: .word 0x2468ace0
record_a: .word -1,-1,-1,-1
record_a_guard: .word 0x31415926
record_b: .word -1,-1,-1,-1
record_b_guard: .word 0x27182818
available: .word 12
status: .word 0
.align 3
payload_address: .dword 0
state_end: .word 0x76543210
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $a0, packet
    lw $a1, available
    la $a2, record_a
    balc parse_packet
parse_ready:
    nop
    sw $v0, status
    sd $v1, payload_address
    bc done
function_return:
    nop
done:
    li $v0, 10
    syscall

# a0 = bounded input, a1 = available bytes, a2 = selected 16-byte record.
parse_packet:
    li $v0, -1
    move $v1, $zero
    li $t9, 8
    bltuc $a1, $t9, packet_return
    nop
    li $v0, -2
    lbu $t7, 0($a0)
    li $t9, 0x4d
    bnec $t7, $t9, packet_return
    nop
    lbu $t7, 1($a0)
    li $t9, 0x50
    bnec $t7, $t9, packet_return
    nop
    li $v0, -3
    lbu $t7, 2($a0)
    li $t9, 1
    bnec $t7, $t9, packet_return
    nop
    li $v0, -4
    lbu $t1, 3($a0)
    andi $t9, $t1, 254
    bnezc $t9, packet_return
    nop
packet_length:
    lbu $t0, 4($a0)
    sll $t0, $t0, 8
    lbu $t7, 5($a0)
    or $t0, $t0, $t7
    li $v0, -5
    li $t9, 64
    bltuc $t9, $t0, packet_return
    nop
    addiu $t8, $t0, 9
    li $v0, -1
    bltuc $a1, $t8, packet_return
    nop
packet_fields:
    lbu $t2, 6($a0)
    sll $t2, $t2, 8
    lbu $t7, 7($a0)
    or $t2, $t2, $t7
    move $t3, $zero
    move $t5, $a0
    li $t6, 8
header_sum:
    lbu $t7, 0($t5)
    addu $t3, $t3, $t7
    daddiu $t5, $t5, 1
    addiu $t6, $t6, -1
    bnezc $t6, header_sum
    nop
    move $t4, $zero
    move $t6, $t0
    beqzc $t6, trailer_sum
    nop
payload_sum:
    lbu $t7, 0($t5)
    addu $t3, $t3, $t7
    addu $t4, $t4, $t7
    daddiu $t5, $t5, 1
    addiu $t6, $t6, -1
    bnezc $t6, payload_sum
    nop
trailer_sum:
    lbu $t7, 0($t5)
    addu $t3, $t3, $t7
    andi $t3, $t3, 255
    li $v0, -6
    bnezc $t3, packet_return
    nop
packet_commit:
    andi $t4, $t4, 255
    sw $t1, 0($a2)
    sw $t2, 4($a2)
    sw $t0, 8($a2)
    sw $t4, 12($a2)
    move $v0, $t8
    daddiu $v1, $a0, 8
packet_return:
    jrc $ra
