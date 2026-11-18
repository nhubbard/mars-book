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
    dla $a0, code_a
    dla $t0, length
    lw $a1, 0($t0)
    dla $a2, state_a
    dla $t0, stack_a
    sd $t0, 24($a2)
    dla $t0, fuel
    lw $a3, 0($t0)
vm_call:
    jal run_vm
    nop
vm_ready:
    nop
vm_continue:
    dla $t0, last_status
    sw $v0, 0($t0)
    sw $v1, 4($t0)
    beq $zero, $zero, done
    nop
function_return:
    nop
done:
    li $v0, 10
    syscall

run_vm:
    daddiu $sp, $sp, -64
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $s7, 56($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    ld $s3, 24($s2)
    lw $s4, 16($s2)
    move $s5, $zero
    move $s6, $zero
    move $s7, $zero
    move $v0, $zero
    move $v1, $zero
    li $a2, -1
format_loop:
# BEGIN format_fetch
    beq $zero, $zero, vm_commit
    nop
# END format_fetch
# BEGIN format_arithmetic
    beq $zero, $zero, vm_commit
    nop
# END format_arithmetic
# BEGIN format_stack
    beq $zero, $zero, vm_commit
    nop
# END format_stack
# BEGIN format_short
    beq $zero, $zero, vm_commit
    nop
# END format_short
# BEGIN format_wide
    beq $zero, $zero, vm_commit
    nop
# END format_wide
format_size:
# BEGIN format_bound
    beq $zero, $zero, vm_commit
    nop
# END format_bound
# BEGIN format_mark
    beq $zero, $zero, vm_commit
    nop
# END format_mark
# BEGIN format_advance
    beq $zero, $zero, vm_commit
    nop
# END format_advance
truncated_image:
    li $v0, -2
image_error:
    move $a2, $s6
    move $s6, $zero
    beq $zero, $zero, vm_commit
    nop
targets_begin:
    move $s6, $zero
targets_loop:
# BEGIN targets_fetch
    beq $zero, $zero, vm_commit
    nop
# END targets_fetch
# BEGIN targets_sizes
    beq $zero, $zero, vm_commit
    nop
# END targets_sizes
# BEGIN targets_branches
    beq $zero, $zero, vm_commit
    nop
# END targets_branches
target_check:
# BEGIN target_range
    beq $zero, $zero, vm_commit
    nop
# END target_range
# BEGIN target_bit
    beq $zero, $zero, vm_commit
    nop
# END target_bit
target_short_size:
# BEGIN target_short
    beq $zero, $zero, vm_commit
    nop
# END target_short
target_wide_size:
# BEGIN target_wide
    beq $zero, $zero, vm_commit
    nop
# END target_wide
target_advance:
# BEGIN target_advance
    beq $zero, $zero, vm_commit
    nop
# END target_advance
bad_target:
    li $v0, -3
    beq $zero, $zero, image_error
    nop
execute_begin:
    move $s6, $zero
vm_loop:
# BEGIN execute_guard
    beq $zero, $zero, vm_commit
    nop
# END execute_guard
# BEGIN dispatch_constants
    beq $zero, $zero, vm_commit
    nop
# END dispatch_constants
# BEGIN dispatch_arithmetic
    beq $zero, $zero, vm_commit
    nop
# END dispatch_arithmetic
# BEGIN dispatch_stack
    beq $zero, $zero, vm_commit
    nop
# END dispatch_stack
# BEGIN dispatch_branches
    beq $zero, $zero, vm_commit
    nop
# END dispatch_branches
vm_halt:
# BEGIN halt
    beq $zero, $zero, vm_commit
    nop
# END halt
vm_push8:
# BEGIN push8
    beq $zero, $zero, vm_commit
    nop
# END push8
vm_push32:
# BEGIN push32_decode
    beq $zero, $zero, vm_commit
    nop
# END push32_decode
# BEGIN push32_store
    beq $zero, $zero, vm_commit
    nop
# END push32_store
vm_dup:
# BEGIN dup
    beq $zero, $zero, vm_commit
    nop
# END dup
vm_over:
# BEGIN over
    beq $zero, $zero, vm_commit
    nop
# END over
vm_copy:
# BEGIN copy
    beq $zero, $zero, vm_commit
    nop
# END copy
vm_drop:
# BEGIN drop
    beq $zero, $zero, vm_commit
    nop
# END drop
vm_swap:
# BEGIN swap
    beq $zero, $zero, vm_commit
    nop
# END swap
vm_binary:
# BEGIN binary_operands
    beq $zero, $zero, vm_commit
    nop
# END binary_operands
# BEGIN binary_dispatch
    beq $zero, $zero, vm_commit
    nop
# END binary_dispatch
# BEGIN divide
    beq $zero, $zero, vm_commit
    nop
# END divide
vm_add:
# BEGIN add
    beq $zero, $zero, vm_commit
    nop
# END add
vm_sub:
# BEGIN subtract
    beq $zero, $zero, vm_commit
    nop
# END subtract
vm_mul:
# BEGIN multiply
    beq $zero, $zero, vm_commit
    nop
# END multiply
vm_check_word:
# BEGIN check_word
    beq $zero, $zero, vm_commit
    nop
# END check_word
vm_jump:
# BEGIN jump
    beq $zero, $zero, vm_commit
    nop
# END jump
vm_jump_zero:
# BEGIN jump_zero
    beq $zero, $zero, vm_commit
    nop
# END jump_zero
vm_retire:
# BEGIN retire
    beq $zero, $zero, vm_commit
    nop
# END retire
end_fault:
    li $v0, -4
    beq $zero, $zero, runtime_error
    nop
underflow_stack:
    li $v0, -5
    beq $zero, $zero, runtime_error
    nop
overflow_stack:
    li $v0, -6
    beq $zero, $zero, runtime_error
    nop
arithmetic_overflow:
    li $v0, -7
    beq $zero, $zero, runtime_error
    nop
zero_divisor:
    li $v0, -8
runtime_error:
    move $a2, $s6
vm_commit:
    sw $s6, 0($s2)
    sw $s7, 4($s2)
    sw $v1, 8($s2)
    sw $v0, 12($s2)
    sw $a2, 20($s2)
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $s7, 56($sp)
    daddiu $sp, $sp, 64
    jr $ra
    nop
