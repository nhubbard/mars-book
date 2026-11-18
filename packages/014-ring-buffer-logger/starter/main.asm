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
    dla $s0, descriptor_a
    dla $t0, ring_a
    sd $t0, 16($s0)
    dla $s1, commands
    dla $t0, command_count
    lw $s2, 0($t0)
    move $s3, $zero
    beq $s2, $zero, done
    nop
next_command:
    lw $s4, 0($s1)
    lw $a2, 4($s1)
    lw $t0, 8($s1)
    move $a0, $s0
    bne $s4, $zero, call_pop
    nop
    dla $a1, source
    daddu $a1, $a1, $t0
    jal push_record
    nop
    beq $zero, $zero, publish
    nop
call_pop:
    dla $a1, output
    daddu $a1, $a1, $t0
    jal pop_record
    nop
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
    bne $t0, $zero, next_command
    nop
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

push_record:
# BEGIN push_fit
# END push_fit
# BEGIN push_space
# END push_space
# BEGIN push_prefix
# END push_prefix
# BEGIN push_advance
# END push_advance
push_payload:
# BEGIN push_init
# END push_init
push_copy:
# BEGIN push_bytes
# END push_bytes
# BEGIN push_wrap
# END push_wrap
push_cursor:
# BEGIN push_count
# END push_count
push_commit:
# BEGIN push_publish
# END push_publish
push_return:
    jr $ra
    nop

pop_record:
# BEGIN pop_empty
# END pop_empty
# BEGIN pop_prefix
# END pop_prefix
# BEGIN pop_fit
# END pop_fit
# BEGIN pop_advance
# END pop_advance
pop_payload:
# BEGIN pop_init
# END pop_init
pop_copy:
# BEGIN pop_bytes
# END pop_bytes
# BEGIN pop_wrap
# END pop_wrap
pop_cursor:
# BEGIN pop_count
# END pop_count
pop_commit:
# BEGIN pop_publish
# END pop_publish
pop_return:
    jr $ra
    nop
