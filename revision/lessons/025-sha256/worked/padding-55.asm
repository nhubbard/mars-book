# Worked experiment: construct padding bytes without computing a digest.
# total is the original message length. A large value is a synthetic checkpoint;
# this program has not read or hashed that many bytes.
.data
.align 3
total: .dword 55
tail_count: .word 55
tail: .byte 128, 201, 18, 91, 164, 237, 54, 127, 200, 17, 90, 163, 236, 53, 126, 199, 16, 89, 162, 235, 52, 125, 198, 15, 88, 161, 234, 51, 124, 197, 14, 87, 160, 233, 50, 123, 196, 13, 86, 159, 232, 49, 122, 195, 12, 85, 158, 231, 48, 121, 194, 11, 84, 157, 230
padded: .byte 165:128
.text
.globl main
main:
    dla $t0, total
    ld $s2, 0($t0)
    dla $t0, tail_count
    lwu $s1, 0($t0)
    dla $s0, tail
    dla $s4, padded
    move $t0, $s4
    move $t1, $s1
copy_tail:
    beqzc $t1, append_marker
    nop
    lbu $t2, 0($s0)
    sb $t2, 0($t0)
    daddiu $s0, $s0, 1
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, -1
    bc copy_tail
    nop
append_marker:
    li $t2, 128
    sb $t2, 0($t0)
    daddiu $t0, $t0, 1
marker_ready:
    daddiu $t1, $s1, 1
    sltiu $t3, $t1, 57
    beqzc $t3, two_blocks
    nop
    li $s3, 64
    bc chosen_size
    nop
two_blocks:
    li $s3, 128
chosen_size:
    daddiu $t2, $s3, -8
zero_padding:
    beqc $t1, $t2, length_bytes
    nop
    sb $zero, 0($t0)
    daddiu $t0, $t0, 1
    daddiu $t1, $t1, 1
    bc zero_padding
    nop
length_bytes:
    dsll $t3, $s2, 3
    li $t4, 56
    li $t5, 8
write_length:
    dsrlv $t6, $t3, $t4
    sb $t6, 0($t0)
    daddiu $t0, $t0, 1
    daddiu $t4, $t4, -8
    addiu $t5, $t5, -1
    bnezc $t5, write_length
    nop
padding_ready:
    li $v0, 10
    syscall
