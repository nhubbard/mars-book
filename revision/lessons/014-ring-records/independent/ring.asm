.data
.align 3
descriptor_a: .word 0,0,0,8
    .dword 0
descriptor_a_guard: .word 0x13579bdf
.align 3
descriptor_b: .word 0,0,0,7
    .dword 0
descriptor_b_guard: .word 0x2468ace0
ring_a_guard: .word 0x31415926
ring_a: .space 32
ring_a_end: .word 0x27182818
ring_b_guard: .word 0x12345678
ring_b: .space 32
ring_b_end: .word 0x23456789
source_guard: .word 0x34567890
source: .byte 65,66,67,0,0,0,0,0
    .byte 120,121,0,0,0,0,0,0
    .byte 127,-128,-1,0,0,0,0,0
    .space 8
    .byte 0,1,2,127,-128,-2,-1,0
    .space 24
source_end: .word 0x45678901
output_guard: .word 0x56789012
output: .word 0x55555555,0x55555555,0x55555555,0x55555555
    .word 0x55555555,0x55555555,0x55555555,0x55555555
    .word 0x55555555,0x55555555,0x55555555,0x55555555
    .word 0x55555555,0x55555555,0x55555555,0x55555555
output_end: .word 0x67890123
commands:
    .word 0,3,0
    .word 0,2,8
    .word 1,31,0
    .word 0,3,16
    .word 1,31,8
    .word 1,31,16
    .word 0,0,24
    .word 1,0,24
    .word 0,7,32
    .word 0,0,24
    .word 1,6,32
    .word 1,7,32
    .word 1,31,48
    .word 0,8,48
command_count: .word 14
last_status: .word -99
last_aux: .word -99
operation_index: .word -1
state_end: .word 0x78901234
.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    dla $s0, descriptor_a
    dla $t0, ring_a
    sd $t0, 16($s0)
    dla $s1, commands
    dla $t0, command_count
    lw $s2, 0($t0)
    move $s3, $zero
    beqzc $s2, done
    nop
next_command:
    lw $s4, 0($s1)
    lw $a2, 4($s1)
    lw $t0, 8($s1)
    move $a0, $s0
    bnezc $s4, call_pop
    nop
    dla $a1, source
    daddu $a1, $a1, $t0
    balc push_record
    bc publish
call_pop:
    dla $a1, output
    daddu $a1, $a1, $t0
    balc pop_record
publish:
    dla $t0, last_status
    sw $v0, 0($t0)
    dla $t0, last_aux
    sw $v1, 0($t0)
    dla $t0, operation_index
    sw $s3, 0($t0)
operation_ready:
    nop
operation_continue:
    daddiu $s1, $s1, 12
    addiu $s3, $s3, 1
    sltu $t0, $s3, $s2
    bnezc $t0, next_command
    nop
    bc done
function_return:
    nop
done:
    li $v0, 10
    syscall

# Implement the existing 014 ring-buffer interface. Equivalent algorithms are allowed.
# Preserve the public caller/data/operation_ready observation contract.
push_record:
    break

pop_record:
    break
