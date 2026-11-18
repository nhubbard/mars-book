.data
.align 6
data: .byte 49,50,51,52,53,54,55,56,57
      .byte -91:4151
data_end: .word 0x13579bdf
.align 6
crc_table: .word 0x6a6b6c6d:272
table_end: .word 0x2468ace0
path: .asciiz "fixture.bin"
      .space 116
.align 6
scratch: .byte -91:320
scratch_end: .word 0x31415926
input_count: .word 9
initial_state: .word 0xffffffff
exercise: .word 0
buffer_capacity: .word 7
method: .word 0
raw_result: .word -1
file_crc: .word -1
file_status: .word -1
.text
.globl main
main:
    li $sp, 0x7fff0000
    dla $t0, exercise
    lw $t0, 0($t0)
    beq $t0, $zero, choose_bit
    nop
    li $t1, 1
    beq $t0, $t1, choose_init
    nop
    li $t1, 2
    beq $t0, $t1, choose_table
    nop
    dla $a0, path
    dla $a1, scratch
    dla $t0, buffer_capacity
    lw $a2, 0($t0)
    dla $t0, method
    lw $a3, 0($t0)
file_call:
    jal crc32_file
    nop
file_done:
    dla $t0, file_crc
    sw $v0, 0($t0)
    dla $t0, file_status
    sw $v1, 0($t0)
    beq $zero, $zero, results_ready
    nop
choose_bit:
    dla $t9, crc32_bit_update
    beq $zero, $zero, kernel_arguments
    nop
choose_table:
    dla $t9, crc32_table_update
kernel_arguments:
    dla $a0, data
    dla $t0, input_count
    lw $a1, 0($t0)
    dla $t0, initial_state
    lwu $a2, 0($t0)
    dla $a3, crc_table
    beq $zero, $zero, kernel_call
    nop
choose_init:
    dla $a0, crc_table
    dla $t9, crc32_table_init
kernel_call:
    jalr $t9
    nop
kernel_done:
    dla $t0, raw_result
    sw $v0, 0($t0)
results_ready:
    li $v0, 10
    syscall
function_return:
    beq $zero, $zero, results_ready
    nop

crc32_bit_update:
# BEGIN b_setup
    move $v0, $a2
    beq $zero, $zero, bit_return
    nop
# END b_setup
bit_next_byte:
# BEGIN b_gate
    beq $zero, $zero, bit_return
    nop
# END b_gate
# BEGIN b_byte
    beq $zero, $zero, bit_return
    nop
# END b_byte
bit_next_bit:
# BEGIN b_step
    beq $zero, $zero, bit_return
    nop
# END b_step
bit_no_feedback:
# BEGIN b_bits
    beq $zero, $zero, bit_return
    nop
# END b_bits
# BEGIN b_bytes
    beq $zero, $zero, bit_return
    nop
# END b_bytes
bit_return:
    jr $ra
    nop

crc32_table_init:
# BEGIN i_setup
    beq $zero, $zero, init_provisional
    nop
# END i_setup
init_entry:
# BEGIN i_entry
    beq $zero, $zero, init_provisional
    nop
# END i_entry
init_bit:
# BEGIN i_step
    beq $zero, $zero, init_provisional
    nop
# END i_step
init_no_feedback:
# BEGIN i_bits
    beq $zero, $zero, init_provisional
    nop
# END i_bits
# BEGIN i_store
    beq $zero, $zero, init_provisional
    nop
# END i_store
# BEGIN i_next
    beq $zero, $zero, init_provisional
    nop
# END i_next
    li $v0, 256
    jr $ra
    nop

init_provisional:
    move $v0, $zero
    jr $ra
    nop

crc32_table_update:
# BEGIN t_setup
    move $v0, $a2
    beq $zero, $zero, table_return
    nop
# END t_setup
table_next_byte:
# BEGIN t_gate
    beq $zero, $zero, table_return
    nop
# END t_gate
# BEGIN t_lookup
    beq $zero, $zero, table_return
    nop
# END t_lookup
# BEGIN t_fold
    beq $zero, $zero, table_return
    nop
# END t_fold
# BEGIN t_next
    beq $zero, $zero, table_return
    nop
# END t_next
table_return:
    jr $ra
    nop

crc32_file:
    daddiu $sp, $sp, -64
    sd $s0, 0($sp)
    sd $s1, 8($sp)
    sd $s2, 16($sp)
    sd $s3, 24($sp)
    sd $s4, 32($sp)
    sd $s5, 40($sp)
    sd $s6, 48($sp)
    sd $ra, 56($sp)
    move $s0, $a0
    move $s1, $a1
    move $s2, $a2
    move $s3, $a3
    li $s4, -1
    dli $s5, 0xffffffff
    move $s6, $zero
# BEGIN f_open
    beq $zero, $zero, file_provisional
    nop
# END f_open
# BEGIN f_select
    beq $zero, $zero, file_provisional
    nop
# END f_select
file_read:
# BEGIN f_read
    beq $zero, $zero, file_provisional
    nop
# END f_read
# BEGIN f_classify
    beq $zero, $zero, file_provisional
    nop
# END f_classify
# BEGIN f_update
    beq $zero, $zero, file_provisional
    nop
# END f_update
# BEGIN f_repeat
    beq $zero, $zero, file_provisional
    nop
# END f_repeat
file_finish:
# BEGIN f_finish
    beq $zero, $zero, file_provisional
    nop
# END f_finish
file_read_error:
    move $a0, $s4
    li $v0, 16
    syscall
    move $v0, $zero
    li $v1, 2
    beq $zero, $zero, file_return
    nop
file_open_error:
    move $v0, $zero
    li $v1, 1
file_return:
    ld $s0, 0($sp)
    ld $s1, 8($sp)
    ld $s2, 16($sp)
    ld $s3, 24($sp)
    ld $s4, 32($sp)
    ld $s5, 40($sp)
    ld $s6, 48($sp)
    ld $ra, 56($sp)
    daddiu $sp, $sp, 64
    jr $ra
    nop

file_provisional:
    bltz $s4, provisional_no_close
    nop
    move $a0, $s4
    li $v0, 16
    syscall
provisional_no_close:
    move $v0, $s5
    li $v1, 3
    beq $zero, $zero, file_return
    nop

save_raw_state:
    dla $t0, raw_result
    sw $v0, 0($t0)
state_saved:
    nop
