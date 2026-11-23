# Worked experiment: serialize a GIVEN final SHA state for abc.
# This program does not compute the rounds, padding or digest state.
.data
.align 2
final_state: .word 0xba7816bf, 0x8f01cfea, 0x414140de, 0x5dae2223, 0xb00361a3, 0x96177a9c, 0xb410ff61, 0xf20015ad
digest: .byte 165:32
.text
.globl main
main:
    dla $s0, final_state
    dla $s1, digest
    li $s2, 8
serialize_word:
    lwu $t0, 0($s0)
word_ready:
    srl $t1, $t0, 24
    sb $t1, 0($s1)
    srl $t1, $t0, 16
    sb $t1, 1($s1)
    srl $t1, $t0, 8
    sb $t1, 2($s1)
    sb $t0, 3($s1)
    daddiu $s0, $s0, 4
    daddiu $s1, $s1, 4
    addiu $s2, $s2, -1
    bnezc $s2, serialize_word
    nop
bytes_ready:
    li $v0, 10
    syscall
