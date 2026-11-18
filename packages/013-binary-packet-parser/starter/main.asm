.data
.align 2
packet_guard: .word 0x13579bdf
packet: .byte 0x4d,0x50,1,1,0,3,0x12,0x34,127,-128,-1,26
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
    dla $a0, packet
    dla $t0, available
    lw $a1, 0($t0)
    dla $a2, record_a
    jal parse_packet
    nop
parse_ready:
    nop
    dla $t0, status
    sw $v0, 0($t0)
    dla $t0, payload_address
    sd $v1, 0($t0)
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

parse_packet:
# BEGIN bounds
# END bounds
# BEGIN magic
# END magic
# BEGIN version
# END version
# BEGIN flags
# END flags
# BEGIN length
# END length
# BEGIN limit
# END limit
# BEGIN available
# END available
# BEGIN sequence
# END sequence
# BEGIN header_init
# END header_init
header_sum:
# BEGIN header_loop
# END header_loop
# BEGIN payload_init
# END payload_init
payload_sum:
# BEGIN payload_loop
# END payload_loop
trailer_sum:
# BEGIN trailer
# END trailer
# BEGIN commit
# END commit
# BEGIN success
# END success
packet_return:
    jr $ra
    nop
