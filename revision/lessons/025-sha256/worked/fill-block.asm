# Worked experiment: fill the last byte of a partial block.
# Stop BEFORE compression. used=64 is a provisional state, not a valid update return.
# The total counts the full two-byte request once, before either byte is copied.
# A real update must compress, clear used, and then consume the remaining byte.
.data
.align 3
context: .word 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    .dword 63
    .word 63, 0
    .byte 128, 201, 18, 91, 164, 237, 54, 127, 200, 17, 90, 163, 236, 53, 126, 199, 16, 89, 162, 235, 52, 125, 198, 15, 88, 161, 234, 51, 124, 197, 14, 87, 160, 233, 50, 123, 196, 13, 86, 159, 232, 49, 122, 195, 12, 85, 158, 231, 48, 121, 194, 11, 84, 157, 230, 47, 120, 193, 10, 83, 156, 229, 46
    .byte 165:257
source: .byte 128,129
.text
.globl main
main:
    dla $s0, context
    dla $s4, source
    li $s1, 2
    ld $t0, 32($s0)
    daddu $t0, $t0, $s1
    sd $t0, 32($s0)
    lwu $s2, 40($s0)
    li $t0, 64
    subu $s3, $t0, $s2
fragment_size:
    lbu $t1, 0($s4)
    daddu $t2, $s0, $s2
    sb $t1, 48($t2)
    addiu $s2, $s2, 1
    sw $s2, 40($s0)
    daddiu $s4, $s4, 1
    daddiu $s1, $s1, -1
block_ready:
    li $v0, 10
    syscall
