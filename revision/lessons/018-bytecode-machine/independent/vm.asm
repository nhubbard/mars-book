# Independent bounded bytecode interpreter. Implement run_vm; keep the caller/data arenas.
.data
code_a_guard: .word 0x13579bdf
code_a: .byte 1,0,1,3,32,49,16,34,35,16,34,1,1,17,48,4,33,0
    .space 78
code_a_end: .word 0x2468ace0
code_b_guard: .word 0x31415926
code_b: .space 96
code_b_end: .word 0x27182818
stack_a_guard: .word 0x11223344
stack_a: .space 64
stack_a_end: .word 0x22334455
stack_b_guard: .word 0x33445566
stack_b: .space 64
stack_b_end: .word 0x44556677
.align 3
state_a: .word 99,99,99,99,8,99
    .dword 0
state_a_end: .word 0x55667788
.align 3
state_b: .word 99,99,99,99,3,99
    .dword 0
state_b_end: .word 0x66778899
length: .word 18
fuel: .word 64
last_status: .word 99
last_steps: .word 99
state_end: .word 0x77889900
.text
.globl main
main:
    li $t0, -16
    and $sp, $sp, $t0
    dla $a0, code_a
    dla $t0, length
    lw $a1, 0($t0)
    dla $a2, state_a
    dla $t0, stack_a
    sd $t0, 24($a2)
    dla $t0, fuel
    lw $a3, 0($t0)
vm_call:
    balc run_vm
    nop
vm_ready:
    nop
vm_continue:
    dla $t0, last_status
    sw $v0, 0($t0)
    sw $v1, 4($t0)
vm_stored:
    nop
    bc done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

run_vm:
    break
