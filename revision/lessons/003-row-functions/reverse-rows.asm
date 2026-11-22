.data
.align 2
input_rows: .word 0x16, 0x81
results:    .word 0, 0
rows_guard: .word 0x13579bdf

.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    la $s0, results
    la $t0, input_rows
    lw $a0, 0($t0)
    lw $a1, 4($t0)
main_call:
    balc reverse_pair
after_pair:
    sw $v0, 0($s0)
    sw $v1, 4($s0)
done:
    li $v0, 10
    syscall

reverse_pair:
    daddiu $sp, $sp, -32
    sd $ra, 24($sp)
    sd $s0, 16($sp)
    sd $s1, 8($sp)
pair_frame_ready:
    move $s0, $a1
    balc reverse_byte
pair_first_ready:
    move $s1, $v0
    move $a0, $s0
pair_second_call:
    balc reverse_byte
pair_second_ready:
    move $v1, $v0
    move $v0, $s1
    ld $s1, 8($sp)
    ld $s0, 16($sp)
    ld $ra, 24($sp)
    daddiu $sp, $sp, 32
pair_return:
    jrc $ra

reverse_byte:
    andi $t0, $a0, 0xff
    move $v0, $zero
    li $t1, 8
reverse_bit:
    sll $v0, $v0, 1
    andi $t2, $t0, 1
    or $v0, $v0, $t2
    srl $t0, $t0, 1
    addiu $t1, $t1, -1
    bnezc $t1, reverse_bit
    nop
reverse_byte_return:
    jrc $ra
