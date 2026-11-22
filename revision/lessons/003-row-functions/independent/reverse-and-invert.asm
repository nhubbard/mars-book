.text
.globl main
main:
    dli $t0, -16
    and $sp, $sp, $t0
    li $a0, 0x16
    balc reverse_and_invert
after_invert:
    move $s0, $v0
    li $a0, 0x81
    balc reverse_and_invert
after_repeat:
    move $s1, $v0
    li $v0, 10
    syscall

# Write reverse_and_invert. Use reverse_byte; return the inverted low-byte result
# in $v0, preserve $s0..$s7, $fp, $gp and $sp, and return through the incoming $ra.
# The low eight bits of any $a0 value are the input. Upper bits do not affect it.
reverse_and_invert:
    break

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
