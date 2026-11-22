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
    beqzc $t0, choose_bit
    nop
    li $t1, 1
    beqc $t0, $t1, choose_init
    nop
    li $t1, 2
    beqc $t0, $t1, choose_table
    nop
    dla $a0, path
    dla $a1, scratch
    dla $t0, buffer_capacity
    lw $a2, 0($t0)
    dla $t0, method
    lw $a3, 0($t0)
file_call:
    balc crc32_file
    nop
file_done:
    dla $t0, file_crc
    sw $v0, 0($t0)
    dla $t0, file_status
    sw $v1, 0($t0)
    bc results_ready
    nop
choose_bit:
    dla $t9, crc32_bit_update
    bc kernel_arguments
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
    bc kernel_call
    nop
choose_init:
    dla $a0, crc_table
    dla $t9, crc32_table_init
kernel_call:
    jalrc $t9
    nop
kernel_done:
    dla $t0, raw_result
    sw $v0, 0($t0)
results_ready:
    li $v0, 10
    syscall
function_return:
    bc results_ready
    nop

crc32_bit_update:
    move $v0, $a2
    dli $t3, 0xedb88320
bit_next_byte:
    beqzc $a1, bit_return
    nop
    lbu $t0, 0($a0)
    xor $v0, $v0, $t0
    li $t1, 8
bit_next_bit:
    andi $t2, $v0, 1
    dsrl $v0, $v0, 1
    beqzc $t2, bit_no_feedback
    nop
    xor $v0, $v0, $t3
bit_no_feedback:
    addiu $t1, $t1, -1
    bnezc $t1, bit_next_bit
    nop
    daddiu $a0, $a0, 1
    addiu $a1, $a1, -1
    bc bit_next_byte
    nop
bit_return:
    jrc $ra
    nop

crc32_table_init:
    move $t4, $a0
    move $t5, $zero
    dli $t3, 0xedb88320
init_entry:
    move $t0, $t5
    li $t1, 8
init_bit:
    andi $t2, $t0, 1
    dsrl $t0, $t0, 1
    beqzc $t2, init_no_feedback
    nop
    xor $t0, $t0, $t3
init_no_feedback:
    addiu $t1, $t1, -1
    bnezc $t1, init_bit
    nop
    sw $t0, 0($t4)
    daddiu $t4, $t4, 4
    addiu $t5, $t5, 1
    sltiu $t6, $t5, 256
    bnezc $t6, init_entry
    nop
    li $v0, 256
    jrc $ra
    nop

init_provisional:
    move $v0, $zero
    jrc $ra
    nop

crc32_table_update:
    move $v0, $a2
table_next_byte:
    beqzc $a1, table_return
    nop
    lbu $t0, 0($a0)
    xor $t0, $t0, $v0
    andi $t0, $t0, 255
    dsll $t0, $t0, 2
    daddu $t0, $a3, $t0
    lwu $t1, 0($t0)
    dsrl $v0, $v0, 8
    xor $v0, $v0, $t1
    daddiu $a0, $a0, 1
    addiu $a1, $a1, -1
    bc table_next_byte
    nop
table_return:
    jrc $ra
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
    li $a1, 0
    li $v0, 13
    syscall
    bltzc $v0, file_open_error
    nop
    move $s4, $v0
    dli $s5, 0xffffffff
    beqzc $s3, file_bit_method
    nop
    dla $a0, crc_table
    balc crc32_table_init
    nop
    dla $s6, crc32_table_update
    bc file_read
    nop
file_bit_method:
    dla $s6, crc32_bit_update
file_read:
    move $a0, $s4
    move $a1, $s1
    move $a2, $s2
    li $v0, 14
    syscall
    bltzc $v0, file_read_error
    nop
    beqzc $v0, file_finish
    nop
    move $a1, $v0
    move $a0, $s1
    move $a2, $s5
    dla $a3, crc_table
    jalrc $s6
    nop
    move $s5, $v0
    bc file_read
    nop
file_finish:
    dli $t0, 0xffffffff
    xor $s5, $s5, $t0
    move $a0, $s4
    li $v0, 16
    syscall
    move $v0, $s5
    move $v1, $zero
    bc file_return
    nop
file_read_error:
    move $a0, $s4
    li $v0, 16
    syscall
    move $v0, $zero
    li $v1, 2
    bc file_return
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
    jrc $ra
    nop

file_provisional:
    bltzc $s4, provisional_no_close
    nop
    move $a0, $s4
    li $v0, 16
    syscall
provisional_no_close:
    move $v0, $s5
    li $v1, 3
    bc file_return
    nop

save_raw_state:
    dla $t0, raw_result
    sw $v0, 0($t0)
state_saved:
    nop
